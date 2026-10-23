package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ClinicalTealLight,
    onPrimary = Color(0xFF00383D),
    primaryContainer = ClinicalTealDark,
    onPrimaryContainer = ClinicalTealLight,
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF0D47A1),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

private val LightColorScheme = lightColorScheme(
    primary = ClinicalTealPrimary,
    onPrimary = Color.White,
    primaryContainer = ClinicalTealContainer,
    onPrimaryContainer = OnClinicalTealContainer,
    secondary = MedicalBlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = MedicalBlueContainer,
    onSecondaryContainer = OnMedicalBlueContainer,
    tertiary = ClinicAccentCoral,
    background = ClinicBgLight,
    surface = ClinicSurfaceLight,
    surfaceVariant = ClinicSurfaceVariant,
    outline = ClinicOutline,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun KlinikRomanaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KlinikRomanaTheme(darkTheme = darkTheme, content = content)
}
