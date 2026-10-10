package com.akrishna87.budgettracker.ui.monthly

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthKeyOfDate
import com.akrishna87.budgettracker.util.monthKeyOfEpochMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthlyBreakdownItem(
    val id: String,
    val label: String,
    val amount: Double,
    val isRecurringPreview: Boolean = false,
    val recurringDueDay: Int? = null
)

data class MonthlyTaskRow(
    val category: CategoryEntity,
    val checked: Boolean,
    val amount: Double,
    val breakdown: List<MonthlyBreakdownItem> = emptyList()
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
            repository.observeSubcategories(),
            repository.observeMonthlyChecks(month),
            repository.observeTransactions(),
            repository.observeRecurringExpenses()
        ) { categories, subcategories, checks, transactions, recurringExpenses ->
            val checkedIds = checks.map { it.categoryId }.toSet()
            val monthTransactions = transactions.filter { monthKeyOfDate(it.date) == month }
            val subcategoriesByCategory = subcategories.groupBy { it.categoryId }

            val rows = categories.sortedBy { it.sortOrder }.map { category ->
                val categoryTransactions = monthTransactions.filter { it.categoryId == category.id }
                val categorySubcategories = subcategoriesByCategory[category.id].orEmpty().sortedBy { it.sortOrder }
                val transactionsBySubcategory = categoryTransactions.groupBy { it.subcategoryId }

                val breakdown = mutableListOf<MonthlyBreakdownItem>()
                if (categorySubcategories.isNotEmpty()) {
                    categorySubcategories.forEach { subcategory ->
                        breakdown += MonthlyBreakdownItem(
                            id = subcategory.id,
                            label = subcategory.name,
                            amount = transactionsBySubcategory[subcategory.id]?.sumOf { it.amount } ?: 0.0
                        )
                    }
                    val uncategorized = transactionsBySubcategory[null]?.sumOf { it.amount } ?: 0.0
                    if (uncategorized > 0) {
                        breakdown += MonthlyBreakdownItem(id = "uncategorized", label = "Uncategorized", amount = uncategorized)
                    }
                }

                var total = categoryTransactions.sumOf { it.amount }

                recurringExpenses.filter { it.categoryId == category.id }.forEach { recurring ->
                    val createdMonth = monthKeyOfEpochMillis(recurring.createdAt)
                    val alreadyLoggedForMonth = recurring.lastGeneratedMonth != null && recurring.lastGeneratedMonth >= month
                    if (month >= createdMonth && !alreadyLoggedForMonth) {
                        total += recurring.amount
                        breakdown += MonthlyBreakdownItem(
                            id = "recurring:${recurring.id}",
                            label = recurring.name,
                            amount = recurring.amount,
                            isRecurringPreview = true,
                            recurringDueDay = recurring.dueDayOfMonth
                        )
                    }
                }

                MonthlyTaskRow(category, category.id in checkedIds, total, breakdown)
            }

            MonthlyUiState(selectedMonth = month, rows = rows)
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
