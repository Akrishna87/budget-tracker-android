package com.akrishna87.budgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.akrishna87.budgettracker.ui.BudgetViewModelFactory
import com.akrishna87.budgettracker.ui.navigation.BudgetNavHost
import com.akrishna87.budgettracker.ui.theme.BudgetTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BudgetTrackerApp
        val factory = BudgetViewModelFactory(app.repository)

        setContent {
            BudgetTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BudgetNavHost(factory)
                }
            }
        }
    }
}
