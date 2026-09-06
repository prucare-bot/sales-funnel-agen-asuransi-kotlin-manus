package id.jagakeluarga.salesfunnel.ui.screens.prospek

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline
import id.jagakeluarga.salesfunnel.ui.screens.prospek.ProspekFilterDialog
import id.jagakeluarga.salesfunnel.ui.common.ContactPickerDialog
import id.jagakeluarga.salesfunnel.ui.common.WhatsAppTemplateDialog
import androidx.core.content.ContextCompat
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppHelper

@Composable
fun ProspekScreen(
    prospekList: List<Prospek>,
    nasabahList: List<id.jagakeluarga.salesfunnel.data.entity.Nasabah> = emptyList(),
    agendaList: List<id.jagakeluarga.salesfunnel.data.entity.Agenda> = emptyList(),
    onSimpan: (Prospek) -> Unit,
    onHapus: (Prospek) -> Unit,
    onBukaDetail: (Prospek) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var kataKunci by remember { mutableStateOf("") }
    var tanggalMulai by remember { mutableStateOf<Long?>(null) }
    var tanggalAkhir by remember { mutableStateOf<Long?>(null) }
    var tahapFilter by remember { mutableStateOf<TahapPipeline?>(null) }
    var kotaFilter by remember { mutableStateOf<String?>(null) }
    var sumberFilter by remember { mutableStateOf<String?>(null) }
    var hanyaBelumKonversi by remember { mutableStateOf(false) }
    var hanyaTanpaFollowUp by remember { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }

    val hasilFilter = remember(
        prospekList,
        nasabahList,
        agendaList,
        kataKunci,
        tanggalMulai,
        tanggalAkhir,
        tahapFilter,
        kotaFilter,
        sumberFilter,
        hanyaBelumKonversi,
        hanyaTanpaFollowUp,
    ) {
        prospekList.filter { prospek ->
            val cocokKataKunci = kataKunci.isBlank() ||
                prospek.nama.contains(kataKunci, ignoreCase = true) ||
                prospek.nomorTelepon.orEmpty().contains(kataKunci, ignoreCase = true)
            val cocokMulai = tanggalMulai == null || prospek.dibuatPada >= tanggalMulai!!
            val cocokAkhir = tanggalAkhir == null || prospek.dibuatPada <= tanggalAkhir!!
            val cocokTahap = tahapFilter == null || prospek.tahap == tahapFilter
            val cocokKota = kotaFilter == null || prospek.kotaDomisili.orEmpty().contains(kotaFilter!!, ignoreCase = true)
            val cocokSumber = sumberFilter == null || prospek.sumberProspek == sumberFilter
            val sudahDikonversi = nasabahList.any { it.prospekAsalId == prospek.id }
            val punyaFollowUp = agendaList.any { it.prospekId == prospek.id }
            val cocokKonversi = !hanyaBelumKonversi || !sudahDikonversi
            val cocokFollowUp = !hanyaTanpaFollowUp || !punyaFollowUp
            cocokKataKunci && cocokMulai && cocokAkhir && cocokTahap && cocokKota && cocokSumber && cocokKonversi && cocokFollowUp
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Daftar Prospek") })
        },
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
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )
            OutlinedButton(
                onClick = { showFilter = true },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (tanggalMulai != null || tanggalAkhir != null || tahapFilter != null || kotaFilter != null || sumberFilter != null || hanyaBelumKonversi || hanyaTanpaFollowUp) "Filter aktif" else "Filter lanjutan prospek",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = hanyaBelumKonversi,
                    onClick = { hanyaBelumKonversi = !hanyaBelumKonversi },
                    label = { Text("Belum nasabah", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = hanyaTanpaFollowUp,
                    onClick = { hanyaTanpaFollowUp = !hanyaTanpaFollowUp },
                    label = { Text("Tanpa follow-up", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.weight(1f),
                )
            }

            if (hasilFilter.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (kataKunci.isBlank() && tanggalMulai == null && tanggalAkhir == null && tahapFilter == null && kotaFilter == null && sumberFilter == null && !hanyaBelumKonversi && !hanyaTanpaFollowUp) "Belum ada prospek" else "Tidak ada prospek yang sesuai filter",
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
                        Column(Modifier.animateItem()) {
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
    }

    if (showFilter) {
        ProspekFilterDialog(
            tanggalMulai = tanggalMulai,
            tanggalAkhir = tanggalAkhir,
            tahapAwal = tahapFilter,
            kotaAwal = kotaFilter,
            sumberAwal = sumberFilter,
            onDismiss = { showFilter = false },
            onApply = { mulai, akhir, tahap, kota, sumber ->
                tanggalMulai = mulai
                tanggalAkhir = akhir
                tahapFilter = tahap
                kotaFilter = kota
                sumberFilter = sumber
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
    var showTemplateDialog by remember { mutableStateOf(false) }

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
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
        backgroundContent = {
            val (warna, ikon, alignment) = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Triple(Color(0xFF3F8F5F), Icons.Filled.Phone, Alignment.CenterStart)
                SwipeToDismissBoxValue.EndToStart -> Triple(Color(0xFFC1502E), Icons.Filled.Delete, Alignment.CenterEnd)
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
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            val warnaTahapIni = id.jagakeluarga.salesfunnel.ui.common.warnaTahap(prospek.tahap)
            val inisial = prospek.nama.trim().split(" ")
                .filter { it.isNotBlank() }.take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "?" }
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(warnaTahapIni),
                        contentAlignment = Alignment.Center,
                    ) { Text(inisial, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                },
                headlineContent = { Text(prospek.nama) },
                supportingContent = { Text("No HP/WA: ${prospek.nomorTelepon ?: "-"}") },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                                .background(warnaTahapIni)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(prospek.tahap.label, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        IconButton(
                            enabled = !prospek.nomorTelepon.isNullOrBlank(),
                            onClick = { showTemplateDialog = true },
                        ) { Icon(Icons.Filled.Send, contentDescription = "Kirim WhatsApp") }
                    }
                },
                modifier = Modifier.clickable(onClick = onKlik),
            )
        }
    }

    if (showTemplateDialog) {
        WhatsAppTemplateDialog(
            nama = prospek.nama,
            onDismiss = { showTemplateDialog = false },
            onSend = { message ->
                WhatsAppHelper.openChat(context, prospek.nomorTelepon, message)
                showTemplateDialog = false
            },
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
    var kotaDomisili by remember { mutableStateOf(initial?.kotaDomisili ?: "") }
    var sumberProspek by remember {
        mutableStateOf(if (initial == null) "Organik" else initial.sumberProspek.orEmpty())
    }
    var estimasiPremi by remember(initial?.id) { mutableStateOf(initial?.estimasiPremi?.toString().orEmpty()) }
    var tahap by remember { mutableStateOf(initial?.tahap ?: TahapPipeline.PROSPEK) }
    var expanded by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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

    if (!showContactPicker || initial != null) AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = Color.White)
            }
        },
        title = { Text(if (initial == null) "Tambah Prospek" else "Edit Prospek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (initial == null) {
                    OutlinedButton(
                        onClick = ::openContactPicker,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih Kontak")
                    }
                }
                OutlinedTextField(
                    nama, { nama = it },
                    label = { Text("Nama") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    telepon, { telepon = it },
                    label = { Text("No HP/WA") },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = kotaDomisili,
                    onValueChange = { kotaDomisili = it },
                    label = { Text("Kota domisili") },
                    leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Text("Sumber prospek", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = sumberProspek == "Referensi",
                        onClick = { sumberProspek = "Referensi" },
                        label = { Text("Referensi") },
                        modifier = Modifier.weight(1f),
                    )
                    FilterChip(
                        selected = sumberProspek == "Organik",
                        onClick = { sumberProspek = "Organik" },
                        label = { Text("Organik") },
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = estimasiPremi,
                    onValueChange = { estimasiPremi = it.filter(Char::isDigit) },
                    label = { Text("Estimasi premi") },
                    leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = tahap.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tahap") },
                        leadingIcon = { Icon(Icons.Filled.Timeline, contentDescription = null) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        TahapPipeline.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.label) }, onClick = { tahap = t; expanded = false })
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
                if (nama.isNotBlank()) {
                    onSimpan(
                        (initial ?: Prospek(nama = nama)).copy(
                            nama = nama,
                            nomorTelepon = telepon.ifBlank { null },
                            kotaDomisili = kotaDomisili.trim().ifBlank { null },
                            sumberProspek = sumberProspek.trim().ifBlank { null },
                            estimasiPremi = estimasiPremi.toLongOrNull(),
                            tahap = tahap,
                            diperbaruiPada = System.currentTimeMillis(),
                        )
                    )
                    validationError = null
                } else {
                    validationError = "Nama prospek wajib diisi."
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
