package com.akrishna87.budgettracker.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val indianGrouping: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
    maximumFractionDigits = 2
    minimumFractionDigits = 0
}

fun formatMoney(amount: Double, currencySymbol: String = "₹"): String {
    return currencySymbol + indianGrouping.format(amount)
}

fun todayKey(): String = LocalDate.now().toString() // yyyy-MM-dd

fun currentMonthKey(): String = LocalDate.now().toString().substring(0, 7) // yyyy-MM

fun monthKeyOfDate(date: String): String = date.substring(0, 7)

private val longDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

fun formatDateLong(date: String): String {
    return try {
        LocalDate.parse(date).format(longDateFormatter)
    } catch (e: Exception) {
        date
    }
}

fun monthLabel(monthKey: String): String {
    return try {
        val date = LocalDate.parse("$monthKey-01")
        val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        "$monthName ${date.year}"
    } catch (e: Exception) {
        monthKey
    }
}

fun shortMonthLabel(monthKey: String): String {
    return try {
        LocalDate.parse("$monthKey-01").month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    } catch (e: Exception) {
        monthKey
    }
}

/** Adds [months] to a "yyyy-MM" key, wrapping the year as needed. */
fun shiftMonthKey(monthKey: String, months: Int): String {
    return try {
        LocalDate.parse("$monthKey-01").plusMonths(months.toLong()).toString().substring(0, 7)
    } catch (e: Exception) {
        monthKey
    }
}

/**
 * The next occurrence of [dueDayOfMonth] that isn't in a month already covered by
 * [lastPaidMonth] ("yyyy-MM"). Skipping a paid month this way, rather than just
 * taking the closest occurrence on/after [from], means a bill paid ahead of its
 * due date correctly rolls to next month instead of appearing due again immediately.
 */
fun nextUnpaidDueDate(dueDayOfMonth: Int, lastPaidMonth: String?, from: LocalDate = LocalDate.now()): LocalDate {
    var candidateMonth = from.withDayOfMonth(1)
    while (true) {
        val monthKey = candidateMonth.toString().substring(0, 7)
        if (monthKey != lastPaidMonth) {
            return candidateMonth.withDayOfMonth(minOf(dueDayOfMonth, candidateMonth.lengthOfMonth()))
        }
        candidateMonth = candidateMonth.plusMonths(1)
    }
}
