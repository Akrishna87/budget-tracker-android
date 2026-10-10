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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val totalLoanOutstanding: Double = 0.0
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

    val uiState: StateFlow<DashboardUiState> = combine(
        selectedMonth,
        repository.observeTransactions(),
        repository.observeCategories()
    ) { month, transactions, categories ->
        val monthTransactions = transactions.filter { monthKeyOfDate(it.date) == month }
        val incomeTransactions = monthTransactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = monthTransactions.filter { it.type == TransactionType.EXPENSE }
        val investmentTransactions = expenseTransactions.filter {
            categories.find { c -> c.id == it.categoryId }?.isInvestment == true
        }
        val plainExpenseTransactions = expenseTransactions.filter {
            categories.find { c -> c.id == it.categoryId }?.isInvestment != true
        }

        // A category used only for logging income (never an expense, across
        // all history, not just this month) has no place in a spending
        // breakdown - it'd only ever show ₹0 there since this panel is built
        // from expense activity. The INCOME stat above already covers it.
        val categoryIdsEverExpensed = transactions.filter { it.type == TransactionType.EXPENSE }.mapNotNull { it.categoryId }.toSet()
        val categoryIdsEverIncome = transactions.filter { it.type == TransactionType.INCOME }.mapNotNull { it.categoryId }.toSet()
        val incomeOnlyCategoryIds = categoryIdsEverIncome - categoryIdsEverExpensed
        val spendCategories = categories.filterNot { it.id in incomeOnlyCategoryIds }

        DashboardUiState(
            selectedMonth = month,
            totalIncome = incomeTransactions.sumOf { it.amount },
            totalExpense = expenseTransactions.sumOf { it.amount },
            totalInvestment = investmentTransactions.sumOf { it.amount },
            totalPlainExpense = plainExpenseTransactions.sumOf { it.amount },
            // Every spending category (investment or plain expense alike) in
            // one flat list keyed to how much moved through it this month -
            // a single simple "Categories" section instead of three separate
            // by-type panels.
            categoryTotals = totalsByCategory(expenseTransactions, spendCategories)
        )
    }.combine(repository.observeLoans()) { state, loans ->
        state.copy(loans = loans, totalLoanOutstanding = loans.sumOf { it.outstandingAmount })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun selectMonth(month: String) {
        selectedMonth.value = month
    }
}
