package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    data object Add : Screen("add", "Add", Icons.Filled.Add)
    data object History : Screen("history", "History", Icons.Filled.History)
    data object Recurring : Screen("recurring", "Recurring", Icons.Filled.Repeat)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        // Add is reached via the Scaffold's FAB instead of a tab, so the bar
        // reads as four peer destinations rather than cramming a verb in
        // next to three nouns.
        val bottomNavItems = listOf(Dashboard, History, Recurring, Settings)
    }
}
