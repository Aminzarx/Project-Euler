package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuDivider
import com.realestate.app.ui.components.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.code
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.data.dealassistant.calculateCommission
import com.realestate.app.data.dealassistant.calculatePricePerMeter
import com.realestate.app.data.dealassistant.defaultCommissionRate
import com.realestate.app.data.dealassistant.estimateMarketValue
import com.realestate.app.data.property.TimelineEventType
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CircleIconButton
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.components.StatusPillBadge
import com.realestate.app.ui.components.buildShareMessage
import com.realestate.app.ui.components.color
import com.realestate.app.ui.components.formatPrice
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.components.label
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.ui.theme.imageScrimGradient
import com.realestate.app.viewmodel.PropertyViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val STALE_PROPERTY_DAYS = 14L

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailScreen(
    propertyId: Long,
    viewModel: PropertyViewModel,
    profileViewModel: com.realestate.app.viewmodel.ProfileViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onStoryCard: (Long) -> Unit,
    onOpenDealTool: (DealToolId) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val agentPhone = profileViewModel.profile.collectAsStateWithLifecycle().value.mobileNumber
    val property by viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val notes by viewModel.getNotesForProperty(propertyId).collectAsStateWithLifecycle(initialValue = emptyList())
    val timeline by viewModel.getTimelineForProperty(propertyId).collectAsStateWithLifecycle(initialValue = emptyList())
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val favoriteFolders by viewModel.favoriteFolders.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showStatusSheet by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showFollowUpDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<com.realestate.app.data.property.Note?>(null) }
    var hasMarkedViewed by remember(propertyId) { mutableStateOf(false) }
    var hasLoadedOnce by remember(propertyId) { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(property) {
        val p = property
        if (p != null && !hasMarkedViewed) {
            hasMarkedViewed = true
            viewModel.markViewed(p)
        }
    }

    // Room's Flow emits at least once almost immediately, but "null" from that first emission
    // and "no emission yet" are indistinguishable through collectAsStateWithLifecycle's single
    // initialValue — so this separately confirms a real emission happened before ever showing
    // "ملک یافت نشد" (never show it while the very first load is still in flight).
    LaunchedEffect(propertyId) {
        viewModel.getPropertyById(propertyId).collect { hasLoadedOnce = true }
    }

    LaunchedEffect(viewModel) {
        viewModel.noteDeletionEvents.collect {
            val result = snackbarHostState.showSnackbar(
                message = "یادداشت حذف شد",
                actionLabel = "بازگردانی",
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoLastNoteDelete()
            }
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(property?.title ?: "جزئیات ملک") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    property?.let { p ->
                        IconButton(onClick = { viewModel.toggleFavorite(p) }) {
                            Icon(
                                imageVector = if (p.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "علاقه‌مندی"
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "بیشتر")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("کارت استوری") },
                                    leadingIcon = Icons.Rounded.Share,
                                    onClick = { showMenu = false; onStoryCard(p.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text("ویرایش") },
                                    leadingIcon = Icons.Rounded.Edit,
                                    onClick = { showMenu = false; onEdit(p.id) }
                                )
                                DropdownMenuDivider()
                                DropdownMenuItem(
                                    text = { Text(if (p.isPinned) "برداشتن سنجاق" else "سنجاق کردن") },
                                    leadingIcon = Icons.Rounded.PushPin,
                                    onClick = { showMenu = false; viewModel.togglePinned(p) }
                                )
                                DropdownMenuItem(
                                    text = { Text("صف کاری") },
                                    leadingIcon = Icons.Rounded.Folder,
                                    onClick = { showMenu = false; showFolderDialog = true }
                                )
                                DropdownMenuItem(
                                    text = { Text("تنظیم پیگیری") },
                                    leadingIcon = Icons.Rounded.EventAvailable,
                                    onClick = { showMenu = false; showFollowUpDialog = true }
                                )
                                DropdownMenuDivider()
                                if (p.status == PropertyStatus.ARCHIVED) {
                                    DropdownMenuItem(
                                        text = { Text("بازگردانی از بایگانی") },
                                        leadingIcon = Icons.Rounded.Unarchive,
                                        onClick = { showMenu = false; viewModel.restoreProperty(p) }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text("بایگانی") },
                                        leadingIcon = Icons.Rounded.Archive,
                                        onClick = { showMenu = false; viewModel.archiveProperty(p) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("حذف") },
                                    leadingIcon = Icons.Rounded.Delete,
                                    danger = true,
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = property
        if (current == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (!hasLoadedOnce) {
                    androidx.compose.material3.CircularProgressIndicator()
                } else {
                    Text("ملک یافت نشد")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (current.imageUri != null) {
                        AsyncImage(
                            model = current.imageUri,
                            contentDescription = current.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(heroGradient()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Home,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(imageScrimGradient())
                    )

                    if (current.isPinned) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = "سنجاق‌شده",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        StatusPillBadge(
                            text = current.status.label(),
                            color = current.status.color(),
                            onClick = { showStatusSheet = true },
                            containerColor = Color.White.copy(alpha = 0.92f),
                            textColor = Color.Black,
                            icon = current.status.icon()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = current.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                clipboardManager.setText(AnnotatedString(current.code))
                                Toast.makeText(context, "کد ملک کپی شد", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "${current.propertyType.label()} · ${current.dealType.label()} · ${current.code}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = "کپی کد ملک",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatPrice(current.price, current.dealType),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.screen, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAction(
                        icon = Icons.Rounded.Call,
                        label = "تماس",
                        enabled = current.ownerPhone.isNotBlank(),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${current.ownerPhone}")))
                        }
                    )
                    QuickAction(
                        icon = Icons.Rounded.Share,
                        label = "اشتراک",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareMessage(current, agentPhone))
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ملک"))
                            viewModel.markShared(current)
                        }
                    )
                    QuickAction(
                        icon = Icons.Rounded.Edit,
                        label = "ویرایش",
                        onClick = { onEdit(current.id) }
                    )
                }

                Column(modifier = Modifier.padding(horizontal = Spacing.screen)) {
                val daysSinceUpdate = (System.currentTimeMillis() - current.lastModifiedAt) / (24 * 60 * 60 * 1000L)
                if (daysSinceUpdate >= STALE_PROPERTY_DAYS && current.status != PropertyStatus.ARCHIVED &&
                    current.status != PropertyStatus.SOLD && current.status != PropertyStatus.RENTED
                ) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.extendedColors.warning
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "این ملک $daysSinceUpdate روزه بروزرسانی نشده — شاید وقتشه اطلاعاتش رو بازبینی کنی",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.cardGap))
                }
                DealAssistantSection(current = current, allProperties = allProperties, onOpenDealTool = onOpenDealTool)
                Spacer(modifier = Modifier.height(Spacing.cardGap))
                CollapsibleSection(title = "اطلاعات ملک", initiallyExpanded = true) {
                    DetailRow(label = "شهر", value = current.city)
                    DetailRow(label = "آدرس", value = current.address)
                    DetailRow(label = "متراژ", value = "${current.area} متر مربع")
                    DetailRow(label = "تعداد اتاق", value = current.rooms.toString())
                    if (current.ownerName.isNotBlank()) {
                        DetailRow(label = "مالک", value = current.ownerName)
                    }
                    if (current.ownerPhone.isNotBlank()) {
                        DetailRow(label = "شماره تماس", value = current.ownerPhone)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.cardGap))
                CollapsibleSection(
                    title = "برچسب‌ها",
                    subtitle = if (current.tags.isEmpty()) null else "${current.tags.size} برچسب"
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddTagDialog = true }) { Text("افزودن برچسب") }
                    }
                    if (current.tags.isEmpty()) {
                        Text(
                            "برچسبی اضافه نشده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            current.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { viewModel.removeTag(current, tag) },
                                    label = { Text(tag) },
                                    trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "حذف برچسب", modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.cardGap))
                CollapsibleSection(title = "توضیحات") {
                    Text(text = current.description, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(Spacing.cardGap))
                CollapsibleSection(
                    title = "یادداشت‌ها",
                    subtitle = if (notes.isEmpty()) null else "${notes.size} یادداشت"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddNoteDialog = true }) { Text("افزودن یادداشت") }
                    }
                    if (notes.isEmpty()) {
                        Text(
                            "یادداشتی ثبت نشده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            notes.forEach { note ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(note.content, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            formatDateTime(note.createdAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = { editingNote = note }) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = "ویرایش یادداشت",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteNote(note) }) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = "حذف یادداشت",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.cardGap))
                CollapsibleSection(
                    title = "تاریخچه",
                    subtitle = if (timeline.isEmpty()) null else "${timeline.size} رویداد"
                ) {
                    if (timeline.isEmpty()) {
                        Text(
                            "هنوز رویدادی ثبت نشده",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            timeline.forEach { event ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        event.type.icon(),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(event.description, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            formatDateTime(event.createdAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                }
            }

            if (showStatusSheet) {
                ModalBottomSheet(onDismissRequest = { showStatusSheet = false }) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("وضعیت ملک", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        PropertyStatus.entries.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateStatus(current, status)
                                        showStatusSheet = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(status.color())
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    status.label(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (status == current.status) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "انتخاب‌شده",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (showAddTagDialog) {
                AddTagDialog(
                    suggestions = allTags.filterNot { current.tags.contains(it) },
                    onAdd = { tag -> viewModel.addTag(current, tag) },
                    onDismiss = { showAddTagDialog = false }
                )
            }

            if (showAddNoteDialog) {
                AddNoteDialog(
                    onAdd = { content -> viewModel.addNote(current.id, content) },
                    onDismiss = { showAddNoteDialog = false }
                )
            }

            editingNote?.let { note ->
                EditNoteDialog(
                    initialText = note.content,
                    onSave = { newContent -> viewModel.updateNote(note.copy(content = newContent)) },
                    onDismiss = { editingNote = null }
                )
            }

            if (showFolderDialog) {
                FavoriteFolderDialog(
                    currentFolder = current.favoriteFolder,
                    existingFolders = favoriteFolders,
                    onSelect = { folder -> viewModel.setFavoriteFolder(current, folder) },
                    onDismiss = { showFolderDialog = false }
                )
            }

            if (showFollowUpDialog) {
                FollowUpDialog(
                    hasFollowUp = current.followUpAt != null,
                    onSelect = { timestamp -> viewModel.setFollowUp(current, timestamp) },
                    onDismiss = { showFollowUpDialog = false }
                )
            }

            if (showDeleteConfirm) {
                ConfirmationDialog(
                    title = "حذف این ملک؟",
                    text = "«${current.title}» برای همیشه حذف می‌شود و این کار قابل بازگشت نیست.",
                    confirmLabel = "حذف",
                    danger = true,
                    onConfirm = {
                        viewModel.deleteProperty(current)
                        onDeleted()
                    },
                    onDismiss = { showDeleteConfirm = false }
                )
            }

        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircleIconButton(
            icon = icon,
            onClick = onClick,
            contentDescription = label,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            size = 52.dp,
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Type A/C of the Smart Deal Assistant: commission and price/m² are computed and shown here
 * automatically — no tool to open. Construction cost and rental conversion only ever need one
 * extra number, so they're a single tap away instead of a whole separate workflow.
 */
@Composable
private fun DealAssistantSection(
    current: com.realestate.app.data.Property,
    allProperties: List<com.realestate.app.data.Property>,
    onOpenDealTool: (DealToolId) -> Unit
) {
    val commission = calculateCommission(current.price, defaultCommissionRate())
    val pricePerMeter = calculatePricePerMeter(current.price, current.area)
    val marketEstimate = estimateMarketValue(current, allProperties)
    val moneyFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text("دستیار معامله", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("کارمزد (٪۰.۵)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${moneyFormat.format(commission.perSideShare.toLong())} تومان", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("قیمت هر متر", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${moneyFormat.format(pricePerMeter.toLong())} تومان", style = MaterialTheme.typography.titleSmall)
            }
            if (marketEstimate.comparableCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("نسبت به میانگین بازار", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val diff = marketEstimate.differencePercent
                    Text(
                        "${if (diff >= 0) "+" else ""}${"%.1f".format(diff)}٪",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (diff >= 0) MaterialTheme.extendedColors.success else MaterialTheme.extendedColors.danger
                    )
                }
            }

            val showConstruction = current.propertyType == PropertyType.LAND || current.propertyType == PropertyType.VILLA
            val showRentalConversion = current.dealType == DealType.RENT
            if (showConstruction || showRentalConversion) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showConstruction) {
                        AssistChip(
                            onClick = { onOpenDealTool(DealToolId.CONSTRUCTION_COST) },
                            label = { Text("برآورد هزینه ساخت") }
                        )
                    }
                    if (showRentalConversion) {
                        AssistChip(
                            onClick = { onOpenDealTool(DealToolId.RENTAL_CONVERSION) },
                            label = { Text("تبدیل رهن و اجاره") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, trailing: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTagDialog(suggestions: List<String>, onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن برچسب") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("مثلاً فوری، VIP") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestions.take(8).forEach { suggestion ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    onAdd(suggestion)
                                    onDismiss()
                                },
                                label = { Text(suggestion) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) onAdd(text)
                onDismiss()
            }) { Text("افزودن") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun AddNoteDialog(onAdd: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("افزودن یادداشت") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) onAdd(text)
                onDismiss()
            }) { Text("ثبت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@Composable
private fun EditNoteDialog(initialText: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش یادداشت") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.isNotBlank()) onSave(text)
                onDismiss()
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

private val presetWorkQueues = listOf("پیگیری امروز", "VIP", "سرمایه‌گذاری", "فوری", "این هفته")

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoriteFolderDialog(
    currentFolder: String?,
    existingFolders: List<String>,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentFolder ?: "") }
    val suggestions = (presetWorkQueues + existingFolders).distinct()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("صف کاری") },
        text = {
            Column {
                Text(
                    "این ملک را به یکی از صف‌های کاری اضافه کن یا یک صف جدید بساز",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("مثلاً مشتریان امروز") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { folder ->
                        FilterChip(
                            selected = folder == text,
                            onClick = { text = folder },
                            label = { Text(folder) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSelect(text.trim().ifBlank { null })
                onDismiss()
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FollowUpDialog(
    hasFollowUp: Boolean,
    onSelect: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val dayMillis = 24L * 60 * 60 * 1000
    val options = listOf(
        "امروز" to 0L,
        "فردا" to dayMillis,
        "۳ روز دیگر" to dayMillis * 3,
        "هفته دیگر" to dayMillis * 7
    )
    var customDays by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنظیم پیگیری") },
        text = {
            Column {
                Text(
                    "این ملک در «پیگیری‌های امروز» یادآوری می‌شود",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (label, offset) ->
                        AssistChip(
                            onClick = {
                                onSelect(System.currentTimeMillis() + offset)
                                onDismiss()
                            },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "یا یک تاریخ دلخواه (چند روز دیگر):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customDays,
                        onValueChange = { customDays = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("مثلاً ۱۰") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    TextButton(
                        onClick = {
                            val days = customDays.toLongOrNull()
                            if (days != null && days > 0) {
                                onSelect(System.currentTimeMillis() + dayMillis * days)
                                onDismiss()
                            }
                        },
                        enabled = customDays.toLongOrNull()?.let { it > 0 } == true
                    ) { Text("تنظیم") }
                }
                if (hasFollowUp) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = {
                        onSelect(null)
                        onDismiss()
                    }) { Text("حذف پیگیری") }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        }
    )
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(timestamp))

private fun TimelineEventType.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    TimelineEventType.CREATED -> Icons.Rounded.Add
    TimelineEventType.EDITED -> Icons.Rounded.Edit
    TimelineEventType.PRICE_CHANGED -> Icons.Rounded.TrendingUp
    TimelineEventType.SHARED -> Icons.Rounded.Share
    TimelineEventType.ARCHIVED -> Icons.Rounded.History
    TimelineEventType.RESTORED -> Icons.Rounded.CheckCircle
    TimelineEventType.NOTE_ADDED -> Icons.Rounded.StickyNote2
    TimelineEventType.FOLLOW_UP_SET -> Icons.Rounded.EventAvailable
}
