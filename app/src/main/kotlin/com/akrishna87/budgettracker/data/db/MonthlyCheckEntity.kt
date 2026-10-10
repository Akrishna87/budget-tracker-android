package com.akrishna87.budgettracker.data.db

import androidx.room.Entity

/** Marks a category "done" (paid/logged) for one month, on the Monthly screen's checklist. */
@Entity(tableName = "monthly_checks", primaryKeys = ["categoryId", "monthKey"])
data class MonthlyCheckEntity(
    val categoryId: String,
    /** "yyyy-MM". */
    val monthKey: String,
    val checkedAt: Long = System.currentTimeMillis()
)
