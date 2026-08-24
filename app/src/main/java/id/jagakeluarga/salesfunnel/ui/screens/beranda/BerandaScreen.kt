package id.jagakeluarga.salesfunnel.ui.screens.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun BerandaScreen(
    prospekList: List<Prospek>,
    agendaList: List<Agenda>,
    namaAgen: String = "Densus",
) {
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

    val grouped = TahapPipeline.entries.associateWith { tahap -> prospekList.count { it.tahap == tahap } }
    val maxJumlah = (grouped.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val totalProspek = prospekList.size.coerceAtLeast(1)
    val closingBulanIni = prospekList.count { prospek ->
        prospek.tahap == TahapPipeline.CLOSING && isBulanSama(prospek.diperbaruiPada, System.currentTimeMillis())
    }
    val totalProspekAktif = prospekList.count { it.tahap != TahapPipeline.CLOSING }

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

private fun isBulanSama(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH)
}
