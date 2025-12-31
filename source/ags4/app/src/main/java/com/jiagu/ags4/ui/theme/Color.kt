package com.jiagu.ags4.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

var BlackAlpha = Color(0x4D000000)
var LightDarkAlpha = Color(0x1A000000)
var DarkAlpha = Color(0xBF000000)
var VeryDarkAlpha = Color(0xF2000000)

var WhiteAlpha = Color(0x80FFFFFF)

var buttonGroup = Color(0xFFF2F2F2)
var buttonDisabled = Color(0xFF8C8C8C)
var planParamBoxBg = Color(0xFFEBEBEB)
var paramTitleTextColor = Color(0xCC000000)

var primary = Color(0xFF07913A) // Color(0xFF0069ED)
val onPrimary = Color(0xFFFFFFFF)
val secondary = Color(0xFF008810) // Color(0xFF5DE152)
val onSecondary = Color(0xFFFFFFFF)
val tertiary = Color(0xFFF49D15) // Color(0xFFFFBA65)
val onTertiary = Color(0xFFFFFFFF)
val error = Color(0xFFE12C2C) // Color(0xFFFFB4AC)
val onError = Color(0xFFFFFFFF)
val background = Color(0xFFFAF8FF)
val onBackground = Color(0xFF181B24)
val surface = Color(0xFFFAF8FF)
val onSurface = Color(0xFF181B24)
val surfaceVariant = Color(0xFFDEE2F4)
val onSurfaceVariant = Color(0xFF414655)
val outline = Color(0xFF727787)
val outlineVariant = Color(0xFFC1C6D8)
val scrim = Color(0xFF000000)
val inverseSurface = Color(0xFF2D3039)
val inverseOnSurface = Color(0xFFEFF0FC)
val inversePrimary = Color(0xFFAFC6FF)
val surfaceDim = Color(0xFFD8D9E5)
val surfaceBright = Color(0xFFFAF8FF)
val surfaceContainerLowest = Color(0xFFFFFFFF)
val surfaceContainerLow = Color(0xFFF2F3FF)
val surfaceContainer = Color(0xFFECEDF9)
val surfaceContainerHigh = Color(0xFFE6E7F3)
val surfaceContainerHighest = Color(0xFFE0E2EE)

/*
    Dark Theme
 */

val primaryDark = Color(0xFF0069ED) // Color(0xFFAFC6FF)
val onPrimaryDark = Color(0xFFFFFFFF) // Color(0xFF002D6D)
val primaryContainerDark = Color(0xFF0069ED)
val onPrimaryContainerDark = Color(0xFFFFFFFF)
val secondaryDark = Color(0xFF008810) // Color(0xFF5DE152)
val onSecondaryDark = Color(0xFFFFFFFF) // Color(0xFF003A03)
val secondaryContainerDark = Color(0xFF008810)
val onSecondaryContainerDark = Color(0xFFFFFFFF)
val tertiaryDark = Color(0xFFDE8D00) // Color(0xFFFFBA65)
val onTertiaryDark = Color(0xFF1E0F00) // Color(0xFF472A00)
val tertiaryContainerDark = Color(0xFFDE8D00)
val onTertiaryContainerDark = Color(0xFF1E0F00)
val errorDark = Color(0xFFD52225) // Color(0xFFFFB4AC)
val onErrorDark = Color(0xFFFFFFFF) // Color(0xFF690006)
val errorContainerDark = Color(0xFFD52225)
val onErrorContainerDark = Color(0xFFFFFFFF)
val backgroundDark = Color(0xFF10131B)
val onBackgroundDark = Color(0xFFE0E2EE)
val surfaceDark = Color(0xFF10131B)
val onSurfaceDark = Color(0xFFE0E2EE)
val surfaceVariantDark = Color(0xFF414655)
val onSurfaceVariantDark = Color(0xFFC1C6D8)
val outlineDark = Color(0xFF8C90A1)
val outlineVariantDark = Color(0xFF414655)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE0E2EE)
val inverseOnSurfaceDark = Color(0xFF2D3039)
val inversePrimaryDark = Color(0xFF0058C9)
val surfaceDimDark = Color(0xFF10131B)
val surfaceBrightDark = Color(0xFF363942)
val surfaceContainerLowestDark = Color(0xFF0B0E16)
val surfaceContainerLowDark = Color(0xFF181B24)
val surfaceContainerDark = Color(0xFF1C1F28)
val surfaceContainerHighDark = Color(0xFF272A32)
val surfaceContainerHighestDark = Color(0xFF32353E)

@Composable
fun textFieldDefaultsColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun textFieldErrorColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.error,
        unfocusedTextColor = MaterialTheme.colorScheme.error,
        focusedContainerColor = MaterialTheme.colorScheme.onError,
        unfocusedContainerColor = MaterialTheme.colorScheme.onError,
        focusedIndicatorColor = MaterialTheme.colorScheme.error,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.error
    )
}

@Composable
fun textFieldOnPrimaryColors(): TextFieldColors {
    return TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.primary,
        unfocusedTextColor = MaterialTheme.colorScheme.primary,
        focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
        focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
fun miniTextFieldStyle(): TextStyle {
    return TextStyle(
        color = Color.Black, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun getDefaultButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        contentColor = MaterialTheme.colorScheme.onBackground,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun getPrimaryButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = buttonDisabled
    )
}

@Composable
fun getMainButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = BlackAlpha,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = buttonDisabled
    )
}
