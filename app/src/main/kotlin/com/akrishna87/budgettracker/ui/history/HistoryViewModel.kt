package com.akrishna87.budgettracker.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.monthKeyOfDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TypeFilter { ALL, INCOME, EXPENSE }

data class HistoryFilter(
    val type: TypeFilter = TypeFilter.ALL,
    val categoryId: String? = null,
    val month: String? = null,
    val search: String = "",
    /** null = no constraint; true/false = only entries whose category is/isn't marked as an investment. */
    val investmentFilter: Boolean? = null
)

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter(),
    val categories: List<CategoryEntity> = emptyList(),
    val subcategories: List<SubcategoryEntity> = emptyList(),
    val availableMonths: List<String> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)

class HistoryViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val filter = MutableStateFlow(HistoryFilter())

    val uiState: StateFlow<HistoryUiState> = combine(
        filter,
        repository.observeTransactions(),
        repository.observeCategories(),
        repository.observeSubcategories()
    ) { currentFilter, transactions, categories, subcategories ->
        val months = transactions.map { monthKeyOfDate(it.date) }.distinct().sortedDescending()
        val filtered = transactions.filter { tx ->
            (currentFilter.type == TypeFilter.ALL ||
                (currentFilter.type == TypeFilter.INCOME && tx.type == TransactionType.INCOME) ||
                (currentFilter.type == TypeFilter.EXPENSE && tx.type == TransactionType.EXPENSE)) &&
                (currentFilter.categoryId == null || tx.categoryId == currentFilter.categoryId) &&
                (currentFilter.month == null || monthKeyOfDate(tx.date) == currentFilter.month) &&
                (currentFilter.search.isBlank() || tx.note.contains(currentFilter.search, ignoreCase = true)) &&
                (currentFilter.investmentFilter == null ||
                    (categories.find { it.id == tx.categoryId }?.isInvestment ?: false) == currentFilter.investmentFilter)
        }.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.createdAt })

        HistoryUiState(
            filter = currentFilter,
            categories = categories,
            subcategories = subcategories,
            availableMonths = months,
            transactions = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setTypeFilter(type: TypeFilter) {
        filter.value = filter.value.copy(type = type)
    }

    fun setCategoryFilter(categoryId: String?) {
        filter.value = filter.value.copy(categoryId = categoryId)
    }

    fun setMonthFilter(month: String?) {
        filter.value = filter.value.copy(month = month)
    }

    fun setInvestmentFilter(investmentFilter: Boolean?) {
        filter.value = filter.value.copy(investmentFilter = investmentFilter)
    }

    fun setSearch(search: String) {
        filter.value = filter.value.copy(search = search)
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch { repository.deleteTransaction(id) }
    }

    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.upsertTransaction(transaction) }
    }
}
