package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.realestate.app.ui.STORE_URL
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
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش پروفایل")
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
                    .background(heroGradient())
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
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
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile.fullName.ifBlank { "کاربر مدیریت املاک" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    if (profile.agencyName.isNotBlank()) {
                        Text(
                            profile.agencyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    if (profile.mobileNumber.isNotBlank()) {
                        Text(
                            profile.mobileNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileActionRow(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = "کیف پول",
                    subtitle = "${NumberFormat.getNumberInstance(Locale.US).format(balance)} تومان",
                    onClick = onOpenWallet
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileActionRow(
                    icon = Icons.Filled.Storefront,
                    title = "فروشگاه ما",
                    subtitle = "خرید تجهیزات و ابزارهای اختصاصی مشاوران",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(STORE_URL)))
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileActionRow(
                    icon = Icons.Filled.Settings,
                    title = "تنظیمات",
                    subtitle = "ظاهر برنامه، پشتیبان‌گیری و موارد دیگر",
                    onClick = onOpenSettings
                )

                if (profile.biography.isNotBlank()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("درباره من", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        profile.biography,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (profile.instagram.isNotBlank() || profile.telegram.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("شبکه‌های اجتماعی", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    if (profile.instagram.isNotBlank()) {
                        Text("اینستاگرام: ${profile.instagram}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (profile.telegram.isNotBlank()) {
                        Text("تلگرام: ${profile.telegram}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروج از حساب")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

