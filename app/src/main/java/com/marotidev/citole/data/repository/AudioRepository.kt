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


package com.marotidev.citole.data.repository

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.AudioService.AudioType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class AudioRepository @Inject constructor(
    private val audioService: AudioService,
    private val application: Application,
) {
    var allTracks: MutableStateFlow<List<AudioService.TrackData>> = MutableStateFlow(emptyList())
    var allAlbums: MutableStateFlow<List<AudioService.AlbumData>> = MutableStateFlow(emptyList())
    var allArtists: MutableStateFlow<List<AudioService.ArtistData>> = MutableStateFlow(emptyList())

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun List<AudioService.TrackData>.determineArtists(): Pair<List<String>, List<String>> {
        val artistFrequency : MutableMap<String, Int> = mutableMapOf()
        this.forEach { track ->
            track.artists.forEach { artist ->
                artistFrequency[artist] = artistFrequency[artist]?.plus(1) ?: 0
            }
        }
        val artists = mutableListOf<String>()
        artistFrequency.forEach { (name, times) ->
            if (times >= this.size / 2) {
                artists.add(name)
            }
        }
        if (artists.isEmpty()) {
            return Pair(listOf("Various Artists"), artistFrequency.keys.toList())
        }
        return Pair(artists, artistFrequency.keys.toList())
    }

    fun List<AudioService.TrackData>.groupToAlbum() : List<AudioService.AlbumData> {
        return this.groupBy { it.albumId }
            .map { (albumId, tracks) ->
                val sequentialTracks = tracks.sortedBy { it.trackNumber }
                val ownerAndAllArtists = tracks.determineArtists()
                AudioService.AlbumData(
                    albumId = albumId,
                    albumName = tracks.firstOrNull()?.albumName ?: "Unknown Album",
                    ownerArtists = ownerAndAllArtists.first,
                    allArtists = ownerAndAllArtists.second,
                    tracks = sequentialTracks,
                    type = tracks.firstOrNull()?.type ?: AudioType.Other,
                    artworkUri = tracks.firstOrNull()?.artworkUri,
                    dateAdded = tracks.minByOrNull { it.dateAdded }?.dateAdded ?: 0
                )
            }
    }

    fun List<AudioService.TrackData>.groupToArtist(albums: List<AudioService.AlbumData>): List<AudioService.ArtistData> {
        val artistTracks = mutableMapOf<String, MutableList<AudioService.TrackData>>()

        this.forEach { track ->
            track.artists.forEach { artist ->
                artistTracks.getOrPut(artist) { mutableListOf() }.add(track)
            }
        }

        return artistTracks.map { (artistName, tracks) ->
            val allAlbums =  albums.filter { artistName in it.allArtists }
            val singles = allAlbums.filter { it.tracks.size == 1 && it.tracks.firstOrNull()?.title?.contains(it.albumName) ?: false}
            val ownAlbums = albums.filter { artistName in it.ownerArtists }
            AudioService.ArtistData(
                name = artistName,
                tracks = tracks,
                singles = singles,
                albums = ownAlbums.subtract(singles.toSet()).toList(),
                allAlbums = allAlbums,
                appearsIn = allAlbums.subtract(ownAlbums.toSet()).toList(),
                dateAdded = tracks.maxByOrNull { it.dateAdded }?.dateAdded ?: 0,
                type = tracks.firstOrNull()?.type ?: AudioType.Other
            )
        }
    }

    fun findTrackById(id: Long?) : AudioService.TrackData? {
        return allTracks.value.find { it.id == id }
    }

    fun checkHasAudioPermission() : Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(application, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun fetchOrUpdateTracks() {
        serviceScope.launch {
            val tracks = audioService.fetchAudioFiles(application)
            allTracks.value = tracks
            allAlbums.value = tracks.groupToAlbum()
            allArtists.value = tracks.groupToArtist(allAlbums.value)
        }
    }
}