package com.akrishna87.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akrishna87.budgettracker.ui.theme.Surface
import com.akrishna87.budgettracker.ui.theme.SurfaceVariant

/**
 * The app's standard card: a subtle top-to-bottom gradient (instead of a
 * flat fill) plus real elevation, so cards read as raised surfaces rather
 * than just differently-colored rectangles.
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(SurfaceVariant, Surface)))
                .padding(contentPadding.dp)
        ) {
            content()
        }
    }
}
