package com.akrishna87.budgettracker.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.SavingsGoalEntity
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class GoalsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.observeSavingsGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(name: String, targetAmount: Double) {
        if (name.isBlank() || targetAmount <= 0.0) return
        viewModelScope.launch {
            repository.upsertSavingsGoal(
                SavingsGoalEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    emoji = "",
                    targetAmount = targetAmount
                )
            )
        }
    }

    fun updateGoal(goal: SavingsGoalEntity, name: String, targetAmount: Double) {
        if (name.isBlank() || targetAmount <= 0.0) return
        viewModelScope.launch {
            repository.upsertSavingsGoal(goal.copy(name = name.trim(), targetAmount = targetAmount))
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.deleteSavingsGoal(goal) }
    }

    fun restoreGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.upsertSavingsGoal(goal) }
    }

    fun contribute(goal: SavingsGoalEntity, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertSavingsGoal(goal.copy(savedAmount = goal.savedAmount + amount))
        }
    }

    fun withdraw(goal: SavingsGoalEntity, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertSavingsGoal(goal.copy(savedAmount = (goal.savedAmount - amount).coerceAtLeast(0.0)))
        }
    }
}
