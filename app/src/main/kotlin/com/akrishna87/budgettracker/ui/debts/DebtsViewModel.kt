package com.akrishna87.budgettracker.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.DebtAccountEntity
import com.akrishna87.budgettracker.data.db.DebtType
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.todayKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class DebtsUiState(
    val accounts: List<DebtAccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

class DebtsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<DebtsUiState> = combine(
        repository.observeDebts(),
        repository.observeCategories()
    ) { accounts, categories ->
        DebtsUiState(accounts = accounts, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DebtsUiState())

    fun addAccount(name: String, type: DebtType, originalAmount: Double, outstandingAmount: Double) {
        if (name.isBlank() || outstandingAmount < 0.0) return
        viewModelScope.launch {
            repository.upsertDebt(
                DebtAccountEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    type = type,
                    originalAmount = originalAmount,
                    outstandingAmount = outstandingAmount
                )
            )
        }
    }

    fun updateAccount(account: DebtAccountEntity, name: String, type: DebtType, originalAmount: Double, outstandingAmount: Double) {
        if (name.isBlank() || outstandingAmount < 0.0) return
        viewModelScope.launch {
            repository.upsertDebt(
                account.copy(
                    name = name.trim(),
                    type = type,
                    originalAmount = originalAmount,
                    outstandingAmount = outstandingAmount
                )
            )
        }
    }

    fun deleteAccount(account: DebtAccountEntity) {
        viewModelScope.launch { repository.deleteDebt(account) }
    }

    fun restoreAccount(account: DebtAccountEntity) {
        viewModelScope.launch { repository.upsertDebt(account) }
    }

    /** Reduces the outstanding balance and logs the money leaving as an expense. */
    fun recordPayment(account: DebtAccountEntity, amount: Double, categoryId: String?) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    date = todayKey(),
                    note = "Payment: ${account.name}",
                    categoryId = categoryId,
                    paymentMethod = null
                )
            )
            repository.upsertDebt(account.copy(outstandingAmount = (account.outstandingAmount - amount).coerceAtLeast(0.0)))
        }
    }

    /** Raises the outstanding balance, e.g. a new card purchase or a loan top-up. No transaction is logged. */
    fun addCharge(account: DebtAccountEntity, amount: Double) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertDebt(account.copy(outstandingAmount = account.outstandingAmount + amount))
        }
    }
}
