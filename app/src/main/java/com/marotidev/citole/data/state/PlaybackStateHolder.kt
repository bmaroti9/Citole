package com.marotidev.citole.data.state

import androidx.media3.common.Player
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.data.service.AudioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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

    val queueSnapshotAtRegeneration = MutableStateFlow<List<Long>>(emptyList())

    suspend fun checkExtendQueue(
        recommendationRepository: RecommendationRepository,
        player: Player?,
        audioService: AudioService,
        force: Boolean = false
    ) {

        val currentIds = playerQueue.value.map { it.track.id }
        if (!force && currentIds == queueSnapshotAtRegeneration.value) return

        val newTracks = recommendationRepository.extendQueue(currentIds, 8)

        player?.removeMediaItems(playerQueue.value.size, playerQueue.value.size + generatedQueue.value.size)
        with (audioService) {
            player?.addMediaItems(newTracks.map {track -> track.toMediaItem()})
        }

        generatedQueue.update {
            newTracks.map {track -> QueueItem(track, isGenerated = true) }
        }
        queueSnapshotAtRegeneration.value = currentIds
    }
}