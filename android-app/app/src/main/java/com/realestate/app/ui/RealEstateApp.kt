package com.realestate.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Scaffold
import com.realestate.app.data.dealassistant.DealToolId
import com.realestate.app.ui.screens.dealassistant.AdTextGeneratorScreen
import com.realestate.app.ui.screens.dealassistant.DealAssistantScreen
import com.realestate.app.ui.screens.dealassistant.DealCalculatorScreen
import com.realestate.app.ui.screens.dealassistant.PropertyAnalysisScreen
import com.realestate.app.ui.screens.dealassistant.QuickNotesScreen
import com.realestate.app.ui.screens.dealassistant.dealAssistantNavIcon
import com.realestate.app.viewmodel.DealAssistantViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.realestate.app.ui.components.BottomNavItem
import com.realestate.app.ui.components.FloatingBottomNav
import com.realestate.app.ui.components.RouteErrorState
import com.realestate.app.ui.navigation.Screen
import com.realestate.app.ui.screens.AboutScreen
import com.realestate.app.ui.screens.ActivityHistoryScreen
import com.realestate.app.ui.screens.AddEditPropertyScreen
import com.realestate.app.ui.screens.EditProfileScreen
import com.realestate.app.ui.screens.FavoritesScreen
import com.realestate.app.ui.screens.HelpCenterScreen
import com.realestate.app.ui.screens.HomeScreen
import com.realestate.app.ui.screens.ProfileScreen
import com.realestate.app.ui.screens.PropertyDetailScreen
import com.realestate.app.ui.screens.PropertyListScreen
import com.realestate.app.ui.screens.SettingsScreen
import com.realestate.app.ui.screens.StoryCardScreen
import com.realestate.app.ui.screens.WalletScreen
import com.realestate.app.viewmodel.AppLockViewModel
import com.realestate.app.viewmodel.AuthViewModel
import com.realestate.app.viewmodel.BackupViewModel
import com.realestate.app.viewmodel.ProfileViewModel
import com.realestate.app.viewmodel.PropertyViewModel
import com.realestate.app.viewmodel.WalletViewModel

private data class BottomTab(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Screen.Home, "خانه", Icons.Rounded.Home),
    BottomTab(Screen.List, "املاک", Icons.Rounded.List),
    BottomTab(Screen.DealAssistant, "دستیار", dealAssistantNavIcon),
    BottomTab(Screen.Favorites, "علاقه‌مندی‌ها", Icons.Rounded.Favorite),
    BottomTab(Screen.Profile, "پروفایل", Icons.Rounded.Person)
)

/** The 5 bottom-tab destinations switch between each other laterally (fade through); every other
 *  route is a hierarchical push/pop, which slides in from the reading-direction edge instead. */
private val topLevelRoutes = bottomTabs.map { it.screen.route }.toSet()

@Composable
fun RealEstateApp(
    viewModel: PropertyViewModel,
    walletViewModel: WalletViewModel,
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    backupViewModel: BackupViewModel,
    dealAssistantViewModel: DealAssistantViewModel,
    appLockViewModel: AppLockViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Fall back to Home so the bottom nav shows it selected immediately on first launch,
    // before the NavController's back-stack state flow emits its first value.
    val currentRoute = backStackEntry?.destination?.route ?: Screen.Home.route
    val showBottomBar = currentRoute == Screen.Home.route ||
        currentRoute == Screen.List.route ||
        currentRoute == Screen.DealAssistant.route ||
        currentRoute == Screen.Favorites.route ||
        currentRoute == Screen.Profile.route

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.deletionEvents.collect { propertyTitle ->
            val result = snackbarHostState.showSnackbar(
                message = "«$propertyTitle» حذف شد",
                actionLabel = "بازگردانی",
                duration = androidx.compose.material3.SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastDelete()
            } else {
                // Undo window closed without a tap — safe to clean up the property's
                // notes/timeline now so they don't linger as orphaned rows forever.
                viewModel.finalizeDelete()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomNav(
                    items = bottomTabs.map { tab ->
                        BottomNavItem(
                            icon = tab.icon,
                            label = tab.label,
                            selected = currentRoute == tab.screen.route,
                            onClick = {
                                if (currentRoute != tab.screen.route) {
                                    navController.navigate(tab.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    ) { innerPadding ->
        fun isTabSwitch(scope: AnimatedContentTransitionScope<androidx.navigation.NavBackStackEntry>): Boolean {
            val from = scope.initialState.destination.route
            val to = scope.targetState.destination.route
            return from in topLevelRoutes && to in topLevelRoutes
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                if (isTabSwitch(this)) {
                    fadeIn(animationSpec = tween(200))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                if (isTabSwitch(this)) {
                    fadeOut(animationSpec = tween(150))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(200))
                }
            },
            popEnterTransition = {
                if (isTabSwitch(this)) {
                    fadeIn(animationSpec = tween(200))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                if (isTabSwitch(this)) {
                    fadeOut(animationSpec = tween(150))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(200))
                }
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    appLockViewModel = appLockViewModel,
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) },
                    onSearchClick = {
                        navController.navigate(Screen.List.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenQuickNotes = { navController.navigate(Screen.QuickNotes.route) },
                    onOpenActivityHistory = { navController.navigate(Screen.ActivityHistory.route) }
                )
            }
            composable(Screen.List.route) {
                PropertyListScreen(
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    walletViewModel = walletViewModel,
                    propertyViewModel = viewModel,
                    authViewModel = authViewModel,
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onOpenWallet = { navController.navigate(Screen.Wallet.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenHelp = { navController.navigate(Screen.HelpCenter.route) },
                    onOpenAbout = { navController.navigate(Screen.About.route) },
                    onOpenActivityHistory = { navController.navigate(Screen.ActivityHistory.route) },
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(Screen.ActivityHistory.route) {
                ActivityHistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Screen.HelpCenter.route) {
                HelpCenterScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    profileViewModel = profileViewModel,
                    propertyViewModel = viewModel,
                    backupViewModel = backupViewModel,
                    appLockViewModel = appLockViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Wallet.route) {
                WalletScreen(
                    viewModel = walletViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument(Screen.Detail.ARG_PROPERTY_ID) { type = NavType.LongType })
            ) { entry ->
                val propertyId = entry.arguments?.getLong(Screen.Detail.ARG_PROPERTY_ID) ?: -1L
                PropertyDetailScreen(
                    propertyId = propertyId,
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    appLockViewModel = appLockViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.AddEdit.createRoute(id)) },
                    onDeleted = { navController.popBackStack() },
                    onStoryCard = { id -> navController.navigate(Screen.StoryCard.createRoute(id)) },
                    onOpenDealTool = { tool ->
                        navController.navigate(Screen.DealCalculator.createRoute(tool.name, propertyId))
                    }
                )
            }
            composable(
                route = Screen.StoryCard.route,
                arguments = listOf(navArgument(Screen.StoryCard.ARG_PROPERTY_ID) { type = NavType.LongType })
            ) { entry ->
                val propertyId = entry.arguments?.getLong(Screen.StoryCard.ARG_PROPERTY_ID) ?: -1L
                StoryCardScreen(
                    propertyId = propertyId,
                    viewModel = viewModel,
                    profileViewModel = profileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AddEdit.route,
                arguments = listOf(
                    navArgument(Screen.AddEdit.ARG_PROPERTY_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val propertyId = entry.arguments?.getLong(Screen.AddEdit.ARG_PROPERTY_ID) ?: -1L
                AddEditPropertyScreen(
                    propertyId = if (propertyId == -1L) null else propertyId,
                    viewModel = viewModel,
                    onDone = { navController.popBackStack() }
                )
            }
            composable(Screen.DealAssistant.route) {
                DealAssistantScreen(
                    viewModel = dealAssistantViewModel,
                    propertyViewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenCalculator = { tool, propertyId ->
                        navController.navigate(Screen.DealCalculator.createRoute(tool.name, propertyId))
                    },
                    onOpenAnalysis = { tool, propertyId ->
                        navController.navigate(Screen.PropertyAnalysis.createRoute(tool.name, propertyId))
                    },
                    onOpenQuickNotes = { navController.navigate(Screen.QuickNotes.route) },
                    onOpenAdText = { id -> navController.navigate(Screen.AdTextGenerator.createRoute(id)) },
                    onOpenStoryCard = { id -> navController.navigate(Screen.StoryCard.createRoute(id)) }
                )
            }
            composable(
                route = Screen.DealCalculator.route,
                arguments = listOf(
                    navArgument(Screen.DealCalculator.ARG_TOOL_ID) { type = NavType.StringType },
                    navArgument(Screen.DealCalculator.ARG_PROPERTY_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val toolId = DealToolId.fromKey(entry.arguments?.getString(Screen.DealCalculator.ARG_TOOL_ID).orEmpty())
                val propertyId = entry.arguments?.getLong(Screen.DealCalculator.ARG_PROPERTY_ID) ?: -1L
                if (toolId != null) {
                    DealCalculatorScreen(
                        toolId = toolId,
                        propertyId = propertyId,
                        viewModel = dealAssistantViewModel,
                        propertyViewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    RouteErrorState(onBack = { navController.popBackStack() })
                }
            }
            composable(
                route = Screen.PropertyAnalysis.route,
                arguments = listOf(
                    navArgument(Screen.PropertyAnalysis.ARG_TOOL_ID) { type = NavType.StringType },
                    navArgument(Screen.PropertyAnalysis.ARG_PROPERTY_ID) {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                val toolId = DealToolId.fromKey(entry.arguments?.getString(Screen.PropertyAnalysis.ARG_TOOL_ID).orEmpty())
                val propertyId = entry.arguments?.getLong(Screen.PropertyAnalysis.ARG_PROPERTY_ID) ?: -1L
                if (toolId != null) {
                    PropertyAnalysisScreen(
                        toolId = toolId,
                        propertyId = if (propertyId == -1L) null else propertyId,
                        viewModel = dealAssistantViewModel,
                        propertyViewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    RouteErrorState(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.QuickNotes.route) {
                QuickNotesScreen(viewModel = dealAssistantViewModel, onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.AdTextGenerator.route,
                arguments = listOf(navArgument(Screen.AdTextGenerator.ARG_PROPERTY_ID) { type = NavType.LongType })
            ) { entry ->
                val propertyId = entry.arguments?.getLong(Screen.AdTextGenerator.ARG_PROPERTY_ID) ?: -1L
                AdTextGeneratorScreen(
                    propertyId = propertyId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
