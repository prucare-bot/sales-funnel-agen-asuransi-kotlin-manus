package id.jagakeluarga.salesfunnel.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "prospek_status_history",
    indices = [Index(value = ["prospekId"])],
)
data class ProspekStatusHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prospekId: String,
    val tahap: TahapPipeline,
    val diubahPada: Long = System.currentTimeMillis(),
)
