package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    /** Unused: all goals render a fixed Icons.Outlined.Flag. Kept only for schema compatibility. */
    val emoji: String = "🎯",
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
