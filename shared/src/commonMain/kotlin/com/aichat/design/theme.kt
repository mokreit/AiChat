﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿﻿package com.aichat.design

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

// Black & White color scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000000),            // black
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3F4F6),   // gray-100
    onPrimaryContainer = Color(0xFF111827), // gray-900
    secondary = Color(0xFF9CA3AF),          // gray-400
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF9FAFB), // gray-50
    onSecondaryContainer = Color(0xFF374151), // gray-700
    tertiary = Color(0xFF6B7280),           // gray-500
    background = Color(0xFFF9FAFB),         // gray-50
    onBackground = Color(0xFF111827),       // gray-900
    surface = Color.White,
    onSurface = Color(0xFF111827),          // gray-900
    surfaceVariant = Color(0xFFF3F4F6),     // gray-100
    onSurfaceVariant = Color(0xFF9CA3AF),   // gray-400
    outline = Color(0xFFE5E7EB),            // gray-200
    outlineVariant = Color(0xFFE5E7EB),     // gray-200
    error = Color(0xFFEF4444),
    onError = Color.White,
    surfaceContainerLow = Color(0xFFF9FAFB), // gray-50
    surfaceContainer = Color(0xFFF3F4F6),    // gray-100
    surfaceContainerHigh = Color(0xFFE5E7EB),// gray-200
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF374151),   // gray-700
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF9CA3AF),
    onSecondary = Color(0xFF111827),
    secondaryContainer = Color(0xFF1F2937), // gray-800
    onSecondaryContainer = Color(0xFFD1D5DB),
    tertiary = Color(0xFFD1D5DB),           // gray-300
    background = Color(0xFF111111),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF4B5563),            // gray-600
    outlineVariant = Color(0xFF374151),
    error = Color(0xFFF87171),
    onError = Color(0xFF111111),
    surfaceContainerLow = Color(0xFF151515),
    surfaceContainer = Color(0xFF1F2937),
    surfaceContainerHigh = Color(0xFF374151),
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

// Black & White colors
object AiChatColors {
    val aiAccent = Color(0xFF000000)             // black
    val aiAccentLight = Color(0xFF374151)        // gray-700
    val aiAccentDark = Color(0xFF000000)         // black
    val aiAccentSurface = Color(0xFFF3F4F6)      // gray-100

    // Chat bubbles - black & white
    val chatBubbleUser = Color(0xFF000000)         // black
    val chatBubbleUserText = Color.White
    val chatBubbleAssistant = Color.White           // white
    val chatBubbleAssistantText = Color(0xFF1F2937) // gray-800
    val chatBubbleAssistantBorder = Color(0xFFE5E7EB) // gray-200

    // Dark mode
    val chatBubbleUserDark = Color.White
    val chatBubbleAssistantDark = Color(0xFF1F2937)
    val chatBubbleAssistantDarkBorder = Color(0xFF4B5563)
    val chatBubbleAssistantDarkText = Color(0xFFF9FAFB)

    // Input
    val chatInputBackground = Color(0xFFF9FAFB)  // gray-50
    val chatInputFocus = Color(0xFF000000)       // black

    // Status
    val online = Color(0xFF10B981)
    val streaming = Color(0xFF000000)
    val voiceWave = Color(0xFF000000)

    // Typing indicator
    val typingDot = Color(0xFF9CA3AF)            // gray-400
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
