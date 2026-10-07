package com.akrishna87.budgettracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.security.SecurityPrefs
import com.akrishna87.budgettracker.ui.addtransaction.AddTransactionViewModel
import com.akrishna87.budgettracker.ui.bills.BillsViewModel
import com.akrishna87.budgettracker.ui.dashboard.DashboardViewModel
import com.akrishna87.budgettracker.ui.debts.DebtsViewModel
import com.akrishna87.budgettracker.ui.goals.GoalsViewModel
import com.akrishna87.budgettracker.ui.history.HistoryViewModel
import com.akrishna87.budgettracker.ui.settings.SettingsViewModel
import com.akrishna87.budgettracker.ui.theme.ThemeController
import com.akrishna87.budgettracker.ui.trends.TrendsViewModel

class BudgetViewModelFactory(
    private val repository: BudgetRepository,
    private val securityPrefs: SecurityPrefs,
    private val themeController: ThemeController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(repository, securityPrefs, themeController) as T
            modelClass.isAssignableFrom(BillsViewModel::class.java) ->
                BillsViewModel(repository) as T
            modelClass.isAssignableFrom(DebtsViewModel::class.java) ->
                DebtsViewModel(repository) as T
            modelClass.isAssignableFrom(GoalsViewModel::class.java) ->
                GoalsViewModel(repository) as T
            modelClass.isAssignableFrom(TrendsViewModel::class.java) ->
                TrendsViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
