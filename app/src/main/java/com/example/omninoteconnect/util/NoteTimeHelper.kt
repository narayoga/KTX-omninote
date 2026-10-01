package com.example.omninoteconnect.util

import com.example.omninoteconnect.data.Note.Notes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object NoteTimeHelper {

    // Menggabungkan tanggal catatan dengan jam mulai ("HH:mm") jadi satu waktu dalam milidetik.
    // Kalau format jam salah, hasilnya -1.
    fun getStartMillis(note: Notes): Long {
        val parts = note.startTime.split(":")
        if (parts.size != 2) {
            return -1
        }
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        if (hour == null || minute == null) {
            return -1
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = note.date
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Jam 00:00 hari ini
    fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // Jam 00:00 di tanggal yang sama dengan timeMillis
    fun getStartOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun isToday(timeMillis: Long): Boolean {
        return getStartOfDay(timeMillis) == getStartOfToday()
    }

    // Contoh hasil: "Rabu, 01 Okt 2026"
    fun formatDate(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID"))
        return dateFormat.format(timeMillis)
    }
}
