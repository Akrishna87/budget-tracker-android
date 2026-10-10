package com.akrishna87.budgettracker.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
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

/**
 * What the user picked on the add-entry toggle. INVESTMENT is still stored
 * as an EXPENSE transaction (see [TransactionDraft.type]) - it only narrows
 * which categories are offered, to the ones marked "is this an investment
 * category?" in Settings.
 */
enum class EntryKind {
    INCOME,
    EXPENSE,
    INVESTMENT
}

data class TransactionDraft(
    val id: String? = null,
    val kind: EntryKind = EntryKind.EXPENSE,
    val amount: String = "",
    val date: String = todayKey(),
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    val paymentMethod: String? = null,
    val note: String = "",
    val showMore: Boolean = false
) {
    val type: TransactionType get() = if (kind == EntryKind.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
}

data class AddTransactionUiState(
    val draft: TransactionDraft = TransactionDraft(),
    val categories: List<CategoryEntity> = emptyList(),
    val subcategories: List<SubcategoryEntity> = emptyList(),
    val paymentMethods: List<String> = DEFAULT_PAYMENT_METHODS,
    val saved: Boolean = false
)

class AddTransactionViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val draft = MutableStateFlow(TransactionDraft())
    private val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private val subcategories = MutableStateFlow<List<SubcategoryEntity>>(emptyList())
    private val paymentMethods = MutableStateFlow(DEFAULT_PAYMENT_METHODS)
    private val savedFlag = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCategories().collect { cats ->
                categories.value = cats
                if (draft.value.categoryId == null && cats.isNotEmpty()) {
                    draft.update {
                        it.copy(categoryId = categoriesForKind(it.kind, cats).firstOrNull()?.id ?: cats.first().id)
                    }
                }
                pushState()
            }
        }
        viewModelScope.launch {
            repository.observeSubcategories().collect { subs ->
                subcategories.value = subs
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
                val isInvestmentCategory = repository.observeCategories().first()
                    .find { it.id == last.categoryId }?.isInvestment == true
                draft.update {
                    it.copy(
                        kind = if (isInvestmentCategory) EntryKind.INVESTMENT else EntryKind.EXPENSE,
                        categoryId = last.categoryId ?: it.categoryId,
                        subcategoryId = last.subcategoryId,
                        paymentMethod = last.paymentMethod
                    )
                }
            }
            pushState()
        }
    }

    private fun categoriesForKind(kind: EntryKind, cats: List<CategoryEntity> = categories.value): List<CategoryEntity> {
        return when (kind) {
            EntryKind.INCOME -> cats
            EntryKind.EXPENSE -> cats.filter { !it.isInvestment }
            EntryKind.INVESTMENT -> cats.filter { it.isInvestment }
        }
    }

    private fun pushState() {
        _uiState.value = AddTransactionUiState(
            draft = draft.value,
            categories = categories.value,
            subcategories = subcategories.value,
            paymentMethods = paymentMethods.value,
            saved = savedFlag.value
        )
    }

    fun startNew() {
        savedFlag.value = false
        val lastKind = draft.value.kind
        val lastCategory = draft.value.categoryId
        val lastSubcategory = draft.value.subcategoryId
        val lastPayment = draft.value.paymentMethod
        draft.value = TransactionDraft(kind = lastKind, categoryId = lastCategory, subcategoryId = lastSubcategory, paymentMethod = lastPayment)
        pushState()
    }

    fun loadForEdit(transaction: TransactionEntity) {
        savedFlag.value = false
        val kind = when {
            transaction.type == TransactionType.INCOME -> EntryKind.INCOME
            categories.value.find { it.id == transaction.categoryId }?.isInvestment == true -> EntryKind.INVESTMENT
            else -> EntryKind.EXPENSE
        }
        draft.value = TransactionDraft(
            id = transaction.id,
            kind = kind,
            amount = formatAmountForEdit(transaction.amount),
            date = transaction.date,
            categoryId = transaction.categoryId,
            subcategoryId = transaction.subcategoryId,
            paymentMethod = transaction.paymentMethod,
            note = transaction.note,
            showMore = true
        )
        pushState()
    }

    private fun formatAmountForEdit(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) amount.toLong().toString() else amount.toString()
    }

    fun setKind(kind: EntryKind) {
        draft.update { current ->
            // Don't silently fall back to some other category of the new kind
            // (e.g. the default "Mutual Fund" on every switch to Investment) -
            // that leaves whatever category you actually meant to pick (and its
            // subcategories) un-selected with no visible sign why. Clear to
            // nothing instead so picking a category is always an explicit tap.
            val matching = categoriesForKind(kind)
            val categoryId = current.categoryId?.takeIf { id -> matching.any { it.id == id } }
            current.copy(kind = kind, categoryId = categoryId, subcategoryId = null)
        }
        pushState()
    }

    fun setAmount(amount: String) {
        draft.update { it.copy(amount = amount) }
        pushState()
    }

    fun setCategory(categoryId: String) {
        // A subcategory belongs to one category, so switching category drops
        // whatever subcategory was selected under the old one.
        draft.update { it.copy(categoryId = categoryId, subcategoryId = null) }
        pushState()
    }

    fun setSubcategory(subcategoryId: String?) {
        draft.update { it.copy(subcategoryId = subcategoryId) }
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
                subcategoryId = current.subcategoryId,
                paymentMethod = if (current.type == TransactionType.EXPENSE) current.paymentMethod else null
            )
            repository.upsertTransaction(entry)
            savedFlag.value = true
            pushState()
        }
    }
}
