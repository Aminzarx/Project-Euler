package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.realestate.app.ui.SUPPORT_EMAIL
import com.realestate.app.ui.SUPPORT_TELEGRAM_URL
import com.realestate.app.ui.SUPPORT_WEBSITE_URL
import com.realestate.app.ui.SUPPORT_WHATSAPP_URL
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.theme.Spacing

private data class FaqItem(val question: String, val answer: String)

private val FAQ_ITEMS = listOf(
    FaqItem(
        "آیا اطلاعات ملک‌ها و مشتریانم جایی آنلاین ذخیره می‌شود؟",
        "خیر. برنامه کاملاً آفلاین کار می‌کند و همه اطلاعات فقط روی همین گوشی ذخیره می‌شوند. " +
            "تنها راه خروج داده از گوشی، فایل پشتیبانی است که خودتان از بخش تنظیمات می‌سازید."
    ),
    FaqItem(
        "چطور یک ملک جدید ثبت کنم؟",
        "از تب اصلی روی دکمه «افزودن ملک» بزنید. فیلدهای قیمت و متراژ اگر خالی بمانند صفر ذخیره " +
            "می‌شوند، پس می‌توانید سریع ثبت کنید و بعداً تکمیلش کنید."
    ),
    FaqItem(
        "پشتیبان‌گیری و بازیابی چطور کار می‌کند؟",
        "پشتیبان‌گیری یک فایل JSON از همه ملک‌ها، یادداشت‌ها و تراکنش‌های کیف پول می‌سازد. " +
            "بازیابی از یک فایل پشتیبان، تمام اطلاعات فعلی گوشی را با محتوای آن فایل جایگزین می‌کند — " +
            "قبل از انجام آن، همیشه یک پیش‌نمایش از تعداد رکوردهای داخل فایل نشان داده می‌شود."
    ),
    FaqItem(
        "چرا شماره تماس داخل کارت استوری یا پیام اشتراک‌گذاری، شماره خودم است نه مالک ملک؟",
        "این یک انتخاب طراحی عمدی است: اگر شماره مالک مستقیم به مشتری داده شود، مشتری می‌تواند " +
            "بدون واسطه با مالک تماس بگیرد و شما را از معامله حذف کند."
    ),
    FaqItem(
        "اگر گوشی‌ام را عوض کنم اطلاعاتم از بین می‌رود؟",
        "اگر قبل از تعویض گوشی از بخش تنظیمات پشتیبان‌گیری کرده باشید، می‌توانید همان فایل را در " +
            "گوشی جدید بازیابی کنید. بدون پشتیبان‌گیری، چون اطلاعات فقط روی خود گوشی است، از بین می‌رود."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("راهنما و پشتیبانی") },
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
            Text("تماس با پشتیبانی", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            AppListRow(
                icon = Icons.Rounded.Send,
                title = "تلگرام",
                subtitle = "پاسخ‌گویی در ساعات اداری",
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_TELEGRAM_URL))) }
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppListRow(
                icon = Icons.Rounded.Send,
                title = "واتساپ",
                subtitle = "پاسخ‌گویی در ساعات اداری",
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_WHATSAPP_URL))) }
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppListRow(
                icon = Icons.Rounded.Email,
                title = "ایمیل",
                subtitle = SUPPORT_EMAIL,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$SUPPORT_EMAIL")
                    }
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            AppListRow(
                icon = Icons.Rounded.Language,
                title = "وب‌سایت راهنما",
                subtitle = SUPPORT_WEBSITE_URL,
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_WEBSITE_URL))) }
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
            Text("سوالات متداول", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Spacing.md))
            FAQ_ITEMS.forEach { item -> FaqRow(item) }
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun FaqRow(item: FaqItem) {
    var expanded by remember { mutableStateOf(false) }
    AppCard(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.clickable { expanded = !expanded }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Rounded.QuestionAnswer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        item.question,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "بستن" else "باز کردن"
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
}
