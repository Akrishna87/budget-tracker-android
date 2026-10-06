package com.akrishna87.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IconBadge(
    emoji: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    small: Boolean = false
) {
    val size = if (small) 30.dp else 36.dp
    Box(
        modifier = modifier
            .size(size)
            .background(tint.copy(alpha = 0.16f), RoundedCornerShape(if (small) 9.dp else 11.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = if (small) 15.sp else 17.sp)
    }
}
