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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marotidev.citole.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class Point(val x: Float = 0.5f, val y: Float = 0.5f, val r: Float = 1f, val shape: Int = 0)

fun getIntersectionPoints(A: Point, B: Point, newR: Float, seed: Random, shapeCount: Int): List<Point> {

    val k = A.r + newR + 0.05f
    val l = B.r + newR + 0.05f

    val dx = B.x - A.x
    val dy = B.y - A.y
    val d = sqrt(dx * dx + dy * dy)

    if (d > k + l || d < abs(k - l) || d == 0f) {
        return emptyList()
    }

    val a = (k * k - l * l + d * d) / (2 * d)
    val hSq = k * k - a * a
    val h = if (hSq < 0) 0f else sqrt(hSq)

    val x0 = A.x + (a * dx) / d
    val y0 = A.y + (a * dy) / d

    if (h == 0f) {
        return listOf(Point(x0, y0, newR, seed.nextInt(shapeCount)))
    }

    val p1 = Point(x0 + (h * dy) / d, y0 - (h * dx) / d, newR, seed.nextInt(shapeCount))
    val p2 = Point(x0 - (h * dy) / d, y0 + (h * dx) / d, newR, seed.nextInt(shapeCount))

    return listOf(p1, p2)
}

fun getDistance(A: Point, B: Point) : Float {
    return sqrt((A.x - B.x).pow(2) + (A.y - B.y).pow(2))
}

@Composable
fun ArtworkCollage(hash: Int, artworkUris: List<Uri?>) {

    if (artworkUris.isEmpty()) return
    val count = artworkUris.size

    val seed = Random(hash + 9) //3

    val shapeList = listOf(
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.VerySunny.toShape(),
        MaterialShapes.Cookie12Sided.toShape(),
        MaterialShapes.Square.toShape()
    )
    val shapeSizeScale = listOf(
        1f,
        1f,
        1f,
        1f,
        1f,
        1f,
        1f,
        0.9f
    )
    val sizeList = listOf(1f, 1f, 0.5f, 0.5f, 0.5f, 0.2f, 0.25f, 0.25f, 0.25f, 0.25f)

    //Generate a list of coordinates Using Wang's circle packing algorithm with the seed

    val points: MutableList<Point> = mutableListOf(Point(shape = seed.nextInt(shapeList.size)))

    if (count > 1) {

        val newR = sizeList.random(seed)
        val newA = seed.nextFloat() * 2 * 3.14159f
        val dis = points[0].r + newR + 0.05f
        points.add(Point(points[0].x + cos(newA) * dis, points[0].y + sin(newA) * dis, newR))

        var index = 0

        //start the packing
        loop@ while (index < 200 && points.size < min(count, 7)) {
            index++

            val newR = sizeList.random(seed)

            val randomTwo = mutableListOf(points.random(seed))
            while (randomTwo.size < 2) {
                val newPoint = points.random(seed)
                if (newPoint != randomTwo[0]) {
                    randomTwo.add(newPoint)
                }
            }

            val newPoints = getIntersectionPoints(randomTwo[0], randomTwo[1], newR, seed, shapeList.size)

            newPoints.forEach { newPoint ->
                var good = true
                points.forEach { oldPoint ->
                    if (oldPoint !in randomTwo && getDistance(newPoint, oldPoint) < newPoint.r + oldPoint.r + 0.05f) {
                        good = false
                    }
                }
                if (good) {
                    points.add(newPoint)
                    continue@loop
                }
            }
        }
    }

    var smallestX = 100f
    var largestX = -100f
    var smallestY = 100f
    var largestY = -100f

    points.forEach {
        smallestX = min(smallestX, it.x - it.r * shapeSizeScale[it.shape])
        largestX = max(largestX, it.x + it.r * shapeSizeScale[it.shape])
        smallestY = min(smallestY, it.y - it.r * shapeSizeScale[it.shape])
        largestY = max(largestY, it.y + it.r * shapeSizeScale[it.shape])
    }

    val globalScaler = 1f / max(largestX - smallestX, largestY - smallestY)

    val scaledClusterWidth = (largestX - smallestX) * globalScaler
    val scaledClusterHeight = (largestY - smallestY) * globalScaler

    val offsetX = (1f - scaledClusterWidth) / 2f
    val offsetY = (1f - scaledClusterHeight) / 2f

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val baseSizeDp = 100.dp
    val baseSizePx = with(LocalDensity.current) { baseSizeDp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged { containerSize = it }
    ) {
        if (containerSize != IntSize.Zero) {
            for (index in 0..min(points.size - 1, 8)) {
                val point = points[index]
                val rotate = if (count > 1) (seed.nextFloat() - 0.5f) * 145 else 0f

                val newX = ((point.x - smallestX) * globalScaler) + offsetX
                val newY = ((point.y - smallestY) * globalScaler) + offsetY
                val newScale = if (count > 1) (point.r * 2 * globalScaler * shapeSizeScale[point.shape])
                else 0.7f / shapeSizeScale[point.shape]
                Box(
                    modifier = Modifier
                        .size(baseSizeDp)
                        .graphicsLayer {
                            translationX = (newX * containerSize.width) - (baseSizePx / 2f)
                            translationY = (newY * containerSize.height) - (baseSizePx / 2f)
                            rotationZ = rotate
                            scaleX = (containerSize.width * newScale * shapeSizeScale[point.shape]) / baseSizePx
                            scaleY = (containerSize.height * newScale * shapeSizeScale[point.shape]) / baseSizePx
                        },
                ) {
                    AsyncImage(
                        model = artworkUris[index],
                        contentDescription = "Album artwork",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shapeList[point.shape])
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        error = tintedPainter(R.drawable.ic_citole_black, MaterialTheme.colorScheme.outline),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }

    }

}