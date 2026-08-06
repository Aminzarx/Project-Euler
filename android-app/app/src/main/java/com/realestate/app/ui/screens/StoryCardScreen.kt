package com.realestate.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.CaseType
import com.realestate.app.data.Property
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.StoryAspectRatio
import com.realestate.app.ui.components.StoryBadge
import com.realestate.app.ui.components.StoryCardConfig
import com.realestate.app.ui.components.StoryCardContent
import com.realestate.app.ui.components.StoryTemplate
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/** Where a rendered story card image should be handed off to. */
private enum class StoryShareTarget(val label: String, val packageName: String?) {
    INSTAGRAM("استوری اینستاگرام", "com.instagram.android"),
    TELEGRAM("استوری تلگرام", "org.telegram.messenger"),
    WHATSAPP("استاتوس واتساپ", "com.whatsapp"),
    GENERIC("سایر برنامه‌ها", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryCardScreen(
    propertyId: Long,
    viewModel: PropertyViewModel,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val property by viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val agentPhone = profile.mobileNumber
    val agencyName = profile.agencyName
    var hasLoadedOnce by remember(propertyId) { mutableStateOf(false) }

    // See the identical pattern in PropertyDetailScreen: distinguishes "still loading from Room"
    // from "this property genuinely doesn't exist," so "ملک یافت نشد" never flashes during the
    // first load.
    LaunchedEffect(propertyId) {
        viewModel.getPropertyById(propertyId).collect { hasLoadedOnce = true }
    }

    var shareTarget by remember { mutableStateOf<StoryShareTarget?>(null) }
    var selectedTemplate by remember { mutableStateOf(StoryTemplate.GRADIENT) }
    var showFullPreview by remember { mutableStateOf(false) }
    var showCustomizeSheet by remember { mutableStateOf(false) }

    var aspectRatio by remember { mutableStateOf(StoryAspectRatio.STORY) }
    var showPrice by remember { mutableStateOf(true) }
    var showArea by remember { mutableStateOf(true) }
    var showRooms by remember { mutableStateOf(true) }
    var showContact by remember { mutableStateOf(true) }
    var showCta by remember { mutableStateOf(true) }
    var showQrCode by remember { mutableStateOf(false) }
    var selectedBadge by remember { mutableStateOf<StoryBadge?>(null) }

    val config = StoryCardConfig(
        aspectRatio = aspectRatio,
        showPrice = showPrice,
        showArea = showArea,
        showRooms = showRooms,
        showContact = showContact,
        showCta = showCta,
        showQrCode = showQrCode,
        badge = selectedBadge,
        agencyLogoUri = profile.agencyLogoUri
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کارت استوری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (property?.caseType == CaseType.OWNER) {
                        IconButton(onClick = { showCustomizeSheet = true }) {
                            Icon(Icons.Rounded.FilterList, contentDescription = "شخصی‌سازی کارت")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = property
        if (current == null || current.caseType == CaseType.CLIENT_REQUEST) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    current != null -> Text(
                        "کارت استوری فقط برای پرونده‌های مالک در دسترس است.",
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    hasLoadedOnce -> Text("ملک یافت نشد")
                    else -> CircularProgressIndicator()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.screen),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StoryTemplate.entries.forEach { template ->
                        FilterChip(
                            selected = template == selectedTemplate,
                            onClick = { selectedTemplate = template },
                            label = { Text(template.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                StoryCardContent(
                    property = current,
                    agentPhone = agentPhone,
                    agencyName = agencyName,
                    template = selectedTemplate,
                    config = config,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFullPreview = true }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "برای پیش‌نمایش تمام‌صفحه ضربه بزنید",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StoryShareChip(
                        label = "اینستاگرام",
                        color = Color(0xFFC13584),
                        modifier = Modifier.weight(1f),
                        onClick = { shareTarget = StoryShareTarget.INSTAGRAM }
                    )
                    StoryShareChip(
                        label = "تلگرام",
                        color = Color(0xFF2AABEE),
                        modifier = Modifier.weight(1f),
                        onClick = { shareTarget = StoryShareTarget.TELEGRAM }
                    )
                    StoryShareChip(
                        label = "واتساپ",
                        color = Color(0xFF25D366),
                        modifier = Modifier.weight(1f),
                        onClick = { shareTarget = StoryShareTarget.WHATSAPP }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                PrimaryButton(
                    text = "اشتراک‌گذاری در سایر برنامه‌ها",
                    onClick = { shareTarget = StoryShareTarget.GENERIC },
                    icon = Icons.Rounded.Share,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val target = shareTarget
            if (target != null) {
                StoryCardCaptureDialog(
                    property = current,
                    agentPhone = agentPhone,
                    agencyName = agencyName,
                    template = selectedTemplate,
                    config = config,
                    target = target,
                    onDismiss = { shareTarget = null }
                )
            }

            if (showFullPreview) {
                FullPreviewDialog(
                    property = current,
                    agentPhone = agentPhone,
                    agencyName = agencyName,
                    template = selectedTemplate,
                    config = config,
                    onDismiss = { showFullPreview = false }
                )
            }

            if (showCustomizeSheet) {
                CustomizeCardSheet(
                    aspectRatio = aspectRatio,
                    onAspectRatioChange = { aspectRatio = it },
                    showPrice = showPrice,
                    onShowPriceChange = { showPrice = it },
                    showArea = showArea,
                    onShowAreaChange = { showArea = it },
                    showRooms = showRooms,
                    onShowRoomsChange = { showRooms = it },
                    showContact = showContact,
                    onShowContactChange = { showContact = it },
                    showCta = showCta,
                    onShowCtaChange = { showCta = it },
                    showQrCode = showQrCode,
                    onShowQrCodeChange = { showQrCode = it },
                    selectedBadge = selectedBadge,
                    onBadgeChange = { selectedBadge = it },
                    onDismiss = { showCustomizeSheet = false }
                )
            }
        }
    }
}

@Composable
private fun StoryShareChip(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        modifier = modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

/** A larger, full-width rendering of the same card for a closer look before sharing. */
@Composable
private fun FullPreviewDialog(
    property: Property,
    agentPhone: String,
    agencyName: String,
    template: StoryTemplate,
    config: StoryCardConfig,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            StoryCardContent(
                property = property,
                agentPhone = agentPhone,
                agencyName = agencyName,
                template = template,
                config = config,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeCardSheet(
    aspectRatio: StoryAspectRatio,
    onAspectRatioChange: (StoryAspectRatio) -> Unit,
    showPrice: Boolean,
    onShowPriceChange: (Boolean) -> Unit,
    showArea: Boolean,
    onShowAreaChange: (Boolean) -> Unit,
    showRooms: Boolean,
    onShowRoomsChange: (Boolean) -> Unit,
    showContact: Boolean,
    onShowContactChange: (Boolean) -> Unit,
    showCta: Boolean,
    onShowCtaChange: (Boolean) -> Unit,
    showQrCode: Boolean,
    onShowQrCodeChange: (Boolean) -> Unit,
    selectedBadge: StoryBadge?,
    onBadgeChange: (StoryBadge?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.md)) {
            Text("قالب خروجی", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StoryAspectRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = ratio == aspectRatio,
                        onClick = { onAspectRatioChange(ratio) },
                        label = { Text(ratio.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.md))

            Text("برچسب تبلیغاتی", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "اختیاری — روی گوشه کارت نمایش داده می‌شود",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedBadge == null,
                    onClick = { onBadgeChange(null) },
                    label = { Text("بدون برچسب") }
                )
                StoryBadge.entries.forEach { badge ->
                    FilterChip(
                        selected = selectedBadge == badge,
                        onClick = { onBadgeChange(badge) },
                        label = { Text(badge.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(Spacing.md))

            Text("اطلاعات نمایش‌داده‌شده", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            ToggleRow("قیمت", showPrice, onShowPriceChange)
            ToggleRow("متراژ", showArea, onShowAreaChange)
            ToggleRow("تعداد اتاق", showRooms, onShowRoomsChange)
            ToggleRow("شماره تماس مشاور", showContact, onShowContactChange)
            ToggleRow("دعوت به تماس", showCta, onShowCtaChange)
            ToggleRow("کد QR تماس مستقیم", showQrCode, onShowQrCodeChange)
            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StoryCardCaptureDialog(
    property: Property,
    agentPhone: String,
    agencyName: String,
    template: StoryTemplate,
    config: StoryCardConfig,
    target: StoryShareTarget,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var captureReady by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        StoryCardContent(
            property = property,
            agentPhone = agentPhone,
            agencyName = agencyName,
            template = template,
            config = config,
            modifier = Modifier
                .width(360.dp)
                .onGloballyPositioned { coordinates ->
                    if (coordinates.size.width > 0 && coordinates.size.height > 0) {
                        captureReady = true
                    }
                }
        )

        LaunchedEffect(captureReady) {
            if (!captureReady) return@LaunchedEffect
            // One more frame beyond layout to make sure the card has actually been drawn —
            // replaces a fixed delay(300) that could either waste time or, on a slow device,
            // still fire before the real draw finished.
            withFrameNanos {}
            val result = runCatching {
                check(view.width > 0 && view.height > 0) { "Story card view has no measured size" }
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                val file = withContext(Dispatchers.IO) { saveBitmapToCache(context, bitmap) }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                shareStoryImage(context, uri, target)
            }
            showSpinner = false
            if (result.isFailure) {
                Toast.makeText(context, "امکان ساخت تصویر وجود نداشت. لطفاً دوباره تلاش کنید.", Toast.LENGTH_SHORT).show()
            } else if (target != StoryShareTarget.INSTAGRAM) {
                // Instagram/Telegram/WhatsApp switch apps immediately, which is itself the
                // confirmation; ActivityNotFoundException already toasts its own message.
                // For the generic Android chooser there's no equally obvious signal, so confirm here.
                Toast.makeText(context, "کارت ملک آماده اشتراک‌گذاری شد", Toast.LENGTH_SHORT).show()
            }
            onDismiss()
        }
    }

    // A second, independent dialog window so the spinner overlay is never part of the
    // pixels captured from the first dialog's view.
    if (showSpinner) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(shape = MaterialTheme.shapes.large, color = Color.Black.copy(alpha = 0.55f)) {
                Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

/** Hands the rendered story image directly to the target app when installed, falling back to the system chooser otherwise. */
private fun shareStoryImage(context: Context, uri: android.net.Uri, target: StoryShareTarget) {
    if (target == StoryShareTarget.GENERIC || target.packageName == null) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری کارت ملک"))
        return
    }

    try {
        val intent = if (target == StoryShareTarget.INSTAGRAM) {
            Intent("com.instagram.share.ADD_TO_STORY").apply {
                setDataAndType(uri, "image/png")
                putExtra("interactive_asset_uri", uri)
                setPackage(target.packageName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage(target.packageName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "${target.label} روی این گوشی نصب نیست", Toast.LENGTH_SHORT).show()
    }
}

/** Clears any previously shared card before writing the new one — story cards are single-use, so nothing is worth keeping around. */
private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    dir.listFiles()?.forEach { it.delete() }
    val file = File(dir, "story_card_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}
