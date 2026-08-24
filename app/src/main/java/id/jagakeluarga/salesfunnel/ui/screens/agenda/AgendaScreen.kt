package id.jagakeluarga.salesfunnel.ui.screens.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
                Text("Belum ada agenda follow-up.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Tambahkan agenda pertama untuk mulai mengatur tindak lanjut.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { dialogAgenda = null; showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah agenda")
                }
            }
        } else LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(agendaList, key = { it.id }) { agenda ->
                val namaProspek = prospekList.find { it.id == agenda.prospekId }?.nama ?: "(prospek dihapus)"
                val terlambat = !agenda.selesai && agenda.waktuMulai < System.currentTimeMillis()
                ListItem(
                    leadingContent = {
                        Checkbox(
                            checked = agenda.selesai,
                            onCheckedChange = { onToggleSelesai(agenda) },
                        )
                    },
                    headlineContent = { Text(agenda.judul) },
                    supportingContent = {
                        Column {
                            Text("$namaProspek · ${agenda.jenis.label} · ${fmt.format(Date(agenda.waktuMulai))}")
                            if (terlambat) {
                                Text("Terlambat", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(
                                    enabled = !prospekList.find { it.id == agenda.prospekId }?.nomorTelepon.isNullOrBlank(),
                                    onClick = { templateAgenda = agenda },
                                ) { Icon(Icons.Filled.Send, contentDescription = "Pilih template WhatsApp") }
                                IconButton(
                                    onClick = {
                                        onSimpan(agenda.copy(waktuMulai = agenda.waktuMulai + 24 * 60 * 60 * 1000, selesai = false))
                                    },
                                ) { Icon(Icons.Filled.Schedule, contentDescription = "Tunda 1 hari") }
                                IconButton(onClick = {
                                    dialogAgenda = agenda
                                    showDialog = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit agenda")
                                }
                                IconButton(onClick = { onHapus(agenda) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                                }
                            }
                        }
                    },
                )
                HorizontalDivider()
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
        title = { Text(if (agendaAwal == null) "Tambah Agenda" else "Edit Agenda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul") },
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
            TextButton(onClick = {
                if (judul.isNotBlank()) {
                    val agenda = agendaAwal?.copy(
                        prospekId = prospek.id,
                        judul = judul.trim(),
                        jenis = jenis,
                        waktuMulai = waktuMulai,
                        reminderOffsetHours = reminderOffsetHours,
                    ) ?: Agenda(
                        prospekId = prospek.id,
                        judul = judul.trim(),
                        jenis = jenis,
                        waktuMulai = waktuMulai,
                        reminderOffsetHours = reminderOffsetHours,
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
