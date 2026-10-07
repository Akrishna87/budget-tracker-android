package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    data object Add : Screen("add", "Add", Icons.Filled.Add)
    data object History : Screen("history", "History", Icons.Filled.History)
    data object Bills : Screen("bills", "Bills", Icons.Filled.Receipt)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    // Reached from Dashboard shortcuts rather than the bottom bar, to keep that
    // bar from growing past five items as more features are added.
    data object Debts : Screen("debts", "Debts & Credit Cards", Icons.Filled.CreditCard)
    data object Goals : Screen("goals", "Savings Goals", Icons.Filled.Savings)
    data object Trends : Screen("trends", "Trends", Icons.Filled.TrendingUp)

    companion object {
        val bottomNavItems = listOf(Dashboard, Add, History, Bills, Settings)
    }
}
