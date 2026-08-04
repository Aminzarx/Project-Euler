package com.realestate.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.realestate.app.ui.navigation.Screen
import com.realestate.app.ui.screens.AddEditPropertyScreen
import com.realestate.app.ui.screens.FavoritesScreen
import com.realestate.app.ui.screens.PropertyDetailScreen
import com.realestate.app.ui.screens.PropertyListScreen
import com.realestate.app.viewmodel.PropertyViewModel

private data class BottomTab(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Screen.List, "املاک", Icons.Filled.Home),
    BottomTab(Screen.Favorites, "علاقه‌مندی‌ها", Icons.Filled.Favorite)
)

@Composable
fun RealEstateApp(viewModel: PropertyViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Screen.List.route || currentRoute == Screen.Favorites.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.screen.route,
                            onClick = {
                                if (currentRoute != tab.screen.route) {
                                    navController.navigate(tab.screen.route) {
                                        popUpTo(Screen.List.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.List.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.List.route) {
                PropertyListScreen(
                    viewModel = viewModel,
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onAddClick = { navController.navigate(Screen.AddEdit.createRoute()) }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onPropertyClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
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
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.AddEdit.createRoute(id)) },
                    onDeleted = { navController.popBackStack() }
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
        }
    }
}
