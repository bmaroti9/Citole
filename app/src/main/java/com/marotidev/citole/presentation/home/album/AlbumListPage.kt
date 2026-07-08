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

package com.marotidev.citole.presentation.home.album

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.presentation.app.AlbumViewDestination
import com.marotidev.citole.presentation.utils.tintedPainter
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.calculateBorderRadiusForGridItem

@Composable
fun AlbumListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    albumListViewModel: AlbumListViewModel = hiltViewModel()
) {
    val filteredAlbums by albumListViewModel.filteredAlbums.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp)
    ) {
        itemsIndexed(
            filteredAlbums,
            key = { _, album -> album.albumId }
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
                filteredAlbums.count()
            )
        }
    }
}

@Composable
fun AlbumItem(
    album: AudioService.AlbumData,
    playerViewModel: PlayerViewModel,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    columns: Int = 2,
) {
    val checked = playerViewModel.currentlyPlaying?.track?.albumId == album.albumId

    val corners = calculateBorderRadiusForGridItem(index, count, columns)

    val topStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = corners[0])
    val topEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = corners[1])
    val bottomEndShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = corners[2])
    val bottomStartShape by animateDpAsState(animationSpec = spring(stiffness = Spring.StiffnessMedium),
        targetValue = corners[3])

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
            modifier = Modifier.padding(22.dp)
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
            Column(modifier = Modifier.height(67.dp)) {
                Text(
                    text = album.albumName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 12.dp, start = 1.dp)
                )
                Text(
                    text = album.ownerArtists.joinToString(", "),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp, start = 1.dp)
                )
            }

        }
    }
}