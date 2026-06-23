package com.aichat.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 主色调 - 柔和的靛蓝色
val Primary = Color(0xFF5C6BC0)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE8EAF6)
val OnPrimaryContainer = Color(0xFF1A237E)

// 次要色调
val Secondary = Color(0xFF26A69A)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE0F2F1)
val OnSecondaryContainer = Color(0xFF004D40)

// 第三色调
val Tertiary = Color(0xFFAB47BC)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFF3E5F5)
val OnTertiaryContainer = Color(0xFF4A148C)

// 错误
val Error = Color(0xFFEF5350)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFCDD2)
val OnErrorContainer = Color(0xFFB71C1C)

// 背景
val BackgroundLight = Color(0xFFFAFAFA)
val OnBackgroundLight = Color(0xFF212121)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF212121)

val BackgroundDark = Color(0xFF121212)
val OnBackgroundDark = Color(0xFFE0E0E0)
val SurfaceDark = Color(0xFF1E1E1E)
val OnSurfaceDark = Color(0xFFE0E0E0)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
