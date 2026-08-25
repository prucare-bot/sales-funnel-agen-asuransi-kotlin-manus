package id.jagakeluarga.salesfunnel.backup

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import id.jagakeluarga.salesfunnel.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File as JavaFile

/**
 * Backs up / restores the local Room database file to the user's hidden
 * "App Data" folder on their own Google Drive (free, no server needed).
 * Only this app can see files stored there.
 */
class GoogleDriveBackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_FILE_NAME = "sales_funnel_backup.db"
    }

    fun signInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun signInIntent(): Intent = signInClient().signInIntent

    fun currentAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    fun hasDrivePermission(account: GoogleSignInAccount): Boolean =
        GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_APPDATA))

    fun signOut() {
        signInClient().signOut()
    }

    private fun driveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = Account(account.email ?: error("Akun Google tidak punya email"), "com.google")
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential,
        ).setApplicationName("Sales Funnel Agen Asuransi").build()
    }

    /** Uploads (or overwrites) the local DB file to appDataFolder. Returns backup timestamp millis. */
    suspend fun backupNow(dbFile: JavaFile): Long = withContext(Dispatchers.IO) {
        AppDatabase.checkpoint()
        check(dbFile.exists() && dbFile.length() > 0L) { "File database belum siap untuk backup" }
        val account = currentAccount() ?: error("Belum sign in ke Google")
        check(hasDrivePermission(account)) { "Akun Google belum memberi izin Google Drive. Tekan login Google lagi dan izinkan akses Drive." }
        val drive = driveService(account)

        val existingId = findBackupFileId(drive)
        val mediaContent = com.google.api.client.http.FileContent("application/x-sqlite3", dbFile)

        if (existingId != null) {
            drive.files().update(existingId, DriveFile(), mediaContent).execute()
        } else {
            val metadata = DriveFile().apply {
                name = BACKUP_FILE_NAME
                parents = listOf("appDataFolder")
            }
            drive.files().create(metadata, mediaContent).execute()
        }
        System.currentTimeMillis()
    }

    /** Downloads the backup from appDataFolder and overwrites the local DB file. Returns true if a backup existed. */
    suspend fun restoreNow(dbFile: JavaFile): Boolean = withContext(Dispatchers.IO) {
        val account = currentAccount() ?: error("Belum sign in ke Google")
        check(hasDrivePermission(account)) { "Akun Google belum memberi izin Google Drive. Tekan login Google lagi dan izinkan akses Drive." }
        val drive = driveService(account)
        val fileId = findBackupFileId(drive) ?: return@withContext false

        val temporary = JavaFile("${dbFile.path}.drive-restore.tmp")
        try {
            temporary.outputStream().use { out ->
                drive.files().get(fileId).executeMediaAndDownloadTo(out)
            }
            check(temporary.length() > 0L) { "File backup Drive kosong" }
            AppDatabase.closeInstance()
            dbFile.parentFile?.mkdirs()
            JavaFile("${dbFile.path}-wal").delete()
            JavaFile("${dbFile.path}-shm").delete()
            temporary.copyTo(dbFile, overwrite = true)
            true
        } finally {
            temporary.delete()
        }
    }

    private fun findBackupFileId(drive: Drive): String? {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME'")
            .setFields("files(id, name)")
            .execute()
        return result.files?.firstOrNull()?.id
    }
}
