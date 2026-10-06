package com.akrishna87.budgettracker.ui.goals

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.SavingsGoalEntity
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.theme.Danger
import com.akrishna87.budgettracker.ui.theme.Income
import com.akrishna87.budgettracker.ui.theme.Muted
import com.akrishna87.budgettracker.ui.theme.SurfaceWell
import com.akrishna87.budgettracker.util.formatMoney

@Composable
fun GoalsScreen(viewModel: GoalsViewModel) {
    val goals by viewModel.goals.collectAsState()
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var contributeTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var withdrawTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }
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
                "Set a target for each goal and chip away at it whenever you can.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (goals.isEmpty()) {
            item {
                Text(
                    "No savings goals yet.",
                    color = Muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                if (editingGoal?.id == goal.id) {
                    GoalFormPanel(
                        title = "Edit goal",
                        initial = goal,
                        onSubmit = { name, emoji, target ->
                            viewModel.updateGoal(goal, name, emoji, target)
                            editingGoal = null
                        },
                        onCancel = { editingGoal = null }
                    )
                } else {
                    GoalRow(
                        goal = goal,
                        onContribute = { contributeTarget = goal },
                        onWithdraw = { withdrawTarget = goal },
                        onEdit = { editingGoal = goal },
                        onDelete = { pendingDelete = goal }
                    )
                }
            }
        }

        item {
            if (showAdd) {
                GoalFormPanel(
                    title = "Add savings goal",
                    initial = null,
                    onSubmit = { name, emoji, target ->
                        viewModel.addGoal(name, emoji, target)
                        showAdd = false
                    },
                    onCancel = { showAdd = false }
                )
            } else {
                OutlinedButton(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add savings goal")
                }
            }
        }
    }

    pendingDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${goal.name}\"?") },
            text = { Text("This removes the goal and its saved progress.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal)
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    contributeTarget?.let { goal ->
        AmountDialog(
            title = "Add to \"${goal.name}\"",
            confirmLabel = "Add",
            onConfirm = { amount ->
                viewModel.contribute(goal, amount)
                contributeTarget = null
            },
            onDismiss = { contributeTarget = null }
        )
    }

    withdrawTarget?.let { goal ->
        AmountDialog(
            title = "Withdraw from \"${goal.name}\"",
            confirmLabel = "Withdraw",
            onConfirm = { amount ->
                viewModel.withdraw(goal, amount)
                withdrawTarget = null
            },
            onDismiss = { withdrawTarget = null }
        )
    }
}

@Composable
private fun GoalRow(
    goal: SavingsGoalEntity,
    onContribute: () -> Unit,
    onWithdraw: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fraction = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    ElevatedPanel(contentPadding = 14) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(emoji = goal.emoji, tint = Income)
            Column(Modifier.weight(1f)) {
                Text(goal.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatMoney(goal.savedAmount)} / ${formatMoney(goal.targetAmount)}",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Danger) }
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = Income,
            trackColor = SurfaceWell
        )
        Spacer(Modifier.height(4.dp))
        Text("${(fraction * 100).toInt()}% of goal", color = Muted, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onContribute, modifier = Modifier.weight(1f)) { Text("Add money") }
            OutlinedButton(onClick = onWithdraw, modifier = Modifier.weight(1f)) { Text("Withdraw") }
        }
    }
}

@Composable
private fun GoalFormPanel(
    title: String,
    initial: SavingsGoalEntity?,
    onSubmit: (String, String, Double) -> Unit,
    onCancel: () -> Unit
) {
    var emoji by remember { mutableStateOf(initial?.emoji ?: "🎯") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var target by remember { mutableStateOf(initial?.targetAmount?.toString() ?: "") }

    ElevatedPanel(contentPadding = 14) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = emoji, onValueChange = { emoji = it }, modifier = Modifier.width(70.dp), label = { Text("Icon") })
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f), label = { Text("Name") })
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = target,
            onValueChange = { target = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Target amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onSubmit(name, emoji, target.toDoubleOrNull() ?: 0.0) }
            ) { Text(if (initial == null) "Add goal" else "Save changes") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

@Composable
private fun AmountDialog(
    title: String,
    confirmLabel: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(amount.toDoubleOrNull() ?: 0.0) }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
