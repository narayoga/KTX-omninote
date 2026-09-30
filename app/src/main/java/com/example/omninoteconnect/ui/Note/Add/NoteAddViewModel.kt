package com.example.omninoteconnect.ui.Note.Add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omninoteconnect.data.Note.NoteRepository
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.util.Event
import kotlinx.coroutines.launch

class NoteAddViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _saved = MutableLiveData<Event<Boolean>>()
    val saved: LiveData<Event<Boolean>> get() = _saved

    fun insertNote(title: String, date: Long, startTime: String, endTime: String, desc: String, lat:Double, lon:Double) {
        viewModelScope.launch {
            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                _saved.value = Event(false)
                return@launch
            }

            val newNote = Notes(
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                description = desc,
                latitude = lat,
                longitude = lon
            )
            repository.insert(newNote)
            _saved.value = Event(true)
        }
    }
}