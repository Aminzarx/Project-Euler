package com.realestate.app.ui.components

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realestate.app.data.Property
import com.realestate.app.ui.theme.heroGradient

enum class StoryTemplate(val label: String) {
    GRADIENT("گرادیانت"),
    MINIMAL("مینیمال"),
    BOLD_PRICE("قیمت‌محور"),
    EDITORIAL("ویژه")
}

@Composable
fun StoryCardContent(
    property: Property,
    agentPhone: String,
    agencyName: String,
    modifier: Modifier = Modifier,
    template: StoryTemplate = StoryTemplate.GRADIENT
) {
    when (template) {
        StoryTemplate.GRADIENT -> GradientStoryCard(property, agentPhone, agencyName, modifier)
        StoryTemplate.MINIMAL -> MinimalStoryCard(property, agentPhone, agencyName, modifier)
        StoryTemplate.BOLD_PRICE -> BoldPriceStoryCard(property, agentPhone, agencyName, modifier)
        StoryTemplate.EDITORIAL -> EditorialStoryCard(property, agentPhone, agencyName, modifier)
    }
}

@Composable
private fun GradientStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(MaterialTheme.shapes.large)
            .background(heroGradient())
    ) {
        CodeBadge(
            property.id,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            containerColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
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
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color.White)
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color.White)
                }
            }

            AgentPhonePill(agentPhone, containerColor = Color.White.copy(alpha = 0.16f), contentColor = Color.White)
        }
    }
}

/** Clean white-card template: dark ink text, a single brand accent bar, generous whitespace. */
@Composable
private fun MinimalStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    modifier: Modifier = Modifier
) {
    val ink = Color(0xFF16181D)
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
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
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
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
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = ink)
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = ink)
                }
            }

            AgentPhonePill(agentPhone, containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), contentColor = ink)
        }
    }
}

/** Price-dominant template: the number is the hero, everything else is secondary. */
@Composable
private fun BoldPriceStoryCard(
    property: Property,
    agentPhone: String,
    agencyName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFF0B0C0F))
    ) {
        CodeBadge(
            property.id,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White.copy(alpha = 0.8f)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleSmall)

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                Text(property.title, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${property.propertyType.label()} · ${property.city}",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color.White)
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                AgentPhonePill(agentPhone, containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White)
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
            .aspectRatio(9f / 16f)
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
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
                CodeBadge(property.id, containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
            }
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
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    formatPrice(property.price, property.dealType),
                    color = Color(0xFF16181D),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color(0xFF16181D))
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color(0xFF16181D))
                }
            }

            AgentPhonePill(
                agentPhone,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                contentColor = Color(0xFF16181D)
            )
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
