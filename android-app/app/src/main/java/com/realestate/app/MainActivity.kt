package com.realestate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.realestate.app.ui.RootApp
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val propertyViewModel: PropertyViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val dealAssistantViewModel: DealAssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RootApp(
                authViewModel = authViewModel,
                propertyViewModel = propertyViewModel,
                walletViewModel = walletViewModel,
                profileViewModel = profileViewModel,
                backupViewModel = backupViewModel,
                dealAssistantViewModel = dealAssistantViewModel
            )
        }
    }
}
