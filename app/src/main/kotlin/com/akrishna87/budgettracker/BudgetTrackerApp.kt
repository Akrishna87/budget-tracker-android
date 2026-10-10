package com.akrishna87.budgettracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.akrishna87.budgettracker.data.db.AppDatabase
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.work.RecurringExpenseWorker
import java.util.concurrent.TimeUnit

class BudgetTrackerApp : Application() {

    lateinit var repository: BudgetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = BudgetRepository(
            database.categoryDao(),
            database.subcategoryDao(),
            database.transactionDao(),
            database.recurringExpenseDao(),
            database.loanDao()
        )

        scheduleRecurringExpenseCheck()
    }

    private fun scheduleRecurringExpenseCheck() {
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "recurring-expense-check",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecurringExpenseWorker>(1, TimeUnit.DAYS).build()
        )
        // Runs once immediately so a due recurring expense is logged on this very
        // launch, rather than waiting for the first periodic tick (which can be
        // delayed by hours).
        workManager.enqueue(OneTimeWorkRequestBuilder<RecurringExpenseWorker>().build())
    }
}
