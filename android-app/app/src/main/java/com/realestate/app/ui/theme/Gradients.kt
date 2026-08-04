package com.realestate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

@Composable
fun heroGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))
}

@Composable
fun cardGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(listOf(scheme.primaryContainer, scheme.tertiaryContainer))
}
