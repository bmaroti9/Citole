package com.marotidev.citole.presentation.player

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.marotidev.citole.data.local.TrackPlayLog
import com.marotidev.citole.data.local.TrackPlayLogDao
import com.materialkolor.ktx.themeColor
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

class PlayerViewModel @Inject constructor(
    private val application: Application,
    private val trackPlayLogDao: TrackPlayLogDao,
    private val audioService: AudioService
) : ViewModel() {

    var playing by mutableStateOf(false)
        private set

    var currentQueue = mutableStateListOf<AudioService.TrackData>()
        private set

    var currentlyPlaying by mutableStateOf<AudioService.TrackData?>(null)
    var currentIndex by mutableIntStateOf(0)

    var progress by mutableLongStateOf(0L)

    private var player : MediaController? = null

    private var progressJob: Job? = null

    private var systemPrimaryColor = Color.Cyan
    private val _themeColor = MutableStateFlow(Color.Gray)
    val themeColor: StateFlow<Color> = _themeColor.asStateFlow()

    private val imageLoader = ImageLoader(application)

    fun addToPlayLog(track: AudioService.TrackData, playbackDurationMs: Long) {
        viewModelScope.launch {
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
                //log the previous play
                currentlyPlaying?.let {
                    addToPlayLog(it, progress)
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

    fun playQueue(tracks: List<AudioService.TrackData>, startIndex: Int = 0) {
        currentQueue.clear()
        currentQueue.addAll(tracks)
        currentIndex = startIndex
        currentlyPlaying = tracks[startIndex]

        val mediaItems = tracks.map { with(audioService) {it.toMediaItem()} }

        player?.setMediaItems(mediaItems, startIndex, 0L)
        player?.prepare()
        player?.play()
    }

    fun addToQueue(track: AudioService.TrackData, index: Int = currentQueue.size) {
        val mediaItem = with(audioService) {track.toMediaItem()}
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