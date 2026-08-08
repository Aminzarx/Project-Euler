package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.AppTextButton
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: ProfileViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var agencyName by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var agencyLogoUri by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var showNameError by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Compared against the loaded profile rather than tracked per-field-edit — simpler, and
    // correct even for a field the agent edits then reverts back to its original value.
    val isDirty = loaded && (
        fullName != profile.fullName ||
            agencyName != profile.agencyName ||
            businessAddress != profile.businessAddress ||
            biography != profile.biography ||
            slogan != profile.slogan ||
            instagram != profile.instagram ||
            telegram != profile.telegram ||
            website != profile.website ||
            profilePhotoUri != profile.profilePhotoUri ||
            agencyLogoUri != profile.agencyLogoUri
        )

    fun handleBack() {
        if (isDirty) showDiscardConfirm = true else onDone()
    }

    BackHandler(onBack = ::handleBack)

    LaunchedEffect(profile) {
        if (!loaded) {
            fullName = profile.fullName
            agencyName = profile.agencyName
            businessAddress = profile.businessAddress
            biography = profile.biography
            slogan = profile.slogan
            instagram = profile.instagram
            telegram = profile.telegram
            website = profile.website
            profilePhotoUri = profile.profilePhotoUri
            agencyLogoUri = profile.agencyLogoUri
            loaded = true
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val persisted = runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.isSuccess
            profilePhotoUri = uri.toString()
            if (!persisted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("این عکس ممکن است بعد از بستن برنامه دوباره نیاز به انتخاب داشته باشد")
                }
            }
        }
    }

    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val persisted = runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.isSuccess
            agencyLogoUri = uri.toString()
            if (!persisted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("این لوگو ممکن است بعد از بستن برنامه دوباره نیاز به انتخاب داشته باشد")
                }
            }
        }
    }

    val nextAction = KeyboardOptions(imeAction = ImeAction.Next)
    val nextActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    fun attemptSave() {
        if (fullName.isBlank()) {
            showNameError = true
            return
        }
        viewModel.updateProfile {
            it.copy(
                fullName = fullName.trim(),
                agencyName = agencyName.trim(),
                businessAddress = businessAddress.trim(),
                biography = biography.trim(),
                slogan = slogan.trim(),
                instagram = instagram.trim(),
                telegram = telegram.trim(),
                website = website.trim(),
                profilePhotoUri = profilePhotoUri,
                agencyLogoUri = agencyLogoUri
            )
        }
        // The confirmation itself is shown by RealEstateApp's shell (see ProfileViewModel.saveEvents)
        // once this screen has already popped off the back stack, not here.
        onDone()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ویرایش پروفایل") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    AppTextButton(text = "ذخیره", onClick = ::attemptSave)
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
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { photoPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Rounded.AddAPhoto, contentDescription = "افزودن عکس پروفایل")
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionLabel("اطلاعات شخصی")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; showNameError = false },
                    label = { Text("نام و نام‌خانوادگی *") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    isError = showNameError,
                    supportingText = if (showNameError) {
                        { Text("این فیلد الزامی است") }
                    } else null,
                    singleLine = true,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionLabel("اطلاعات آژانس")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text("لوگوی آژانس", style = MaterialTheme.typography.labelLarge)
                Text(
                    "روی کارت‌های استوری به‌جای آیکن پیش‌فرض نمایش داده می‌شود",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { logoPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (agencyLogoUri != null) {
                        AsyncImage(
                            model = agencyLogoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = "افزودن لوگوی آژانس")
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = agencyName,
                    onValueChange = { agencyName = it },
                    label = { Text("نام آژانس/بنگاه") },
                    leadingIcon = { Icon(Icons.Rounded.Storefront, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = slogan,
                    onValueChange = { slogan = it },
                    label = { Text("شعار کسب‌وکار") },
                    leadingIcon = { Icon(Icons.Rounded.FormatQuote, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = businessAddress,
                    onValueChange = { businessAddress = it },
                    label = { Text("آدرس محل کسب‌وکار") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = biography,
                    onValueChange = { biography = it },
                    label = { Text("درباره من") },
                    leadingIcon = { Icon(Icons.Rounded.Notes, contentDescription = null) },
                    minLines = 3,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SectionLabel("شبکه‌های اجتماعی و وب‌سایت")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = { Text("آیدی اینستاگرام") },
                    leadingIcon = { Icon(Icons.Rounded.Send, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = telegram,
                    onValueChange = { telegram = it },
                    label = { Text("آیدی تلگرام") },
                    leadingIcon = { Icon(Icons.Rounded.Send, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = nextAction,
                    keyboardActions = nextActions,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("وب‌سایت") },
                    leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); attemptSave() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }

    if (showDiscardConfirm) {
        ConfirmationDialog(
            title = "تغییرات ذخیره‌نشده",
            text = "اگر خارج شوید، تغییراتی که در این صفحه دادید از بین می‌رود.",
            confirmLabel = "خروج بدون ذخیره",
            danger = true,
            onConfirm = onDone,
            onDismiss = { showDiscardConfirm = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(Spacing.sm))
}
