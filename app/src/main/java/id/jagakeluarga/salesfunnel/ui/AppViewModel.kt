package id.jagakeluarga.salesfunnel.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.jagakeluarga.salesfunnel.data.AppDatabase
import id.jagakeluarga.salesfunnel.data.entity.Agenda
import id.jagakeluarga.salesfunnel.data.entity.Nasabah
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import id.jagakeluarga.salesfunnel.data.repository.SalesFunnelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SalesFunnelRepository(AppDatabase.getInstance(application))

    val prospekList = repository.prospekList.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val agendaList = repository.agendaList.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val nasabahList = repository.nasabahList.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun saveProspek(prospek: Prospek) = viewModelScope.launch { repository.saveProspek(prospek) }
    fun deleteProspek(prospek: Prospek) = viewModelScope.launch { repository.deleteProspek(prospek) }

    fun saveAgenda(agenda: Agenda) = viewModelScope.launch { repository.saveAgenda(agenda) }
    fun deleteAgenda(agenda: Agenda) = viewModelScope.launch { repository.deleteAgenda(agenda) }

    fun saveNasabah(nasabah: Nasabah) = viewModelScope.launch { repository.saveNasabah(nasabah) }
    fun deleteNasabah(nasabah: Nasabah) = viewModelScope.launch { repository.deleteNasabah(nasabah) }
}
