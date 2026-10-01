package com.example.omninoteconnect.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.omninoteconnect.R

object DarkModeHelper {

    // value: "auto", "on", atau "off" (lihat array dark_mode_value di arrays.xml)
    fun applyDarkMode(context: Context, value: String) {
        if (value == context.getString(R.string.pref_dark_on)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (value == context.getString(R.string.pref_dark_off)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
