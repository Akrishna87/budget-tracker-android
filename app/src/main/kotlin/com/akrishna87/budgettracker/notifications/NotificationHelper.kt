package com.akrishna87.budgettracker.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.akrishna87.budgettracker.R
import com.akrishna87.budgettracker.data.db.RecurringBillEntity
import com.akrishna87.budgettracker.util.formatMoney

private const val CHANNEL_ID = "bill_reminders"

fun createBillReminderChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bill reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for recurring bills and subscriptions coming due"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

fun showBillReminder(context: Context, bill: RecurringBillEntity, daysUntilDue: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val body = when {
        daysUntilDue <= 0 -> "${bill.name} (${formatMoney(bill.amount)}) is due today."
        daysUntilDue == 1L -> "${bill.name} (${formatMoney(bill.amount)}) is due tomorrow."
        else -> "${bill.name} (${formatMoney(bill.amount)}) is due in $daysUntilDue days."
    }
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle("Upcoming bill")
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(bill.id.hashCode(), notification)
}
