package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE type = 'EXPENSE' " +
            "ORDER BY createdAt DESC LIMIT 1"
    )
    fun observeMostRecentExpense(): Flow<TransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}
