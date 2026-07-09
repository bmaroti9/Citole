package com.marotidev.citole.data.state

import androidx.compose.runtime.mutableStateListOf
import com.marotidev.citole.data.service.AudioService
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

data class QueueItem(
    val track: AudioService.TrackData,
    val isGenerated: Boolean = false,
    val id: String = UUID.randomUUID().toString(),
)

class PlaybackStateHolder {
    val queueId = MutableStateFlow<Long>(0)

    val playerQueue = MutableStateFlow<List<QueueItem>>(emptyList())
    val generatedQueue = MutableStateFlow<List<QueueItem>>(emptyList())

    val currentIndex = MutableStateFlow(0)
    val currentlyPlaying = MutableStateFlow<QueueItem?>(null)

    val lastKnownDuration = MutableStateFlow<Long>(0)
}