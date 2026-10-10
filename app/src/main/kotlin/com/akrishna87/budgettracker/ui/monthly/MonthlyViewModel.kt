package com.akrishna87.budgettracker.ui.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthKeyOfDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthlyTaskRow(
    val category: CategoryEntity,
    val checked: Boolean,
    val amount: Double
)

data class MonthlyUiState(
    val selectedMonth: String = currentMonthKey(),
    val rows: List<MonthlyTaskRow> = emptyList()
)

class MonthlyViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val selectedMonth = MutableStateFlow(currentMonthKey())

    val uiState: StateFlow<MonthlyUiState> = selectedMonth.flatMapLatest { month ->
        combine(
            repository.observeCategories(),
            repository.observeMonthlyChecks(month),
            repository.observeTransactions()
        ) { categories, checks, transactions ->
            val checkedIds = checks.map { it.categoryId }.toSet()
            val amountsByCategoryId = transactions
                .filter { monthKeyOfDate(it.date) == month }
                .groupBy { it.categoryId }
                .mapValues { (_, items) -> items.sumOf { it.amount } }
            MonthlyUiState(
                selectedMonth = month,
                rows = categories
                    .sortedBy { it.sortOrder }
                    .map { category ->
                        MonthlyTaskRow(category, category.id in checkedIds, amountsByCategoryId[category.id] ?: 0.0)
                    }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyUiState()
    )

    fun selectMonth(month: String) {
        selectedMonth.value = month
    }

    fun setChecked(categoryId: String, checked: Boolean) {
        viewModelScope.launch {
            repository.setMonthlyChecked(categoryId, selectedMonth.value, checked)
        }
    }
}
