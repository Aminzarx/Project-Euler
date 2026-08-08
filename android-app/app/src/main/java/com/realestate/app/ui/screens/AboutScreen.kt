package com.realestate.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.realestate.app.BuildConfig
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("درباره برنامه") },
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
            Icon(
                Icons.Rounded.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(48.dp)
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text("مدیریت املاک", style = MaterialTheme.typography.headlineSmall)
            Text(
                "نسخه ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("حریم خصوصی و داده‌ها", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "این برنامه به‌صورت کاملاً آفلاین کار می‌کند. اطلاعات ملک‌ها، مشتریان، " +
                        "یادداشت‌ها و تراکنش‌های کیف پول فقط روی همین گوشی ذخیره می‌شوند و به هیچ " +
                        "سروری ارسال نمی‌شوند. فایل پشتیبان که از بخش تنظیمات می‌سازید هم یک فایل " +
                        "معمولی JSON است که خودتان مالک و مسئول نگهداری آن هستید.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}
