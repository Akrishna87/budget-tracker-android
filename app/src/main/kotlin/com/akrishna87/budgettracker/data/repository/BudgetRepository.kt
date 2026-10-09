package com.akrishna87.budgettracker.data.repository

import com.akrishna87.budgettracker.data.db.CategoryDao
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.RecurringExpenseDao
import com.akrishna87.budgettracker.data.db.RecurringExpenseEntity
import com.akrishna87.budgettracker.data.db.TransactionDao
import com.akrishna87.budgettracker.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringExpenseDao: RecurringExpenseDao
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeMostRecentExpense(): Flow<TransactionEntity?> = transactionDao.observeMostRecentExpense()

    suspend fun upsertCategory(category: CategoryEntity) = categoryDao.upsert(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun upsertTransaction(transaction: TransactionEntity) = transactionDao.upsert(transaction)

    suspend fun deleteTransaction(id: String) = transactionDao.deleteById(id)

    fun observeRecurringExpenses(): Flow<List<RecurringExpenseEntity>> = recurringExpenseDao.observeAll()

    suspend fun getRecurringExpenses(): List<RecurringExpenseEntity> = recurringExpenseDao.getAll()

    suspend fun upsertRecurringExpense(expense: RecurringExpenseEntity) = recurringExpenseDao.upsert(expense)

    suspend fun deleteRecurringExpense(expense: RecurringExpenseEntity) = recurringExpenseDao.delete(expense)
}
