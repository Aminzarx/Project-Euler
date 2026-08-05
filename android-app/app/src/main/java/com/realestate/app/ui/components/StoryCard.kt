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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.realestate.app.data.Property
import com.realestate.app.ui.theme.heroGradient

enum class StoryTemplate(val label: String) {
    GRADIENT("گرادیانت"),
    MINIMAL("مینیمال"),
    BOLD_PRICE("قیمت‌محور")
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
                    Icon(Icons.Outlined.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
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
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    formatPrice(property.price, property.dealType),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color.White)
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color.White)
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "کد ملک", value = "#${property.id}", color = Color.White)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    agentPhone.ifBlank { "—" },
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFFFAFAFA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
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
                    Icon(Icons.Outlined.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color(0xFF16181D), style = MaterialTheme.typography.titleSmall)
            }

            Column {
                Text(
                    property.propertyType.label(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(property.title, color = Color(0xFF16181D), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    property.city,
                    color = Color(0xFF77797F),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    formatPrice(property.price, property.dealType),
                    color = Color(0xFF16181D),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    StoryStat(label = "متراژ", value = "${property.area.toInt()} متر", color = Color(0xFF16181D))
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "اتاق", value = property.rooms.toString(), color = Color(0xFF16181D))
                    Spacer(modifier = Modifier.width(24.dp))
                    StoryStat(label = "کد ملک", value = "#${property.id}", color = Color(0xFF16181D))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    agentPhone.ifBlank { "—" },
                    color = Color(0xFF16181D),
                    style = MaterialTheme.typography.bodyMedium
                )
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(MaterialTheme.shapes.large)
            .background(Color(0xFF0B0C0F))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(agencyName.ifBlank { "مدیریت املاک" }, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleSmall)

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    formatPrice(property.price, property.dealType),
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
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
                    StoryStat(label = "کد ملک", value = "#${property.id}", color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(agentPhone.ifBlank { "—" }, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
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
