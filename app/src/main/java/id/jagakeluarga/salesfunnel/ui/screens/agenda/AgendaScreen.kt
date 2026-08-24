package id.jagakeluarga.salesfunnel.ui.screens.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.JenisAgenda
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.ui.common.DateTimePickerField
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaScreen(
    agendaList: List<Agenda>,
    prospekList: List<Prospek>,
    onSimpan: (Agenda) -> Unit,
    onHapus: (Agenda) -> Unit,
) {
    var dialogAgenda by remember { mutableStateOf<Agenda?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda Follow-up") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dialogAgenda = null
                showDialog = true
            }) { Text("+") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(agendaList, key = { it.id }) { agenda ->
                val namaProspek = prospekList.find { it.id == agenda.prospekId }?.nama ?: "(prospek dihapus)"
                ListItem(
                    headlineContent = { Text(agenda.judul) },
                    supportingContent = { Text("$namaProspek · ${agenda.jenis.label} · ${fmt.format(Date(agenda.waktuMulai))}") },
                    trailingContent = {
                        Row {
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
                    },
                )
                HorizontalDivider()
            }
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
    var pencarianProspek by remember { mutableStateOf("") }
    var expandedProspek by remember { mutableStateOf(false) }
    var expandedJenis by remember { mutableStateOf(false) }

    val prospekTerfilter = remember(pencarianProspek, prospekTerurut) {
        val query = pencarianProspek.trim().lowercase(Locale("id", "ID"))
        if (query.isBlank()) prospekTerurut
        else prospekTerurut.filter { it.nama.lowercase(Locale("id", "ID")).contains(query) }
    }

    AlertDialog(
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
                ExposedDropdownMenuBox(
                    expanded = expandedProspek,
                    onExpandedChange = { expandedProspek = it },
                ) {
                    OutlinedTextField(
                        value = prospek.nama,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prospek") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProspek,
                        onDismissRequest = { expandedProspek = false },
                        modifier = Modifier.heightIn(max = 360.dp),
                    ) {
                        OutlinedTextField(
                            value = pencarianProspek,
                            onValueChange = { pencarianProspek = it },
                            label = { Text("Cari nasabah/prospek") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                        )
                        if (prospekTerfilter.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Prospek tidak ditemukan") },
                                onClick = {},
                                enabled = false,
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                                items(prospekTerfilter, key = { it.id }) { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.nama) },
                                        onClick = {
                                            prospek = p
                                            pencarianProspek = ""
                                            expandedProspek = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
                DateTimePickerField(
                    selectedMillis = waktuMulai,
                    onSelectedMillisChange = { waktuMulai = it },
                )
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
                    ) ?: Agenda(
                        prospekId = prospek.id,
                        judul = judul.trim(),
                        jenis = jenis,
                        waktuMulai = waktuMulai,
                    )
                    onSimpan(agenda)
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
