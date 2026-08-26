package id.jagakeluarga.salesfunnel

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
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
import id.jagakeluarga.salesfunnel.backup.LocalBackupScheduler
import id.jagakeluarga.salesfunnel.notification.AgendaReminderWorker
import id.jagakeluarga.salesfunnel.security.AppLockGate
import id.jagakeluarga.salesfunnel.security.AppLockManager
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
import id.jagakeluarga.salesfunnel.ui.theme.AppThemeColor
import id.jagakeluarga.salesfunnel.ui.theme.SalesFunnelTheme

class MainActivity : FragmentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onStop() {
        super.onStop()
        AppLockManager.markBackground(this)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBackupScheduler.schedule(this)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            var current by remember {
                mutableStateOf(
                    if (intent?.action == AgendaReminderWorker.ACTION_OPEN_AGENDA) Destination.AGENDA else Destination.BERANDA,
                )
            }
            var selectedProspekId by remember { mutableStateOf<String?>(null) }
            val settings = remember { getSharedPreferences("sales_funnel_settings", MODE_PRIVATE) }
            var namaAgen by remember {
                mutableStateOf(settings.getString("nama_user", "Densus") ?: "Densus")
            }
            var targetClosing by remember { mutableStateOf(settings.getInt("target_closing", 10)) }
            var targetPremi by remember { mutableStateOf(settings.getLong("target_premi", 0L)) }
            var tema by remember {
                mutableStateOf(
                    runCatching { AppThemeColor.valueOf(settings.getString("theme_color", AppThemeColor.HIJAU.name) ?: AppThemeColor.HIJAU.name) }
                        .getOrDefault(AppThemeColor.HIJAU)
                )
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
            val errorMessage by viewModel.errorMessage.collectAsState()

            LaunchedEffect(errorMessage) {
                errorMessage?.let { message ->
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }

            val prospekTerpilih = selectedProspekId?.let { id -> prospekList.find { it.id == id } }

            SalesFunnelTheme(theme = tema) {
                AppLockGate {
                    if (prospekTerpilih != null) {
                    val riwayatStatus by viewModel.statusHistoryForProspek(prospekTerpilih.id).collectAsState(initial = emptyList())
                    val riwayatAktivitas by viewModel.aktivitasForProspek(prospekTerpilih.id).collectAsState(initial = emptyList())
                    DetailProspekScreen(
                        prospek = prospekTerpilih,
                        riwayatAgenda = agendaList.filter { it.prospekId == prospekTerpilih.id },
                        riwayatStatus = riwayatStatus,
                        riwayatAktivitas = riwayatAktivitas,
                        sudahJadiNasabah = nasabahList.any { it.prospekAsalId == prospekTerpilih.id },
                        onKembali = { selectedProspekId = null },
                        onSimpanProspek = viewModel::saveProspek,
                        onHapusProspek = { viewModel.deleteProspek(it) },
                        onSimpanAktivitas = viewModel::saveAktivitas,
                        onKonversiNasabah = { prospek, produk, nomorPolis, onResult ->
                            viewModel.convertProspekToNasabah(prospek, produk, nomorPolis) { result ->
                                val message = when (result) {
                                    is id.jagakeluarga.salesfunnel.data.repository.SalesFunnelRepository.ConversionResult.Created -> "Nasabah berhasil dibuat."
                                    is id.jagakeluarga.salesfunnel.data.repository.SalesFunnelRepository.ConversionResult.AlreadyConverted -> "Prospek ini sudah menjadi nasabah."
                                    is id.jagakeluarga.salesfunnel.data.repository.SalesFunnelRepository.ConversionResult.DuplicatePhone -> "Nomor HP/WA sudah digunakan oleh nasabah lain."
                                }
                                onResult(message)
                            }
                        },
                        onSimpanAgenda = viewModel::saveAgenda,
                        onToggleSelesai = { agenda -> viewModel.saveAgenda(agenda.copy(selesai = !agenda.selesai)) },
                    )
                } else {
                    AdaptiveScaffold(
                        windowSizeClass = windowSizeClass,
                        current = current,
                        onNavigate = { current = it },
                        onQuickSearch = { current = Destination.PROSPEK },
                        headerSubtitle = namaAgen,
                    ) { destination ->
                        when (destination) {
                            Destination.BERANDA -> BerandaScreen(
                                prospekList = prospekList,
                                agendaList = agendaList,
                                namaAgen = namaAgen,
                                targetClosing = targetClosing,
                                targetPremi = targetPremi,
                                onHomeClick = { current = Destination.BERANDA },
                                onBukaAgenda = { current = Destination.AGENDA },
                                onTandaiAgendaSelesai = { agenda -> viewModel.saveAgenda(agenda.copy(selesai = true)) },
                            )
                            Destination.PIPELINE -> PipelineScreen(
                                windowSizeClass = windowSizeClass,
                                prospekList = prospekList,
                                onTambahProspek = { current = Destination.PROSPEK },
                                onBukaProspek = { selectedProspekId = it.id },
                            )
                            Destination.PROSPEK -> ProspekScreen(
                                prospekList = prospekList,
                                nasabahList = nasabahList,
                                agendaList = agendaList,
                                onSimpan = viewModel::saveProspek,
                                onHapus = viewModel::deleteProspek,
                                onBukaDetail = { selectedProspekId = it.id },
                            )
                            Destination.AGENDA -> AgendaScreen(
                                agendaList = agendaList,
                                prospekList = prospekList,
                                onSimpan = viewModel::saveAgenda,
                                onHapus = viewModel::deleteAgenda,
                                onToggleSelesai = { agenda -> viewModel.saveAgenda(agenda.copy(selesai = !agenda.selesai)) },
                            )
                            Destination.NASABAH -> NasabahScreen(
                                nasabahList = nasabahList,
                                onSimpan = viewModel::saveNasabah,
                                onHapus = viewModel::deleteNasabah,
                            )
                            Destination.SETTINGS -> SettingsScreen(
                                dbFilePath = getDatabasePath("sales_funnel.db").absolutePath,
                                onNamaUserChanged = { namaAgen = it },
                                selectedTheme = tema,
                                onThemeChanged = {
                                    tema = it
                                    settings.edit().putString("theme_color", it.name).apply()
                                },
                                prospekList = prospekList,
                                agendaList = agendaList,
                                nasabahList = nasabahList,
                                targetClosing = targetClosing,
                                targetPremi = targetPremi,
                                onTargetChanged = { closing, premi ->
                                    targetClosing = closing
                                    targetPremi = premi
                                    settings.edit()
                                        .putInt("target_closing", closing)
                                        .putLong("target_premi", premi)
                                        .apply()
                                },
                                onDatabaseRestored = viewModel::reloadAfterRestore,
                            )
                        }
                    }
                }
            }
        }
    }
}
}
