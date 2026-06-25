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

// AI-Native UI Color Palette
// Primary accent: #6366F1 (Indigo/AI Purple)
// Neutral backgrounds, subtle depth, minimal chrome

// Light color scheme - AI-Native
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),            // AI Accent Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),   // Very light indigo
    onPrimaryContainer = Color(0xFF3730A3),
    secondary = Color(0xFF64748B),          // Slate gray
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9), // Light slate
    onSecondaryContainer = Color(0xFF334155),
    tertiary = Color(0xFF6366F1),           // Same as primary accent
    background = Color(0xFFFAFBFC),         // Near-white, clean
    onBackground = Color(0xFF1E293B),       // Dark slate
    surface = Color.White,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),     // Light slate for cards
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),            // Subtle border
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444),
    onError = Color.White,
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE2E8F0),
)

// Dark color scheme - AI-Native Dark
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),            // Lighter indigo for dark
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),   // Deep indigo
    onPrimaryContainer = Color(0xFFC7D2FE),
    secondary = Color(0xFF94A3B8),          // Light slate
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFCBD5E1),
    tertiary = Color(0xFF818CF8),
    background = Color(0xFF0F172A),         // Deep navy-black
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),            // Dark slate surface
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),     // Medium slate
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFF87171),
    onError = Color(0xFF1E1B4B),
    surfaceContainerLow = Color(0xFF162032),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerHigh = Color(0xFF334155),
)

// Custom typography - AI-Native (clean, modern)
object AiChatTypography {
    val headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
    )
    val headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
    )
    val titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    )
    val titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
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

// AI-Native custom colors
object AiChatColors {
    // AI accent - Indigo
    val aiAccent = Color(0xFF6366F1)
    val aiAccentLight = Color(0xFF818CF8)
    val aiAccentDark = Color(0xFF4F46E5)
    val aiAccentSurface = Color(0xFFEEF2FF)

    // Chat bubbles - AI-Native
    val chatBubbleUser = Color(0xFF6366F1)       // Indigo accent
    val chatBubbleUserText = Color.White
    val chatBubbleAssistant = Color.White
    val chatBubbleAssistantText = Color(0xFF1E293B)
    val chatBubbleAssistantBorder = Color(0xFFE2E8F0)

    // Dark mode bubbles
    val chatBubbleUserDark = Color(0xFF6366F1)
    val chatBubbleAssistantDark = Color(0xFF1E293B)
    val chatBubbleAssistantDarkBorder = Color(0xFF334155)
    val chatBubbleAssistantDarkText = Color(0xFFE2E8F0)

    // Input
    val chatInputBackground = Color(0xFFF8FAFC)
    val chatInputFocus = Color(0xFF6366F1)

    // Status
    val online = Color(0xFF10B981)           // Emerald green
    val streaming = Color(0xFF6366F1)        // Accent for streaming
    val voiceWave = Color(0xFF6366F1)

    // Typing indicator
    val typingDot = Color(0xFF6366F1)
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
