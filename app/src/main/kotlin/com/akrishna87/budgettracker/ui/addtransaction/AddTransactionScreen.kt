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
import com.akrishna87.budgettracker.data.db.SubcategoryEntity
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
        item {
            Column {
                Text("Category (tap one)", style = MaterialTheme.typography.labelLarge, color = colors.muted)
                Spacer(Modifier.height(8.dp))
                if (state.categories.isEmpty()) {
                    Text("No categories yet. Add one in Settings.", style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.categories.sortedBy { it.sortOrder }.forEach { category ->
                            CategoryGroup(
                                category = category,
                                subcategories = state.subcategories.filter { it.categoryId == category.id },
                                selectedCategoryId = draft.categoryId,
                                selectedSubcategoryId = draft.subcategoryId,
                                accentColor = kindAccentColor,
                                accentOnColor = kindAccentOnColor,
                                onSelectCategory = { viewModel.setCategory(category.id) },
                                onSelectSubcategory = { subcategoryId ->
                                    val newSubcategoryId = if (draft.categoryId == category.id && draft.subcategoryId == subcategoryId) {
                                        null
                                    } else {
                                        subcategoryId
                                    }
                                    viewModel.setCategoryAndSubcategory(category.id, newSubcategoryId)
                                }
                            )
                        }
                    }
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

/**
 * One category "header" chip, with its subcategories (if any) shown as a
 * chip row nested right underneath it - always, not gated behind first
 * selecting the category - so every category you've added in Settings, and
 * everything you've filed under it, is visible at a glance.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGroup(
    category: CategoryEntity,
    subcategories: List<SubcategoryEntity>,
    selectedCategoryId: String?,
    selectedSubcategoryId: String?,
    accentColor: Color,
    accentOnColor: Color,
    onSelectCategory: () -> Unit,
    onSelectSubcategory: (String) -> Unit
) {
    val isCategorySelected = category.id == selectedCategoryId
    Column {
        FilterChip(
            selected = isCategorySelected,
            onClick = onSelectCategory,
            label = { Text(category.name, fontWeight = FontWeight.SemiBold) },
            leadingIcon = { Icon(categoryIcon(category), contentDescription = null, modifier = Modifier.height(16.dp)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = accentColor,
                selectedLabelColor = accentOnColor,
                selectedLeadingIconColor = accentOnColor
            )
        )
        if (subcategories.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.padding(start = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subcategories.forEach { subcategory ->
                    FilterChip(
                        selected = isCategorySelected && subcategory.id == selectedSubcategoryId,
                        onClick = { onSelectSubcategory(subcategory.id) },
                        label = { Text(subcategory.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = accentOnColor
                        )
                    )
                }
            }
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
