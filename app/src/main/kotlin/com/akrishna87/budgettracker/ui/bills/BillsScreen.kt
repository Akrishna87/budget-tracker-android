package com.akrishna87.budgettracker.ui.bills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.RecurringBillEntity
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.theme.Danger
import com.akrishna87.budgettracker.ui.theme.Income
import com.akrishna87.budgettracker.ui.theme.Muted
import com.akrishna87.budgettracker.ui.theme.Warn
import com.akrishna87.budgettracker.util.formatMoney

@Composable
fun BillsScreen(viewModel: BillsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editingBill by remember { mutableStateOf<RecurringBillEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<RecurringBillEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Recurring bills and subscriptions. You'll get a reminder a few days before each is due.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.bills.isEmpty()) {
            item {
                Text(
                    "No recurring bills yet.",
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(state.bills, key = { it.bill.id }) { item ->
                if (editingBill?.id == item.bill.id) {
                    BillFormPanel(
                        title = "Edit bill",
                        initial = item.bill,
                        categories = state.categories,
                        onSubmit = { name, amount, categoryId, dueDay, reminderDays ->
                            viewModel.updateBill(item.bill, name, amount, categoryId, dueDay, reminderDays)
                            editingBill = null
                        },
                        onCancel = { editingBill = null }
                    )
                } else {
                    BillRow(
                        item = item,
                        onMarkPaid = { viewModel.markPaid(item.bill) },
                        onEdit = { editingBill = item.bill },
                        onDelete = { pendingDelete = item.bill }
                    )
                }
            }
        }

        item {
            if (showAdd) {
                BillFormPanel(
                    title = "Add recurring bill",
                    initial = null,
                    categories = state.categories,
                    onSubmit = { name, amount, categoryId, dueDay, reminderDays ->
                        viewModel.addBill(name, amount, categoryId, dueDay, reminderDays)
                        showAdd = false
                    },
                    onCancel = { showAdd = false }
                )
            } else {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add recurring bill")
                }
            }
        }
    }

    pendingDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${bill.name}\"?") },
            text = { Text("This stops future reminders. Past payments already logged stay in History.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBill(bill)
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
private fun BillRow(
    item: BillWithStatus,
    onMarkPaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val bill = item.bill
    val statusText = when {
        item.paidThisCycle -> "Paid for this cycle"
        item.daysUntilDue < 0 -> "Overdue by ${-item.daysUntilDue} day${if (item.daysUntilDue == -1L) "" else "s"}"
        item.daysUntilDue == 0L -> "Due today"
        item.daysUntilDue == 1L -> "Due tomorrow"
        else -> "Due in ${item.daysUntilDue} days"
    }
    val statusColor = when {
        item.paidThisCycle -> Income
        item.daysUntilDue < 0 -> Danger
        item.daysUntilDue <= bill.reminderDaysBefore -> Warn
        else -> Muted
    }

    ElevatedPanel(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(emoji = item.category?.emoji ?: "🧾", tint = statusColor)
            Column(Modifier.weight(1f)) {
                Text(bill.name, fontWeight = FontWeight.SemiBold)
                Text(formatMoney(bill.amount), color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Danger) }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(statusText, color = statusColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            if (!item.paidThisCycle) {
                Button(onClick = onMarkPaid) { Text("Mark paid") }
            }
        }
    }
}

@Composable
private fun BillFormPanel(
    title: String,
    initial: RecurringBillEntity?,
    categories: List<CategoryEntity>,
    onSubmit: (String, Double, String?, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amount by remember { mutableStateOf(initial?.amount?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "") }
    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var dueDay by remember { mutableStateOf((initial?.dueDayOfMonth ?: 1).toString()) }
    var reminderDays by remember { mutableStateOf((initial?.reminderDaysBefore ?: 2).toString()) }

    ElevatedPanel(contentPadding = 14) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            placeholder = { Text("e.g. Netflix") }
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
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = reminderDays,
            onValueChange = { reminderDays = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Remind me this many days before") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSubmit(
                        name,
                        amount.toDoubleOrNull() ?: 0.0,
                        categoryId,
                        dueDay.toIntOrNull() ?: 1,
                        reminderDays.toIntOrNull() ?: 2
                    )
                }
            ) { Text(if (initial == null) "Add bill" else "Save changes") }
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
    val label = categories.find { it.id == selectedId }?.let { "${it.emoji} ${it.name}" } ?: "No category"
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("No category") }, onClick = { onSelect(null); expanded = false })
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text("${cat.emoji} ${cat.name}") },
                    onClick = { onSelect(cat.id); expanded = false }
                )
            }
        }
    }
}
