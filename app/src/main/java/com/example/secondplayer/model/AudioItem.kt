package com.example.secondplayer.model

import android.net.Uri

data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    val folderName: String,
    var isFavorite: Boolean = false
)

data class FolderItem(
    val folderName: String,
    val trackCount: Int,
    val tracks: List<AudioItem>
)
