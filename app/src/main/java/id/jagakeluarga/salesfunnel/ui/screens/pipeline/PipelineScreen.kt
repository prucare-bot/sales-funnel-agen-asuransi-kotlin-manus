package id.jagakeluarga.salesfunnel.ui.screens.pipeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PipelineScreen(
    windowSizeClass: WindowSizeClass,
    prospekList: List<Prospek>,
    onTambahProspek: () -> Unit,
    onBukaProspek: (Prospek) -> Unit,
) {
    val grouped = TahapPipeline.entries.associateWith { tahap ->
        prospekList.filter { it.tahap == tahap }
    }
    val isWide = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pipeline") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onTambahProspek) { Text("+") }
        },
    ) { padding ->
        if (isWide) {
            // Layar lebar: semua kolom tahap tampil berdampingan
            LazyRow(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(TahapPipeline.entries.toList()) { tahap ->
                    PipelineColumn(
                        tahap = tahap,
                        items = grouped[tahap].orEmpty(),
                        width = 280.dp,
                        onBukaProspek = onBukaProspek,
                    )
                }
            }
        } else {
            // HP sempit: satu kolom, per-tahap sebagai section, scroll vertikal
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(TahapPipeline.entries.toList()) { tahap ->
                    PipelineSection(
                        tahap = tahap,
                        items = grouped[tahap].orEmpty(),
                        onBukaProspek = onBukaProspek,
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineColumn(
    tahap: TahapPipeline,
    items: List<Prospek>,
    width: androidx.compose.ui.unit.Dp,
    onBukaProspek: (Prospek) -> Unit,
) {
    Column(Modifier.width(width)) {
        Text(
            "${tahap.label} (${items.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { prospek -> ProspekCard(prospek, onClick = { onBukaProspek(prospek) }) }
        }
    }
}

@Composable
private fun PipelineSection(
    tahap: TahapPipeline,
    items: List<Prospek>,
    onBukaProspek: (Prospek) -> Unit,
) {
    Column {
        Text(
            "${tahap.label} (${items.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { prospek -> ProspekCard(prospek, onClick = { onBukaProspek(prospek) }) }
        }
    }
}

@Composable
private fun ProspekCard(prospek: Prospek, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(prospek.nama, fontWeight = FontWeight.SemiBold)
            prospek.nomorTelepon?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            prospek.estimasiPremi?.let {
                Text(
                    "Est. Rp ${"%,d".format(it).replace(',', '.')}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
