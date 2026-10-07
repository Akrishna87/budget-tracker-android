package com.akrishna87.budgettracker.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthKeyOfDate
import com.akrishna87.budgettracker.util.shiftMonthKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class MonthlyAggregate(val monthKey: String, val income: Double, val expense: Double) {
    val net: Double get() = income - expense
}

data class TrendsUiState(
    val recentMonths: List<MonthlyAggregate> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val yearMonths: List<MonthlyAggregate> = emptyList(),
    val yearIncome: Double = 0.0,
    val yearExpense: Double = 0.0
) {
    val yearNet: Double get() = yearIncome - yearExpense
}

class TrendsViewModel(repository: BudgetRepository) : ViewModel() {

    private val selectedYear = MutableStateFlow(LocalDate.now().year)

    val uiState: StateFlow<TrendsUiState> = combine(
        repository.observeTransactions(),
        selectedYear
    ) { transactions, year ->
        val byMonth: Map<String, List<TransactionEntity>> = transactions.groupBy { monthKeyOfDate(it.date) }

        fun aggregateFor(monthKey: String): MonthlyAggregate {
            val items = byMonth[monthKey].orEmpty()
            val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            return MonthlyAggregate(monthKey, income, expense)
        }

        val currentMonth = currentMonthKey()
        val recentMonths = (5 downTo 0).map { offset -> aggregateFor(shiftMonthKey(currentMonth, -offset)) }

        val yearMonths = (1..12).map { month -> aggregateFor("%04d-%02d".format(year, month)) }

        TrendsUiState(
            recentMonths = recentMonths,
            selectedYear = year,
            yearMonths = yearMonths,
            yearIncome = yearMonths.sumOf { it.income },
            yearExpense = yearMonths.sumOf { it.expense }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrendsUiState())

    fun selectYear(year: Int) {
        selectedYear.value = year
    }
}
