package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringBillDao {
    @Query("SELECT * FROM recurring_bills ORDER BY dueDayOfMonth ASC")
    fun observeAll(): Flow<List<RecurringBillEntity>>

    @Query("SELECT * FROM recurring_bills WHERE isActive = 1")
    suspend fun getActive(): List<RecurringBillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bill: RecurringBillEntity)

    @Delete
    suspend fun delete(bill: RecurringBillEntity)
}
