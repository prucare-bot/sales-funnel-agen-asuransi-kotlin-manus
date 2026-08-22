package id.jagakeluarga.salesfunnel.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.jagakeluarga.salesfunnel.data.dao.AgendaDao
import id.jagakeluarga.salesfunnel.data.dao.NasabahDao
import id.jagakeluarga.salesfunnel.data.dao.ProspekDao
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek

@Database(
    entities = [Prospek::class, Agenda::class, Nasabah::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prospekDao(): ProspekDao
    abstract fun agendaDao(): AgendaDao
    abstract fun nasabahDao(): NasabahDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_funnel.db",
                ).build().also { instance = it }
            }
    }
}
