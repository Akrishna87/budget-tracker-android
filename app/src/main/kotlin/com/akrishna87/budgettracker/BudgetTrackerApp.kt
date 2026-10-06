package com.akrishna87.budgettracker

import android.app.Application
import com.akrishna87.budgettracker.data.db.AppDatabase
import com.akrishna87.budgettracker.data.repository.BudgetRepository

class BudgetTrackerApp : Application() {

    lateinit var repository: BudgetRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = BudgetRepository(database.categoryDao(), database.transactionDao())
    }
}
