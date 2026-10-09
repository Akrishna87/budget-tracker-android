package com.akrishna87.budgettracker.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akrishna87.budgettracker.BudgetTrackerApp
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.util.currentMonthKey
import java.time.LocalDate
import java.util.UUID

/**
 * Logs each configured recurring expense as a real transaction once its due
 * day for the current month arrives, so a fixed monthly cost (rent, a
 * subscription) needs configuring only once. [RecurringExpenseEntity.lastGeneratedMonth]
 * guards against logging the same month twice, including across this
 * worker's daily run and the one-time catch-up run on app launch.
 */
class RecurringExpenseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as BudgetTrackerApp).repository
        val today = LocalDate.now()
        val monthKey = currentMonthKey()

        repository.getRecurringExpenses().forEach { recurring ->
            if (recurring.lastGeneratedMonth == monthKey) return@forEach

            val dueDay = minOf(recurring.dueDayOfMonth, today.lengthOfMonth())
            if (today.dayOfMonth < dueDay) return@forEach

            repository.upsertTransaction(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    type = TransactionType.EXPENSE,
                    amount = recurring.amount,
                    date = today.withDayOfMonth(dueDay).toString(),
                    note = "Auto: ${recurring.name}",
                    categoryId = recurring.categoryId
                )
            )
            repository.upsertRecurringExpense(recurring.copy(lastGeneratedMonth = monthKey))
        }
        return Result.success()
    }
}
