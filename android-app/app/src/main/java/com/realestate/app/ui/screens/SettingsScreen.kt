package com.realestate.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.datastore.BackupHistoryEntry
import com.realestate.app.data.datastore.ThemePreference
import com.realestate.app.data.formatAppDate
import com.realestate.app.ui.PinKeypad
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.AppTextButton
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.components.GlassAlertDialog
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.SecondaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.AppLockViewModel
import com.realestate.app.viewmodel.BackupUiStatus
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.RestorePreviewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profileViewModel: ProfileViewModel,
    propertyViewModel: PropertyViewModel,
    backupViewModel: BackupViewModel,
    appLockViewModel: AppLockViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val backupHistory by backupViewModel.history.collectAsStateWithLifecycle()
    val backupStatus by backupViewModel.status.collectAsStateWithLifecycle()
    val restorePreview by backupViewModel.restorePreview.collectAsStateWithLifecycle()
    val securitySettings by appLockViewModel.settings.collectAsStateWithLifecycle()
    var searchClearedMessage by remember { mutableStateOf(false) }
    var showPinSetup by remember { mutableStateOf(false) }
    var showPinRemoveConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { backupViewModel.backup(it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { backupViewModel.peekRestore(it) } }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            appLockViewModel.setNotificationsEnabled(true)
        } else {
            coroutineScope.launch { snackbarHostState.showSnackbar("بدون مجوز اعلان، این قابلیت کار نمی‌کند") }
        }
    }

    val biometricAvailable = remember {
        BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            Text("تاریخ و تقویم", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                CalendarSelector(
                    jalali = securitySettings.calendarJalali,
                    onSelect = { appLockViewModel.setCalendarJalali(it) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("امنیت و حریم خصوصی", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                ToggleRow(
                    icon = Icons.Rounded.Lock,
                    title = "قفل برنامه (پین)",
                    subtitle = if (securitySettings.hasPin) "فعال" else "غیرفعال",
                    checked = securitySettings.hasPin,
                    onCheckedChange = { enable ->
                        if (enable) showPinSetup = true else showPinRemoveConfirm = true
                    }
                )
                if (securitySettings.hasPin) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "مدت قفل خودکار",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "بعد از بستن برنامه، تا این مدت نیازی به وارد کردن دوباره پین نیست.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AutoLockSelector(
                        minutes = securitySettings.autoLockMinutes,
                        onSelect = { appLockViewModel.setAutoLockMinutes(it) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                ToggleRow(
                    icon = Icons.Rounded.Fingerprint,
                    title = "ورود با اثر انگشت/چهره",
                    subtitle = when {
                        !securitySettings.hasPin -> "ابتدا قفل برنامه را فعال کنید"
                        !biometricAvailable -> "روی این گوشی در دسترس نیست"
                        else -> "به‌جای پین استفاده می‌شود"
                    },
                    checked = securitySettings.biometricEnabled,
                    enabled = securitySettings.hasPin && biometricAvailable,
                    onCheckedChange = { appLockViewModel.setBiometricEnabled(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                ToggleRow(
                    icon = Icons.Rounded.Shield,
                    title = "امنیت صفحه",
                    subtitle = "جلوگیری از اسکرین‌شات و نمایش محتوا در لیست برنامه‌های اخیر",
                    checked = securitySettings.screenSecurityEnabled,
                    onCheckedChange = { appLockViewModel.setScreenSecurityEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("اعلان‌ها", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                ToggleRow(
                    icon = Icons.Rounded.Notifications,
                    title = "یادآوری پیگیری‌های امروز",
                    subtitle = "حداکثر یک اعلان در روز، فقط وقتی ملکی پیگیری امروز دارد",
                    checked = securitySettings.notificationsEnabled,
                    onCheckedChange = { enable ->
                        if (!enable) {
                            appLockViewModel.setNotificationsEnabled(false)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            appLockViewModel.setNotificationsEnabled(true)
                        }
                    }
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
                        backupHistory.forEach { entry -> BackupHistoryRow(entry, securitySettings.calendarJalali) }
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
                        coroutineScope.launch { snackbarHostState.showSnackbar("فایل‌های موقت پاک شدند") }
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

    if (showPinSetup) {
        PinSetupDialog(
            onDismiss = { showPinSetup = false },
            onConfirmed = { pin ->
                appLockViewModel.setPin(pin)
                showPinSetup = false
                coroutineScope.launch { snackbarHostState.showSnackbar("قفل برنامه فعال شد") }
            }
        )
    }

    if (showPinRemoveConfirm) {
        ConfirmationDialog(
            title = "غیرفعال کردن قفل برنامه؟",
            text = "پین و ورود با اثر انگشت/چهره غیرفعال خواهد شد.",
            confirmLabel = "غیرفعال کن",
            danger = true,
            onConfirm = { appLockViewModel.clearPin() },
            onDismiss = { showPinRemoveConfirm = false }
        )
    }
}

@Composable
private fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.extendedColors.onDisabled
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun PinSetupDialog(onDismiss: () -> Unit, onConfirmed: (String) -> Unit) {
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var confirming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (!confirming) "یک پین ۴ رقمی وارد کنید" else "پین را دوباره وارد کنید") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { index ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (index < currentPin.length) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }
                if (error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("پین‌ها یکسان نبودند، دوباره تلاش کنید", color = MaterialTheme.extendedColors.danger, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(20.dp))
                PinKeypad(
                    onDigit = { digit ->
                        if (currentPin.length < 4) {
                            currentPin += digit
                            if (currentPin.length == 4) {
                                if (!confirming) {
                                    firstPin = currentPin
                                    currentPin = ""
                                    confirming = true
                                    error = false
                                } else if (currentPin == firstPin) {
                                    onConfirmed(currentPin)
                                } else {
                                    error = true
                                    currentPin = ""
                                    firstPin = ""
                                    confirming = false
                                }
                            }
                        }
                    },
                    onBackspace = { if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            AppTextButton(text = "انصراف", onClick = onDismiss)
        }
    )
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
private fun BackupHistoryRow(entry: BackupHistoryEntry, jalali: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                formatAppDate(entry.timestamp, jalali),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarSelector(jalali: Boolean, onSelect: (Boolean) -> Unit) {
    val options = listOf(true to "شمسی", false to "میلادی")
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = jalali == value,
                onClick = { onSelect(value) },
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

private val autoLockOptions = listOf(0 to "بلافاصله", 1 to "۱ دقیقه", 5 to "۵ دقیقه", 15 to "۱۵ دقیقه", 30 to "۳۰ دقیقه")

@Composable
private fun AutoLockSelector(minutes: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        autoLockOptions.forEach { (value, label) ->
            FilterChip(selected = minutes == value, onClick = { onSelect(value) }, label = { Text(label) })
        }
    }
}
