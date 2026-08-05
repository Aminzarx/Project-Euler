package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: ProfileViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var fullName by remember { mutableStateOf("") }
    var agencyName by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    var instagram by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!loaded) {
            fullName = profile.fullName
            agencyName = profile.agencyName
            businessAddress = profile.businessAddress
            biography = profile.biography
            instagram = profile.instagram
            telegram = profile.telegram
            profilePhotoUri = profile.profilePhotoUri
            loaded = true
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // برخی منابع اجازه دسترسی دائمی نمی‌دهند
            }
            profilePhotoUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش پروفایل") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.updateProfile {
                            it.copy(
                                fullName = fullName.trim(),
                                agencyName = agencyName.trim(),
                                businessAddress = businessAddress.trim(),
                                biography = biography.trim(),
                                instagram = instagram.trim(),
                                telegram = telegram.trim(),
                                profilePhotoUri = profilePhotoUri
                            )
                        }
                        onDone()
                    }) {
                        Text("ذخیره")
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

            Spacer12()
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("نام و نام‌خانوادگی") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
            OutlinedTextField(
                value = agencyName,
                onValueChange = { agencyName = it },
                label = { Text("نام آژانس/بنگاه") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
            OutlinedTextField(
                value = businessAddress,
                onValueChange = { businessAddress = it },
                label = { Text("آدرس محل کسب‌وکار") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
            OutlinedTextField(
                value = biography,
                onValueChange = { biography = it },
                label = { Text("درباره من") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
            OutlinedTextField(
                value = instagram,
                onValueChange = { instagram = it },
                label = { Text("آیدی اینستاگرام") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
            OutlinedTextField(
                value = telegram,
                onValueChange = { telegram = it },
                label = { Text("آیدی تلگرام") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer12()
        }
    }
}

@Composable
private fun Spacer12() {
    Spacer(modifier = Modifier.height(12.dp))
}
