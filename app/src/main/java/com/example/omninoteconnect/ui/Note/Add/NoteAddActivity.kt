package com.example.omninoteconnect.ui.Note.Add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.omninoteconnect.databinding.ActivityNoteAddBinding
import com.example.omninoteconnect.ui.Map.MapsActivity
import com.example.omninoteconnect.ui.Note.NoteAddViewModelFactory
import com.example.omninoteconnect.util.DatePickerFragment
import com.example.omninoteconnect.util.TimePickerFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NoteAddActivity : AppCompatActivity(), DatePickerFragment.DialogDateListener, TimePickerFragment.DialogTimeListener {

    private lateinit var tvStart: TextView
    private lateinit var tvEnd: TextView
    private lateinit var date: LinearLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var etNoteLatitude: TextInputEditText
    private lateinit var etNoteLongitude: TextInputEditText
    private lateinit var submitBtn: View
    private lateinit var backBtn: View
    private lateinit var lontongBtn: View
    private lateinit var viewModel: NoteAddViewModel
    private lateinit var binding: ActivityNoteAddBinding
    private var dueDate: Long = System.currentTimeMillis()

    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@NoteAddActivity);

        etTitle = binding.etNoteTitle
        date = binding.llDate
        tvStart = binding.startTimeText
        tvEnd = binding.endTimeText
        etDesc = binding.etNoteDesc
        etNoteLatitude = binding.etNoteLatitude
        etNoteLongitude = binding.etNoteLongitude

        val viewFactory = NoteAddViewModelFactory.createFactory(this)
        viewModel = ViewModelProvider(this, viewFactory)[NoteAddViewModel::class.java]
        viewModel.saved.observe(this) {
            if (it.getContentIfNotHandled() == true) {
                finish()
            } else {
                Log.d("addNote", "$it")
            }
        }

        checkPermission()
        setupView()
        setupAction()
    }

    private fun setupAction() {
        backBtn = binding.addNoteBackBtn
        submitBtn = binding.addNoteSubmitBtn

        backBtn.setOnClickListener{
            onBackPressed()
        }

        submitBtn.setOnClickListener{
            viewModel.insertNote(
                etTitle.text.toString().trim(),
                dueDate,
                tvStart.text.toString(),
                tvEnd.text.toString(),
                etDesc.text.toString().trim(),
                etNoteLatitude.text.toString().trim().toDouble(),
                etNoteLongitude.text.toString().trim().toDouble()
            )
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
                Toast.makeText(this, "Sorry can't get location $it", Toast.LENGTH_SHORT).show()
            } else it.apply {
                etNoteLatitude.setText(it.latitude.toString())
                etNoteLongitude.setText(it.longitude.toString())
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    getMyLocation()
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun showDatePicker(view: View) {
        val dialogFragment = DatePickerFragment()
        dialogFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDialogDateSet(tag: String?, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.addTvDueDate.text = dateFormat.format(calendar.time)
        dueDate = calendar.timeInMillis
    }

    override fun onDialogTimeSet(tag: String?, hour: Int, minute: Int) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        val formatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (tag == "startTimePicker") {
            tvStart.text = formatTime.format(cal.time)
        } else {
            tvEnd.text = formatTime.format(cal.time)
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

}