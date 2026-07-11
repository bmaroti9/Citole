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

package com.marotidev.citole.presentation.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.MoreExecutors
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.PlaybackService
import com.marotidev.citole.data.repository.TrackLogRepository
import com.marotidev.citole.data.state.PlaybackStateHolder
import com.marotidev.citole.data.state.QueueItem
import com.materialkolor.ktx.themeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.map
import kotlin.time.Duration.Companion.milliseconds
import kotlin.collections.plus

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val application: Application,
    private val audioService: AudioService,
    private val trackLogRepository: TrackLogRepository,
    private val playbackStateHolder: PlaybackStateHolder,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    val currentlyPlaying = playbackStateHolder.currentlyPlaying

    val playerQueue = playbackStateHolder.playerQueue
    val generatedQueue = playbackStateHolder.generatedQueue
    val currentIndex = playbackStateHolder.currentIndex

    var playing by mutableStateOf(false)
        private set


    var progress by mutableLongStateOf(0L)

    private var player : MediaController? = null

    private var progressJob: Job? = null

    private var systemPrimaryColor = Color.Cyan
    private val _themeColor = MutableStateFlow(Color.Gray)
    val themeColor: StateFlow<Color> = _themeColor.asStateFlow()

    private val imageLoader = ImageLoader(application)


    private suspend fun updateColorFromAlbumArt(artworkUri: Uri?, context: Context) {
        if (artworkUri == null) {
            _themeColor.value = systemPrimaryColor
            return
        }

        val request = ImageRequest.Builder(context)
            .data(artworkUri)
            .size(64)
            .allowHardware(false)
            .build()

        val result = imageLoader.execute(request)
        if (result is SuccessResult) {

            val seedColor = withContext(Dispatchers.Default) {
                val bitmap = result.drawable.toBitmap().asImageBitmap()
                bitmap.themeColor(fallback = systemPrimaryColor)
            }
            _themeColor.value = seedColor
        }
        else {
            _themeColor.value = systemPrimaryColor
        }
    }

    init {
        val sessionToken = SessionToken(
            application,
            ComponentName(application, PlaybackService::class.java)
        )
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()

                playing = player?.isPlaying ?: false
                if (playing) {
                    startProgressUpdate()
                }

                setupListeners()
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupListeners() {
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        stopProgressUpdate()
                    }
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
                playing = playWhenReady
            }
        })

        viewModelScope.launch {
            playbackStateHolder.currentlyPlaying
                .map { queueItem -> queueItem?.track?.artworkUri }
                .distinctUntilChanged()
                .collect { artworkUri ->
                    updateColorFromAlbumArt(artworkUri, application)
                }
        }
    }


    fun updateDefaultColor(color: Color) {
        if (playbackStateHolder.currentlyPlaying.value == null) {
            systemPrimaryColor = color
            _themeColor.value = color
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (NonCancellable.isActive) {
                progress = player?.currentPosition ?: 0
                playbackStateHolder.lastKnownDuration.value = progress
                delay(500.milliseconds)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun playQueue(tracks: List<AudioService.TrackData>, startIndex: Int = 0, startPosition: Long = 0,
                  givenQueueId: Long? = null) {
        if (givenQueueId == null) {
            val queueId = System.currentTimeMillis()
            playbackStateHolder.queueId.value = queueId
            trackLogRepository.addInitialEmptyQueueLog(queueId, tracks)
        } else {
            playbackStateHolder.queueId.value = givenQueueId
        }

        playbackStateHolder.playerQueue.update {
            tracks.map { track -> QueueItem(track) }
        }

        val mediaItems = tracks.map { with(audioService) {it.toMediaItem()} }

        player?.setMediaItems(mediaItems, startIndex, startPosition)
        player?.prepare()
        player?.play()
    }

    fun addToQueue(track: AudioService.TrackData, index: Int = playerQueue.value.size) {
        val mediaItem = with(audioService) {track.toMediaItem()}
        if (playerQueue.value.isEmpty()) {
            playbackStateHolder.queueId.value = System.currentTimeMillis()

            playbackStateHolder.playerQueue.update {
                it + QueueItem(track)
            }

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
            progress = 0
        } else {
            playbackStateHolder.playerQueue.update { currentQueue ->
                currentQueue.toMutableList().apply {
                    add(index, QueueItem(track))
                }
            }
            player?.addMediaItem(index, mediaItem)
            
            syncQueueLogIndexes()
        }

        trackLogRepository.addEmptyPlayLog(track, index, playbackStateHolder.queueId.value)
    }

    fun removeFromPlayerQueue(index: Int) {
        trackLogRepository.deleteLogFromQueue(index, playbackStateHolder.queueId.value)
        syncQueueLogIndexes()

        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                removeAt(index)
            }
        }

        player?.removeMediaItem(index)
    }

    fun removeFromGeneratedQueue(index: Int) {
        playbackStateHolder.generatedQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun reorderInQueue(from: Int, to: Int) {
        trackLogRepository.updateLogQueueIndex(playbackStateHolder.queueId.value,
            playerQueue.value[from].track.id, to)
        trackLogRepository.updateLogQueueIndex(playbackStateHolder.queueId.value,
            playerQueue.value[to].track.id, from)

        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                val item = removeAt(from)
                add(to, item)
            }
        }
        player?.moveMediaItem(from, to)
    }

    fun reorderFromGeneratedToPlayerQueue(from: Int, to: Int) {
        trackLogRepository.addEmptyPlayLog(generatedQueue.value[from].track, to, playbackStateHolder.queueId.value)
        //bumpQueueLogIndexes(to, 1)

        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                val item = QueueItem(generatedQueue.value[from].track, isGenerated = false)
                add(to, item)
            }
        }

        playbackStateHolder.generatedQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                removeAt(from)
            }
        }
    }

    fun updateQueuesAfterDrag(
        finalPlayerList: List<QueueItem>,
        finalGeneratedList: List<QueueItem>
    ) {
        if (finalGeneratedList == generatedQueue.value) {
            println("player queue reorder")
            //reorder happened in the player queue
            val indexes = mutableListOf<Int>()
            finalPlayerList.forEachIndexed { index, item ->
                if (playerQueue.value[index] != item) {
                    indexes += index
                }
            }
            if (indexes.size == 2) {
                trackLogRepository.updateLogQueueIndex(playbackStateHolder.queueId.value,
                    playerQueue.value[indexes[0]].track.id, indexes[1])
                trackLogRepository.updateLogQueueIndex(playbackStateHolder.queueId.value,
                    playerQueue.value[indexes[1]].track.id, indexes[0])

                player?.moveMediaItem(indexes[0], indexes[1])

                playbackStateHolder.playerQueue.value = finalPlayerList
            }
        } else {
            println("generated queue promotion")
            //a generated track got promoted

            var removed = 0
            for ((index, element) in finalGeneratedList.withIndex()) {
                if (generatedQueue.value[index] != element) {
                    removed = index
                    break
                }
            }

            var added = 0
            for ((index, element) in playerQueue.value.withIndex()) {
                if (element != finalPlayerList[index]) {
                    added = index
                    break
                }
            }

            println("$removed, $added")

            trackLogRepository.addEmptyPlayLog(generatedQueue.value[removed].track, added, playbackStateHolder.queueId.value)
            //bumpQueueLogIndexes(added, 1)

            with (audioService) {
                player?.addMediaItem(added, finalPlayerList[added].track.toMediaItem())
            }

            playbackStateHolder.playerQueue.value = finalPlayerList
            playbackStateHolder.generatedQueue.value = finalGeneratedList
        }
    }

    fun playerQueueItemReorder(item: QueueItem, items: List<Any>) {
        val from = playerQueue.value.indexOf(item)
        val to = items.indexOf(item)
        if (from == -1 || to == -1 || to >= playerQueue.value.size) return

        player?.moveMediaItem(from, to)
        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                if (remove(item)) {
                    add(to, item)
                }
            }
        }

        syncQueueLogIndexes()
    }

    fun generatedQueueToPlayerQueue(item: QueueItem, items: List<Any>) {
        val to = items.indexOf(item)
        if (to == -1 || to > playerQueue.value.size) return

        with (audioService) {
            player?.addMediaItem(to, item.track.toMediaItem())
        }
        trackLogRepository.addEmptyPlayLog(item.track, to, playbackStateHolder.queueId.value)

        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                add(to, item)
            }
        }

        playbackStateHolder.generatedQueue.update { currentQueue ->
            currentQueue.toMutableList().apply {
                remove(item)
            }
        }

        syncQueueLogIndexes()
    }

    fun skipInQueue(newIndex: Int) {
        player?.seekTo(newIndex, 0L)
        player?.play()
    }

    fun skipToGeneratedInQueue(index: Int) {
        playbackStateHolder.playerQueue.update { currentQueue ->
            currentQueue + QueueItem(playbackStateHolder.generatedQueue.value[index].track, isGenerated = false)
        }
        viewModelScope.launch {
            val newTracks = recommendationRepository.extendQueue(playerQueue.value.map {queueItem -> queueItem.track.id }, 12)
            playbackStateHolder.generatedQueue.update {
                newTracks.map {track -> QueueItem(track, isGenerated = true) }
            }
        }
    }

    fun syncQueueLogIndexes() {
        playerQueue.value.forEachIndexed { index, item ->
            trackLogRepository.updateLogQueueIndex(playbackStateHolder.queueId.value,
                item.track.id, index)
        }
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
        progress = position
    }

    fun skipNext() {
        if (player?.hasNextMediaItem() ?: false) {
            player?.seekToNext()
            progress =  0
        }
    }

    fun skipPrevious() {
        player?.seekToPrevious()
        progress =  0
    }

    fun dismissPlayer() {
        playbackStateHolder.currentlyPlaying.value?.let {
            trackLogRepository.updateLogTimeValues(playbackStateHolder.queueId.value, trackId = it.track.id,
                System.currentTimeMillis(), progress)
        }

        playbackStateHolder.playerQueue.update { emptyList() }
        playbackStateHolder.generatedQueue.update { emptyList() }
        progress = 0
        playbackStateHolder.currentIndex.value = 0
        playbackStateHolder.currentlyPlaying.value = null

        player?.stop()
        player?.clearMediaItems()
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
    }
}