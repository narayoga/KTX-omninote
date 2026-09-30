package com.example.omninoteconnect.ui.Note.Detail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.omninoteconnect.R

class NoteDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)
    }

    companion object {
        const val NOTE_ID = "noteId"
    }
}