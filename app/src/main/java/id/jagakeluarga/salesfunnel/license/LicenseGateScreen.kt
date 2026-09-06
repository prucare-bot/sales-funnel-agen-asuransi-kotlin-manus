package id.jagakeluarga.salesfunnel.license

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LicenseGateScreen(onActivated: () -> Unit) {
    val context = LocalContext.current
    val deviceId = remember { LicenseManager.getDeviceId(context) }
    var inputKey by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 12.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text("Masa trial sudah berakhir", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Masukkan kunci aktivasi untuk terus memakai aplikasi ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("ID perangkat Anda", style = MaterialTheme.typography.labelMedium)
                Text(
                    "Kirim ID ini ke penjual untuk mendapatkan kunci aktivasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        deviceId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    IconButton(onClick = {
                        clipboard?.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Salin ID perangkat")
                    }
                }
            }
        }

        OutlinedTextField(
            value = inputKey,
            onValueChange = { inputKey = it; error = null },
            label = { Text("Kunci aktivasi") },
            placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 4.dp))
        }
        Button(
            onClick = {
                if (LicenseManager.activate(context, inputKey)) {
                    onActivated()
                } else {
                    error = "Kunci tidak valid untuk perangkat ini."
                }
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text("Aktifkan") }
    }
}
