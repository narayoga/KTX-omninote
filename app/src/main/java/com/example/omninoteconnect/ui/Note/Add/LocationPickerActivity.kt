package com.example.omninoteconnect.ui.Note.Add

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.omninoteconnect.R
import com.example.omninoteconnect.databinding.ActivityLocationPickerBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.Locale

// Layar untuk memilih lokasi kegiatan: user menggeser peta sampai pin di tengah
// berada di lokasi yang diinginkan, lalu menekan "Pakai lokasi ini".
class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityLocationPickerBinding
    private lateinit var mMap: GoogleMap

    private var selectedLatitude = 0.0
    private var selectedLongitude = 0.0
    private var isMapReady = false

    // nama tempat terakhir yang diisi otomatis dari alamat.
    // Dipakai supaya nama yang diketik user sendiri tidak ditimpa.
    private var lastAutoPlaceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        // kalau sebelumnya sudah ada nama tempat, tampilkan di kolom nama
        val oldPlaceName = intent.getStringExtra(EXTRA_PLACE_NAME)
        if (oldPlaceName != null) {
            binding.etPlaceName.setText(oldPlaceName)
        }
    }

    private fun setupAction() {
        binding.pickerBackBtn.setOnClickListener {
            finish()
        }

        binding.btnUseLocation.setOnClickListener {
            // titik belum didapat dari peta, jangan kirim koordinat 0,0
            if (!isMapReady) {
                Toast.makeText(this, getString(R.string.map_not_ready), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_LATITUDE, selectedLatitude)
            resultIntent.putExtra(EXTRA_LONGITUDE, selectedLongitude)
            resultIntent.putExtra(EXTRA_PLACE_NAME, binding.etPlaceName.text.toString().trim())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false

        // setiap peta selesai digeser, ambil titik tengahnya
        mMap.setOnCameraIdleListener {
            val center = mMap.cameraPosition.target
            selectedLatitude = center.latitude
            selectedLongitude = center.longitude
            isMapReady = true
            binding.tvPickerCoordinate.text = getString(R.string.location_format, selectedLatitude, selectedLongitude)
            findPlaceName(selectedLatitude, selectedLongitude)
        }

        moveToStartLocation()
    }

    @SuppressLint("MissingPermission")
    private fun moveToStartLocation() {
        val hasOldLocation = intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE)
        if (hasOldLocation) {
            // edit catatan: mulai dari lokasi yang sudah dipilih sebelumnya
            val lat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
            val lon = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16f))
        } else {
            // posisi awal Indonesia dulu, nanti dipindah ke lokasi HP kalau bisa
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-2.5, 118.0), 4f))
        }

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            if (!hasOldLocation) {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f))
                    }
                }
            }
        }
    }

    // Mencari nama jalan/tempat dari koordinat (reverse geocoding).
    // Butuh internet dan bisa lambat, jadi dijalankan di Thread terpisah.
    private fun findPlaceName(lat: Double, lon: Double) {
        if (!Geocoder.isPresent()) {
            return
        }
        val thread = Thread {
            var name = ""
            try {
                val geocoder = Geocoder(this, Locale("id", "ID"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0]
                    if (address.getAddressLine(0) != null) {
                        name = address.getAddressLine(0)
                    }
                }
            } catch (e: IOException) {
                // tidak ada internet / layanan sedang error, biarkan nama kosong
            }

            // mengubah tampilan harus di main thread
            runOnUiThread {
                // jangan ganti kalau peta sudah digeser lagi ke titik lain
                if (name.isNotEmpty() && lat == selectedLatitude && lon == selectedLongitude) {
                    val currentText = binding.etPlaceName.text.toString()
                    // hanya isi otomatis kalau kolomnya kosong atau masih berisi hasil otomatis sebelumnya
                    if (currentText.isEmpty() || currentText == lastAutoPlaceName) {
                        binding.etPlaceName.setText(name)
                        lastAutoPlaceName = name
                    }
                }
            }
        }
        thread.start()
    }

    companion object {
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
        const val EXTRA_PLACE_NAME = "extra_place_name"
    }
}
