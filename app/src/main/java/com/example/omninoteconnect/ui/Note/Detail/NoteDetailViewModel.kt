package com.example.omninoteconnect.ui.Note.Detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omninoteconnect.data.Note.NoteRepository
import com.example.omninoteconnect.data.Note.Notes
import kotlinx.coroutines.launch

class NoteDetailViewModel(private val repository: NoteRepository) : ViewModel() {

    fun getNote(id: Int): LiveData<Notes?> {
        return repository.getNote(id)
    }

    fun delete(note: Notes) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    fun setDone(note: Notes, isDone: Boolean) {
        viewModelScope.launch {
            repository.update(note.copy(isDone = isDone))
        }
    }
}
