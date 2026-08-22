package id.jagakeluarga.salesfunnel.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "nasabah")
data class Nasabah(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val prospekAsalId: String? = null,
    val nama: String,
    val nomorTelepon: String? = null,
    val produk: String,
    val nomorPolis: String? = null,
    val premi: Long? = null,
    val tanggalMulai: Long? = null,
    val tanggalJatuhTempo: Long? = null,
    val catatan: String? = null,
    val dibuatPada: Long = System.currentTimeMillis(),
)
