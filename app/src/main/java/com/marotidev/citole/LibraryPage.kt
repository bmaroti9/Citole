package com.marotidev.citole

import android.content.ContentUris
import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType


class LibraryViewModel : ViewModel() {

    var showSongs by mutableStateOf(true)
    var showPodcasts by mutableStateOf(false)
    var showAudiobooks by mutableStateOf(false)
    var showOther by mutableStateOf(false)

    var selectedSortChip by mutableStateOf(SortChip.Name)

    var reverseSortOrder by mutableStateOf(false)

    fun onShowSongsChanged() {
        showSongs = !showSongs
        updateFilteredTracks()
    }

    fun onShowPodcastsChanged() {
        showPodcasts = !showPodcasts
        updateFilteredTracks()
    }

    fun onShowAudiobooksChanged() {
        showAudiobooks = !showAudiobooks
        updateFilteredTracks()
    }

    fun onShowOtherChanged() {
        showOther = !showOther
        updateFilteredTracks()
    }

    fun onSelectedSortChipChanged(to: SortChip) {
        selectedSortChip = to
        updateFilteredTracks()
    }

    fun onReverseSortOrderChanged(to: Boolean) {
        reverseSortOrder = to
        updateFilteredTracks()
    }

    var allTracks by mutableStateOf<List<AudioHelper.AudioData>>(emptyList())
        private set

    var filteredTracks by mutableStateOf<List<AudioHelper.AudioData>>(emptyList())
        private set

    var allAlbums by mutableStateOf<List<AudioHelper.AlbumData>>(emptyList())
        private set

    var filteredAlbums by mutableStateOf<List<AudioHelper.AlbumData>>(emptyList())
        private set

    var filteredArtists by mutableStateOf<List<AudioHelper.AlbumData>>(emptyList())
        private set


    var searchQuery by mutableStateOf("")
        private set


    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery

        updateFilteredTracks()
    }

    fun updateFilteredTracks() {
        filteredTracks = allTracks.filterTracksByQuery().filterTracksByFilterChips().sortTracksBySortChip().sortBySortOrder()

        filteredAlbums = filteredTracks.groupToAlbum()
    }

    fun List<AudioHelper.AudioData>.filterTracksByQuery() : List<AudioHelper.AudioData> {
        return this.filter { track ->
            track.name.contains(searchQuery, ignoreCase = true)
                    || track.artist.contains(searchQuery, ignoreCase = true)
                    || track.albumName.contains(searchQuery, ignoreCase = true)
        }
    }

    fun List<AudioHelper.AudioData>.sortTracksBySortChip() : List<AudioHelper.AudioData> {
        return this.sortedBy { track ->
            when (selectedSortChip) {
                SortChip.Name -> track.name
                SortChip.Album -> track.albumName
                SortChip.Artist -> track.artist
                SortChip.DateAdded -> track.dateAdded.toString()
            }
        }
    }

    fun List<AudioHelper.AudioData>.filterTracksByFilterChips() : List<AudioHelper.AudioData> {
        return this.filter { track ->
            when (track.type) {
                AudioType.Song -> showSongs
                AudioType.Podcast -> showPodcasts
                AudioType.Audiobook -> showAudiobooks
                AudioType.Other -> showOther
            }
        }
    }

    fun List<AudioHelper.AudioData>.sortBySortOrder() : List<AudioHelper.AudioData> {
        return when (reverseSortOrder) {
            true -> this.reversed()
            false -> this
        }
    }

    fun List<AudioHelper.AudioData>.groupToAlbum() : List<AudioHelper.AlbumData> {
        return this.groupBy { it.albumId }
            .map { (albumId, tracksInAlbum) ->
                val sequentialTracks = tracksInAlbum.sortedBy { it.trackNumber }
                AudioHelper.AlbumData(
                    albumId = albumId,
                    albumName = tracksInAlbum.firstOrNull()?.albumName ?: "Unknown Album",
                    artist = tracksInAlbum.firstOrNull()?.artist ?: "Unknown Artist",
                    tracks = sequentialTracks,
                    type = tracksInAlbum.firstOrNull()?.type ?: AudioType.Other,
                    artworkUri = tracksInAlbum.firstOrNull()?.artworkUri
                )
            }
    }

    fun findAlbumById(albumId: Long) : AudioHelper.AlbumData? {
        return allAlbums.find { it.albumId == albumId }
    }

    fun loadTracks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = AudioHelper.fetchAudioFiles(context)
            allTracks = tracks
            allAlbums = tracks.groupToAlbum()
            updateFilteredTracks()
        }
    }
}

@Composable
fun TracksPage(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 30.dp)
    ) {
        items(
            items = libraryViewModel.filteredTracks,
            key = { track -> track.uri }
        ) { track ->
            TrackItem (
                track = track,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                playerViewModel = playerViewModel
            ) {
                playerViewModel.addToQueue(track)
            }
        }
    }
}

@Composable
fun TrackItem(
    track: AudioHelper.AudioData,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isCurrentlyPlaying = playerViewModel.currentlyPlaying?.uri == track.uri

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = {
                onClicked()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            })
            .background(
                if (isCurrentlyPlaying) {MaterialTheme.colorScheme.secondaryContainer}
                else {Color.Transparent}
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp)),
            error = painterResource(R.drawable.ic_library),
            contentScale = ContentScale.Crop
        )

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

@Composable
fun AlbumsPage(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 30.dp)
    ) {
        items(
            libraryViewModel.filteredAlbums,
            key = { album -> album.albumId }
        ) { album ->
            AlbumItem(album,
                onClicked = {
                navController.navigate(AlbumViewDestination(albumId = album.albumId))
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
            )
        }
    }
}

@Composable
fun AlbumItem(album: AudioHelper.AlbumData, onClicked: () -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier
            .padding(10.dp)
            .clickable(
                onClick = {onClicked()}
            ),
    ) {
        AsyncImage(
            model = album.artworkUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp)),
            error = painterResource(R.drawable.ic_library),
            contentScale = ContentScale.Crop
        )
        Text(
            text = album.albumName,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 5.dp, start = 1.dp)
        )
    }
}


@Composable
fun CarouselExample_MultiBrowse() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val items = remember {
        listOf(
            CarouselItem(0, R.drawable.ic_album, "cupcake"),
            CarouselItem(1, R.drawable.ic_album, "donut"),
            CarouselItem(2, R.drawable.ic_album, "eclair"),
            CarouselItem(3, R.drawable.ic_album, "froyo"),
            CarouselItem(4, R.drawable.ic_album, "gingerbread"),
        )
    }

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),
        preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = items[i]
        Image(
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
            painter = painterResource(id = item.imageResId),
            contentDescription = item.contentDescription,
            contentScale = ContentScale.Crop
        )
    }
}