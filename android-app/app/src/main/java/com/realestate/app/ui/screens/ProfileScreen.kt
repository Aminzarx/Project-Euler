package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.data.DealType
import com.realestate.app.data.PropertyStatus
import com.realestate.app.data.datastore.completionPercent
import com.realestate.app.data.datastore.missingFieldSuggestions
import com.realestate.app.ui.STORE_URL
import com.realestate.app.ui.components.AppCard
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.components.CircleIconButton
import com.realestate.app.ui.components.ConfirmationDialog
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import com.realestate.app.ui.components.icon
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    walletViewModel: WalletViewModel,
    propertyViewModel: PropertyViewModel,
    authViewModel: AuthViewModel,
    onEditProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAbout: () -> Unit,
    onPropertyClick: (Long) -> Unit
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val balance by walletViewModel.balance.collectAsStateWithLifecycle()
    val allProperties by propertyViewModel.allProperties.collectAsStateWithLifecycle()
    val favoriteProperties by propertyViewModel.favoriteProperties.collectAsStateWithLifecycle()
    val recentActivities by propertyViewModel.recentActivities.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val activeCount = remember(allProperties) {
        allProperties.count { it.status != PropertyStatus.SOLD && it.status != PropertyStatus.RENTED && it.status != PropertyStatus.ARCHIVED }
    }
    val soldCount = remember(allProperties) { allProperties.count { it.status == PropertyStatus.SOLD } }
    val rentalCount = remember(allProperties) {
        allProperties.count { it.dealType == DealType.RENT && it.status != PropertyStatus.ARCHIVED }
    }
    val upcomingFollowUps = remember(allProperties) {
        allProperties.filter { it.followUpAt != null }.sortedBy { it.followUpAt }.take(3)
    }
    val recentActivityPreview = remember(recentActivities) { recentActivities.take(5) }
    val completionPercent = remember(profile) { profile.completionPercent() }
    val missingFields = remember(profile) { profile.missingFieldSuggestions(2) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "بیشتر")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("خروج از حساب") },
                                leadingIcon = Icons.AutoMirrored.Rounded.Logout,
                                danger = true,
                                onClick = {
                                    showMenu = false
                                    showLogoutConfirm = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.extendedColors.accent)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.profilePhotoUri != null) {
                                AsyncImage(
                                    model = profile.profilePhotoUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        CircleIconButton(
                            icon = Icons.Rounded.Edit,
                            onClick = onEditProfile,
                            contentDescription = "ویرایش پروفایل",
                            containerColor = MaterialTheme.extendedColors.accent,
                            contentColor = MaterialTheme.extendedColors.onAccent,
                            size = 32.dp,
                            modifier = Modifier.align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile.fullName.ifBlank { "کاربر مدیریت املاک" },
                        style = MaterialTheme.typography.headlineLarge
                    )
                    if (profile.agencyName.isNotBlank()) {
                        Text(
                            profile.agencyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (profile.slogan.isNotBlank()) {
                        Text(
                            profile.slogan,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (profile.mobileNumber.isNotBlank()) {
                        Text(
                            profile.mobileNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = Spacing.screen)
                    .offset(y = (-24).dp)
            ) {
                if (completionPercent < 100) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onEditProfile)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تکمیل پروفایل کسب‌وکار", style = MaterialTheme.typography.titleSmall)
                            Text("$completionPercent٪", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { completionPercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (missingFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "پیشنهاد بعدی: ${missingFields.joinToString("، ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.lg))
                }

                Text("خلاصه کسب‌وکار", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Spacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("کل ملک‌ها", allProperties.size.toString(), Icons.Rounded.Home, Modifier.weight(1f))
                    StatTile("آگهی فعال", activeCount.toString(), PropertyStatus.ACTIVE.icon(), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("فروخته‌شده", soldCount.toString(), PropertyStatus.SOLD.icon(), Modifier.weight(1f))
                    StatTile("اجاره‌ای", rentalCount.toString(), PropertyStatus.RENTED.icon(), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("علاقه‌مندی‌ها", favoriteProperties.size.toString(), Icons.Rounded.Person, Modifier.weight(1f))
                    StatTile(
                        label = "کیف پول",
                        value = "${NumberFormat.getNumberInstance(Locale.US).format(balance)} ت",
                        icon = Icons.Rounded.AccountBalanceWallet,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenWallet
                    )
                }

                if (upcomingFollowUps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    Text("پیگیری‌های پیش رو", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        upcomingFollowUps.forEachIndexed { index, property ->
                            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPropertyClick(property.id) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(property.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 10.dp))
                                }
                                Text(
                                    relativeFollowUpLabel(property.followUpAt ?: 0L),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (recentActivityPreview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    Text("فعالیت‌های اخیر", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(Spacing.md))
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        recentActivityPreview.forEachIndexed { index, activity ->
                            if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPropertyClick(activity.propertyId) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(activity.event.type.icon(), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Column(modifier = Modifier.padding(start = 10.dp)) {
                                    Text(activity.propertyTitle, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        activity.event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                Text("حساب کاربری", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.Person,
                    title = "اطلاعات پروفایل",
                    subtitle = "نام، عکس، شماره و اطلاعات آژانس",
                    onClick = onEditProfile
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.Storefront,
                    title = "فروشگاه ما",
                    subtitle = "خرید تجهیزات و ابزارهای اختصاصی مشاوران",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(STORE_URL)))
                    }
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.Settings,
                    title = "تنظیمات",
                    subtitle = "ظاهر برنامه، پشتیبان‌گیری و موارد دیگر",
                    onClick = onOpenSettings
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.HelpOutline,
                    title = "راهنما و پشتیبانی",
                    subtitle = "سوالات متداول و راه‌های تماس با ما",
                    onClick = onOpenHelp
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.Info,
                    title = "درباره برنامه",
                    subtitle = "نسخه، حریم خصوصی و اطلاعات برنامه",
                    onClick = onOpenAbout
                )

                if (profile.biography.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xl))
                    Text("درباره من", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        profile.biography,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (profile.instagram.isNotBlank() || profile.telegram.isNotBlank() || profile.website.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text("شبکه‌های اجتماعی", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (profile.instagram.isNotBlank()) {
                        Text("اینستاگرام: ${profile.instagram}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (profile.telegram.isNotBlank()) {
                        Text("تلگرام: ${profile.telegram}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (profile.website.isNotBlank()) {
                        Text("وب‌سایت: ${profile.website}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }

    if (showLogoutConfirm) {
        ConfirmationDialog(
            title = "خروج از حساب؟",
            text = "برای ورود دوباره باید شماره موبایل خود را تأیید کنید. اطلاعات ملک‌ها روی گوشی باقی می‌ماند.",
            confirmLabel = "خروج",
            danger = true,
            onConfirm = { authViewModel.logout() },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

private fun relativeFollowUpLabel(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val dayMillis = 24 * 60 * 60 * 1000L
    val diffDays = (timestamp - now) / dayMillis
    return when {
        timestamp <= now -> "سررسیده"
        diffDays <= 0 -> "امروز"
        diffDays == 1L -> "فردا"
        else -> "$diffDays روز دیگر"
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
