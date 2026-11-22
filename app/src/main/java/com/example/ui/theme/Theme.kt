package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class CytoExtendedColors(
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
    val onHeroMuted: Color,
    val statusReady: Color,
    val statusReadyContainer: Color,
    val statusWarning: Color,
    val statusWarningContainer: Color,
    val cardElevated: Color
)

private val LightExtendedColors = CytoExtendedColors(
    heroStart = HeroGradientStartLight,
    heroEnd = HeroGradientEndLight,
    onHero = Color(0xFFFFFFFF),
    onHeroMuted = Color(0xFFC8ECE8),
    statusReady = StatusReadyLight,
    statusReadyContainer = StatusReadyContainerLight,
    statusWarning = StatusWarningLight,
    statusWarningContainer = StatusWarningContainerLight,
    cardElevated = Color(0xFFFFFFFF)
)

private val DarkExtendedColors = CytoExtendedColors(
    heroStart = HeroGradientStartDark,
    heroEnd = HeroGradientEndDark,
    onHero = Color(0xFFF0FDFB),
    onHeroMuted = Color(0xFF9ADAD2),
    statusReady = StatusReadyDark,
    statusReadyContainer = StatusReadyContainerDark,
    statusWarning = StatusWarningDark,
    statusWarningContainer = StatusWarningContainerDark,
    cardElevated = Color(0xFF162428)
)

val LocalCytoColors = staticCompositionLocalOf { LightExtendedColors }

private val LightColorScheme = lightColorScheme(
    primary = BioTealPrimary,
    onPrimary = BioTealOnPrimary,
    primaryContainer = BioTealPrimaryContainer,
    onPrimaryContainer = BioTealOnPrimaryContainer,
    secondary = BioCyanSecondary,
    onSecondary = BioCyanOnSecondary,
    secondaryContainer = BioCyanSecondaryContainer,
    onSecondaryContainer = BioCyanOnSecondaryContainer,
    tertiary = TrypanCobaltTertiary,
    onTertiary = TrypanCobaltOnTertiary,
    tertiaryContainer = TrypanCobaltContainer,
    onTertiaryContainer = TrypanCobaltOnTertiaryContainer,
    background = LabPorcelainBackground,
    onBackground = LabInkOnBackground,
    surface = LabSurface,
    onSurface = LabInkOnBackground,
    surfaceVariant = LabSurfaceVariant,
    onSurfaceVariant = LabOnSurfaceVariant,
    outline = LabOutline,
    outlineVariant = LabOutlineVariant,
    error = LabError,
    onError = LabOnError,
    errorContainer = LabErrorContainer,
    onErrorContainer = LabOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBioCyanPrimary,
    onPrimary = DarkBioCyanOnPrimary,
    primaryContainer = DarkBioCyanPrimaryContainer,
    onPrimaryContainer = DarkBioCyanOnPrimaryContainer,
    secondary = DarkMintSecondary,
    onSecondary = DarkMintOnSecondary,
    secondaryContainer = DarkMintSecondaryContainer,
    onSecondaryContainer = DarkMintOnSecondaryContainer,
    tertiary = DarkTrypanTertiary,
    onTertiary = DarkTrypanOnTertiary,
    tertiaryContainer = DarkTrypanContainer,
    onTertiaryContainer = DarkTrypanOnTertiaryContainer,
    background = DarkLabBackground,
    onBackground = DarkLabOnBackground,
    surface = DarkLabSurface,
    onSurface = DarkLabOnBackground,
    surfaceVariant = DarkLabSurfaceVariant,
    onSurfaceVariant = DarkLabOnSurfaceVariant,
    outline = DarkLabOutline,
    outlineVariant = DarkLabOutlineVariant,
    error = DarkLabError,
    onError = DarkLabOnError,
    errorContainer = DarkLabErrorContainer,
    onErrorContainer = DarkLabOnErrorContainer
)

val CytoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CytoCountTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalCytoColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CytoTypography,
            shapes = CytoShapes,
            content = content
        )
    }
}
