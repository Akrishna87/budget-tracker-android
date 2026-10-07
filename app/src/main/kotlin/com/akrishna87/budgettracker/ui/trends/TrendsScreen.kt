package com.akrishna87.budgettracker.ui.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.theme.Accent
import com.akrishna87.budgettracker.ui.theme.BudgetTheme
import com.akrishna87.budgettracker.util.formatMoney
import com.akrishna87.budgettracker.util.monthLabel
import com.akrishna87.budgettracker.util.shortMonthLabel

@Composable
fun TrendsScreen(viewModel: TrendsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = BudgetTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedPanel {
                Text("Last 6 months", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                LegendRow()
                Spacer(Modifier.height(12.dp))
                MonthlyBarChart(months = state.recentMonths)
                Spacer(Modifier.height(16.dp))
                Text("Net trend", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                NetTrendLine(months = state.recentMonths)
            }
        }

        item {
            ElevatedPanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectYear(state.selectedYear - 1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous year")
                    }
                    Text(state.selectedYear.toString(), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.selectYear(state.selectedYear + 1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next year")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    YearStat(label = "Income", value = state.yearIncome, color = colors.income, modifier = Modifier.weight(1f))
                    YearStat(label = "Expenses", value = state.yearExpense, color = colors.expense, modifier = Modifier.weight(1f))
                    YearStat(
                        label = "Net",
                        value = state.yearNet,
                        color = if (state.yearNet >= 0) colors.income else colors.danger,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                state.yearMonths.forEach { month ->
                    MonthRow(month)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LegendRow() {
    val colors = BudgetTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendDot(color = colors.income, label = "Income")
        LegendDot(color = colors.expense, label = "Expense")
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(modifier = Modifier.height(10.dp).width(10.dp)) {
            drawCircle(color = color, radius = size.minDimension / 2f)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = BudgetTheme.colors.muted)
    }
}

@Composable
private fun MonthlyBarChart(months: List<MonthlyAggregate>) {
    val colors = BudgetTheme.colors
    val maxValue = (months.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0.0).coerceAtLeast(1.0)

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            if (months.isEmpty()) return@Canvas
            val groupWidth = size.width / months.size
            val barWidth = groupWidth / 3.5f
            months.forEachIndexed { index, month ->
                val groupX = index * groupWidth
                val incomeHeight = (month.income / maxValue).toFloat() * size.height
                val expenseHeight = (month.expense / maxValue).toFloat() * size.height
                drawRect(
                    color = colors.income,
                    topLeft = Offset(groupX + barWidth * 0.4f, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight)
                )
                drawRect(
                    color = colors.expense,
                    topLeft = Offset(groupX + barWidth * 1.8f, size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            months.forEach { month ->
                Text(
                    shortMonthLabel(month.monthKey),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NetTrendLine(months: List<MonthlyAggregate>) {
    val colors = BudgetTheme.colors
    val maxAbs = (months.maxOfOrNull { kotlin.math.abs(it.net) } ?: 0.0).coerceAtLeast(1.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        if (months.size < 2) return@Canvas
        val stepX = size.width / (months.size - 1)
        val midY = size.height / 2f
        val points = months.mapIndexed { index, month ->
            Offset(index * stepX, midY - (month.net / maxAbs).toFloat() * midY)
        }

        drawLine(
            color = colors.muted.copy(alpha = 0.3f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 1.dp.toPx()
        )
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Accent,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEach { point ->
            drawCircle(color = Accent, radius = 4.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun YearStat(label: String, value: Double, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = BudgetTheme.colors.muted)
        Spacer(Modifier.height(4.dp))
        Text(formatMoney(value), style = MaterialTheme.typography.titleSmall, color = color, maxLines = 1)
    }
}

@Composable
private fun MonthRow(month: MonthlyAggregate) {
    val colors = BudgetTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(monthLabel(month.monthKey), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        if (month.income == 0.0 && month.expense == 0.0) {
            Text("No activity", color = colors.muted, style = MaterialTheme.typography.bodySmall)
        } else {
            Text(
                "+${formatMoney(month.income)}  -${formatMoney(month.expense)}",
                color = colors.muted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
