package id.jagakeluarga.salesfunnel.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TahapPipeline(val label: String) {
    PROSPEK("Prospek"),
    KUALIFIKASI("Kualifikasi"),
    PRESENTASI("Presentasi"),
    PROPOSAL("Proposal"),
    CLOSING("Closing"),
}

@Entity(tableName = "prospek")
data class Prospek(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val nomorTelepon: String? = null,
    val email: String? = null,
    val sumberProspek: String? = null,
    val tahap: TahapPipeline = TahapPipeline.PROSPEK,
    val estimasiPremi: Long? = null,
    val catatan: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
    val diperbaruiPada: Long = System.currentTimeMillis(),
)
