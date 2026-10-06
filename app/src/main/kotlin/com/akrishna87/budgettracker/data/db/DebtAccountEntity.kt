package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType {
    LOAN,
    CREDIT_CARD
}

@Entity(tableName = "debt_accounts")
data class DebtAccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: DebtType,
    /** 0 means the original amount isn't tracked, so no payoff progress bar is shown. */
    val originalAmount: Double = 0.0,
    val outstandingAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)
