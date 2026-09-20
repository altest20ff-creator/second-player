package com.example.secondplayer.model

import java.io.Serializable

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val mediaUriStr: String,
    val albumArtUriStr: String,
    val folderName: String,
    var isFavorite: Boolean = false
) : Serializable

data class FolderModel(
    val folderName: String,
    val trackCount: Int,
    val tracks: List<AudioTrack>
)
