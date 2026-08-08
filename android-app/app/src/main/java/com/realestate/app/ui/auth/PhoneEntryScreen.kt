package com.realestate.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.viewmodel.AuthUiState
import com.realestate.app.viewmodel.ReferralFieldStatus

@Composable
fun PhoneEntryScreen(
    state: AuthUiState,
    onMobileChange: (String) -> Unit,
    onReferralCodeChange: (String) -> Unit,
    onSubmit: () -> Unit
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
            Icon(Icons.Rounded.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("مدیریت املاک", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text(
                "ورود سریع و امن",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.screen)) {
            Text("شماره موبایل خود را وارد کنید", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "کد تایید برای این شماره پیامک می‌شود",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.mobileNumber,
                onValueChange = onMobileChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("09xxxxxxxxx") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = state.error != null
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("کد معرف", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))

            val referralIsError = state.referralStatus == ReferralFieldStatus.INVALID_FORMAT ||
                state.referralStatus == ReferralFieldStatus.NOT_FOUND ||
                state.referralStatus == ReferralFieldStatus.NETWORK_ERROR
            OutlinedTextField(
                value = state.referralCode,
                onValueChange = onReferralCodeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ABCD1234") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Characters
                ),
                singleLine = true,
                isError = referralIsError,
                trailingIcon = {
                    when (state.referralStatus) {
                        ReferralFieldStatus.CHECKING -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        ReferralFieldStatus.VALID -> Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        ReferralFieldStatus.INVALID_FORMAT, ReferralFieldStatus.NOT_FOUND, ReferralFieldStatus.NETWORK_ERROR -> Icon(
                            Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        ReferralFieldStatus.EMPTY -> {}
                    }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            val (referralHelperText, referralHelperColor) = when (state.referralStatus) {
                ReferralFieldStatus.EMPTY -> "برای استفاده از برنامه، کد معرف معتبر وارد کنید." to MaterialTheme.colorScheme.onSurfaceVariant
                ReferralFieldStatus.INVALID_FORMAT -> "کد معرف معتبر نیست." to MaterialTheme.colorScheme.error
                ReferralFieldStatus.CHECKING -> "در حال بررسی کد معرف..." to MaterialTheme.colorScheme.onSurfaceVariant
                ReferralFieldStatus.VALID -> "کد معرف معتبر است." to MaterialTheme.colorScheme.primary
                ReferralFieldStatus.NOT_FOUND -> "کد معرف معتبر نیست." to MaterialTheme.colorScheme.error
                ReferralFieldStatus.NETWORK_ERROR -> "خطا در بررسی کد معرف. اتصال اینترنت را بررسی کنید." to MaterialTheme.colorScheme.error
            }
            Text(referralHelperText, color = referralHelperColor, style = MaterialTheme.typography.bodySmall)

            if (state.error != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = "ورود",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                loading = state.isLoading,
                enabled = state.mobileNumber.length == 11 &&
                    state.referralStatus != ReferralFieldStatus.EMPTY &&
                    state.referralStatus != ReferralFieldStatus.INVALID_FORMAT &&
                    !state.isLoading
            )
        }
    }
}
