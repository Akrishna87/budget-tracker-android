package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtAccountDao {
    @Query("SELECT * FROM debt_accounts ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<DebtAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(account: DebtAccountEntity)

    @Delete
    suspend fun delete(account: DebtAccountEntity)
}
