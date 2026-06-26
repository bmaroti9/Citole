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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.MoreExecutors
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.service.PlaybackService
import com.marotidev.citole.data.repository.TrackLogRepository
import com.materialkolor.ktx.themeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val application: Application,
    private val audioService: AudioService,
    private val trackLogRepository: TrackLogRepository,
) : ViewModel() {

    var playing by mutableStateOf(false)
        private set

    var currentQueue = mutableStateListOf<AudioService.TrackData>()
        private set

    var currentlyPlaying by mutableStateOf<AudioService.TrackData?>(null)
    var currentIndex by mutableIntStateOf(0)

    private var queueId : Long = 0

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

                currentQueue.addAll(
                    (0 until (player?.mediaItemCount ?: 0)).map { index ->
                        with (audioService) {
                            player!!.getMediaItemAt(index).toAudioData()
                        }
                    }
                )

                currentIndex = player?.currentMediaItemIndex ?: 0
                currentlyPlaying = currentQueue.getOrNull(currentIndex)
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

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                //log the play times
                currentlyPlaying?.let {
                    trackLogRepository.updateLogTimeValues(queueId, it.id,
                        playbackEndedMs = System.currentTimeMillis(), playbackDurationMs = progress)
                }

                currentIndex = player?.currentMediaItemIndex ?: 0
                currentlyPlaying = currentQueue.getOrNull(currentIndex)
            }
        })

        viewModelScope.launch {
            snapshotFlow { currentlyPlaying?.artworkUri }.collect { artworkUri ->
                updateColorFromAlbumArt(artworkUri, application)
            }
        }
    }


    fun updateDefaultColor(color: Color) {
        if (currentlyPlaying == null) {
            systemPrimaryColor = color
            _themeColor.value = color
        }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (NonCancellable.isActive) {
                progress = player?.currentPosition ?: 0
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
            queueId = System.currentTimeMillis()
            trackLogRepository.addInitialEmptyQueueLog(queueId, tracks)
        } else {
            queueId = givenQueueId
        }

        currentQueue.clear()
        currentQueue.addAll(tracks)

        val mediaItems = tracks.map { with(audioService) {it.toMediaItem()} }

        player?.setMediaItems(mediaItems, startIndex, startPosition)
        player?.prepare()
        player?.play()
    }

    fun addToQueue(track: AudioService.TrackData, index: Int = currentQueue.size) {
        val mediaItem = with(audioService) {track.toMediaItem()}
        if (currentQueue.isEmpty()) {
            queueId = System.currentTimeMillis()

            currentQueue += track

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
            progress = 0
        } else {
            currentQueue.add(index, track)
            player?.addMediaItem(index, mediaItem)
        }

        trackLogRepository.addEmptyPlayLog(track, index, queueId)
    }

    fun removeFromQueue(index: Int) {
        trackLogRepository.deleteLogFromQueue(index, queueId)

        currentQueue.removeAt(index)
        player?.removeMediaItem(index)
    }

    fun reorderInQueue(from: Int, to: Int) {
        trackLogRepository.updateLogQueueIndex(queueId, currentQueue[from].id, to)
        trackLogRepository.updateLogQueueIndex(queueId, currentQueue[to].id, from)

        currentQueue.add(to, currentQueue.removeAt(from))
        player?.moveMediaItem(from, to)
    }

    fun skipInQueue(newIndex: Int) {
        player?.seekTo(newIndex, 0L)
        player?.play()
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
        currentlyPlaying?.let {
            trackLogRepository.updateLogTimeValues(queueId, trackId = it.id,
                System.currentTimeMillis(), progress)
        }

        currentQueue.clear()
        progress = 0
        currentIndex = 0
        currentlyPlaying = null

        player?.stop()
        player?.clearMediaItems()
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
    }
}