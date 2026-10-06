package com.akrishna87.budgettracker.ui.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.data.db.TransactionType
import com.akrishna87.budgettracker.ui.theme.Expense
import com.akrishna87.budgettracker.ui.theme.Income
import com.akrishna87.budgettracker.ui.theme.Muted

@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val draft = state.draft
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    LaunchedEffect(Unit) {
        if (draft.id == null) focusRequester.requestFocus()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                if (draft.id != null) "Edit entry" else "Add entry",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            TypeToggle(
                selected = draft.type,
                onSelect = viewModel::setType
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

        if (draft.type == TransactionType.EXPENSE) {
            item {
                Column {
                    Text("Category (tap one)", style = MaterialTheme.typography.labelLarge, color = Muted)
                    Spacer(Modifier.height(8.dp))
                    ChipFlow(
                        items = state.categories.map { it.id to "${it.emoji} ${it.name}" },
                        selected = draft.categoryId,
                        onSelect = viewModel::setCategory
                    )
                }
            }
            item {
                Column {
                    Text("Payment method (tap one)", style = MaterialTheme.typography.labelLarge, color = Muted)
                    Spacer(Modifier.height(8.dp))
                    ChipFlow(
                        items = state.paymentMethods.map { it to it },
                        selected = draft.paymentMethod,
                        onSelect = viewModel::setPaymentMethod
                    )
                }
            }
        } else {
            item {
                OutlinedTextField(
                    value = draft.sourceName,
                    onValueChange = viewModel::setSourceName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Income source") },
                    placeholder = { Text("e.g. Salary – Primary") }
                )
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
                Text(if (draft.id != null) "Save changes" else if (draft.type == TransactionType.INCOME) "Add income" else "Add expense")
            }
        }
    }
}

@Composable
private fun TypeToggle(selected: TransactionType, onSelect: (TransactionType) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(4.dp)) {
            ToggleButton(
                label = "💰 Income",
                active = selected == TransactionType.INCOME,
                activeColor = Income,
                modifier = Modifier.weight(1f)
            ) { onSelect(TransactionType.INCOME) }
            ToggleButton(
                label = "💸 Expense",
                active = selected == TransactionType.EXPENSE,
                activeColor = Expense,
                modifier = Modifier.weight(1f)
            ) { onSelect(TransactionType.EXPENSE) }
        }
    }
}

@Composable
private fun ToggleButton(
    label: String,
    active: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = if (active) {
        ButtonDefaults.buttonColors(containerColor = activeColor, contentColor = Color(0xFF06301F))
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Muted
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(
    items: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit
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
                    selectedContainerColor = Expense,
                    selectedLabelColor = Color(0xFF3A1400)
                )
            )
        }
    }
}
