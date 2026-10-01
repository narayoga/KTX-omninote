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
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.databinding.FragmentHomeBinding
import com.example.omninoteconnect.ui.Note.Detail.NoteDetailActivity
import com.example.omninoteconnect.ui.Note.NoteAdapter
import com.example.omninoteconnect.ui.Note.NoteViewHolder
import com.example.omninoteconnect.ui.Note.NoteViewModel
import com.example.omninoteconnect.ui.Note.NoteViewModelFactory
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
    private val noteAdapter: NoteAdapter by lazy {
        NoteAdapter(::onNoteClick)
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
    }

    private fun onNoteClick(notes: Notes) {
        val intent = Intent(requireContext(), NoteDetailActivity::class.java)
        intent.putExtra(NoteDetailActivity.NOTE_ID, notes.id)
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
            Toast.makeText(requireContext(), getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
        }
    }
}
