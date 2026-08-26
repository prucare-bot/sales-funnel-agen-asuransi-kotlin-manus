package id.jagakeluarga.salesfunnel.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.jagakeluarga.salesfunnel.data.AppDatabase
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.entity.ProspekAktivitas
import id.jagakeluarga.salesfunnel.data.repository.SalesFunnelRepository
import id.jagakeluarga.salesfunnel.notification.AgendaScheduler
import id.jagakeluarga.salesfunnel.notification.BirthdayReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    @Volatile private var repository = SalesFunnelRepository(AppDatabase.getInstance(application))
    private val databaseGeneration = MutableStateFlow(0)
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun clearError() { _errorMessage.value = null }
    private fun reportError(prefix: String, error: Throwable) {
        _errorMessage.value = "$prefix: ${error.message ?: "silakan coba lagi"}"
    }

    val prospekList = databaseGeneration.flatMapLatest { repository.prospekList }.catch { emit(emptyList()) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val agendaList = databaseGeneration.flatMapLatest { repository.agendaList }.catch { emit(emptyList()) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val nasabahList = databaseGeneration.flatMapLatest { repository.nasabahList }.catch { emit(emptyList()) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    /** Rebinds all observable flows to the database file just restored on disk. */
    fun reloadAfterRestore() {
        repository = SalesFunnelRepository(AppDatabase.getInstance(getApplication()))
        databaseGeneration.value += 1
    }

    init {
        viewModelScope.launch {
            repository.nasabahList.first().forEach { nasabah ->
                BirthdayReminderScheduler.scheduleIfBirthdayToday(getApplication(), nasabah)
            }
        }
    }

    fun saveProspek(prospek: Prospek) = viewModelScope.launch {
        runCatching { repository.saveProspek(prospek) }
            .onFailure { reportError("Prospek gagal disimpan", it) }
    }
    fun deleteProspek(prospek: Prospek) = viewModelScope.launch {
        runCatching { repository.deleteProspek(prospek) }
            .onFailure { reportError("Prospek gagal dihapus", it) }
    }

    fun statusHistoryForProspek(prospekId: String) = repository.statusHistoryForProspek(prospekId)
    fun aktivitasForProspek(prospekId: String) = repository.aktivitasForProspek(prospekId)

    fun saveAktivitas(aktivitas: ProspekAktivitas) = viewModelScope.launch {
        runCatching { repository.saveAktivitas(aktivitas) }
            .onFailure { reportError("Aktivitas gagal disimpan", it) }
    }

    fun convertProspekToNasabah(
        prospek: Prospek,
        produk: String,
        nomorPolis: String?,
        onResult: (SalesFunnelRepository.ConversionResult) -> Unit,
    ) = viewModelScope.launch {
        val result = runCatching { repository.convertProspekToNasabah(prospek, produk, nomorPolis) }
            .getOrElse {
                reportError("Prospek gagal dikonversi", it)
                return@launch
            }
        if (result is SalesFunnelRepository.ConversionResult.Created) {
            BirthdayReminderScheduler.scheduleIfBirthdayToday(getApplication(), result.nasabah)
        }
        onResult(result)
    }

    fun saveAgenda(agenda: Agenda) = viewModelScope.launch {
        runCatching {
            repository.saveAgenda(agenda)
            AgendaScheduler.schedule(getApplication(), agenda)
        }.onFailure { reportError("Agenda gagal disimpan", it) }
    }

    fun deleteAgenda(agenda: Agenda) = viewModelScope.launch {
        runCatching {
            AgendaScheduler.cancel(getApplication(), agenda.id)
            repository.deleteAgenda(agenda)
        }.onFailure { reportError("Agenda gagal dihapus", it) }
    }

    fun saveNasabah(nasabah: Nasabah) = viewModelScope.launch {
        runCatching {
            repository.saveNasabah(nasabah)
            BirthdayReminderScheduler.cancel(getApplication(), nasabah.id)
            BirthdayReminderScheduler.scheduleIfBirthdayToday(getApplication(), nasabah)
        }.onFailure { reportError("Nasabah gagal disimpan", it) }
    }
    fun deleteNasabah(nasabah: Nasabah) = viewModelScope.launch {
        runCatching {
            BirthdayReminderScheduler.cancel(getApplication(), nasabah.id)
            repository.deleteNasabah(nasabah)
        }.onFailure { reportError("Nasabah gagal dihapus", it) }
    }
}
