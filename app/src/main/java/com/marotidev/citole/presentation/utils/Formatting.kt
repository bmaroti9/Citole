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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
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

