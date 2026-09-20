package com.example.secondplayer

import android.content.Context
import android.graphics.Color

object ThemeManager {
    private const val PREFS_NAME = "player_themes"
    private const val KEY_COLOR = "accent_color"

    // الألوان المتاحة للثيمات (ذهبي، أرجواني، أخضر نيون، أزرق ساطع، أحمر)
    val THEME_COLORS = listOf(
        "#FFB703", // الذهبي الاصلي
        "#9D4EDD", // الأرجواني الفاخر
        "#00F5D4", // التيل / الأخضر النيون
        "#00B4D8", // الأزرق السماوي
        "#E63946"  # الأحادي العاطفي
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
