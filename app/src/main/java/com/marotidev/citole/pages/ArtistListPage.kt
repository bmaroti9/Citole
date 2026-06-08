package com.marotidev.citole.pages

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.AlbumViewDestination
import com.marotidev.citole.R
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.services.tintedPainter
import com.marotidev.citole.viewmodels.LibraryViewModel
import com.marotidev.citole.viewmodels.PlayerViewModel

@Composable
fun ArtistListPage(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp)
    ) {
        itemsIndexed(
            libraryViewModel.filteredAlbums,
            key = { index, album -> album.albumId }
        ) { index, album ->
            AlbumItem(
                album,
                playerViewModel,
                onClicked = {
                    navController.navigate(AlbumViewDestination(albumId = album.albumId))
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                index,
                libraryViewModel.filteredAlbums.count()
            )
        }
    }
}

@Composable
fun ArtistItem(
    album: AudioService.AlbumData,
    playerViewModel: PlayerViewModel,
    onClicked: () -> Unit,
    modifier: Modifier,
    index: Int,
    count: Int
) {
    val checked = playerViewModel.currentlyPlaying?.albumId == album.albumId

    val roundedCornerDp = 16.dp
    val flatCornerDp = 4.dp
    val topStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index == 0 || checked) {roundedCornerDp} else {flatCornerDp},)
    val topEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index == 1 || count == 1 || checked) {roundedCornerDp} else {flatCornerDp},)
    val bottomStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (index >= count + (count % 2) - 2 && index % 2 == 0 || checked) {roundedCornerDp} else {flatCornerDp},)
    val bottomEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if ((index >= count + (count % 2) - 2 && index % 2 == 1) || count == 1 || checked) {roundedCornerDp} else {flatCornerDp},)

    val containerColor by animateColorAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = if (checked) {MaterialTheme.colorScheme.secondaryContainer}
        else { MaterialTheme.colorScheme.surface}
    )

    Card(
        shape = RoundedCornerShape(topStartShape, topEndShape, bottomEndShape, bottomStartShape),
        modifier = modifier.padding(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        onClick = {onClicked()},
    ) {
        Column(
            modifier = Modifier.padding(22.dp).aspectRatio(0.685f)
        ) {
            AsyncImage(
                model = album.artworkUri,
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                error = tintedPainter(R.drawable.ic_citole_black, MaterialTheme.colorScheme.outline),
                contentScale = ContentScale.Crop
            )
            Text(
                text = album.albumName,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 12.dp, start = 1.dp)
            )
            Text(
                text = album.artists.joinToString(", "),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp, start = 1.dp)
            )
        }
    }
}