package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: TransactionType,
    val amount: Double,
    /** ISO date, "YYYY-MM-DD", chosen by the user (not necessarily today). */
    val date: String,
    val note: String = "",
    val categoryId: String? = null,
    val subcategoryId: String? = null,
    /** Expense only. */
    val paymentMethod: String? = null,
    /** Insertion order, used for "most recent entry" independent of the user-editable date. */
    val createdAt: Long = System.currentTimeMillis()
)
