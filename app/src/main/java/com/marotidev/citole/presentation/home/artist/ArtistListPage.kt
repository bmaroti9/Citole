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

package com.marotidev.citole.presentation.home.artist

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.marotidev.citole.data.service.AudioService
import com.marotidev.citole.presentation.app.ArtistViewDestination
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.utils.ArtworkCollage
import com.marotidev.citole.presentation.utils.calculateBorderRadiusForGridItem

@Composable
fun ArtistListPage(
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues,
    navController: NavController,
    artistListViewModel: ArtistListViewModel = hiltViewModel()
) {
    val filteredArtists by artistListViewModel.filteredArtists.collectAsStateWithLifecycle()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp)
    ) {
        itemsIndexed(
            filteredArtists,
            key = { _, artist -> artist.name }
        ) { index, artist ->
            ArtistItem(
                artist,
                playerViewModel,
                onClicked = {
                    navController.navigate(ArtistViewDestination(artistName = artist.name))
                },
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                index,
                filteredArtists.count()
            )
        }
    }
}

@Composable
fun ArtistItem(
    artist: AudioService.ArtistData,
    playerViewModel: PlayerViewModel,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    columns : Int = 2
) {
    val currentlyPlaying = playerViewModel.currentlyPlaying.collectAsStateWithLifecycle()
    val checked = currentlyPlaying.value?.track?.artists?.contains(artist.name) ?: false

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
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.padding(bottom = 14.dp, start = 12.dp, end = 12.dp).align(Alignment.CenterHorizontally)
            ) {
                ArtworkCollage(
                    hash = artist.name.hashCode(),
                    artworkUris = artist.allAlbums.map { it.artworkUri ?: Uri.EMPTY }
                )
            }
            Text(
                text = artist.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.height(36.dp)
            )
        }
    }
}