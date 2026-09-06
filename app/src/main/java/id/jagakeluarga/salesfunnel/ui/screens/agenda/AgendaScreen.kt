package id.jagakeluarga.salesfunnel.ui.screens.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.JenisAgenda
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.ui.common.DateTimePickerField
import id.jagakeluarga.salesfunnel.ui.common.WhatsAppTemplateDialog
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaScreen(
    agendaList: List<Agenda>,
    prospekList: List<Prospek>,
    onSimpan: (Agenda) -> Unit,
    onHapus: (Agenda) -> Unit,
    onToggleSelesai: (Agenda) -> Unit = {},
) {
    var dialogAgenda by remember { mutableStateOf<Agenda?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    var templateAgenda by remember { mutableStateOf<Agenda?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda Follow-up") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dialogAgenda = null
                showDialog = true
            }) { Icon(Icons.Filled.Add, contentDescription = "Tambah agenda") }
        },
    ) { padding ->
        if (agendaList.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Belum ada agenda follow-up.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tambahkan agenda pertama untuk mulai mengatur tindak lanjut.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { dialogAgenda = null; showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah agenda")
                }
            }
        } else {
            val sekarang = System.currentTimeMillis()
            val awalHari = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val akhirHari = awalHari + 24 * 60 * 60 * 1000
            val terlambat = agendaList.filter { !it.selesai && it.waktuMulai < sekarang }.sortedBy { it.waktuMulai }
            val hariIni = agendaList.filter { it.waktuMulai in awalHari until akhirHari && it.waktuMulai >= sekarang || (it.selesai && it.waktuMulai in awalHari until akhirHari) }
                .sortedBy { it.waktuMulai }
            val berikutnya = agendaList.filter { !it.selesai && it.waktuMulai >= akhirHari }.sortedBy { it.waktuMulai }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (terlambat.isNotEmpty()) {
                    item { AgendaGroupHeader("Terlambat") }
                    items(terlambat, key = { it.id }) { agenda ->
                        Box(Modifier.animateItem()) {
                            AgendaRow(agenda, prospekList, fmt, onToggleSelesai, onSimpan, { dialogAgenda = agenda; showDialog = true }, onHapus, { templateAgenda = agenda })
                        }
                    }
                }
                if (hariIni.isNotEmpty()) {
                    item { AgendaGroupHeader("Hari ini") }
                    items(hariIni, key = { it.id }) { agenda ->
                        Box(Modifier.animateItem()) {
                            AgendaRow(agenda, prospekList, fmt, onToggleSelesai, onSimpan, { dialogAgenda = agenda; showDialog = true }, onHapus, { templateAgenda = agenda })
                        }
                    }
                }
                if (berikutnya.isNotEmpty()) {
                    item { AgendaGroupHeader("Berikutnya") }
                    items(berikutnya, key = { it.id }) { agenda ->
                        Box(Modifier.animateItem()) {
                            AgendaRow(agenda, prospekList, fmt, onToggleSelesai, onSimpan, { dialogAgenda = agenda; showDialog = true }, onHapus, { templateAgenda = agenda })
                        }
                    }
                }
            }
        }
    }

    templateAgenda?.let { agenda ->
        val prospek = prospekList.find { it.id == agenda.prospekId }
        if (prospek != null) {
            WhatsAppTemplateDialog(
                nama = prospek.nama,
                agenda = agenda.judul,
                waktu = fmt.format(Date(agenda.waktuMulai)),
                onDismiss = { templateAgenda = null },
                onSend = { message ->
                    WhatsAppHelper.openChat(context, prospek.nomorTelepon, message)
                    templateAgenda = null
                },
            )
        }
    }

    if (showDialog && prospekList.isNotEmpty()) {
        AgendaDialog(
            prospekList = prospekList,
            agendaAwal = dialogAgenda,
            onDismiss = {
                showDialog = false
                dialogAgenda = null
            },
            onSimpan = {
                onSimpan(it)
                showDialog = false
                dialogAgenda = null
            },
        )
    }
}

@Composable
private fun SnoozeButton(
    onSnooze: (deltaMillis: Long) -> Unit,
    onSnoozeKe: (targetMillis: Long) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCustomPicker by remember { mutableStateOf(false) }
    var customMillis by remember { mutableStateOf(System.currentTimeMillis() + 24 * 60 * 60 * 1000) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Filled.Schedule, contentDescription = "Tunda pengingat")
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Tunda 1 jam") },
                onClick = { showMenu = false; onSnooze(60L * 60 * 1000) },
            )
            DropdownMenuItem(
                text = { Text("Tunda 3 jam") },
                onClick = { showMenu = false; onSnooze(3 * 60L * 60 * 1000) },
            )
            DropdownMenuItem(
                text = { Text("Tunda 1 hari") },
                onClick = { showMenu = false; onSnooze(24 * 60L * 60 * 1000) },
            )
            DropdownMenuItem(
                text = { Text("Tunda 3 hari") },
                onClick = { showMenu = false; onSnooze(3 * 24 * 60L * 60 * 1000) },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Pilih tanggal & waktu...") },
                onClick = { showMenu = false; showCustomPicker = true },
            )
        }
    }

    if (showCustomPicker) {
        AlertDialog(
            onDismissRequest = { showCustomPicker = false },
            title = { Text("Tunda ke tanggal & waktu") },
            text = {
                id.jagakeluarga.salesfunnel.ui.common.DateTimePickerField(
                    selectedMillis = customMillis,
                    onSelectedMillisChange = { customMillis = it },
                    label = "Tunda hingga",
                )
            },
            confirmButton = {
                TextButton(onClick = { showCustomPicker = false; onSnoozeKe(customMillis) }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showCustomPicker = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun AgendaGroupHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
    )
}

/** Baris agenda dengan aksen warna di kiri: merah untuk terlambat, teal untuk yang akan datang. */
@Composable
private fun AgendaRow(
    agenda: Agenda,
    prospekList: List<Prospek>,
    fmt: SimpleDateFormat,
    onToggleSelesai: (Agenda) -> Unit,
    onSimpan: (Agenda) -> Unit,
    onEdit: () -> Unit,
    onHapus: (Agenda) -> Unit,
    onKirimTemplate: () -> Unit,
) {
    val namaProspek = prospekList.find { it.id == agenda.prospekId }?.nama ?: "(prospek dihapus)"
    val terlambat = !agenda.selesai && agenda.waktuMulai < System.currentTimeMillis()
    val warnaAksen = if (terlambat) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(warnaAksen),
        )
        ListItem(
            leadingContent = {
                Checkbox(checked = agenda.selesai, onCheckedChange = { onToggleSelesai(agenda) })
            },
            headlineContent = {
                Text(
                    agenda.judul,
                    style = if (agenda.selesai) MaterialTheme.typography.bodyLarge.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyLarge,
                    color = if (agenda.selesai) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            },
            supportingContent = {
                Column {
                    Text("$namaProspek · ${agenda.jenis.label} · ${fmt.format(Date(agenda.waktuMulai))}")
                    if (terlambat) {
                        Text("Terlambat", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(
                            enabled = !prospekList.find { it.id == agenda.prospekId }?.nomorTelepon.isNullOrBlank(),
                            onClick = onKirimTemplate,
                        ) { Icon(Icons.Filled.Send, contentDescription = "Pilih template WhatsApp") }
                        SnoozeButton(
                            onSnooze = { deltaMillis ->
                                onSimpan(agenda.copy(waktuMulai = agenda.waktuMulai + deltaMillis, selesai = false))
                            },
                            onSnoozeKe = { targetMillis ->
                                onSimpan(agenda.copy(waktuMulai = targetMillis, selesai = false))
                            },
                        )
                        IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit agenda") }
                        IconButton(onClick = { onHapus(agenda) }) { Icon(Icons.Filled.Delete, contentDescription = "Hapus") }
                    }
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AgendaDialog(
    prospekList: List<Prospek>,
    agendaAwal: Agenda?,
    onDismiss: () -> Unit,
    onSimpan: (Agenda) -> Unit,
) {
    val prospekTerurut = remember(prospekList) {
        prospekList.sortedBy { it.nama.trim().lowercase(Locale("id", "ID")) }
    }
    var judul by remember(agendaAwal) { mutableStateOf(agendaAwal?.judul.orEmpty()) }
    var prospek by remember(agendaAwal, prospekTerurut) {
        mutableStateOf(
            prospekTerurut.firstOrNull { it.id == agendaAwal?.prospekId } ?: prospekTerurut.first()
        )
    }
    var jenis by remember(agendaAwal) { mutableStateOf(agendaAwal?.jenis ?: JenisAgenda.LAINNYA) }
    var waktuMulai by remember(agendaAwal) {
        mutableStateOf(agendaAwal?.waktuMulai ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000))
    }
    var reminderOffsetHours by remember(agendaAwal) { mutableStateOf(agendaAwal?.reminderOffsetHours ?: 24) }
    var catatan by remember(agendaAwal) { mutableStateOf(agendaAwal?.catatan.orEmpty()) }
    var expandedReminder by remember { mutableStateOf(false) }
    var showProspekPicker by remember { mutableStateOf(false) }
    var expandedJenis by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    if (showProspekPicker) {
        ProspekPickerDialog(
            prospekList = prospekTerurut,
            selectedProspekId = prospek.id,
            onDismiss = { showProspekPicker = false },
            onSelected = {
                prospek = it
                showProspekPicker = false
            },
        )
    }

    if (!showProspekPicker) AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
            }
        },
        title = { Text(if (agendaAwal == null) "Tambah Agenda" else "Edit Agenda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul") },
                    leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = prospek.nama,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prospek") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showProspekPicker = true },
                    )
                }
                DateTimePickerField(
                    selectedMillis = waktuMulai,
                    onSelectedMillisChange = { waktuMulai = it },
                )
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Keterangan follow-up (masuk Timeline Aktivitas)") },
                    leadingIcon = { Icon(Icons.Filled.Notes, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
                ExposedDropdownMenuBox(
                    expanded = expandedReminder,
                    onExpandedChange = { expandedReminder = it },
                ) {
                    OutlinedTextField(
                        value = if (reminderOffsetHours == 24) "1 hari sebelum janji" else "4 jam sebelum janji",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pengingat") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = expandedReminder,
                        onDismissRequest = { expandedReminder = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("1 hari sebelum janji") },
                            onClick = { reminderOffsetHours = 24; expandedReminder = false },
                        )
                        DropdownMenuItem(
                            text = { Text("4 jam sebelum janji") },
                            onClick = { reminderOffsetHours = 4; expandedReminder = false },
                        )
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedJenis,
                    onExpandedChange = { expandedJenis = it },
                ) {
                    OutlinedTextField(
                        value = jenis.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis") },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = expandedJenis,
                        onDismissRequest = { expandedJenis = false },
                    ) {
                        JenisAgenda.entries.forEach { j ->
                            DropdownMenuItem(
                                text = { Text(j.label) },
                                onClick = { jenis = j; expandedJenis = false },
                            )
                        }
                    }
                }
                validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                onClick = {
                if (judul.isNotBlank()) {
                    val agenda = agendaAwal?.copy(
                        prospekId = prospek.id,
                        judul = judul.trim(),
                        jenis = jenis,
                        waktuMulai = waktuMulai,
                        reminderOffsetHours = reminderOffsetHours,
                        catatan = catatan.trim().takeIf { it.isNotBlank() },
                    ) ?: Agenda(
                        prospekId = prospek.id,
                        judul = judul.trim(),
                        jenis = jenis,
                        waktuMulai = waktuMulai,
                        reminderOffsetHours = reminderOffsetHours,
                        catatan = catatan.trim().takeIf { it.isNotBlank() },
                    )
                    onSimpan(agenda)
                    validationError = null
                } else {
                    validationError = "Judul agenda wajib diisi."
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun ProspekPickerDialog(

    prospekList: List<Prospek>,
    selectedProspekId: String,
    onDismiss: () -> Unit,
    onSelected: (Prospek) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(prospekList, query) {
        val normalized = query.trim().lowercase(Locale("id", "ID"))
        if (normalized.isBlank()) prospekList
        else prospekList.filter { it.nama.lowercase(Locale("id", "ID")).contains(normalized) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Prospek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Cari nama prospek") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(filtered, key = { it.id }) { item ->
                        DropdownMenuItem(
                            text = { Text(item.nama) },
                            trailingIcon = { if (item.id == selectedProspekId) Text("✓") },
                            onClick = { onSelected(item) },
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } },
    )
}
