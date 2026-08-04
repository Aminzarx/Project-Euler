package com.realestate.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.datastore.BackupHistoryEntry
import com.realestate.app.data.datastore.ThemePreference
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.SecondaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.BackupUiStatus
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    propertyViewModel: PropertyViewModel,
    backupViewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val backupHistory by backupViewModel.history.collectAsStateWithLifecycle()
    val backupStatus by backupViewModel.status.collectAsStateWithLifecycle()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { backupViewModel.backup(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { backupViewModel.restore(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(Spacing.screen)
        ) {
            Text("ظاهر برنامه", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                ThemeSelector(
                    selected = profile.themePreference,
                    onSelect = { profileViewModel.setThemePreference(it) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("پشتیبان‌گیری و بازیابی", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "کل اطلاعات ملک‌ها، یادداشت‌ها و تراکنش‌های کیف پول به‌صورت آفلاین ذخیره می‌شود",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton(
                        text = "پشتیبان‌گیری",
                        onClick = { backupLauncher.launch("real-estate-backup-${System.currentTimeMillis()}.json") },
                        modifier = Modifier.weight(1f),
                        enabled = backupStatus != BackupUiStatus.Working
                    )
                    SecondaryButton(
                        text = "بازیابی",
                        onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f),
                        enabled = backupStatus != BackupUiStatus.Working
                    )
                }

                when (val status = backupStatus) {
                    is BackupUiStatus.Working -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال پردازش...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is BackupUiStatus.Success -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(status.message, color = MaterialTheme.extendedColors.success, style = MaterialTheme.typography.bodyMedium)
                    }
                    is BackupUiStatus.Failure -> {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(status.message, color = MaterialTheme.extendedColors.danger, style = MaterialTheme.typography.bodyMedium)
                    }
                    BackupUiStatus.Idle -> Unit
                }

                if (backupHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("تاریخچه پشتیبان‌گیری", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        backupHistory.forEach { entry -> BackupHistoryRow(entry) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("جستجو", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(
                    text = "پاک کردن جستجوهای اخیر",
                    onClick = { propertyViewModel.clearRecentSearches() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("درباره برنامه", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "مدیریت املاک — نسخه ۱٫۰",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun BackupHistoryRow(entry: BackupHistoryEntry) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(entry.timestamp)),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "${entry.propertyCount} ملک · ${entry.sizeBytes / 1024} کیلوبایت",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(selected: ThemePreference, onSelect: (ThemePreference) -> Unit) {
    val options = listOf(
        ThemePreference.SYSTEM to "سیستم",
        ThemePreference.LIGHT to "روشن",
        ThemePreference.DARK to "تاریک"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (pref, label) ->
            SegmentedButton(
                selected = selected == pref,
                onClick = { onSelect(pref) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(label)
            }
        }
    }
}
