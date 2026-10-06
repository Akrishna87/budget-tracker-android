package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val amount: Double,
    val categoryId: String? = null,
    /** 1-28; clamped so every month has this day. */
    val dueDayOfMonth: Int,
    val reminderDaysBefore: Int = 2,
    val isActive: Boolean = true,
    /** "yyyy-MM" of the last month this bill was marked paid for. */
    val lastPaidMonth: String? = null,
    /** "yyyy-MM" the due-soon notification last fired for, to avoid repeats. */
    val lastNotifiedMonth: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
