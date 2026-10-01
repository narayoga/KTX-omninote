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

    // catatan yang terakhir disimpan (sudah punya id), dipakai untuk menjadwalkan alarm
    var lastSavedNote: Notes? = null

    fun getNote(id: Int): LiveData<Notes?> {
        return repository.getNote(id)
    }

    // id = 0 berarti catatan baru, selain itu berarti edit catatan yang sudah ada
    fun saveNote(id: Int, title: String, date: Long, startTime: String, endTime: String, desc: String,
                 lat: Double, lon: Double, placeName: String, isDone: Boolean) {
        viewModelScope.launch {
            if (title.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                _saved.value = Event(false)
                return@launch
            }

            val note = Notes(
                id = id,
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                description = desc,
                latitude = lat,
                longitude = lon,
                placeName = placeName,
                isDone = isDone
            )

            if (id == 0) {
                val newId = repository.insert(note)
                lastSavedNote = note.copy(id = newId.toInt())
            } else {
                repository.update(note)
                lastSavedNote = note
            }
            _saved.value = Event(true)
        }
    }
}
