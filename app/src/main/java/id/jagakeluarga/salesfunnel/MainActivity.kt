package id.jagakeluarga.salesfunnel

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import id.jagakeluarga.salesfunnel.ui.AppViewModel
import id.jagakeluarga.salesfunnel.ui.navigation.AdaptiveScaffold
import id.jagakeluarga.salesfunnel.ui.navigation.Destination
import id.jagakeluarga.salesfunnel.ui.screens.agenda.AgendaScreen
import id.jagakeluarga.salesfunnel.ui.screens.beranda.BerandaScreen
import id.jagakeluarga.salesfunnel.ui.screens.nasabah.NasabahScreen
import id.jagakeluarga.salesfunnel.ui.screens.pipeline.PipelineScreen
import id.jagakeluarga.salesfunnel.ui.screens.prospek.DetailProspekScreen
import id.jagakeluarga.salesfunnel.ui.screens.prospek.ProspekScreen
import id.jagakeluarga.salesfunnel.ui.screens.settings.SettingsScreen
import id.jagakeluarga.salesfunnel.ui.theme.SalesFunnelTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            var current by remember { mutableStateOf(Destination.BERANDA) }
            var selectedProspekId by remember { mutableStateOf<String?>(null) }
            var namaAgen by remember {
                mutableStateOf(getSharedPreferences("sales_funnel_settings", MODE_PRIVATE).getString("nama_user", "Densus") ?: "Densus")
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* hasil izin diabaikan; notifikasi hanya tidak akan muncul kalau ditolak */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val prospekList by viewModel.prospekList.collectAsState()
            val agendaList by viewModel.agendaList.collectAsState()
            val nasabahList by viewModel.nasabahList.collectAsState()

            val prospekTerpilih = selectedProspekId?.let { id -> prospekList.find { it.id == id } }

            SalesFunnelTheme {
                if (prospekTerpilih != null) {
                    DetailProspekScreen(
                        prospek = prospekTerpilih,
                        riwayatAgenda = agendaList.filter { it.prospekId == prospekTerpilih.id },
                        onKembali = { selectedProspekId = null },
                        onSimpanProspek = viewModel::saveProspek,
                        onHapusProspek = { viewModel.deleteProspek(it) },
                        onSimpanAgenda = viewModel::saveAgenda,
                        onToggleSelesai = { agenda -> viewModel.saveAgenda(agenda.copy(selesai = !agenda.selesai)) },
                    )
                } else {
                    AdaptiveScaffold(
                        windowSizeClass = windowSizeClass,
                        current = current,
                        onNavigate = { current = it },
                    ) {
                        when (current) {
                            Destination.BERANDA -> BerandaScreen(
                                prospekList = prospekList,
                                agendaList = agendaList,
                                namaAgen = namaAgen,
                            )
                            Destination.PIPELINE -> PipelineScreen(
                                windowSizeClass = windowSizeClass,
                                prospekList = prospekList,
                                onTambahProspek = { current = Destination.PROSPEK },
                                onBukaProspek = { selectedProspekId = it.id },
                            )
                            Destination.PROSPEK -> ProspekScreen(
                                prospekList = prospekList,
                                onSimpan = viewModel::saveProspek,
                                onHapus = viewModel::deleteProspek,
                                onBukaDetail = { selectedProspekId = it.id },
                            )
                            Destination.AGENDA -> AgendaScreen(
                                agendaList = agendaList,
                                prospekList = prospekList,
                                onSimpan = viewModel::saveAgenda,
                                onHapus = viewModel::deleteAgenda,
                            )
                            Destination.NASABAH -> NasabahScreen(
                                nasabahList = nasabahList,
                                onSimpan = viewModel::saveNasabah,
                                onHapus = viewModel::deleteNasabah,
                            )
                            Destination.SETTINGS -> SettingsScreen(
                                dbFilePath = getDatabasePath("sales_funnel.db").absolutePath,
                                onNamaUserChanged = { namaAgen = it },
                            )
                        }
                    }
                }
            }
        }
    }
}
