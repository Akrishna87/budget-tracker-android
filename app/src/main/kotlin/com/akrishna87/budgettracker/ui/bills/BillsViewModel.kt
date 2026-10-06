package com.akrishna87.budgettracker.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.RecurringBillEntity
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.nextUnpaidDueDate
import com.akrishna87.budgettracker.util.todayKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class BillWithStatus(
    val bill: RecurringBillEntity,
    val category: CategoryEntity?,
    val dueDate: LocalDate,
    val daysUntilDue: Long,
    val paidThisCycle: Boolean
)

data class BillsUiState(
    val bills: List<BillWithStatus> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

class BillsViewModel(private val repository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<BillsUiState> = combine(
        repository.observeBills(),
        repository.observeCategories()
    ) { bills, categories ->
        val today = LocalDate.now()
        val withStatus = bills.map { bill ->
            val paidThisCycle = bill.lastPaidMonth == currentMonthKey()
            val dueDate = nextUnpaidDueDate(bill.dueDayOfMonth, bill.lastPaidMonth, today)
            BillWithStatus(
                bill = bill,
                category = categories.find { it.id == bill.categoryId },
                dueDate = dueDate,
                daysUntilDue = ChronoUnit.DAYS.between(today, dueDate),
                paidThisCycle = paidThisCycle
            )
        }.sortedBy { it.dueDate }
        BillsUiState(bills = withStatus, categories = categories)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillsUiState())

    fun addBill(name: String, amount: Double, categoryId: String?, dueDayOfMonth: Int, reminderDaysBefore: Int) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertBill(
                RecurringBillEntity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    amount = amount,
                    categoryId = categoryId,
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 28),
                    reminderDaysBefore = reminderDaysBefore.coerceIn(0, 10)
                )
            )
        }
    }

    fun updateBill(
        bill: RecurringBillEntity,
        name: String,
        amount: Double,
        categoryId: String?,
        dueDayOfMonth: Int,
        reminderDaysBefore: Int
    ) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.upsertBill(
                bill.copy(
                    name = name.trim(),
                    amount = amount,
                    categoryId = categoryId,
                    dueDayOfMonth = dueDayOfMonth.coerceIn(1, 28),
                    reminderDaysBefore = reminderDaysBefore.coerceIn(0, 10)
                )
            )
        }
    }

    fun deleteBill(bill: RecurringBillEntity) {
        viewModelScope.launch { repository.deleteBill(bill) }
    }

    fun markPaid(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.upsertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.EXPENSE,
                    amount = bill.amount,
                    date = todayKey(),
                    note = "Bill: ${bill.name}",
                    categoryId = bill.categoryId,
                    paymentMethod = null
                )
            )
            repository.upsertBill(bill.copy(lastPaidMonth = currentMonthKey()))
        }
    }
}
