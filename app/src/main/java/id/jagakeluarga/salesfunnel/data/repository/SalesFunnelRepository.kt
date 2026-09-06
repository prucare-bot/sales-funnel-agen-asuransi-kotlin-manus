package id.jagakeluarga.salesfunnel.data.repository

import id.jagakeluarga.salesfunnel.data.AppDatabase
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.ProspekStatusHistory
import id.jagakeluarga.salesfunnel.data.entity.ProspekAktivitas
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class SalesFunnelRepository(private val db: AppDatabase) {
    val prospekList: Flow<List<Prospek>> = db.prospekDao().observeAll()
    val agendaList: Flow<List<Agenda>> = db.agendaDao().observeAll()
    val nasabahList: Flow<List<Nasabah>> = db.nasabahDao().observeAll()
    /** Seluruh riwayat perpindahan tahap lintas prospek, untuk dashboard analitik. */
    val statusHistoryAll: Flow<List<ProspekStatusHistory>> = db.prospekStatusHistoryDao().observeAll()

    suspend fun getProspek(id: String) = db.prospekDao().getById(id)

    suspend fun saveProspek(prospek: Prospek) = db.withTransaction {
        val sebelumnya = db.prospekDao().getById(prospek.id)
        db.prospekDao().upsert(prospek)
        if (sebelumnya == null || sebelumnya.tahap != prospek.tahap) {
                db.prospekStatusHistoryDao().insert(
                    ProspekStatusHistory(prospekId = prospek.id, tahap = prospek.tahap),
                )
                db.prospekAktivitasDao().insert(
                    ProspekAktivitas(
                        prospekId = prospek.id,
                        jenis = "STATUS",
                        judul = if (sebelumnya == null) "Prospek dibuat" else "Tahap berubah menjadi ${prospek.tahap.label}",
                        catatan = prospek.catatan,
                    ),
                )
        }
    }

    suspend fun deleteProspek(prospek: Prospek) = db.withTransaction {
        db.prospekStatusHistoryDao().deleteByProspek(prospek.id)
        db.prospekAktivitasDao().deleteByProspek(prospek.id)
        db.prospekDao().delete(prospek)
    }

    fun statusHistoryForProspek(prospekId: String): Flow<List<ProspekStatusHistory>> =
        db.prospekStatusHistoryDao().observeByProspek(prospekId)

    fun aktivitasForProspek(prospekId: String): Flow<List<ProspekAktivitas>> =
        db.prospekAktivitasDao().observeByProspek(prospekId)

    suspend fun saveAktivitas(aktivitas: ProspekAktivitas) = db.withTransaction {
        db.prospekAktivitasDao().insert(aktivitas)

        val prospek = db.prospekDao().getById(aktivitas.prospekId) ?: return@withTransaction
        val tahapBerikutnya = when (prospek.tahap) {
            TahapPipeline.PROSPEK -> TahapPipeline.KUALIFIKASI
            TahapPipeline.KUALIFIKASI -> TahapPipeline.PRESENTASI
            TahapPipeline.PRESENTASI -> TahapPipeline.PROPOSAL
            TahapPipeline.PROPOSAL -> TahapPipeline.CLOSING
            TahapPipeline.CLOSING -> null
        }
        if (aktivitas.jenis == "TELEPON" || aktivitas.jenis == "PERTEMUAN") {
            if (tahapBerikutnya != null) {
                val sekarang = System.currentTimeMillis()
                db.prospekDao().upsert(
                    prospek.copy(tahap = tahapBerikutnya, diperbaruiPada = sekarang),
                )
                db.prospekStatusHistoryDao().insert(
                    ProspekStatusHistory(prospekId = prospek.id, tahap = tahapBerikutnya, diubahPada = sekarang),
                )
                db.prospekAktivitasDao().insert(
                    ProspekAktivitas(
                        prospekId = prospek.id,
                        jenis = "STATUS",
                        judul = "Tahap otomatis menjadi ${tahapBerikutnya.label}",
                        catatan = "Dipicu oleh ${aktivitas.jenis.lowercase()}: ${aktivitas.judul}",
                        dibuatPada = sekarang,
                    ),
                )
            }
        }
    }

    suspend fun convertProspekToNasabah(prospek: Prospek, produk: String, nomorPolis: String?): ConversionResult =
        db.withTransaction {
            val existingByProspek = db.nasabahDao().getByProspekAsalId(prospek.id)
            if (existingByProspek != null) return@withTransaction ConversionResult.AlreadyConverted(existingByProspek)

            val nomorTelepon = prospek.nomorTelepon?.trim()?.takeIf { it.isNotEmpty() }
            val nomorTeleponNormal = nomorTelepon?.filter(Char::isDigit)
            val existingByPhone = nomorTeleponNormal
                ?.takeIf { it.isNotEmpty() }
                ?.let { nomor ->
                    db.nasabahDao().getAllOnce().firstOrNull { existing ->
                        existing.nomorTelepon?.filter(Char::isDigit) == nomor
                    }
                }
            if (existingByPhone != null) return@withTransaction ConversionResult.DuplicatePhone(existingByPhone)

            val nasabah = Nasabah(
                prospekAsalId = prospek.id,
                nama = prospek.nama,
                nomorTelepon = nomorTelepon,
                produk = produk.trim(),
                nomorPolis = nomorPolis?.trim()?.takeIf { it.isNotEmpty() },
            )
            db.nasabahDao().upsert(nasabah)
            val closingProspek = if (prospek.tahap == TahapPipeline.CLOSING) {
                prospek
            } else {
                prospek.copy(
                    tahap = TahapPipeline.CLOSING,
                    diperbaruiPada = System.currentTimeMillis(),
                )
            }
            db.prospekDao().upsert(closingProspek)
            if (closingProspek.tahap != prospek.tahap) {
                db.prospekStatusHistoryDao().insert(
                    ProspekStatusHistory(prospekId = prospek.id, tahap = closingProspek.tahap),
                )
                db.prospekAktivitasDao().insert(
                    ProspekAktivitas(
                        prospekId = prospek.id,
                        jenis = "KONVERSI",
                        judul = "Prospek dikonversi menjadi nasabah",
                        catatan = "Produk: ${produk.trim()}",
                    ),
                )
            }
            ConversionResult.Created(nasabah)
        }

    sealed interface ConversionResult {
        data class Created(val nasabah: Nasabah) : ConversionResult
        data class AlreadyConverted(val nasabah: Nasabah) : ConversionResult
        data class DuplicatePhone(val nasabah: Nasabah) : ConversionResult
    }

    fun agendaForProspek(prospekId: String): Flow<List<Agenda>> =
        db.agendaDao().observeByProspek(prospekId)
    suspend fun saveAgenda(agenda: Agenda) = db.withTransaction {
        val sebelumnya = db.agendaDao().getById(agenda.id)
        db.agendaDao().upsert(agenda)
        if (sebelumnya == null) {
            db.prospekAktivitasDao().insert(
                ProspekAktivitas(
                    prospekId = agenda.prospekId,
                    jenis = "FOLLOW_UP",
                    judul = "Follow-up: ${agenda.judul}",
                    catatan = agenda.catatan,
                    dibuatPada = agenda.waktuMulai,
                ),
            )
        }
    }
    suspend fun deleteAgenda(agenda: Agenda) = db.agendaDao().delete(agenda)

    suspend fun getNasabah(id: String) = db.nasabahDao().getById(id)
    suspend fun saveNasabah(nasabah: Nasabah) = db.nasabahDao().upsert(nasabah)
    suspend fun deleteNasabah(nasabah: Nasabah) = db.nasabahDao().delete(nasabah)
}
