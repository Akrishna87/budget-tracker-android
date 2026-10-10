package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyCheckDao {
    @Query("SELECT * FROM monthly_checks WHERE monthKey = :monthKey")
    fun observeForMonth(monthKey: String): Flow<List<MonthlyCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: MonthlyCheckEntity)

    @Query("DELETE FROM monthly_checks WHERE categoryId = :categoryId AND monthKey = :monthKey")
    suspend fun delete(categoryId: String, monthKey: String)

    @Query("DELETE FROM monthly_checks WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: String)
}
