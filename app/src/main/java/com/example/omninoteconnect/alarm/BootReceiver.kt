package com.example.omninoteconnect.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Semua alarm hilang saat HP dimatikan/restart.
// Receiver ini dipanggil sistem setelah HP menyala lagi (dan setelah izin alarm tepat waktu berubah),
// lalu menjadwalkan ulang semua pengingat.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == ACTION_EXACT_ALARM_CHANGED) {
            AlarmHelper.rescheduleAll(context)
        }
    }

    companion object {
        // sama dengan AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED (Android 12+)
        const val ACTION_EXACT_ALARM_CHANGED = "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }
}
