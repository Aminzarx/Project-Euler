package com.realestate.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.realestate.app.data.Property
import com.realestate.app.ui.theme.extendedColors

/**
 * Compact, scannable property row supporting the app's core gestures in one place:
 * tap to open, long-press to enter multi-select, double-tap to favorite, and swipe
 * to call (toward the reading-start edge) or share (toward the end edge).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeablePropertyRow(
    property: Property,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCall: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCall()
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShare()
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = !selectionMode,
        enableDismissFromEndToStart = !selectionMode,
        backgroundContent = {
            val isCall = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val isShare = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardShape)
                    .background(
                        when {
                            isCall -> MaterialTheme.extendedColors.success
                            isShare -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = when {
                    isCall -> Alignment.CenterStart
                    isShare -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                when {
                    isCall -> Icon(Icons.Rounded.Call, contentDescription = "تماس", tint = Color.White)
                    isShare -> Icon(Icons.Rounded.Share, contentDescription = "اشتراک‌گذاری", tint = Color.White)
                    else -> Unit
                }
            }
        }
    ) {
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress,
                    onDoubleClick = onToggleFavorite
                ),
            contentPadding = PaddingValues(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectionMode) {
                    Icon(
                        if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                            Icons.Rounded.Home,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(property.status.color())
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (property.isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = "سنجاق‌شده",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "${property.city} · ${formatPrice(property.price, property.dealType)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!selectionMode) {
                    Icon(
                        if (property.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "علاقه‌مندی",
                        tint = if (property.isFavorite) MaterialTheme.extendedColors.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
