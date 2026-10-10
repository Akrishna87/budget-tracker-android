package com.akrishna87.budgettracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoryDao {
    @Query("SELECT * FROM subcategories ORDER BY sortOrder ASC, name ASC")
    fun observeAll(): Flow<List<SubcategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(subcategory: SubcategoryEntity)

    @Delete
    suspend fun delete(subcategory: SubcategoryEntity)

    @Query("DELETE FROM subcategories WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: String)
}
