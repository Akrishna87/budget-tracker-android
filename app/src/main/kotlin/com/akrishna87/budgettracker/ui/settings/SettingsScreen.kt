package com.akrishna87.budgettracker.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.data.db.LoanEntity
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
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
    val subcategories by viewModel.subcategories.collectAsState()
    val loans by viewModel.loans.collectAsState()
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    var editingLoan by remember { mutableStateOf<LoanEntity?>(null) }
    var pendingDeleteLoan by remember { mutableStateOf<LoanEntity?>(null) }
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
                "Set a monthly budget per category to track it on the Dashboard. Leave budget at 0 for no limit. " +
                    "Tap a category to add subcategories under it.",
                color = colors.muted,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(categories, key = { it.id }) { category ->
            if (editingCategory?.id == category.id) {
                CategoryEditRow(
                    initial = category,
                    onSave = { name, budget, isInvestment ->
                        viewModel.updateCategory(category, name, budget, isInvestment)
                        editingCategory = null
                    },
                    onCancel = { editingCategory = null }
                )
            } else {
                Column {
                    CategoryRow(
                        category = category,
                        expanded = expandedCategoryId == category.id,
                        onToggleExpand = {
                            expandedCategoryId = if (expandedCategoryId == category.id) null else category.id
                        },
                        onEdit = { editingCategory = category },
                        onDelete = { pendingDelete = category }
                    )
                    if (expandedCategoryId == category.id) {
                        SubcategoryManager(
                            subcategories = subcategories.filter { it.categoryId == category.id },
                            onAdd = { name -> viewModel.addSubcategory(category.id, name) },
                            onDelete = viewModel::deleteSubcategory
                        )
                    }
                }
            }
        }

        item {
            AddCategoryRow(onAdd = viewModel::addCategory)
        }

        item {
            Text(
                "Loans",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        items(loans, key = { it.id }) { loan ->
            if (editingLoan?.id == loan.id) {
                LoanEditRow(
                    initial = loan,
                    onSave = { name, outstanding, monthlyPayment ->
                        viewModel.updateLoan(loan, name, outstanding, monthlyPayment)
                        editingLoan = null
                    },
                    onCancel = { editingLoan = null }
                )
            } else {
                LoanRow(
                    loan = loan,
                    onEdit = { editingLoan = loan },
                    onDelete = { pendingDeleteLoan = loan }
                )
            }
        }

        item {
            AddLoanRow(onAdd = viewModel::addLoan)
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

    pendingDeleteLoan?.let { loan ->
        AlertDialog(
            onDismissRequest = { pendingDeleteLoan = null },
            title = { Text("Delete \"${loan.name}\"?") },
            text = { Text("This removes it from the Dashboard's Loan Details.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLoan(loan)
                    pendingDeleteLoan = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "\"${loan.name}\" deleted",
                            actionLabel = "Undo"
                        )
                        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                            viewModel.restoreLoan(loan)
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteLoan = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = BudgetTheme.colors
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(12.dp),
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
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colors.muted
            )
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.danger) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubcategoryManager(
    subcategories: List<SubcategoryEntity>,
    onAdd: (String) -> Unit,
    onDelete: (SubcategoryEntity) -> Unit
) {
    val colors = BudgetTheme.colors
    var newName by remember { mutableStateOf("") }

    Column(Modifier.padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 4.dp)) {
        if (subcategories.isEmpty()) {
            Text(
                "No subcategories yet.",
                color = colors.muted,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subcategories.forEach { subcategory ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(subcategory.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Delete ${subcategory.name}",
                                modifier = Modifier
                                    .height(16.dp)
                                    .clickable(onClick = { onDelete(subcategory) })
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                modifier = Modifier.weight(1f),
                label = { Text("New subcategory") },
                singleLine = true
            )
            TextButton(onClick = {
                onAdd(newName)
                newName = ""
            }) { Text("Add") }
        }
    }
}

@Composable
private fun CategoryEditRow(
    initial: CategoryEntity,
    onSave: (String, Double, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var budget by remember { mutableStateOf(if (initial.budget > 0) initial.budget.toString() else "") }
    var isInvestment by remember { mutableStateOf(initial.isInvestment) }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isInvestment, onCheckedChange = { isInvestment = it })
                Text("This is an investment category (Mutual Fund, RD, ...)")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSave(name, budget.toDoubleOrNull() ?: 0.0, isInvestment) }) { Text("Save") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun AddCategoryRow(onAdd: (String, Double, Boolean) -> Unit) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var isInvestment by remember { mutableStateOf(false) }

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isInvestment, onCheckedChange = { isInvestment = it })
            Text("This is an investment category (Mutual Fund, RD, ...)")
        }
        Spacer(Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAdd(name, budget.toDoubleOrNull() ?: 0.0, isInvestment)
                name = ""
                budget = ""
                isInvestment = false
            }
        ) { Text("Add category") }
    }
}

@Composable
private fun LoanRow(loan: LoanEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            Column(Modifier.weight(1f)) {
                Text(loan.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatMoney(loan.outstandingAmount)} outstanding" +
                        if (loan.monthlyPayment > 0) " · ${formatMoney(loan.monthlyPayment)}/mo" else "",
                    color = colors.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.danger) }
        }
    }
}

@Composable
private fun LoanEditRow(
    initial: LoanEntity,
    onSave: (String, Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var outstanding by remember { mutableStateOf(initial.outstandingAmount.toString()) }
    var monthlyPayment by remember { mutableStateOf(if (initial.monthlyPayment > 0) initial.monthlyPayment.toString() else "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Name") })
            OutlinedTextField(
                value = outstanding,
                onValueChange = { outstanding = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Outstanding amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = monthlyPayment,
                onValueChange = { monthlyPayment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly payment (optional, for months-left)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    onSave(name, outstanding.toDoubleOrNull() ?: 0.0, monthlyPayment.toDoubleOrNull() ?: 0.0)
                }) { Text("Save") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}

@Composable
private fun AddLoanRow(onAdd: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var outstanding by remember { mutableStateOf("") }
    var monthlyPayment by remember { mutableStateOf("") }

    ElevatedPanel(contentPadding = 14) {
        Text("Add loan", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            placeholder = { Text("e.g. Home loan") }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = outstanding,
            onValueChange = { outstanding = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Outstanding amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = monthlyPayment,
            onValueChange = { monthlyPayment = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Monthly payment (optional, for months-left)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        Spacer(Modifier.height(10.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onAdd(name, outstanding.toDoubleOrNull() ?: 0.0, monthlyPayment.toDoubleOrNull() ?: 0.0)
                name = ""
                outstanding = ""
                monthlyPayment = ""
            }
        ) { Text("Add loan") }
    }
}
