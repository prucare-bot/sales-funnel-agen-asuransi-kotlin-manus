package id.jagakeluarga.salesfunnel.ui.screens.agenda

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.JenisAgenda
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgendaScreen(
    agendaList: List<Agenda>,
    prospekList: List<Prospek>,
    onSimpan: (Agenda) -> Unit,
    onHapus: (Agenda) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda Follow-up") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Text("+") }
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
                        IconButton(onClick = { onHapus(agenda) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus")
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
            onDismiss = { showDialog = false },
            onSimpan = { onSimpan(it); showDialog = false },
        )
    }
}

@Composable
private fun AgendaDialog(
    prospekList: List<Prospek>,
    onDismiss: () -> Unit,
    onSimpan: (Agenda) -> Unit,
) {
    var judul by remember { mutableStateOf("") }
    var prospek by remember { mutableStateOf(prospekList.first()) }
    var jenis by remember { mutableStateOf(JenisAgenda.LAINNYA) }
    var expandedProspek by remember { mutableStateOf(false) }
    var expandedJenis by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Agenda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(judul, { judul = it }, label = { Text("Judul") })
                ExposedDropdownMenuBox(expanded = expandedProspek, onExpandedChange = { expandedProspek = it }) {
                    OutlinedTextField(
                        value = prospek.nama, onValueChange = {}, readOnly = true,
                        label = { Text("Prospek") }, modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = expandedProspek, onDismissRequest = { expandedProspek = false }) {
                        prospekList.forEach { p ->
                            DropdownMenuItem(text = { Text(p.nama) }, onClick = { prospek = p; expandedProspek = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedJenis, onExpandedChange = { expandedJenis = it }) {
                    OutlinedTextField(
                        value = jenis.label, onValueChange = {}, readOnly = true,
                        label = { Text("Jenis") }, modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = expandedJenis, onDismissRequest = { expandedJenis = false }) {
                        JenisAgenda.entries.forEach { j ->
                            DropdownMenuItem(text = { Text(j.label) }, onClick = { jenis = j; expandedJenis = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (judul.isNotBlank()) {
                    onSimpan(
                        Agenda(
                            prospekId = prospek.id,
                            judul = judul,
                            jenis = jenis,
                            waktuMulai = System.currentTimeMillis() + 24 * 60 * 60 * 1000,
                        )
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
