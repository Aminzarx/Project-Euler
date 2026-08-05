package com.realestate.app.ui.screens.dealassistant

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.PrimaryButton
import com.realestate.app.ui.components.formatPrice
import com.realestate.app.ui.components.label
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.viewmodel.PropertyViewModel

private enum class AdTextStyle(val label: String) {
    PROFESSIONAL("رسمی"),
    SHORT("کوتاه و جذاب"),
    BULLET("فهرست‌وار")
}

private fun generateAdText(property: Property, style: AdTextStyle): String {
    val dealWord = if (property.dealType == DealType.RENT) "اجاره" else "فروش"
    val price = formatPrice(property.price, property.dealType)
    return when (style) {
        AdTextStyle.PROFESSIONAL ->
            "${dealWord} ${property.propertyType.label()} در ${property.city}\n\n" +
                "${property.title}\n" +
                "متراژ: ${property.area.toInt()} متر · ${property.rooms} خوابه\n" +
                "قیمت: $price\n\n" +
                (if (property.description.isNotBlank()) property.description + "\n\n" else "") +
                "برای اطلاعات بیشتر و بازدید با ما تماس بگیرید."

        AdTextStyle.SHORT ->
            "🏠 ${dealWord} ${property.propertyType.label()} در ${property.city}\n" +
                "${property.area.toInt()} متر · ${property.rooms} خواب · $price\n" +
                "برای بازدید تماس بگیرید."

        AdTextStyle.BULLET ->
            "$dealWord ${property.propertyType.label()} — ${property.title}\n\n" +
                "• شهر: ${property.city}\n" +
                "• متراژ: ${property.area.toInt()} متر\n" +
                "• تعداد اتاق: ${property.rooms}\n" +
                "• قیمت: $price\n" +
                (if (property.tags.isNotEmpty()) "• امکانات: ${property.tags.joinToString("، ")}\n" else "") +
                "\nبرای هماهنگی بازدید در ارتباط باشید."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdTextGeneratorScreen(propertyId: Long, viewModel: PropertyViewModel, onBack: () -> Unit) {
    val property by viewModel.getPropertyById(propertyId).collectAsStateWithLifecycle(initialValue = null)
    var style by remember { mutableStateOf(AdTextStyle.PROFESSIONAL) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("متن آگهی") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        val current = property
        if (current == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            val text = generateAdText(current, style)
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(Spacing.screen)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdTextStyle.entries.forEach { option ->
                        FilterChip(selected = style == option, onClick = { style = option }, label = { Text(option.label) })
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "کپی متن",
                        onClick = { clipboard.setText(AnnotatedString(text)) },
                        icon = Icons.Rounded.ContentCopy,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "اشتراک‌گذاری",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری متن آگهی"))
                        },
                        icon = Icons.Rounded.Share,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
