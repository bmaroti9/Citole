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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.tintedPainter
import kotlin.collections.get

@Composable
fun ForYouListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
) {

    val recentlyAdded = libraryViewModel.allTracks.sortedByDescending { it.dateAdded }.take(14)
    val carouselState = rememberCarouselState { recentlyAdded.size }

    LazyColumn(
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 24.dp)
    ) {
        item {
            Column() {
                Text("Recently Added", style = MaterialTheme.typography.titleSmall)
                HorizontalMultiBrowseCarousel(
                    state = carouselState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = 12.dp, bottom = 16.dp),
                    preferredItemWidth = 180.dp,
                    flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(carouselState),
                    itemSpacing = 8.dp,
                ) { i ->
                    val track = recentlyAdded[i]
                    Column() {
                        AsyncImage(
                            model = track.artworkUri,
                            contentDescription = "Album Art",
                            modifier = Modifier
                                .height(205.dp)
                                .maskClip(MaterialTheme.shapes.extraLarge)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            error = tintedPainter(
                                R.drawable.ic_citole_black,
                                MaterialTheme.colorScheme.outline
                            ),
                            contentScale = ContentScale.Crop
                        )
                        Text(track.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }

        }

    }
}