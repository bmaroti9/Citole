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

import android.util.Size
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.presentation.home.track.SwipeableTrackItem
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.tintedPainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.marotidev.citole.data.repository.RecommendationRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.presentation.utils.MorphingClipImage
import com.marotidev.citole.presentation.utils.durationToString
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ForYouListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    forYouViewModel: ForYouViewModel = hiltViewModel()
) {

    val recentlyAdded by forYouViewModel.recentlyAdded.collectAsStateWithLifecycle()
    val recentlyPlayed by forYouViewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val lastPodcast by forYouViewModel.lastPodcast.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 2.dp)
    ) {
        item {
            TrackCarousel(recentlyAdded, playerViewModel)
        }

        item {
            OfferResumePlayback(lastPodcast, playerViewModel)
        }

        item {
            Column() {
                Text("Recently Played", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
                recentlyPlayed.forEachIndexed { index, track ->
                    if (track != null) {
                        SwipeableTrackItem (
                            track = track,
                            playerViewModel = playerViewModel,
                            index = index,
                            count = recentlyPlayed.size,
                            navController = navController
                        ) {
                            playerViewModel.playQueue(listOf(track))
                        }
                    }

                }
            }
        }

    }
}

@Composable
fun TrackCarousel(tracks: List<AudioService.TrackData>, playerViewModel: PlayerViewModel) {

    AnimatedVisibility(
        visible = tracks.isNotEmpty(),
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    ) {
        val carouselState = rememberCarouselState { tracks.size }

        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(34.dp))
                .padding(12.dp)
        ) {
            Text("Recently Added", style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))

            HorizontalMultiBrowseCarousel(
                state = carouselState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                preferredItemWidth = 180.dp,
                flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(carouselState),
                itemSpacing = 6.dp,
            ) { i ->
                val track = tracks[i]

                val drawInfo = carouselItemDrawInfo

                Box(
                    modifier = Modifier
                        .height(205.dp)
                        .maskClip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable(
                            onClick = {
                                playerViewModel.playQueue(listOf(track))
                            }
                        )
                ) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        error = tintedPainter(
                            R.drawable.ic_citole_black,
                            MaterialTheme.colorScheme.outline
                        ),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                    startY = 250f
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val currentWidth = drawInfo.size
                                val maxWidth = drawInfo.maxSize
                                val fadeThreshold = maxWidth * 0.8f
                                alpha = if (maxWidth <= 0f || currentWidth < fadeThreshold) {
                                    0f
                                } else {
                                    ((currentWidth - fadeThreshold) / (maxWidth - fadeThreshold)).coerceIn(0f, 1f)
                                }
                            }
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium
                                    .copy(color = Color.White),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = track.artists.joinToString(", "),
                                style = MaterialTheme.typography.labelMedium
                                    .copy(color = Color.White, fontWeight = FontWeight.W700),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun OfferResumePlayback(
    trackWithPlaybackState: RecommendationRepository.TrackWithPlaybackState?,
    playerViewModel: PlayerViewModel
) {
    var targetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(trackWithPlaybackState) {
        if (trackWithPlaybackState != null) {
            delay(1000.milliseconds)
            targetVisible = true
        } else {
            targetVisible = false
        }
    }

    AnimatedVisibility(
        visible = targetVisible,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    ) {

        if (trackWithPlaybackState == null) {
            Spacer(modifier = Modifier)
        } else {
            val track = trackWithPlaybackState.track
            val log = trackWithPlaybackState.log

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {
                            playerViewModel.playQueue(listOf(track), startPosition = log.playbackDurationMs)
                        }
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    AsyncImage(
//                        model = track.artworkUri,
//                        contentDescription = "Album Art",
//                        modifier = Modifier.size(80.dp)
//                            .clip(CircleShape)
//                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
//                        error = tintedPainter(
//                            R.drawable.ic_citole_black,
//                            MaterialTheme.colorScheme.outline
//                        ),
//                        contentScale = ContentScale.Crop
//                    )
                    MorphingClipImage(track.artworkUri, 80.dp)
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text("Resume playback of", style = MaterialTheme.typography.labelSmall)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(3.dp)
                                    .background(MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Text(durationToString(log.playbackDurationMs), style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Text(track.title, style = MaterialTheme.typography.titleSmall)
                        Text(track.artists.joinToString(", "), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}