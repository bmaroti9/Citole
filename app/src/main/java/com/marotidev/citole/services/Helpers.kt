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

package com.marotidev.citole.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

fun durationToString(duration: Long) : String {
    return "%01d:%02d".format((duration / 1000) / 60, (duration / 1000) % 60)
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