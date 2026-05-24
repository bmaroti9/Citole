package com.marotidev.citole

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import java.time.LocalTime

enum class AudioType {
    Song,
    Podcast,
    Audiobook,
    Other
}

fun getAudioType(isSong : Int, isPodcast: Int, isAudiobook: Int) : AudioType {
    return when {
        isSong == 1-> AudioType.Song
        isPodcast == 1 -> AudioType.Podcast
        isAudiobook == 1 -> AudioType.Audiobook
        else -> AudioType.Other
    }
}

object AudioHelper {

    data class AudioData(
        val uri: Uri,
        val artworkUri : Uri,

        val name: String,

        val title: String,
        val artist: String,

        val albumId : Long,
        val albumName : String,

        val duration: Long,
        val size: Int,
        val dateAdded: Int,
        val trackNumber: Int,

        val type: AudioType
    )

    data class AlbumData(
        val albumId: Long,

        val artworkUri : Uri?,

        val albumName: String,
        val artist: String,

        val tracks : List<AudioData>,

        val type: AudioType
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

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.TRACK,

            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_PODCAST,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.IS_AUDIOBOOK)
            }
        }.toTypedArray()

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
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackNumberColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            val isMusicRow = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val isPodcastRow = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)
            val isAudiobookRow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_AUDIOBOOK)} else {0}

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val albumName = cursor.getString(albumNameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val dateAdded = cursor.getInt(dateAddedColumn)
                val trackNumber = cursor.getInt(trackNumberColumn) % 1000

                val isMusic = cursor.getInt(isMusicRow)
                val isPodcast = cursor.getInt(isPodcastRow)
                val isAudiobook = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {cursor.getInt(isAudiobookRow)} else {0}

                val audioType = getAudioType(isMusic, isPodcast, isAudiobook)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val artworkUri : Uri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                    albumId
                )

                audioList += AudioData(contentUri, artworkUri, name, title, artist, albumId, albumName,
                    duration, size, dateAdded, trackNumber, audioType)
            }
        }

        return audioList
    }
}