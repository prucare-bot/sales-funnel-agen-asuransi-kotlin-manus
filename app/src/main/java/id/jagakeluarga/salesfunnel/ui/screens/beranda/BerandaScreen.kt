package id.jagakeluarga.salesfunnel.ui.screens.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline
import id.jagakeluarga.salesfunnel.ui.common.warnaTahap
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    prospekList: List<Prospek>,
    agendaList: List<Agenda>,
    namaAgen: String = "Densus",
    onHomeClick: () -> Unit = {},
) {
    var periodeTerpilih by remember { mutableStateOf(InsightPeriod.LIFETIME) }
    var showQuickSearch by remember { mutableStateOf(false) }
    var quickSearchQuery by remember { mutableStateOf("") }
    var periodeMenuTerbuka by remember { mutableStateOf(false) }
    val sekarang = System.currentTimeMillis()
    val awalInsight = periodeTerpilih.startMillis(sekarang)
    val akhirInsight = if (periodeTerpilih == InsightPeriod.YESTERDAY) startOfDay(sekarang) else null
    val prospekInsight = prospekList.filter { prospek ->
        (awalInsight == null || prospek.dibuatPada >= awalInsight) &&
            (akhirInsight == null || prospek.dibuatPada < akhirInsight)
    }

    val jam = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val sapaan = when {
        jam < 11 -> "Selamat pagi"
        jam < 15 -> "Selamat siang"
        jam < 18 -> "Selamat sore"
        else -> "Selamat malam"
    }

    val awalHari = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val akhirHari = awalHari + 24 * 60 * 60 * 1000
    val agendaHariIni = agendaList.filter { it.waktuMulai in awalHari until akhirHari && !it.selesai }
        .sortedBy { it.waktuMulai }

    val grouped = TahapPipeline.entries.associateWith { tahap -> prospekInsight.count { it.tahap == tahap } }
    val maxJumlah = (grouped.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val totalProspek = prospekInsight.size.coerceAtLeast(1)
    val closingBulanIni = prospekList.count { prospek ->
        prospek.tahap == TahapPipeline.CLOSING && isBulanSama(prospek.diperbaruiPada, System.currentTimeMillis())
    }
    val totalProspekAktif = prospekInsight.count { it.tahap != TahapPipeline.CLOSING }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HeroWelcomeCard(sapaan = sapaan, namaAgen = namaAgen)

            ExposedDropdownMenuBox(
                expanded = periodeMenuTerbuka,
                onExpandedChange = { periodeMenuTerbuka = it },
            ) {
                OutlinedTextField(
                    value = periodeTerpilih.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Periode Insights") },
                    leadingIcon = { Icon(Icons.Filled.Tune, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = periodeMenuTerbuka,
                    onDismissRequest = { periodeMenuTerbuka = false },
                ) {
                    InsightPeriod.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { periodeTerpilih = option; periodeMenuTerbuka = false },
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RingkasanCard(Modifier.weight(1f), "$totalProspekAktif", "Prospek aktif")
                RingkasanCard(Modifier.weight(1f), "$closingBulanIni", "Closing bulan ini")
                RingkasanCard(Modifier.weight(1f), "${agendaHariIni.size}", "Agenda hari ini")
            }

            Column {
                Text("Dashboard Statistik Prospek", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                TahapPipeline.entries.forEach { tahap ->
                    val jumlah = grouped[tahap] ?: 0
                    FunnelBar(
                        label = tahap.label,
                        jumlah = jumlah,
                        persentase = jumlah * 100 / totalProspek,
                        maxJumlah = maxJumlah,
                        warna = warnaTahap(tahap),
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Column {
                Text("Agenda Hari Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (agendaHariIni.isEmpty()) {
                    Text(
                        "Tidak ada agenda hari ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val fmtJam = SimpleDateFormat("HH:mm", Locale("id", "ID"))
                    agendaHariIni.forEach { agenda ->
                        ListItem(
                            headlineContent = { Text(agenda.judul) },
                            supportingContent = { Text(agenda.jenis.label) },
                            trailingContent = { Text(fmtJam.format(Date(agenda.waktuMulai))) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showQuickSearch) {
        val query = quickSearchQuery.trim()
        val hasilProspek = if (query.isBlank()) emptyList() else prospekList.filter { it.nama.contains(query, ignoreCase = true) }
        val hasilAgenda = if (query.isBlank()) emptyList() else agendaList.filter { it.judul.contains(query, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showQuickSearch = false; quickSearchQuery = "" },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Pencarian Cepat")
                    IconButton(onClick = { showQuickSearch = false; quickSearchQuery = "" }) { Icon(Icons.Filled.Close, contentDescription = "Tutup") }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quickSearchQuery,
                        onValueChange = { quickSearchQuery = it },
                        placeholder = { Text("Cari prospek atau agenda...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (query.isNotBlank() && hasilProspek.isEmpty() && hasilAgenda.isEmpty()) Text("Tidak ada hasil")
                    hasilProspek.forEach { Text("Prospek · ${it.nama}", style = MaterialTheme.typography.bodyLarge) }
                    hasilAgenda.forEach { Text("Agenda · ${it.judul}", style = MaterialTheme.typography.bodyLarge) }
                }
            },
            confirmButton = { TextButton(onClick = { showQuickSearch = false; quickSearchQuery = "" }) { Text("Tutup") } },
        )
    }
}

@Composable
private fun HeroWelcomeCard(sapaan: String, namaAgen: String) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(listOf(colors.primary, colors.secondary)))
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.onPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(32.dp))
            }
            Column {
                Text(sapaan, style = MaterialTheme.typography.titleMedium, color = colors.onPrimary.copy(alpha = 0.88f))
                Text(namaAgen, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.onPrimary)
                Text("Kelola prospek dan agenda Anda hari ini.", style = MaterialTheme.typography.bodySmall, color = colors.onPrimary.copy(alpha = 0.82f))
            }
        }
    }
}

@Composable
private fun RingkasanCard(modifier: Modifier = Modifier, angka: String, label: String) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(angka, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FunnelBar(label: String, jumlah: Int, persentase: Int, maxJumlah: Int, warna: Color) {
    val fraksi = (jumlah.toFloat() / maxJumlah.toFloat()).coerceIn(0.04f, 1f)
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$jumlah ($persentase%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp))
                .background(warna.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier.fillMaxWidth(fraksi).height(10.dp).clip(RoundedCornerShape(6.dp)).background(warna)
            )
        }
    }
}

private enum class InsightPeriod(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_3_MONTHS("Last 3 months"),
    LAST_6_MONTHS("Last 6 months"),
    LAST_12_MONTHS("Last 12 months"),
    LIFETIME("Lifetime");

    fun startMillis(now: Long): Long? = when (this) {
        LIFETIME -> null
        TODAY -> startOfDay(now)
        YESTERDAY -> startOfDay(now) - DAY_MILLIS
        LAST_7_DAYS -> now - 7 * DAY_MILLIS
        LAST_30_DAYS -> now - 30 * DAY_MILLIS
        LAST_3_MONTHS -> now - 90 * DAY_MILLIS
        LAST_6_MONTHS -> now - 180 * DAY_MILLIS
        LAST_12_MONTHS -> now - 365 * DAY_MILLIS
    }
}

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun isBulanSama(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH)
}
