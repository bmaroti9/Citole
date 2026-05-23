package com.marotidev.citole

import android.content.ContentUris
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
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

    Scaffold(
        topBar =  {
            TopAppBar(
                //title = {Text(album.albumName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 4.dp))},
                title = {},
                contentPadding = PaddingValues(horizontal = 10.dp),
                navigationIcon = {
                    FilledIconButton(
                        onClick = {navController.popBackStack()},
                        shapes = IconButtonDefaults.shapes(
                            shape = CircleShape,
                            pressedShape = MaterialTheme.shapes.medium
                        ),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                        )
                    }
                }

            )
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AsyncImage(
                model = album.artworkUri,
                contentDescription = "Album Art",
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 20.dp)
                    .size(250.dp)
                    .clip(RoundedCornerShape(33.dp)),
                error = painterResource(R.drawable.ic_library),
                contentScale = ContentScale.Crop
            )

            Text(album.albumName, style = MaterialTheme.typography.headlineSmall,)
            Text(album.artist, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(30.dp))

            LazyColumn() {
                items(album.tracks) { track ->
                    AlbumTrackItem(track, index = 0)
                }
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
        Text("$index.")

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