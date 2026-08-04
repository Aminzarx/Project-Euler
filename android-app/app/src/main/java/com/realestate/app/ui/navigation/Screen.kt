package com.realestate.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object List : Screen("list")
    object Favorites : Screen("favorites")

    object Detail : Screen("detail/{propertyId}") {
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(propertyId: Long) = "detail/$propertyId"
    }

    object AddEdit : Screen("add_edit?propertyId={propertyId}") {
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(propertyId: Long? = null) =
            if (propertyId != null) "add_edit?propertyId=$propertyId" else "add_edit"
    }

    object StoryCard : Screen("story_card/{propertyId}") {
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(propertyId: Long) = "story_card/$propertyId"
    }

    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Wallet : Screen("wallet")
}
