package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subcategories")
data class SubcategoryEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String,
    val name: String,
    val sortOrder: Int = 0
)
