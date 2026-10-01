package com.example.omninoteconnect

import android.app.Application
import androidx.preference.PreferenceManager
import com.example.omninoteconnect.util.DarkModeHelper

// Class ini dijalankan paling awal setiap aplikasi dibuka (didaftarkan di AndroidManifest).
// Dipakai untuk menerapkan setting mode gelap sebelum layar apa pun muncul.
class OmniApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkMode = preferences.getString(getString(R.string.pref_key_dark), getString(R.string.pref_dark_auto))
        if (darkMode != null) {
            DarkModeHelper.applyDarkMode(this, darkMode)
        }
    }
}
