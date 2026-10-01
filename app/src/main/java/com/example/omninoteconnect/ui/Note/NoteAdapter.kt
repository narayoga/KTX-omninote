package com.example.omninoteconnect.ui.Note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.omninoteconnect.data.Note.Notes
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.databinding.RowNoteBinding
import com.example.omninoteconnect.util.DateConverter

class NoteAdapter(private val clickListener:(Notes) -> Unit) :
    PagedListAdapter<Notes, NoteViewHolder>(DIFF_CALLBACK){

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
           holder.bind(notes, clickListener)
        }
    }

}

class NoteViewHolder(view: View, binding: RowNoteBinding): RecyclerView.ViewHolder(view) {

    private lateinit var note: Notes
    private val timeString = itemView.context.resources.getString(R.string.time_format)

    private val titleTextView = binding.tvTitle
    private val timeTextView = binding.tvTime
    private val descTextView = binding.tvDesc
    private val locationTextView = binding.tvLocation
    private val locationString = itemView.context.resources.getString(R.string.location_format)

    fun bind(notes: Notes, clickListener: (Notes) -> Unit) {
        this.note = notes

        note.apply {
            val timeFormat = String.format(timeString, DateConverter.convertMillisToString(notes.date), startTime, endTime )
            titleTextView.text = title
            timeTextView.text = timeFormat
            descTextView.text = description
            locationTextView.text = String.format(locationString, latitude, longitude)
        }

        itemView.setOnClickListener{
            clickListener(notes)
        }
    }

    fun getNote(): Notes = note

}