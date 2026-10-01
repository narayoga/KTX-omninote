package com.example.omninoteconnect.ui.Setting

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.example.omninoteconnect.R
import com.example.omninoteconnect.util.DarkModeHelper

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val darkModePreference = findPreference<ListPreference>(getString(R.string.pref_key_dark))
        if (darkModePreference != null) {
            darkModePreference.setOnPreferenceChangeListener { _, newValue ->
                // langsung terapkan mode gelap/terang yang dipilih
                DarkModeHelper.applyDarkMode(requireContext(), newValue.toString())
                true
            }
        }
    }
}
