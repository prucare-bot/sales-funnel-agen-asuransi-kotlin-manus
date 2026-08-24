package id.jagakeluarga.salesfunnel.ui.screens.prospek

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
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
import id.jagakeluarga.salesfunnel.ui.common.DateTimePickerField
import id.jagakeluarga.salesfunnel.ui.common.warnaTahap
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailProspekScreen(
    prospek: Prospek,
    riwayatAgenda: List<Agenda>,
    onKembali: () -> Unit,
    onSimpanProspek: (Prospek) -> Unit,
    onHapusProspek: (Prospek) -> Unit,
    onSimpanAgenda: (Agenda) -> Unit,
    onToggleSelesai: (Agenda) -> Unit,
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showHapusConfirm by remember { mutableStateOf(false) }
    var showTambahAgenda by remember { mutableStateOf(false) }
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
            FloatingActionButton(onClick = { showTambahAgenda = true }) { Text("+") }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
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
                Column {
                    Text(prospek.nama, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = {}, label = { Text(prospek.tahap.label) })
                }
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

            HorizontalDivider()

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
