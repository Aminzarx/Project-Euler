package com.realestate.app.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.realestate.app.data.Property
import com.realestate.app.ui.theme.heroGradient

enum class StoryTemplate(val label: String) {
    GRADIENT("گرادیانت"),
    MINIMAL("مینیمال"),
    BOLD_PRICE("قیمت‌محور"),
    EDITORIAL("ویژه")
}

/** Output aspect ratio the card is rendered at. New formats only need a new entry here. */
enum class StoryAspectRatio(val ratio: Float, val label: String) {
    STORY(9f / 16f, "استوری ۹:۱۶"),
    SQUARE(1f, "مربعی ۱:۱"),
    PORTRAIT(4f / 5f, "پرتره ۴:۵")
}

/** Optional configurable marketing badge shown as a ribbon on the card. Colors reuse the app's brand palette. */
enum class StoryBadge(val label: String, val color: Color) {
    HOT_PROPERTY("پیشنهاد داغ", Color(0xFF0B5D8C)),
    URGENT_SALE("فروش فوری", Color(0xFF1D3F6E)),
    INVESTMENT_OPPORTUNITY("فرصت سرمایه‌گذاری", Color(0xFF1F7A8C)),
    BELOW_MARKET("زیر قیمت بازار", Color(0xFF053C5E)),
    EXCLUSIVE_LISTING("فایل اختصاصی", Color(0xFF6B4EFF)),
    NEWLY_LISTED("تازه ثبت‌شده", Color(0xFF1F9D55)),
    LUXURY("لوکس", Color(0xFF9A7B2F)),
    SPECIAL_OFFER("پیشنهاد ویژه", Color(0xFFE8630A))
}

/**
 * Which optional fields and marketing elements appear on a rendered card. Everything here is
 * opt-in/opt-out per share so an agent can tailor what a specific card reveals, without the
 * templates themselves needing to change.
 */
data class StoryCardConfig(
    val aspectRatio: StoryAspectRatio = StoryAspectRatio.STORY,
    val showPrice: Boolean = true,
    val showArea: Boolean = true,
    val showRooms: Boolean = true,
    val showContact: Boolean = true,
    val showCta: Boolean = true,
    val showQrCode: Boolean = false,
    val badge: StoryBadge? = null,
    val agencyLogoUri: String? = null
)

@Composable
fun StoryCardContent(
    property: Property,
    agentPhone: String,
    agencyName: String,
    modifier: Modifier = Modifier,
    template: StoryTemplate = StoryTemplate.GRADIENT,
    config: StoryCardConfig = StoryCardConfig()
) {
    when (template) {
        StoryTemplate.GRADIENT -> GradientStoryCard(property, agentPhone, agencyName, config, modifier)
        StoryTemplate.MINIMAL -> MinimalStoryCard(property, agentPhone, agencyName, config, modifier)
        StoryTemplate.BOLD_PRICE -> BoldPriceStoryCard(property, agentPhone, agencyName, config, modifier)
        StoryTemplate.EDITORIAL -> EditorialStoryCard(property, agentPhone, agencyName, config, modifier)
    }
}

@Composable
private fun GradientStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    config: StoryCardConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(config.aspectRatio.ratio)
            .clip(MaterialTheme.shapes.large)
            .background(heroGradient())
    ) {
        CodeBadge(
            property.id,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        )
        config.badge?.let {
            BadgeRibbon(it, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        }
        if (config.showQrCode) {
            StoryQrCode(agentPhone, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgencyMark(
                    config.agencyLogoUri,
                    tintColor = Color.White,
                    backgroundColor = Color.White.copy(alpha = 0.22f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White, style = MaterialTheme.typography.titleSmall)
            }

            Column {
                Surface(color = Color.White.copy(alpha = 0.18f), shape = MaterialTheme.shapes.small) {
                    Text(
                        property.propertyType.label(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(property.title, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    property.city,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (config.showPrice) {
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        "قیمت",
                        color = Color.White.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        formatPrice(property.price, property.dealType),
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                if (config.showArea || config.showRooms) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        if (config.showArea) StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color.White)
                        if (config.showArea && config.showRooms) Spacer(modifier = Modifier.width(24.dp))
                        if (config.showRooms) StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color.White)
                    }
                }
            }

            if (config.showCta) {
                CtaLine(color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (config.showContact) {
                AgentPhonePill(agentPhone, containerColor = Color.White.copy(alpha = 0.16f), contentColor = Color.White)
            }
        }
    }
}

/** Clean white-card template: dark ink text, a single brand accent bar, generous whitespace. */
@Composable
private fun MinimalStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    config: StoryCardConfig,
    modifier: Modifier = Modifier
) {
    val ink = Color(0xFF16181D)
    Box(
        modifier = modifier
            .aspectRatio(config.aspectRatio.ratio)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFFAFAFA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        CodeBadge(
            property.id,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 26.dp, end = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            contentColor = MaterialTheme.colorScheme.primary
        )
        config.badge?.let {
            BadgeRibbon(it, modifier = Modifier.align(Alignment.TopStart).padding(top = 26.dp, start = 20.dp))
        }
        if (config.showQrCode) {
            StoryQrCode(agentPhone, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgencyMark(
                    config.agencyLogoUri,
                    tintColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(agencyName.ifBlank { "مدیریت املاک" }, color = ink, style = MaterialTheme.typography.titleSmall)
            }

            Column {
                Text(
                    property.propertyType.label(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(property.title, color = ink, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    property.city,
                    color = Color(0xFF77797F),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (config.showPrice) {
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        "قیمت",
                        color = Color(0xFF9A9CA5),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        formatPrice(property.price, property.dealType),
                        color = ink,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
                if (config.showArea || config.showRooms) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        if (config.showArea) StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = ink)
                        if (config.showArea && config.showRooms) Spacer(modifier = Modifier.width(24.dp))
                        if (config.showRooms) StoryStat(label = "اتاق", value = property.rooms.toString(), color = ink)
                    }
                }
            }

            if (config.showCta) {
                CtaLine(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (config.showContact) {
                AgentPhonePill(agentPhone, containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), contentColor = ink)
            }
        }
    }
}

/** Price-dominant template: the number is the hero, everything else is secondary. */
@Composable
private fun BoldPriceStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    config: StoryCardConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(config.aspectRatio.ratio)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFF0B0C0F))
    ) {
        CodeBadge(
            property.id,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White.copy(alpha = 0.8f)
        )
        config.badge?.let {
            BadgeRibbon(it, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        }
        if (config.showQrCode) {
            StoryQrCode(agentPhone, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AgencyMark(
                    config.agencyLogoUri,
                    tintColor = Color.White.copy(alpha = 0.85f),
                    backgroundColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleSmall)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (config.showPrice) {
                    Text(
                        "قیمت",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatPrice(property.price, property.dealType),
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                Text(property.title, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${property.propertyType.label()} · ${property.city}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column {
                if (config.showArea || config.showRooms) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (config.showArea) StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color.White)
                        if (config.showRooms) StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (config.showCta) {
                    CtaLine(color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (config.showContact) {
                    AgentPhonePill(agentPhone, containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
                }
            }
        }
    }
}

/** Asymmetric editorial layout: a diagonally-cut brand-color band up top, oversized price as the focal point below. */
@Composable
private fun EditorialStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    config: StoryCardConfig,
    modifier: Modifier = Modifier
) {
    val diagonalShape = remember(property.id) {
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height * 0.30f)
            lineTo(0f, size.height * 0.42f)
            close()
        }
    }
    Box(
        modifier = modifier
            .aspectRatio(config.aspectRatio.ratio)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFFAFAFA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(diagonalShape)
                .background(heroGradient())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AgencyMark(
                        config.agencyLogoUri,
                        tintColor = Color.White,
                        backgroundColor = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                CodeBadge(property.id, containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
            }
        }
        config.badge?.let {
            BadgeRibbon(it, modifier = Modifier.align(Alignment.TopStart).padding(top = 90.dp, start = 20.dp))
        }
        if (config.showQrCode) {
            StoryQrCode(agentPhone, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            Column {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), shape = MaterialTheme.shapes.small) {
                    Text(
                        property.propertyType.label(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(property.title, color = Color(0xFF16181D), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(property.city, color = Color(0xFF77797F), style = MaterialTheme.typography.bodyMedium)
                if (config.showPrice) {
                    Spacer(modifier = Modifier.height(26.dp))
                    Text(
                        formatPrice(property.price, property.dealType),
                        color = Color(0xFF16181D),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (config.showArea || config.showRooms) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row {
                        if (config.showArea) StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color(0xFF16181D))
                        if (config.showArea && config.showRooms) Spacer(modifier = Modifier.width(24.dp))
                        if (config.showRooms) StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color(0xFF16181D))
                    }
                }
            }

            if (config.showCta) {
                CtaLine(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (config.showContact) {
                AgentPhonePill(
                    agentPhone,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    contentColor = Color(0xFF16181D)
                )
            }
        }
    }
}

/** Small elegant badge for the property code — never the exact address, just a shareable reference number. */
@Composable
private fun CodeBadge(propertyId: Long, modifier: Modifier = Modifier, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
        Text(
            "کد ملک · ${propertyId}",
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Configurable marketing ribbon (Hot Property, Urgent Sale, ...) — always white text on the badge's own brand color, independent of template background. */
@Composable
private fun BadgeRibbon(badge: StoryBadge, modifier: Modifier = Modifier) {
    Surface(color = badge.color, shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
        Text(
            badge.label,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Short call-to-action microcopy placed just above the phone pill. */
@Composable
private fun CtaLine(color: Color) {
    Text(
        "☎ همین حالا تماس بگیرید",
        color = color,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

/** Agency logo when the agent has set one, falling back to a generic house glyph otherwise. */
@Composable
private fun AgencyMark(
    agencyLogoUri: String?,
    tintColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (agencyLogoUri != null) {
            AsyncImage(
                model = agencyLogoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.small)
            )
        } else {
            Icon(Icons.Rounded.Home, contentDescription = null, tint = tintColor, modifier = Modifier.size(20.dp))
        }
    }
}

/** QR code encoding a direct call to the agent — the only destination this offline app can honestly promise stays valid. */
@Composable
private fun StoryQrCode(agentPhone: String, modifier: Modifier = Modifier) {
    if (agentPhone.isBlank()) return
    val bitmap = remember(agentPhone) { generateQrBitmap("tel:$agentPhone", 240) }
    if (bitmap != null) {
        Surface(color = Color.White, shape = MaterialTheme.shapes.extraSmall, modifier = modifier) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "کد QR تماس با مشاور",
                modifier = Modifier.size(56.dp).padding(4.dp)
            )
        }
    }
}

/**
 * Fills an [IntArray] and hands it to [Bitmap.createBitmap] in one shot rather than calling
 * `setPixel` per pixel. At 240×240 the old form made 57,600 separate JNI calls — each one bounds
 * checked and each one crossing into native code — on the main thread inside composition, which is
 * long enough to be a visible stall when the story card first appears.
 */
private fun generateQrBitmap(content: String, sizePx: Int): Bitmap? = runCatching {
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    val pixels = IntArray(sizePx * sizePx)
    for (y in 0 until sizePx) {
        val rowOffset = y * sizePx
        for (x in 0 until sizePx) {
            pixels[rowOffset + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
        }
    }
    Bitmap.createBitmap(pixels, sizePx, sizePx, Bitmap.Config.RGB_565)
}.getOrNull()

/** Professionally placed agent phone number — a pill, never a bare line of text. */
@Composable
private fun AgentPhonePill(agentPhone: String, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, shape = MaterialTheme.shapes.small) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Call, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                agentPhone.ifBlank { "—" },
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StoryStat(label: String, value: String, color: Color) {
    Column {
        Text(value, color = color, style = MaterialTheme.typography.titleMedium)
        Text(label, color = color.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
    }
}
