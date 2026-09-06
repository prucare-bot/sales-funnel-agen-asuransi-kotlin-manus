package id.jagakeluarga.salesfunnel.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppTemplate
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppTemplateStore
import id.jagakeluarga.salesfunnel.whatsapp.WhatsAppTemplates

@Composable
fun WhatsAppTemplateDialog(
    nama: String,
    agenda: String? = null,
    waktu: String? = null,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    val context = LocalContext.current
    var templates by remember { mutableStateOf(WhatsAppTemplateStore.load(context)) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    fun persist(baru: List<WhatsAppTemplate>) {
        templates = baru
        WhatsAppTemplateStore.save(context, baru)
        if (selectedIndex >= baru.size) selectedIndex = (baru.size - 1).coerceAtLeast(0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih template WhatsApp") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                templates.forEachIndexed { index, template ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedIndex = index }
                            .padding(vertical = 2.dp),
                    ) {
                        RadioButton(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                        )
                        Column(modifier = Modifier.padding(start = 4.dp).weight(1f)) {
                            Text(template.title)
                            Text(
                                template.message,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(onClick = { editingIndex = index; showEditor = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit template")
                        }
                        IconButton(
                            enabled = templates.size > 1,
                            onClick = { persist(templates.toMutableList().apply { removeAt(index) }) },
                        ) { Icon(Icons.Filled.Delete, contentDescription = "Hapus template") }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingIndex = null; showEditor = true }
                        .padding(vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Tambah template baru", modifier = Modifier.padding(start = 8.dp))
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

    if (showEditor) {
        val awal = editingIndex?.let { templates.getOrNull(it) }
        TemplateEditorDialog(
            awal = awal,
            onDismiss = { showEditor = false },
            onSimpan = { hasil ->
                val baru = templates.toMutableList()
                if (editingIndex != null && editingIndex!! < baru.size) {
                    baru[editingIndex!!] = hasil
                } else {
                    baru.add(hasil)
                }
                persist(baru)
                showEditor = false
            },
        )
    }
}

@Composable
private fun TemplateEditorDialog(
    awal: WhatsAppTemplate?,
    onDismiss: () -> Unit,
    onSimpan: (WhatsAppTemplate) -> Unit,
) {
    var judul by remember { mutableStateOf(awal?.title ?: "") }
    var pesan by remember { mutableStateOf(awal?.message ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (awal == null) "Template baru" else "Edit template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Nama template") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = pesan,
                    onValueChange = { pesan = it },
                    label = { Text("Isi pesan") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Gunakan {nama}, {agenda}, atau {waktu} - akan otomatis diganti saat dikirim.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = judul.isNotBlank() && pesan.isNotBlank(),
                onClick = { onSimpan(WhatsAppTemplate(title = judul.trim(), message = pesan.trim())) },
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
