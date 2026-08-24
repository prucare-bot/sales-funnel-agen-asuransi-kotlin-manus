package id.jagakeluarga.salesfunnel.ui.common

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ContactPickerDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSelected: (List<Prospek>) -> Unit,
) {
    var contacts by remember { mutableStateOf<List<Prospek>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        contacts = withContext(Dispatchers.IO) {
            val result = mutableListOf<Prospek>()
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC",
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex).orEmpty().trim()
                    val number = cursor.getString(numberIndex).orEmpty().trim()
                    if (name.isNotBlank() && number.isNotBlank()) {
                        result += Prospek(
                            id = "contact-${cursor.getString(idIndex)}-${number.hashCode()}",
                            nama = name,
                            nomorTelepon = number,
                        )
                    }
                }
            }
            result.distinctBy { "${it.nama.lowercase()}|${it.nomorTelepon}" }
        }
    }

    val filtered = remember(contacts, query) {
        val normalized = query.trim().lowercase()
        if (normalized.isBlank()) contacts
        else contacts.filter {
            it.nama.lowercase().contains(normalized) || it.nomorTelepon.orEmpty().contains(normalized)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Kontak") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text("Cari nama atau No HP/WA") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Pilih satu atau beberapa kontak sekaligus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (filtered.isEmpty()) {
                    Text("Kontak tidak ditemukan.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                        items(filtered, key = { it.id }) { contact ->
                            val checked = contact.id in selectedIds
                            ListItem(
                                leadingContent = { Icon(Icons.Filled.Contacts, contentDescription = null) },
                                headlineContent = { Text(contact.nama) },
                                supportingContent = { Text(contact.nomorTelepon.orEmpty()) },
                                trailingContent = {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            if (it) selectedIds.add(contact.id) else selectedIds.remove(contact.id)
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedIds.isNotEmpty(),
                onClick = { onSelected(contacts.filter { it.id in selectedIds }) },
            ) { Text("Import ${selectedIds.size} kontak") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
