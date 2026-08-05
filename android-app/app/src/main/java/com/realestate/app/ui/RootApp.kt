package com.realestate.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.datastore.ThemePreference
import com.realestate.app.ui.auth.AuthFlow
import com.realestate.app.ui.theme.RealEstateAppTheme
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel

@Composable
fun RootApp(
    authViewModel: AuthViewModel,
    propertyViewModel: PropertyViewModel,
    walletViewModel: WalletViewModel,
    profileViewModel: ProfileViewModel,
    backupViewModel: BackupViewModel,
    dealAssistantViewModel: DealAssistantViewModel
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (profile.themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    RealEstateAppTheme(darkTheme = useDarkTheme) {
        val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
        when (isLoggedIn) {
            null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            false -> AuthFlow(viewModel = authViewModel)

            true -> RealEstateApp(
                viewModel = propertyViewModel,
                walletViewModel = walletViewModel,
                profileViewModel = profileViewModel,
                authViewModel = authViewModel,
                backupViewModel = backupViewModel,
                dealAssistantViewModel = dealAssistantViewModel
            )
        }
    }
}
