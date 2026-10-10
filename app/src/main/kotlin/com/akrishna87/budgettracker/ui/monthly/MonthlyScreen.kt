package com.akrishna87.budgettracker.ui.monthly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.EmptyState
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.components.categoryIcon
import com.akrishna87.budgettracker.ui.theme.BudgetTheme
import com.akrishna87.budgettracker.util.formatMoney
import com.akrishna87.budgettracker.util.monthLabel
import com.akrishna87.budgettracker.util.shiftMonthKey

@Composable
fun MonthlyScreen(viewModel: MonthlyViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = BudgetTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectMonth(shiftMonthKey(state.selectedMonth, -1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(monthLabel(state.selectedMonth), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.selectMonth(shiftMonthKey(state.selectedMonth, 1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }
        }

        item {
            Text(
                "Tick a category off once it's paid or logged for the month.",
                color = colors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.rows.isEmpty()) {
            item {
                EmptyState(icon = Icons.Outlined.CheckCircle, text = "No categories yet. Add one in Settings.")
            }
        } else {
            items(state.rows, key = { it.category.id }) { row ->
                MonthlyTaskCard(row = row, onToggle = { checked -> viewModel.setChecked(row.category.id, checked) })
            }
        }
    }
}

@Composable
private fun MonthlyTaskCard(row: MonthlyTaskRow, onToggle: (Boolean) -> Unit) {
    val colors = BudgetTheme.colors
    val category = row.category
    val tint = if (category.isInvestment) colors.accentSecondary else MaterialTheme.colorScheme.primary

    ElevatedPanel(contentPadding = 12) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { onToggle(!row.checked) }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(icon = categoryIcon(category), tint = tint, small = true)
            Column(Modifier.weight(1f)) {
                Text(
                    category.name,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (row.checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (row.checked) colors.muted else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    formatMoney(row.amount) + " this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.muted
                )
            }
            Checkbox(checked = row.checked, onCheckedChange = onToggle)
        }
    }
}
