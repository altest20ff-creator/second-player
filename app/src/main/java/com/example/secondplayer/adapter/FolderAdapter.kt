package com.example.secondplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.secondplayer.R
import com.example.secondplayer.model.FolderItem

class FolderAdapter(
    private val folderList: List<FolderItem>,
    private val onFolderClick: (FolderItem) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFolderName: TextView = view.findViewById(R.id.tvSongTitle)
        val tvTrackCount: TextView = view.findViewById(R.id.tvSongArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val item = folderList[position]
        holder.tvFolderName.text = "📁 " + item.folderName
        holder.tvTrackCount.text = "${item.trackCount} مسار صوتي"

        holder.itemView.setOnClickListener {
            onFolderClick(item)
        }
    }

    override fun getItemCount(): Int = folderList.size
}
