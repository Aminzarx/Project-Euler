package com.realestate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.realestate.app.data.CaseType
import com.realestate.app.data.DealType
import com.realestate.app.data.Property
import com.realestate.app.ui.theme.extendedColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PropertyCard(
    property: Property,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (property.imageUri != null) {
                    AsyncImage(
                        model = property.imageUri,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(property.status.color())
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = property.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (property.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = "سنجاق‌شده",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (property.caseType == CaseType.CLIENT_REQUEST) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            CaseType.CLIENT_REQUEST.icon(),
                            contentDescription = "درخواست مشتری",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${property.city} · ${property.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCasePriceLine(property),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onFavoriteClick()
            }) {
                Icon(
                    imageVector = if (property.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "علاقه‌مندی",
                    tint = if (property.isFavorite) MaterialTheme.extendedColors.danger else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PropertyMiniCard(
    property: Property,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    AppCard(
        modifier = modifier.width(160.dp),
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (property.imageUri != null) {
                    AsyncImage(
                        model = property.imageUri,
                        contentDescription = property.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(property.status.color())
                )
                if (property.isFavorite) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "موردعلاقه",
                        tint = MaterialTheme.extendedColors.danger,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
                if (property.isPinned) {
                    Icon(
                        Icons.Rounded.PushPin,
                        contentDescription = "سنجاق‌شده",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .size(14.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCasePriceLine(property),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

fun formatPrice(price: Long, dealType: DealType): String {
    val formatted = NumberFormat.getNumberInstance(Locale.US).format(price)
    return if (dealType == DealType.RENT) "$formatted تومان (اجاره)" else "$formatted تومان"
}

/** [formatPrice] only makes sense for an OWNER case that actually has a price. A CLIENT_REQUEST
 *  case has no price of its own — cards and rows show its budget range instead, so a priceless
 *  request never renders as a misleading "۰ تومان". */
fun formatCasePriceLine(property: Property): String {
    if (property.caseType != CaseType.CLIENT_REQUEST) return formatPrice(property.price, property.dealType)
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    return when {
        property.budgetMin != null && property.budgetMax != null ->
            "بودجه: ${numberFormat.format(property.budgetMin)} تا ${numberFormat.format(property.budgetMax)} تومان"
        property.budgetMax != null -> "بودجه: تا ${numberFormat.format(property.budgetMax)} تومان"
        property.budgetMin != null -> "بودجه: از ${numberFormat.format(property.budgetMin)} تومان"
        else -> "بودجه مشخص نشده"
    }
}

/** The one-line summary used in list rows — bakes in area+price for an OWNER case (its metraj is
 *  a defining fact), but skips the area segment for a CLIENT_REQUEST case, whose "area" is a
 *  desired range rather than a settled fact, in favor of [formatCasePriceLine]'s budget line. */
fun formatCaseRowSubtitle(property: Property): String = if (property.caseType == CaseType.CLIENT_REQUEST) {
    "${property.city} · ${formatCasePriceLine(property)}"
} else {
    "${property.city} · ${"%.0f".format(property.area)} متر · ${formatCasePriceLine(property)}"
}
