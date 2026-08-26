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
import id.jagakeluarga.salesfunnel.data.dao.ProspekStatusHistoryDao
import id.jagakeluarga.salesfunnel.data.dao.ProspekAktivitasDao
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.ProspekStatusHistory
import id.jagakeluarga.salesfunnel.data.entity.ProspekAktivitas

@Database(
    entities = [Prospek::class, Agenda::class, Nasabah::class, ProspekStatusHistory::class, ProspekAktivitas::class],
    version = 6,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prospekDao(): ProspekDao
    abstract fun agendaDao(): AgendaDao
    abstract fun nasabahDao(): NasabahDao
    abstract fun prospekStatusHistoryDao(): ProspekStatusHistoryDao
    abstract fun prospekAktivitasDao(): ProspekAktivitasDao

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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prospek ADD COLUMN kotaDomisili TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS prospek_aktivitas (
                        id TEXT NOT NULL PRIMARY KEY,
                        prospekId TEXT NOT NULL,
                        jenis TEXT NOT NULL,
                        judul TEXT NOT NULL,
                        catatan TEXT,
                        dibuatPada INTEGER NOT NULL,
                        FOREIGN KEY(prospekId) REFERENCES prospek(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_prospek_aktivitas_prospekId ON prospek_aktivitas(prospekId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS prospek_status_history (
                        id TEXT NOT NULL PRIMARY KEY,
                        prospekId TEXT NOT NULL,
                        tahap TEXT NOT NULL,
                        diubahPada INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_prospek_status_history_prospekId ON prospek_status_history(prospekId)")
                db.execSQL(
                    "INSERT OR IGNORE INTO prospek_status_history (id, prospekId, tahap, diubahPada) " +
                        "SELECT 'initial-' || id, id, tahap, dibuatPada FROM prospek",
                )
            }
        }

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_funnel.db",
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                ).build().also { instance = it }
            }

        /** Flushes Room's WAL so file-based backups include the latest committed rows. */
        fun checkpoint() {
            instance?.openHelper?.writableDatabase?.query("PRAGMA wal_checkpoint(FULL)")?.use { }
        }

        /** Closes the active Room connection before an on-disk database replacement. */
        fun closeInstance() {
            synchronized(this) {
                instance?.close()
                instance = null
            }
        }
    }
}
