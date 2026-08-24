package id.jagakeluarga.salesfunnel.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import id.jagakeluarga.salesfunnel.backup.GoogleDriveBackupManager
import id.jagakeluarga.salesfunnel.backup.LocalBackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(dbFilePath: String, onNamaUserChanged: (String) -> Unit = {}) {
    val context = LocalContext.current
    val manager = remember { GoogleDriveBackupManager(context) }
    val scope = rememberCoroutineScope()

    var account by remember { mutableStateOf(manager.currentAccount()) }
    var status by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("sales_funnel_settings", 0) }
    var namaUser by remember { mutableStateOf(preferences.getString("nama_user", "Densus") ?: "Densus") }
    var lastBackup by remember { mutableStateOf<Long?>(null) }

    val localBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            isBusy = true
            scope.launch {
                try {
                    LocalBackupManager.exportDatabase(context.contentResolver, java.io.File(dbFilePath), uri)
                    status = "Backup lokal berhasil disimpan."
                } catch (e: Exception) {
                    status = "Backup lokal gagal: ${e.message}"
                } finally { isBusy = false }
            }
        }
    }
    val localRestoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isBusy = true
            scope.launch {
                try {
                    LocalBackupManager.importDatabase(context.contentResolver, uri, java.io.File(dbFilePath))
                    status = "Restore lokal berhasil. Tutup paksa lalu buka kembali aplikasi agar data dimuat."
                } catch (e: Exception) {
                    status = "Restore lokal gagal: ${e.message}"
                } finally { isBusy = false }
            }
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                account = task.result
                status = "Berhasil masuk sebagai ${account?.email}"
            } catch (e: Exception) {
                status = "Gagal masuk: ${e.message}"
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Pengaturan & Backup") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Profil User", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = namaUser,
                onValueChange = { namaUser = it },
                label = { Text("Nama user") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = namaUser.isNotBlank(),
                onClick = {
                    val nama = namaUser.trim()
                    preferences.edit().putString("nama_user", nama).apply()
                    onNamaUserChanged(nama)
                    status = "Nama user berhasil disimpan."
                },
            ) { Text("Simpan nama user") }

            Text("Backup lokal", style = MaterialTheme.typography.titleMedium)
            Text(
                "Simpan file backup ke memori HP, kartu SD, atau folder cloud yang dipilih melalui pemilih file Android.",
                style = MaterialTheme.typography.bodySmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    enabled = !isBusy,
                    onClick = { localBackupLauncher.launch("sales_funnel_backup.db") },
                ) { Text("Simpan ke perangkat") }
                OutlinedButton(
                    enabled = !isBusy,
                    onClick = { localRestoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) },
                ) { Text("Pulihkan file") }
            }

            Text("Backup ke Google Drive", style = MaterialTheme.typography.titleMedium)
            Text(
                "Data (Pipeline, Prospek, Agenda, Nasabah) tersimpan di HP. " +
                    "Backup ke Google Drive kamu sendiri (folder tersembunyi, hanya bisa diakses app ini) " +
                    "supaya tidak hilang kalau ganti HP.",
                style = MaterialTheme.typography.bodySmall,
            )

            if (account == null) {
                Button(onClick = { signInLauncher.launch(manager.signInIntent()) }) {
                    Text("Masuk dengan Google")
                }
            } else {
                Text("Masuk sebagai: ${account?.email}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !isBusy,
                        onClick = {
                            isBusy = true
                            scope.launch {
                                try {
                                    val dbFile = java.io.File(dbFilePath)
                                    val time = manager.backupNow(dbFile)
                                    lastBackup = time
                                    status = "Backup berhasil."
                                } catch (e: Exception) {
                                    status = "Backup gagal: ${e.message}"
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                    ) { Text("Backup Sekarang") }

                    OutlinedButton(
                        enabled = !isBusy,
                        onClick = {
                            isBusy = true
                            scope.launch {
                                try {
                                    val dbFile = java.io.File(dbFilePath)
                                    val found = manager.restoreNow(dbFile)
                                    status = if (found) {
                                        "Berhasil dipulihkan. Tutup dan buka lagi aplikasinya."
                                    } else {
                                        "Belum ada backup tersimpan di Drive."
                                    }
                                } catch (e: Exception) {
                                    status = "Gagal memulihkan: ${e.message}"
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                    ) { Text("Pulihkan dari Drive") }
                }
                TextButton(onClick = { manager.signOut(); account = null }) { Text("Keluar") }
            }

            if (isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            lastBackup?.let {
                val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                Text("Backup terakhir: ${fmt.format(Date(it))}", style = MaterialTheme.typography.bodySmall)
            }

            status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
