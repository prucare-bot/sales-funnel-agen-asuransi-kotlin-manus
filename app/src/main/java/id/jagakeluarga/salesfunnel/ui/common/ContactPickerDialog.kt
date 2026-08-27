package id.jagakeluarga.salesfunnel.ui.common

import android.content.Context
import android.provider.ContactsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.jagakeluarga.salesfunnel.data.entity.Prospek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
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
    val allVisibleSelected = filtered.isNotEmpty() && filtered.all { it.id in selectedIds }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Text(
                "Impor kontak",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Pilih kontak yang ingin dimasukkan sebagai prospek.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari kontak") },
                placeholder = { Text("Cari nama atau nomor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (allVisibleSelected) {
                            selectedIds.removeAll(filtered.map { it.id }.toSet())
                        } else {
                            filtered.forEach { if (it.id !in selectedIds) selectedIds.add(it.id) }
                        }
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    if (allVisibleSelected) "Batalkan semua terlihat" else "Pilih semua terlihat",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "${selectedIds.size} dipilih",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = selectedIds.isNotEmpty(),
                onClick = { onSelected(contacts.filter { it.id in selectedIds }) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Tambah ${selectedIds.size} ke prospek")
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(4.dp))

            when {
                contacts.isEmpty() -> {
                    Text(
                        "Memuat kontak…",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                filtered.isEmpty() -> {
                    Text(
                        "Kontak tidak ditemukan.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        items(filtered, key = { it.id }) { contact ->
                            val checked = contact.id in selectedIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = checked,
                                        onClick = {
                                            if (checked) selectedIds.remove(contact.id) else selectedIds.add(contact.id)
                                        },
                                        role = Role.Checkbox,
                                    )
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.nama, fontWeight = FontWeight.Bold)
                                    Text(
                                        contact.nomorTelepon.orEmpty(),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
