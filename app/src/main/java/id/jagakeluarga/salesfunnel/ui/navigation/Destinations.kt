package id.jagakeluarga.salesfunnel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    PIPELINE("pipeline", "Pipeline", Icons.Filled.ViewKanban),
    PROSPEK("prospek", "Prospek", Icons.Filled.PersonSearch),
    AGENDA("agenda", "Agenda", Icons.Filled.CalendarMonth),
    NASABAH("nasabah", "Nasabah", Icons.Filled.People),
}
