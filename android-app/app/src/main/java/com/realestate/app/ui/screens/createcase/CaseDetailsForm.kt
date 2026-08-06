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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.realestate.app.data.MortgageStatus
import com.realestate.app.data.PropertyFormShape
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.PropertyType
import com.realestate.app.data.RequestValidityType
import com.realestate.app.data.formShape
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.CollapsibleSection
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import com.realestate.app.ui.components.MoneyField
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.RequiredFieldLabel
import com.realestate.app.ui.components.color
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.components.label
import com.realestate.app.ui.components.normalizeDigits
import com.realestate.app.ui.theme.Spacing

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
    onEditCaseType: () -> Unit,
    onEditTransactionType: () -> Unit,
    onEditPropertyType: () -> Unit,
    onPickImage: () -> Unit,
    onSave: () -> Unit
) {
    val isFormValid = when (caseType) {
        CaseType.OWNER -> state.address.isNotBlank() || state.title.isNotBlank()
        CaseType.CLIENT_REQUEST -> state.contactName.isNotBlank() || state.contactPhone.isNotBlank()
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
            OwnerSections(state, propertyType, transactionType)
        } else {
            ClientRequestSections(state, transactionType)
        }

        Spacer(modifier = Modifier.height(Spacing.cardGap))
        Text("برچسب‌ها", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Spacing.sm))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            TagEditor(tags = state.tags, onTagsChange = { state.tags = it })
        }

        Spacer(modifier = Modifier.height(Spacing.xl))
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
private fun OwnerSections(state: CaseFormState, propertyType: PropertyType, transactionType: CaseTransactionType?) {
    Text("اطلاعات مالک", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        LabeledField(state::contactName, "نام مالک")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::contactPhone, "شماره تماس مالک", keyboardType = KeyboardType.Phone)
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("اطلاعات ملک", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.title,
            onValueChange = { state.title = it },
            label = { RequiredFieldLabel("عنوان پرونده") },
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
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::city, "شهر")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::address, "آدرس")
        Spacer(modifier = Modifier.height(8.dp))
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
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        IntegerField(state::constructionAge, "سن بنا (سال)")
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("قیمت", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        MoneyField(
            label = if (transactionType in rentLikeTransactions) "اجاره ماهانه (تومان)" else "قیمت کل (تومان)",
            value = state.price,
            onValueChange = { state.price = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("وضعیت رهن", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MortgageStatus.entries.forEach { option ->
                FilterChip(selected = state.mortgageStatus == option, onClick = { state.mortgageStatus = option }, label = { Text(option.label()) })
            }
        }
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("وضعیت پرونده", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        StatusDropdown(status = state.status, options = ownerStatuses, onSelect = { state.status = it })
        Spacer(modifier = Modifier.height(8.dp))
        PriorityRow(state.priority) { state.priority = it }
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    CollapsibleSection(title = "بازدید و همکاری", subtitle = "ساعت بازدید، کلیدار و شرایط همکاری") {
        LabeledField(state::viewingHours, "ساعات بازدید")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::keyHolder, "کلیدار")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::paymentConditions, "شرایط پرداخت")
        Spacer(modifier = Modifier.height(10.dp))
        FlagChipGroup(
            flags = listOf(CaseFlag.VACANT, CaseFlag.NEGOTIABLE, CaseFlag.IMMEDIATE_SALE, CaseFlag.EXCHANGE_ACCEPTED),
            selected = state.ownerFlags,
            onToggle = { flag, isOn -> state.ownerFlags = if (isOn) state.ownerFlags + flag else state.ownerFlags - flag }
        )
    }

    Spacer(modifier = Modifier.height(Spacing.sm))
    CollapsibleSection(title = "اطلاعات حقوقی", subtitle = "وضعیت سند و مسائل قانونی") {
        TriStateRow("سند آماده است", state.titleDeedReady) { state.titleDeedReady = it }
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::legalStatus, "وضعیت حقوقی")
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
private fun ClientRequestSections(state: CaseFormState, transactionType: CaseTransactionType?) {
    Text("اطلاعات مشتری", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.contactName,
            onValueChange = { state.contactName = it },
            label = { RequiredFieldLabel("نام مشتری") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::contactPhone, "شماره تماس", keyboardType = KeyboardType.Phone)
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
        FlagChipGroup(
            flags = listOf(CaseFlag.LOAN_REQUIRED),
            selected = state.requirementFlags,
            onToggle = { flag, isOn -> state.requirementFlags = if (isOn) state.requirementFlags + flag else state.requirementFlags - flag }
        )
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
        IntegerField(state::desiredBedrooms, "تعداد اتاق مورد نیاز")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::floorPreference, "طبقه ترجیحی")
        Spacer(modifier = Modifier.height(8.dp))
        LabeledField(state::viewPreference, "منظره ترجیحی")
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
            label = "افزودن محله/منطقه"
        )
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    CollapsibleSection(title = "ویژگی‌های مورد نیاز", subtitle = "امکاناتی که مشتری به آن‌ها نیاز دارد", initiallyExpanded = true) {
        FlagChipGroup(
            flags = CaseFormState.REQUIREMENT_FLAGS.filter { it != CaseFlag.LOAN_REQUIRED },
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
    }

    Spacer(modifier = Modifier.height(Spacing.cardGap))
    Text("وضعیت پرونده", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(Spacing.sm))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        StatusDropdown(status = state.status, options = clientRequestStatuses, onSelect = { state.status = it })
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

@Composable
private fun TagEditor(tags: List<String>, onTagsChange: (List<String>) -> Unit, label: String = "افزودن برچسب") {
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
        Text(
            "افزودن «${input.trim()}»",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable {
                onTagsChange(tags + input.trim())
                input = ""
            }
        )
    }
}
