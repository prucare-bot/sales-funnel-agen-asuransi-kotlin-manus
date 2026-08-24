package id.jagakeluarga.salesfunnel.ui.screens.prospek

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
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
import id.jagakeluarga.salesfunnel.ui.screens.prospek.ProspekFilterDialog
import id.jagakeluarga.salesfunnel.ui.common.ContactPickerDialog
import androidx.core.content.ContextCompat

@Composable
fun ProspekScreen(
    prospekList: List<Prospek>,
    onSimpan: (Prospek) -> Unit,
    onHapus: (Prospek) -> Unit,
    onBukaDetail: (Prospek) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var kataKunci by remember { mutableStateOf("") }
    var tanggalMulai by remember { mutableStateOf<Long?>(null) }
    var tanggalAkhir by remember { mutableStateOf<Long?>(null) }
    var tahapFilter by remember { mutableStateOf<TahapPipeline?>(null) }
    var showFilter by remember { mutableStateOf(false) }

    val hasilFilter = remember(prospekList, kataKunci, tanggalMulai, tanggalAkhir, tahapFilter) {
        prospekList.filter { prospek ->
            val cocokNama = kataKunci.isBlank() || prospek.nama.contains(kataKunci, ignoreCase = true)
            val cocokMulai = tanggalMulai == null || prospek.dibuatPada >= tanggalMulai!!
            val cocokAkhir = tanggalAkhir == null || prospek.dibuatPada <= tanggalAkhir!!
            val cocokTahap = tahapFilter == null || prospek.tahap == tahapFilter
            cocokNama && cocokMulai && cocokAkhir && cocokTahap
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daftar Prospek") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah prospek")
            }
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
            OutlinedButton(
                onClick = { showFilter = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (tanggalMulai != null || tanggalAkhir != null || tahapFilter != null) "Filter aktif" else "Filter tanggal & status")
            }

            if (hasilFilter.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (kataKunci.isBlank() && tanggalMulai == null && tanggalAkhir == null && tahapFilter == null) "Belum ada prospek" else "Tidak ada prospek yang sesuai filter",
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
                            onKlik = { onBukaDetail(prospek) },
                            onHapus = { onHapus(prospek) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showFilter) {
        ProspekFilterDialog(
            tanggalMulai = tanggalMulai,
            tanggalAkhir = tanggalAkhir,
            tahapAwal = tahapFilter,
            onDismiss = { showFilter = false },
            onApply = { mulai, akhir, tahap ->
                tanggalMulai = mulai
                tanggalAkhir = akhir
                tahapFilter = tahap
                showFilter = false
            },
        )
    }

    if (showDialog) {
        ProspekDialog(
            initial = null,
            onDismiss = { showDialog = false },
            onSimpan = { onSimpan(it); showDialog = false },
            onImportBanyak = { kontak -> kontak.forEach(onSimpan) },
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
            supportingContent = { Text("${prospek.tahap.label} · No HP/WA: ${prospek.nomorTelepon ?: "-"}") },
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
fun ProspekDialog(
    initial: Prospek?,
    onDismiss: () -> Unit,
    onSimpan: (Prospek) -> Unit,
    onImportBanyak: (List<Prospek>) -> Unit = {},
) {
    val context = LocalContext.current
    var nama by remember { mutableStateOf(initial?.nama ?: "") }
    var telepon by remember { mutableStateOf(initial?.nomorTelepon ?: "") }
    var tahap by remember { mutableStateOf(initial?.tahap ?: TahapPipeline.PROSPEK) }
    var expanded by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showContactPicker = true }

    fun openContactPicker() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            showContactPicker = true
        } else {
            contactPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }

    if (showContactPicker && initial == null) {
        ContactPickerDialog(
            context = context,
            onDismiss = { showContactPicker = false },
            onSelected = { contacts ->
                showContactPicker = false
                if (contacts.size == 1) {
                    nama = contacts.first().nama
                    telepon = contacts.first().nomorTelepon.orEmpty()
                } else {
                    onImportBanyak(contacts)
                    onDismiss()
                }
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Tambah Prospek" else "Edit Prospek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initial == null) {
                    OutlinedButton(onClick = ::openContactPicker, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih kontak (satu atau banyak)")
                    }
                }
                OutlinedTextField(nama, { nama = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(telepon, { telepon = it }, label = { Text("No HP/WA") }, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = tahap.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tahap") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
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
