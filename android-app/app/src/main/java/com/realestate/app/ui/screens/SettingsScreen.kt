package com.realestate.app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.datastore.BackupHistoryEntry
import com.realestate.app.data.datastore.ThemePreference
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.SecondaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.BackupUiStatus
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.RestorePreviewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val backupHistory by backupViewModel.history.collectAsStateWithLifecycle()
    val backupStatus by backupViewModel.status.collectAsStateWithLifecycle()
    val restorePreview by backupViewModel.restorePreview.collectAsStateWithLifecycle()
    var searchClearedMessage by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { backupViewModel.backup(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { backupViewModel.peekRestore(it) } }

    var dbSizeBytes by remember { mutableStateOf<Long?>(null) }
    var cacheSizeBytes by remember { mutableStateOf<Long?>(null) }
    var cacheRefreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(cacheRefreshKey) {
        withContext(Dispatchers.IO) {
            dbSizeBytes = context.getDatabasePath("real_estate.db").let { if (it.exists()) it.length() else 0L }
            cacheSizeBytes = directorySize(context.cacheDir)
        }
    }

    LaunchedEffect(backupStatus) {
        if (backupStatus is BackupUiStatus.Success) cacheRefreshKey++
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
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
            Text("فضای ذخیره‌سازی", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                StorageRow("پایگاه داده (ملک‌ها، یادداشت‌ها، تراکنش‌ها)", dbSizeBytes)
                Spacer(modifier = Modifier.height(8.dp))
                StorageRow("فایل‌های موقت و کش تصاویر", cacheSizeBytes)
                Spacer(modifier = Modifier.height(14.dp))
                SecondaryButton(
                    text = "پاک‌سازی فایل‌های موقت",
                    onClick = {
                        File(context.cacheDir, "shared").listFiles()?.forEach { it.delete() }
                        cacheRefreshKey++
                        Toast.makeText(context, "فایل‌های موقت پاک شدند", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("جستجو", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                SecondaryButton(
                    text = "پاک کردن جستجوهای اخیر",
                    onClick = {
                        propertyViewModel.clearRecentSearches()
                        searchClearedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (searchClearedMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "جستجوهای اخیر پاک شد",
                        color = MaterialTheme.extendedColors.success,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("سایر تنظیمات", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "این موارد به‌زودی به برنامه اضافه می‌شوند",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ComingSoonRow(Icons.Rounded.Language, "زبان برنامه")
                    ComingSoonRow(Icons.Rounded.Straighten, "واحد اندازه‌گیری و ارز")
                    ComingSoonRow(Icons.Rounded.CalendarMonth, "تقویم شمسی/میلادی")
                    ComingSoonRow(Icons.Rounded.Notifications, "اعلان‌ها")
                    ComingSoonRow(Icons.Rounded.PrivacyTip, "حریم خصوصی")
                    ComingSoonRow(Icons.Rounded.Lock, "قفل برنامه (پین)")
                    ComingSoonRow(Icons.Rounded.Fingerprint, "ورود با اثر انگشت/چهره")
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }

    when (val preview = restorePreview) {
        is RestorePreviewState.Loading -> {
            // برای فایل‌های معمولی محلی این حالت کسری از ثانیه طول می‌کشد؛ دیالوگ تأیید بلافاصله جایگزین می‌شود.
        }
        is RestorePreviewState.Ready -> {
            ConfirmationDialog(
                title = "بازیابی از فایل پشتیبان؟",
                text = "این فایل شامل ${preview.preview.propertyCount} ملک، ${preview.preview.noteCount} یادداشت، " +
                    "${preview.preview.eventCount} رویداد و ${preview.preview.transactionCount} تراکنش کیف پول است.\n\n" +
                    "با ادامه، تمام اطلاعات فعلی این گوشی پاک و با محتوای همین فایل جایگزین می‌شود. این کار قابل بازگشت نیست.",
                confirmLabel = "بازیابی و جایگزینی",
                danger = true,
                onConfirm = { backupViewModel.confirmRestore(preview.uri) },
                onDismiss = { backupViewModel.dismissRestorePreview() }
            )
        }
        is RestorePreviewState.Failed -> {
            ConfirmationDialog(
                title = "خطا در خواندن فایل",
                text = preview.message,
                confirmLabel = "باشه",
                onConfirm = { backupViewModel.dismissRestorePreview() },
                onDismiss = { backupViewModel.dismissRestorePreview() }
            )
        }
        RestorePreviewState.Idle -> Unit
    }
}

@Composable
private fun ComingSoonRow(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AssistChip(onClick = {}, enabled = false, label = { Text("به‌زودی") })
    }
}

@Composable
private fun StorageRow(label: String, sizeBytes: Long?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (sizeBytes == null) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        } else {
            Text(
                formatStorageSize(sizeBytes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatStorageSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) {
        "${kb.toInt()} کیلوبایت"
    } else {
        val mb = kb / 1024.0
        "%.1f مگابایت".format(Locale.US, mb)
    }
}

private fun directorySize(file: File): Long {
    if (!file.exists()) return 0L
    return if (file.isDirectory) {
        file.listFiles()?.sumOf { directorySize(it) } ?: 0L
    } else {
        file.length()
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
                "${entry.propertyCount} ملک · ${entry.noteCount} یادداشت · ${entry.transactionCount} تراکنش · " +
                    "${entry.sizeBytes / 1024} کیلوبایت",
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
