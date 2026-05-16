package com.marotidev.citole

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import com.materialkolor.ktx.themeColor

class LibraryViewModel : ViewModel() {

    var allSongs by mutableStateOf<List<AudioHelper.AudioData>>(emptyList())
        private set

    var artists by mutableStateOf<Map<String, List<AudioHelper.AudioData>>>(emptyMap())
        private set

    var albums by mutableStateOf<Map<String, List<AudioHelper.AudioData>>>(emptyMap())
        private set

    var filteredSongs by mutableStateOf<List<AudioHelper.AudioData>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery = newQuery

        filteredSongs = allSongs.filter { song ->
                song.name.contains(newQuery, ignoreCase = true) ||
                        song.artist.contains(newQuery, ignoreCase = true)
            }
    }

    fun loadSongs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val songs = AudioHelper.fetchAudioFiles(context)
            allSongs = songs
            filteredSongs = songs
            artists = songs.groupBy { it.artist }
            albums = songs.groupBy { it.albumName }
        }
    }
}

@Composable
fun SongsPage(
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
            items = libraryViewModel.filteredSongs,
            key = { song -> song.uri }
        ) { song ->
            SongItem(
                song = song,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                playerViewModel = playerViewModel
            ) {
                playerViewModel.addToQueue(song)
            }
        }
    }
}

@Composable
fun SongItem(
    song: AudioHelper.AudioData,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isSelected = playerViewModel.currentlyPlaying?.uri == song.uri

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
                if (isSelected) {MaterialTheme.colorScheme.secondaryContainer}
                else {Color.Transparent}
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val artworkUri = ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            song.albumId
        )

        AsyncImage(
            model = artworkUri,
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
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = song.artist,
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
    paddingValues: PaddingValues
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 30.dp)
    ) {
        val albumList = libraryViewModel.albums.keys.sorted()

        items(albumList) { albumName ->
            val songList : List<AudioHelper.AudioData>? = libraryViewModel.albums[albumName]
            AlbumItem(songList)
        }
    }
}

@Composable
fun AlbumItem(songs: List<AudioHelper.AudioData>?) {
    if (songs.isNullOrEmpty()) return
    val firstSong = songs.first()
    val albumName = firstSong.albumName

    val artworkUri = ContentUris.withAppendedId(
        "content://media/external/audio/albumart".toUri(),
        firstSong.albumId
    )

    Column(
        modifier = Modifier.padding(10.dp),
    ) {
        AsyncImage(
            model = artworkUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp)),
            error = painterResource(R.drawable.ic_library),
            contentScale = ContentScale.Crop
        )
        Text(
            text = albumName,
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