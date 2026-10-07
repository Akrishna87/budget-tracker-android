package com.akrishna87.budgettracker.ui.debts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.akrishna87.budgettracker.data.db.DebtAccountEntity
import com.akrishna87.budgettracker.data.db.DebtType
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.EmptyState
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.components.LocalSnackbarHostState
import com.akrishna87.budgettracker.ui.theme.Danger
import com.akrishna87.budgettracker.ui.theme.Income
import com.akrishna87.budgettracker.ui.theme.Muted
import com.akrishna87.budgettracker.ui.theme.SurfaceWell
import com.akrishna87.budgettracker.util.formatMoney
import kotlinx.coroutines.launch

@Composable
fun DebtsScreen(viewModel: DebtsViewModel) {
    val state by viewModel.uiState.collectAsState()
    var editingAccount by remember { mutableStateOf<DebtAccountEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<DebtAccountEntity?>(null) }
    var paymentTarget by remember { mutableStateOf<DebtAccountEntity?>(null) }
    var chargeTarget by remember { mutableStateOf<DebtAccountEntity?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Loans and credit cards, tracked by outstanding principal remaining.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (state.accounts.isEmpty()) {
            item {
                EmptyState(emoji = "💳", text = "No loans or credit cards added yet.")
            }
        } else {
            items(state.accounts, key = { it.id }) { account ->
                if (editingAccount?.id == account.id) {
                    DebtFormPanel(
                        title = "Edit account",
                        initial = account,
                        onSubmit = { name, type, original, outstanding ->
                            viewModel.updateAccount(account, name, type, original, outstanding)
                            editingAccount = null
                        },
                        onCancel = { editingAccount = null }
                    )
                } else {
                    DebtRow(
                        account = account,
                        onRecordPayment = { paymentTarget = account },
                        onAddCharge = { chargeTarget = account },
                        onEdit = { editingAccount = account },
                        onDelete = { pendingDelete = account }
                    )
                }
            }
        }

        item {
            if (showAdd) {
                DebtFormPanel(
                    title = "Add loan or credit card",
                    initial = null,
                    onSubmit = { name, type, original, outstanding ->
                        viewModel.addAccount(name, type, original, outstanding)
                        showAdd = false
                    },
                    onCancel = { showAdd = false }
                )
            } else {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add loan or credit card")
                }
            }
        }
    }

    pendingDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${account.name}\"?") },
            text = { Text("This stops tracking its balance. Past payments already logged stay in History.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account)
                    pendingDelete = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${account.name}\" deleted",
                            actionLabel = "Undo"
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.restoreAccount(account)
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    paymentTarget?.let { account ->
        PaymentDialog(
            account = account,
            categories = state.categories,
            onConfirm = { amount, categoryId ->
                viewModel.recordPayment(account, amount, categoryId)
                paymentTarget = null
            },
            onDismiss = { paymentTarget = null }
        )
    }

    chargeTarget?.let { account ->
        ChargeDialog(
            account = account,
            onConfirm = { amount ->
                viewModel.addCharge(account, amount)
                chargeTarget = null
            },
            onDismiss = { chargeTarget = null }
        )
    }
}

@Composable
private fun DebtRow(
    account: DebtAccountEntity,
    onRecordPayment: () -> Unit,
    onAddCharge: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val emoji = if (account.type == DebtType.CREDIT_CARD) "💳" else "🏦"
    val paidFraction = if (account.originalAmount > 0) {
        (1.0 - (account.outstandingAmount / account.originalAmount)).toFloat().coerceIn(0f, 1f)
    } else null

    ElevatedPanel(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(emoji = emoji, tint = Danger)
            Column(Modifier.weight(1f)) {
                Text(account.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "Outstanding: " + formatMoney(account.outstandingAmount),
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Danger) }
        }
        if (paidFraction != null) {
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { paidFraction },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Income,
                trackColor = SurfaceWell
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${(paidFraction * 100).toInt()}% paid off of ${formatMoney(account.originalAmount)}",
                color = Muted,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRecordPayment, modifier = Modifier.weight(1f)) { Text("Record payment") }
            OutlinedButton(onClick = onAddCharge, modifier = Modifier.weight(1f)) { Text("Add charge") }
        }
    }
}

@Composable
private fun DebtFormPanel(
    title: String,
    initial: DebtAccountEntity?,
    onSubmit: (String, DebtType, Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: DebtType.LOAN) }
    var original by remember { mutableStateOf(if ((initial?.originalAmount ?: 0.0) > 0) initial!!.originalAmount.toString() else "") }
    var outstanding by remember { mutableStateOf(initial?.outstandingAmount?.toString() ?: "") }

    ElevatedPanel(contentPadding = 14) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = type == DebtType.LOAN, onClick = { type = DebtType.LOAN }, label = { Text("🏦 Loan") })
            FilterChip(selected = type == DebtType.CREDIT_CARD, onClick = { type = DebtType.CREDIT_CARD }, label = { Text("💳 Credit card") })
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            placeholder = { Text("e.g. Car loan") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = outstanding,
            onValueChange = { outstanding = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Outstanding amount (principal remaining)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = original,
            onValueChange = { original = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Original amount (optional, for payoff progress)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onSubmit(name, type, original.toDoubleOrNull() ?: 0.0, outstanding.toDoubleOrNull() ?: 0.0)
                }
            ) { Text(if (initial == null) "Add account" else "Save changes") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun PaymentDialog(
    account: DebtAccountEntity,
    categories: List<CategoryEntity>,
    onConfirm: (Double, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Paying towards \"${account.name}\". This also logs an expense.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                CategoryPicker(selectedId = categoryId, categories = categories, onSelect = { categoryId = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0, categoryId) }) { Text("Pay") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ChargeDialog(
    account: DebtAccountEntity,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add charge") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Raises the outstanding balance on \"${account.name}\".", color = Muted, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
