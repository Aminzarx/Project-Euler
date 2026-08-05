package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.ui.STORE_URL
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.components.SecondaryButton
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.heroGradient
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.WalletViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    walletViewModel: WalletViewModel,
    authViewModel: AuthViewModel,
    onEditProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val balance by walletViewModel.balance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پروفایل") },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Rounded.Edit, contentDescription = "ویرایش پروفایل")
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
                    .background(heroGradient())
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-48).dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                AppListRow(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "کیف پول",
                    subtitle = "${NumberFormat.getNumberInstance(Locale.US).format(balance)} تومان",
                    onClick = onOpenWallet
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

                if (profile.instagram.isNotBlank() || profile.telegram.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Text("شبکه‌های اجتماعی", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (profile.instagram.isNotBlank()) {
                        Text("اینستاگرام: ${profile.instagram}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (profile.telegram.isNotBlank()) {
                        Text("تلگرام: ${profile.telegram}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
                SecondaryButton(
                    text = "خروج از حساب",
                    onClick = { authViewModel.logout() },
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

