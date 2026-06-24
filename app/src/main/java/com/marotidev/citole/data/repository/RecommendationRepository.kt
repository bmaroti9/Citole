package com.marotidev.citole.data.repository

import android.util.Log
import androidx.room.Index
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

class RecommendationRepository @Inject constructor(
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
    var lastPodcast: MutableStateFlow<TrackPlayLog?> = MutableStateFlow(null)

    var lastAudiobook: MutableStateFlow<TrackPlayLog?> = MutableStateFlow(null)
    var lastAudiobookQueue: MutableStateFlow<List<TrackPlayLog>> = MutableStateFlow(emptyList())

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

    fun fetchLogs() {
        serviceScope.launch {
            allLogs.value = trackPlayLogDao.getAll().reversed()
            lastPodcast.value = trackPlayLogDao.getLastByType(AudioService.AudioType.Podcast.ordinal)
            fetchLastAudiobookQueue()
        }
    }

    suspend fun fetchLastAudiobookQueue() {
        lastAudiobook.value = trackPlayLogDao.getLastByType(AudioService.AudioType.Audiobook.ordinal)
        lastAudiobook.value?.let {
            lastAudiobookQueue.value = trackPlayLogDao.getAllByQueueId(it.queueId)
        }
    }

    fun deleteLogFromQueue(index: Int, queueId: Long) {
        serviceScope.launch {
            deleteFromQueueAndReIndex(index, queueId)
        }
    }

    //they both have to succeed or fail
    @Transaction
    suspend fun deleteFromQueueAndReIndex(index: Int, queueId: Long) {
        trackPlayLogDao.deleteLogFromQueue(index, queueId)
        trackPlayLogDao.decreaseIndexAfter(index, queueId)
    }
}