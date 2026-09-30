package com.example.omninoteconnect.ui.Note

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.databinding.ActivityNoteBinding
import com.example.omninoteconnect.ui.Note.Add.NoteAddActivity
import com.example.omninoteconnect.ui.Note.Detail.NoteDetailActivity

class NoteActivity : AppCompatActivity() {

    private lateinit var syncBtn: View
    private lateinit var addBtn: View
    private lateinit var fabNote: View
    private var rotate = false

    private lateinit var viewModel: NoteViewModel
    private lateinit var rvNotes: RecyclerView
    private lateinit var binding: ActivityNoteBinding
    private val noteAdapter: NoteAdapter by lazy {
        NoteAdapter(::onNoteClick)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = NoteViewModelFactory.createFactory(this)
        viewModel = ViewModelProvider(this, factory).get(NoteViewModel::class.java)

        setupView()
        setUpRecycler()
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
        viewModel.notes.observe(this) {
            noteAdapter.submitList(it)
            binding.tvEmptyList.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setUpRecycler() {
        rvNotes = binding.rvNotes
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = noteAdapter
    }

    private fun setupAction() {
        syncBtn = binding.syncBtn
        addBtn = binding.addBtn
        fabNote = binding.fabNote

        syncBtn.setOnClickListener{
            Toast.makeText(this, "synchronize", Toast.LENGTH_SHORT).show()
        }

        addBtn.setOnClickListener{
            val intent = Intent(this, NoteAddActivity::class.java)
            startActivity(intent)
        }

        fabNote.setOnClickListener{
            toggleFab(it)
        }
        val callback = Callback()
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(rvNotes)
    }

    private fun toggleFab(view: View) {
        rotate = rotateFab(view, !rotate)
        if(rotate) {
            showIn(syncBtn)
            showIn(addBtn)
        } else {
            showOut(syncBtn)
            showOut(addBtn)
        }
    }

    private fun showOut(view: View) {
        view.apply {
            visibility = View.VISIBLE
            alpha = 1f
            translationY = 0f
            animate()
                .setDuration(200)
                .translationY(height.toFloat())
                .setListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.GONE
                        super.onAnimationEnd(animation)
                    }
                })
                .alpha(0f)
                .start()
        }
    }

    private fun showIn(view: View) {
        view.apply { 
            visibility = View.VISIBLE
            alpha = 0f
            translationY = height.toFloat()
            animate()
                .setDuration(200)
                .translationY(0f)
                .setListener(object : AnimatorListenerAdapter(){})
                .alpha(1f)
                .start()
        }
    }

    private fun rotateFab(view: View, rotate: Boolean): Boolean {
        view.animate()
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter(){})
            .rotation(if(rotate) 180f else 0f)
        return rotate
    }

    private fun onNoteClick(notes: Notes) {
        val intent = Intent(this, NoteDetailActivity::class.java)
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
            val course = (viewHolder as NoteViewHolder).getNote()
            viewModel.delete(course)
        }
    }
}