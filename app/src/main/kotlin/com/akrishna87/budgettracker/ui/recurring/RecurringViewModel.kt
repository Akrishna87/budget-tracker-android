package com.akrishna87.budgettracker.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.RecurringExpenseEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class RecurringItem(
    val expense: RecurringExpenseEntity,
    val category: CategoryEntity?
)

data class RecurringUiState(
    val items: List<RecurringItem> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

class RecurringViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<RecurringUiState> = combine(
        repository.observeRecurringExpenses(),
        repository.observeCategories()
    ) { expenses, categories ->
        val items = expenses
            .map { expense -> RecurringItem(expense, categories.find { it.id == expense.categoryId }) }
            .sortedBy { it.expense.dueDayOfMonth }
        RecurringUiState(items = items, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RecurringUiState())

    fun add(name: String, amount: Double, categoryId: String?, dueDayOfMonth: Int) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertRecurringExpense(
                RecurringExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amount = amount,
                    categoryId = categoryId,
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 28)
                )
            )
        }
    }

    fun update(expense: RecurringExpenseEntity, name: String, amount: Double, categoryId: String?, dueDayOfMonth: Int) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertRecurringExpense(
                expense.copy(
                    name = name.trim(),
                    amount = amount,
                    categoryId = categoryId,
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 28)
                )
            )
        }
    }

    fun delete(expense: RecurringExpenseEntity) {
        viewModelScope.launch { repository.deleteRecurringExpense(expense) }
    }

    fun restore(expense: RecurringExpenseEntity) {
        viewModelScope.launch { repository.upsertRecurringExpense(expense) }
    }
}
