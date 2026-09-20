package com.example.secondplayer

import android.content.Context
import android.graphics.Color

object ThemeManager {
    private const val PREFS_NAME = "player_themes"
    private const val KEY_COLOR = "accent_color"

    val THEME_COLORS = listOf(
        "#FFB703", // ذهبي ساطع
        "#9D4EDD", // أرجواني فاخر
        "#00F5D4", // أخضر نيون
        "#00B4D8", // أزرق سماوي
        "#FF2E63"  // وردي توهج
    )

    fun getAccentColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val colorHex = prefs.getString(KEY_COLOR, THEME_COLORS[0]) ?: THEME_COLORS[0]
        return Color.parseColor(colorHex)
    }

    fun setAccentColor(context: Context, colorHex: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_COLOR, colorHex).apply()
    }
}
