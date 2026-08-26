package id.jagakeluarga.salesfunnel.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "prospek_aktivitas",
    foreignKeys = [
        ForeignKey(
            entity = Prospek::class,
            parentColumns = ["id"],
            childColumns = ["prospekId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["prospekId"])],
)
data class ProspekAktivitas(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prospekId: String,
    val jenis: String,
    val judul: String,
    val catatan: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
)
