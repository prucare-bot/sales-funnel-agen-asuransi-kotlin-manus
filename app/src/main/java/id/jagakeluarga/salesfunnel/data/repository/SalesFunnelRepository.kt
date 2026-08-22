package id.jagakeluarga.salesfunnel.data.repository

import id.jagakeluarga.salesfunnel.data.AppDatabase
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import kotlinx.coroutines.flow.Flow

class SalesFunnelRepository(private val db: AppDatabase) {
    val prospekList: Flow<List<Prospek>> = db.prospekDao().observeAll()
    val agendaList: Flow<List<Agenda>> = db.agendaDao().observeAll()
    val nasabahList: Flow<List<Nasabah>> = db.nasabahDao().observeAll()

    suspend fun getProspek(id: String) = db.prospekDao().getById(id)
    suspend fun saveProspek(prospek: Prospek) = db.prospekDao().upsert(prospek)
    suspend fun deleteProspek(prospek: Prospek) = db.prospekDao().delete(prospek)

    fun agendaForProspek(prospekId: String): Flow<List<Agenda>> =
        db.agendaDao().observeByProspek(prospekId)
    suspend fun saveAgenda(agenda: Agenda) = db.agendaDao().upsert(agenda)
    suspend fun deleteAgenda(agenda: Agenda) = db.agendaDao().delete(agenda)

    suspend fun getNasabah(id: String) = db.nasabahDao().getById(id)
    suspend fun saveNasabah(nasabah: Nasabah) = db.nasabahDao().upsert(nasabah)
    suspend fun deleteNasabah(nasabah: Nasabah) = db.nasabahDao().delete(nasabah)
}
