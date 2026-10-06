package com.akrishna87.budgettracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.akrishna87.budgettracker.data.db.AppDatabase
import com.akrishna87.budgettracker.data.repository.BudgetRepository
import com.akrishna87.budgettracker.notifications.BillReminderWorker
import com.akrishna87.budgettracker.notifications.createBillReminderChannel
import java.util.concurrent.TimeUnit

class BudgetTrackerApp : Application() {

    lateinit var repository: BudgetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = BudgetRepository(
            database.categoryDao(),
            database.transactionDao(),
            database.recurringBillDao(),
            database.debtAccountDao(),
            database.savingsGoalDao()
        )

        createBillReminderChannel(this)
        scheduleBillReminders()
    }

    private fun scheduleBillReminders() {
        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "bill-reminder-check",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<BillReminderWorker>(1, TimeUnit.DAYS).build()
        )
        // Runs once immediately so a bill due soon is surfaced on this very launch,
        // rather than waiting for the first periodic tick (which can be delayed by hours).
        workManager.enqueue(OneTimeWorkRequestBuilder<BillReminderWorker>().build())
    }
}
