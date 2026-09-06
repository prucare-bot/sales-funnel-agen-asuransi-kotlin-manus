package id.jagakeluarga.salesfunnel.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.ui.common.warnaTahap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun initialOf(nama: String): String = nama.trim().split(" ")
    .filter { it.isNotBlank() }.take(2)
    .joinToString("") { it.first().uppercase() }
    .ifBlank { "?" }

/**
 * Pencarian lintas layar: Prospek, Nasabah, dan Agenda dalam satu tempat.
 * Dipicu dari ikon kaca pembesar di header (sebelumnya cuma pindah ke tab
 * Prospek tanpa pencarian sungguhan).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    prospekList: List<Prospek>,
    nasabahList: List<Nasabah>,
    agendaList: List<Agenda>,
    onTutup: () -> Unit,
    onBukaProspek: (Prospek) -> Unit,
    onBukaNasabah: (Nasabah) -> Unit,
    onBukaAgenda: (Agenda) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val fmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")) }

    val q = query.trim()
    val hasilProspek = if (q.isBlank()) emptyList() else prospekList.filter { p ->
        p.nama.contains(q, ignoreCase = true) ||
            p.nomorTelepon?.contains(q, ignoreCase = true) == true ||
            p.kotaDomisili?.contains(q, ignoreCase = true) == true
    }
    val hasilNasabah = if (q.isBlank()) emptyList() else nasabahList.filter { n ->
        n.nama.contains(q, ignoreCase = true) ||
            n.produk.contains(q, ignoreCase = true) ||
            n.nomorPolis?.contains(q, ignoreCase = true) == true
    }
    val hasilAgenda = if (q.isBlank()) emptyList() else agendaList.filter { a ->
        a.judul.contains(q, ignoreCase = true)
    }
    val totalHasil = hasilProspek.size + hasilNasabah.size + hasilAgenda.size

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Cari...") },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Hapus")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onTutup) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
            )
        },
    ) { padding ->
        if (q.isBlank()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ketik nama, nomor HP, kota, produk, atau nomor polis",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (totalHasil == 0) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tidak ada hasil untuk \"$q\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (hasilProspek.isNotEmpty()) {
                    item { SearchGroupHeader("Prospek", hasilProspek.size) }
                    items(hasilProspek, key = { "p_" + it.id }) { prospek ->
                        val warna = warnaTahap(prospek.tahap)
                        SearchResultRow(
                            leading = {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(warna),
                                    contentAlignment = Alignment.Center,
                                ) { Text(initialOf(prospek.nama), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium) }
                            },
                            title = prospek.nama,
                            subtitle = listOfNotNull(prospek.tahap.label, prospek.nomorTelepon, prospek.kotaDomisili).joinToString(" · "),
                            onClick = { onBukaProspek(prospek) },
                        )
                    }
                }
                if (hasilNasabah.isNotEmpty()) {
                    item { SearchGroupHeader("Nasabah", hasilNasabah.size) }
                    items(hasilNasabah, key = { "n_" + it.id }) { nasabah ->
                        SearchResultRow(
                            leading = {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            },
                            title = nasabah.nama,
                            subtitle = listOfNotNull(nasabah.produk, nasabah.nomorPolis).joinToString(" · "),
                            onClick = { onBukaNasabah(nasabah) },
                        )
                    }
                }
                if (hasilAgenda.isNotEmpty()) {
                    item { SearchGroupHeader("Agenda", hasilAgenda.size) }
                    items(hasilAgenda, key = { "a_" + it.id }) { agenda ->
                        SearchResultRow(
                            leading = {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(18.dp))
                                }
                            },
                            title = agenda.judul,
                            subtitle = "${agenda.jenis.label} · ${fmt.format(Date(agenda.waktuMulai))}",
                            onClick = { onBukaAgenda(agenda) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchGroupHeader(label: String, jumlah: Int) {
    Text(
        "$label ($jumlah)",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp, start = 4.dp),
    )
}

@Composable
private fun SearchResultRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
