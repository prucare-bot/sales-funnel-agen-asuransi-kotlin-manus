package id.jagakeluarga.salesfunnel.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import id.jagakeluarga.salesfunnel.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LocalBackupManager {
    fun latestAutomaticBackup(context: Context): File? =
        File(context.filesDir, "automatic_backups")
            .listFiles { file -> file.isFile && file.name.startsWith("sales_funnel_auto_") && file.extension == "db" }
            ?.maxByOrNull { it.lastModified() }

    suspend fun exportDatabase(resolver: ContentResolver, databaseFile: File, destination: Uri) = withContext(Dispatchers.IO) {
        AppDatabase.checkpoint()
        check(databaseFile.exists() && databaseFile.length() > 0L) { "File database belum tersedia" }
        resolver.openOutputStream(destination)?.use { output ->
            databaseFile.inputStream().use { input -> input.copyTo(output) }
        } ?: error("Tidak dapat membuka file tujuan")
    }

    suspend fun importDatabase(resolver: ContentResolver, source: Uri, databaseFile: File) = withContext(Dispatchers.IO) {
        val temporary = File(databaseFile.parentFile, "sales_funnel_restore.tmp")
        try {
            resolver.openInputStream(source)?.use { input ->
                temporary.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Tidak dapat membuka file backup")
            check(temporary.length() > 0L) { "File backup kosong" }
            databaseFile.parentFile?.mkdirs()
            File("${databaseFile.path}-wal").delete()
            File("${databaseFile.path}-shm").delete()
            temporary.copyTo(databaseFile, overwrite = true)
        } finally {
            temporary.delete()
        }
    }
}
