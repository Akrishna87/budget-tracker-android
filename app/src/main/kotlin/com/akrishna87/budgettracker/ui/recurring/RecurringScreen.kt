package com.akrishna87.budgettracker.ui.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.RecurringExpenseEntity
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.EmptyState
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.components.LocalSnackbarHostState
import com.akrishna87.budgettracker.ui.components.categoryIcon
import com.akrishna87.budgettracker.ui.theme.BudgetTheme
import com.akrishna87.budgettracker.util.formatMoney
import com.akrishna87.budgettracker.util.currentMonthKey
import com.akrishna87.budgettracker.util.monthLabel
import kotlinx.coroutines.launch

@Composable
fun RecurringScreen(viewModel: RecurringViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editingExpense by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val colors = BudgetTheme.colors

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Fixed monthly costs, configured once. Each is logged as an expense automatically on its due day every month.",
                color = colors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.items.isEmpty()) {
            item {
                EmptyState(icon = Icons.Outlined.Repeat, text = "No recurring expenses yet.")
            }
        } else {
            items(state.items, key = { it.expense.id }) { item ->
                if (editingExpense?.id == item.expense.id) {
                    RecurringFormPanel(
                        title = "Edit recurring expense",
                        initial = item.expense,
                        categories = state.categories,
                        onSubmit = { name, amount, categoryId, dueDay ->
                            viewModel.update(item.expense, name, amount, categoryId, dueDay)
                            editingExpense = null
                        },
                        onCancel = { editingExpense = null }
                    )
                } else {
                    RecurringRow(
                        item = item,
                        onEdit = { editingExpense = item.expense },
                        onDelete = { pendingDelete = item.expense }
                    )
                }
            }
        }

        item {
            if (showAdd) {
                RecurringFormPanel(
                    title = "Add recurring expense",
                    initial = null,
                    categories = state.categories,
                    onSubmit = { name, amount, categoryId, dueDay ->
                        viewModel.add(name, amount, categoryId, dueDay)
                        showAdd = false
                    },
                    onCancel = { showAdd = false }
                )
            } else {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add recurring expense")
                }
            }
        }
    }

    pendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${expense.name}\"?") },
            text = { Text("This stops future auto-logging. Entries already logged stay in History.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(expense)
                    pendingDelete = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${expense.name}\" deleted",
                            actionLabel = "Undo"
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.restore(expense)
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RecurringRow(
    item: RecurringItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = BudgetTheme.colors
    val expense = item.expense
    val statusText = if (expense.lastGeneratedMonth == currentMonthKey()) {
        "Logged for " + monthLabel(currentMonthKey())
    } else {
        "Repeats monthly on day ${expense.dueDayOfMonth}"
    }

    ElevatedPanel(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(icon = item.category?.let { categoryIcon(it) } ?: Icons.Outlined.Repeat, tint = colors.expense)
            Column(Modifier.weight(1f)) {
                Text(expense.name, fontWeight = FontWeight.SemiBold)
                Text(formatMoney(expense.amount), color = colors.muted, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.danger) }
        }
        Spacer(Modifier.height(10.dp))
        Text(statusText, color = colors.muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RecurringFormPanel(
    title: String,
    initial: RecurringExpenseEntity?,
    categories: List<CategoryEntity>,
    onSubmit: (String, Double, String?, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var dueDay by remember { mutableStateOf((initial?.dueDayOfMonth ?: 1).toString()) }

    ElevatedPanel(contentPadding = 14) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            placeholder = { Text("e.g. Rent") }
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = dueDay,
                onValueChange = { dueDay = it },
                modifier = Modifier.width(100.dp),
                label = { Text("Due day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(8.dp))
        CategoryPicker(selectedId = categoryId, categories = categories, onSelect = { categoryId = it })
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSubmit(
                        name,
                        amount.toDoubleOrNull() ?: 0.0,
                        categoryId,
                        dueDay.toIntOrNull() ?: 1
                    )
                }
            ) { Text(if (initial == null) "Add" else "Save changes") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun CategoryPicker(
    selectedId: String?,
    categories: List<CategoryEntity>,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = categories.find { it.id == selectedId }?.name ?: "No category"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("No category") }, onClick = { onSelect(null); expanded = false })
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    leadingIcon = { Icon(categoryIcon(cat), contentDescription = null) },
                    onClick = { onSelect(cat.id); expanded = false }
                )
            }
        }
    }
}
