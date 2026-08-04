package com.realestate.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.viewmodel.AuthUiState

@Composable
fun OtpVerificationScreen(
    state: AuthUiState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    onEditNumber: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(heroGradient()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.Sms, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("کد تایید ارسال شد", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(
                state.mobileNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.screen)) {
            Text("کد ۵ رقمی را وارد کنید", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.otpInput,
                onValueChange = onOtpChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("• • • • •") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                isError = state.error != null
            )
            if (state.error != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            state.generatedOtp?.let { otp ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "کد نمایشی (نسخه آزمایشی، بدون پیامک واقعی): $otp",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "تایید و ورود",
                onClick = onVerify,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.otpInput.length == 5 && !state.isLoading,
                loading = state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEditNumber) { Text("ویرایش شماره") }
                TextButton(onClick = onResend, enabled = state.resendSecondsLeft == 0) {
                    Text(if (state.resendSecondsLeft > 0) "ارسال مجدد (${state.resendSecondsLeft})" else "ارسال مجدد کد")
                }
            }
        }
    }
}
