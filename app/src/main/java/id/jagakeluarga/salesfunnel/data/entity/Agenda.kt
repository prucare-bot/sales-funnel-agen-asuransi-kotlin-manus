package id.jagakeluarga.salesfunnel.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class JenisAgenda(val label: String) {
    TELEPON("Telepon"),
    PERTEMUAN("Pertemuan"),
    KIRIM_PROPOSAL("Kirim Proposal"),
    LAINNYA("Lainnya"),
}

@Entity(
    tableName = "agenda",
    foreignKeys = [
        ForeignKey(
            entity = Prospek::class,
            parentColumns = ["id"],
            childColumns = ["prospekId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("prospekId")],
)
data class Agenda(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prospekId: String,
    val judul: String,
    val jenis: JenisAgenda = JenisAgenda.LAINNYA,
    val waktuMulai: Long,
    val selesai: Boolean = false,
    /** Jeda pengingat sebelum janji dalam jam; pilihan UI: 24 atau 4. */
    val reminderOffsetHours: Int = 24,
    val catatan: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
)
