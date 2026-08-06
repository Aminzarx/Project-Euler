package com.realestate.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.realestate.app.ui.RootApp
import com.realestate.app.viewmodel.AppLockViewModel
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.CaseTransferViewModel
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.UpdateCheckViewModel
import com.realestate.app.viewmodel.WalletViewModel

/** Extends FragmentActivity (not just ComponentActivity) because BiometricPrompt requires a
 *  FragmentActivity or Fragment host to attach its internal dialog fragment to. */
class MainActivity : FragmentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val propertyViewModel: PropertyViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val backupViewModel: BackupViewModel by viewModels()
    private val caseTransferViewModel: CaseTransferViewModel by viewModels()
    private val dealAssistantViewModel: DealAssistantViewModel by viewModels()
    private val appLockViewModel: AppLockViewModel by viewModels()
    private val updateCheckViewModel: UpdateCheckViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createFollowUpNotificationChannel()
        setContent {
            RootApp(
                authViewModel = authViewModel,
                propertyViewModel = propertyViewModel,
                walletViewModel = walletViewModel,
                profileViewModel = profileViewModel,
                backupViewModel = backupViewModel,
                caseTransferViewModel = caseTransferViewModel,
                dealAssistantViewModel = dealAssistantViewModel,
                appLockViewModel = appLockViewModel,
                updateCheckViewModel = updateCheckViewModel
            )
        }
    }

    private fun createFollowUpNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOLLOW_UP_NOTIFICATION_CHANNEL_ID,
                "پیگیری ملک‌ها",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "یادآوری ملک‌هایی که پیگیری امروز دارند" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val FOLLOW_UP_NOTIFICATION_CHANNEL_ID = "followups"
    }
}
