package com.akrishna87.budgettracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrishna87.budgettracker.ui.theme.Accent
import com.akrishna87.budgettracker.ui.theme.BudgetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = {
            Text(
                title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                BrandBadge()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun BrandBadge() {
    val colors = BudgetTheme.colors
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                Brush.linearGradient(listOf(Accent, colors.accentSecondary)),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("₹", color = colors.accentOnColor, fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}
