package com.realestate.app.ui.screens.dealassistant

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.realestate.app.data.dealassistant.DealCategory
import com.realestate.app.data.dealassistant.DealToolId

val DealToolId.icon: ImageVector
    get() = when (this) {
        DealToolId.COMMISSION -> Icons.Rounded.Percent
        DealToolId.PURCHASE_COST -> Icons.Rounded.Receipt
        DealToolId.CONSTRUCTION_COST -> Icons.Rounded.Construction
        DealToolId.RENTAL_CONVERSION -> Icons.Rounded.SwapHoriz
        DealToolId.ROI -> Icons.Rounded.TrendingUp
        DealToolId.LOAN -> Icons.Rounded.AccountBalance
        DealToolId.INSTALLMENT -> Icons.Rounded.CalendarMonth
        DealToolId.AVERAGE_PRICE -> Icons.Rounded.BarChart
        DealToolId.MARKET_VALUE -> Icons.Rounded.Insights
        DealToolId.NEIGHBORHOOD -> Icons.Rounded.LocationCity
        DealToolId.RANKING -> Icons.Rounded.EmojiEvents
        DealToolId.COMPARISON -> Icons.Rounded.CompareArrows
        DealToolId.STORY_GENERATOR -> Icons.Rounded.AutoStories
        DealToolId.AD_TEXT -> Icons.Rounded.Campaign
        DealToolId.QR_CODE -> Icons.Rounded.QrCode
        DealToolId.POSTER_GENERATOR -> Icons.Rounded.Image
        DealToolId.PDF_PRESENTATION -> Icons.Rounded.PictureAsPdf
        DealToolId.BRANDING_TEMPLATES -> Icons.Rounded.Style
        DealToolId.SHARE_TEMPLATES -> Icons.Rounded.Share
        DealToolId.AREA_CONVERTER -> Icons.Rounded.Straighten
        DealToolId.PERCENTAGE -> Icons.Rounded.Percent
        DealToolId.DATE_CALCULATOR -> Icons.Rounded.CalendarMonth
        DealToolId.QUICK_NOTES -> Icons.Rounded.StickyNote2
        DealToolId.CURRENCY_CONVERTER -> Icons.Rounded.CurrencyExchange
    }

val DealCategory.icon: ImageVector
    get() = when (this) {
        DealCategory.FINANCIAL -> Icons.Rounded.Payments
        DealCategory.ANALYSIS -> Icons.Rounded.Insights
        DealCategory.MARKETING -> Icons.Rounded.Campaign
        DealCategory.UTILITIES -> Icons.Rounded.Build
    }

val dealAssistantNavIcon: ImageVector get() = Icons.Rounded.AutoAwesome
