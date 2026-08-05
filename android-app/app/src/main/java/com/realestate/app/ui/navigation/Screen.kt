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
    object Settings : Screen("settings")
    object HelpCenter : Screen("help_center")
    object About : Screen("about")

    object DealAssistant : Screen("deal_assistant")

    object DealCalculator : Screen("deal_calculator/{toolId}?propertyId={propertyId}") {
        const val ARG_TOOL_ID = "toolId"
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(toolId: String, propertyId: Long? = null) =
            if (propertyId != null) "deal_calculator/$toolId?propertyId=$propertyId" else "deal_calculator/$toolId?propertyId=-1"
    }

    object PropertyAnalysis : Screen("property_analysis/{toolId}?propertyId={propertyId}") {
        const val ARG_TOOL_ID = "toolId"
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(toolId: String, propertyId: Long? = null) =
            if (propertyId != null) "property_analysis/$toolId?propertyId=$propertyId" else "property_analysis/$toolId?propertyId=-1"
    }

    object QuickNotes : Screen("quick_notes")

    object AdTextGenerator : Screen("ad_text_generator/{propertyId}") {
        const val ARG_PROPERTY_ID = "propertyId"
        fun createRoute(propertyId: Long) = "ad_text_generator/$propertyId"
    }
}
