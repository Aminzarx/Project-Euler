package com.realestate.app.ui.screens.createcase

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.CaseType
import com.realestate.app.data.Property
import com.realestate.app.data.PropertyType
import com.realestate.app.data.clientRequestTransactionTypes
import com.realestate.app.data.ownerTransactionTypes
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.components.label
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.PropertyViewModel

/**
 * Replaces AddEditPropertyScreen as the app's single entry point for creating or editing a
 * record. The core idea (see data/Property.kt): every record is a Case — either an [CaseType.OWNER]
 * listing or a [CaseType.CLIENT_REQUEST] — and the form that follows is assembled from only the
 * fields that case actually needs, never a single generic property form.
 *
 * New cases walk Steps 1→4 in order. Editing an existing case jumps straight to Step 4 (its
 * identity is already known), with the three Step 1-3 choices shown as tappable chips that jump
 * back if the agent needs to correct one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCaseWizardScreen(
    propertyId: Long?,
    viewModel: PropertyViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = propertyId != null
    val existing by if (propertyId != null) {
        viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf<Property?>(null) }
    }
    var hasLoadedOnce by remember(propertyId) { mutableStateOf(propertyId == null) }
    LaunchedEffect(propertyId) {
        if (propertyId != null) viewModel.getPropertyById(propertyId).collect { hasLoadedOnce = true }
    }

    var step by rememberSaveable { mutableStateOf(if (isEditMode) 4 else 1) }
    var caseType by rememberSaveable { mutableStateOf<CaseType?>(if (isEditMode) null else null) }
    var transactionType by rememberSaveable { mutableStateOf<CaseTransactionType?>(null) }
    var propertyType by rememberSaveable { mutableStateOf<PropertyType?>(null) }
    val formState = remember { CaseFormState() }
    var loadedIntoForm by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(existing) {
        val p = existing
        if (p != null && !loadedIntoForm) {
            caseType = p.caseType
            transactionType = p.transactionType
            propertyType = p.propertyType
            formState.loadFrom(p)
            loadedIntoForm = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // برخی منابع اجازه دسترسی دائمی نمی‌دهند؛ عکس فقط تا پایان این نشست قابل نمایش خواهد بود
            }
            formState.imageUri = uri.toString()
        }
    }

    fun goBack() {
        when {
            step > 1 && !isEditMode -> step -= 1
            step == 4 && isEditMode -> onDone()
            else -> onDone()
        }
    }

    BackHandler(onBack = ::goBack)

    fun buildProperty(): Property {
        val resolvedCaseType = caseType ?: CaseType.OWNER
        val flags = (formState.ownerFlags + formState.requirementFlags + formState.rentalFlags)
        return Property(
            id = propertyId ?: 0,
            title = formState.title.trim().ifBlank {
                if (resolvedCaseType == CaseType.OWNER) formState.address.trim() else formState.contactName.trim()
            },
            description = formState.description.trim(),
            price = formState.price.toLongOrNull() ?: 0,
            area = formState.area.toDoubleOrNull() ?: 0.0,
            rooms = formState.rooms.toIntOrNull() ?: 0,
            city = formState.city.trim(),
            address = formState.address.trim(),
            ownerName = formState.contactName.trim(),
            ownerPhone = formState.contactPhone.trim(),
            dealType = legacyDealTypeFor(transactionType),
            propertyType = propertyType ?: PropertyType.APARTMENT,
            status = formState.status,
            tags = formState.tags,
            imageUri = formState.imageUri,
            isFavorite = existing?.isFavorite ?: false,
            isPinned = existing?.isPinned ?: false,
            favoriteFolder = existing?.favoriteFolder,
            dateAdded = existing?.dateAdded ?: System.currentTimeMillis(),
            lastViewedAt = existing?.lastViewedAt,
            lastSharedAt = existing?.lastSharedAt,
            viewCount = existing?.viewCount ?: 0,
            followUpAt = existing?.followUpAt,
            caseType = resolvedCaseType,
            transactionType = transactionType,
            caseFlags = flags.toList(),
            priority = formState.priority,
            expiryType = formState.expiryType,
            customExpiryAt = formState.customExpiryAt,
            mortgageStatus = formState.mortgageStatus,
            titleDeedReady = formState.titleDeedReady,
            reasonForSelling = formState.reasonForSelling.trim().ifBlank { null },
            viewingHours = formState.viewingHours.trim().ifBlank { null },
            keyHolder = formState.keyHolder.trim().ifBlank { null },
            paymentConditions = formState.paymentConditions.trim().ifBlank { null },
            constructionAge = formState.constructionAge.toIntOrNull(),
            legalStatus = formState.legalStatus.trim().ifBlank { null },
            hiddenNotes = formState.hiddenNotes.trim().ifBlank { null },
            floor = formState.floor.toIntOrNull(),
            totalFloors = formState.totalFloors.toIntOrNull(),
            landZoning = formState.landZoning.trim().ifBlank { null },
            hasBusinessLicense = formState.hasBusinessLicense,
            budgetMin = formState.budgetMin.toLongOrNull(),
            budgetMax = formState.budgetMax.toLongOrNull(),
            desiredMinArea = formState.desiredMinArea.toDoubleOrNull(),
            desiredMaxArea = formState.desiredMaxArea.toDoubleOrNull(),
            desiredBedrooms = formState.desiredBedrooms.toIntOrNull(),
            preferredAreas = formState.preferredAreas,
            floorPreference = formState.floorPreference.trim().ifBlank { null },
            viewPreference = formState.viewPreference.trim().ifBlank { null },
            cashAvailable = formState.cashAvailable.toLongOrNull(),
            maxDeposit = formState.maxDeposit.toLongOrNull(),
            maxMonthlyRent = formState.maxMonthlyRent.toLongOrNull()
        )
    }

    fun save() {
        if (isSaving) return
        isSaving = true
        val property = buildProperty()
        if (isEditMode) {
            viewModel.updateProperty(property)
            Toast.makeText(context, "تغییرات ذخیره شد", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addProperty(property)
            Toast.makeText(context, "پرونده ثبت شد", Toast.LENGTH_SHORT).show()
        }
        onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stepTitle(step, isEditMode)) },
                navigationIcon = {
                    IconButton(onClick = ::goBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        if (isEditMode && !hasLoadedOnce) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isEditMode) {
                LinearProgressIndicator(
                    progress = { step / 4f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState >= initialState) {
                        (slideInHorizontally(tween(280)) { it / 3 } + fadeIn(tween(280))) togetherWith
                            (slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(tween(280)) { -it / 3 } + fadeIn(tween(280))) togetherWith
                            (slideOutHorizontally(tween(280)) { it / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "create-case-step"
            ) { currentStep ->
                when (currentStep) {
                    1 -> CaseTypeStep(
                        selected = caseType,
                        onSelect = { caseType = it; step = 2 }
                    )
                    2 -> {
                        val type = caseType
                        if (type != null) {
                            TransactionTypeStep(
                                caseType = type,
                                selected = transactionType,
                                onSelect = { transactionType = it; step = 3 }
                            )
                        }
                    }
                    3 -> PropertyTypeStep(
                        selected = propertyType,
                        onSelect = { propertyType = it; step = 4 }
                    )
                    else -> {
                        val type = caseType
                        if (type != null) {
                            CaseDetailsForm(
                                state = formState,
                                caseType = type,
                                transactionType = transactionType,
                                propertyType = propertyType ?: PropertyType.APARTMENT,
                                isEditMode = isEditMode,
                                isSaving = isSaving,
                                onEditCaseType = { step = 1 },
                                onEditTransactionType = { step = 2 },
                                onEditPropertyType = { step = 3 },
                                onPickImage = { imagePicker.launch("image/*") },
                                onSave = ::save
                            )
                        }
                    }
                }
            }
        }

        if (showExitConfirm) {
            ConfirmationDialog(
                title = "خروج بدون ذخیره؟",
                text = "تغییراتی که وارد کرده‌اید ذخیره نشده و از دست می‌رود.",
                confirmLabel = "خروج بدون ذخیره",
                danger = true,
                onConfirm = onDone,
                onDismiss = { showExitConfirm = false }
            )
        }
    }
}

private fun stepTitle(step: Int, isEditMode: Boolean): String = when {
    isEditMode -> "ویرایش پرونده"
    step == 1 -> "چه می‌خواهید ثبت کنید؟"
    step == 2 -> "نوع معامله"
    step == 3 -> "نوع ملک"
    else -> "جزئیات پرونده"
}

/** [DealType] predates the Case redesign and several legacy screens (StoryCard, ad-text, price
 *  formatting) still read it directly — this keeps them working by mapping every transaction type
 *  onto whichever of SALE/RENT it reads closest to, rather than rewriting every such screen in
 *  this pass. See ARCHITECTURE notes in AboutScreen/PropertyDetailScreen for the follow-up. */
private fun legacyDealTypeFor(transactionType: CaseTransactionType?): com.realestate.app.data.DealType =
    when (transactionType) {
        com.realestate.app.data.CaseTransactionType.RENT,
        com.realestate.app.data.CaseTransactionType.MORTGAGE_AND_RENT,
        com.realestate.app.data.CaseTransactionType.FULL_MORTGAGE -> com.realestate.app.data.DealType.RENT
        else -> com.realestate.app.data.DealType.SALE
    }

@Composable
private fun CaseTypeStep(selected: CaseType?, onSelect: (CaseType) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screen),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "چه می‌خواهید ثبت کنید؟",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "هر پرونده یا مربوط به یک مالک است یا یک درخواست از سمت مشتری.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        CaseTypeCard(
            icon = Icons.Rounded.Storefront,
            title = "مالک",
            subtitle = "مشتری صاحب ملکی است که می‌خواهد بفروشد، اجاره دهد یا واگذار کند.",
            selected = selected == CaseType.OWNER,
            onClick = { onSelect(CaseType.OWNER) }
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        CaseTypeCard(
            icon = Icons.Rounded.PersonSearch,
            title = "درخواست مشتری",
            subtitle = "مشتری به دنبال ملکی برای خرید، رهن یا اجاره است.",
            selected = selected == CaseType.CLIENT_REQUEST,
            onClick = { onSelect(CaseType.CLIENT_REQUEST) }
        )
    }
}

@Composable
private fun CaseTypeCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AppCard(
        onClick = onClick,
        contentPadding = PaddingValues(Spacing.lg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.extendedColors.accent.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TransactionTypeStep(
    caseType: CaseType,
    selected: CaseTransactionType?,
    onSelect: (CaseTransactionType) -> Unit
) {
    val options = if (caseType == CaseType.OWNER) ownerTransactionTypes else clientRequestTransactionTypes
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screen)
    ) {
        Text("نوع معامله مورد نظر چیست؟", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Spacing.md))
        options.forEach { type ->
            AppCard(
                onClick = { onSelect(type) },
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 14.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(type.label(), style = MaterialTheme.typography.titleMedium)
                    if (selected == type) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyTypeStep(selected: PropertyType?, onSelect: (PropertyType) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(Spacing.screen)) {
        Text("نوع ملک چیست؟", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Spacing.md))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(PropertyType.entries) { type ->
                AppCard(
                    onClick = { onSelect(type) },
                    contentPadding = PaddingValues(Spacing.md),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            type.icon(),
                            contentDescription = null,
                            tint = if (selected == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(type.label(), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
