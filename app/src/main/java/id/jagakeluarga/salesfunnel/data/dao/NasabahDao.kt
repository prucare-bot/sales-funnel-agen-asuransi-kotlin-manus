package id.jagakeluarga.salesfunnel.data.dao

import androidx.room.*
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import kotlinx.coroutines.flow.Flow

@Dao
interface NasabahDao {
    @Query("SELECT * FROM nasabah ORDER BY nama ASC")
    fun observeAll(): Flow<List<Nasabah>>

    @Query("SELECT * FROM nasabah WHERE id = :id")
    suspend fun getById(id: String): Nasabah?

    @Query("SELECT * FROM nasabah WHERE prospekAsalId = :prospekId LIMIT 1")
    suspend fun getByProspekAsalId(prospekId: String): Nasabah?

    @Query("SELECT * FROM nasabah WHERE nomorTelepon = :nomorTelepon LIMIT 1")
    suspend fun getByNomorTelepon(nomorTelepon: String): Nasabah?

    @Query("SELECT * FROM nasabah")
    suspend fun getAllOnce(): List<Nasabah>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(nasabah: Nasabah)

    @Delete
    suspend fun delete(nasabah: Nasabah)
}
