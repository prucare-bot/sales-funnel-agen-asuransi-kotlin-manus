package id.jagakeluarga.salesfunnel.ui.screens.prospek

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

@Composable
fun ProspekScreen(
    prospekList: List<Prospek>,
    onSimpan: (Prospek) -> Unit,
    onHapus: (Prospek) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Prospek?>(null) }
    var kataKunci by remember { mutableStateOf("") }

    val hasilFilter = remember(prospekList, kataKunci) {
        if (kataKunci.isBlank()) {
            prospekList
        } else {
            prospekList.filter { it.nama.contains(kataKunci, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daftar Prospek") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) { Text("+") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = kataKunci,
                onValueChange = { kataKunci = it },
                placeholder = { Text("Cari nama prospek...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (kataKunci.isNotEmpty()) {
                        IconButton(onClick = { kataKunci = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )

            if (hasilFilter.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(
                        if (kataKunci.isBlank()) "Belum ada prospek" else "Tidak ditemukan \"$kataKunci\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(hasilFilter, key = { it.id }) { prospek ->
                        ListItem(
                            headlineContent = { Text(prospek.nama) },
                            supportingContent = { Text("${prospek.tahap.label} · ${prospek.nomorTelepon ?: "-"}") },
                            trailingContent = {
                                IconButton(onClick = { onHapus(prospek) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                                }
                            },
                            modifier = Modifier.clickable { editing = prospek; showDialog = true },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showDialog) {
        ProspekDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSimpan = { onSimpan(it); showDialog = false },
        )
    }
}

@Composable
private fun ProspekDialog(
    initial: Prospek?,
    onDismiss: () -> Unit,
    onSimpan: (Prospek) -> Unit,
) {
    var nama by remember { mutableStateOf(initial?.nama ?: "") }
    var telepon by remember { mutableStateOf(initial?.nomorTelepon ?: "") }
    var tahap by remember { mutableStateOf(initial?.tahap ?: TahapPipeline.PROSPEK) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Prospek" else "Edit Prospek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nama, { nama = it }, label = { Text("Nama") })
                OutlinedTextField(telepon, { telepon = it }, label = { Text("No. Telepon") })
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = tahap.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tahap") },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        TahapPipeline.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.label) }, onClick = { tahap = t; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nama.isNotBlank()) {
                    onSimpan(
                        (initial ?: Prospek(nama = nama)).copy(
                            nama = nama,
                            nomorTelepon = telepon.ifBlank { null },
                            tahap = tahap,
                            diperbaruiPada = System.currentTimeMillis(),
                        )
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
