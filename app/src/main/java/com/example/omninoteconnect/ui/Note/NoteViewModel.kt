package com.example.omninoteconnect.ui.Note

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.omninoteconnect.data.Note.NoteRepository
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.util.SortType
import kotlinx.coroutines.launch

class NoteViewModel (private val repository: NoteRepository) : ViewModel() {
    private val _sortParams = MutableLiveData<SortType>()

    init {
        _sortParams.value = SortType.TIME
    }

    val notes = _sortParams.switchMap {
        repository.getAllNote(it)
    }

    fun sort(newValue: SortType) {
        _sortParams.value = newValue
    }

    fun delete(note: Notes) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

}