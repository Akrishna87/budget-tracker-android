package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val amount: Double,
    val categoryId: String? = null,
    /** 1-28; clamped so every month has this day. */
    val dueDayOfMonth: Int,
    /** "yyyy-MM" of the last month an expense was auto-logged for this entry. */
    val lastGeneratedMonth: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
