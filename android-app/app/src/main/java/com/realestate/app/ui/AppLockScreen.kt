package com.realestate.app.ui

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.ui.components.SecondaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.AppLockViewModel

private const val PIN_LENGTH = 4

/**
 * Full-screen PIN/biometric gate shown by RootApp whenever a PIN is set and the app isn't
 * unlocked for this session. Success is reported by mutating [AppLockViewModel.isUnlocked]
 * directly (verifyPin/unlockWithBiometric) — RootApp observes that same state to decide when to
 * swap this screen out, so there's no separate "onUnlocked" callback to keep in sync.
 */
@Composable
fun AppLockScreen(viewModel: AppLockViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    fun triggerBiometric() {
        val hostActivity = activity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val prompt = BiometricPrompt(
            hostActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.unlockWithBiometric()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("باز کردن قفل برنامه")
            .setNegativeButtonText("استفاده از پین")
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(settings.biometricEnabled) {
        if (settings.biometricEnabled) triggerBiometric()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.screen),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("برنامه قفل است", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "برای ادامه، پین خود را وارد کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(PIN_LENGTH) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < pin.length) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
            if (error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "پین اشتباه است",
                    color = MaterialTheme.extendedColors.danger,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            PinKeypad(
                onDigit = { digit ->
                    if (pin.length < PIN_LENGTH) {
                        pin += digit
                        error = false
                        if (pin.length == PIN_LENGTH) {
                            val entered = pin
                            viewModel.verifyPin(entered) { ok ->
                                if (!ok) error = true
                            }
                            pin = ""
                        }
                    }
                },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )
            if (settings.biometricEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                SecondaryButton(
                    text = "ورود با اثر انگشت/چهره",
                    onClick = { triggerBiometric() },
                    icon = Icons.Rounded.Fingerprint
                )
            }
        }
    }
}

@Composable
internal fun PinKeypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Box(modifier = Modifier.size(64.dp))
                        key == "⌫" -> Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onBackspace),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Backspace, contentDescription = "حذف رقم")
                        }
                        else -> Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onDigit(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(key, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
