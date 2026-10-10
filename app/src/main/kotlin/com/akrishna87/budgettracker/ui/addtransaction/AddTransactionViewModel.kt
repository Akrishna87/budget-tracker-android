package com.akrishna87.budgettracker.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.todayKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

val DEFAULT_PAYMENT_METHODS = listOf("Cash", "Card", "UPI", "Bank Transfer", "Other")

data class TransactionDraft(
    val id: String? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val date: String = todayKey(),
    val categoryId: String? = null,
    val paymentMethod: String? = null,
    val note: String = "",
    val showMore: Boolean = false
)

data class AddTransactionUiState(
    val draft: TransactionDraft = TransactionDraft(),
    val categories: List<CategoryEntity> = emptyList(),
    val paymentMethods: List<String> = DEFAULT_PAYMENT_METHODS,
    val saved: Boolean = false
)

class AddTransactionViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val draft = MutableStateFlow(TransactionDraft())
    private val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private val paymentMethods = MutableStateFlow(DEFAULT_PAYMENT_METHODS)
    private val savedFlag = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCategories().collect { cats ->
                categories.value = cats
                if (draft.value.categoryId == null && cats.isNotEmpty()) {
                    draft.update { it.copy(categoryId = cats.first().id) }
                }
                pushState()
            }
        }
        viewModelScope.launch {
            repository.observeTransactions().collect { transactions ->
                val historicalMethods = transactions.mapNotNull { it.paymentMethod }.distinct()
                paymentMethods.value = (DEFAULT_PAYMENT_METHODS + historicalMethods).distinct()
                pushState()
            }
        }
        seedDraftFromLastExpense()
    }

    private fun seedDraftFromLastExpense() {
        viewModelScope.launch {
            val last = repository.observeMostRecentExpense().first()
            if (last != null) {
                draft.update {
                    it.copy(
                        categoryId = last.categoryId ?: it.categoryId,
                        paymentMethod = last.paymentMethod
                    )
                }
            }
            pushState()
        }
    }

    private fun pushState() {
        _uiState.value = AddTransactionUiState(
            draft = draft.value,
            categories = categories.value,
            paymentMethods = paymentMethods.value,
            saved = savedFlag.value
        )
    }

    fun startNew() {
        savedFlag.value = false
        val lastCategory = draft.value.categoryId
        val lastPayment = draft.value.paymentMethod
        draft.value = TransactionDraft(categoryId = lastCategory, paymentMethod = lastPayment)
        pushState()
    }

    fun loadForEdit(transaction: TransactionEntity) {
        savedFlag.value = false
        draft.value = TransactionDraft(
            id = transaction.id,
            type = transaction.type,
            amount = formatAmountForEdit(transaction.amount),
            date = transaction.date,
            categoryId = transaction.categoryId,
            paymentMethod = transaction.paymentMethod,
            note = transaction.note,
            showMore = true
        )
        pushState()
    }

    private fun formatAmountForEdit(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
    }

    fun setType(type: TransactionType) {
        draft.update { it.copy(type = type) }
        pushState()
    }

    fun setAmount(amount: String) {
        draft.update { it.copy(amount = amount) }
        pushState()
    }

    fun setCategory(categoryId: String) {
        draft.update { it.copy(categoryId = categoryId) }
        pushState()
    }

    fun setPaymentMethod(method: String) {
        draft.update { it.copy(paymentMethod = method) }
        pushState()
    }

    fun setDate(date: String) {
        draft.update { it.copy(date = date) }
        pushState()
    }

    fun setNote(note: String) {
        draft.update { it.copy(note = note) }
        pushState()
    }

    fun showMoreDetails() {
        draft.update { it.copy(showMore = true) }
        pushState()
    }

    fun save() {
        val current = draft.value
        val amountValue = current.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0.0) return

        viewModelScope.launch {
            val entry = TransactionEntity(
                id = current.id ?: UUID.randomUUID().toString(),
                type = current.type,
                amount = amountValue,
                date = current.date,
                note = current.note,
                categoryId = current.categoryId,
                paymentMethod = if (current.type == TransactionType.EXPENSE) current.paymentMethod else null
            )
            repository.upsertTransaction(entry)
            savedFlag.value = true
            pushState()
        }
    }
}
