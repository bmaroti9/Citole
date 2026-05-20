package com.marotidev.citole
import android.content.ContentUris
import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
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
import kotlin.math.PI
import kotlin.math.sin

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

    private suspend fun updateColorFromAlbumArt(albumId: Long?, context: Context) {
        if (albumId == null) {
            _themeColor.value = systemPrimaryColor
            return
        }

        val artworkUri = ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            albumId
        )

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
            snapshotFlow { currentlyPlaying?.albumId }.collect { albumId ->
                updateColorFromAlbumArt(albumId, context)
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

//    fun playSong(uri: Uri) {
//        Log.i("PLAY SONG", "playing song: $uri")
//        val mediaItem = MediaItem.fromUri(uri)
//        player.setMediaItem(mediaItem)
//        player.prepare()
//        player.play()
//    }

    fun playQueue(tracks: List<AudioHelper.AudioData>, startIndex: Int = 0) {
        currentQueue = tracks
        currentIndex = startIndex

        val mediaItems = tracks.map { MediaItem.fromUri(it.uri) }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.play()
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

@ExperimentalMaterial3ExpressiveApi
@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    currentlyPlaying : AudioHelper.AudioData
) {

    val openAlertDialog = remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (openAlertDialog.value) {
            QueueDialog (
                onDismissRequest = {openAlertDialog.value = false},
                playerViewModel
            )
        }

        ThumbnailCard(currentlyPlaying)

        TitleAndArtist(currentlyPlaying)

        ExpressiveWavySlider(playerViewModel, currentlyPlaying)

        PlayPauseRow(playerViewModel)

        Spacer(Modifier.weight(1f))

        PlayerBottomBar({openAlertDialog.value = true})

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailCard(
    currentlyPlaying : AudioHelper.AudioData
) {
    val artworkUri = ContentUris.withAppendedId(
        "content://media/external/audio/albumart".toUri(),
        currentlyPlaying.albumId
    )
    Card(
        modifier = Modifier
            .padding(start = 40.dp, end = 40.dp, bottom = 30.dp, top = 80.dp)
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = artworkUri,
            contentDescription = "Album Art",
            error = painterResource(R.drawable.ic_library),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TitleAndArtist(
    currentlyPlaying : AudioHelper.AudioData
) {
    Text(
        currentlyPlaying.title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 26.sp,
        modifier = Modifier
            .basicMarquee()
            .padding(horizontal = 25.dp),
    )
    Text(
        currentlyPlaying.artist,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(horizontal = 25.dp)
    )
}

@Composable
fun CustomWavySlider(
    playerViewModel: PlayerViewModel,
    currentlyPlaying: AudioHelper.AudioData,
    sliderDragGlobal: Float,
    onSliderDragChanged: (Float) -> Unit,
    sliderInteractionSource: MutableInteractionSource,
)  {

    val haptic = LocalHapticFeedback.current

    val getPrimaryColor = rememberUpdatedState(MaterialTheme.colorScheme.primary)
    val getInactiveColor = rememberUpdatedState(MaterialTheme.colorScheme.surfaceContainer)

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(), // One full rotation
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing), // Adjust duration for speed
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val isPressed by sliderInteractionSource.collectIsPressedAsState()
    val isDragged by sliderInteractionSource.collectIsDraggedAsState()

    val animatedAmplitude by animateFloatAsState(
        targetValue = if (isPressed || isDragged || !playerViewModel.playing) 0.dp.value else 12.dp.value,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
    )

    val animatedPlayerProgress by animateFloatAsState(
        targetValue = if (isDragged) sliderDragGlobal else playerViewModel.progress.toFloat() / currentlyPlaying.duration,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
    )

    //need this to instantly update, otherwise clicking the slider will result in delayed values
    var sliderDragLocal by remember { mutableFloatStateOf(0f) }

    Slider(
        value = animatedPlayerProgress,
        onValueChange = {
            sliderDragLocal = it
            onSliderDragChanged(it)
        },
        onValueChangeFinished = {
            playerViewModel.seekTo((currentlyPlaying.duration * sliderDragLocal).toLong())
            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
        },
        interactionSource = sliderInteractionSource,
        track = { sliderState ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .drawWithCache {
                        val path = Path()
                        onDrawBehind {

                            val width = size.width
                            val centerY = size.height / 2
                            val waveLength = 22.dp.toPx()
                            val gap = (26.dp / width).value

                            if ((sliderState.value + gap) < 1) {
                                drawLine(
                                    start = Offset((sliderState.value + gap) * width, centerY),
                                    end = Offset(width, centerY),
                                    strokeWidth = 4.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    color = getInactiveColor.value
                                )
                                drawCircle(
                                    center = Offset(width, centerY),
                                    color = getPrimaryColor.value,
                                    radius = 2.dp.toPx()
                                )
                            }

                            val rangeStart = 0f
                            val rangeEnd = sliderState.value - gap
                            val startX = width * rangeStart
                            val endX = width * rangeEnd

                            if (startX < endX) {
                                path.reset()
                                var x = startX
                                while (x <= endX) {
                                    val relativeX = x / waveLength
                                    val y = centerY + (sin((relativeX * 2 * PI.toFloat()) + phase) * animatedAmplitude)

                                    if (x == startX) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                    x += 4f
                                }
                                drawPath(path, getPrimaryColor.value, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                            }
                        }
                    }
            )
        }
    )
}

fun durationToString(duration: Long) : String {
    return "%01d:%02d".format((duration / 1000) / 60, (duration / 1000) % 60)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveWavySlider(playerViewModel: PlayerViewModel, currentlyPlaying: AudioHelper.AudioData) {

    var sliderDrag by remember { mutableFloatStateOf(0f) }
    val sliderInteractionSource = remember { MutableInteractionSource() }
    val isDragged by sliderInteractionSource.collectIsDraggedAsState()

    val timerTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontFeatureSettings = "tnum", //tabular numbers
    )

    Row(
        modifier = Modifier.padding(horizontal = 25.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            durationToString(if (isDragged) {(sliderDrag * currentlyPlaying.duration).toLong()} else {playerViewModel.progress}),
            style = timerTextStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            CustomWavySlider(
                playerViewModel,
                currentlyPlaying,
                sliderDrag,
                onSliderDragChanged = { it ->
                    sliderDrag = it
                },
                sliderInteractionSource
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(durationToString(currentlyPlaying.duration), style = timerTextStyle, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayPauseRow(
    playerViewModel: PlayerViewModel
) {
    val haptic = LocalHapticFeedback.current

    val getOnPrimaryColor = rememberUpdatedState(MaterialTheme.colorScheme.onPrimary)
    val getPrimaryColor = rememberUpdatedState(MaterialTheme.colorScheme.primary)

    Box(
        modifier = Modifier.fillMaxWidth()
    )
    {
        ButtonGroup(
            expandedRatio = 0.3f,
            overflowIndicator = { menuState ->
                ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
            },
            horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally),
            modifier = Modifier.align(Alignment.Center).padding(top = 10.dp).width(250.dp)
        ) {
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val cornerRadius by animateDpAsState(
                        targetValue = if (isPressed) 10.dp else 50.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )

                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .weight(1f)
                            .drawBehind {
                                drawRoundRect(color = getOnPrimaryColor.value, cornerRadius = CornerRadius(cornerRadius.toPx()))
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                onClick = {
                                    playerViewModel.skipPrevious()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                indication = null
                            )
                            .animateWidth(interactionSource),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_skip_previous),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center).size(32.dp)
                        )
                    }
                },
                menuContent = {},
            )
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val cornerRadius by animateDpAsState(
                        targetValue = if (isPressed || playerViewModel.playing) 10.dp else 50.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )

                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .weight(1.5f)
                            .drawBehind {
                                drawRoundRect(color = getPrimaryColor.value, cornerRadius = CornerRadius(cornerRadius.toPx()))
                            }
                            .toggleable(
                                value = playerViewModel.playing,
                                interactionSource = interactionSource,
                                onValueChange = {
                                    playerViewModel.togglePlayPause()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                indication = null
                            )
                            .animateWidth(interactionSource),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (playerViewModel.playing) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                menuContent = {}
            )
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val cornerRadius by animateDpAsState(
                        targetValue = if (isPressed) 10.dp else 50.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioNoBouncy)
                    )

                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .weight(1f)
                            .drawBehind {
                                drawRoundRect(color = getOnPrimaryColor.value, cornerRadius = CornerRadius(cornerRadius.toPx()))
                            }
                            .clickable(
                                interactionSource = interactionSource,
                                onClick = {
                                    playerViewModel.skipNext()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                indication = null
                            )
                            .animateWidth(interactionSource),
                    ) {
                        Icon(

                            painter = painterResource(id = R.drawable.ic_skip_next),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center).size(32.dp)
                        )
                    }
                },
                menuContent = {}
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerBottomBar(onOpenDialog: () -> Unit) {
    Row (
        modifier = Modifier.padding(horizontal = 5.dp)
    ) {
        FilledTonalButton(
            onClick = {onOpenDialog()},
            modifier = Modifier.padding(vertical = 10.dp),
            contentPadding = PaddingValues(vertical = 18.dp, horizontal = 18.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_queue_music),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.size(6.dp))
            Text("Queue", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun QueueDialog(
    onDismissRequest: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            dialogWindowProvider?.window?.setDimAmount(0.2f)
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardColors(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondary,
            ),
            modifier = Modifier.height(500.dp)
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 10.dp)
            ) {
                items(playerViewModel.currentQueue.size) { index ->
                    TrackItem (
                        playerViewModel.currentQueue[index],
                        playerViewModel
                    ) {
                        playerViewModel.skipInQueue(index)
                    }
                }
            }
        }
    }
}