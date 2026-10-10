package com.akrishna87.budgettracker.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.components.categoryIcon
import com.akrishna87.budgettracker.ui.theme.BudgetTheme
import com.akrishna87.budgettracker.util.formatMoney
import com.akrishna87.budgettracker.util.monthLabel
import com.akrishna87.budgettracker.util.shiftMonthKey

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenCategory: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = BudgetTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        state.mostRecentExpense?.let { lastExpense ->
            item {
                RepeatLastExpenseCard(
                    lastExpense = lastExpense,
                    justRepeatedId = state.justRepeatedId,
                    onRepeat = viewModel::repeatLastExpense,
                    onUndo = viewModel::undoRepeat
                )
            }
        }

        item {
            MonthSummaryCard(
                monthKey = state.selectedMonth,
                totalIncome = state.totalIncome,
                totalExpense = state.totalExpense,
                net = state.net,
                onPrevMonth = { viewModel.selectMonth(shiftMonthKey(state.selectedMonth, -1)) },
                onNextMonth = { viewModel.selectMonth(shiftMonthKey(state.selectedMonth, 1)) }
            )
        }

        item {
            ElevatedPanel {
                Text(
                    "Income by category",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                if (state.incomeByCategory.isEmpty()) {
                    Text(
                        "No income logged for this month yet.",
                        color = colors.muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    state.incomeByCategory.forEach { entry ->
                        IncomeCategoryRow(entry, onClick = { onOpenCategory(entry.category.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        item {
            ElevatedPanel {
                Text(
                    "Expense by category",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                if (state.expenseByCategory.isEmpty()) {
                    Text(
                        "No expenses logged for this month yet.",
                        color = colors.muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    state.expenseByCategory.forEach { entry ->
                        ExpenseCategoryRow(entry, onClick = { onOpenCategory(entry.category.id) })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RepeatLastExpenseCard(
    lastExpense: com.akrishna87.budgettracker.data.db.TransactionEntity,
    justRepeatedId: String?,
    onRepeat: () -> Unit,
    onUndo: (String) -> Unit
) {
    val colors = BudgetTheme.colors
    ElevatedPanel(contentPadding = 16) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (justRepeatedId == lastExpense.id) {
                IconBadge(icon = Icons.Outlined.CheckCircle, tint = colors.income)
                Column(Modifier.widthIn(min = 1.dp)) {
                    Text(
                        "Added " + formatMoney(lastExpense.amount),
                        color = colors.income,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.widthIn(min = 1.dp))
                OutlinedButton(onClick = { onUndo(lastExpense.id) }) {
                    Text("Undo")
                }
            } else {
                IconBadge(icon = Icons.Outlined.Repeat, tint = colors.expense)
                Column(Modifier.weight(1f)) {
                    Text("Repeat last expense", fontWeight = FontWeight.Bold)
                    Text(
                        formatMoney(lastExpense.amount) +
                            (lastExpense.paymentMethod?.let { " · $it" } ?: ""),
                        color = colors.muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(onClick = onRepeat) {
                    Text("Log it again")
                }
            }
        }
    }
}

@Composable
private fun MonthSummaryCard(
    monthKey: String,
    totalIncome: Double,
    totalExpense: Double,
    net: Double,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val colors = BudgetTheme.colors
    ElevatedPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(monthLabel(monthKey), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryStat(label = "Income", value = totalIncome, color = colors.income, modifier = Modifier.weight(1f))
            SummaryStat(label = "Expenses", value = totalExpense, color = colors.expense, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        SummaryStat(
            label = "Net",
            value = net,
            color = if (net >= 0) colors.income else colors.expense,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SummaryStat(label: String, value: Double, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val colors = BudgetTheme.colors
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surfaceWell),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = colors.muted)
            Spacer(Modifier.height(4.dp))
            Text(formatMoney(value), style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

@Composable
private fun ExpenseCategoryRow(entry: CategoryTotal, onClick: () -> Unit) {
    val colors = BudgetTheme.colors
    val category = entry.category
    val budget = category.budget
    val spent = entry.amount
    val fraction = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 1f
    val barColor = when {
        budget <= 0 -> MaterialTheme.colorScheme.primary
        spent > budget -> colors.danger
        spent >= budget * 0.8 -> colors.warn
        else -> MaterialTheme.colorScheme.primary
    }

    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconBadge(icon = categoryIcon(category), small = true)
                Text(category.name, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (budget > 0) "${formatMoney(spent)} / ${formatMoney(budget)}" else formatMoney(spent),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.muted)
            }
        }
        Spacer(Modifier.height(6.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = barColor,
            trackColor = colors.surfaceWell
        )
    }
}

@Composable
private fun IncomeCategoryRow(entry: CategoryTotal, onClick: () -> Unit) {
    val colors = BudgetTheme.colors
    val category = entry.category

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconBadge(icon = categoryIcon(category), tint = colors.income, small = true)
            Text(category.name, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatMoney(entry.amount), color = colors.income, style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = colors.muted)
        }
    }
}
