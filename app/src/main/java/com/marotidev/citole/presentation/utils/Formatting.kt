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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.room.Index
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

fun durationToString(duration: Long) : String {
    return "%01d:%02d".format((duration / 1000) / 60, (duration / 1000) % 60)
}

fun calculateBorderRadiusForGridItem(index: Int, count: Int, columnCount: Int) : List<Dp> {
    val roundedCornerDp = 16.dp
    val flatCornerDp = 4.dp

    val columns = min(count, columnCount)
    val rows = ceil(count * 1.0 / columns).toInt()

    val isLeftEdge = index % columns == 0
    val isRightEdge = index % columns == columns - 1

    val isTopEdge = index.floorDiv(columns) == 0
    val isBottomEdge = index.floorDiv(columns) == rows - 1

    return listOf(
        if (isTopEdge && isLeftEdge) roundedCornerDp else flatCornerDp,
        if (isTopEdge && isRightEdge) roundedCornerDp else flatCornerDp,
        if (isBottomEdge && isRightEdge) roundedCornerDp else flatCornerDp,
        if (isBottomEdge && isLeftEdge) roundedCornerDp else flatCornerDp,
    )
}

@Composable
fun tintedPainter(id: Int, color: Color): Painter {
    val base = painterResource(id)
    val colorState = rememberUpdatedState(color)
    return remember(id) {
        object : Painter() {
            override val intrinsicSize = base.intrinsicSize
            override fun DrawScope.onDraw() {
                with(base) { draw(size, colorFilter = ColorFilter.tint(colorState.value)) }
            }
        }
    }
}

fun Modifier.verticalScrollbar(
    state: LazyListState,
    thumbColor: Color,
    barColor: Color,
): Modifier {
    return this.then(
        Modifier.drawWithContent {
            drawContent()

            val verticalPadding = 12.dp.toPx()
            val width = 6.dp.toPx()
            val thumbPadding = 4.dp.toPx()

            val layoutInfo = state.layoutInfo
            val contentPadding = layoutInfo.beforeContentPadding + layoutInfo.afterContentPadding
            val items = layoutInfo.visibleItemsInfo
            if (items.isEmpty()) return@drawWithContent

            val viewportHeight = layoutInfo.viewportSize.height.toFloat() - verticalPadding * 2
            val itemsCount = layoutInfo.totalItemsCount
            val visibleHeight = items.sumOf { it.size }
            val avgItemSize = visibleHeight / items.size
            val totalHeight = avgItemSize * itemsCount

            val scrollOffset = state.firstVisibleItemIndex * avgItemSize + state.firstVisibleItemScrollOffset

            val thumbHeight = 46.dp.toPx()
            val maxOffset = viewportHeight - thumbHeight
            val thumbOffsetY = (scrollOffset * 1f / (totalHeight - viewportHeight + contentPadding) * maxOffset)
                .coerceIn(0f, maxOffset)

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(size.width - width - 10.dp.toPx(), thumbOffsetY + verticalPadding),
                size = Size(width, thumbHeight),
                cornerRadius = CornerRadius(width / 2)
            )

            drawRoundRect(
                color = barColor,
                topLeft = Offset(size.width - width - 10.dp.toPx(), verticalPadding),
                size = Size(width, max(thumbOffsetY - thumbPadding, 0f)),
                cornerRadius = CornerRadius(width / 2)
            )

            drawRoundRect(
                color = barColor,
                topLeft = Offset(size.width - width - 10.dp.toPx(), thumbOffsetY + thumbHeight + verticalPadding + thumbPadding),
                size = Size(width, max(viewportHeight - verticalPadding - thumbPadding - thumbOffsetY - thumbHeight, 0f)),
                cornerRadius = CornerRadius(width / 2)
            )
        }
    )
}