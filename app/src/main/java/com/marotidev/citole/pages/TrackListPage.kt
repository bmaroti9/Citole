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

package com.marotidev.citole.pages

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marotidev.citole.R
import com.marotidev.citole.services.AudioService
import com.marotidev.citole.services.durationToString
import com.marotidev.citole.viewmodels.LibraryViewModel
import com.marotidev.citole.viewmodels.PlayerViewModel

@Composable
fun TrackListPage(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .clipToBounds(), //supposedly should stop them bleeding under the search bar when animating
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 72.dp, top = 3.dp)
    ) {
        itemsIndexed(
            items = libraryViewModel.filteredTracks,
            key = { index, track -> track.id }
        ) { index, track ->
            TrackItem (
                track = track,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMedium),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
                    placementSpec = spring(stiffness = Spring.StiffnessMedium)
                ),
                playerViewModel = playerViewModel,
                index = index,
                count = libraryViewModel.filteredTracks.size
            ) {
                playerViewModel.playQueue(listOf(track))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackItem(
    track: AudioService.AudioData,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    index: Int,
    count: Int,
    elevation: Dp = 0.dp,
    dragHandle: (@Composable () -> Unit)? = null,
    onClicked: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val checked = playerViewModel.currentlyPlaying?.id == track.id
    var popupExpanded by remember { mutableStateOf(false) }

    SegmentedListItem(
        modifier = modifier.padding(vertical = 1.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
        checked = checked,
        onCheckedChange = {
            onClicked()
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        shapes = if (count == 1) {
            ListItemDefaults.shapes(shape = MaterialTheme.shapes.large)
        } else {
            ListItemDefaults.segmentedShapes(index = index, count = count)
        },
        elevation = ListItemElevation(draggedElevation = 0.dp, elevation = elevation),
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                dragHandle?.invoke()
                AsyncImage(
                    model = track.artworkUri,
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    error = painterResource(R.drawable.ic_library),
                    contentScale = ContentScale.Crop
                )
            }

        },
        content = {
            Text(
                text = track.title,
                lineHeight = 32.sp,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        supportingContent = {
            FlowRow (
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    lineHeight = 32.sp,
                    text = track.artist,
                    style = MaterialTheme.typography.labelSmall,
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(MaterialTheme.colorScheme.outline, CircleShape)
                )
                Text(
                    lineHeight = 32.sp,
                    text = durationToString(track.duration),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

        },
        trailingContent = {

            FilledTonalIconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (checked) { MaterialTheme.colorScheme.secondary}
                    else { MaterialTheme.colorScheme.secondaryContainer},
                    contentColor = if (checked) { MaterialTheme.colorScheme.onSecondary}
                    else { MaterialTheme.colorScheme.onSecondaryContainer},
                ),
                onClick = {popupExpanded = true },
                modifier = Modifier.size(26.dp, 30.dp),
                shapes = IconButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.small
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
                    contentDescription = "Options",
                    modifier = Modifier.size(16.dp)
                )
            }

            TrackOptionsPopup(
                expanded = popupExpanded,
                onDismiss = {popupExpanded = false},
                playerViewModel,
                track
            )
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackOptionsPopup(
    expanded: Boolean,
    onDismiss: () -> Unit,
    playerViewModel: PlayerViewModel,
    track: AudioService.AudioData
) {
    val groupInteractionSource = remember { MutableInteractionSource() }

    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 0, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            DropdownMenuItem(
                text = { Text("Play Next") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_play), null,
                        modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                onClick = {playerViewModel.addToQueue(track, playerViewModel.currentIndex + 1); onDismiss()}
            )
            DropdownMenuItem(
                text = { Text("Add to queue") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_add), null,
                        modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                onClick = {playerViewModel.addToQueue(track); onDismiss() }
            )
        }

        Spacer(Modifier.height(3.dp))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 1, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            DropdownMenuItem(
                text = { Text("Go to Artist") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_person), null,
                        modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Go to Album") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_album), null,
                        modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                onClick = { }
            )
        }

        Spacer(Modifier.height(3.dp))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(index = 2, count = 3),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            interactionSource = groupInteractionSource,
        ) {
            DropdownMenuItem(
                text = { Text("About") },
                trailingIcon = {
                    Icon(painterResource(R.drawable.ic_info), null,
                        modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                onClick = { }
            )
        }
    }
}