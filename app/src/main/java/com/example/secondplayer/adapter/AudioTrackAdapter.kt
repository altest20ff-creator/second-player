package com.example.secondplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.secondplayer.R
import com.example.secondplayer.model.AudioTrack
import java.util.concurrent.TimeUnit

class AudioTrackAdapter(
    private val tracks: List<AudioTrack>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<AudioTrackAdapter.TrackViewHolder>() {

    class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvArtist)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = tracks[position]
        holder.tvTitle.text = item.title
        holder.tvArtist.text = item.artist

        val min = TimeUnit.MILLISECONDS.toMinutes(item.duration)
        val sec = TimeUnit.MILLISECONDS.toSeconds(item.duration) % 60
        holder.tvDuration.text = String.format("%02d:%02d", min, sec)

        Glide.with(holder.itemView.context)
            .load(item.albumArtUriStr)
            .placeholder(android.R.drawable.ic_media_play)
            .error(android.R.drawable.ic_media_play)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.ivThumb)

        holder.itemView.setOnClickListener {
            onClick(position)
        }
    }

    override fun getItemCount(): Int = tracks.size
}
