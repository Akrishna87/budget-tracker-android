package com.akrishna87.budgettracker.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.theme.Danger
import com.akrishna87.budgettracker.ui.theme.Muted
import com.akrishna87.budgettracker.util.formatMoney

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val categories by viewModel.categories.collectAsState()
    val isLockEnabled by viewModel.isLockEnabled.collectAsState()
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SecuritySection(
                isLockEnabled = isLockEnabled,
                onEnable = viewModel::setPinAndEnableLock,
                onDisable = viewModel::disableLock
            )
        }

        item {
            Text(
                "Set a monthly budget per category to track it on the Dashboard. Leave budget at 0 for no limit.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(categories, key = { it.id }) { category ->
            if (editingCategory?.id == category.id) {
                CategoryEditRow(
                    initial = category,
                    onSave = { name, emoji, budget ->
                        viewModel.updateCategory(category, name, emoji, budget)
                        editingCategory = null
                    },
                    onCancel = { editingCategory = null }
                )
            } else {
                CategoryRow(
                    category = category,
                    onEdit = { editingCategory = category },
                    onDelete = { pendingDelete = category }
                )
            }
        }

        item {
            AddCategoryRow(onAdd = viewModel::addCategory)
        }
    }

    pendingDelete?.let { category ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("Past entries keep their record but will show as deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
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
private fun CategoryRow(category: CategoryEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconBadge(emoji = category.emoji)
            Text(category.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                if (category.budget > 0) "Budget: ${formatMoney(category.budget)}" else "No budget",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Danger) }
        }
    }
}

@Composable
private fun CategoryEditRow(
    initial: CategoryEntity,
    onSave: (String, String, Double) -> Unit,
    onCancel: () -> Unit
) {
    var emoji by remember { mutableStateOf(initial.emoji) }
    var name by remember { mutableStateOf(initial.name) }
    var budget by remember { mutableStateOf(if (initial.budget > 0) initial.budget.toString() else "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = emoji, onValueChange = { emoji = it }, modifier = Modifier.width(70.dp), label = { Text("Icon") })
                OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f), label = { Text("Name") })
            }
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly budget (optional)") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSave(name, emoji, budget.toDoubleOrNull() ?: 0.0) }) { Text("Save") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun AddCategoryRow(onAdd: (String, String, Double) -> Unit) {
    var emoji by remember { mutableStateOf("⭐") }
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    ElevatedPanel(contentPadding = 14) {
        Text("Add category", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = emoji, onValueChange = { emoji = it }, modifier = Modifier.width(70.dp), label = { Text("Icon") })
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.weight(1f), label = { Text("Name") })
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Monthly budget (optional)") }
        )
        Spacer(Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAdd(name, emoji, budget.toDoubleOrNull() ?: 0.0)
                name = ""
                budget = ""
                emoji = "⭐"
            }
        ) { Text("Add category") }
    }
}

@Composable
private fun SecuritySection(
    isLockEnabled: Boolean,
    onEnable: (String) -> Unit,
    onDisable: () -> Unit
) {
    var showSetup by remember { mutableStateOf(false) }

    ElevatedPanel {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("App lock", fontWeight = FontWeight.SemiBold)
                Text(
                    "Require biometrics or a PIN to open the app.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = isLockEnabled,
                onCheckedChange = { enabled -> if (enabled) showSetup = true else onDisable() }
            )
        }
    }

    if (showSetup) {
        PinSetupDialog(
            onConfirm = { pin ->
                onEnable(pin)
                showSetup = false
            },
            onDismiss = { showSetup = false }
        )
    }
}

@Composable
private fun PinSetupDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set a PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Used as a fallback if biometrics aren't available.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("4-6 digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Confirm PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = Danger, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    pin.length < 4 -> error = "PIN must be at least 4 digits"
                    pin != confirmPin -> error = "PINs don't match"
                    else -> onConfirm(pin)
                }
            }) { Text("Set PIN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
