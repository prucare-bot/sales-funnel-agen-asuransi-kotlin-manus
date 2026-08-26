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

    suspend fun exportDatabase(resolver: ContentResolver, databaseFile: File, destination: Uri, pin: String) = withContext(Dispatchers.IO) {
        AppDatabase.checkpoint()
        check(databaseFile.exists() && databaseFile.length() > 0L) { "File database belum tersedia" }
        val plain = databaseFile.readBytes()
        val encrypted = BackupCrypto.encrypt(plain, pin)
        resolver.openOutputStream(destination)?.use { output ->
            output.write(encrypted)
        } ?: error("Tidak dapat membuka file tujuan")
    }

    suspend fun importDatabase(resolver: ContentResolver, source: Uri, databaseFile: File, pin: String) = withContext(Dispatchers.IO) {
        val temporary = File(databaseFile.parentFile, "sales_funnel_restore.tmp")
        try {
            val input = resolver.openInputStream(source)?.use { it.readBytes() }
                ?: error("Tidak dapat membuka file backup")
            val plain = if (BackupCrypto.isEncrypted(input)) BackupCrypto.decrypt(input, pin) else input
            temporary.outputStream().use { output -> output.write(plain) }
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
