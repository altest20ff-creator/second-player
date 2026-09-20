package com.example.secondplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.secondplayer.R
import com.example.secondplayer.model.AudioItem
import java.util.concurrent.TimeUnit

class AudioAdapter(
    private val audioList: List<AudioItem>,
    private val onItemClick: (AudioItem) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvSongTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvSongArtist)
        val tvDuration: TextView = view.findViewById(R.id.tvSongDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val item = audioList[position]
        holder.tvTitle.text = item.title
        holder.tvArtist.text = item.artist
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(item.duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(item.duration) % 60
        holder.tvDuration.text = String.format("%02d:%02d", minutes, seconds)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = audioList.size
}
