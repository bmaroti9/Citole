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

package com.marotidev.citole.presentation.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.marotidev.citole.R
import com.materialkolor.ktx.darken
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun DraggableScrollbar(
    listState: LazyListState,
    itemCount: Int,
    labelForIndex: (Int) -> String,
    modifier: Modifier = Modifier,
) {

    val density = LocalDensity.current
    val thumbWidthPx = with(density) { 6.dp.toPx() }
    val thumbExpandedWidthPx = with(density) {26.dp.toPx()}
    val thumbHeightPx = with(density) {46.dp.toPx()}
    val verticalPaddingPx = with(density) {12.dp.toPx()}
    val thumbPaddingPx = with(density) {4.dp.toPx()}

    val haptic = LocalHapticFeedback.current
    //val scope = rememberCoroutineScope()

    var isDragging by remember { mutableStateOf(false) }
    var viewportHeightPx by remember { mutableFloatStateOf(0f) }
    var thumbOffsetY by remember { mutableFloatStateOf(0f) }
    var maxScrollableHeightPx by remember { mutableFloatStateOf(0f) }
    var maxVisibleScrollableCount by remember { mutableFloatStateOf(0f) }

    val thumbAnimatedWidthPx by animateFloatAsState(
        targetValue = if (isDragging) thumbExpandedWidthPx else thumbWidthPx,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val thumbPath = remember { Path() }

    val iconPainter = painterResource(id = R.drawable.ic_unfold_more)
    val iconAlpha by animateFloatAsState(
        targetValue = if (isDragging) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ScrollbarIconAlpha"
    )

    var currentLabel by remember {mutableStateOf("")}

    var scrollTargetIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(scrollTargetIndex) {
        if (scrollTargetIndex >= 0) {
            listState.scrollToItem(scrollTargetIndex)
        }
    }

    val thumbColor = MaterialTheme.colorScheme.tertiaryContainer.darken(1.25f)
    val barColor = MaterialTheme.colorScheme.surfaceContainerHigh

    LaunchedEffect(listState, isDragging) {
        //listening, updating based on the regular scroll
        if (!isDragging) {
            snapshotFlow { Pair(listState.layoutInfo, viewportHeightPx) }
                .collect { (layoutInfo, currentHeightPx) ->

                    val items = layoutInfo.visibleItemsInfo
                    if (items.isNotEmpty() && currentHeightPx > 0f) {

                        val contentPadding = layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding

                        val itemsCount = layoutInfo.totalItemsCount
                        val avgItemSize = items.sumOf { it.size } / items.size.toFloat()
                        val totalHeight = avgItemSize * itemsCount

                        maxScrollableHeightPx = totalHeight - currentHeightPx + contentPadding
                        maxVisibleScrollableCount = maxScrollableHeightPx / avgItemSize

                        val scrollOffset = listState.firstVisibleItemIndex * avgItemSize + listState.firstVisibleItemScrollOffset

                        val maxOffset = currentHeightPx - thumbHeightPx
                        thumbOffsetY = (scrollOffset * 1f / maxScrollableHeightPx * maxOffset)
                            .coerceIn(0f, maxOffset)
                    }
                }
        }
    }

    LaunchedEffect(currentLabel) {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    Box(
        modifier = modifier
            .onSizeChanged { viewportHeightPx = it.height.toFloat() }
            .pointerInput(itemCount) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { change, dragAmount ->
                        if (!isDragging) return@detectDragGestures
                        change.consume()

                        val maxOffset = viewportHeightPx - thumbHeightPx - verticalPaddingPx * 2f

                        thumbOffsetY = (thumbOffsetY + dragAmount.y).coerceIn(0f, maxOffset)

                        val fraction = thumbOffsetY / maxOffset

                        //val scrollDeltaPx = dragAmount.y / maxOffset * maxScrollableHeightPx
                        //listState.dispatchRawDelta(scrollDeltaPx)

                        scrollTargetIndex = (fraction * (maxVisibleScrollableCount)).roundToInt().coerceIn(0, maxVisibleScrollableCount.roundToInt())

                        val labelTargetIndex = (fraction * (itemCount - 1)).roundToInt().coerceIn(0, itemCount - 1)
                        currentLabel = labelForIndex(labelTargetIndex)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            thumbPath.reset()
            thumbPath.addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(size.width - 10.dp.toPx() - thumbAnimatedWidthPx,
                            thumbOffsetY + verticalPaddingPx),
                        size = Size(thumbAnimatedWidthPx, thumbHeightPx),
                    ),
                    topLeft = CornerRadius(thumbAnimatedWidthPx / 2),
                    topRight = CornerRadius(thumbWidthPx / 2),
                    bottomRight = CornerRadius(thumbWidthPx / 2),
                    bottomLeft = CornerRadius(thumbAnimatedWidthPx / 2)
                )
            )

            drawPath(
                path = thumbPath,
                color = thumbColor
            )

            drawRoundRect(
                color = barColor,
                topLeft = Offset(size.width - thumbWidthPx - 10.dp.toPx(), verticalPaddingPx),
                size = Size(thumbWidthPx, max(thumbOffsetY - thumbPaddingPx, 0f)),
                cornerRadius = CornerRadius(thumbWidthPx / 2)
            )

            drawRoundRect(
                color = barColor,
                topLeft = Offset(size.width - thumbWidthPx - 10.dp.toPx(), thumbOffsetY + thumbHeightPx + verticalPaddingPx + thumbPaddingPx),
                size = Size(thumbWidthPx, max(viewportHeightPx - verticalPaddingPx * 2 - thumbPaddingPx - thumbOffsetY - thumbHeightPx, 0f)),
                cornerRadius = CornerRadius(thumbWidthPx / 2)
            )

            if (iconAlpha > 0f) {
                val iconSizePx = 18.dp.toPx()

                val iconX = size.width - 10.dp.toPx() - thumbAnimatedWidthPx / 2f - iconSizePx / 2f
                val iconY = thumbOffsetY + verticalPaddingPx + (thumbHeightPx - iconSizePx) / 2f

                translate(left = iconX, top = iconY) {
                    with(iconPainter) {
                        draw(
                            size = Size(iconSizePx, iconSizePx),
                            alpha = iconAlpha,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(1f, 0.5f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(1f, 0.5f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .wrapContentSize(unbounded = true)
                .graphicsLayer {
                    translationX = -(thumbWidthPx + 64.dp.toPx())
                    translationY = thumbOffsetY + (thumbHeightPx / 2f) - (size.height / 2f) + verticalPaddingPx
                }
        ) {
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.inverseSurface, MaterialTheme.shapes.large)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}