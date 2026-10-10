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
    val investmentByCategory: List<CategoryTotal> = emptyList(),
    val expenseByCategory: List<CategoryTotal> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val totalLoanOutstanding: Double = 0.0,
    val mostRecentExpense: TransactionEntity? = null,
    val justRepeatedId: String? = null
) {
    val net: Double get() = totalIncome - totalExpense
}

/**
 * One row per [categories] entry (not per category that happens to have a
 * transaction this month) - so a brand-new category shows up right away at
 * ₹0, and a deleted category disappears on its own next time this recomputes
 * since it's simply no longer in [categories].
 */
private fun totalsByCategory(transactions: List<TransactionEntity>, categories: List<CategoryEntity>): List<CategoryTotal> {
    val amountsByCategoryId = transactions.groupBy { it.categoryId }.mapValues { (_, items) -> items.sumOf { it.amount } }
    return categories
        .map { category -> CategoryTotal(category, amountsByCategoryId[category.id] ?: 0.0) }
        .sortedWith(compareByDescending<CategoryTotal> { it.amount }.thenBy { it.category.sortOrder })
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
        val plainCategories = categories.filter { !it.isInvestment }
        val investmentCategories = categories.filter { it.isInvestment }

        DashboardUiState(
            selectedMonth = month,
            totalIncome = incomeTransactions.sumOf { it.amount },
            totalExpense = expenseTransactions.sumOf { it.amount },
            totalInvestment = investmentTransactions.sumOf { it.amount },
            totalPlainExpense = plainExpenseTransactions.sumOf { it.amount },
            incomeByCategory = totalsByCategory(incomeTransactions, plainCategories),
            investmentByCategory = totalsByCategory(investmentTransactions, investmentCategories),
            expenseByCategory = totalsByCategory(plainExpenseTransactions, plainCategories),
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
