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

package com.marotidev.citole.presentation.home.artist

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.marotidev.citole.R
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.home.album.AlbumItem
import com.marotidev.citole.presentation.home.track.SwipeableTrackItem
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.ArtworkCollage
import kotlin.math.min

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistDetailScreen(
    playerViewModel: PlayerViewModel,
    navController: NavController,
    artistDetailViewModel: ArtistDetailViewModel = hiltViewModel()
) {

    val artistState by artistDetailViewModel.artist.collectAsStateWithLifecycle()
    val artist = artistState ?: return Box(modifier = Modifier.fillMaxSize()) {
        Text(
            "Album not found",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val statusBarTopDp = statusBarPadding.calculateTopPadding()

    val density = LocalDensity.current
    val expandedHeight = 400.dp + statusBarTopDp
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

    var maxSongsToShow by remember { mutableIntStateOf(8) }

    val chunkedAlbums = artist.albums.chunked(2)
    val chunkedSingles = artist.singles.chunked(2)
    val chunkedAppearsIn = artist.appearsIn.chunked(2)

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
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = collapsedHeight + 20.dp)
                            .aspectRatio(1f)
                    ) {
                        ArtworkCollage(
                            hash = artist.name.hashCode(),
                            artworkUris = artist.allAlbums.map { it.artworkUri }
                        )
                    }
                    Text(artist.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 25.dp, bottom = 40.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(start = 76.dp, end = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = artist.name,
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = innerPadding
        ) {

            item {
                if (artist.tracks.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                            .padding(12.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            .clipToBounds(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
                        ) {
                            Text("Tracks", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    playerViewModel.playQueue(artist.tracks, 0)
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
                                onClick = {
                                    playerViewModel.playQueue(artist.tracks.shuffled(), 0)
                                },
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

                        artist.tracks.take(maxSongsToShow).forEachIndexed { index, track ->
                            SwipeableTrackItem(
                                track = track,
                                playerViewModel = playerViewModel,
                                index = index,
                                count = min(artist.tracks.count(), maxSongsToShow),
                                navController = navController
                            ) {
                                playerViewModel.playQueue(artist.tracks, index)
                            }
                        }

                        if (maxSongsToShow < artist.tracks.size) {
                            FilledTonalButton(
                                onClick = { maxSongsToShow = 1000 },
                                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 6.dp),
                                shapes = ButtonDefaults.shapes(
                                    shape = CircleShape,
                                    pressedShape = MaterialTheme.shapes.medium
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = "Show More",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Show More", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                        } else if (maxSongsToShow == 1000) {
                            FilledTonalButton(
                                onClick = { maxSongsToShow = 8 },
                                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 6.dp),
                                shapes = ButtonDefaults.shapes(
                                    shape = CircleShape,
                                    pressedShape = MaterialTheme.shapes.medium
                                ),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_up),
                                    contentDescription = "Show Less",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Show Less", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                        }
                    }
                }
            }

            item {
                if (artist.albums.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("Albums", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }

                        chunkedAlbums.forEachIndexed { rowIndex, rowAlbums ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.5.dp)
                            ) {
                                for (i in 0..<rowAlbums.count()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AlbumItem(
                                            album = rowAlbums[i],
                                            playerViewModel = playerViewModel,
                                            onClicked = {
                                                navController.navigate(AlbumViewDestination(albumId = rowAlbums[i].albumId))
                                            },
                                            index = rowIndex * 2 + i,
                                            count = artist.albums.count()
                                        )
                                    }
                                }

                                if (rowAlbums.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (artist.singles.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("Singles", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }

                        chunkedSingles.forEachIndexed { rowIndex, rowAlbums ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.5.dp)
                            ) {
                                for (i in 0..<rowAlbums.count()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AlbumItem(
                                            album = rowAlbums[i],
                                            playerViewModel = playerViewModel,
                                            onClicked = {
                                                navController.navigate(AlbumViewDestination(albumId = rowAlbums[i].albumId))
                                            },
                                            index = rowIndex * 2 + i,
                                            count = artist.singles.count()
                                        )
                                    }
                                }

                                if (rowAlbums.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (artist.appearsIn.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(28.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text("Appears In", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))
                        }

                        chunkedAppearsIn.forEachIndexed { rowIndex, rowAlbums ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(0.5.dp)
                            ) {
                                for (i in 0..<rowAlbums.count()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AlbumItem(
                                            album = rowAlbums[i],
                                            playerViewModel = playerViewModel,
                                            onClicked = {
                                                navController.navigate(AlbumViewDestination(albumId = rowAlbums[i].albumId))
                                            },
                                            index = rowIndex * 2 + i,
                                            count = artist.appearsIn.count()
                                        )
                                    }
                                }

                                if (rowAlbums.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}