package com.example.secondplayer.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.secondplayer.model.AudioItem
import com.example.secondplayer.model.FolderItem
import java.io.File

class MediaRepository(private val context: Context) {

    fun fetchLocalAudioFiles(): List<AudioItem> {
        val audioList = mutableListOf<AudioItem>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(collection, projection, selection, null, null)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "مسار صوتي"
                val artist = cursor.getString(artistColumn) ?: "فنان غير معروف"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val path = cursor.getString(dataColumn) ?: ""
                
                val albumUri = ContentUris.withAppendedId(sArtworkUri, albumId)
                val folderName = try {
                    File(path).parentFile?.name ?: "المستندات العامة"
                } catch (e: Exception) {
                    "مجلد عام"
                }

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                audioList.add(AudioItem(id, title, artist, duration, contentUri, albumUri, folderName))
            }
        }
        return audioList
    }

    fun getFolders(tracks: List<AudioItem>): List<FolderItem> {
        return tracks.groupBy { it.folderName }.map { (folder, trackList) ->
            FolderItem(folder, trackList.size, trackList)
        }
    }
}
