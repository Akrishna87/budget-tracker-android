package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY dueDayOfMonth ASC")
    fun observeAll(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses")
    suspend fun getAll(): List<RecurringExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(expense: RecurringExpenseEntity)

    @Delete
    suspend fun delete(expense: RecurringExpenseEntity)
}
