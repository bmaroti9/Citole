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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import com.materialkolor.ktx.darken
import kotlin.collections.plus
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    onDismissRequest: () -> Unit,
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    val sheetState = rememberModalBottomSheetState()

    val sheetCornerRadius by animateDpAsState(
        targetValue = if (sheetState.currentValue == SheetValue.Expanded) 0.dp else 32.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    ModalBottomSheet(
        onDismissRequest = {
            onDismissRequest()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = sheetCornerRadius, topEnd = sheetCornerRadius)
    ) {
        ReorderableQueueList(playerViewModel, navController)
    }
}

@Composable
fun ReorderableQueueList(
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    val hapticFeedback = LocalHapticFeedback.current

    val currentQueue = playerViewModel.playerQueue.collectAsStateWithLifecycle()
    val generatedQueue = playerViewModel.generatedQueue.collectAsStateWithLifecycle()

    var items by remember { mutableStateOf<List<Any>>(emptyList()) }

    val lazyListState = rememberLazyListState()
    var enableDivider by remember { mutableStateOf(false) }

    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        items = items.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        val dividerIndex = items.indexOf("DIVIDER")
        enableDivider = from.index > dividerIndex + 1
    }

    LaunchedEffect(currentQueue.value, generatedQueue.value, reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            println("REFRESHED")
            items = currentQueue.value +
                    "DIVIDER" +
                    generatedQueue.value
        }
    }

    val currentDividerIndex = items.indexOf("DIVIDER")

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> if (item == "DIVIDER") "DIVIDER" else (item as QueueItem).id }
        ) { index, item ->

            if (index == items.size - 1 && item == "DIVIDER") return@itemsIndexed
            if (item == "DIVIDER") {
                ReorderableItem(
                    state = reorderableState,
                    key = "DIVIDER",
                    enabled = enableDivider
                ) {
                    WavyDivider()
                }
            } else {
                item as QueueItem

                val isAboveDivider = index < currentDividerIndex

                ReorderableItem(
                    state = reorderableState,
                    key = item.id,
                    enabled = isAboveDivider
                ) { isDragging ->

                    val elevation by animateDpAsState(if (isDragging) 3.dp else 0.dp)

                    val transparentConditionalModifier = if (isAboveDivider) {
                        Modifier
                    } else {
                        //makes it greyscale-ish
                        val matrix = ColorMatrix().apply { setToSaturation(0.65f) }
                        Modifier.graphicsLayer {
                            alpha = 0.85f
                            colorFilter = ColorFilter.colorMatrix(matrix)
                        }
                    }

                    QueueTrackItem (
                        item,
                        playerViewModel,
                        index = index,
                        modifier = transparentConditionalModifier
                            .longPressDraggableHandle (
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    val isAboveNow = items.indexOf(item) < items.indexOf("DIVIDER")
                                    enableDivider = !isAboveNow
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    playerViewModel.decideReorderType(item, items)
                                    enableDivider = false
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
                                val isAboveNow = items.indexOf(item) < items.indexOf("DIVIDER")
                                enableDivider = !isAboveNow
                            },
                            onDragStopped = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                playerViewModel.decideReorderType(item, items)
                                enableDivider = false
                            }
                        ),
                        elevation = elevation,
                        count = currentQueue.value.size,
                        navController = navController,
                        onDismiss = {
                            val isAboveNow = items.indexOf(item) < items.indexOf("DIVIDER")
                            if (isAboveNow) {
                                playerViewModel.removeFromPlayerQueue(index)
                            } else {
                                playerViewModel.removeFromGeneratedQueue(item)
                            }

                        }
                    ) {
                        val isAboveNow = items.indexOf(item) < items.indexOf("DIVIDER")
                        if (isAboveNow) {
                            playerViewModel.skipInQueue(index)
                        } else {
                            playerViewModel.skipToGeneratedInQueue(item)
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
    onDismiss: () -> Unit,
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
        onDismiss = {onDismiss()},
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

@Composable
fun WavyDivider() {
    Row(
        Modifier.padding(vertical = 24.dp, horizontal = 24.dp)
            .fillMaxWidth().height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WavyLine(MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxHeight().weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_wand_stars),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 14.dp).size(20.dp)
        )
        Text(
            "Picked For You",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(end = 14.dp, start = 6.dp)
        )
        WavyLine(MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxHeight().weight(1f))
    }
}

@Composable
fun WavyLine(wavyColor: Color, modifier: Modifier = Modifier) {
    val path = Path()
    Canvas(
        modifier
    ) {
        path.reset()
        val waveLength = 22.dp.toPx()
        var x = 0f
        while (x <= size.width) {
            val relativeX = x / waveLength
            val y = size.height / 2f + sin((relativeX * 2 * PI.toFloat())) * 7f

            if (x == 0f) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            x += 4f
        }
        drawPath(path, wavyColor, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}