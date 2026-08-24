package id.jagakeluarga.salesfunnel.ui.screens.prospek

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.TahapPipeline
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProspekFilterDialog(
    tanggalMulai: Long?,
    tanggalAkhir: Long?,
    tahapAwal: TahapPipeline?,
    onDismiss: () -> Unit,
    onApply: (Long?, Long?, TahapPipeline?) -> Unit,
) {
    var mulai by remember { mutableStateOf(tanggalMulai) }
    var akhir by remember { mutableStateOf(tanggalAkhir) }
    var tahap by remember { mutableStateOf(tahapAwal) }
    var showMulai by remember { mutableStateOf(false) }
    var showAkhir by remember { mutableStateOf(false) }
    var expandedTahap by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    fun dayStart(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    if (showMulai) {
        val state = rememberDatePickerState(initialSelectedDateMillis = mulai ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showMulai = false },
            confirmButton = {
                TextButton(onClick = { state.selectedDateMillis?.let { mulai = dayStart(it) }; showMulai = false }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showMulai = false }) { Text("Batal") } },
        ) { DatePicker(state = state) }
    }
    if (showAkhir) {
        val state = rememberDatePickerState(initialSelectedDateMillis = akhir ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showAkhir = false },
            confirmButton = {
                TextButton(onClick = { state.selectedDateMillis?.let { akhir = dayStart(it) + 24 * 60 * 60 * 1000 - 1 }; showAkhir = false }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showAkhir = false }) { Text("Batal") } },
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Prospek") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showMulai = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (mulai == null) "Tanggal mulai" else "Mulai: ${formatter.format(Date(mulai!!))}")
                }
                OutlinedButton(onClick = { showAkhir = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (akhir == null) "Tanggal akhir" else "Akhir: ${formatter.format(Date(akhir!!))}")
                }
                ExposedDropdownMenuBox(expanded = expandedTahap, onExpandedChange = { expandedTahap = it }) {
                    OutlinedTextField(
                        value = tahap?.label ?: "Semua status funnel",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Status funnel") }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = expandedTahap, onDismissRequest = { expandedTahap = false }) {
                        DropdownMenuItem(text = { Text("Semua status funnel") }, onClick = { tahap = null; expandedTahap = false })
                        TahapPipeline.entries.forEach { item ->
                            DropdownMenuItem(text = { Text(item.label) }, onClick = { tahap = item; expandedTahap = false })
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(mulai, akhir, tahap) }) { Text("Terapkan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
