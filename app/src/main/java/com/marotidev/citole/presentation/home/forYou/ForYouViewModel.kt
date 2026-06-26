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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ForYouViewModel @Inject constructor(
    audioRepository : AudioRepository,
    trackLogRepository: TrackLogRepository
) : ViewModel() {

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
            .filter { log -> log.playbackDurationMs > 0 }
            .mapNotNull { log -> tracks.find { it.id == log.trackId } }
            .take(10)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastPodcast = combine(
        trackLogRepository.lastPodcast,
        audioRepository.allTracks
    ) { lastPodcast, tracks ->
        val track = tracks.findLast { it.id == lastPodcast?.trackId }
        println(lastPodcast?.trackId)
        if (track == null || lastPodcast == null) {
            null
        } else {
            TrackLogRepository.TrackWithPlaybackState(
                track,
                lastPodcast.playbackDurationMs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val lastAudiobook = combine(
        trackLogRepository.lastAudiobook,
        trackLogRepository.lastAudiobookQueue,
        audioRepository.allTracks
    ) { lastAudioBook, lastAudiobookQueue, allTracks ->
        lastAudioBook?.let {
            val tracks = lastAudiobookQueue.mapNotNull { log -> allTracks.find { track -> track.id == log.trackId } }
            TrackLogRepository.QueueWithPlaybackState(
                tracks = tracks,
                queueIndex = it.queueIndex.coerceIn(0, tracks.size - 1),
                playbackDurationMs = it.playbackDurationMs,
                queueId = it.queueId
            )
        }
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