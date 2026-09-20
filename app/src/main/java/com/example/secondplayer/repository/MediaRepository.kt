package com.example.secondplayer.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.secondplayer.model.AudioTrack
import java.io.File

class MediaRepository(private val context: Context) {

    fun loadAllTracks(): List<AudioTrack> {
        val tracksList = mutableListOf<AudioTrack>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            val baseArtworkUri = Uri.parse("content://media/external/audio/albumart")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "مسار صوتي"
                val artist = cursor.getString(artistCol) ?: "فنان غير معروف"
                val album = cursor.getString(albumCol) ?: "ألبوم عام"
                val duration = cursor.getLong(durationCol)
                val albumId = cursor.getLong(albumIdCol)
                val path = cursor.getString(dataCol) ?: ""

                val albumArtUri = ContentUris.withAppendedId(baseArtworkUri, albumId).toString()
                val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                val folderName = try {
                    File(path).parentFile?.name ?: "مجلد آخر"
                } catch (e: Exception) {
                    "مجلد عام"
                }

                tracksList.add(
                    AudioTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        mediaUriStr = mediaUri,
                        albumArtUriStr = albumArtUri,
                        folderName = folderName
                    )
                )
            }
        }
        return tracksList
    }
}
