package id.jagakeluarga.salesfunnel.backup

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LocalBackupManager {
    suspend fun exportDatabase(resolver: ContentResolver, databaseFile: File, destination: Uri) = withContext(Dispatchers.IO) {
        check(databaseFile.exists()) { "File database belum tersedia" }
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
            temporary.copyTo(databaseFile, overwrite = true)
        } finally {
            temporary.delete()
        }
    }
}
