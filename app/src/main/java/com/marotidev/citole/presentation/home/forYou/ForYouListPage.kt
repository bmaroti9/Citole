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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.data.repository.TrackLogRepository
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.presentation.home.track.SwipeableTrackItem
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.MorphingClipImage
import com.marotidev.citole.presentation.utils.SectionTitle
import com.marotidev.citole.presentation.utils.durationToString
import com.marotidev.citole.presentation.utils.tintedPainter

@Composable
fun ForYouListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    forYouViewModel: ForYouViewModel = hiltViewModel()
) {

    val showUniversalSearch by forYouViewModel.showUniversalSearch.collectAsStateWithLifecycle()

    val recentlyAdded by forYouViewModel.recentlyAdded.collectAsStateWithLifecycle()
    val recentlyPlayed by forYouViewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val mostPlayed by forYouViewModel.mostPlayed.collectAsStateWithLifecycle()
    //val lastPodcast by forYouViewModel.lastPodcast.collectAsStateWithLifecycle()
    val lastAudiobook by forYouViewModel.lastAudiobook.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = showUniversalSearch,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform { _, _ ->
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)
            }
        },
    ) { show ->
        if (show) {
            UniversalSearchScreen(paddingValues, playerViewModel, navController)
        } else {
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
                    OfferResumePlayback(forYouViewModel.resumePlaybackAnimationState,lastAudiobook, playerViewModel)
                }

                item {
                    RecentlyPlayedTracks(recentlyPlayed, playerViewModel, navController)
                }

                item {
                    MostPlayedTracks(mostPlayed, playerViewModel, navController)
                }

            }
        }
    }

}

@Composable
fun TrackCarousel(tracks: List<AudioService.TrackData>, playerViewModel: PlayerViewModel) {
    val haptic = LocalHapticFeedback.current

    val carouselState = rememberCarouselState { tracks.size }

    Column(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        SectionTitle("Recently Added")

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
                    .maskClip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(
                        onClick = {
                            playerViewModel.playQueue(listOf(track))
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
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

@Composable
fun OfferResumePlayback(
    animationState: Int,
    queueWithPlaybackState: TrackLogRepository.QueueWithPlaybackState?,
    playerViewModel: PlayerViewModel
) {

    AnimatedVisibility(
        visible = animationState > 0,
        enter = expandVertically(
            expandFrom = Alignment.Top,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    ) {

        if (queueWithPlaybackState?.tracks.isNullOrEmpty()) {
            Spacer(modifier = Modifier)
        } else {
            val track = queueWithPlaybackState.tracks[queueWithPlaybackState.queueIndex]

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        onClick = {
                            playerViewModel.playQueue(
                                queueWithPlaybackState.tracks,
                                startIndex = queueWithPlaybackState.queueIndex,
                                startPosition = queueWithPlaybackState.playbackDurationMs,
                                givenQueueId = queueWithPlaybackState.queueId
                            )
                        }
                    )
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorphingClipImage(track.artworkUri, 80.dp, runAnimation = animationState == 1)
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text("Resume playback of", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface)
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .size(3.dp)
                                    .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                            )
                            Text(durationToString(queueWithPlaybackState.playbackDurationMs), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Text(track.title, style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text(track.artists.joinToString(", "), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedTracks(
    recentlyPlayed: List<AudioService.TrackData>,
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    Column() {
        SectionTitle("Recently Played")
        recentlyPlayed.forEachIndexed { index, track ->
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

@Composable
fun MostPlayedTracks(
    mostPlayed: List<AudioService.TrackData>,
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    Column() {
        SectionTitle("Most Played")
        mostPlayed.forEachIndexed { index, track ->
            SwipeableTrackItem (
                track = track,
                playerViewModel = playerViewModel,
                index = index,
                count = mostPlayed.size,
                navController = navController
            ) {
                playerViewModel.playQueue(listOf(track))
            }
        }
    }
}