package com.example.superpodcast.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.superpodcast.R
import com.example.superpodcast.model.PodcastSummaryViewData

class PodcastListAdapter(
    private var items: List<PodcastSummaryViewData>,
    private val listener: Listener
) : RecyclerView.Adapter<PodcastListAdapter.VH>() {

    interface Listener {
        fun onPodcastClicked(item: PodcastSummaryViewData)
    }

    fun submitList(newItems: List<PodcastSummaryViewData>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_podcast, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.itemView.setOnClickListener { listener.onPodcastClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.titleText)
    }
}
