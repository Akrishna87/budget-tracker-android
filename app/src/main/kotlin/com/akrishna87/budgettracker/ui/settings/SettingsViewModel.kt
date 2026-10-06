package com.akrishna87.budgettracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
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

    fun addCategory(name: String, emoji: String, budget: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (categories.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.upsertCategory(
                CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    emoji = emoji.ifBlank { "⭐" },
                    budget = budget,
                    sortOrder = nextOrder
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity, name: String, emoji: String, budget: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.upsertCategory(category.copy(name = name.trim(), emoji = emoji.ifBlank { "⭐" }, budget = budget))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }
}
