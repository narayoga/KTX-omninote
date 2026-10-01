package com.example.omninoteconnect.ui.Note.Detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.omninoteconnect.R
import com.example.omninoteconnect.alarm.AlarmHelper
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.databinding.ActivityNoteDetailBinding
import com.example.omninoteconnect.ui.Note.Add.NoteAddActivity
import com.example.omninoteconnect.ui.Note.NoteViewModelFactory
import com.example.omninoteconnect.util.NoteTimeHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var viewModel: NoteDetailViewModel

    // catatan yang sedang ditampilkan, null kalau belum dimuat
    private var currentNote: Notes? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = NoteViewModelFactory.createFactory(this)
        viewModel = ViewModelProvider(this, factory).get(NoteDetailViewModel::class.java)

        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        val noteId = intent.getIntExtra(NOTE_ID, 0)
        viewModel.getNote(noteId).observe(this) {
            if (it == null) {
                // catatan sudah dihapus (atau id-nya salah), tutup layar ini
                if (currentNote == null) {
                    Toast.makeText(this, getString(R.string.note_not_found), Toast.LENGTH_SHORT).show()
                }
                finish()
            } else {
                currentNote = it
                showNote(it)
            }
        }
    }

    private fun showNote(note: Notes) {
        binding.tvDetailTitle.text = note.title

        if (note.description.isEmpty()) {
            binding.tvDetailDesc.text = getString(R.string.no_description)
        } else {
            binding.tvDetailDesc.text = note.description
        }

        binding.tvDetailDate.text = NoteTimeHelper.formatDate(note.date)
        binding.tvDetailTime.text = getString(R.string.time_range, note.startTime, note.endTime)

        if (note.placeName.isNotEmpty()) {
            binding.tvDetailPlace.text = note.placeName
        } else {
            binding.tvDetailPlace.text = getString(R.string.location)
        }
        binding.tvDetailCoordinate.text = getString(R.string.location_format, note.latitude, note.longitude)

        if (note.isDone) {
            binding.tvStatus.text = getString(R.string.status_done)
            binding.btnToggleDone.text = getString(R.string.mark_not_done)
        } else {
            binding.tvStatus.text = getString(R.string.status_not_done)
            binding.btnToggleDone.text = getString(R.string.mark_done)
        }
    }

    private fun setupAction() {
        binding.detailBackBtn.setOnClickListener {
            finish()
        }

        binding.detailEditBtn.setOnClickListener {
            val note = currentNote
            if (note != null) {
                val intent = Intent(this, NoteAddActivity::class.java)
                intent.putExtra(NoteAddActivity.EXTRA_NOTE_ID, note.id)
                startActivity(intent)
            }
        }

        binding.btnToggleDone.setOnClickListener {
            val note = currentNote
            if (note != null) {
                val newDone = !note.isDone
                viewModel.setDone(note, newDone)
                if (newDone) {
                    AlarmHelper.cancelReminder(this, note.id)
                } else {
                    AlarmHelper.setReminder(this, note.copy(isDone = false))
                }
            }
        }

        binding.btnOpenMaps.setOnClickListener {
            val note = currentNote
            if (note != null) {
                openInMaps(note)
            }
        }

        binding.btnDelete.setOnClickListener {
            val note = currentNote
            if (note != null) {
                showDeleteDialog(note)
            }
        }
    }

    private fun showDeleteDialog(note: Notes) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_confirm_message))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                AlarmHelper.cancelReminder(this, note.id)
                viewModel.delete(note)
                Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun openInMaps(note: Notes) {
        // format "geo:" dipahami oleh Google Maps dan aplikasi peta lain
        var label = note.title
        if (note.placeName.isNotEmpty()) {
            label = note.placeName
        }
        val uri = Uri.parse("geo:${note.latitude},${note.longitude}?q=${note.latitude},${note.longitude}(${Uri.encode(label)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_maps_app), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val NOTE_ID = "noteId"
    }
}
