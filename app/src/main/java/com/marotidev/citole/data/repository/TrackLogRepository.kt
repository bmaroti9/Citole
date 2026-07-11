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

import androidx.room.Transaction
import com.marotidev.citole.data.local.TrackPlayLog
import com.marotidev.citole.data.local.TrackPlayLogDao
import com.marotidev.citole.data.service.AudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class TrackLogRepository @Inject constructor(
    private val trackPlayLogDao: TrackPlayLogDao,
) {

    data class TrackWithPlaybackState(
        val track: AudioService.TrackData,
        val playbackDurationMs: Long
    )

    data class QueueWithPlaybackState(
        val tracks: List<AudioService.TrackData>,
        val queueIndex: Int,
        val playbackDurationMs: Long,
        val queueId: Long
    )

    var allLogs: MutableStateFlow<List<TrackPlayLog>> = MutableStateFlow(emptyList())

    var mostPlayedRecentTracks : MutableStateFlow<List<TrackPlayLog>> = MutableStateFlow(emptyList())

    var lastPodcast: MutableStateFlow<TrackPlayLog?> = MutableStateFlow(null)

    var lastAudiobook: MutableStateFlow<TrackPlayLog?> = MutableStateFlow(null)
    var lastAudiobookQueue: MutableStateFlow<List<TrackPlayLog>> = MutableStateFlow(emptyList())

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun fetchLastAudiobookQueue() {
        lastAudiobook.value = trackPlayLogDao.getLastByType(AudioService.AudioType.Audiobook.ordinal)
        lastAudiobook.value?.let {
            lastAudiobookQueue.value = trackPlayLogDao.getAllByQueueId(it.queueId)
        }
    }

    fun addInitialEmptyQueueLog(queueId: Long, tracks: List<AudioService.TrackData>) {
        serviceScope.launch {
            val logs = List(tracks.size) { index ->
                val track = tracks[index]
                TrackPlayLog(
                    trackId = track.id,
                    queueId = queueId,
                    trackType = track.type.ordinal,
                    queueIndex = index,
                )
            }
            trackPlayLogDao.insertAll(logs)
        }
    }

    fun updateLogTimeValues(queueId: Long, trackId: Long, playbackEndedMs: Long, playbackDurationMs: Long) {
        serviceScope.launch {
            trackPlayLogDao.updateLogTimeValues(queueId, trackId, playbackEndedMs, playbackDurationMs)
            println("$trackId, $playbackDurationMs")
        }
    }

    fun updateLogQueueIndex(queueId: Long, trackId: Long, newIndex: Int) {
        serviceScope.launch {
            trackPlayLogDao.updateLogQueueIndex(queueId, trackId, newIndex)
        }
    }

    fun addEmptyPlayLog(track: AudioService.TrackData, queueIndex: Int, queueId: Long) {
        serviceScope.launch {
            val trackPlayLog = TrackPlayLog(
                trackId = track.id,
                trackType = track.type.ordinal,
                queueIndex = queueIndex,
                queueId = queueId
            )
            trackPlayLogDao.insertAll(listOf(trackPlayLog))
        }
    }

    fun deleteLogFromQueue(index: Int, queueId: Long) {
        serviceScope.launch {
            deleteFromQueueAndReIndex(index, queueId)
        }
    }

    //they all have to succeed or fail
    @Transaction
    suspend fun deleteFromQueueAndReIndex(index: Int, queueId: Long) {
        //detach and delete are opposites and both will never run at the same time
        trackPlayLogDao.detachLogFromQueue(index, queueId, System.currentTimeMillis())
        trackPlayLogDao.deleteLogFromQueue(index, queueId)
        trackPlayLogDao.decreaseIndexAfter(index, queueId)
    }

    fun fetchLogs() {
        serviceScope.launch {
            allLogs.value = trackPlayLogDao.getAllPlayedLogs().reversed()
            lastPodcast.value = trackPlayLogDao.getLastByType(AudioService.AudioType.Podcast.ordinal)
            mostPlayedRecentTracks.value = trackPlayLogDao.getMostPlayedFromDate(
                System.currentTimeMillis().minus(7.days.inWholeMilliseconds), AudioService.AudioType.Song.ordinal)
            fetchLastAudiobookQueue()
        }
    }
}