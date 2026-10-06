package com.akrishna87.budgettracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BudgetColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = AccentOnColor,
    secondary = Expense,
    onSecondary = Color(0xFF3A1400),
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Muted,
    error = Danger,
    onError = Color(0xFF2C0A0A)
)

@Composable
fun BudgetTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The app is designed dark-first (matches the rest of this author's
    // finance apps); light mode just reuses the same scheme for now.
    MaterialTheme(
        colorScheme = BudgetColorScheme,
        typography = BudgetTypography,
        content = content
    )
}
