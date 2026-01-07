package com.example.superpodcast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Amber,
    secondary = Mint,
    background = Espresso,
    surface = Cocoa,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun SuperPodcastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
