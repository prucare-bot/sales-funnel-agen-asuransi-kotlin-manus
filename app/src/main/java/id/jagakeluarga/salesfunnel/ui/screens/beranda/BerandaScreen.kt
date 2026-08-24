package id.jagakeluarga.salesfunnel.ui.screens.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
) {
    var periodeTerpilih by remember { mutableStateOf(InsightPeriod.LIFETIME) }
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

    Scaffold(topBar = { TopAppBar(title = { Text("Beranda") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("$sapaan, $namaAgen 👋", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

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
