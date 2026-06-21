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

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import coil.compose.AsyncImage
import com.marotidev.citole.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MorphingClipImage(
    uri: Uri,
    size: androidx.compose.ui.unit.Dp = 200.dp
) {
    val morph = remember {
        Morph(MaterialShapes.Circle, MaterialShapes.Cookie12Sided)
    }

    val cycleDuration = 4000

    val progress = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = cycleDuration
                    0f at 0
                    1f at 800 using FastOutSlowInEasing
                    1f at 2000
                    0f at 3200 using FastOutSlowInEasing
                    0f at cycleDuration
                }
            )
        }
        launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = keyframes {
                    durationMillis = cycleDuration
                    0f at 0
                    65f at 800 using FastOutLinearInEasing
                    262f at 2000 using LinearEasing
                    360f at 3200 using LinearOutSlowInEasing
                    360f at cycleDuration
                }
            )

        }
    }

    val clipShape = remember(progress.value, rotation.value) {
        RotatingMorphShape(morph, progress.value, rotation.value)
    }

    AsyncImage(
        model = uri,
        contentDescription = "Album Art",
        modifier = Modifier.size(size)
            .clip(clipShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        error = tintedPainter(
            R.drawable.ic_citole_black,
            MaterialTheme.colorScheme.outline
        ),
        contentScale = ContentScale.Crop
    )
}

private class RotatingMorphShape(
    private val morph: Morph,
    private val progress: Float,
    private val rotation: Float
) : Shape {

    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = morph.toComposePath(progress)

        matrix.resetToPivotedTransform(
            pivotX = 0.5f,
            pivotY = 0.5f,
            translationX = size.width / 2,
            translationY = size.height / 2,
            scaleX = size.width,
            scaleY = size.height,
            rotationZ = rotation,
        )

        path.transform(matrix)
        return Outline.Generic(path)
    }
}

private fun Morph.toComposePath(progress: Float): Path {
    val path = Path()
    var first = true
    forEachCubic(progress) { cubic ->
        if (first) {
            path.moveTo(cubic.anchor0X, cubic.anchor0Y)
            first = false
        }
        path.cubicTo(
            cubic.control0X, cubic.control0Y,
            cubic.control1X, cubic.control1Y,
            cubic.anchor1X, cubic.anchor1Y
        )
    }
    path.close()
    return path
}
