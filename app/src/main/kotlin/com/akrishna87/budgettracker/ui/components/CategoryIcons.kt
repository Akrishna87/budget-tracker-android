package com.akrishna87.budgettracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Build
import androidx.compose.ui.graphics.vector.ImageVector
import com.akrishna87.budgettracker.data.db.CategoryEntity

/**
 * One icon per built-in category id. A custom (user-created) category has no
 * id we recognize, so it falls back to a plain label icon rather than the
 * free-text emoji field this used to be (removed - a mix of vector icons and
 * arbitrary emoji would look more inconsistent than one generic icon).
 */
fun categoryIcon(category: CategoryEntity): ImageVector = when (category.id) {
    "housing" -> Icons.Outlined.Home
    "loan" -> Icons.Outlined.AccountBalance
    "investment" -> Icons.Outlined.TrendingUp
    "rd" -> Icons.Outlined.Savings
    "electricity" -> Icons.Outlined.Bolt
    "groceries" -> Icons.Outlined.ShoppingCart
    "transport" -> Icons.Outlined.DirectionsCar
    "dining" -> Icons.Outlined.Restaurant
    "utilities" -> Icons.Outlined.Build
    "healthcare" -> Icons.Outlined.LocalHospital
    "shopping" -> Icons.Outlined.ShoppingBag
    "entertainment" -> Icons.Outlined.Movie
    "other" -> Icons.Outlined.HelpOutline
    else -> Icons.Outlined.Label
}
