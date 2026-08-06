package com.realestate.app.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.realestate.app.R
import com.realestate.app.data.datastore.ThemePreference
import com.realestate.app.ui.auth.AuthFlow
import com.realestate.app.ui.theme.RealEstateAppTheme
import com.realestate.app.viewmodel.AppLockViewModel
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.DealAssistantViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel

@Composable
fun RootApp(
    authViewModel: AuthViewModel,
    propertyViewModel: PropertyViewModel,
    walletViewModel: WalletViewModel,
    profileViewModel: ProfileViewModel,
    backupViewModel: BackupViewModel,
    dealAssistantViewModel: DealAssistantViewModel,
    appLockViewModel: AppLockViewModel
) {
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (profile.themePreference) {
        ThemePreference.SYSTEM -> systemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    val securitySettings by appLockViewModel.settings.collectAsStateWithLifecycle()
    val isUnlocked by appLockViewModel.isUnlocked.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Screen security: block screenshots and hide app content from the recents thumbnail while enabled.
    LaunchedEffect(securitySettings.screenSecurityEnabled) {
        (context as? Activity)?.window?.let { window ->
            if (securitySettings.screenSecurityEnabled) {
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    // Re-lock every time the app leaves the foreground — an app lock that stayed unlocked across
    // backgrounding would defeat its own purpose (someone else picking up the phone).
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentAppLockViewModel by rememberUpdatedState(appLockViewModel)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) currentAppLockViewModel.lock()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    RealEstateAppTheme(darkTheme = useDarkTheme) {
        val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
        // A single Crossfade across all three top-level states (loading/auth/main) turns what used
        // to be an abrupt cut between them into one continuous, intentional transition.
        Crossfade(targetState = isLoggedIn, animationSpec = tween(350), label = "root-app-state") { loginState ->
            when (loginState) {
                null -> BrandedLoadingState()

                false -> AuthFlow(viewModel = authViewModel)

                true -> if (securitySettings.hasPin && !isUnlocked) {
                    AppLockScreen(viewModel = appLockViewModel)
                } else {
                    RealEstateApp(
                        viewModel = propertyViewModel,
                        walletViewModel = walletViewModel,
                        profileViewModel = profileViewModel,
                        authViewModel = authViewModel,
                        backupViewModel = backupViewModel,
                        dealAssistantViewModel = dealAssistantViewModel,
                        appLockViewModel = appLockViewModel
                    )
                }
            }
        }
    }
}

/** Branded launch state shown while the saved session is being read — replaces a bare spinner. */
@Composable
private fun BrandedLoadingState() {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val scale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.75f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splash-logo-scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(400),
        label = "splash-logo-alpha"
    )
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 20.dp).graphicsLayer { this.alpha = alpha }
            )
            Box(modifier = Modifier.padding(top = 28.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            }
        }
    }
}
