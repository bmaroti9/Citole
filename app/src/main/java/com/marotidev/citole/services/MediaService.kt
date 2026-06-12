/*
Copyright (C) <2026>  <Balint Maroti>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

*/

package com.marotidev.citole.services

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata


object AudioService {

    private val artistSplitterRegex = Regex(
        pattern = """\s*([,;\\/&]|feat\.?|ft\.?|\|)\s*""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    enum class AudioType {
        Song,
        Podcast,
        Audiobook,
        Other
    }

    data class AudioData(
        val id: Long,

        val uri: Uri,
        val artworkUri : Uri,

        val name: String,

        val title: String,
        val rawArtist: String,
        val artists: List<String>,

        val albumId : Long,
        val albumName : String,

        val duration: Long,

        val dateAdded: Int,
        val trackNumber: Int,

        val type: AudioType
    )

    data class AlbumData(
        val albumId: Long,

        val artworkUri : Uri?,

        val albumName: String,
        val ownerArtists: List<String>,
        val allArtists: List<String>,

        val tracks : List<AudioData>,

        val type: AudioType
    )

    data class ArtistData(
        val name : String,

        val tracks: List<AudioData>,
        val albums: List<AlbumData>,
        val appearsIn: List<AlbumData>,
        val singles: List<AlbumData>,
        val allAlbums: List<AlbumData>,
    )

    fun AudioType.toInt() = ordinal
    fun Int.toAudioType() = AudioType.entries[this]

    fun getAudioType(isSong : Int, isPodcast: Int, isAudiobook: Int) : AudioType {
        return when {
            isSong == 1-> AudioType.Song
            isPodcast == 1 -> AudioType.Podcast
            isAudiobook == 1 -> AudioType.Audiobook
            else -> AudioType.Other
        }
    }

    fun AudioData.toMediaItem() : MediaItem {
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(title)
                    .setArtist(rawArtist)
                    .setArtworkUri(artworkUri)
                    .setTitle(name)
                    .setAlbumTitle(albumName)
                    .setDurationMs(duration)
                    .setTrackNumber(trackNumber)
                    .setExtras(Bundle().apply {
                        putLong("albumId", albumId)
                        putInt("dateAdded", dateAdded)
                        putInt("audioType", type.toInt())
                    })
                    .build()
            )
            .build()
    }

    fun MediaItem.toAudioData() : AudioData {
        return AudioData(
            id = mediaId.toLong(),
            uri = localConfiguration?.uri ?: Uri.EMPTY,
            artworkUri = mediaMetadata.artworkUri ?: Uri.EMPTY,
            name = mediaMetadata.title.toString(),
            title = mediaMetadata.displayTitle.toString(),
            rawArtist = mediaMetadata.artist.toString(),
            artists = splitArtists(mediaMetadata.artist.toString()),
            albumId = mediaMetadata.extras?.getLong("albumId") ?: 0,
            albumName = mediaMetadata.albumTitle.toString(),
            duration = mediaMetadata.durationMs ?: 0,
            dateAdded = mediaMetadata.extras?.getInt("dateAdded") ?: 0,
            trackNumber = mediaMetadata.trackNumber ?: 0,
            type = mediaMetadata.extras?.getInt("audioType")?.toAudioType() ?: AudioType.Other,
        )
    }

    fun splitArtists(rawArtist: String?): List<String> {
        if (rawArtist.isNullOrBlank()) return listOf("Unknown Artist")

        return rawArtist
            .split(artistSplitterRegex)
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("and", ignoreCase = true) }
            .distinct()
    }

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
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val trackNumberColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            val isMusicRow = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val isPodcastRow = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)
            val isAudiobookRow = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_AUDIOBOOK)} else {0}

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val title = cursor.getString(titleColumn)
                val artistsPlainString = cursor.getString(artistColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val albumName = cursor.getString(albumNameColumn)
                val duration = cursor.getLong(durationColumn)
                val dateAdded = cursor.getInt(dateAddedColumn)
                val trackNumber = cursor.getInt(trackNumberColumn) % 1000

                val isMusic = cursor.getInt(isMusicRow)
                val isPodcast = cursor.getInt(isPodcastRow)
                val isAudiobook = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {cursor.getInt(isAudiobookRow)} else {0}

                val audioType = getAudioType(isMusic, isPodcast, isAudiobook)
                val artists = splitArtists(artistsPlainString)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val artworkUri : Uri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                    albumId
                )

                audioList += AudioData(
                    id = id,
                    uri = contentUri,
                    artworkUri = artworkUri,
                    title = title,
                    name = name,
                    rawArtist = artistsPlainString,
                    albumId = albumId,
                    albumName = albumName,
                    duration = duration,
                    dateAdded = dateAdded,
                    trackNumber = trackNumber,
                    type = audioType,
                    artists = artists,
                )
            }
        }

        return audioList
    }
}