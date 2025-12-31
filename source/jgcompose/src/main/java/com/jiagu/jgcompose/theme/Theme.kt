package com.jiagu.jgcompose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

// 扩展 MaterialTheme
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current

@Composable
fun ComposeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondary,
        onSecondaryContainer = onSecondary,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiary,
        onTertiaryContainer = onTertiary,
        error = error,
        onError = onError,
        errorContainer = error,
        onErrorContainer = onError,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        surfaceDim = surfaceDim,
        surfaceBright = surfaceBright,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest

    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = {
            content()
        }
    )
}