package com.example.secondplayer.model

import android.net.Uri
import java.io.Serializable

data class AudioItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val albumUri: Uri?,
    val folderName: String,
    var isFavorite: Boolean = false
) : Serializable

data class CategoryItem(
    val title: String,
    val countText: String,
    val iconResName: String,
    val type: CategoryType
)

enum class CategoryType {
    TRACKS, ALBUMS, ARTISTS, FOLDERS, FAVORITES, RECENT
}
