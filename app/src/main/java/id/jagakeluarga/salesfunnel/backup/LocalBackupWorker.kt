package id.jagakeluarga.salesfunnel.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import id.jagakeluarga.salesfunnel.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalBackupWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        runCatching {
            val databaseFile = applicationContext.getDatabasePath(DATABASE_NAME)
            if (!databaseFile.exists() || databaseFile.length() == 0L) return@runCatching

            AppDatabase.getInstance(applicationContext)
            AppDatabase.checkpoint()
            val backupDir = File(applicationContext.filesDir, BACKUP_DIRECTORY).apply { mkdirs() }
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val temporary = File(backupDir, "$BACKUP_PREFIX$stamp.db.tmp")
            val destination = File(backupDir, "$BACKUP_PREFIX$stamp.db")
            try {
                databaseFile.inputStream().use { input ->
                    temporary.outputStream().use { output -> input.copyTo(output) }
                }
                check(temporary.length() > 0L) { "Backup otomatis kosong" }
                check(isValidDatabase(temporary)) { "Backup otomatis tidak valid" }
                check(temporary.renameTo(destination)) { "Tidak dapat menyimpan backup otomatis" }
                pruneOldBackups(backupDir)
            } finally {
                temporary.delete()
            }
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    private fun isValidDatabase(file: File): Boolean {
        val database = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        return database.use { db ->
            db.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                cursor.moveToFirst() && cursor.getString(0).equals("ok", ignoreCase = true)
            }
        }
    }

    private fun pruneOldBackups(directory: File) {
        directory.listFiles { file -> file.isFile && file.name.startsWith(BACKUP_PREFIX) && file.extension == "db" }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_BACKUPS)
            ?.forEach { it.delete() }
    }

    companion object {
        const val DATABASE_NAME = "sales_funnel.db"
        const val BACKUP_DIRECTORY = "automatic_backups"
        const val BACKUP_PREFIX = "sales_funnel_auto_"
        const val MAX_BACKUPS = 5
    }
}
