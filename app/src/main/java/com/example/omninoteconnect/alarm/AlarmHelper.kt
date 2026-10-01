package com.example.omninoteconnect.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.example.omninoteconnect.R
import com.example.omninoteconnect.data.Note.NoteRepository
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.util.NoteTimeHelper

// Semua urusan menjadwalkan dan membatalkan alarm pengingat ada di sini.
// Setiap catatan punya alarm sendiri, dibedakan dengan id catatan.
object AlarmHelper {

    fun setReminder(context: Context, note: Notes) {
        // catatan yang sudah selesai tidak perlu diingatkan
        if (note.isDone) {
            cancelReminder(context, note.id)
            return
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isReminderOn = preferences.getBoolean(context.getString(R.string.pref_key_reminder), true)
        if (!isReminderOn) {
            cancelReminder(context, note.id)
            return
        }

        val startMillis = NoteTimeHelper.getStartMillis(note)
        if (startMillis == -1L) {
            return
        }

        // berapa menit sebelum kegiatan alarm berbunyi (diatur di Setting)
        val minutesText = preferences.getString(context.getString(R.string.pref_key_reminder_minutes), "10")
        var minutesBefore = 10
        if (minutesText != null && minutesText.toIntOrNull() != null) {
            minutesBefore = minutesText.toInt()
        }
        val alarmTime = startMillis - (minutesBefore * 60 * 1000L)

        // waktunya sudah lewat, tidak usah dijadwalkan
        if (alarmTime <= System.currentTimeMillis()) {
            cancelReminder(context, note.id)
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java)
        intent.putExtra(ReminderReceiver.EXTRA_NOTE_ID, note.id)
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, note.title)
        intent.putExtra(ReminderReceiver.EXTRA_START_TIME, note.startTime)
        intent.putExtra(ReminderReceiver.EXTRA_PLACE, note.placeName)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Android 5 belum punya mode hemat daya (doze)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        } else if (canUseExactAlarm(context)) {
            // alarm tepat waktu, tetap bunyi walaupun HP sedang mode hemat daya (doze)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        } else {
            // belum diizinkan alarm tepat waktu: tetap dijadwalkan, tapi bisa telat beberapa menit
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
        }
    }

    fun cancelReminder(context: Context, noteId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    // Mulai Android 12, aplikasi perlu izin khusus untuk alarm tepat waktu
    fun canUseExactAlarm(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    // Jadwalkan ulang semua catatan yang belum selesai.
    // Dipanggil setelah HP restart dan setelah setting pengingat diubah.
    // Database tidak boleh dibaca di main thread, jadi pakai Thread terpisah.
    fun rescheduleAll(context: Context) {
        val appContext = context.applicationContext
        val thread = Thread {
            val repository = NoteRepository.getInstance(appContext)
            if (repository != null) {
                val notes = repository.getActiveNotesSync()
                for (note in notes) {
                    setReminder(appContext, note)
                }
            }
        }
        thread.start()
    }
}
