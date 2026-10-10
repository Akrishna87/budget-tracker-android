package com.akrishna87.budgettracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.ui.components.ElevatedPanel
import com.akrishna87.budgettracker.ui.components.IconBadge
import com.akrishna87.budgettracker.ui.components.LocalSnackbarHostState
import com.akrishna87.budgettracker.ui.components.categoryIcon
import com.akrishna87.budgettracker.ui.theme.BudgetTheme
import com.akrishna87.budgettracker.util.formatMoney
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val categories by viewModel.categories.collectAsState()
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
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
                "Set a monthly budget per category to track it on the Dashboard. Leave budget at 0 for no limit.",
                color = colors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(categories, key = { it.id }) { category ->
            if (editingCategory?.id == category.id) {
                CategoryEditRow(
                    initial = category,
                    onSave = { name, budget ->
                        viewModel.updateCategory(category, name, budget)
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
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete \"${category.name}\"?") },
            text = { Text("Past entries keep their record but will show as deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    pendingDelete = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${category.name}\" deleted",
                            actionLabel = "Undo"
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.restoreCategory(category)
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
private fun CategoryRow(category: CategoryEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = BudgetTheme.colors
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
            IconBadge(icon = categoryIcon(category))
            Text(category.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                if (category.budget > 0) "Budget: ${formatMoney(category.budget)}" else "No budget",
                color = colors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.danger) }
        }
    }
}

@Composable
private fun CategoryEditRow(
    initial: CategoryEntity,
    onSave: (String, Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var budget by remember { mutableStateOf(if (initial.budget > 0) initial.budget.toString() else "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Name") })
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly budget (optional)") }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSave(name, budget.toDoubleOrNull() ?: 0.0) }) { Text("Save") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun AddCategoryRow(onAdd: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    ElevatedPanel(contentPadding = 14) {
        Text("Add category", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") }
        )
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
                onAdd(name, budget.toDoubleOrNull() ?: 0.0)
                name = ""
                budget = ""
            }
        ) { Text("Add category") }
    }
}
