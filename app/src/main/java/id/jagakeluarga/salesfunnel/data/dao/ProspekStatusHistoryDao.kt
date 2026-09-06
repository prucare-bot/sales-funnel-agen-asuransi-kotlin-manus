package id.jagakeluarga.salesfunnel.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.jagakeluarga.salesfunnel.data.entity.ProspekStatusHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspekStatusHistoryDao {
    @Query("SELECT * FROM prospek_status_history WHERE prospekId = :prospekId ORDER BY diubahPada DESC")
    fun observeByProspek(prospekId: String): Flow<List<ProspekStatusHistory>>

    /** Seluruh riwayat perpindahan tahap lintas prospek, dipakai untuk dashboard analitik. */
    @Query("SELECT * FROM prospek_status_history ORDER BY prospekId ASC, diubahPada ASC")
    fun observeAll(): Flow<List<ProspekStatusHistory>>

    @Query("SELECT * FROM prospek_status_history WHERE prospekId = :prospekId ORDER BY diubahPada ASC LIMIT 1")
    suspend fun getFirstByProspek(prospekId: String): ProspekStatusHistory?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(history: ProspekStatusHistory)

    @Query("DELETE FROM prospek_status_history WHERE prospekId = :prospekId")
    suspend fun deleteByProspek(prospekId: String)
}
