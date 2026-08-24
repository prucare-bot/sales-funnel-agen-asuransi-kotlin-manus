package id.jagakeluarga.salesfunnel.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prospekDao(): ProspekDao
    abstract fun agendaDao(): AgendaDao
    abstract fun nasabahDao(): NasabahDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE agenda ADD COLUMN reminderOffsetHours INTEGER NOT NULL DEFAULT 24")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE nasabah ADD COLUMN tanggalLahir INTEGER")
            }
        }

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_funnel.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
