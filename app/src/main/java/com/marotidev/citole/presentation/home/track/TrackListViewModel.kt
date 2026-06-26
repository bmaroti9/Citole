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

package com.marotidev.citole.presentation.home.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.DataStoreRepository
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.AudioService.AudioType
import com.marotidev.citole.data.state.SearchQueryStateHolder
import com.marotidev.citole.presentation.browse.SortChip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListViewModel @Inject constructor(
    audioRepository : AudioRepository,
    dataStoreRepository: DataStoreRepository,
    searchQueryStateHolder: SearchQueryStateHolder,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    var filteredTracks = combine(
        searchQueryStateHolder.query,
        audioRepository.allTracks,
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

    ) { query, allTracks, sortChip, sortReversed, types ->
        allTracks
            .filterByQuery(query)
            .filterByType(types[0], types[1], types[2], types[3])
            .sortByChip(sortChip)
            .reverseIf(sortReversed)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun List<AudioService.TrackData>.sortByChip(sortChip: SortChip): List<AudioService.TrackData> {
        return if (sortChip == SortChip.DateAdded) {
            this.sortedByDescending { it.dateAdded }
        } else {
            this.sortedBy { track ->
                when (sortChip) {
                    SortChip.Name -> track.name
                    SortChip.Album -> track.albumName
                    SortChip.Artist -> track.rawArtist
                }
            }
        }
    }

    fun List<AudioService.TrackData>.filterByType(
        showSongs: Boolean,
        showPodcasts: Boolean,
        showAudiobooks: Boolean,
        showOther: Boolean,
    ) : List<AudioService.TrackData> {
        return this.filter { track ->
            when (track.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioService.TrackData>.filterByQuery(query: String) : List<AudioService.TrackData> {
        return this.filter { track ->
            track.name.contains(query, ignoreCase = true)
                    || track.rawArtist.contains(query, ignoreCase = true)
                    || track.albumName.contains(query, ignoreCase = true)
        }
    }

    fun List<AudioService.TrackData>.reverseIf(reverse: Boolean) : List<AudioService.TrackData> {
        return if (reverse) {
            this.reversed()
        } else {
            this
        }
    }

    suspend fun generateQueueFromSeed(seedId: Long) : List<AudioService.TrackData> {
        return recommendationRepository.generateQueueFromSeed(seedId, 20)
    }

}