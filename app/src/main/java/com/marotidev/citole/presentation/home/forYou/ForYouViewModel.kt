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

package com.marotidev.citole.presentation.home.forYou

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ForYouViewModel @Inject constructor(
    audioRepository : AudioRepository,
    recommendationRepository: RecommendationRepository
) : ViewModel() {

    val recentlyAdded = audioRepository.allTracks.map { tracks ->
        tracks.sortedBy { it.dateAdded }.take(14)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyPlayed = combine(
        recommendationRepository.allLogs,
        audioRepository.allTracks
    ) { logs, tracks ->
        logs.map { log -> tracks.find { it.id == log.trackId } }.take(10)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastPodcast = combine(
        recommendationRepository.lastPodcast,
        audioRepository.allTracks
    ) { lastPodcast, tracks ->
        val track = tracks.findLast { it.id == lastPodcast?.trackId }
        println(lastPodcast?.trackId)
        if (track == null || lastPodcast == null) {
            null
        } else {
            RecommendationRepository.TrackWithPlaybackState(
                track,
                lastPodcast
            )
        }


    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        recommendationRepository.fetchLogs()
    }

}