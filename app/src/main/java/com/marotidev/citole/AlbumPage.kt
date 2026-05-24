package com.marotidev.citole

import android.content.ContentUris
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun AlbumPageScreen(albumId: Long, libraryViewModel: LibraryViewModel, navController: NavController) {
    val album: AudioHelper.AlbumData = libraryViewModel.findAlbumById(albumId)
        ?: return Box(modifier = Modifier.fillMaxSize()) {
            Text("Album not found", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeight + with(density) { scrollBehavior.state.heightOffset.toDp() })
                    .background(MaterialTheme.colorScheme.surface)
            ) {

                Column(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth().graphicsLayer(alpha = (1f - collapsedFraction * 1.5f).coerceIn(0f, 1f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = album.artworkUri,
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = collapsedHeight + 20.dp, bottom = 30.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(33.dp)),
                        error = painterResource(R.drawable.ic_library),
                        contentScale = ContentScale.Crop
                    )
                    Text(album.albumName, style = MaterialTheme.typography.headlineSmall,)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(album.artist, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(collapsedHeight)
                        .align(Alignment.TopStart)
                        .padding(start = 70.dp, end = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = album.albumName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.graphicsLayer {
                            alpha = ((collapsedFraction - 0.7f) / 0.3f).coerceIn(0f, 1f)
                        }
                    )
                }

                FilledIconButton(
                    onClick = { navController.popBackStack() },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 12.dp, top = 8.dp)
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding // Crucial: passes down the structural top spacing!
        ) {
            items(50) { index ->
                Text(
                    text = "Track listing number $index",
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AlbumTrackItem(track: AudioHelper.AudioData, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = {

            })
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$index.", style = MaterialTheme.typography.labelLarge)

        Spacer(Modifier.width(12.dp))

        Column() {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingAppBarExample() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Collapsing Top Bar",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(100) { index ->
                Text(
                    text = "Item #$index",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}