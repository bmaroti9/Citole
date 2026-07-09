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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.marotidev.citole.R
import com.marotidev.citole.data.state.QueueItem
import com.marotidev.citole.presentation.home.track.TrackItem
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.abs
import kotlin.math.sign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    onDismissRequest: () -> Unit,
    playerViewModel: PlayerViewModel,
    navController: NavController
) {

    val hapticFeedback = LocalHapticFeedback.current

    val currentQueue = playerViewModel.currentQueue.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()

    val sheetCornerRadius by animateDpAsState(
        targetValue = if (sheetState.currentValue == SheetValue.Expanded) 0.dp else 32.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        playerViewModel.reorderInQueue(from.index, to.index)
    }

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
            ) {
                itemsIndexed(
                    currentQueue.value,
                    key = { _, track -> track.id }
                ) { index, queueItem ->

                    ReorderableItem(
                        reorderableLazyListState,
                        key = queueItem.id,
                    ) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 3.dp else 0.dp)

                        QueueTrackItem (
                            queueItem,
                            playerViewModel,
                            index = index,
                            modifier = Modifier
                                .longPressDraggableHandle (
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    }
                                )
                                .animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                ),
                            dragHandleModifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                },
                            ),
                            elevation = elevation,
                            count = currentQueue.value.size,
                            navController = navController
                        ) {
                            playerViewModel.skipInQueue(index)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun QueueTrackItem(
    queueItem: QueueItem,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    dragHandleModifier : Modifier = Modifier,
    index: Int,
    count: Int,
    navController: NavController,
    elevation: Dp = 0.dp,
    onClicked: () -> Unit,
) {
    val currentlyPlaying = playerViewModel.currentlyPlaying.collectAsStateWithLifecycle()

    val dismissState = rememberSwipeToDismissBoxState(
        SwipeToDismissBoxValue.Settled,
        SwipeToDismissBoxDefaults.positionalThreshold
    )

    val haptic = LocalHapticFeedback.current
    var hapticTriggerState by remember { mutableIntStateOf(0) }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        onDismiss = {playerViewModel.removeFromQueue(index)},
        backgroundContent = {
            val draggedPx = try {
                dismissState.requireOffset()
            } catch (_: IllegalStateException) {
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
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    //for some reason if i don't add this it thinks it's a rowScope
                    androidx.compose.animation.AnimatedVisibility(
                        visible = abs(draggedPx) > 100f,
                        enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                        exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
    ) {
        TrackItem(
            queueItem.track,
            playerViewModel,
            index = index,
            count = count,
            dragHandle = {
                Icon(
                    painterResource(R.drawable.ic_drag_indicator),
                    "Drag handle",
                    modifier = dragHandleModifier
                        .padding(end = 8.dp),
                )
            },
            elevation = elevation,
            navController = navController,
            titleBadge = {
                if (queueItem.isGenerated) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wand_stars),
                        contentDescription = "Generated",
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialShapes.Flower.toShape())
                            .padding(3.dp)
                            .size(12.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            },
            checked = currentlyPlaying.value?.id == queueItem.id
        ) { onClicked() }
    }
}