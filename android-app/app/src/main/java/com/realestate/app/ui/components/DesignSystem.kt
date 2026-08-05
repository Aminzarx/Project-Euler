package com.realestate.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.realestate.app.ui.theme.extendedColors

val CardShape = RoundedCornerShape(14.dp)
val PillShape = RoundedCornerShape(50)
val RowShape = RoundedCornerShape(12.dp)

private val ShadowTint = Color.Black.copy(alpha = 0.05f)

/** A very light, barely-visible shadow — used everywhere instead of Material's default (heavier, gray) elevation shadow. */
private fun Modifier.softShadow(shape: Shape, elevation: Dp): Modifier = this.shadow(
    elevation = elevation,
    shape = shape,
    ambientColor = ShadowTint,
    spotColor = ShadowTint
)

/**
 * Elegant borderless rounded card with a very soft shadow — the base surface for every section.
 * When [onClick] is omitted, this renders a plain (non-clickable) Surface rather than a
 * disabled-clickable one, so it never intercepts touch/focus meant for interactive content
 * placed inside it (form fields, buttons, etc.).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = CardShape,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    val cardModifier = modifier.softShadow(shape, 8.dp)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            modifier = cardModifier
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                Column(content = { content() })
            }
        }
    } else {
        Surface(
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
            modifier = cardModifier
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                Column(content = { content() })
            }
        }
    }
}

/** Rounded pill button — warm accent background, dark ink text. The app's primary call to action. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = PillShape,
        color = if (enabled) MaterialTheme.extendedColors.accent else MaterialTheme.extendedColors.disabled,
        contentColor = if (enabled) MaterialTheme.extendedColors.onAccent else MaterialTheme.extendedColors.onDisabled,
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.extendedColors.onAccent,
                    strokeWidth = 2.dp
                )
            } else {
                if (icon != null) {
                    Icon(icon, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Rounded pill button — white background with a light shadow, ink text. Secondary action. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.extendedColors.disabled,
        contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.extendedColors.onDisabled,
        shadowElevation = 0.dp,
        modifier = modifier.height(52.dp).softShadow(PillShape, 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Circular icon button used for compact actions (back, share, favorite, nav items). */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 44.dp,
    elevated: Boolean = true,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) containerColor else MaterialTheme.extendedColors.disabled,
        contentColor = if (enabled) contentColor else MaterialTheme.extendedColors.onDisabled,
        shadowElevation = 0.dp,
        modifier = modifier.size(size).let { if (elevated) it.softShadow(CircleShape, 5.dp) else it }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(20.dp))
        }
    }
}

/** Rounded list row: icon, title, optional subtitle and a trailing chevron — replaces plain rows everywhere. */
@Composable
fun AppListRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth().softShadow(RowShape, 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

data class BottomNavItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

/** Floating rounded bottom navigation — brand-colored active pill, gray inactive icons, soft shadow above content. */
@Composable
fun FloatingBottomNav(items: List<BottomNavItem>, modifier: Modifier = Modifier) {
    val navShape = RoundedCornerShape(24.dp)
    val haptic = LocalHapticFeedback.current
    Surface(
        shape = navShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        modifier = modifier.fillMaxWidth().softShadow(navShape, 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                // 48dp is the Material accessibility minimum touch target; the selected item grows
                // slightly larger as a visual cue, animated rather than snapping instantly.
                val size by animateDpAsState(
                    targetValue = if (item.selected) 52.dp else 48.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "nav-item-size"
                )
                val containerColor by animateColorAsState(
                    targetValue = if (item.selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(220),
                    label = "nav-item-bg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (item.selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(220),
                    label = "nav-item-fg"
                )
                Surface(
                    onClick = {
                        if (!item.selected) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        item.onClick()
                    },
                    shape = CircleShape,
                    color = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.size(size)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

/** Small colored-dot pill badge — used for property status and transaction state. */
@Composable
fun StatusPillBadge(
    text: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val badgeContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = PillShape,
            color = containerColor,
            shadowElevation = 0.dp,
            modifier = modifier,
            content = badgeContent
        )
    } else {
        Surface(
            shape = PillShape,
            color = containerColor,
            shadowElevation = 0.dp,
            modifier = modifier,
            content = badgeContent
        )
    }
}

/** Field label with a colored required-marker — use on the label of any field that must be filled in before saving. */
@Composable
fun RequiredFieldLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(text)
        Text(" *", color = MaterialTheme.extendedColors.danger)
    }
}

/**
 * Overflow / selection menu, restyled at the item level (spacing, typography, theme colors) since
 * this Material3 version doesn't expose container styling params on [androidx.compose.material3.DropdownMenu]
 * itself — its corner radius already follows the design system via [MaterialTheme.shapes.extraSmall].
 * Drop-in replacement: importing this instead of the Material3 original is enough to restyle every
 * three-dot and select menu in the app.
 */
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = content
    )
}

/** Drop-in replacement for [androidx.compose.material3.DropdownMenuItem] with roomier spacing and theme colors. */
@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    androidx.compose.material3.DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = androidx.compose.material3.MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp)
    )
}

/**
 * A card section that starts collapsed (or expanded) and toggles open on tap — used to keep
 * secondary content (description, notes, timeline) out of the way until the user asks for it.
 */
@Composable
fun CollapsibleSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "بستن" else "باز کردن",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Graceful recovery screen for a broken/unreachable navigation destination (an unknown or
 * invalid argument in a route) — used instead of leaving the user staring at a blank screen.
 */
@Composable
fun RouteErrorState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "این صفحه در دسترس نیست",
    subtitle: String = "به‌نظر می‌رسد لینک یا مسیر مورد نظر معتبر نیست. می‌توانید به صفحه قبل بازگردید.",
    actionLabel: String = "بازگشت"
) {
    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.extendedColors.danger.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.extendedColors.danger
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryButton(text = actionLabel, onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}
