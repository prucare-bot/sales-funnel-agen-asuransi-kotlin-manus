package id.jagakeluarga.salesfunnel.data.dao

import androidx.room.*
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaDao {
    @Query("SELECT * FROM agenda ORDER BY waktuMulai ASC")
    fun observeAll(): Flow<List<Agenda>>

    @Query("SELECT * FROM agenda WHERE prospekId = :prospekId ORDER BY waktuMulai ASC")
    fun observeByProspek(prospekId: String): Flow<List<Agenda>>

    @Query("SELECT * FROM agenda WHERE id = :id")
    suspend fun getById(id: String): Agenda?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(agenda: Agenda)

    @Delete
    suspend fun delete(agenda: Agenda)
}
