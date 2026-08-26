package id.jagakeluarga.salesfunnel.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val locked = AppLockManager.isEnabled(context)
    var unlocked by remember(locked) { mutableStateOf(!locked) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val lockoutSeconds = AppLockManager.remainingLockoutSeconds(context)
    val activity = context as? FragmentActivity
    val biometricAvailable = BiometricManager.from(context).canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK,
    ) == BiometricManager.BIOMETRIC_SUCCESS

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, locked) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && AppLockManager.shouldAutoLock(context)) {
                unlocked = false
                error = null
                pin = ""
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(unlocked) {
        if (unlocked) AppLockManager.clearBackgroundMarker(context)
    }
    if (!unlocked) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Aplikasi terkunci", style = MaterialTheme.typography.headlineSmall)
            Text("Masukkan PIN untuk membuka data nasabah.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(8); error = null },
                label = { Text("PIN") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
            val activeLockout = AppLockManager.remainingLockoutSeconds(context)
            if (activeLockout > 0) {
                Text("Terlalu banyak percobaan. Coba lagi dalam ${activeLockout} detik.", color = MaterialTheme.colorScheme.error)
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                enabled = pin.length >= 4 && activeLockout == 0L,
                onClick = {
                    if (AppLockManager.verifyPin(context, pin)) {
                        unlocked = true
                        pin = ""
                    } else {
                        val seconds = AppLockManager.remainingLockoutSeconds(context)
                        error = if (seconds > 0) "PIN dikunci sementara." else "PIN salah. Sisa percobaan sebelum lockout: ${5 - context.getSharedPreferences("sales_funnel_security", 0).getInt("failed_attempts", 0)}."
                    }
                },
            ) { Text("Buka aplikasi") }
            if (biometricAvailable && activity != null) {
                OutlinedButton(
                    onClick = {
                        val prompt = BiometricPrompt(
                            activity,
                            ContextCompat.getMainExecutor(context),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    unlocked = true
                                }

                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    error = errString.toString()
                                }
                            },
                        )
                        val info = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Buka Sales Funnel")
                            .setSubtitle("Gunakan biometrik atau kredensial perangkat")
                            .setNegativeButtonText("Gunakan PIN")
                            .build()
                        prompt.authenticate(info)
                    },
                ) { Text("Buka dengan biometrik") }
            }
        }
    } else {
        content()
    }
}
