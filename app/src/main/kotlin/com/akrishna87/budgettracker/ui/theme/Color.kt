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
    // Softened from a strong blue to a cute periwinkle to match the pastel
    // background direction, while still reading clearly as its own accent.
    accentSecondary = Color(0xFF8E97E0),
    // A warm mauve-gray instead of a cool slate gray, so secondary text
    // belongs to the same cute, warm palette as the backgrounds.
    muted = Color(0xFF8C7680),
    surfaceVariant = Color(0xFFFCE9F0),
    surfaceWell = Color(0xFFF3EAF8),
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

/**
 * Light palette's base roles: a soft, warm pastel look (cream background,
 * white cards, warm near-black text) rather than the cool near-white/near-
 * black pairing this started as.
 */
val LightBackground = Color(0xFFFFF6EE)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF2E2630)

/** The brand accent reads the same, bright, in both themes. */
val Accent = Color(0xFF2FB583)
