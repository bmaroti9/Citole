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

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.marotidev.citole.AudioHelper
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

class PlayerViewModel(context: Context) : ViewModel() {
    var playing by mutableStateOf<Boolean>(false)
        private set

    var currentQueue by mutableStateOf<List<AudioHelper.AudioData>>(emptyList())
        private set

    var currentlyPlaying by mutableStateOf<AudioHelper.AudioData?>(null)
    var currentIndex by mutableIntStateOf(0)

    var progress by mutableLongStateOf(0L)

    val player = ExoPlayer.Builder(context).build()

    private var progressJob: Job? = null

    private var systemPrimaryColor = Color.Cyan
    private val _themeColor = MutableStateFlow(Color.Gray)
    val themeColor: StateFlow<Color> = _themeColor.asStateFlow()

    private val imageLoader = ImageLoader(context)

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
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {

                    }
                    Player.STATE_ENDED -> {
                        stopProgressUpdate()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = player.currentMediaItemIndex
                currentlyPlaying = if (currentQueue.isNotEmpty() and (currentIndex >= 0) and (currentIndex < currentQueue.size)) {
                    currentQueue[currentIndex]
                } else {
                    null
                }

            }
        })

        viewModelScope.launch {
            snapshotFlow { currentlyPlaying?.artworkUri }.collect { artworkUri ->
                updateColorFromAlbumArt(artworkUri, context)
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
                progress = player.currentPosition
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun playQueue(tracks: List<AudioHelper.AudioData>, startIndex: Int = 0, shuffle: Boolean = false) {
        currentQueue = tracks
        currentIndex = startIndex
        currentlyPlaying = tracks[startIndex]

        val mediaItems = tracks.map { MediaItem.fromUri(it.uri) }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()

        playing = true
    }

    fun addToQueue(track: AudioHelper.AudioData) {
        val mediaItem = MediaItem.fromUri(track.uri)
        if (currentQueue.isEmpty()) {
            currentQueue += track
            currentIndex = 0
            currentlyPlaying = track

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            playing = true
        } else {
            currentQueue += track
            player.addMediaItem(mediaItem)
        }
    }

    fun skipInQueue(newIndex: Int) {
        currentIndex = newIndex
        currentlyPlaying = if (currentQueue.isNotEmpty() and (currentIndex >= 0) and (currentIndex < currentQueue.size)) {
            currentQueue[currentIndex]
        } else {
            null
        }
        player.seekTo(newIndex, 0L)
        player.play()
        playing = true
    }

    fun togglePlayPause() {
        if (playing) {
            player.pause()
        }
        else {
            player.play()
        }
        playing = !playing
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        progress = position
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    fun skipPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    fun dismissPlayer() {
        player.stop()
        player.clearMediaItems()
        currentQueue = emptyList()
        currentIndex = 0
        currentlyPlaying = null
        playing = false
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}