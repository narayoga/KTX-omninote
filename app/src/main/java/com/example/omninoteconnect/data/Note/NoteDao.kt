package com.example.omninoteconnect.data.Note

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface NoteDao {
    @RawQuery(observedEntities = [Notes::class])
    fun getAll(query: SupportSQLiteQuery): DataSource.Factory<Int, Notes>

    // pakai Notes? karena hasilnya bisa null kalau catatannya sudah dihapus
    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNote(id: Int): LiveData<Notes?>

    // semua catatan tanpa paging, dipakai untuk ringkasan di Home
    @Query("SELECT * FROM notes ORDER BY date, startTime")
    fun getAllNotesList(): LiveData<List<Notes>>

    // dipanggil dari background thread (bukan main thread), untuk menjadwalkan ulang alarm
    @Query("SELECT * FROM notes WHERE isDone = 0")
    fun getActiveNotesSync(): List<Notes>

    // mengembalikan id catatan yang baru dibuat
    @Insert
    suspend fun insert(notes: Notes): Long

    @Update
    suspend fun update(notes: Notes)

    @Delete
    suspend fun delete(notes: Notes)
}
