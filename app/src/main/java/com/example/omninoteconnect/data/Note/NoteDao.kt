package com.example.omninoteconnect.data.Note

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface NoteDao {
    @RawQuery(observedEntities = [Notes::class])
    fun getAll(query: SupportSQLiteQuery): DataSource.Factory<Int, Notes>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNote(id: Int): LiveData<Notes>

    @Insert
    suspend fun insert(notes: Notes)

    @Delete
    suspend fun delete(notes: Notes)

    @Query("SELECT * FROM notes ORDER BY :param")
    fun sort(param: String): DataSource.Factory<Int, Notes>
}