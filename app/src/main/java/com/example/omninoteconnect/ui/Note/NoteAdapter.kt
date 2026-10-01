package com.example.omninoteconnect.ui.Note

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.omninoteconnect.data.Note.Notes
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.databinding.RowNoteBinding
import com.example.omninoteconnect.util.NoteTimeHelper

class NoteAdapter(
    private val clickListener: (Notes) -> Unit,
    private val doneListener: (Notes, Boolean) -> Unit
) : PagedListAdapter<Notes, NoteViewHolder>(DIFF_CALLBACK){

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Notes>() {
            override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_note, parent, false)
        val binding = RowNoteBinding.bind(view)  // Inisialisasi binding di sini
        return NoteViewHolder(view, binding)     // Kirim binding ke NoteViewHolder
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val notes = getItem(position)
        if (notes !== null){
           holder.bind(notes, clickListener, doneListener)
        }
    }

}

class NoteViewHolder(view: View, binding: RowNoteBinding): RecyclerView.ViewHolder(view) {

    private lateinit var note: Notes
    private val timeString = itemView.context.resources.getString(R.string.time_format)
    private val locationString = itemView.context.resources.getString(R.string.location_format)

    private val titleTextView = binding.tvTitle
    private val timeTextView = binding.tvTime
    private val descTextView = binding.tvDesc
    private val locationTextView = binding.tvLocation
    private val doneCheckBox = binding.cbDone
    private val contentLayout = binding.llContent

    fun bind(notes: Notes, clickListener: (Notes) -> Unit, doneListener: (Notes, Boolean) -> Unit) {
        this.note = notes

        titleTextView.text = notes.title
        timeTextView.text = String.format(timeString, NoteTimeHelper.formatDate(notes.date), notes.startTime, notes.endTime)

        if (notes.description.isEmpty()) {
            descTextView.visibility = View.GONE
        } else {
            descTextView.visibility = View.VISIBLE
            descTextView.text = notes.description
        }

        // kalau ada nama tempat, tampilkan nama tempatnya. Kalau tidak, tampilkan koordinat
        if (notes.placeName.isNotEmpty()) {
            locationTextView.text = notes.placeName
        } else {
            locationTextView.text = String.format(locationString, notes.latitude, notes.longitude)
        }

        // catatan yang sudah selesai dicoret dan dibuat agak pudar
        doneCheckBox.isChecked = notes.isDone
        if (notes.isDone) {
            titleTextView.paintFlags = titleTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            contentLayout.alpha = 0.5f
        } else {
            titleTextView.paintFlags = titleTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            contentLayout.alpha = 1f
        }

        doneCheckBox.setOnClickListener {
            doneListener(notes, doneCheckBox.isChecked)
        }

        itemView.setOnClickListener{
            clickListener(notes)
        }
    }

    fun getNote(): Notes = note

}
