package com.realestate.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.realestate.app.ui.STORE_URL
import com.realestate.app.ui.components.AppListRow
import com.realestate.app.ui.components.CircleIconButton
import com.realestate.app.ui.components.DropdownMenu
import com.realestate.app.ui.components.DropdownMenuItem
import com.realestate.app.ui.theme.Spacing
import com.realestate.app.ui.theme.extendedColors
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
    var showMenu by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                                onClick = {
                                    showMenu = false
                                    authViewModel.logout()
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
                    icon = Icons.Rounded.Person,
                    title = "اطلاعات پروفایل",
                    subtitle = "نام، عکس، شماره و اطلاعات آژانس",
                    onClick = onEditProfile
                )
                Spacer(modifier = Modifier.height(Spacing.md))
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
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.HelpOutline,
                    title = "راهنما و پشتیبانی",
                    subtitle = "سوالات متداول و راه‌های تماس با ما",
                    onClick = { showHelpDialog = true }
                )
                Spacer(modifier = Modifier.height(Spacing.md))
                AppListRow(
                    icon = Icons.Rounded.Info,
                    title = "درباره برنامه",
                    subtitle = "نسخه و اطلاعات برنامه",
                    onClick = { showAboutDialog = true }
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
            }
        }
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("راهنما و پشتیبانی") },
            text = { Text("برای سوالات یا مشکلات فنی می‌توانید از طریق پشتیبانی برنامه یا صفحه «فروشگاه ما» با تیم ما در ارتباط باشید.") },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) { Text("متوجه شدم") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("درباره برنامه") },
            text = { Text("مدیریت املاک\nنسخه ۱.۰") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("بستن") }
            }
        )
    }
}

