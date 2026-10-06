package com.akrishna87.budgettracker.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.TransactionEntity
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.theme.Danger
import com.akrishna87.budgettracker.ui.theme.Expense
import com.akrishna87.budgettracker.ui.theme.Income
import com.akrishna87.budgettracker.ui.theme.Muted
import com.akrishna87.budgettracker.util.formatDateLong
import com.akrishna87.budgettracker.util.formatMoney
import com.akrishna87.budgettracker.util.monthLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEdit: (TransactionEntity) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("History", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.filter.type == TypeFilter.ALL, onClick = { viewModel.setTypeFilter(TypeFilter.ALL) }, label = { Text("All") })
                FilterChip(selected = state.filter.type == TypeFilter.INCOME, onClick = { viewModel.setTypeFilter(TypeFilter.INCOME) }, label = { Text("💰 Income") })
                FilterChip(selected = state.filter.type == TypeFilter.EXPENSE, onClick = { viewModel.setTypeFilter(TypeFilter.EXPENSE) }, label = { Text("💸 Expense") })
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryFilterDropdown(
                    selectedId = state.filter.categoryId,
                    categories = state.categories,
                    onSelect = viewModel::setCategoryFilter,
                    modifier = Modifier.weight(1f)
                )
                MonthFilterDropdown(
                    selectedMonth = state.filter.month,
                    months = state.availableMonths,
                    onSelect = viewModel::setMonthFilter,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = state.filter.search,
                onValueChange = viewModel::setSearch,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search notes") }
            )
        }

        if (state.transactions.isEmpty()) {
            item {
                Text(
                    "No entries match these filters.",
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            items(state.transactions, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    category = state.categories.find { it.id == tx.categoryId },
                    onEdit = { onEdit(tx) },
                    onDeleteRequested = { pendingDelete = tx }
                )
            }
        }
    }

    pendingDelete?.let { tx ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this entry?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(tx.id)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryFilterDropdown(
    selectedId: String?,
    categories: List<com.akrishna87.budgettracker.data.db.CategoryEntity>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = categories.find { it.id == selectedId }?.let { "${it.emoji} ${it.name}" } ?: "All categories"
    Box(modifier) {
        DropdownFilterButton(label = label, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All categories") }, onClick = { onSelect(null); expanded = false })
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text("${cat.emoji} ${cat.name}") },
                    onClick = { onSelect(cat.id); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun MonthFilterDropdown(
    selectedMonth: String?,
    months: List<String>,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = selectedMonth?.let { monthLabel(it) } ?: "All months"
    Box(modifier) {
        DropdownFilterButton(label = label, onClick = { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("All months") }, onClick = { onSelect(null); expanded = false })
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(monthLabel(month)) },
                    onClick = { onSelect(month); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DropdownFilterButton(label: String, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    category: com.akrishna87.budgettracker.data.db.CategoryEntity?,
    onEdit: () -> Unit,
    onDeleteRequested: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val emoji = if (transaction.type == TransactionType.INCOME) "💰" else category?.emoji ?: "💸"
            IconBadge(emoji = emoji, tint = if (transaction.type == TransactionType.INCOME) Income else Expense)

            Column(Modifier.weight(1f)) {
                val title = if (transaction.type == TransactionType.INCOME) {
                    transaction.sourceName?.takeIf { it.isNotBlank() } ?: "Income"
                } else {
                    category?.name ?: "(deleted category)"
                }
                Text(title, fontWeight = FontWeight.SemiBold)
                val subParts = mutableListOf(formatDateLong(transaction.date))
                if (transaction.type == TransactionType.EXPENSE) transaction.paymentMethod?.let { subParts.add(it) }
                if (transaction.note.isNotBlank()) subParts.add(transaction.note)
                Text(subParts.joinToString(" · "), color = Muted, style = MaterialTheme.typography.bodyMedium)
            }

            Text(
                (if (transaction.type == TransactionType.INCOME) "+" else "-") + formatMoney(transaction.amount),
                color = if (transaction.type == TransactionType.INCOME) Income else Expense,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDeleteRequested) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Danger)
            }
        }
    }
}
