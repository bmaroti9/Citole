package com.marotidev.citole

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AudioHelper {

    data class AudioData(
        val uri: Uri,
        val name: String,

        val title: String,
        val artist: String,

        val albumId : Long,
        val albumName : String,

        val duration: Long,
        val size: Int
    )

    fun fetchAudioFiles(context: Context): List<AudioData> {
        Log.i("FETCHING AUDIO", "fetchCalled")
        val audioList = mutableListOf<AudioData>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
        )

        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val cursorQuery = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )

        cursorQuery?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val albumName = cursor.getString(albumNameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getInt(sizeColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                audioList += AudioData(contentUri, name, title, artist, albumId, albumName, duration, size)
            }
        }

        return audioList
    }
}