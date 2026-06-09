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

package com.marotidev.citole.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.SortChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import com.marotidev.citole.services.AudioService.AudioType

class LibraryViewModel : ViewModel() {

    var showSongs by mutableStateOf(true)
    var showPodcasts by mutableStateOf(false)
    var showAudiobooks by mutableStateOf(false)
    var showOther by mutableStateOf(false)

    var selectedSortChip by mutableStateOf(SortChip.Name)

    var reverseSortOrder by mutableStateOf(false)

    fun onShowSongsChanged() {
        showSongs = !showSongs
        updateFilteredTracks()
    }

    fun onShowPodcastsChanged() {
        showPodcasts = !showPodcasts
        updateFilteredTracks()
    }

    fun onShowAudiobooksChanged() {
        showAudiobooks = !showAudiobooks
        updateFilteredTracks()
    }

    fun onShowOtherChanged() {
        showOther = !showOther
        updateFilteredTracks()
    }

    fun onSelectedSortChipChanged(to: SortChip) {
        selectedSortChip = to
        updateFilteredTracks()
    }

    fun onReverseSortOrderChanged(to: Boolean) {
        reverseSortOrder = to
        updateFilteredTracks()
    }

    var allTracks by mutableStateOf<List<AudioService.AudioData>>(emptyList())
        private set

    var filteredTracks by mutableStateOf<List<AudioService.AudioData>>(emptyList())
        private set

    var allAlbums by mutableStateOf<List<AudioService.AlbumData>>(emptyList())
        private set

    var filteredAlbums by mutableStateOf<List<AudioService.AlbumData>>(emptyList())
        private set

    var filteredArtists by mutableStateOf<List<AudioService.ArtistData>>(emptyList())
        private set


    var searchQuery by mutableStateOf("")
        private set


    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery

        updateFilteredTracks()
    }

    fun updateFilteredTracks() {
        filteredTracks = allTracks.filterTracksByQuery().filterTracksByFilterChips().sortTracksBySortChip().sortBySortOrder()

        filteredAlbums = filteredTracks.groupToAlbum()

        filteredArtists = filteredTracks.groupToArtist(filteredAlbums)
    }

    fun List<AudioService.AudioData>.filterTracksByQuery() : List<AudioService.AudioData> {
        return this.filter { track ->
            track.name.contains(searchQuery, ignoreCase = true)
                    || track.rawArtist.contains(searchQuery, ignoreCase = true)
                    || track.albumName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun List<AudioService.AudioData>.sortTracksBySortChip() : List<AudioService.AudioData> {
        return this.sortedBy { track ->
            when (selectedSortChip) {
                SortChip.Name -> track.name
                SortChip.Album -> track.albumName
                SortChip.Artist -> track.rawArtist
                SortChip.DateAdded -> track.dateAdded.toString()
            }
        }
    }

    fun List<AudioService.AudioData>.filterTracksByFilterChips() : List<AudioService.AudioData> {
        return this.filter { track ->
            when (track.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.AudioData>.sortBySortOrder() : List<AudioService.AudioData> {
        return when (reverseSortOrder) {
            true -> this.reversed()
            false -> this
        }
    }

    fun List<AudioService.AudioData>.determineArtists(): Pair<List<String>, List<String>> {
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

    fun List<AudioService.AudioData>.groupToAlbum() : List<AudioService.AlbumData> {
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
                    artworkUri = tracks.firstOrNull()?.artworkUri
                )
            }
    }

    fun List<AudioService.AudioData>.groupToArtist(albums: List<AudioService.AlbumData>): List<AudioService.ArtistData> {
        val artistTracks = mutableMapOf<String, MutableList<AudioService.AudioData>>()

        this.forEach { track ->
            track.artists.forEach { artist ->
                artistTracks.getOrPut(artist) { mutableListOf() }.add(track)
            }
        }

        return artistTracks.map { (artistName, tracks) ->
            AudioService.ArtistData(
                name = artistName,
                tracks = tracks,
                albums = albums.filter { artistName in it.ownerArtists },
                appearsIn = albums.filter { artistName in it.allArtists }
            )
        }
    }

    fun findAlbumById(albumId: Long) : AudioService.AlbumData? {
        return allAlbums.find { it.albumId == albumId }
    }

    fun loadTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = AudioService.fetchAudioFiles(context)
            allTracks = tracks
            allAlbums = tracks.groupToAlbum()
            updateFilteredTracks()
        }
    }
}