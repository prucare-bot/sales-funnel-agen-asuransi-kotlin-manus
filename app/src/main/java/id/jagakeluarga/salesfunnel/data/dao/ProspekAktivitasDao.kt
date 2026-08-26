package id.jagakeluarga.salesfunnel.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.jagakeluarga.salesfunnel.data.entity.ProspekAktivitas
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspekAktivitasDao {
    @Query("SELECT * FROM prospek_aktivitas WHERE prospekId = :prospekId ORDER BY dibuatPada DESC")
    fun observeByProspek(prospekId: String): Flow<List<ProspekAktivitas>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(aktivitas: ProspekAktivitas)

    @Query("DELETE FROM prospek_aktivitas WHERE prospekId = :prospekId")
    suspend fun deleteByProspek(prospekId: String)
}
