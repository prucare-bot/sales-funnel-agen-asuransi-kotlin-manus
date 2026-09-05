package id.jagakeluarga.salesfunnel.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Circle
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.importer.ProspekCsvImporter
import id.jagakeluarga.salesfunnel.security.AppLockManager
import id.jagakeluarga.salesfunnel.backup.LocalBackupManager
import id.jagakeluarga.salesfunnel.data.AppDatabase
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
    onDatabaseRestored: () -> Unit = {},
    onImportProspek: (List<Prospek>) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("sales_funnel_settings", 0) }
    var namaUser by remember { mutableStateOf(preferences.getString("nama_user", "Densus") ?: "Densus") }
    var showBusinessCard by remember { mutableStateOf(false) }
    var targetClosingInput by remember { mutableStateOf(targetClosing.toString()) }
    var targetPremiInput by remember { mutableStateOf(targetPremi.toString()) }
    var pinInput by remember { mutableStateOf("") }
    var lockEnabled by remember { mutableStateOf(AppLockManager.isEnabled(context)) }
    val latestAutomaticBackup = remember { LocalBackupManager.latestAutomaticBackup(context) }
    val usernameFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var requestUsernameFocus by remember { mutableStateOf(false) }
    var usernameSaved by remember { mutableStateOf(false) }
    var importPreview by remember { mutableStateOf<ProspekCsvImporter.Result?>(null) }
    var showBackupPinDialog by remember { mutableStateOf(false) }
    var backupPinInput by remember { mutableStateOf("") }
    var pendingBackupUri by remember { mutableStateOf<Uri?>(null) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(requestUsernameFocus) {
        if (requestUsernameFocus) {
            usernameFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val localBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            pendingBackupUri = uri
            pendingRestoreUri = null
            backupPinInput = ""
            showBackupPinDialog = true
        }
    }
    val localRestoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            pendingBackupUri = null
            backupPinInput = ""
            showBackupPinDialog = true
        }
    }

    val importProspekLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val csv = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Tidak dapat membaca file CSV")
                    val parsed = ProspekCsvImporter.parse(csv)
                    val existingKeys = prospekList.mapNotNull { prospek ->
                        prospek.nomorTelepon?.filter(Char::isDigit)?.takeIf { it.isNotEmpty() }
                            ?: prospek.nama.trim().lowercase().takeIf { it.isNotEmpty() }
                    }.toSet()
                    val newProspek = parsed.valid.filterNot { prospek ->
                        val key = prospek.nomorTelepon?.filter(Char::isDigit)?.takeIf { it.isNotEmpty() }
                            ?: prospek.nama.trim().lowercase()
                        key in existingKeys
                    }
                    val existingDuplicates = parsed.valid.filter { it !in newProspek }.map { "${it.nama} (sudah ada)" }
                    importPreview = parsed.copy(valid = newProspek, duplicates = parsed.duplicates + existingDuplicates)
                } catch (e: Exception) {
                    status = "Impor CSV gagal: ${e.message}"
                }
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

    Scaffold(topBar = { TopAppBar(title = { Text("Pengaturan & Backup") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("User Settings", style = MaterialTheme.typography.headlineSmall)
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
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
                        modifier = Modifier.clickable {
                            requestUsernameFocus = true
                            status = null
                        },
                    )
                }
            }

            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Warna Tema", style = MaterialTheme.typography.titleMedium)
                    Text("Pilih warna utama aplikasi", style = MaterialTheme.typography.bodySmall)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        AppThemeColor.entries.forEach { option ->
                            val warnaTema = when (option) {
                                AppThemeColor.HIJAU -> Color(0xFF1C6E62)
                                AppThemeColor.BIRU -> Color(0xFF1976D2)
                                AppThemeColor.MERAH -> Color(0xFFC62828)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedTheme == option,
                                        onClick = { onThemeChanged(option) },
                                        role = Role.RadioButton,
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selectedTheme == option, onClick = null)
                                Icon(
                                    Icons.Filled.Circle,
                                    contentDescription = "Warna tema ${option.label}",
                                    tint = warnaTema,
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(option.label, color = warnaTema, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profil User", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = namaUser,
                        onValueChange = {
                            namaUser = it
                            usernameSaved = false
                        },
                        label = { Text("Nama user") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().focusRequester(usernameFocusRequester),
                    )
                    Button(
                        enabled = namaUser.isNotBlank(),
                        onClick = {
                            val nama = namaUser.trim()
                            preferences.edit().putString("nama_user", nama).apply()
                            onNamaUserChanged(nama)
                            usernameSaved = true
                            status = null
                        },
                    ) { Text("Simpan nama user") }
                    if (usernameSaved) {
                        Text(
                            "nama pengguna sudah diganti.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    HorizontalDivider()

                    Text("Keamanan Aplikasi", style = MaterialTheme.typography.titleMedium)
                    Text("Lindungi data nasabah dengan PIN minimal 4 digit.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it.filter(Char::isDigit).take(8) },
                        label = { Text(if (lockEnabled) "PIN baru" else "PIN aplikasi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = pinInput.length >= 4,
                            onClick = {
                                if (AppLockManager.setPin(context, pinInput)) {
                                    lockEnabled = true
                                    pinInput = ""
                                    status = "PIN aplikasi berhasil disimpan."
                                } else {
                                    status = "PIN harus berupa minimal 4 digit."
                                }
                            },
                        ) { Text(if (lockEnabled) "Ganti PIN" else "Aktifkan PIN") }
                        if (lockEnabled) {
                            OutlinedButton(onClick = {
                                AppLockManager.clearPin(context)
                                lockEnabled = false
                                pinInput = ""
                                status = "Kunci PIN dinonaktifkan."
                            }) { Text("Nonaktifkan") }
                        }
                    }
                }
            }

            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }
            }

            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Laporan", style = MaterialTheme.typography.titleMedium)
                    Text("Ekspor daftar prospek, agenda, dan nasabah ke CSV yang dapat dibuka di Excel.", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(enabled = !isBusy, onClick = { reportLauncher.launch("sales_funnel_laporan.csv") }) {
                        Text("Ekspor laporan CSV")
                    }
                    OutlinedButton(
                        enabled = !isBusy,
                        onClick = { importProspekLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv", "text/*")) },
                    ) { Text("Impor prospek CSV") }

                    HorizontalDivider()

                    Text("Backup lokal", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Backup lokal otomatis berjalan sekitar sekali sehari saat baterai tidak lemah. Backup manual dienkripsi dengan PIN dan dapat disimpan ke memori HP, kartu SD, atau folder cloud melalui pemilih file Android.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    latestAutomaticBackup?.let { backup ->
                        val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                        Text(
                            "Backup otomatis terakhir: ${fmt.format(Date(backup.lastModified()))} (${backup.length() / 1024} KB)",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } ?: Text("Backup otomatis belum pernah berhasil dibuat.", style = MaterialTheme.typography.bodySmall)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            enabled = !isBusy,
                            onClick = { localBackupLauncher.launch("sales_funnel_backup.db") },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Simpan ke perangkat") }
                        OutlinedButton(
                            enabled = !isBusy,
                            onClick = { localRestoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Pulihkan file") }
                    }
                }
            }

            if (isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }

    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text("Pratinjau impor prospek") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Siap diimpor: ${preview.valid.size} prospek")
                    if (preview.duplicates.isNotEmpty()) Text("Duplikat dilewati: ${preview.duplicates.size}", color = MaterialTheme.colorScheme.tertiary)
                    if (preview.errors.isNotEmpty()) Text("Baris bermasalah: ${preview.errors.size}", color = MaterialTheme.colorScheme.error)
                    preview.valid.take(8).forEach { Text("• ${it.nama}${it.nomorTelepon?.let { phone -> " · $phone" } ?: ""}") }
                    if (preview.valid.size > 8) Text("dan ${preview.valid.size - 8} prospek lainnya...")
                    preview.errors.take(5).forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                }
            },
            dismissButton = { TextButton(onClick = { importPreview = null }) { Text("Batal") } },
            confirmButton = {
                TextButton(
                    enabled = preview.valid.isNotEmpty() && !isBusy,
                    onClick = {
                        onImportProspek(preview.valid)
                        status = "${preview.valid.size} prospek berhasil diimpor."
                        importPreview = null
                    },
                ) { Text("Impor") }
            },
        )
    }

    if (showBackupPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showBackupPinDialog = false
                pendingBackupUri = null
                pendingRestoreUri = null
            },
            title = { Text(if (pendingBackupUri != null) "Amankan backup" else "Buka backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan PIN minimal 4 digit. Gunakan PIN yang sama saat memulihkan backup di perangkat lain.")
                    OutlinedTextField(
                        value = backupPinInput,
                        onValueChange = { backupPinInput = it.filter(Char::isDigit).take(8) },
                        label = { Text("PIN backup") },
                        singleLine = true,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupPinDialog = false
                    pendingBackupUri = null
                    pendingRestoreUri = null
                }) { Text("Batal") }
            },
            confirmButton = {
                TextButton(
                    enabled = backupPinInput.length >= 4 && !isBusy,
                    onClick = {
                        val pin = backupPinInput
                        val backupUri = pendingBackupUri
                        val restoreUri = pendingRestoreUri
                        showBackupPinDialog = false
                        pendingBackupUri = null
                        pendingRestoreUri = null
                        isBusy = true
                        scope.launch {
                            try {
                                if (backupUri != null) {
                                    LocalBackupManager.exportDatabase(context.contentResolver, java.io.File(dbFilePath), backupUri, pin)
                                    status = "Backup lokal terenkripsi berhasil disimpan."
                                } else if (restoreUri != null) {
                                    AppDatabase.closeInstance()
                                    LocalBackupManager.importDatabase(context.contentResolver, restoreUri, java.io.File(dbFilePath), pin)
                                    onDatabaseRestored()
                                    status = "Restore lokal berhasil. Data sudah dimuat ulang."
                                }
                            } catch (e: Exception) {
                                status = if (restoreUri != null) "Restore lokal gagal: ${e.message}" else "Backup lokal gagal: ${e.message}"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                ) { Text("Lanjutkan") }
            },
        )
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
