package com.akrishna87.budgettracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Accessor mirroring `MaterialTheme` for the semantic colors ColorScheme has no slot for. */
object BudgetTheme {
    val colors: BudgetColors
        @Composable get() = LocalBudgetColors.current
}

private val DarkScheme = darkColorScheme(
    primary = DarkBudgetColors.income,
    onPrimary = DarkBudgetColors.accentOnColor,
    secondary = DarkBudgetColors.expense,
    onSecondary = Color(0xFF2B1206),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkBudgetColors.surfaceVariant,
    onSurfaceVariant = DarkBudgetColors.muted,
    error = DarkBudgetColors.danger,
    onError = Color(0xFF2C0A0A)
)

private val LightScheme = lightColorScheme(
    primary = LightBudgetColors.income,
    onPrimary = Color(0xFFFFFFFF),
    secondary = LightBudgetColors.expense,
    onSecondary = Color(0xFFFFFFFF),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightBudgetColors.surfaceVariant,
    onSurfaceVariant = LightBudgetColors.muted,
    error = LightBudgetColors.danger,
    onError = Color(0xFFFFFFFF)
)

@Composable
fun BudgetTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalBudgetColors provides if (darkTheme) DarkBudgetColors else LightBudgetColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = BudgetTypography,
            content = content
        )
    }
}
