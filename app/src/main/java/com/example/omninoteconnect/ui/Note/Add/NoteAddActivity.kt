package com.example.omninoteconnect.ui.Note.Add

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.omninoteconnect.databinding.ActivityNoteAddBinding
import com.example.omninoteconnect.R
import com.example.omninoteconnect.alarm.AlarmHelper
import com.example.omninoteconnect.data.Note.Notes
import com.example.omninoteconnect.ui.Note.NoteAddViewModelFactory
import com.example.omninoteconnect.util.DatePickerFragment
import com.example.omninoteconnect.util.NoteTimeHelper
import com.example.omninoteconnect.util.TimePickerFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Locale

class NoteAddActivity : AppCompatActivity(), DatePickerFragment.DialogDateListener, TimePickerFragment.DialogTimeListener {

    private lateinit var tvStart: TextView
    private lateinit var tvEnd: TextView
    private lateinit var date: LinearLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var submitBtn: View
    private lateinit var backBtn: View
    private lateinit var viewModel: NoteAddViewModel
    private lateinit var binding: ActivityNoteAddBinding
    private var dueDate: Long = NoteTimeHelper.getStartOfToday()

    // lokasi yang dipilih, null kalau belum ada
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var placeName = ""

    // 0 = catatan baru, selain 0 = sedang mengedit catatan dengan id tersebut
    private var editNoteId = 0
    private var isDone = false
    private var isNoteLoaded = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // menerima hasil dari LocationPickerActivity
    private val pickLocationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            latitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_LATITUDE, 0.0)
            longitude = data.getDoubleExtra(LocationPickerActivity.EXTRA_LONGITUDE, 0.0)
            val name = data.getStringExtra(LocationPickerActivity.EXTRA_PLACE_NAME)
            if (name != null) {
                placeName = name
            } else {
                placeName = ""
            }
            showLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@NoteAddActivity)

        etTitle = binding.etNoteTitle
        date = binding.llDate
        tvStart = binding.tvStartTime
        tvEnd = binding.tvEndTime
        etDesc = binding.etNoteDesc

        val viewFactory = NoteAddViewModelFactory.createFactory(this)
        viewModel = ViewModelProvider(this, viewFactory)[NoteAddViewModel::class.java]
        viewModel.saved.observe(this) {
            val isSaved = it.getContentIfNotHandled()
            if (isSaved == true) {
                // jadwalkan alarm pengingat untuk catatan yang baru disimpan
                val savedNote = viewModel.lastSavedNote
                if (savedNote != null) {
                    AlarmHelper.setReminder(this, savedNote)
                }
                finish()
            } else if (isSaved == false) {
                Toast.makeText(this, getString(R.string.input_empty_message), Toast.LENGTH_SHORT).show()
            }
        }

        editNoteId = intent.getIntExtra(EXTRA_NOTE_ID, 0)
        if (editNoteId != 0) {
            binding.tvAddTitle.text = getString(R.string.edit_note_title)
            loadNote()
        } else {
            binding.addTvDueDate.text = NoteTimeHelper.formatDate(dueDate)
            // catatan baru: isi lokasi awal dengan lokasi HP saat ini
            checkPermission()
        }

        setupView()
        setupAction()
        showLocation()
    }

    private fun loadNote() {
        viewModel.getNote(editNoteId).observe(this) {
            // isi form hanya sekali, supaya ketikan user tidak tertimpa
            if (it != null && !isNoteLoaded) {
                isNoteLoaded = true
                fillForm(it)
            }
        }
    }

    private fun fillForm(note: Notes) {
        etTitle.setText(note.title)
        etDesc.setText(note.description)
        dueDate = note.date
        binding.addTvDueDate.text = NoteTimeHelper.formatDate(note.date)
        tvStart.text = note.startTime
        tvEnd.text = note.endTime
        latitude = note.latitude
        longitude = note.longitude
        placeName = note.placeName
        isDone = note.isDone
        showLocation()
    }

    private fun showLocation() {
        val lat = latitude
        val lon = longitude
        if (lat == null || lon == null) {
            binding.tvPlaceName.text = getString(R.string.choose_location)
            binding.tvCoordinate.text = getString(R.string.tap_to_choose_location)
        } else {
            if (placeName.isNotEmpty()) {
                binding.tvPlaceName.text = placeName
            } else {
                binding.tvPlaceName.text = getString(R.string.current_location)
            }
            binding.tvCoordinate.text = getString(R.string.location_format, lat, lon)
        }
    }

    private fun setupAction() {
        backBtn = binding.addNoteBackBtn
        submitBtn = binding.addNoteSubmitBtn

        backBtn.setOnClickListener{
            finish()
        }

        binding.cardLocation.setOnClickListener {
            val intent = Intent(this, LocationPickerActivity::class.java)
            val lat = latitude
            val lon = longitude
            if (lat != null && lon != null) {
                intent.putExtra(LocationPickerActivity.EXTRA_LATITUDE, lat)
                intent.putExtra(LocationPickerActivity.EXTRA_LONGITUDE, lon)
                intent.putExtra(LocationPickerActivity.EXTRA_PLACE_NAME, placeName)
            }
            pickLocationLauncher.launch(intent)
        }

        submitBtn.setOnClickListener{
            val lat = latitude
            val lon = longitude

            // lokasi wajib dipilih dulu
            if (lat == null || lon == null) {
                Toast.makeText(this, getString(R.string.location_empty_message), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.saveNote(
                    editNoteId,
                    etTitle.text.toString().trim(),
                    dueDate,
                    tvStart.text.toString(),
                    tvEnd.text.toString(),
                    etDesc.text.toString().trim(),
                    lat,
                    lon,
                    placeName,
                    isDone
                )
            }
        }
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
    }

    private fun checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            getMyLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private  fun getMyLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it == null) {
                Toast.makeText(this, getString(R.string.location_failed), Toast.LENGTH_SHORT).show()
            } else if (latitude == null) {
                // hanya diisi kalau user belum memilih lokasi sendiri
                latitude = it.latitude
                longitude = it.longitude
                placeName = ""
                showLocation()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLocation()
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showDatePicker(view: View) {
        val dialogFragment = DatePickerFragment()
        dialogFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        // simpan tanggalnya di jam 00:00 supaya gampang dibandingkan
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        dueDate = calendar.timeInMillis
        binding.addTvDueDate.text = NoteTimeHelper.formatDate(dueDate)
    }

    override fun onDialogTimeSet(tag: String?, hour: Int, minute: Int) {
        // format jam selalu 2 digit, contoh 07:05 (Locale.US supaya angkanya selalu 0-9)
        val time = String.format(Locale.US, "%02d:%02d", hour, minute)
        if (tag == "startTimePicker") {
            tvStart.text = time
        } else {
            tvEnd.text = time
        }
    }


    fun showStartTimePicker(view: View) {
        val dialogFragment = TimePickerFragment()
        dialogFragment.show(supportFragmentManager, "startTimePicker")
    }

    fun showEndTimePicker(view: View) {
        val dialogFragment = TimePickerFragment()
        dialogFragment.show(supportFragmentManager, "endTimePicker")
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }

}
