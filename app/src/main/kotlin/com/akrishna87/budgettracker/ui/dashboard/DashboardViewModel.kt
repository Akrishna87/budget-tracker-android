package com.akrishna87.budgettracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthKeyOfDate
import com.akrishna87.budgettracker.util.todayKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CategorySpend(
    val category: CategoryEntity,
    val spent: Double
)

data class DashboardUiState(
    val selectedMonth: String = currentMonthKey(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categorySpend: List<CategorySpend> = emptyList(),
    val mostRecentExpense: TransactionEntity? = null,
    val justRepeatedId: String? = null,
    val totalOutstandingDebt: Double = 0.0,
    val savingsGoalsCount: Int = 0,
    val totalSaved: Double = 0.0
) {
    val net: Double get() = totalIncome - totalExpense
}

class DashboardViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val selectedMonth = MutableStateFlow(currentMonthKey())
    private val justRepeatedId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        selectedMonth,
        repository.observeTransactions(),
        repository.observeCategories(),
        repository.observeMostRecentExpense(),
        justRepeatedId
    ) { month, transactions, categories, mostRecent, repeatedId ->
        val monthTransactions = transactions.filter { monthKeyOfDate(it.date) == month }
        val income = monthTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = monthTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val spendByCategory = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        val categorySpend = spendByCategory.entries
            .mapNotNull { (categoryId, spent) ->
                val category = categories.find { it.id == categoryId } ?: return@mapNotNull null
                CategorySpend(category, spent)
            }
            .sortedByDescending { it.spent }

        DashboardUiState(
            selectedMonth = month,
            totalIncome = income,
            totalExpense = expense,
            categorySpend = categorySpend,
            mostRecentExpense = mostRecent,
            justRepeatedId = repeatedId
        )
    }.combine(repository.observeDebts()) { state, debts ->
        state.copy(totalOutstandingDebt = debts.sumOf { it.outstandingAmount })
    }.combine(repository.observeSavingsGoals()) { state, goals ->
        state.copy(savingsGoalsCount = goals.size, totalSaved = goals.sumOf { it.savedAmount })
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun selectMonth(month: String) {
        selectedMonth.value = month
    }

    fun repeatLastExpense() {
        val last = uiState.value.mostRecentExpense ?: return
        viewModelScope.launch {
            val entry = last.copy(id = UUID.randomUUID().toString(), date = todayKey())
            repository.upsertTransaction(entry)
            justRepeatedId.value = entry.id
        }
    }

    fun undoRepeat(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            justRepeatedId.value = null
        }
    }
}
