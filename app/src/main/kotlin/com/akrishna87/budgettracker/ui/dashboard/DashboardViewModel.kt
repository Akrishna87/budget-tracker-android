package com.akrishna87.budgettracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.LoanEntity
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthKeyOfDate
import com.akrishna87.budgettracker.util.todayKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CategoryTotal(
    val category: CategoryEntity,
    val amount: Double
)

data class DashboardUiState(
    val selectedMonth: String = currentMonthKey(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalInvestment: Double = 0.0,
    val totalPlainExpense: Double = 0.0,
    val incomeByCategory: List<CategoryTotal> = emptyList(),
    val expenseByCategory: List<CategoryTotal> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val totalLoanOutstanding: Double = 0.0,
    val mostRecentExpense: TransactionEntity? = null,
    val justRepeatedId: String? = null
) {
    val net: Double get() = totalIncome - totalExpense
}

private fun totalsByCategory(transactions: List<TransactionEntity>, categories: List<CategoryEntity>): List<CategoryTotal> {
    return transactions
        .groupBy { it.categoryId }
        .mapNotNull { (categoryId, items) ->
            val category = categories.find { it.id == categoryId } ?: return@mapNotNull null
            CategoryTotal(category, items.sumOf { it.amount })
        }
        .sortedByDescending { it.amount }
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
        val incomeTransactions = monthTransactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = monthTransactions.filter { it.type == TransactionType.EXPENSE }
        val investmentTransactions = expenseTransactions.filter {
            categories.find { c -> c.id == it.categoryId }?.isInvestment == true
        }
        val plainExpenseTransactions = expenseTransactions.filter {
            categories.find { c -> c.id == it.categoryId }?.isInvestment != true
        }

        DashboardUiState(
            selectedMonth = month,
            totalIncome = incomeTransactions.sumOf { it.amount },
            totalExpense = expenseTransactions.sumOf { it.amount },
            totalInvestment = investmentTransactions.sumOf { it.amount },
            totalPlainExpense = plainExpenseTransactions.sumOf { it.amount },
            incomeByCategory = totalsByCategory(incomeTransactions, categories),
            expenseByCategory = totalsByCategory(expenseTransactions, categories),
            mostRecentExpense = mostRecent,
            justRepeatedId = repeatedId
        )
    }.combine(repository.observeLoans()) { state, loans ->
        state.copy(loans = loans, totalLoanOutstanding = loans.sumOf { it.outstandingAmount })
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
