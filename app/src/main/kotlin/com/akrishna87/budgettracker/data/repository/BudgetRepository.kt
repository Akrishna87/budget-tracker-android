package com.akrishna87.budgettracker.data.repository

import com.akrishna87.budgettracker.data.db.CategoryDao
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.DebtAccountDao
import com.akrishna87.budgettracker.data.db.DebtAccountEntity
import com.akrishna87.budgettracker.data.db.RecurringBillDao
import com.akrishna87.budgettracker.data.db.RecurringBillEntity
import com.akrishna87.budgettracker.data.db.SavingsGoalDao
import com.akrishna87.budgettracker.data.db.SavingsGoalEntity
import com.akrishna87.budgettracker.data.db.TransactionDao
import com.akrishna87.budgettracker.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val recurringBillDao: RecurringBillDao,
    private val debtAccountDao: DebtAccountDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()

    fun observeMostRecentExpense(): Flow<TransactionEntity?> = transactionDao.observeMostRecentExpense()

    suspend fun upsertCategory(category: CategoryEntity) = categoryDao.upsert(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun upsertTransaction(transaction: TransactionEntity) = transactionDao.upsert(transaction)

    suspend fun deleteTransaction(id: String) = transactionDao.deleteById(id)

    fun observeBills(): Flow<List<RecurringBillEntity>> = recurringBillDao.observeAll()

    suspend fun getActiveBills(): List<RecurringBillEntity> = recurringBillDao.getActive()

    suspend fun upsertBill(bill: RecurringBillEntity) = recurringBillDao.upsert(bill)

    suspend fun deleteBill(bill: RecurringBillEntity) = recurringBillDao.delete(bill)

    fun observeDebts(): Flow<List<DebtAccountEntity>> = debtAccountDao.observeAll()

    suspend fun upsertDebt(account: DebtAccountEntity) = debtAccountDao.upsert(account)

    suspend fun deleteDebt(account: DebtAccountEntity) = debtAccountDao.delete(account)

    fun observeSavingsGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.observeAll()

    suspend fun upsertSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.upsert(goal)

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.delete(goal)
}
