package com.akrishna87.budgettracker.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors Material3's ColorScheme has no slot for (income/expense,
 * muted text, the inset "well" surface, ...). Dark and light need distinct
 * values, not just a background swap: the dark palette's bright accents
 * read fine on a near-black surface but fail text-contrast on a near-white
 * one, so light mode uses deepened versions of the same hues rather than
 * reusing the dark values.
 */
data class BudgetColors(
    val income: Color,
    val expense: Color,
    val danger: Color,
    val warn: Color,
    val accentSecondary: Color,
    val muted: Color,
    val surfaceVariant: Color,
    val surfaceWell: Color,
    val accentOnColor: Color
)

val DarkBudgetColors = BudgetColors(
    income = Color(0xFF3DDC97),
    expense = Color(0xFFFF5C5C),
    danger = Color(0xFFFF5C5C),
    warn = Color(0xFFF5B942),
    accentSecondary = Color(0xFF5B9DF9),
    muted = Color(0xFF8A91A3),
    surfaceVariant = Color(0xFF1B212C),
    surfaceWell = Color(0xFF0F131A),
    accentOnColor = Color(0xFF06241A)
)

val LightBudgetColors = BudgetColors(
    income = Color(0xFF1C8F64),
    expense = Color(0xFFC13B33),
    danger = Color(0xFFC13B33),
    warn = Color(0xFF8A5E10),
    accentSecondary = Color(0xFF2E6FC9),
    muted = Color(0xFF5B6272),
    surfaceVariant = Color(0xFFEFF1F6),
    surfaceWell = Color(0xFFE9ECF2),
    // Same dark-on-accent pairing as the dark theme: the brand accent is a
    // mid-tone green that contrasts far better with dark text than white
    // text in either theme, so this isn't a per-theme choice.
    accentOnColor = Color(0xFF06241A)
)

val LocalBudgetColors = staticCompositionLocalOf { DarkBudgetColors }

/** Dark palette's base roles (background/surface/text), fed to MaterialTheme's ColorScheme. */
val DarkBackground = Color(0xFF0B0E14)
val DarkSurface = Color(0xFF151922)
val DarkOnBackground = Color(0xFFF2F4F8)

/** Light palette's base roles. */
val LightBackground = Color(0xFFF7F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF14161C)

/** The brand accent reads the same, bright, in both themes. */
val Accent = Color(0xFF2FB583)
