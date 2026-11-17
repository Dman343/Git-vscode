package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class SemanticCodeColors(
    val jsonKey: Color,
    val jsonBooleanTrue: Color,
    val jsonBooleanFalse: Color,
    val jsonString: Color,
    val jsonNumber: Color,
    val jsonPunctuation: Color,
    val modifiedBadge: Color
)

val LocalSemanticCodeColors = staticCompositionLocalOf {
    SemanticCodeColors(
        jsonKey = CyanPrimaryDark,
        jsonBooleanTrue = EmeraldAccentDark,
        jsonBooleanFalse = AmberAccentDark,
        jsonString = EmeraldAccentDark,
        jsonNumber = AmberAccentDark,
        jsonPunctuation = TextSecondaryDark,
        modifiedBadge = AmberAccentDark
    )
}

private val DarkColors = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = CyanOnPrimaryDark,
    primaryContainer = CyanContainerDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = EmeraldAccentDark,
    onSecondary = CyanOnPrimaryDark,
    tertiary = AmberAccentDark,
    background = SlateBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateSurfaceElevatedDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateOutlineDark,
    error = RoseErrorDark,
    onError = CyanOnPrimaryDark
)

private val LightColors = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = CyanOnPrimaryLight,
    primaryContainer = CyanContainerLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = EmeraldAccentLight,
    onSecondary = CyanOnPrimaryLight,
    tertiary = AmberAccentLight,
    background = SlateBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SlateSurfaceElevatedLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = SlateOutlineLight,
    error = RoseErrorLight,
    onError = CyanOnPrimaryLight
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun VSCodeConfigTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val codeColors = if (darkTheme) {
        SemanticCodeColors(
            jsonKey = CyanPrimaryDark,
            jsonBooleanTrue = EmeraldAccentDark,
            jsonBooleanFalse = AmberAccentDark,
            jsonString = Color(0xFFA5F3FC),
            jsonNumber = Color(0xFFFDE68A),
            jsonPunctuation = TextSecondaryDark,
            modifiedBadge = AmberAccentDark
        )
    } else {
        SemanticCodeColors(
            jsonKey = CyanPrimaryLight,
            jsonBooleanTrue = EmeraldAccentLight,
            jsonBooleanFalse = AmberAccentLight,
            jsonString = Color(0xFF0E7490),
            jsonNumber = Color(0xFFB45309),
            jsonPunctuation = TextSecondaryLight,
            modifiedBadge = AmberAccentLight
        )
    }

    CompositionLocalProvider(LocalSemanticCodeColors provides codeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
