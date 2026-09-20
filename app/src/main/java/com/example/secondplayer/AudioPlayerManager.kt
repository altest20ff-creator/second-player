package com.example.secondplayer

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.secondplayer.model.AudioTrack

object AudioPlayerManager {

    private var exoPlayer: ExoPlayer? = null
    var playlist: List<AudioTrack> = emptyList()
    var currentIndex: Int = -1

    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
        }
        return exoPlayer!!
    }

    fun playTrackAt(context: Context, list: List<AudioTrack>, index: Int) {
        if (index !in list.indices) return

        playlist = list
        currentIndex = index
        val track = playlist[currentIndex]

        val player = getPlayer(context)
        player.stop()
        player.setMediaItem(MediaItem.fromUri(Uri.parse(track.mediaUriStr)))
        player.prepare()
        player.play()
    }

    fun getCurrentTrack(): AudioTrack? {
        return if (currentIndex in playlist.indices) playlist[currentIndex] else null
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun togglePlayPause(context: Context) {
        val player = getPlayer(context)
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun playNext(context: Context) {
        if (playlist.isNotEmpty() && currentIndex < playlist.size - 1) {
            playTrackAt(context, playlist, currentIndex + 1)
        }
    }

    fun playPrevious(context: Context) {
        if (playlist.isNotEmpty() && currentIndex > 0) {
            playTrackAt(context, playlist, currentIndex - 1)
        }
    }
}
