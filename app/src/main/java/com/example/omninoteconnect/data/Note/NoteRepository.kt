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

    fun getCourse(id: Int): LiveData<Notes> {
        return dao.getNote(id)
    }

    suspend fun insert(note: Notes) {
        dao.insert(note)
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