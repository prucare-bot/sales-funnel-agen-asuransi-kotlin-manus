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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(nasabah: Nasabah)

    @Delete
    suspend fun delete(nasabah: Nasabah)
}
