package com.aichat.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Light color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF5F5F5),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF666666),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF333333),
    tertiary = Color(0xFF4D6BFE),
    background = Color(0xFFF7F7F7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFCCCCCC),
    error = Color(0xFFE53935),
    onError = Color.White,
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary = Color(0xFF999999),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFCCCCCC),
    tertiary = Color(0xFF7B93FF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFF999999),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF555555),
    error = Color(0xFFEF5350),
    onError = Color.White,
)

// Custom typography
object AiChatTypography {
    val headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    )
    val headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    )
    val titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    )
    val titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
}

// Spacing
object Spacing {
    val xs = 4.0
    val sm = 8.0
    val md = 16.0
    val lg = 24.0
    val xl = 32.0
    val xxl = 48.0
}

// Custom colors not in Material theme
object AiChatColors {
    val chatBubbleUser = Color(0xFF1A1A1A)
    val chatBubbleUserText = Color.White
    val chatBubbleAssistant = Color.White
    val chatBubbleAssistantText = Color(0xFF1A1A1A)
    val chatInputBackground = Color.White
    val online = Color(0xFF4CAF50)
    val voiceWave = Color(0xFF4D6BFE)
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AiChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appStrings = rememberStrings()

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalStrings provides appStrings,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
