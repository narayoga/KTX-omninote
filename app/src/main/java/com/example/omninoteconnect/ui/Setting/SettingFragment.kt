package com.example.omninoteconnect.ui.Setting

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.omninoteconnect.R
import com.example.omninoteconnect.alarm.AlarmHelper
import com.example.omninoteconnect.util.DarkModeHelper

class SettingFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

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

        val exactAlarmPreference = findPreference<Preference>(getString(R.string.pref_key_exact_alarm))
        if (exactAlarmPreference != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                exactAlarmPreference.setOnPreferenceClickListener {
                    // buka halaman sistem untuk mengizinkan alarm tepat waktu
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.data = Uri.parse("package:" + requireContext().packageName)
                    startActivity(intent)
                    true
                }
            } else {
                // Android 11 ke bawah tidak butuh izin ini
                exactAlarmPreference.isVisible = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        updateExactAlarmSummary()
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    // Dipanggil setelah sebuah setting tersimpan
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.pref_key_reminder) || key == getString(R.string.pref_key_reminder_minutes)) {
            // pengingat dinyalakan/dimatikan atau waktunya diubah: jadwalkan ulang semua alarm
            AlarmHelper.rescheduleAll(requireContext())
        }
    }

    private fun updateExactAlarmSummary() {
        val exactAlarmPreference = findPreference<Preference>(getString(R.string.pref_key_exact_alarm))
        if (exactAlarmPreference != null) {
            if (AlarmHelper.canUseExactAlarm(requireContext())) {
                exactAlarmPreference.summary = getString(R.string.pref_exact_alarm_on)
            } else {
                exactAlarmPreference.summary = getString(R.string.pref_exact_alarm_off)
            }
        }
    }
}
