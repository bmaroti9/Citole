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

package com.marotidev.citole.presentation.home.forYou

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.app.ArtistViewDestination
import com.marotidev.citole.presentation.home.album.AlbumItem
import com.marotidev.citole.presentation.home.artist.ArtistItem
import com.marotidev.citole.presentation.home.track.SwipeableTrackItem
import com.marotidev.citole.presentation.player.PlayerViewModel


@Composable
fun UniversalSearchScreen(
    paddingValues: PaddingValues,
    playerViewModel: PlayerViewModel,
    navController: NavController,
    universalSearchViewModel: UniversalSearchViewModel = hiltViewModel()
) {
    val searchResults by universalSearchViewModel.searchResults.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    LaunchedEffect(searchResults) {gridState.scrollToItem(0)}

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .clipToBounds(),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 2.dp),
        state = gridState
    ) {
        searchResults.forEach { result ->
            if (result.score > 0f) {
                when (result) {
                    is SearchResultGroup.Tracks -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "tracks_text_key") {
                            Text(
                                "Tracks",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(top = 24.dp, bottom = 12.dp, start = 8.dp)
                                    .animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                        fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                        placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                    ),
                            )
                        }

                        itemsIndexed(
                            items = result.items,
                            key = { _, scoredTrack-> "track_${scoredTrack.item.id}" },
                            span = { _, _ -> GridItemSpan(maxLineSpan) }
                        ) { index, scoredTrack ->
                            SwipeableTrackItem(
                                track = scoredTrack.item,
                                playerViewModel = playerViewModel,
                                index = index,
                                count = result.items.size,
                                navController = navController
                            ) {
                                playerViewModel.playQueue(listOf(result.items[index].item))
                            }
                        }
                    }

                    is SearchResultGroup.Albums -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "albums_text_key") {
                            Text(
                                "Albums",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(top = 24.dp, bottom = 12.dp, start = 8.dp)
                                    .animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                        fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                        placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                    ),
                            )
                        }

                        itemsIndexed(
                            items = result.items,
                            key = { _, scoredAlbum -> "album_${scoredAlbum.item.albumId}"},
                        ) { index, scoredAlbum ->
                            AlbumItem(
                                scoredAlbum.item,
                                playerViewModel,
                                onClicked = {
                                    navController.navigate(AlbumViewDestination(albumId = scoredAlbum.item.albumId))
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                ),
                                index,
                                result.items.size
                            )
                        }
                    }

                    is SearchResultGroup.Artists -> {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "artists_text_key") {
                            Text(
                                "Artists",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(top = 24.dp, bottom = 12.dp, start = 8.dp)
                                    .animateItem(
                                        fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                        fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                        placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                    ),
                            )
                        }

                        itemsIndexed(
                            items = result.items,
                            key = { _, scoredArtist -> "artist_${scoredArtist.item.name}" }
                        ) { index, scoredArtist ->
                            ArtistItem(
                                scoredArtist.item,
                                playerViewModel,
                                onClicked = {
                                    navController.navigate(ArtistViewDestination(artistName = scoredArtist.item.name))
                                },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                                ),
                                index,
                                result.items.size
                            )
                        }
                    }
                }
            }
        }
    }
}