package id.jagakeluarga.salesfunnel.ui.screens.prospek

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
                        SwipeableProspekItem(
                            prospek = prospek,
                            onKlik = { editing = prospek; showDialog = true },
                            onHapus = { onHapus(prospek) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableProspekItem(
    prospek: Prospek,
    onKlik: () -> Unit,
    onHapus: () -> Unit,
) {
    val context = LocalContext.current
    var konfirmasiHapus by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    konfirmasiHapus = true
                    false // jangan langsung hilang, tunggu konfirmasi dialog
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!prospek.nomorTelepon.isNullOrBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prospek.nomorTelepon}"))
                        context.startActivity(intent)
                    }
                    false // kembali ke posisi semula setelah buka dialer
                }
                SwipeToDismissBoxValue.Settled -> true
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val (warna, ikon, alignment) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(Color(0xFF10B981), Icons.Filled.Phone, Alignment.CenterStart)
                SwipeToDismissBoxValue.EndToStart -> Triple(Color(0xFFEF4444), Icons.Filled.Delete, Alignment.CenterEnd)
                SwipeToDismissBoxValue.Settled -> Triple(Color.Transparent, Icons.Filled.Phone, Alignment.CenterStart)
            }
            Box(
                Modifier.fillMaxSize().background(warna).padding(horizontal = 20.dp),
                contentAlignment = alignment,
            ) {
                Icon(ikon, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        ListItem(
            headlineContent = { Text(prospek.nama) },
            supportingContent = { Text("${prospek.tahap.label} · ${prospek.nomorTelepon ?: "-"}") },
            modifier = Modifier.clickable(onClick = onKlik),
        )
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus prospek?") },
            text = { Text("\"${prospek.nama}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { onHapus(); konfirmasiHapus = false }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapus = false }) { Text("Batal") }
            },
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
