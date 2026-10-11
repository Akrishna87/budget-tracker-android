package com.akrishna87.budgettracker.data.db

import androidx.room.Entity

/**
 * Marks a category, or one of its subcategories, "done" (paid/logged) for one
 * month on the Checklist screen. [categoryId] actually holds whichever id was
 * ticked - a category's own id when it has no subcategories, otherwise one of
 * its subcategory ids - since category and subcategory ids never collide and
 * this avoids a schema change to add a second nullable key column.
 */
@Entity(tableName = "monthly_checks", primaryKeys = ["categoryId", "monthKey"])
data class MonthlyCheckEntity(
    val categoryId: String,
    /** "yyyy-MM". */
    val monthKey: String,
    val checkedAt: Long = System.currentTimeMillis()
)
