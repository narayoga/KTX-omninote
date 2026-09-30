package com.example.omninoteconnect.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.data.Dashboard.Insights
import com.example.omninoteconnect.databinding.ActivityDashboardBinding
import com.example.omninoteconnect.ui.BottomTab.ProfileActivity
import com.example.omninoteconnect.ui.Note.NoteActivity

class DashboardActivity : AppCompatActivity() {

    private val list = ArrayList<Insights>()
    private lateinit var rvInsights: RecyclerView
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rvInsights = findViewById(R.id.rv_insights)
        rvInsights.setHasFixedSize(true)
        list.addAll(getListInsights())

        setupView()
        setUpRecycler()
        setupAction()
    }

    private fun setUpRecycler() {
        rvInsights.layoutManager =LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        val listInsightAdapter = InsightAdapter(list)
        rvInsights.adapter = listInsightAdapter
    }

    private fun getListInsights(): ArrayList<Insights> {
        val dataTitle = resources.getStringArray(R.array.dummy_insight_title)
        val dataSubTitle = resources.getStringArray(R.array.dummy_insight_subtitle)
        val dataPhoto = resources.obtainTypedArray(R.array.dummy_insight_photo)
        val listInsight = ArrayList<Insights>()
        for (i in dataTitle.indices) {
            val insight = Insights(dataTitle[i], dataPhoto.getResourceId(i, -1), dataSubTitle[i])
            listInsight.add(insight)
        }

        return listInsight
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
        binding.profileBottomTab.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.mainFavoriteBtn.setOnClickListener{
            val intent = Intent(this, NoteActivity::class.java)
            startActivity(intent)
        }
    }
}