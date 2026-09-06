package id.jagakeluarga.salesfunnel.ui.common

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    selectedMillis: Long,
    onSelectedMillisChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Waktu agenda",
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val calendar = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedMillis,
        initialDisplayMode = DisplayMode.Input,
    )

    OutlinedTextField(
        value = dateFormatter.format(Date(selectedMillis)),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().clickable { showDatePicker = true },
        trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Pilih") } },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Calendar.getInstance().apply { timeInMillis = millis }
                        calendar.set(
                            Calendar.YEAR,
                            selectedDate.get(Calendar.YEAR),
                        )
                        calendar.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
                        onSelectedMillisChange(calendar.timeInMillis)
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Lanjut") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            },
        ) {
            DatePicker(state = datePickerState, title = { Text("Pilih tanggal") })
        }
    }

    if (showTimePicker) {
        DisposableEffect(Unit) {
            val dialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    val updated = Calendar.getInstance().apply {
                        timeInMillis = selectedMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onSelectedMillisChange(updated.timeInMillis)
                    showTimePicker = false
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            )
            dialog.setOnCancelListener { showTimePicker = false }
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }
}
