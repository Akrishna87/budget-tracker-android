package com.akrishna87.budgettracker.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The app's standard card: a flat surface with real elevation (shadow), not
 * a gradient fill - a gradient on every single card reads as an AI-generated
 * template rather than a considered design.
 */
@Composable
fun ElevatedPanel(
    modifier: Modifier = Modifier,
    contentPadding: Int = 18,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding.dp)) {
            content()
        }
    }
}
