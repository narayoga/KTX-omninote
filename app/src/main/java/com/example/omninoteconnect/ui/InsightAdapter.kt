package com.example.omninoteconnect.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.omninoteconnect.R
import com.example.omninoteconnect.data.Dashboard.Insights

class InsightAdapter(private val listInsight:ArrayList<Insights>) :
    RecyclerView.Adapter<InsightAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Insights)
    }

    class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tv_title_insight_item)
        val subtitle: TextView = view.findViewById(R.id.tv_subtitle_insight_item)
        val image: ImageView = view.findViewById(R.id.iv_insight_item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.row_insight, parent ,false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val (title, src, subtitle) = listInsight[position]
        holder.image.setImageResource(src)
        holder.title.text = title
        holder.subtitle.text = subtitle
        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(listInsight[holder.adapterPosition]) }
    }

    override fun getItemCount(): Int {
        return listInsight.size
    }

}