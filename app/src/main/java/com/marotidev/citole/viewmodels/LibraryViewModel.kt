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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.SortChip
import com.marotidev.citole.data.repository.AudioRepository
import kotlinx.coroutines.launch
import com.marotidev.citole.data.service.AudioService.AudioType
import com.marotidev.citole.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    audioRepository : AudioRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val allTracks: StateFlow<List<AudioService.TrackData>> = audioRepository.allTracks
    val allAlbums: StateFlow<List<AudioService.AlbumData>> = audioRepository.allAlbums
    val allArtists: StateFlow<List<AudioService.ArtistData>> = audioRepository.allArtists

    var showSongs by mutableStateOf(true)
    var showPodcasts by mutableStateOf(false)
    var showAudiobooks by mutableStateOf(false)
    var showOther by mutableStateOf(false)

    var selectedSortChip by mutableStateOf(SortChip.Name)

    var reverseSortOrder by mutableStateOf(false)

    fun setChipShowSongs(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowSongs(to) }
    }

    fun setChipShowPodcasts(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowPodcasts(to) }
    }

    fun setChipShowAudiobooks(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowAudiobooks(to) }
    }

    fun setChipShowOther(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipShowOther(to) }
    }

    fun setSortChipSort(to: SortChip) {
        viewModelScope.launch { dataStoreRepository.saveChipSortChip(to) }
    }

    fun onReverseSortOrderChanged(to: Boolean) {
        viewModelScope.launch { dataStoreRepository.saveChipSortReversed(to) }
    }

    var filteredTracks by mutableStateOf<List<AudioService.TrackData>>(emptyList())
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

    private fun observeFilterChanges() {
        viewModelScope.launch {
            combine(
                listOf(
                    dataStoreRepository.chipShowSongs as Flow<Any>,
                    dataStoreRepository.chipShowPodcasts as Flow<Any>,
                    dataStoreRepository.chipShowAudiobooks as Flow<Any>,
                    dataStoreRepository.chipShowOther as Flow<Any>,
                    dataStoreRepository.chipSortChip as Flow<Any>,
                    dataStoreRepository.chipSortReversed as Flow<Any>
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

    fun List<AudioService.TrackData>.filterTracksByQuery() : List<AudioService.TrackData> {
        return this.filter { track ->
            track.name.contains(searchQuery, ignoreCase = true)
                    || track.rawArtist.contains(searchQuery, ignoreCase = true)
                    || track.albumName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun List<AudioService.TrackData>.sortTracksBySortChip(): List<AudioService.TrackData> {
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

    fun List<AudioService.TrackData>.filterTracksByFilterChips() : List<AudioService.TrackData> {
        return this.filter { track ->
            when (track.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.TrackData>.sortBySortOrder() : List<AudioService.TrackData> {
        return when (reverseSortOrder) {
            true -> this.reversed()
            false -> this
        }
    }
}