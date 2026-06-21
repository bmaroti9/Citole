package com.marotidev.citole.data.repository

import android.util.Log
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

    var allLogs: MutableStateFlow<List<TrackPlayLog>> = MutableStateFlow(emptyList())

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun addToPlayLog(track: AudioService.TrackData, playbackDurationMs: Long) {
        serviceScope.launch {
            val trackPlayLog = TrackPlayLog(
                trackId = track.id,
                playbackEndedMs = System.currentTimeMillis(),
                playbackDurationMs = playbackDurationMs,
                trackType = track.type.ordinal
            )
            trackPlayLogDao.insertAll(trackPlayLog)
            Log.i("AddedToLog", "${track.id}, $playbackDurationMs")
        }
    }

    fun fetchLogs() {
        serviceScope.launch {
            allLogs.value = trackPlayLogDao.getAll().reversed()
        }
    }
}