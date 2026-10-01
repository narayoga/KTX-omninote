package com.example.omninoteconnect.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.omninoteconnect.R
import com.example.omninoteconnect.databinding.ActivityDashboardBinding
import com.example.omninoteconnect.ui.Home.HomeFragment
import com.example.omninoteconnect.ui.Map.MapFragment
import com.example.omninoteconnect.ui.Note.Add.NoteAddActivity
import com.example.omninoteconnect.ui.Notification.NotificationFragment
import com.example.omninoteconnect.ui.Profile.ProfileFragment

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private var currentTab = TAB_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        if (savedInstanceState == null) {
            // pertama kali dibuka, tampilkan Home
            showTab(TAB_HOME)
        } else {
            // kalau layar diputar, fragment-nya sudah dikembalikan otomatis oleh sistem,
            // jadi kita cukup ingat tab mana yang aktif lalu warnai ulang
            currentTab = savedInstanceState.getInt(KEY_TAB, TAB_HOME)
            updateTabColor()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, currentTab)
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
    }

    private fun setupAction() {
        binding.navHome.setOnClickListener {
            if (currentTab != TAB_HOME) {
                showTab(TAB_HOME)
            }
        }
        binding.navMap.setOnClickListener {
            if (currentTab != TAB_MAP) {
                showTab(TAB_MAP)
            }
        }
        binding.navNotification.setOnClickListener {
            if (currentTab != TAB_NOTIFICATION) {
                showTab(TAB_NOTIFICATION)
            }
        }
        binding.navProfile.setOnClickListener {
            if (currentTab != TAB_PROFILE) {
                showTab(TAB_PROFILE)
            }
        }
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, NoteAddActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showTab(tab: Int) {
        currentTab = tab

        val fragment: Fragment
        when (tab) {
            TAB_MAP -> fragment = MapFragment()
            TAB_NOTIFICATION -> fragment = NotificationFragment()
            TAB_PROFILE -> fragment = ProfileFragment()
            else -> fragment = HomeFragment()
        }

        // ganti isi fragment_container dengan fragment tab yang dipilih
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        updateTabColor()
    }

    private fun updateTabColor() {
        val activeColor = ContextCompat.getColor(this, R.color.primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.nav_inactive)

        // semua tab dibuat abu-abu dulu
        binding.ivNavHome.setColorFilter(inactiveColor)
        binding.tvNavHome.setTextColor(inactiveColor)
        binding.ivNavMap.setColorFilter(inactiveColor)
        binding.tvNavMap.setTextColor(inactiveColor)
        binding.ivNavNotification.setColorFilter(inactiveColor)
        binding.tvNavNotification.setTextColor(inactiveColor)
        binding.ivNavProfile.setColorFilter(inactiveColor)
        binding.tvNavProfile.setTextColor(inactiveColor)

        // lalu tab yang sedang aktif diberi warna utama
        when (currentTab) {
            TAB_HOME -> {
                binding.ivNavHome.setColorFilter(activeColor)
                binding.tvNavHome.setTextColor(activeColor)
            }
            TAB_MAP -> {
                binding.ivNavMap.setColorFilter(activeColor)
                binding.tvNavMap.setTextColor(activeColor)
            }
            TAB_NOTIFICATION -> {
                binding.ivNavNotification.setColorFilter(activeColor)
                binding.tvNavNotification.setTextColor(activeColor)
            }
            TAB_PROFILE -> {
                binding.ivNavProfile.setColorFilter(activeColor)
                binding.tvNavProfile.setTextColor(activeColor)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // kalau sedang di tab lain, tombol back kembali ke Home dulu
        if (currentTab != TAB_HOME) {
            showTab(TAB_HOME)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val TAB_HOME = 0
        const val TAB_MAP = 1
        const val TAB_NOTIFICATION = 2
        const val TAB_PROFILE = 3
        private const val KEY_TAB = "current_tab"
    }
}
