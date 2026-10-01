package com.example.omninoteconnect.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.omninoteconnect.R
import com.example.omninoteconnect.ui.Note.Detail.NoteDetailActivity

// Dipanggil oleh AlarmManager saat waktu pengingat tiba, lalu menampilkan notifikasi
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val startTime = intent.getStringExtra(EXTRA_START_TIME)
        val place = intent.getStringExtra(EXTRA_PLACE)

        if (noteId == -1 || title == null) {
            return
        }

        var message = context.getString(R.string.reminder_message, startTime)
        if (place != null && place.isNotEmpty()) {
            message = context.getString(R.string.reminder_message_with_place, startTime, place)
        }

        showNotification(context, noteId, title, message)
    }

    private fun showNotification(context: Context, noteId: Int, title: String, message: String) {
        // Android 13 ke atas: notifikasi hanya boleh tampil kalau izinnya sudah diberikan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // Android 8 ke atas: notifikasi wajib punya channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = context.getString(R.string.reminder_channel_desc)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // kalau notifikasi diklik, buka detail catatan
        val detailIntent = Intent(context, NoteDetailActivity::class.java)
        detailIntent.putExtra(NoteDetailActivity.NOTE_ID, noteId)
        detailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            detailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(noteId, builder.build())
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_PLACE = "extra_place"
        private const val CHANNEL_ID = "reminder_channel"
    }
}
