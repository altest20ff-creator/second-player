package com.example.secondplayer.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.secondplayer.model.AudioItem
import java.io.File

class MediaRepository(private val context: Context) {

    fun fetchAllAudio(): List<AudioItem> {
        val list = mutableListOf<AudioItem>()
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

            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "مسار بدون عنوان"
                val artist = cursor.getString(artistCol) ?: "فنان غير معروف"
                val album = cursor.getString(albumCol) ?: "ألبوم غير معروف"
                val duration = cursor.getLong(durationCol)
                val albumId = cursor.getLong(albumIdCol)
                val path = cursor.getString(dataCol) ?: ""

                val albumUri = ContentUris.withAppendedId(sArtworkUri, albumId)
                val folderName = try {
                    File(path).parentFile?.name ?: "مجلد عام"
                } catch (e: Exception) {
                    "مجلد عام"
                }

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                list.add(AudioItem(id, title, artist, album, duration, contentUri, albumUri, folderName))
            }
        }
        return list
    }
}
