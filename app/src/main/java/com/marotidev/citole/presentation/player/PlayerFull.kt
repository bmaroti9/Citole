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
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.data.state.QueueItem
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.app.ArtistViewDestination
import com.marotidev.citole.presentation.utils.durationToString
import com.marotidev.citole.presentation.utils.tintedPainter
import com.materialkolor.ktx.darken
import kotlin.math.PI
import kotlin.math.sin

@ExperimentalMaterial3ExpressiveApi
@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel,
    currentlyPlaying : QueueItem,
    navController: NavController,
    onPlayerClose: () -> Unit
) {

    var queueSheetOpen by remember { mutableStateOf(false) }
    val track = currentlyPlaying.track

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .requiredHeightIn(720.dp)
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .systemBarsPadding(),
    ) {
        if (queueSheetOpen) {
            QueueSheet(
                onDismissRequest = { queueSheetOpen = false },
                playerViewModel,
                navController
            )
        }

        StatusBar(onPlayerClose)

        Spacer(Modifier.weight(0.3f))

        ThumbnailCard(track)

        Spacer(Modifier.weight(0.4f))

        TitleAndArtists(track, navController, onPlayerClose)

        Spacer(Modifier.weight(0.3f))

        ExpressiveWavySlider(playerViewModel, track)

        Spacer(Modifier.weight(0.7f))

        PlayPauseRow(playerViewModel)

        Spacer(Modifier.weight(1.1f))

        PlayerBottomBar(
            playerViewModel
        ) {
            playerViewModel.checkExtendQueue()
            queueSheetOpen = true
        }

        Spacer(Modifier.weight(0.4f))

    }
}

@Composable
fun StatusBar(onPlayerClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilledTonalIconButton (
            onClick = { onPlayerClose() },
            shapes = IconButtonDefaults.shapes(
                shape = CircleShape,
                pressedShape = MaterialTheme.shapes.medium
            ),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.darken(1.2f)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_keyboard_down),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text("Now Playing", style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        FilledTonalIconButton (
            onClick = {  },
            shapes = IconButtonDefaults.shapes(
                shape = CircleShape,
                pressedShape = MaterialTheme.shapes.medium
            ),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.darken(1.2f)
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_vert),
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThumbnailCard(
    track : AudioService.TrackData,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = track.artworkUri,
            contentDescription = "Album Art",
            error = tintedPainter(R.drawable.ic_citole_black, MaterialTheme.colorScheme.outline),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun TitleAndArtists(
    track : AudioService.TrackData,
    navController: NavController,
    onPlayerClose: () -> Unit,
) {

    Text(
        track.title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 26.sp,
        modifier = Modifier
            .basicMarquee()
            .padding(horizontal = 25.dp)
            .clickable(
                onClick = {
                    navController.navigate(AlbumViewDestination(albumId = track.albumId)) {
                        launchSingleTop = true
                    }
                    onPlayerClose()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
    )

    FlowRow(
        modifier = Modifier.padding(horizontal = 25.dp, vertical = 2.dp)
    ) {
        track.artists.forEachIndexed { index, artist ->
            Text(
                if (index == track.artists.size - 1) {artist} else {
                    "$artist, "
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            navController.navigate(ArtistViewDestination(artistName = artist)) {
                                launchSingleTop = true
                            }
                            onPlayerClose()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
            )
        }
    }

}

@Composable
fun CustomWavySlider(
    playerViewModel: PlayerViewModel,
    currentlyPlaying: AudioService.TrackData,
    sliderDragGlobal: Float,
    onSliderDragChanged: (Float) -> Unit,
    sliderInteractionSource: MutableInteractionSource,
)  {

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
        },
        interactionSource = sliderInteractionSource,
        track = { sliderState ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
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



@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveWavySlider(playerViewModel: PlayerViewModel, track: AudioService.TrackData) {

    var sliderDrag by remember { mutableFloatStateOf(0f) }
    val sliderInteractionSource = remember { MutableInteractionSource() }
    val isDragged by sliderInteractionSource.collectIsDraggedAsState()

    val timerTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontFeatureSettings = "tnum", //tabular numbers
    )

    Row(
        modifier = Modifier.padding(horizontal = 25.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            durationToString(
                if (isDragged) {
                    (sliderDrag * track.duration).toLong()
                } else {
                    playerViewModel.progress
                }
            ),
            style = timerTextStyle,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f)
        ) {
            CustomWavySlider(
                playerViewModel,
                track,
                sliderDrag,
                onSliderDragChanged = {
                    sliderDrag = it
                },
                sliderInteractionSource
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(durationToString(track.duration), style = timerTextStyle, color = MaterialTheme.colorScheme.onSurface)
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
            modifier = Modifier.align(Alignment.Center).width(250.dp)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
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
fun PlayerBottomBar(
    playerViewModel: PlayerViewModel,
    onOpenDialog: () -> Unit
) {

    val repeatIcon = when (playerViewModel.repeatMode) {
        Player.REPEAT_MODE_OFF -> R.drawable.ic_repeat
        Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
        else -> R.drawable.ic_repeat
    }

    Row (
        modifier = Modifier
            .fillMaxWidth(0.81f)
            .height(70.dp)
            .background(MaterialTheme.colorScheme.primaryContainer.darken(1.2f), CircleShape)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        ToggleButton(
            modifier = Modifier.fillMaxHeight().weight(1f),
            checked = false,
            onCheckedChange = {  },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(painterResource(R.drawable.ic_favorite), contentDescription = null, modifier = Modifier.size(21.dp))
        }
        ToggleButton(
            modifier = Modifier.fillMaxHeight().weight(1f),
            checked = playerViewModel.repeatMode != Player.REPEAT_MODE_OFF,
            onCheckedChange = { playerViewModel.toggleRepeat() },
            shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(painterResource(repeatIcon), contentDescription = null, modifier = Modifier.size(21.dp))
        }
        ToggleButton(
            modifier = Modifier.fillMaxHeight().weight(1f),
            checked = false,
            onCheckedChange = { onOpenDialog() },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Icon(painterResource(R.drawable.ic_queue_music), contentDescription = null, modifier = Modifier.size(21.dp))
        }
    }
}
