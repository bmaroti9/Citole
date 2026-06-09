package com.marotidev.citole.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.services.tintedPainter
import com.marotidev.citole.viewmodels.LibraryViewModel
import com.marotidev.citole.viewmodels.PlayerViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.ui.unit.IntSize
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

@Composable
fun ArtistListPage(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp)
    ) {
        itemsIndexed(
            libraryViewModel.filteredArtists,
            key = { index, artist -> artist.name }
        ) { index, album ->
            ArtistItem(
                album,
                playerViewModel,
                onClicked = {

                },
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                index,
                libraryViewModel.filteredArtists.count()
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: AudioService.ArtistData,
    playerViewModel: PlayerViewModel,
    onClicked: () -> Unit,
    modifier: Modifier,
    index: Int,
    count: Int
) {
    val checked = playerViewModel.currentlyPlaying?.artists?.contains(artist.name) ?: false

    val roundedCornerDp = 16.dp
    val flatCornerDp = 4.dp
    val topStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index == 0 || checked) {roundedCornerDp} else {flatCornerDp},)
    val topEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index == 1 || count == 1 || checked) {roundedCornerDp} else {flatCornerDp},)
    val bottomStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index >= count + (count % 2) - 2 && index % 2 == 0 || checked) {roundedCornerDp} else {flatCornerDp},)
    val bottomEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if ((index >= count + (count % 2) - 2 && index % 2 == 1) || count == 1 || checked) {roundedCornerDp} else {flatCornerDp},)

    val containerColor by animateColorAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (checked) {MaterialTheme.colorScheme.secondaryContainer}
        else { MaterialTheme.colorScheme.surface}
    )

    Card(
        shape = RoundedCornerShape(topStartShape, topEndShape, bottomEndShape, bottomStartShape),
        modifier = modifier.padding(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        onClick = {onClicked()},
    ) {
        Column(
            modifier = Modifier.padding(22.dp).aspectRatio(0.78f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ArtistCollage(artist.name, artist.appearsIn)
            Text(
                text = artist.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp, start = 1.dp)
            )
        }
    }
}

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
fun ArtistCollage(artistName: String, albums: List<AudioService.AlbumData>) {

    val hash = artistName.hashCode()
    val seed = Random(hash)

    val shapeList = listOf(
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.Circle.toShape(),
        MaterialShapes.VerySunny.toShape(),
        MaterialShapes.Cookie12Sided.toShape(),
        MaterialShapes.Arch.toShape(),
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
        0.85f,
        0.85f
    )
    val sizeList = listOf(1f, 0.6f, 0.3f)

    //Generate a list of coordinates Using Wang's circle packing algorithm with the seed

    val points: MutableList<Point> = mutableListOf(Point(shape = seed.nextInt(shapeList.size)))

    if (albums.size > 1) {

        val newR = sizeList.random(seed)
        val newA = seed.nextFloat() * 2 * 3.14159f
        val dis = points[0].r + newR + 0.05f
        points.add(Point(points[0].x + cos(newA) * dis, points[0].y + sin(newA) * dis, newR))

        var index = 0

        //start the packing
        loop@ while (index < 200 && points.size < min(albums.size, 10)) {
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
            for (index in 0..min(points.size - 1, albums.size - 1)) {
                val point = points[index]
                val album = albums[index]
                val rotate = if (albums.size > 1) (seed.nextFloat() - 0.5f) * 145 else 0f

                val newX = ((point.x - smallestX) * globalScaler) + offsetX
                val newY = ((point.y - smallestY) * globalScaler) + offsetY
                val newScale = if (albums.size > 1) (point.r * 2 * globalScaler) else 0.7f
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
                        model = album.artworkUri,
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