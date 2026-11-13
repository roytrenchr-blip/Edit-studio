package com.editstudio.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EditStudioDarkColorScheme = darkColorScheme(
    primary = NavyPrimary,
    onPrimary = Color(0xFF041423),
    primaryContainer = NavyPrimaryDeep,
    onPrimaryContainer = NavyTextPrimary,
    secondary = NavyAccentTeal,
    onSecondary = Color(0xFF031F1D),
    secondaryContainer = NavyElevated,
    onSecondaryContainer = NavyTextPrimary,
    tertiary = NavyAmber,
    onTertiary = Color(0xFF1F1300),
    background = NavyBg,
    onBackground = NavyTextPrimary,
    surface = NavySurface,
    onSurface = NavyTextPrimary,
    surfaceVariant = NavyCard,
    onSurfaceVariant = NavyTextSecondary,
    outline = NavyBorder,
    error = NavyCoral,
    onError = Color.White
)

val EditStudioTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = NavyTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = NavyTextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        color = NavyTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = NavyTextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = NavyTextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = NavyTextPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = NavyTextSecondary
    )
)

val EditStudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun EditStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EditStudioDarkColorScheme,
        typography = EditStudioTypography,
        shapes = EditStudioShapes,
        content = content
    )
}
