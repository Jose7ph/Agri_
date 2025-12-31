package com.jiagu.ags4.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jiagu.ags4.repo.sp.AppConfig

@Composable
fun ComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
//    val view = LocalView.current
    var primaryColor = remember { AppConfig(context).primaryColor }
    LaunchedEffect(key1 = AppConfig(context).primaryColor) {
        primaryColor = AppConfig(context).primaryColor
    }
    var colorScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = onPrimary,
        primaryContainer = primaryColor,
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
        content = content
    )
}