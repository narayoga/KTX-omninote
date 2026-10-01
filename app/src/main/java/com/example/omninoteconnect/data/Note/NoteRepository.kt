package com.example.omninoteconnect.data.Note

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.omninoteconnect.util.QueryUtil
import com.example.omninoteconnect.util.SortType

class NoteRepository(private val dao: NoteDao) {
    fun getAllNote(sortType: SortType): LiveData<PagedList<Notes>> {
        val query = QueryUtil.sortedQuery(sortType)
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .setEnablePlaceholders(false)
            .build()
        return LivePagedListBuilder(dao.getAll(query), config).build()
    }

    fun getNote(id: Int): LiveData<Notes?> {
        return dao.getNote(id)
    }

    fun getAllNotesList(): LiveData<List<Notes>> {
        return dao.getAllNotesList()
    }

    fun getActiveNotesSync(): List<Notes> {
        return dao.getActiveNotesSync()
    }

    suspend fun insert(note: Notes): Long {
        return dao.insert(note)
    }

    suspend fun update(note: Notes) {
        dao.update(note)
    }

    suspend fun delete(note: Notes) {
        dao.delete(note)
    }

    companion object {
        @Volatile
        private var instance: NoteRepository? = null
        private const val PAGE_SIZE = 10

        fun getInstance(context: Context): NoteRepository? {
            return instance ?: synchronized(NoteRepository::class.java) {
                if (instance == null) {
                    val database = NoteDatabase.getInstance(context)
                    instance = NoteRepository(database.noteDao())
                }
                return instance
            }
        }
    }

}
