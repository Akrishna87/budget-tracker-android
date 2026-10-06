package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.ui.BudgetViewModelFactory
import com.akrishna87.budgettracker.ui.addtransaction.AddTransactionScreen
import com.akrishna87.budgettracker.ui.addtransaction.AddTransactionViewModel
import com.akrishna87.budgettracker.ui.bills.BillsScreen
import com.akrishna87.budgettracker.ui.bills.BillsViewModel
import com.akrishna87.budgettracker.ui.components.BrandTopBar
import com.akrishna87.budgettracker.ui.dashboard.DashboardScreen
import com.akrishna87.budgettracker.ui.dashboard.DashboardViewModel
import com.akrishna87.budgettracker.ui.history.HistoryScreen
import com.akrishna87.budgettracker.ui.history.HistoryViewModel
import com.akrishna87.budgettracker.ui.settings.SettingsScreen
import com.akrishna87.budgettracker.ui.settings.SettingsViewModel
import com.akrishna87.budgettracker.ui.theme.Accent

@Composable
fun BudgetNavHost(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = factory)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTitle = Screen.bottomNavItems
        .firstOrNull { screen -> currentDestination?.hierarchy?.any { it.route == screen.route } == true }
        ?.label ?: Screen.Dashboard.label

    Scaffold(
        topBar = { BrandTopBar(title = currentTitle) },
        bottomBar = {
            NavigationBar {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (screen == Screen.Add) addTransactionViewModel.startNew()
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Accent,
                            selectedTextColor = Accent,
                            indicatorColor = Accent.copy(alpha = 0.16f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(viewModel)
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(
                    viewModel = addTransactionViewModel,
                    onSaved = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(
                    viewModel = viewModel,
                    onEdit = { transaction: TransactionEntity ->
                        addTransactionViewModel.loadForEdit(transaction)
                        navController.navigate(Screen.Add.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Bills.route) {
                val viewModel: BillsViewModel = viewModel(factory = factory)
                BillsScreen(viewModel)
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel)
            }
        }
    }
}
