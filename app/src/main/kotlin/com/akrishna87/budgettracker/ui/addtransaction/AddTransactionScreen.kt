package com.akrishna87.budgettracker.ui.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.CategoryEntity
import com.akrishna87.budgettracker.ui.components.categoryIcon
import com.akrishna87.budgettracker.ui.theme.BudgetTheme

@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val draft = state.draft
    val focusRequester = remember { FocusRequester() }
    val colors = BudgetTheme.colors

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    LaunchedEffect(Unit) {
        if (draft.id == null) focusRequester.requestFocus()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (draft.id != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = colors.muted, modifier = Modifier.height(16.dp))
                    Text(
                        "Editing an existing entry",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.muted
                    )
                }
            }
        }

        item {
            KindToggle(
                selected = draft.kind,
                onSelect = viewModel::setKind
            )
        }

        item {
            OutlinedTextField(
                value = draft.amount,
                onValueChange = viewModel::setAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.headlineSmall
            )
        }

        val kindAccentColor = when (draft.kind) {
            EntryKind.INCOME -> colors.income
            EntryKind.EXPENSE -> colors.expense
            EntryKind.INVESTMENT -> colors.accentSecondary
        }
        val kindAccentOnColor = when (draft.kind) {
            EntryKind.INCOME -> colors.accentOnColor
            EntryKind.EXPENSE -> Color(0xFF3A1400)
            EntryKind.INVESTMENT -> colors.accentOnColor
        }
        val categoriesForKind = state.categories.filter {
            when (draft.kind) {
                EntryKind.INCOME -> true
                EntryKind.EXPENSE -> !it.isInvestment
                EntryKind.INVESTMENT -> it.isInvestment
            }
        }

        item {
            Column {
                Text("Category (tap one)", style = MaterialTheme.typography.labelLarge, color = colors.muted)
                Spacer(Modifier.height(8.dp))
                if (categoriesForKind.isEmpty()) {
                    Text(
                        "No investment categories yet. Mark one in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.muted
                    )
                } else {
                    CategoryChipFlow(
                        categories = categoriesForKind,
                        selected = draft.categoryId,
                        onSelect = viewModel::setCategory,
                        accentColor = kindAccentColor,
                        accentOnColor = kindAccentOnColor
                    )
                }
            }
        }

        val subcategoriesForCategory = state.subcategories.filter { it.categoryId == draft.categoryId }
        if (subcategoriesForCategory.isNotEmpty()) {
            item {
                Column {
                    Text("Subcategory (optional)", style = MaterialTheme.typography.labelLarge, color = colors.muted)
                    Spacer(Modifier.height(8.dp))
                    ChipFlow(
                        items = subcategoriesForCategory.map { it.id to it.name },
                        selected = draft.subcategoryId,
                        onSelect = { id -> viewModel.setSubcategory(if (draft.subcategoryId == id) null else id) },
                        accentColor = kindAccentColor,
                        accentOnColor = kindAccentOnColor
                    )
                }
            }
        }

        if (draft.kind != EntryKind.INCOME) {
            item {
                Column {
                    Text("Payment method (tap one)", style = MaterialTheme.typography.labelLarge, color = colors.muted)
                    Spacer(Modifier.height(8.dp))
                    ChipFlow(
                        items = state.paymentMethods.map { it to it },
                        selected = draft.paymentMethod,
                        onSelect = viewModel::setPaymentMethod,
                        accentColor = kindAccentColor,
                        accentOnColor = kindAccentOnColor
                    )
                }
            }
        }

        if (!draft.showMore) {
            item {
                TextButton(onClick = viewModel::showMoreDetails) {
                    Text("+ Date & note (defaults to today, optional)")
                }
            }
        } else {
            item {
                OutlinedTextField(
                    value = draft.date,
                    onValueChange = viewModel::setDate,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (yyyy-mm-dd)") }
                )
            }
            item {
                OutlinedTextField(
                    value = draft.note,
                    onValueChange = viewModel::setNote,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") },
                    placeholder = { Text("optional note") }
                )
            }
        }

        item {
            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (draft.id != null) "Save changes" else when (draft.kind) {
                        EntryKind.INCOME -> "Add income"
                        EntryKind.EXPENSE -> "Add expense"
                        EntryKind.INVESTMENT -> "Add investment"
                    }
                )
            }
        }
    }
}

@Composable
private fun KindToggle(selected: EntryKind, onSelect: (EntryKind) -> Unit) {
    val colors = BudgetTheme.colors
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surfaceWell),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(4.dp)) {
            ToggleButton(
                label = "Income",
                icon = Icons.Outlined.ArrowCircleUp,
                active = selected == EntryKind.INCOME,
                activeColor = colors.income,
                modifier = Modifier.weight(1f)
            ) { onSelect(EntryKind.INCOME) }
            ToggleButton(
                label = "Expense",
                icon = Icons.Outlined.ArrowCircleDown,
                active = selected == EntryKind.EXPENSE,
                activeColor = colors.expense,
                modifier = Modifier.weight(1f)
            ) { onSelect(EntryKind.EXPENSE) }
            ToggleButton(
                label = "Investment",
                icon = Icons.Outlined.TrendingUp,
                active = selected == EntryKind.INVESTMENT,
                activeColor = colors.accentSecondary,
                modifier = Modifier.weight(1f)
            ) { onSelect(EntryKind.INVESTMENT) }
        }
    }
}

@Composable
private fun ToggleButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = BudgetTheme.colors
    val buttonColors = if (active) {
        ButtonDefaults.buttonColors(containerColor = activeColor, contentColor = Color(0xFF06301F))
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colors.muted
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = buttonColors,
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.height(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryChipFlow(
    categories: List<CategoryEntity>,
    selected: String?,
    onSelect: (String) -> Unit,
    accentColor: Color,
    accentOnColor: Color
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = category.id == selected,
                onClick = { onSelect(category.id) },
                label = { Text(category.name) },
                leadingIcon = { Icon(categoryIcon(category), contentDescription = null, modifier = Modifier.height(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = accentOnColor,
                    selectedLeadingIconColor = accentOnColor
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(
    items: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
    accentColor: Color,
    accentOnColor: Color
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (id, label) ->
            FilterChip(
                selected = id == selected,
                onClick = { onSelect(id) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor,
                    selectedLabelColor = accentOnColor
                )
            )
        }
    }
}
