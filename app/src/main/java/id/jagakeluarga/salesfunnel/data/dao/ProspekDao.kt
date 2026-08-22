package id.jagakeluarga.salesfunnel.data.dao

import androidx.room.*
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspekDao {
    @Query("SELECT * FROM prospek ORDER BY diperbaruiPada DESC")
    fun observeAll(): Flow<List<Prospek>>

    @Query("SELECT * FROM prospek WHERE id = :id")
    suspend fun getById(id: String): Prospek?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prospek: Prospek)

    @Delete
    suspend fun delete(prospek: Prospek)
}
