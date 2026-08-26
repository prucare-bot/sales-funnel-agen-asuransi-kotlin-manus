package id.jagakeluarga.salesfunnel.ui.screens.prospek

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.JenisAgenda
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.ProspekStatusHistory
import id.jagakeluarga.salesfunnel.data.entity.ProspekAktivitas
import id.jagakeluarga.salesfunnel.ui.common.DateTimePickerField
import id.jagakeluarga.salesfunnel.ui.common.warnaTahap
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailProspekScreen(
    prospek: Prospek,
    riwayatAgenda: List<Agenda>,
    riwayatStatus: List<ProspekStatusHistory> = emptyList(),
    riwayatAktivitas: List<ProspekAktivitas> = emptyList(),
    sudahJadiNasabah: Boolean = false,
    onKembali: () -> Unit,
    onSimpanProspek: (Prospek) -> Unit,
    onHapusProspek: (Prospek) -> Unit,
    onSimpanAktivitas: (ProspekAktivitas) -> Unit = {},
    onKonversiNasabah: (Prospek, String, String?, (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onSimpanAgenda: (Agenda) -> Unit,
    onToggleSelesai: (Agenda) -> Unit,
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showHapusConfirm by remember { mutableStateOf(false) }
    var showTambahAgenda by remember { mutableStateOf(false) }
    var showKonversiDialog by remember { mutableStateOf(false) }
    var showAktivitasDialog by remember { mutableStateOf(false) }
    var conversionMessage by remember { mutableStateOf<String?>(null) }
    val warna = warnaTahap(prospek.tahap)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(prospek.nama) },
                navigationIcon = {
                    IconButton(onClick = onKembali) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showHapusConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTambahAgenda = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah follow-up")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val inisial = prospek.nama.trim().split(" ")
                    .filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercase() }.ifBlank { "?" }
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(warna),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(inisial, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(16.dp))
                AssistChip(onClick = {}, label = { Text(prospek.tahap.label) })
            }

            if (!prospek.nomorTelepon.isNullOrBlank()) {
                ElevatedCard(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prospek.nomorTelepon}")))
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = warna)
                        Spacer(Modifier.width(8.dp))
                        Text("No HP/WA: ${prospek.nomorTelepon}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            prospek.estimasiPremi?.let {
                Text("Estimasi premi: Rp ${"%,d".format(it).replace(',', '.')}", style = MaterialTheme.typography.bodyMedium)
            }
            prospek.sumberProspek?.let { Text("Sumber: $it", style = MaterialTheme.typography.bodyMedium) }
            prospek.catatan?.let { Text("Catatan: $it", style = MaterialTheme.typography.bodyMedium) }

            if (sudahJadiNasabah) {
                AssistChip(onClick = {}, label = { Text("Sudah menjadi nasabah") })
            } else {
                OutlinedButton(onClick = { showKonversiDialog = true }) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Jadikan Nasabah")
                }
            }
            conversionMessage?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Timeline Aktivitas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { showAktivitasDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
            if (riwayatAktivitas.isEmpty()) {
                Text(
                    "Belum ada aktivitas manual. Perubahan tahap dan follow-up baru akan tercatat otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val aktivitasFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                riwayatAktivitas.sortedByDescending { it.dibuatPada }.forEach { aktivitas ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(aktivitas.judul, fontWeight = FontWeight.SemiBold)
                            Text("${aktivitas.jenis.replace('_', ' ')} · ${aktivitasFmt.format(Date(aktivitas.dibuatPada))}", style = MaterialTheme.typography.labelMedium, color = warna)
                            aktivitas.catatan?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                        }
                    }
                }
            }

            Text("Riwayat Tahap Funnel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (riwayatStatus.isEmpty()) {
                Text(
                    "Riwayat status akan tercatat saat tahap Prospek disimpan atau dikonversi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val statusFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                riwayatStatus.sortedByDescending { it.diubahPada }.forEach { history ->
                    ListItem(
                        headlineContent = { Text(history.tahap.label, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(statusFmt.format(Date(history.diubahPada))) },
                    )
                    HorizontalDivider()
                }
            }

            Text("Riwayat Follow-up", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (riwayatAgenda.isEmpty()) {
                Text(
                    "Belum ada follow-up tercatat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                riwayatAgenda.sortedByDescending { it.waktuMulai }.forEach { agenda ->
                    ListItem(
                        headlineContent = { Text(agenda.judul) },
                        supportingContent = { Text("${agenda.jenis.label} · ${fmt.format(Date(agenda.waktuMulai))}") },
                        leadingContent = {
                            Checkbox(checked = agenda.selesai, onCheckedChange = { onToggleSelesai(agenda) })
                        },
                        modifier = Modifier.clickable { onToggleSelesai(agenda) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAktivitasDialog) {
        TambahAktivitasDialog(
            prospekId = prospek.id,
            onDismiss = { showAktivitasDialog = false },
            onSimpan = {
                onSimpanAktivitas(it)
                showAktivitasDialog = false
            },
        )
    }

    if (showKonversiDialog) {
        KonversiNasabahDialog(
            onDismiss = { showKonversiDialog = false },
            onConfirm = { produk, nomorPolis ->
                onKonversiNasabah(prospek, produk, nomorPolis) { message ->
                    conversionMessage = message
                    showKonversiDialog = false
                }
            },
        )
    }

    if (showEditDialog) {
        ProspekDialog(
            initial = prospek,
            onDismiss = { showEditDialog = false },
            onSimpan = { onSimpanProspek(it); showEditDialog = false },
        )
    }

    if (showHapusConfirm) {
        AlertDialog(
            onDismissRequest = { showHapusConfirm = false },
            title = { Text("Hapus prospek?") },
            text = { Text("\"${prospek.nama}\" beserta riwayat follow-up-nya akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { onHapusProspek(prospek); showHapusConfirm = false; onKembali() }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { showHapusConfirm = false }) { Text("Batal") } },
        )
    }

    if (showTambahAgenda) {
        TambahFollowUpDialog(
            prospekId = prospek.id,
            onDismiss = { showTambahAgenda = false },
            onSimpan = {
                onSimpanAgenda(it)
                showTambahAgenda = false
            },
        )
    }
}

@Composable
private fun TambahAktivitasDialog(
    prospekId: String,
    onDismiss: () -> Unit,
    onSimpan: (ProspekAktivitas) -> Unit,
) {
    var jenis by remember { mutableStateOf("CATATAN") }
    var judul by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val jenisAktivitas = listOf("TELEPON", "PERTEMUAN", "FOLLOW_UP", "CATATAN", "LAINNYA")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Aktivitas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = jenis.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis aktivitas") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        jenisAktivitas.forEach { pilihan ->
                            DropdownMenuItem(
                                text = { Text(pilihan.replace('_', ' ')) },
                                onClick = { jenis = pilihan; expanded = false },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul aktivitas") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = judul.isNotBlank(),
                onClick = {
                    onSimpan(
                        ProspekAktivitas(
                            prospekId = prospekId,
                            jenis = jenis,
                            judul = judul.trim(),
                            catatan = catatan.trim().takeIf { it.isNotBlank() },
                        ),
                    )
                },
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun KonversiNasabahDialog(
    onDismiss: () -> Unit,
    onConfirm: (produk: String, nomorPolis: String?) -> Unit,
) {
    var produk by remember { mutableStateOf("") }
    var nomorPolis by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jadikan Nasabah") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Data nama dan nomor HP/WA prospek akan disalin ke data nasabah.")
                OutlinedTextField(
                    value = produk,
                    onValueChange = { produk = it },
                    label = { Text("Produk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = nomorPolis,
                    onValueChange = { nomorPolis = it },
                    label = { Text("No. Polis (opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = produk.isNotBlank(),
                onClick = { onConfirm(produk, nomorPolis.ifBlank { null }) },
            ) { Text("Konversi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun TambahFollowUpDialog(
    prospekId: String,
    onDismiss: () -> Unit,
    onSimpan: (Agenda) -> Unit,
) {
    var judul by remember { mutableStateOf("") }
    var jenis by remember { mutableStateOf(JenisAgenda.LAINNYA) }
    var waktuMulai by remember { mutableStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000) }
    var reminderOffsetHours by remember { mutableStateOf(24) }
    var expanded by remember { mutableStateOf(false) }
    var expandedReminder by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Follow-up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(judul, { judul = it }, label = { Text("Judul") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = jenis.label, onValueChange = {}, readOnly = true,
                        label = { Text("Jenis") }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        JenisAgenda.entries.forEach { j ->
                            DropdownMenuItem(text = { Text(j.label) }, onClick = { jenis = j; expanded = false })
                        }
                    }
                }
                DateTimePickerField(
                    selectedMillis = waktuMulai,
                    onSelectedMillisChange = { waktuMulai = it },
                )
                ExposedDropdownMenuBox(expanded = expandedReminder, onExpandedChange = { expandedReminder = it }) {
                    OutlinedTextField(
                        value = if (reminderOffsetHours == 24) "1 hari sebelum janji" else "4 jam sebelum janji",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pengingat") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expandedReminder, onDismissRequest = { expandedReminder = false }) {
                        DropdownMenuItem(text = { Text("1 hari sebelum janji") }, onClick = { reminderOffsetHours = 24; expandedReminder = false })
                        DropdownMenuItem(text = { Text("4 jam sebelum janji") }, onClick = { reminderOffsetHours = 4; expandedReminder = false })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (judul.isNotBlank()) {
                    onSimpan(
                        Agenda(
                            prospekId = prospekId,
                            judul = judul.trim(),
                            jenis = jenis,
                            waktuMulai = waktuMulai,
                            reminderOffsetHours = reminderOffsetHours,
                        )
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
