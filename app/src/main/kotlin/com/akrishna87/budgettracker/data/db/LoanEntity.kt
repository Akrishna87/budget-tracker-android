package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val outstandingAmount: Double,
    /** Used with [outstandingAmount] to estimate months left; 0 means unknown. */
    val monthlyPayment: Double,
    val sortOrder: Int = 0
)
