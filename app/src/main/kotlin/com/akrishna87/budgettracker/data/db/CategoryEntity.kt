package com.akrishna87.budgettracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    /** Unused: icons are now looked up by [id] (see categoryIcon()), kept only for schema compatibility. */
    val emoji: String,
    /** Monthly budget limit in rupees; 0 means no budget set. */
    val budget: Double = 0.0,
    /** True for categories that represent money set aside (mutual funds, RDs, ...) rather than spent. */
    val isInvestment: Boolean = false,
    val sortOrder: Int = 0
)

object DefaultCategories {
    val seed: List<CategoryEntity> = listOf(
        CategoryEntity("housing", "Housing / Rent", "", sortOrder = 0),
        CategoryEntity("loan", "Loan EMI", "", sortOrder = 1),
        CategoryEntity("investment", "Mutual Fund", "", isInvestment = true, sortOrder = 2),
        CategoryEntity("rd", "RD", "", isInvestment = true, sortOrder = 3),
        CategoryEntity("electricity", "EB Bill", "", sortOrder = 4),
        CategoryEntity("groceries", "Groceries", "", sortOrder = 5),
        CategoryEntity("transport", "Transport", "", sortOrder = 6),
        CategoryEntity("dining", "Dining Out", "", sortOrder = 7),
        CategoryEntity("utilities", "Utilities", "", sortOrder = 8),
        CategoryEntity("healthcare", "Healthcare", "", sortOrder = 9),
        CategoryEntity("shopping", "Shopping", "", sortOrder = 10),
        CategoryEntity("entertainment", "Entertainment", "", sortOrder = 11),
        CategoryEntity("other", "Other", "", sortOrder = 12)
    )
}
