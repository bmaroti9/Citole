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

package com.marotidev.citole.presentation.home.track

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuDefaults.rememberDropdownMenuPopupPositionProvider
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.app.ArtistViewDestination
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.durationToString
import com.marotidev.citole.presentation.utils.tintedPainter
import com.marotidev.citole.presentation.utils.verticalScrollbar
import com.materialkolor.ktx.darken
import com.materialkolor.ktx.harmonize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

@Composable
fun TrackListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    trackListViewModel: TrackListViewModel = hiltViewModel(),
) {
    val filteredTracks by trackListViewModel.filteredTracks.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScrollbar(
                state = listState,
                thumbColor = MaterialTheme.colorScheme.tertiaryContainer.darken(1.25f),
                barColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .padding(start = 16.dp, end = 24.dp)
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp),
        state = listState
    ) {
        itemsIndexed(
            items = filteredTracks,
            key = { index, track -> track.id }
        ) { index, track ->
            SwipeableTrackItem (
                track = track,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                playerViewModel = playerViewModel,
                index = index,
                count = filteredTracks.size,
                navController = navController
            ) {
                scope.launch {
                    val generatedQueue = trackListViewModel.generateQueueFromSeed(track.id)
                    playerViewModel.playQueue(generatedQueue)
                }
            }
        }
    }
}

@Composable
fun SwipeableTrackItem(
    track: AudioService.TrackData,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    navController: NavController,
    onClicked: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val swipeState =
        rememberSwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
        )

    var hapticTriggerState by remember { mutableIntStateOf(0) }

    val playNextContainer = Color(0xFFFF9300).harmonize(other = MaterialTheme.colorScheme.primary)
    val playNextOnContainer = Color(0xFF563200).harmonize(other = MaterialTheme.colorScheme.primary)
    val addToQueueContainer = Color(0xFF00B2B2).harmonize(other = MaterialTheme.colorScheme.primary)
    val addToQueueOnContainer = Color(0xFF004141).harmonize(other = MaterialTheme.colorScheme.primary)

    val iconScale = remember { Animatable(1f) }

    SwipeToDismissBox(
        modifier = modifier,
        state = swipeState,
        onDismiss = { value ->
            coroutineScope.launch {
                launch {
                    iconScale.animateTo(
                        1.25f,
                        spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    iconScale.animateTo(
                        1f,
                        spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                }

                delay(300)
                if (value == SwipeToDismissBoxValue.StartToEnd) {
                    playerViewModel.addToQueue(track, playerViewModel.currentIndex + 1)
                } else {
                    playerViewModel.addToQueue(track)
                }
                swipeState.reset()
            }
        },
        backgroundContent = {
            val direction = swipeState.dismissDirection

            val draggedPx = try {
                swipeState.requireOffset()
            } catch (e: IllegalStateException) {
                0f
            }

            LaunchedEffect(draggedPx) {
                val absoluteOffset = abs(draggedPx)

                if (absoluteOffset > 100f && hapticTriggerState.sign != draggedPx.toInt().sign) {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    hapticTriggerState = draggedPx.toInt().sign
                }

                else if (absoluteOffset < 10f) {
                    hapticTriggerState = 0
                }
            }

            Box(Modifier.fillMaxSize(),
                contentAlignment = if (draggedPx < 0) {Alignment.CenterEnd} else {Alignment.CenterStart}
            ) {
                Box(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { abs(draggedPx).toDp() })
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            if (direction == SwipeToDismissBoxValue.StartToEnd) {playNextContainer}
                            else {addToQueueContainer}
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    //for some reason if i don't add this it thinks it's a rowScope
                    androidx.compose.animation.AnimatedVisibility(
                        visible = abs(draggedPx) > 100f,
                        enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                        exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                        modifier = Modifier.scale(iconScale.value)
                    ) {

                        if (direction == SwipeToDismissBoxValue.StartToEnd) {
                            Icon(
                                painterResource(R.drawable.ic_read_more),
                                contentDescription = "Play next",
                                tint = playNextOnContainer,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        else {
                            Icon(
                                painterResource(R.drawable.ic_low_priority),
                                contentDescription = "Add to Queue",
                                tint = addToQueueOnContainer
                            )
                        }

                    }
                }
            }
        },
    ) {
        TrackItem(
            track,
            playerViewModel,
            index = index,
            count = count,
            navController = navController,
        ) { onClicked() }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackItem(
    track: AudioService.TrackData,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    navController: NavController,
    elevation: Dp = 0.dp,
    dragHandle: (@Composable () -> Unit)? = null,
    onClicked: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val checked = playerViewModel.currentlyPlaying?.id == track.id
    var popupExpanded by remember { mutableStateOf(false) }

    SegmentedListItem(
        modifier = modifier.padding(vertical = 1.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
        checked = checked,
        onCheckedChange = {
            onClicked()
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        shapes = if (count == 1) {
            ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
        } else {
            ListItemDefaults.segmentedShapes(index = index, count = count)
        },
        elevation = ListItemElevation(draggedElevation = 0.dp, elevation = elevation),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                dragHandle?.invoke()
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    error = tintedPainter(R.drawable.ic_citole_black, MaterialTheme.colorScheme.outline),
                    contentScale = ContentScale.Crop
                )
            }

        },
        content = {
            Text(
                text = track.title,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        supportingContent = {
            FlowRow (
                modifier = Modifier.padding(top = 1.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = track.artists.joinToString(separator = ", "),
                    style = MaterialTheme.typography.labelSmall,
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(MaterialTheme.colorScheme.outline, CircleShape)
                )
                Text(
                    text = durationToString(track.duration),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

        },
        trailingContent = {

            FilledTonalIconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (checked) { MaterialTheme.colorScheme.secondary}
                    else { MaterialTheme.colorScheme.secondaryContainer},
                    contentColor = if (checked) { MaterialTheme.colorScheme.onSecondary}
                    else { MaterialTheme.colorScheme.onSecondaryContainer},
                ),
                onClick = {popupExpanded = true },
                modifier = Modifier.size(26.dp, 30.dp),
                shapes = IconButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.small
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "Options",
                    modifier = Modifier.size(16.dp)
                )
            }

            TrackOptionsPopup(
                expanded = popupExpanded,
                onDismiss = {popupExpanded = false},
                playerViewModel,
                track,
                navController
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackOptionsPopup(
    expanded: Boolean,
    onDismiss: () -> Unit,
    playerViewModel: PlayerViewModel,
    track: AudioService.TrackData,
    navController: NavController,
) {
    val groupInteractionSource = remember { MutableInteractionSource() }
    var artistMenuExpanded by remember { mutableStateOf(false) }

    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 0, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            DropdownMenuItem(
                text = { Text("Play Next") },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_read_more),
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                onClick = {
                    playerViewModel.addToQueue(
                        track,
                        playerViewModel.currentIndex + 1
                    ); onDismiss()
                }
            )
            DropdownMenuItem(
                text = { Text("Add to queue") },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_low_priority),
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                onClick = { playerViewModel.addToQueue(track); onDismiss() }
            )
        }

        Spacer(Modifier.height(3.dp))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 1, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            Box {
                DropdownMenuItem(
                    text = { Text("Go to Artist") },
                    trailingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_person),
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    onClick = {
                        if (track.artists.size == 1) {
                            navController.navigate(ArtistViewDestination(artistName = track.artists[0])) {
                                launchSingleTop = true
                            }
                            onDismiss()
                        } else {
                            artistMenuExpanded = true
                        }
                    }
                )

                DropdownMenuPopup(
                    expanded = artistMenuExpanded,
                    onDismissRequest = { artistMenuExpanded = false },
                    popupPositionProvider = rememberDropdownMenuPopupPositionProvider(
                        dropdownMenuAnchorPosition = MenuAnchorPosition.Start
                    ),
                ) {
                    val subInteractionSource = remember { MutableInteractionSource() }

                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShape(index = 0, count = 1),
                        interactionSource = subInteractionSource,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        track.artists.forEachIndexed { index, artist ->
                            DropdownMenuItem(
                                text = { Text(artist) },
                                onClick = {
                                    navController.navigate(ArtistViewDestination(artistName = track.artists[index])) {
                                        launchSingleTop = true
                                    }
                                    artistMenuExpanded = false
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }

            DropdownMenuItem(
                text = { Text("Go to Album") },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_album),
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                onClick = {
                    navController.navigate(AlbumViewDestination(albumId = track.albumId)) {
                        launchSingleTop = true
                    }
                    onDismiss()
                }
            )
        }

        Spacer(Modifier.height(3.dp))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 2, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            DropdownMenuItem(
                text = { Text("About") },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_info),
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                onClick = { }
            )
        }
    }
}