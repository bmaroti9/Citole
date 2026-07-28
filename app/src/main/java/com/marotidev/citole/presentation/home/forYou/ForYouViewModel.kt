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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marotidev.citole.data.repository.AudioRepository
import com.marotidev.citole.data.repository.TrackLogRepository
import com.marotidev.citole.data.state.SearchQueryStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ForYouViewModel @Inject constructor(
    audioRepository : AudioRepository,
    trackLogRepository: TrackLogRepository,
    searchQueryStateHolder: SearchQueryStateHolder
) : ViewModel() {

    val showUniversalSearch = searchQueryStateHolder.query.map {
        it != ""
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val recentlyAdded = audioRepository.allTracks.map { tracks ->
        tracks.sortedBy { it.dateAdded }.reversed().take(16)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyPlayed = combine(
        trackLogRepository.allLogs,
        audioRepository.allTracks
    ) { logs, tracks ->
            logs
            .mapNotNull { log -> tracks.find { it.id == log.trackId } }
            .take(10)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mostPlayed = combine(
        trackLogRepository.mostPlayedRecentTracks,
        audioRepository.allTracks
    ) { mostPlayed, tracks ->
        mostPlayed.mapNotNull { log -> tracks.find { it.id == log.trackId } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastAudiobook : StateFlow<TrackLogRepository.QueueWithPlaybackState?> = combine(
        trackLogRepository.lastAudiobook,
        trackLogRepository.lastAudiobookQueue,
        audioRepository.allTracks
    ) { lastAudiobook, lastAudiobookQueue, allTracks ->
        if (lastAudiobook != null && lastAudiobookQueue != null) {
            val tracks = lastAudiobookQueue.mapNotNull { log -> allTracks.find { track -> track.id == log.trackId } }
            TrackLogRepository.QueueWithPlaybackState(
                tracks = tracks,
                queueIndex = lastAudiobook.queueIndex.coerceIn(0, tracks.size - 1),
                playbackDurationMs = lastAudiobook.playbackDurationMs,
                queueId = lastAudiobook.queueId
            )
        } else {null}
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    var resumePlaybackAnimationState by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            delay(700.milliseconds)
            resumePlaybackAnimationState = 1
            delay(1000.milliseconds)
            resumePlaybackAnimationState = 2
        }
    }

}