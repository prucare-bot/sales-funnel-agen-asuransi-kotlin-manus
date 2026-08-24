package id.jagakeluarga.salesfunnel.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import id.jagakeluarga.salesfunnel.backup.GoogleDriveBackupManager
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.backup.LocalBackupManager
import id.jagakeluarga.salesfunnel.ui.theme.AppThemeColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    dbFilePath: String,
    onNamaUserChanged: (String) -> Unit = {},
    selectedTheme: AppThemeColor = AppThemeColor.HIJAU,
    onThemeChanged: (AppThemeColor) -> Unit = {},
    prospekList: List<Prospek> = emptyList(),
    agendaList: List<Agenda> = emptyList(),
    nasabahList: List<Nasabah> = emptyList(),
    targetClosing: Int = 10,
    targetPremi: Long = 0L,
    onTargetChanged: (Int, Long) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val manager = remember { GoogleDriveBackupManager(context) }
    val scope = rememberCoroutineScope()

    var account by remember { mutableStateOf(manager.currentAccount()) }
    var status by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("sales_funnel_settings", 0) }
    var namaUser by remember { mutableStateOf(preferences.getString("nama_user", "Densus") ?: "Densus") }
    var lastBackup by remember { mutableStateOf<Long?>(null) }
    var showBusinessCard by remember { mutableStateOf(false) }
    var targetClosingInput by remember { mutableStateOf(targetClosing.toString()) }
    var targetPremiInput by remember { mutableStateOf(targetPremi.toString()) }

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

    val reportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            isBusy = true
            scope.launch {
                try {
                    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    val csv = buildString {
                        appendLine("Jenis,Judul/Nama,Tahap atau Jenis,Tanggal,Status")
                        prospekList.forEach { prospek ->
                            appendLine(listOf("Prospek", prospek.nama, prospek.tahap.label, fmt.format(Date(prospek.dibuatPada)), "Aktif").toCsvLine())
                        }
                        agendaList.forEach { agenda ->
                            appendLine(listOf("Agenda", agenda.judul, agenda.jenis.label, fmt.format(Date(agenda.waktuMulai)), if (agenda.selesai) "Selesai" else "Belum selesai").toCsvLine())
                        }
                        nasabahList.forEach { nasabah ->
                            appendLine(listOf("Nasabah", nasabah.nama, nasabah.produk, "", "Aktif").toCsvLine())
                        }
                    }
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(csv.toByteArray())
                    }
                    status = "Laporan berhasil diekspor."
                } catch (e: Exception) {
                    status = "Ekspor laporan gagal: ${e.message}"
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
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("User Settings", style = MaterialTheme.typography.headlineSmall)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) },
                        headlineContent = { Text("My Profile") },
                        supportingContent = { Text("Kelola identitas user aplikasi") },
                        modifier = Modifier.clickable { status = "Bagian profil aktif." },
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Contacts, contentDescription = null) },
                        headlineContent = { Text("Business Card") },
                        supportingContent = { Text("Lihat kartu nama digital") },
                        modifier = Modifier.clickable { showBusinessCard = true },
                    )
                    HorizontalDivider()
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        headlineContent = { Text("Change user name") },
                        supportingContent = { Text("Nama yang tampil di dashboard") },
                        modifier = Modifier.clickable { status = "Silakan ubah nama pada kolom Profil User." },
                    )
                }
            }

            Text("Warna Tema", style = MaterialTheme.typography.titleMedium)
            Text("Pilih warna utama aplikasi", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AppThemeColor.entries.forEach { option ->
                    FilterChip(
                        selected = selectedTheme == option,
                        onClick = { onThemeChanged(option) },
                        label = { Text(option.label) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

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

            Text("Target Penjualan", style = MaterialTheme.typography.titleMedium)
            Text("Tetapkan target closing dan estimasi premi untuk memantau pencapaian di Beranda.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = targetClosingInput,
                onValueChange = { targetClosingInput = it.filter(Char::isDigit) },
                label = { Text("Target closing per bulan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = targetPremiInput,
                onValueChange = { targetPremiInput = it.filter(Char::isDigit) },
                label = { Text("Target estimasi premi per bulan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                enabled = targetClosingInput.isNotBlank() && targetPremiInput.isNotBlank(),
                onClick = {
                    val closing = targetClosingInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val premi = targetPremiInput.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                    onTargetChanged(closing, premi)
                    status = "Target penjualan berhasil disimpan."
                },
            ) { Text("Simpan target") }

            Text("Laporan", style = MaterialTheme.typography.titleMedium)
            Text("Ekspor daftar prospek, agenda, dan nasabah ke CSV yang dapat dibuka di Excel.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(enabled = !isBusy, onClick = { reportLauncher.launch("sales_funnel_laporan.csv") }) {
                Text("Ekspor laporan CSV")
            }

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

    if (showBusinessCard) {
        AlertDialog(
            onDismissRequest = { showBusinessCard = false },
            title = { Text("Business Card") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(namaUser, style = MaterialTheme.typography.headlineSmall)
                    Text("Sales Funnel Agen Asuransi", style = MaterialTheme.typography.bodyLarge)
                    Text("Hubungi saya untuk diskusi kebutuhan perlindungan.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = { TextButton(onClick = { showBusinessCard = false }) { Text("Tutup") } },
        )
    }
}

private fun List<String>.toCsvLine(): String = joinToString(",") { value ->
    "\"${value.replace("\"", "\"\"")}\""
}
