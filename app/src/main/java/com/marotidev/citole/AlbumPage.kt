package com.marotidev.citole

import android.content.ContentUris
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage

@Composable
fun AlbumPageScreen(albumId: Long, libraryViewModel: LibraryViewModel) {
    val album: AudioHelper.AlbumData = libraryViewModel.findAlbumById(albumId)
        ?: return Box(modifier = Modifier.fillMaxSize()) {
            Text("Album not found", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Center))
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
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