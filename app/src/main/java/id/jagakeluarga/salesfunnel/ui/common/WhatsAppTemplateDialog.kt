package id.jagakeluarga.salesfunnel.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppTemplates

@Composable
fun WhatsAppTemplateDialog(
    nama: String,
    agenda: String? = null,
    waktu: String? = null,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val templates = WhatsAppTemplates.defaults

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih template WhatsApp") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                templates.forEachIndexed { index, template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index }
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(template.title)
                            Text(
                                template.message,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSend(WhatsAppTemplates.render(templates[selectedIndex], nama, agenda, waktu))
            }) { Text("Buka WhatsApp") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
