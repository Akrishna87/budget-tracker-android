package com.akrishna87.budgettracker.data.repository

import com.akrishna87.budgettracker.data.db.CategoryDao
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.LoanDao
import com.akrishna87.budgettracker.data.db.LoanEntity
import com.akrishna87.budgettracker.data.db.MonthlyCheckDao
import com.akrishna87.budgettracker.data.db.MonthlyCheckEntity
import com.akrishna87.budgettracker.data.db.RecurringExpenseDao
import com.akrishna87.budgettracker.data.db.RecurringExpenseEntity
import com.akrishna87.budgettracker.data.db.SubcategoryDao
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
import com.akrishna87.budgettracker.data.db.TransactionDao
import com.akrishna87.budgettracker.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val subcategoryDao: SubcategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringExpenseDao: RecurringExpenseDao,
    private val loanDao: LoanDao,
    private val monthlyCheckDao: MonthlyCheckDao
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun upsertCategory(category: CategoryEntity) = categoryDao.upsert(category)

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.delete(category)
        subcategoryDao.deleteByCategory(category.id)
        monthlyCheckDao.deleteByCategory(category.id)
    }

    fun observeSubcategories(): Flow<List<SubcategoryEntity>> = subcategoryDao.observeAll()

    suspend fun upsertSubcategory(subcategory: SubcategoryEntity) = subcategoryDao.upsert(subcategory)

    suspend fun deleteSubcategory(subcategory: SubcategoryEntity) = subcategoryDao.delete(subcategory)

    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeMostRecentExpense(): Flow<TransactionEntity?> = transactionDao.observeMostRecentExpense()

    suspend fun upsertTransaction(transaction: TransactionEntity) = transactionDao.upsert(transaction)

    suspend fun deleteTransaction(id: String) = transactionDao.deleteById(id)

    suspend fun getRecurringExpenses(): List<RecurringExpenseEntity> = recurringExpenseDao.getAll()

    suspend fun upsertRecurringExpense(expense: RecurringExpenseEntity) = recurringExpenseDao.upsert(expense)

    fun observeLoans(): Flow<List<LoanEntity>> = loanDao.observeAll()

    suspend fun upsertLoan(loan: LoanEntity) = loanDao.upsert(loan)

    suspend fun deleteLoan(loan: LoanEntity) = loanDao.delete(loan)

    fun observeMonthlyChecks(monthKey: String): Flow<List<MonthlyCheckEntity>> = monthlyCheckDao.observeForMonth(monthKey)

    suspend fun setMonthlyChecked(categoryId: String, monthKey: String, checked: Boolean) {
        if (checked) {
            monthlyCheckDao.upsert(MonthlyCheckEntity(categoryId = categoryId, monthKey = monthKey))
        } else {
            monthlyCheckDao.delete(categoryId, monthKey)
        }
    }
}
