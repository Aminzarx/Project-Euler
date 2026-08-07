@file:OptIn(ExperimentalLayoutApi::class)

package com.realestate.app.ui.screens.createcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.realestate.app.data.CaseFlag
import com.realestate.app.data.CasePriority
import com.realestate.app.data.CaseTransactionType
import com.realestate.app.data.CaseType
import com.realestate.app.data.contact.Contact
import com.realestate.app.data.FloorPreferenceOption
import com.realestate.app.data.LeadSource
import com.realestate.app.data.LegalDocumentType
import com.realestate.app.data.OwnershipType
import com.realestate.app.data.PropertyFormShape
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.RequestValidityType
import com.realestate.app.data.ViewPreferenceOption
import com.realestate.app.data.VisitStatus
import com.realestate.app.data.WaterSource
import com.realestate.app.data.formShape
import com.realestate.app.data.isValidIranianMobile
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.color
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.components.label
import com.realestate.app.ui.components.normalizeDigits
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ownerStatuses = listOf(
    PropertyStatus.NEW, PropertyStatus.READY, PropertyStatus.ACTIVE, PropertyStatus.NEGOTIATING,
    PropertyStatus.RESERVED, PropertyStatus.SOLD, PropertyStatus.RENTED, PropertyStatus.ARCHIVED
)
private val clientRequestStatuses = listOf(
    PropertyStatus.SEARCHING, PropertyStatus.VISITED, PropertyStatus.NEGOTIATING, PropertyStatus.WAITING,
    PropertyStatus.RESERVED, PropertyStatus.CONTRACT_SIGNED, PropertyStatus.ARCHIVED,
    PropertyStatus.CANCELLED, PropertyStatus.EXPIRED
)

private val rentLikeTransactions = setOf(
    CaseTransactionType.RENT, CaseTransactionType.MORTGAGE_AND_RENT, CaseTransactionType.FULL_MORTGAGE
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CaseDetailsForm(
    state: CaseFormState,
    caseType: CaseType,
    transactionType: CaseTransactionType?,
    propertyType: PropertyType,
    isEditMode: Boolean,
    isSaving: Boolean,
    availableDistricts: List<String>,
    searchContacts: suspend (String) -> List<Contact>,
    caseCountForContact: suspend (Long) -> Int,
    onEditCaseType: () -> Unit,
    onEditTransactionType: () -> Unit,
    onEditPropertyType: () -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit
) {
    // Each case type accepts either one of two fields — never both — so the reason a disabled
    // Save button gives has to name both alternatives, not just one, or it reads as a lie once the
    // agent fills the field it happened to omit.
    val identityFilled = when (caseType) {
        CaseType.OWNER -> state.address.isNotBlank() || state.title.isNotBlank()
        CaseType.CLIENT_REQUEST -> state.contactName.isNotBlank() || state.contactPhone.isNotBlank()
    }
    val phoneValid = state.contactPhone.isBlank() || isValidIranianMobile(state.contactPhone)
    val isFormValid = identityFilled && phoneValid
    val disabledReason = when {
        !identityFilled && caseType == CaseType.OWNER -> "عنوان پرونده یا آدرس را وارد کنید"
        !identityFilled -> "نام مشتری یا شماره تماس را وارد کنید"
        !phoneValid -> "شماره تماس معتبر نیست"
        else -> null
    }

    // Non-blocking data-quality nudges — never disable Save (an agent legitimately creating a
    // case before every detail is settled is a normal workflow this app already supports), but
    // surface what's missing so it doesn't silently stay missing forever.
    val completenessHints = buildList {
        if (state.city.isBlank()) add("شهر مشخص نشده")
        if (state.description.isNotBlank() && state.description.trim().length < 10) {
            add("توضیحات خیلی کوتاه است")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.screen)
    ) {
        // Selection summary — tap any chip to jump back and change it.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = onEditCaseType, label = { Text(caseType.label()) })
            // Rendered even with nothing selected. A record created before the Case redesign has a
            // null transactionType, and tapping this chip is the only route to the step that sets
            // one — hiding it when empty made that step permanently unreachable for exactly the
            // records that still need it.
            AssistChip(
                onClick = onEditTransactionType,
                label = { Text(transactionType?.label() ?: "نوع معامله؟") }
            )
            AssistChip(onClick = onEditPropertyType, label = { Text(propertyType.label()) })
        }
        Spacer(modifier = Modifier.height(Spacing.md))

        ImagePickerBox(imageUri = state.imageUri, onPick = onPickImage, onRemove = { state.imageUri = null })
        Spacer(modifier = Modifier.height(Spacing.cardGap))

        if (caseType == CaseType.OWNER) {
            OwnerSections(state, propertyType, transactionType, availableDistricts, searchContacts, caseCountForContact)
        } else {
            ClientRequestSections(state, transactionType, availableDistricts, searchContacts, caseCountForContact)
        }

        Spacer(modifier = Modifier.height(Spacing.cardGap))
        ManagementSection(state)

        Spacer(modifier = Modifier.height(Spacing.cardGap))
        Text("برچسب‌ها", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Spacing.sm))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            TagEditor(tags = state.tags, onTagsChange = { state.tags = it })
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
        if (disabledReason != null) {
            ValidationMessage(text = disabledReason, modifier = Modifier.padding(bottom = 8.dp))
        } else if (completenessHints.isNotEmpty()) {
            CompletenessHintMessage(hints = completenessHints, modifier = Modifier.padding(bottom = 8.dp))
        }
        PrimaryButton(
            text = if (isEditMode) "ذخیره تغییرات" else "ثبت پرونده",
            onClick = onSave,
            enabled = isFormValid && !isSaving,
            loading = isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OwnerSections(
    state: CaseFormState,
    propertyType: PropertyType,
    transactionType: CaseTransactionType?,
    availableDistricts: List<String>,
    searchContacts: suspend (String) -> List<Contact>,
    caseCountForContact: suspend (Long) -> Int
) {
    // ---- Identity ----
    Text("اطلاعات مالک", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        ContactPickerField(
            name = state.contactName,
            phone = state.contactPhone,
            contactId = state.contactId,
            nameLabel = "نام مالک",
            phoneLabel = "شماره تماس مالک",
            onNameChange = { state.contactName = it },
            onPhoneChange = { state.contactPhone = it },
            onContactIdChange = { state.contactId = it },
            searchContacts = searchContacts,
            caseCountForContact = caseCountForContact
        )
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("عنوان و توضیحات", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        // Title and address are the OR-pair that satisfies this case's minimum requirement — the
        // wording says so explicitly instead of marking just one of them with a star, which would
        // claim a strictness neither field actually has on its own.
        Text(
            "برای ثبت، حداقل عنوان پرونده یا آدرس را وارد کنید",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = { state.title = it },
            label = { Text("عنوان پرونده") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = { state.description = it },
            label = { Text("توضیحات") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // ---- Location ----
    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("موقعیت مکانی", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        LabeledField(state::city, "شهر")
        Spacer(modifier = Modifier.height(8.dp))
        DistrictField(value = state.district, onValueChange = { state.district = it }, suggestions = availableDistricts)
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::address, "آدرس")
    }

    // ---- Specifications ----
    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("مشخصات ملک", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        DecimalField(state::area, "متراژ (متر مربع)")

        when (propertyType.formShape()) {
            PropertyFormShape.RESIDENTIAL -> {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IntegerField(state::rooms, "تعداد اتاق", modifier = Modifier.weight(1f))
                    IntegerField(state::floor, "طبقه", modifier = Modifier.weight(1f))
                    IntegerField(state::totalFloors, "تعداد کل طبقات", modifier = Modifier.weight(1f))
                }
            }
            PropertyFormShape.LAND -> {
                Spacer(modifier = Modifier.height(8.dp))
                LabeledField(state::landZoning, "کاربری زمین (مسکونی/کشاورزی/تجاری)")
            }
            PropertyFormShape.COMMERCIAL -> {
                Spacer(modifier = Modifier.height(8.dp))
                TriStateRow("دارای پروانه کسب", state.hasBusinessLicense) { state.hasBusinessLicense = it }
                if (propertyType == PropertyType.SHOP || propertyType == PropertyType.OFFICE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DecimalField(state::frontageWidth, "متراژ بر تجاری (متر)")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        IntegerField(state::constructionAge, "سن بنا (سال)")

        if (propertyType.formShape() == PropertyFormShape.RESIDENTIAL) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("امکانات ملک", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlagChipGroup(
                flags = CaseFormState.AMENITY_OWNER_FLAGS.toList(),
                selected = state.ownerFlags,
                onToggle = { flag, isOn -> state.ownerFlags = if (isOn) state.ownerFlags + flag else state.ownerFlags - flag }
            )
        }
    }

    // ---- Dynamic: Project Details ----
    if (propertyType == PropertyType.PROJECT || transactionType == CaseTransactionType.PRE_SALE) {
        Spacer(modifier = Modifier.height(Spacing.sm))
        CollapsibleSection(
            title = "اطلاعات پروژه",
            subtitle = "سازنده، تاریخ تحویل و پیشرفت ساخت",
            initiallyExpanded = propertyType == PropertyType.PROJECT
        ) {
            LabeledField(state::developerName, "نام سازنده")
            Spacer(modifier = Modifier.height(8.dp))
            DateField(label = "تاریخ تحویل موردانتظار", valueMillis = state.expectedDeliveryDate, onValueChange = { state.expectedDeliveryDate = it })
            Spacer(modifier = Modifier.height(8.dp))
            IntegerField(state::constructionProgressPercent, "درصد پیشرفت ساخت")
        }
    }

    // ---- Dynamic: Agricultural Details ----
    if (propertyType == PropertyType.FARM || propertyType == PropertyType.GARDEN) {
        Spacer(modifier = Modifier.height(Spacing.sm))
        CollapsibleSection(title = "اطلاعات کشاورزی", subtitle = "منبع آب و مجوز چاه", initiallyExpanded = true) {
            Text("منبع آب", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WaterSource.entries.forEach { option ->
                    FilterChip(selected = state.waterSource == option, onClick = { state.waterSource = option }, label = { Text(option.label()) })
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            TriStateRow("دارای پروانه چاه", state.hasWellPermit) { state.hasWellPermit = it }
        }
    }

    // ---- Pricing ----
    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("قیمت", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        if (transactionType == CaseTransactionType.MORTGAGE_AND_RENT) {
            // رهن و اجاره always needs both numbers — showing them side by side keeps the pairing
            // reading as one decision instead of two unrelated fields.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField(label = "مبلغ رهن (تومان)", value = state.depositAmount, onValueChange = { state.depositAmount = it }, modifier = Modifier.weight(1f))
                MoneyField(label = "اجاره ماهانه (تومان)", value = state.price, onValueChange = { state.price = it }, modifier = Modifier.weight(1f))
            }
        } else if (transactionType == CaseTransactionType.FULL_MORTGAGE) {
            MoneyField(label = "مبلغ رهن کامل (تومان)", value = state.depositAmount, onValueChange = { state.depositAmount = it }, modifier = Modifier.fillMaxWidth())
        } else if (transactionType == CaseTransactionType.RENT) {
            MoneyField(label = "اجاره ماهانه (تومان)", value = state.price, onValueChange = { state.price = it }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            MoneyField(label = "مبلغ رهن (اختیاری، تومان)", value = state.depositAmount, onValueChange = { state.depositAmount = it }, modifier = Modifier.fillMaxWidth())
        } else {
            MoneyField(label = "قیمت کل (تومان)", value = state.price, onValueChange = { state.price = it }, modifier = Modifier.fillMaxWidth())
        }
    }

    // ---- Workflow ----
    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("وضعیت پرونده", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        StatusDropdown(status = state.status, options = ownerStatuses, onSelect = { state.status = it })
        Spacer(modifier = Modifier.height(8.dp))
        PriorityRow(state.priority) { state.priority = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("مدت اعتبار آگهی/قرارداد", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestValidityType.entries.forEach { option ->
                FilterChip(selected = state.expiryType == option, onClick = { state.expiryType = option }, label = { Text(option.label()) })
            }
        }
        if (state.expiryType == RequestValidityType.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            DateField(label = "تاریخ انقضا", valueMillis = state.customExpiryAt, onValueChange = { state.customExpiryAt = it })
        }
    }

    // ---- Visit & Collaboration ----
    Spacer(modifier = Modifier.height(Spacing.cardGap))
    CollapsibleSection(title = "بازدید و همکاری", subtitle = "ساعت بازدید، کلیدار و شرایط همکاری") {
        LabeledField(state::viewingHours, "ساعات بازدید (یادداشت آزاد)")
        Spacer(modifier = Modifier.height(8.dp))
        DateField(label = "زمان بازدید بعدی", valueMillis = state.nextViewingAt, onValueChange = { state.nextViewingAt = it })
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::keyHolder, "کلیدار")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::paymentConditions, "شرایط پرداخت")
        Spacer(modifier = Modifier.height(10.dp))
        FlagChipGroup(
            flags = CaseFormState.OWNER_STATUS_FLAGS.toList(),
            selected = state.ownerFlags,
            onToggle = { flag, isOn -> state.ownerFlags = if (isOn) state.ownerFlags + flag else state.ownerFlags - flag }
        )
    }

    // ---- Legal & Ownership ----
    Spacer(modifier = Modifier.height(Spacing.sm))
    CollapsibleSection(title = "اطلاعات حقوقی و مالکیت", subtitle = "نوع سند، وضعیت مالکیت و مسائل قانونی") {
        Text("نوع سند", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        LegalDocumentTypeDropdown(value = state.legalDocumentType, onSelect = { state.legalDocumentType = it })
        Spacer(modifier = Modifier.height(8.dp))
        TriStateRow("سند آماده است", state.titleDeedReady) { state.titleDeedReady = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("وضعیت مالکیت", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OwnershipType.entries.forEach { option ->
                FilterChip(selected = state.ownershipType == option, onClick = { state.ownershipType = option }, label = { Text(option.label()) })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::legalStatus, "توضیحات حقوقی تکمیلی")
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    CollapsibleSection(title = "یادداشت‌ها", subtitle = "دلیل فروش و یادداشت‌های داخلی") {
        LabeledField(state::reasonForSelling, "دلیل فروش/اجاره")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.hiddenNotes,
            onValueChange = { state.hiddenNotes = it },
            label = { Text("یادداشت داخلی (فقط برای شما، در اشتراک‌گذاری نمایش داده نمی‌شود)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ClientRequestSections(
    state: CaseFormState,
    transactionType: CaseTransactionType?,
    availableDistricts: List<String>,
    searchContacts: suspend (String) -> List<Contact>,
    caseCountForContact: suspend (Long) -> Int
) {
    Text("اطلاعات مشتری", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            "برای ثبت، حداقل نام مشتری یا شماره تماس را وارد کنید",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        ContactPickerField(
            name = state.contactName,
            phone = state.contactPhone,
            contactId = state.contactId,
            nameLabel = "نام مشتری",
            phoneLabel = "شماره تماس",
            onNameChange = { state.contactName = it },
            onPhoneChange = { state.contactPhone = it },
            onContactIdChange = { state.contactId = it },
            searchContacts = searchContacts,
            caseCountForContact = caseCountForContact
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = { state.description = it },
            label = { Text("توضیحات درخواست") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("بودجه", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyField(label = "حداقل بودجه (تومان)", value = state.budgetMin, onValueChange = { state.budgetMin = it }, modifier = Modifier.weight(1f))
            MoneyField(label = "حداکثر بودجه (تومان)", value = state.budgetMax, onValueChange = { state.budgetMax = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        MoneyField(label = "نقدینگی در دسترس (تومان)", value = state.cashAvailable, onValueChange = { state.cashAvailable = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("نیاز به وام دارد", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = state.needsLoanFinancing, onCheckedChange = { state.needsLoanFinancing = it })
        }
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("ملک مورد نظر", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DecimalField(state::desiredMinArea, "حداقل متراژ", modifier = Modifier.weight(1f))
            DecimalField(state::desiredMaxArea, "حداکثر متراژ", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        IntegerField(state::desiredBedrooms, "حداقل تعداد اتاق")
        Spacer(modifier = Modifier.height(10.dp))
        Text("طبقه ترجیحی", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FloorPreferenceOption.entries.forEach { option ->
                val isOn = option in state.floorPreferenceOptions
                FilterChip(
                    selected = isOn,
                    onClick = {
                        state.floorPreferenceOptions =
                            if (isOn) state.floorPreferenceOptions - option else state.floorPreferenceOptions + option
                    },
                    label = { Text(option.label()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("منظره ترجیحی", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewPreferenceOption.entries.forEach { option ->
                val isOn = option in state.viewPreferenceOptions
                FilterChip(
                    selected = isOn,
                    onClick = {
                        state.viewPreferenceOptions =
                            if (isOn) state.viewPreferenceOptions - option else state.viewPreferenceOptions + option
                    },
                    label = { Text(option.label()) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("مناطق مورد نظر", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        LabeledField(state::city, "شهر")
        Spacer(modifier = Modifier.height(10.dp))
        TagEditor(
            tags = state.preferredAreas,
            onTagsChange = { state.preferredAreas = it },
            label = "افزودن محله/منطقه",
            suggestions = availableDistricts
        )
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    CollapsibleSection(title = "ویژگی‌های مورد نیاز", subtitle = "امکاناتی که مشتری به آن‌ها نیاز دارد", initiallyExpanded = true) {
        FlagChipGroup(
            flags = CaseFormState.AMENITY_REQUIREMENT_FLAGS.toList(),
            selected = state.requirementFlags,
            onToggle = { flag, isOn -> state.requirementFlags = if (isOn) state.requirementFlags + flag else state.requirementFlags - flag }
        )
    }

    if (transactionType in rentLikeTransactions) {
        Spacer(modifier = Modifier.height(Spacing.sm))
        CollapsibleSection(title = "شرایط اجاره", subtitle = "سقف رهن و اجاره ماهانه", initiallyExpanded = true) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MoneyField(label = "حداکثر رهن (تومان)", value = state.maxDeposit, onValueChange = { state.maxDeposit = it }, modifier = Modifier.weight(1f))
                MoneyField(label = "حداکثر اجاره ماهانه (تومان)", value = state.maxMonthlyRent, onValueChange = { state.maxMonthlyRent = it }, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            FlagChipGroup(
                flags = CaseFormState.RENTAL_FLAGS.toList(),
                selected = state.rentalFlags,
                onToggle = { flag, isOn -> state.rentalFlags = if (isOn) state.rentalFlags + flag else state.rentalFlags - flag }
            )
        }
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    CollapsibleSection(title = "اولویت و اعتبار درخواست", subtitle = "فوریت و مدت اعتبار این درخواست") {
        PriorityRow(state.priority) { state.priority = it }
        Spacer(modifier = Modifier.height(10.dp))
        Text("اعتبار درخواست", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RequestValidityType.entries.forEach { option ->
                FilterChip(selected = state.expiryType == option, onClick = { state.expiryType = option }, label = { Text(option.label()) })
            }
        }
        if (state.expiryType == RequestValidityType.CUSTOM) {
            Spacer(modifier = Modifier.height(8.dp))
            DateField(label = "تاریخ انقضا", valueMillis = state.customExpiryAt, onValueChange = { state.customExpiryAt = it })
        }
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("وضعیت پرونده", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        StatusDropdown(status = state.status, options = clientRequestStatuses, onSelect = { state.status = it })
    }
}

/** Shown for both case types — lead tracking, follow-up bookkeeping and confidentiality aren't
 *  specific to whether the case is a listing or a request, so this sits after both branches
 *  instead of being duplicated inside each one. */
@Composable
private fun ManagementSection(state: CaseFormState) {
    Text("مدیریت پرونده", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text("منبع ورودی", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LeadSource.entries.forEach { option ->
                FilterChip(selected = state.leadSource == option, onClick = { state.leadSource = option }, label = { Text(option.label()) })
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LabeledField(state::responsibleAgent, "مشاور مسئول")
        Spacer(modifier = Modifier.height(8.dp))
        DateField(label = "آخرین تماس", valueMillis = state.lastContactAt, onValueChange = { state.lastContactAt = it })
        Spacer(modifier = Modifier.height(10.dp))
        Text("وضعیت بازدید", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisitStatus.entries.forEach { option ->
                FilterChip(selected = state.visitStatus == option, onClick = { state.visitStatus = option }, label = { Text(option.label()) })
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("محرمانه", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "هرگز در خروجی‌های اشتراک‌گذاری (استوری، متن آگهی، فایل پرونده) ظاهر نمی‌شود",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.isConfidential, onCheckedChange = { state.isConfidential = it })
        }
    }
}

// ---------- Shared small building blocks ----------

@Composable
private fun ImagePickerBox(imageUri: String?, onPick: () -> Unit, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPick)
            .semantics { contentDescription = "افزودن تصویر یا مدرک" },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(model = imageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(140.dp))
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "حذف تصویر")
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("افزودن تصویر یا مدرک")
            }
        }
    }
}

/** A short, actionable line explaining *why* the primary action is disabled — never just a greyed
 *  out button with no explanation of what to fix. */
@Composable
private fun ValidationMessage(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

/** Same layout as [ValidationMessage] but a warning tone and never blocks Save — a nudge toward
 *  more complete data (missing city, a suspiciously short description), not an error. */
@Composable
private fun CompletenessHintMessage(hints: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.extendedColors.warning
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(hints.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extendedColors.warning)
    }
}

@Composable
private fun LabeledField(
    value: kotlin.reflect.KMutableProperty0<String>,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value.get(),
        onValueChange = { value.set(it) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun IntegerField(value: kotlin.reflect.KMutableProperty0<String>, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.get(),
        onValueChange = { value.set(normalizeDigits(it).filter { c -> c.isDigit() }) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun DecimalField(value: kotlin.reflect.KMutableProperty0<String>, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value.get(),
        onValueChange = { input ->
            val digitsOnly = normalizeDigits(input).filter { it in '0'..'9' || it == '.' }
            val firstDot = digitsOnly.indexOf('.')
            value.set(
                if (firstDot == -1) digitsOnly
                else digitsOnly.substring(0, firstDot + 1) + digitsOnly.substring(firstDot + 1).replace(".", "")
            )
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun TriStateRow(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = value == true, onClick = { onChange(true) }, label = { Text("بله") })
            FilterChip(selected = value == false, onClick = { onChange(false) }, label = { Text("خیر") })
            FilterChip(selected = value == null, onClick = { onChange(null) }, label = { Text("نامشخص") })
        }
    }
}

@Composable
private fun PriorityRow(selected: CasePriority, onSelect: (CasePriority) -> Unit) {
    Column {
        Text("اولویت", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CasePriority.entries.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(option.label()) }
                )
            }
        }
    }
}

@Composable
private fun FlagChipGroup(flags: List<CaseFlag>, selected: Set<CaseFlag>, onToggle: (CaseFlag, Boolean) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        flags.forEach { flag ->
            val isOn = flag in selected
            FilterChip(selected = isOn, onClick = { onToggle(flag, !isOn) }, label = { Text(flag.label()) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(status: PropertyStatus, options: List<PropertyStatus>, onSelect: (PropertyStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = status.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text("وضعیت") },
            leadingIcon = { Icon(status.icon(), contentDescription = null, tint = status.color()) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    leadingIcon = option.icon(),
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalDocumentTypeDropdown(value: LegalDocumentType?, onSelect: (LegalDocumentType?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value?.label() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("نوع سند") },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LegalDocumentType.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

/** Read-only text field + calendar icon opening a [DatePickerDialog] — the one shared date input
 *  every new date field in this form (visit scheduling, delivery date, last contact, custom
 *  expiry) is built from, instead of four near-duplicate pickers. Displays in Gregorian (ASCII
 *  digits); the value stored is a plain epoch millis [Long], so switching the display to the
 *  app's Jalali preference later is a display-only change, not a data migration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(label: String, valueMillis: Long?, onValueChange: (Long?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.US) }
    OutlinedTextField(
        value = valueMillis?.let { formatter.format(Date(it)) } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Row {
                if (valueMillis != null) {
                    IconButton(onClick = { onValueChange(null) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "پاک کردن تاریخ")
                    }
                }
                IconButton(onClick = { showPicker = true }) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "انتخاب تاریخ")
                }
            }
        },
        modifier = Modifier.fillMaxWidth().clickable { showPicker = true }
    )
    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = valueMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(pickerState.selectedDateMillis)
                    showPicker = false
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("انصراف") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/** Name + phone fields with a live search-and-reuse layer on top — typing a name or phone that
 *  matches an existing [Contact] surfaces it below (with how many other cases already reference
 *  it) so attaching the existing person is one tap instead of retyping and accidentally creating
 *  a duplicate. Ignoring the suggestions entirely and just typing works exactly as it always did
 *  — [onContactIdChange] simply never fires, and a fresh Contact is created automatically for the
 *  case at save time (see PropertyViewModel.addProperty). */
@Composable
private fun ContactPickerField(
    name: String,
    phone: String,
    contactId: Long?,
    nameLabel: String,
    phoneLabel: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onContactIdChange: (Long?) -> Unit,
    searchContacts: suspend (String) -> List<Contact>,
    caseCountForContact: suspend (Long) -> Int
) {
    var results by remember { mutableStateOf<List<Pair<Contact, Int>>>(emptyList()) }

    LaunchedEffect(name, phone, contactId) {
        if (contactId != null) {
            results = emptyList()
            return@LaunchedEffect
        }
        val query = phone.ifBlank { name }
        results = if (query.isBlank()) {
            emptyList()
        } else {
            searchContacts(query).map { contact -> contact to caseCountForContact(contact.id) }
        }
    }

    OutlinedTextField(
        value = name,
        onValueChange = {
            onNameChange(it)
            if (contactId != null) onContactIdChange(null)
        },
        label = { Text(nameLabel) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    val phoneRaw = phone
    val phoneError = phoneRaw.isNotBlank() && !isValidIranianMobile(phoneRaw)
    OutlinedTextField(
        value = phoneRaw,
        onValueChange = {
            onPhoneChange(normalizeDigits(it).filter { c -> c.isDigit() }.take(11))
            if (contactId != null) onContactIdChange(null)
        },
        label = { Text(phoneLabel) },
        singleLine = true,
        isError = phoneError,
        supportingText = if (phoneError) {
            { Text("شماره موبایل باید ۱۱ رقم و با ۰۹ شروع شود") }
        } else null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
    )

    if (contactId != null) {
        Spacer(modifier = Modifier.height(6.dp))
        AssistChip(onClick = { onContactIdChange(null) }, label = { Text("متصل به مخاطب ثبت‌شده · تغییر") })
    } else if (results.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("مخاطبین مشابه", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Column {
            results.take(5).forEach { (contact, caseCount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNameChange(contact.fullName)
                            onPhoneChange(contact.primaryPhone)
                            onContactIdChange(contact.id)
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(contact.fullName, style = MaterialTheme.typography.bodyMedium)
                        Text(contact.primaryPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (caseCount > 0) {
                        Text(
                            "$caseCount پرونده دیگر",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/** A single-value text field with a tappable suggestion-chip row beneath it, sourced from every
 *  district ever typed on either side of the form (see [PropertyViewModel.availableDistricts]) —
 *  the app's one shared district vocabulary instead of each case retyping it from scratch. */
@Composable
private fun DistrictField(value: String, onValueChange: (String) -> Unit, suggestions: List<String>) {
    val matches = remember(value, suggestions) {
        if (value.isBlank()) suggestions.take(6)
        else suggestions.filter { it.contains(value, ignoreCase = true) && it != value }.take(6)
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("منطقه/محله") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    if (matches.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            matches.forEach { suggestion ->
                AssistChip(onClick = { onValueChange(suggestion) }, label = { Text(suggestion) })
            }
        }
    }
}

@Composable
private fun TagEditor(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    label: String = "افزودن برچسب",
    suggestions: List<String> = emptyList()
) {
    var input by remember { mutableStateOf("") }
    if (tags.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { onTagsChange(tags - tag) },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "حذف", modifier = Modifier.height(16.dp)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth()
    )
    if (input.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        // An AssistChip instead of a bare clickable Text — its built-in Material minimum touch
        // target keeps this tappable at a real finger size, unlike text-glyph-only bounds.
        AssistChip(
            onClick = {
                onTagsChange(tags + input.trim())
                input = ""
            },
            label = { Text("افزودن «${input.trim()}»") },
            leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.height(16.dp)) }
        )
    }
    val matchingSuggestions = remember(input, suggestions, tags) {
        suggestions.filter { it !in tags && (input.isBlank() || it.contains(input, ignoreCase = true)) }.take(6)
    }
    if (matchingSuggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            matchingSuggestions.forEach { suggestion ->
                AssistChip(onClick = { onTagsChange(tags + suggestion); input = "" }, label = { Text(suggestion) })
            }
        }
    }
}
