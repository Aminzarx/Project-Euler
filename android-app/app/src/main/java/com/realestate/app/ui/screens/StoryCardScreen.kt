package com.realestate.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.Property
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.StoryCardContent
import com.realestate.app.ui.components.StoryTemplate
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

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
    var showCaptureDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf(StoryTemplate.GRADIENT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("کارت استوری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        val current = property
        if (current == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ملک یافت نشد")
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(Spacing.screen),
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
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    text = "اشتراک‌گذاری کارت",
                    onClick = { showCaptureDialog = true },
                    icon = Icons.Outlined.Share,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showCaptureDialog) {
                StoryCardCaptureDialog(
                    property = current,
                    agentPhone = agentPhone,
                    agencyName = agencyName,
                    template = selectedTemplate,
                    onDismiss = { showCaptureDialog = false }
                )
            }
        }
    }
}

@Composable
private fun StoryCardCaptureDialog(
    property: Property,
    agentPhone: String,
    agencyName: String,
    template: StoryTemplate,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        val view = LocalView.current
        StoryCardContent(
            property = property,
            agentPhone = agentPhone,
            agencyName = agencyName,
            template = template,
            modifier = Modifier.width(360.dp)
        )

        LaunchedEffect(Unit) {
            delay(300)
            runCatching {
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                val file = saveBitmapToCache(context, bitmap)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری کارت ملک"))
            }
            onDismiss()
        }
    }
}

private fun saveBitmapToCache(context: Context, bitmap: Bitmap): File {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "story_card_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}
