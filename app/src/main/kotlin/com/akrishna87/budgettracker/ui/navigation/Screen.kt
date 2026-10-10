package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    data object Add : Screen("add", "Add", Icons.Filled.Add)
    data object History : Screen("history", "History", Icons.Filled.History)
    data object Monthly : Screen("monthly", "Checklist", Icons.Filled.Checklist)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        // Add is reached via the Scaffold's FAB instead of a tab, so the bar
        // reads as four peer destinations rather than cramming a verb in
        // next to the nouns. Recurring entries are now created inline on Add
        // (a "Recurring" checkbox) rather than through a dedicated tab.
        val bottomNavItems = listOf(Dashboard, History, Monthly, Settings)
    }
}
