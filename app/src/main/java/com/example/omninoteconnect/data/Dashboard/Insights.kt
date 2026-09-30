package com.example.omninoteconnect.data.Dashboard

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Insights(
    val title: String,
    val src: Int,
    val subtitle: String,
) : Parcelable
