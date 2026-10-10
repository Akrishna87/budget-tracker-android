package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val outstandingAmount: Double,
    /** Reference only - no longer drives the months-left display. */
    val monthlyPayment: Double,
    /** Typed in directly rather than calculated; null means not set yet. */
    val remainingMonths: Int? = null,
    val sortOrder: Int = 0
)
