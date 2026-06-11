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

package com.marotidev.citole.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.services.tintedPainter
import com.marotidev.citole.viewmodels.LibraryViewModel
import com.marotidev.citole.viewmodels.PlayerViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    val album: AudioService.AlbumData = libraryViewModel.findAlbumById(albumId)
        ?: return Box(modifier = Modifier.fillMaxSize()) {
            Text("Album not found", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
        }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val statusBarTopDp = statusBarPadding.calculateTopPadding()

    val density = LocalDensity.current
    val expandedHeight = 420.dp + statusBarTopDp
    val collapsedHeight = 64.dp + statusBarTopDp

    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }

    val totalCollapseRangePx = expandedHeightPx - collapsedHeightPx

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(
            initialHeightOffsetLimit = -totalCollapseRangePx
        )
    )

    val collapsedFraction = scrollBehavior.state.collapsedFraction

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight + with(density) { scrollBehavior.state.heightOffset.toDp() })
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(horizontal = 40.dp)
                        .graphicsLayer(alpha = (1f - collapsedFraction * 1.9f).coerceIn(0f, 1f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = album.artworkUri,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = collapsedHeight + 20.dp, bottom = 25.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(26.dp * (1f - collapsedFraction)))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        error = tintedPainter(R.drawable.ic_citole_black, MaterialTheme.colorScheme.outline),
                        contentScale = ContentScale.Crop
                    )
                    Text(album.albumName, style = MaterialTheme.typography.headlineSmall,)
                    Text(album.ownerArtists.joinToString(", "), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 3.dp, bottom = 40.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(start = 76.dp, end = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = album.albumName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer {
                            alpha = ((collapsedFraction - 0.65f) / 0.35f).coerceIn(0f, 1f)
                        }
                    )
                }

                FilledIconButton(
                    onClick = { navController.popBackStack() },
                    shapes = IconButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = MaterialTheme.shapes.medium
                    ),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 18.dp, top = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
            contentPadding = innerPadding
        ) {
            item {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text("Tracks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                playerViewModel.playQueue(album.tracks, 0, false)
                            },
                            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
                            shapes = ButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = MaterialTheme.shapes.medium
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = "Play",
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        FilledTonalIconButton(
                            onClick = {},
                            shapes = IconButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = MaterialTheme.shapes.medium
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shuffle),
                                contentDescription = "Shuffle",
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    album.tracks.forEachIndexed { index, track ->
                        SwipeableTrackItem (
                            track = track,
                            playerViewModel = playerViewModel,
                            index = index,
                            count = album.tracks.count(),
                            navController = navController
                        ) {
                            playerViewModel.playQueue(album.tracks, index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumTrackItem(
    playerViewModel: PlayerViewModel,
    tracks: List<AudioService.AudioData>,
    index: Int,
) {
    val track = tracks[index]
    val isCurrentlyPlaying = playerViewModel.currentlyPlaying?.id == track.id

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = {
                playerViewModel.playQueue(tracks, index)
            })
            .background(if (isCurrentlyPlaying) { MaterialTheme.colorScheme.secondaryContainer} else {Color.Transparent})
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${index + 1}.", style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(32.dp), color = MaterialTheme.colorScheme.secondary)


        Column() {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = track.artists.joinToString(", "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}