package id.jagakeluarga.salesfunnel.ui.screens.nasabah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NasabahScreen(
    nasabahList: List<Nasabah>,
    onSimpan: (Nasabah) -> Unit,
    onHapus: (Nasabah) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingNasabah by remember { mutableStateOf<Nasabah?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nasabah") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingNasabah = null
                showDialog = true
            }) { Text("+") }
        },
    ) { padding ->
        if (nasabahList.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Belum ada data nasabah.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Tambahkan nasabah untuk menyimpan data polis dan ulang tahun.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { editingNasabah = null; showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah nasabah")
                }
            }
        } else LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(nasabahList, key = { it.id }) { nasabah ->
                ListItem(
                    headlineContent = { Text(nasabah.nama) },
                    supportingContent = {
                        val lahir = nasabah.tanggalLahir?.let { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(it)) } ?: "Tanggal lahir belum diisi"
                        Text("${nasabah.produk} · Lahir: $lahir")
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                editingNasabah = nasabah
                                showDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit nasabah")
                            }
                            IconButton(onClick = { onHapus(nasabah) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                            }
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }

    if (showDialog) {
        NasabahDialog(
            initialNasabah = editingNasabah,
            onDismiss = {
                showDialog = false
                editingNasabah = null
            },
            onSimpan = {
                onSimpan(it)
                showDialog = false
                editingNasabah = null
            },
        )
    }
}

@Composable
private fun NasabahDialog(
    initialNasabah: Nasabah?,
    onDismiss: () -> Unit,
    onSimpan: (Nasabah) -> Unit,
) {
    var nama by remember(initialNasabah?.id) { mutableStateOf(initialNasabah?.nama.orEmpty()) }
    var produk by remember(initialNasabah?.id) { mutableStateOf(initialNasabah?.produk.orEmpty()) }
    var nomorPolis by remember(initialNasabah?.id) { mutableStateOf(initialNasabah?.nomorPolis.orEmpty()) }
    var tanggalLahir by rememberSaveable(initialNasabah?.id) { mutableStateOf(initialNasabah?.tanggalLahir) }
    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = tanggalLahir ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { tanggalLahir = state.selectedDateMillis; showDatePicker = false }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialNasabah == null) "Tambah Nasabah" else "Edit Nasabah") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nama, { nama = it }, label = { Text("Nama") })
                OutlinedTextField(produk, { produk = it }, label = { Text("Produk") })
                OutlinedTextField(nomorPolis, { nomorPolis = it }, label = { Text("No. Polis") })
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(tanggalLahir?.let { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date(it)) } ?: "Pilih tanggal lahir")
                }
                validationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nama.isNotBlank() && produk.isNotBlank()) {
                    onSimpan(
                        initialNasabah?.copy(
                            nama = nama,
                            produk = produk,
                            nomorPolis = nomorPolis.ifBlank { null },
                            tanggalLahir = tanggalLahir,
                        ) ?: Nasabah(
                            nama = nama,
                            produk = produk,
                            nomorPolis = nomorPolis.ifBlank { null },
                            tanggalLahir = tanggalLahir,
                        ),
                    )
                    validationError = null
                } else {
                    validationError = "Nama dan produk nasabah wajib diisi."
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
