package com.akrishna87.budgettracker.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.akrishna87.budgettracker.MainActivity
import com.akrishna87.budgettracker.ui.theme.Accent
import com.akrishna87.budgettracker.ui.theme.DarkBudgetColors

class QuickAddWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val openAddIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_ADD, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(Accent))
                    .clickable(actionStartActivity(openAddIntent)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "+ Add expense",
                    style = TextStyle(
                        color = ColorProvider(DarkBudgetColors.accentOnColor),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "open_add"
    }
}
