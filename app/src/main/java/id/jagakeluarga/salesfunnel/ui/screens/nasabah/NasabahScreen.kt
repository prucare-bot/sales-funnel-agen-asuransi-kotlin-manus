package id.jagakeluarga.salesfunnel.ui.screens.nasabah

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nasabah") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Text("+") }
        },
    ) { padding ->
        LazyColumn(
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
                        IconButton(onClick = { onHapus(nasabah) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                        }
                    },
                )
                HorizontalDivider()
            }
        }
    }

    if (showDialog) {
        NasabahDialog(onDismiss = { showDialog = false }, onSimpan = { onSimpan(it); showDialog = false })
    }
}

@Composable
private fun NasabahDialog(onDismiss: () -> Unit, onSimpan: (Nasabah) -> Unit) {
    var nama by remember { mutableStateOf("") }
    var produk by remember { mutableStateOf("") }
    var nomorPolis by remember { mutableStateOf("") }
    var tanggalLahir by rememberSaveable { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

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
        title = { Text("Tambah Nasabah") },
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
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nama.isNotBlank() && produk.isNotBlank()) {
                    onSimpan(Nasabah(nama = nama, produk = produk, nomorPolis = nomorPolis.ifBlank { null }, tanggalLahir = tanggalLahir))
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
