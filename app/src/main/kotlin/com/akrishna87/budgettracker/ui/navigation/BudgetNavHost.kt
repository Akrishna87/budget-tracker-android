package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.akrishna87.budgettracker.ui.components.BrandTopBar
import com.akrishna87.budgettracker.ui.components.LocalSnackbarHostState
import com.akrishna87.budgettracker.ui.dashboard.DashboardScreen
import com.akrishna87.budgettracker.ui.dashboard.DashboardViewModel
import com.akrishna87.budgettracker.ui.history.HistoryScreen
import com.akrishna87.budgettracker.ui.history.HistoryViewModel
import com.akrishna87.budgettracker.ui.recurring.RecurringScreen
import com.akrishna87.budgettracker.ui.recurring.RecurringViewModel
import com.akrishna87.budgettracker.ui.settings.SettingsScreen
import com.akrishna87.budgettracker.ui.settings.SettingsViewModel
import com.akrishna87.budgettracker.ui.theme.Accent

private const val TRANSITION_MS = 220

@Composable
fun BudgetNavHost(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = factory)
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Set by Dashboard's "tap a category" action and consumed once History
    // opens, so tapping a category jumps straight to its individual entries
    // without threading a nav argument through the route string.
    var pendingHistoryCategoryId by remember { mutableStateOf<String?>(null) }

    val currentDestination = backStackEntry?.destination
    val isOnBottomNavRoute = Screen.bottomNavItems.any { currentDestination?.hierarchy?.any { d -> d.route == it.route } == true }
    val currentTitle = (Screen.bottomNavItems + Screen.Add)
        .firstOrNull { screen -> currentDestination?.hierarchy?.any { it.route == screen.route } == true }
        ?.label ?: Screen.Dashboard.label
    val onBack: (() -> Unit)? = if (isOnBottomNavRoute) {
        null
    } else {
        { navController.popBackStack() }
    }

    Scaffold(
        topBar = {
            BrandTopBar(
                title = currentTitle,
                onBack = onBack
            )
        },
        bottomBar = {
            if (isOnBottomNavRoute) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
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
        },
        floatingActionButton = {
            if (isOnBottomNavRoute) {
                FloatingActionButton(
                    onClick = {
                        addTransactionViewModel.startNew()
                        navController.navigate(Screen.Add.route) { launchSingleTop = true }
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add transaction")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(TRANSITION_MS)) + slideInHorizontally(tween(TRANSITION_MS)) { it / 10 }
            },
            exitTransition = { fadeOut(tween(TRANSITION_MS)) },
            popEnterTransition = { fadeIn(tween(TRANSITION_MS)) },
            popExitTransition = {
                fadeOut(tween(TRANSITION_MS)) + slideOutHorizontally(tween(TRANSITION_MS)) { it / 10 }
            }
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenCategory = { categoryId ->
                        pendingHistoryCategoryId = categoryId
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
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
                LaunchedEffect(pendingHistoryCategoryId) {
                    pendingHistoryCategoryId?.let {
                        viewModel.setCategoryFilter(it)
                        pendingHistoryCategoryId = null
                    }
                }
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
            composable(Screen.Recurring.route) {
                val viewModel: RecurringViewModel = viewModel(factory = factory)
                RecurringScreen(viewModel)
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel)
            }
        }
        }
    }
}
