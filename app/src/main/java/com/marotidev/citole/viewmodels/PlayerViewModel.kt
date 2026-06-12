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

package com.marotidev.citole.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadataRetriever
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.common.util.concurrent.MoreExecutors
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.services.PlaybackService
import com.materialkolor.ktx.themeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.plus
import com.marotidev.citole.services.AudioService.toAudioData
import com.marotidev.citole.services.AudioService.toMediaItem

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    var playing by mutableStateOf<Boolean>(false)
        private set

    var currentQueue = mutableStateListOf<AudioService.AudioData>()
        private set

    var currentlyPlaying by mutableStateOf<AudioService.AudioData?>(null)
    var currentIndex by mutableIntStateOf(0)

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
        val sessionToken = SessionToken(application,
            ComponentName(application, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                player = controllerFuture.get()

                currentQueue.addAll(
                    (0 until (player?.mediaItemCount ?: 0)).map { index ->
                        player!!.getMediaItemAt(index).toAudioData()
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
            while (isActive) {
                progress = player?.currentPosition ?: 0
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun playQueue(tracks: List<AudioService.AudioData>, startIndex: Int = 0) {
        currentQueue.clear()
        currentQueue.addAll(tracks)
        currentIndex = startIndex
        currentlyPlaying = tracks[startIndex]

        val mediaItems = tracks.map { it.toMediaItem() }

        player?.setMediaItems(mediaItems, startIndex, 0L)
        player?.prepare()
        player?.play()
    }

    fun addToQueue(track: AudioService.AudioData, index: Int = currentQueue.size) {
        val mediaItem = track.toMediaItem()
        if (currentQueue.isEmpty()) {
            currentQueue += track
            currentIndex = 0
            currentlyPlaying = track

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
            progress = 0
        } else {
            currentQueue.add(index, track)
            player?.addMediaItem(index, mediaItem)
        }
    }

    fun removeFromQueue(index: Int) {
        currentQueue.removeAt(index)
        player?.removeMediaItem(index)
    }

    fun reorderInQueue(from: Int, to: Int) {
        currentQueue.add(to, currentQueue.removeAt(from))
        player?.moveMediaItem(from, to)
    }

    fun skipInQueue(newIndex: Int) {
        currentIndex = newIndex
        currentlyPlaying = currentQueue.getOrNull(currentIndex)
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
        player?.stop()
        player?.clearMediaItems()
        currentQueue.clear()
        currentIndex = 0
        currentlyPlaying = null
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
    }
}