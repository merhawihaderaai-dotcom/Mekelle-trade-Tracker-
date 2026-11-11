package com.example.utils

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    // Modes: "light", "dark", "system"
    fun getThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME_MODE, "dark") ?: "dark" // default to dark
    }

    fun setThemeMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }
}
