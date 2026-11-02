package com.mindnest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Light = lightColorScheme(
    primary = Color(0xFF5B4BDB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E4FF),
    onPrimaryContainer = Color(0xFF1E1748),
    secondary = Color(0xFF2B9C8F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8F3EF),
    onSecondaryContainer = Color(0xFF0B3833),
    background = Color(0xFFF6F6F9),
    onBackground = Color(0xFF1B1C22),
    surface = Color(0xFFF6F6F9),
    onSurface = Color(0xFF1B1C22),
    surfaceVariant = Color(0xFFECECF2),
    onSurfaceVariant = Color(0xFF61646F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFBFD),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF0F0F5),
    surfaceContainerHighest = Color(0xFFE9E9EF),
    outline = Color(0xFFC9CAD3),
    outlineVariant = Color(0xFFE3E3EA),
    error = Color(0xFFD64545),
    errorContainer = Color(0xFFFFE1E1),
    onErrorContainer = Color(0xFF5C1111),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB4A8FF),
    onPrimary = Color(0xFF1E1748),
    primaryContainer = Color(0xFF332B66),
    onPrimaryContainer = Color(0xFFE8E4FF),
    secondary = Color(0xFF6FD6C8),
    onSecondary = Color(0xFF07302B),
    secondaryContainer = Color(0xFF16413C),
    onSecondaryContainer = Color(0xFFCFF5EF),
    background = Color(0xFF0E1014),
    onBackground = Color(0xFFE8E9EE),
    surface = Color(0xFF0E1014),
    onSurface = Color(0xFFE8E9EE),
    surfaceVariant = Color(0xFF1C1F27),
    onSurfaceVariant = Color(0xFFA2A6B2),
    surfaceContainerLowest = Color(0xFF0A0B0E),
    surfaceContainerLow = Color(0xFF14161C),
    surfaceContainer = Color(0xFF171A21),
    surfaceContainerHigh = Color(0xFF1F222A),
    surfaceContainerHighest = Color(0xFF272A33),
    outline = Color(0xFF3B3F4A),
    outlineVariant = Color(0xFF262932),
    error = Color(0xFFFF8A8A),
    errorContainer = Color(0xFF4A1C1F),
    onErrorContainer = Color(0xFFFFDADA),
)

/** Muted, distinct accent colours so folders are recognisable at a glance. */
val FolderColors = listOf(
    Color(0xFF8B7CF6), // violet
    Color(0xFF5B9DF5), // blue
    Color(0xFF2BB8A6), // teal
    Color(0xFF6CC56C), // green
    Color(0xFFF2B33D), // amber
    Color(0xFFF2884B), // orange
    Color(0xFFEF6F8E), // rose
    Color(0xFF8A94A6), // slate
)

fun folderColor(index: Int): Color = FolderColors[Math.floorMod(index, FolderColors.size)]

private val AppTypography = Typography().let { t ->
    t.copy(
        headlineLarge = t.headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 25.sp, letterSpacing = 0.1.sp),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun MindnestTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) Dark else Light,
        typography = AppTypography,
        content = content,
    )
}
