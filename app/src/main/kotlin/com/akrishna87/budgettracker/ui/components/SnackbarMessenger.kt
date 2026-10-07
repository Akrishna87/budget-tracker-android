package com.akrishna87.budgettracker.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

/**
 * Provided once at the nav host's Scaffold so any screen can offer
 * "Deleted. Undo" feedback without threading a callback through every
 * screen's parameter list.
 */
val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}
