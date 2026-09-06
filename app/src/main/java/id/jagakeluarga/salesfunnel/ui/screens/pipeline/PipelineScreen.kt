package id.jagakeluarga.salesfunnel.ui.screens.pipeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline
import id.jagakeluarga.salesfunnel.ui.common.warnaTahap
import kotlinx.coroutines.launch

@Composable
fun PipelineScreen(
    windowSizeClass: WindowSizeClass,
    prospekList: List<Prospek>,
    onTambahProspek: () -> Unit,
    onBukaProspek: (Prospek) -> Unit,
) {
    val tahapList = TahapPipeline.entries.toList()
    val grouped = tahapList.associateWith { tahap -> prospekList.filter { it.tahap == tahap } }
    val isWide = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pipeline") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onTambahProspek) { Text("+") }
        },
    ) { padding ->
        if (isWide) {
            // Layar lebar: semua kolom tahap tampil berdampingan, masing-masing scroll sendiri
            LazyRow(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(tahapList) { tahap ->
                    PipelineColumn(
                        tahap = tahap,
                        items = grouped[tahap].orEmpty(),
                        width = 280.dp,
                        onBukaProspek = onBukaProspek,
                    )
                }
            }
        } else {
            // HP: tahap sebagai halaman yang di-swipe, jadi jumlah prospek di satu
            // tahap tidak pernah menggeser posisi tahap lain.
            val pagerState = rememberPagerState(pageCount = { tahapList.size })
            val scope = rememberCoroutineScope()

            Column(Modifier.padding(padding).fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    tahapList.forEachIndexed { index, tahap ->
                        val jumlah = grouped[tahap].orEmpty().size
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text("${tahap.label} ($jumlah)") },
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top,
                ) { page ->
                    val tahap = tahapList[page]
                    val items = grouped[tahap].orEmpty()
                    if (items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text(
                                "Belum ada prospek di tahap ${tahap.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(items, key = { it.id }) { prospek ->
                                Box(Modifier.animateItem()) {
                                    ProspekCard(prospek, onClick = { onBukaProspek(prospek) })
                                }
                            }
                        }
                    }
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
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(warnaTahap(tahap))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${tahap.label} (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items, key = { it.id }) { prospek ->
                Box(Modifier.animateItem()) {
                    ProspekCard(prospek, onClick = { onBukaProspek(prospek) })
                }
            }
        }
    }
}

@Composable
private fun ProspekCard(prospek: Prospek, onClick: () -> Unit) {
    val warna = warnaTahap(prospek.tahap)
    val inisial = prospek.nama.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(warna),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Text(inisial, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(prospek.nama, fontWeight = FontWeight.SemiBold)
            prospek.nomorTelepon?.let { nomor ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(nomor, style = MaterialTheme.typography.bodySmall)
                }
            }
            prospek.estimasiPremi?.let {
                Text(
                    "Est. Rp ${"%,d".format(it).replace(',', '.')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = warna,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(warna)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                prospek.tahap.label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
