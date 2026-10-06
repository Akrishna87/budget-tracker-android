package com.akrishna87.budgettracker.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akrishna87.budgettracker.BudgetTrackerApp
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.nextUnpaidDueDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as BudgetTrackerApp).repository
        val today = LocalDate.now()

        repository.getActiveBills().forEach { bill ->
            if (bill.lastPaidMonth == currentMonthKey()) return@forEach
            if (bill.lastNotifiedMonth == currentMonthKey()) return@forEach

            val dueDate = nextUnpaidDueDate(bill.dueDayOfMonth, bill.lastPaidMonth, today)
            val daysUntilDue = ChronoUnit.DAYS.between(today, dueDate)

            if (daysUntilDue <= bill.reminderDaysBefore) {
                showBillReminder(applicationContext, bill, daysUntilDue)
                repository.upsertBill(bill.copy(lastNotifiedMonth = currentMonthKey()))
            }
        }
        return Result.success()
    }
}
