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

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.SortChip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.component1
import kotlin.collections.component2
import com.marotidev.citole.services.AudioService.AudioType
import com.marotidev.citole.services.DataStoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStoreService = DataStoreService(application)

    var showSongs by mutableStateOf(true)
    var showPodcasts by mutableStateOf(false)
    var showAudiobooks by mutableStateOf(false)
    var showOther by mutableStateOf(false)

    var selectedSortChip by mutableStateOf(SortChip.Name)

    var reverseSortOrder by mutableStateOf(false)

    fun setChipShowSongs(to: Boolean) {
        viewModelScope.launch { dataStoreService.saveChipShowSongs(to) }
    }

    fun setChipShowPodcasts(to: Boolean) {
        viewModelScope.launch { dataStoreService.saveChipShowPodcasts(to) }
    }

    fun setChipShowAudiobooks(to: Boolean) {
        viewModelScope.launch { dataStoreService.saveChipShowAudiobooks(to) }
    }

    fun setChipShowOther(to: Boolean) {
        viewModelScope.launch { dataStoreService.saveChipShowOther(to) }
    }

    fun setSortChipSort(to: SortChip) {
        viewModelScope.launch { dataStoreService.saveChipSortChip(to) }
    }

    fun onReverseSortOrderChanged(to: Boolean) {
        viewModelScope.launch { dataStoreService.saveChipSortReversed(to) }
    }

    var allTracks by mutableStateOf<List<AudioService.AudioData>>(emptyList())
        private set

    var filteredTracks by mutableStateOf<List<AudioService.AudioData>>(emptyList())
        private set

    var allAlbums by mutableStateOf<List<AudioService.AlbumData>>(emptyList())
        private set

    var filteredAlbums by mutableStateOf<List<AudioService.AlbumData>>(emptyList())
        private set

    var allArtists by mutableStateOf<List<AudioService.ArtistData>>(emptyList())
        private set

    var filteredArtists by mutableStateOf<List<AudioService.ArtistData>>(emptyList())
        private set


    var searchQuery by mutableStateOf("")
        private set


    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery

        updateFilteredTracks()
    }

    private fun observeFilterChanges() {
        viewModelScope.launch {
            combine(
                listOf(
                    dataStoreService.chipShowSongs as Flow<Any>,
                    dataStoreService.chipShowPodcasts as Flow<Any>,
                    dataStoreService.chipShowAudiobooks as Flow<Any>,
                    dataStoreService.chipShowOther as Flow<Any>,
                    dataStoreService.chipSortChip as Flow<Any>,
                    dataStoreService.chipSortReversed as Flow<Any>
                )
            ) { values ->
                showSongs = values[0] as Boolean
                showPodcasts = values[1] as Boolean
                showAudiobooks = values[2] as Boolean
                showOther = values[3] as Boolean
                selectedSortChip = values[4] as SortChip
                reverseSortOrder = values[5] as Boolean

                updateFilteredTracks()
            }.collect()
        }
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

    fun List<AudioService.AudioData>.sortTracksBySortChip(): List<AudioService.AudioData> {
        return if (selectedSortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { track ->
                when (selectedSortChip) {
                    SortChip.Name -> track.name
                    SortChip.Album -> track.albumName
                    SortChip.Artist -> track.rawArtist
                    else -> ""
                }
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
            val allAlbums =  albums.filter { artistName in it.allArtists }
            val singles = allAlbums.filter { it.tracks.size == 1 && it.albumName == it.tracks.firstOrNull()?.title }
            val ownAlbums = albums.filter { artistName in it.ownerArtists }
            AudioService.ArtistData(
                name = artistName,
                tracks = tracks,
                singles = singles,
                albums = ownAlbums.subtract(singles.toSet()).toList(),
                allAlbums = allAlbums,
                appearsIn = allAlbums.subtract(ownAlbums.toSet()).toList(),
            )
        }
    }

    fun findAlbumById(albumId: Long) : AudioService.AlbumData? {
        return allAlbums.find { it.albumId == albumId }
    }

    fun findArtistByName(artistName: String) : AudioService.ArtistData? {
        return allArtists.find { it.name == artistName }
    }

    fun loadTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = AudioService.fetchAudioFiles(context)
            allTracks = tracks
            allAlbums = tracks.groupToAlbum()
            allArtists = tracks.groupToArtist(allAlbums)
            observeFilterChanges()
        }
    }
}