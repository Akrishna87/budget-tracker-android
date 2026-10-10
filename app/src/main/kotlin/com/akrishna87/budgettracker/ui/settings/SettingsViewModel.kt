package com.akrishna87.budgettracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.LoanEntity
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .map { it.sortedBy { c -> c.sortOrder } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subcategories: StateFlow<List<SubcategoryEntity>> = repository.observeSubcategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loans: StateFlow<List<LoanEntity>> = repository.observeLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stashed on delete and replayed on Undo, since deleting a category also
    // deletes its subcategories (deleteCategory() cascades that cleanup) -
    // without this, undoing a category delete would bring the category back
    // but silently lose its subcategories.
    private val pendingRestoreSubcategories = mutableMapOf<String, List<SubcategoryEntity>>()

    fun addCategory(name: String, budget: Double, isInvestment: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (categories.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.upsertCategory(
                CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    emoji = "",
                    budget = budget,
                    isInvestment = isInvestment,
                    sortOrder = nextOrder
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity, name: String, budget: Double, isInvestment: Boolean) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.upsertCategory(category.copy(name = name.trim(), budget = budget, isInvestment = isInvestment))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            pendingRestoreSubcategories[category.id] = subcategories.value.filter { it.categoryId == category.id }
            repository.deleteCategory(category)
        }
    }

    fun restoreCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.upsertCategory(category)
            pendingRestoreSubcategories.remove(category.id)?.forEach { repository.upsertSubcategory(it) }
        }
    }

    fun addSubcategory(categoryId: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (subcategories.value.filter { it.categoryId == categoryId }.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.upsertSubcategory(
                SubcategoryEntity(
                    id = UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    name = name.trim(),
                    sortOrder = nextOrder
                )
            )
        }
    }

    fun deleteSubcategory(subcategory: SubcategoryEntity) {
        viewModelScope.launch { repository.deleteSubcategory(subcategory) }
    }

    fun addLoan(name: String, outstandingAmount: Double, monthlyPayment: Double) {
        if (name.isBlank() || outstandingAmount <= 0.0) return
        viewModelScope.launch {
            val nextOrder = (loans.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.upsertLoan(
                LoanEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    outstandingAmount = outstandingAmount,
                    monthlyPayment = monthlyPayment,
                    sortOrder = nextOrder
                )
            )
        }
    }

    fun updateLoan(loan: LoanEntity, name: String, outstandingAmount: Double, monthlyPayment: Double) {
        if (name.isBlank() || outstandingAmount <= 0.0) return
        viewModelScope.launch {
            repository.upsertLoan(loan.copy(name = name.trim(), outstandingAmount = outstandingAmount, monthlyPayment = monthlyPayment))
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch { repository.deleteLoan(loan) }
    }

    fun restoreLoan(loan: LoanEntity) {
        viewModelScope.launch { repository.upsertLoan(loan) }
    }
}
