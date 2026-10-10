package com.akrishna87.budgettracker.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.launch

private const val TRANSITION_MS = 220
private const val MAIN_ROUTE = "main"

@Composable
fun BudgetNavHost(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = factory)
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()

    // Dashboard, History, Recurring and Settings live as pages of one pager
    // (hoisted here, outside the "add" push/pop, so swiping between them
    // carries no NavHost transition at all - it's just the pager's own drag,
    // which is what makes it swipeable in the first place) rather than as
    // separate NavHost destinations.
    val pagerState = rememberPagerState(pageCount = { Screen.bottomNavItems.size })
    val historyPageIndex = Screen.bottomNavItems.indexOf(Screen.History)

    // Set by Dashboard's "tap a category" action and consumed once History
    // opens, so tapping a category jumps straight to its individual entries
    // without threading a nav argument through the route string.
    var pendingHistoryCategoryId by remember { mutableStateOf<String?>(null) }

    val currentDestination = backStackEntry?.destination
    val isOnMain = currentDestination?.route == MAIN_ROUTE
    val currentTitle = if (isOnMain) Screen.bottomNavItems[pagerState.currentPage].label else Screen.Add.label
    val onBack: (() -> Unit)? = if (isOnMain) null else { { navController.popBackStack() } }

    Scaffold(
        topBar = {
            BrandTopBar(
                title = currentTitle,
                onBack = onBack
            )
        },
        bottomBar = {
            if (isOnMain) {
                NavigationBar {
                    Screen.bottomNavItems.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
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
            if (isOnMain) {
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
            startDestination = MAIN_ROUTE,
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
            composable(MAIN_ROUTE) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (Screen.bottomNavItems[page]) {
                        Screen.Dashboard -> {
                            val viewModel: DashboardViewModel = viewModel(factory = factory)
                            DashboardScreen(
                                viewModel = viewModel,
                                onOpenCategory = { categoryId ->
                                    pendingHistoryCategoryId = categoryId
                                    scope.launch { pagerState.animateScrollToPage(historyPageIndex) }
                                }
                            )
                        }
                        Screen.History -> {
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
                        Screen.Recurring -> {
                            val viewModel: RecurringViewModel = viewModel(factory = factory)
                            RecurringScreen(viewModel)
                        }
                        Screen.Settings -> {
                            val viewModel: SettingsViewModel = viewModel(factory = factory)
                            SettingsScreen(viewModel)
                        }
                        else -> Unit
                    }
                }
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(
                    viewModel = addTransactionViewModel,
                    onSaved = {
                        navController.popBackStack()
                        scope.launch { pagerState.scrollToPage(historyPageIndex) }
                    }
                )
            }
        }
        }
    }
}
