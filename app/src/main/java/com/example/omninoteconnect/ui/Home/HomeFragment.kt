package com.example.omninoteconnect.ui.Home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.alarm.AlarmHelper
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.databinding.FragmentHomeBinding
import com.example.omninoteconnect.ui.Note.Detail.NoteDetailActivity
import com.example.omninoteconnect.ui.Note.NoteAdapter
import com.example.omninoteconnect.ui.Note.NoteViewHolder
import com.example.omninoteconnect.ui.Note.NoteViewModel
import com.example.omninoteconnect.ui.Note.NoteViewModelFactory
import com.example.omninoteconnect.util.NoteTimeHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    // binding di fragment dibuat nullable karena view-nya bisa dihancurkan
    // duluan sebelum fragment-nya (lihat onDestroyView)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NoteViewModel
    private lateinit var rvNotes: RecyclerView
    private var nextNoteId = 0
    private val noteAdapter: NoteAdapter by lazy {
        NoteAdapter(::onNoteClick, ::onNoteDoneChanged)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = NoteViewModelFactory.createFactory(requireActivity())
        viewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)

        setupView()
        setUpRecycler()
        setupAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupView() {
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        binding.tvTodayDate.text = dateFormat.format(Date())

        viewModel.notes.observe(viewLifecycleOwner) {
            noteAdapter.submitList(it)
            if (it.isEmpty()) {
                binding.llEmpty.visibility = View.VISIBLE
            } else {
                binding.llEmpty.visibility = View.GONE
            }
        }

        viewModel.allNotes.observe(viewLifecycleOwner) {
            showSummary(it)
        }
    }

    // Menghitung isi kartu "Kegiatan berikutnya" dan "Ringkasan"
    private fun showSummary(notes: List<Notes>) {
        val now = System.currentTimeMillis()
        var todayCount = 0
        var doneCount = 0
        var upcomingCount = 0
        var nextNote: Notes? = null
        var nextNoteStart = 0L

        for (note in notes) {
            if (NoteTimeHelper.isToday(note.date)) {
                todayCount++
            }
            if (note.isDone) {
                doneCount++
            }

            val start = NoteTimeHelper.getStartMillis(note)
            if (!note.isDone && start > now) {
                upcomingCount++
                // cari catatan yang waktu mulainya paling dekat
                if (nextNote == null || start < nextNoteStart) {
                    nextNote = note
                    nextNoteStart = start
                }
            }
        }

        binding.tvCountToday.text = todayCount.toString()
        binding.tvCountDone.text = getString(R.string.done_count, doneCount, notes.size)
        binding.tvCountUpcoming.text = upcomingCount.toString()

        if (nextNote == null) {
            nextNoteId = 0
            binding.tvNextTitle.text = getString(R.string.no_next_reminder)
            binding.tvNextTime.visibility = View.GONE
            binding.tvNextPlace.visibility = View.GONE
        } else {
            nextNoteId = nextNote.id
            binding.tvNextTitle.text = nextNote.title

            var day = NoteTimeHelper.formatDate(nextNote.date)
            if (NoteTimeHelper.isToday(nextNote.date)) {
                day = getString(R.string.today)
            }
            binding.tvNextTime.text = getString(R.string.time_format, day, nextNote.startTime, nextNote.endTime)
            binding.tvNextTime.visibility = View.VISIBLE

            if (nextNote.placeName.isNotEmpty()) {
                binding.tvNextPlace.text = nextNote.placeName
                binding.tvNextPlace.visibility = View.VISIBLE
            } else {
                binding.tvNextPlace.visibility = View.GONE
            }
        }
    }

    private fun setUpRecycler() {
        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(requireContext())
        rvNotes.adapter = noteAdapter
    }

    private fun setupAction() {
        // geser catatan ke kanan untuk menghapus
        val callback = Callback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvNotes)

        binding.cardNext.setOnClickListener {
            if (nextNoteId != 0) {
                openDetail(nextNoteId)
            }
        }
    }

    private fun onNoteClick(notes: Notes) {
        openDetail(notes.id)
    }

    private fun onNoteDoneChanged(notes: Notes, isDone: Boolean) {
        viewModel.setDone(notes, isDone)
        // selesai = alarm dibatalkan, belum selesai = alarm dijadwalkan lagi
        if (isDone) {
            AlarmHelper.cancelReminder(requireContext(), notes.id)
        } else {
            AlarmHelper.setReminder(requireContext(), notes.copy(isDone = false))
        }
    }

    private fun openDetail(noteId: Int) {
        val intent = Intent(requireContext(), NoteDetailActivity::class.java)
        intent.putExtra(NoteDetailActivity.NOTE_ID, noteId)
        startActivity(intent)
    }

    inner class Callback : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(0, ItemTouchHelper.RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val note = (viewHolder as NoteViewHolder).getNote()
            viewModel.delete(note)
            AlarmHelper.cancelReminder(requireContext(), note.id)
            Toast.makeText(requireContext(), getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
        }
    }
}
