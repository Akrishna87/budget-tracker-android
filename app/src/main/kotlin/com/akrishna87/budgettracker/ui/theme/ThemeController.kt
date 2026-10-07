package com.akrishna87.budgettracker.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Owns both the persisted theme choice and a live Compose-observable copy of
 * it, so changing the theme in Settings updates the running app immediately
 * instead of only taking effect on next launch.
 */
class ThemeController(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    var mode: ThemeMode by mutableStateOf(readPersisted())
        private set

    fun setMode(mode: ThemeMode) {
        this.mode = mode
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    private fun readPersisted(): ThemeMode {
        return ThemeMode.entries.find { it.name == prefs.getString(KEY_MODE, null) } ?: ThemeMode.SYSTEM
    }

    private companion object {
        const val KEY_MODE = "theme_mode"
    }
}
