package com.akrishna87.budgettracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.security.PinHashing
import com.akrishna87.budgettracker.security.SecurityPrefs
import com.akrishna87.budgettracker.ui.theme.ThemeController
import com.akrishna87.budgettracker.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsViewModel(
    private val repository: BudgetRepository,
    private val securityPrefs: SecurityPrefs,
    private val themeController: ThemeController
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = repository.observeCategories()
        .map { it.sortedBy { c -> c.sortOrder } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLockEnabled = MutableStateFlow(securityPrefs.isLockEnabled)
    val isLockEnabled: StateFlow<Boolean> = _isLockEnabled.asStateFlow()

    val themeMode: ThemeMode get() = themeController.mode

    fun setThemeMode(mode: ThemeMode) {
        themeController.select(mode)
    }

    fun setPinAndEnableLock(pin: String) {
        val salt = PinHashing.generateSalt()
        securityPrefs.pinSalt = salt
        securityPrefs.pinHash = PinHashing.hash(pin, salt)
        securityPrefs.isLockEnabled = true
        _isLockEnabled.value = true
    }

    fun disableLock() {
        securityPrefs.isLockEnabled = false
        _isLockEnabled.value = false
    }

    fun addCategory(name: String, budget: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val nextOrder = (categories.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.upsertCategory(
                CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    emoji = "",
                    budget = budget,
                    sortOrder = nextOrder
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity, name: String, budget: Double) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.upsertCategory(category.copy(name = name.trim(), budget = budget))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun restoreCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.upsertCategory(category) }
    }
}
