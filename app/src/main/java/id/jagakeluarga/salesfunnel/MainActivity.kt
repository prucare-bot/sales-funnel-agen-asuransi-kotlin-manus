package id.jagakeluarga.salesfunnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import id.jagakeluarga.salesfunnel.ui.AppViewModel
import id.jagakeluarga.salesfunnel.ui.navigation.AdaptiveScaffold
import id.jagakeluarga.salesfunnel.ui.navigation.Destination
import id.jagakeluarga.salesfunnel.ui.screens.agenda.AgendaScreen
import id.jagakeluarga.salesfunnel.ui.screens.nasabah.NasabahScreen
import id.jagakeluarga.salesfunnel.ui.screens.pipeline.PipelineScreen
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
            var current by remember { mutableStateOf(Destination.PIPELINE) }

            val prospekList by viewModel.prospekList.collectAsState()
            val agendaList by viewModel.agendaList.collectAsState()
            val nasabahList by viewModel.nasabahList.collectAsState()

            SalesFunnelTheme {
                AdaptiveScaffold(
                    windowSizeClass = windowSizeClass,
                    current = current,
                    onNavigate = { current = it },
                ) {
                    when (current) {
                        Destination.PIPELINE -> PipelineScreen(
                            windowSizeClass = windowSizeClass,
                            prospekList = prospekList,
                            onTambahProspek = { current = Destination.PROSPEK },
                            onBukaProspek = { current = Destination.PROSPEK },
                        )
                        Destination.PROSPEK -> ProspekScreen(
                            prospekList = prospekList,
                            onSimpan = viewModel::saveProspek,
                            onHapus = viewModel::deleteProspek,
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
                        )
                    }
                }
            }
        }
    }
}
