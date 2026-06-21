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

package com.marotidev.citole.presentation.home.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.presentation.browse.SortChip
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.AudioService.AudioType
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    dataStoreRepository: DataStoreRepository,
    searchQueryStateHolder: SearchQueryStateHolder
) : ViewModel() {

    var filteredArtists = combine(
        searchQueryStateHolder.query,
        audioRepository.allArtists,
        dataStoreRepository.chipSortChip,
        dataStoreRepository.chipSortReversed,
        combine(
            dataStoreRepository.chipShowSongs,
            dataStoreRepository.chipShowPodcasts,
            dataStoreRepository.chipShowAudiobooks,
            dataStoreRepository.chipShowOther,
        ) { songs, podcasts, audiobooks, other ->
            listOf(songs, podcasts, audiobooks, other)
        }

    ) { query, allArtists, sortChip, sortReversed, types ->
        allArtists
            .filterByQuery(query)
            .filterByType(types[0], types[1], types[2], types[3])
            .sortByChip(sortChip)
            .reverseIf(sortReversed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun List<AudioService.ArtistData>.sortByChip(sortChip: SortChip): List<AudioService.ArtistData> {
        return if (sortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { artist ->
                when (sortChip) {
                    SortChip.Name -> artist.name
                    SortChip.Album -> artist.albums.firstOrNull()?.albumName
                    SortChip.Artist -> artist.name
                }
            }
        }
    }

    fun List<AudioService.ArtistData>.filterByType(
        showSongs: Boolean,
        showPodcasts: Boolean,
        showAudiobooks: Boolean,
        showOther: Boolean,
    ) : List<AudioService.ArtistData> {
        return this.filter { artist ->
            when (artist.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.ArtistData>.filterByQuery(query: String) : List<AudioService.ArtistData> {
        return this.filter { artist ->
            artist.name.contains(query, ignoreCase = true)
                    || artist.tracks.any {it.name.contains(query, ignoreCase = true)}
                    || artist.allAlbums.any {it.albumName.contains(query, ignoreCase = true)}
        }
    }

    fun List<AudioService.ArtistData>.reverseIf(reverse: Boolean) : List<AudioService.ArtistData> {
        return if (reverse) {
            this.reversed()
        } else {
            this
        }
    }

}