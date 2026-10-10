package com.akrishna87.budgettracker.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

fun monthKeyOfEpochMillis(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate().toString().substring(0, 7)

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
