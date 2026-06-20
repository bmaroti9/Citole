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

package com.marotidev.citole.presentation.browse


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.marotidev.citole.R

enum class SortChip {
    Name,
    Album,
    Artist,
    DateAdded,
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilterDialog(
    onDismissRequest: () -> Unit,
    browseViewModel: BrowseViewModel
) {

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        icon = {
            Icon(
                painterResource(R.drawable.ic_page_info),
                contentDescription = null,
                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape).padding(8.dp)
            )
        },
        text = {
            Column() {
                Text("Filter", style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                FlowRow(
                    Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ToggleButton(
                        checked = browseViewModel.showSongs,
                        onCheckedChange = { browseViewModel.setChipShowSongs(it) },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_music), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Songs", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.showPodcasts,
                        onCheckedChange = { browseViewModel.setChipShowPodcasts(it) },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_podcast), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Podcasts", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.showAudiobooks,
                        onCheckedChange = { browseViewModel.setChipShowAudiobooks(it)},
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_book), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Audiobooks", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.showOther,
                        onCheckedChange = { browseViewModel.setChipShowOther(it) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_unknown_document), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Other", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text("Sort", style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 30.dp, bottom = 8.dp, start = 10.dp, end = 10.dp), color = MaterialTheme.colorScheme.onSurface)
                FlowRow(
                    Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ToggleButton(
                        checked = browseViewModel.selectedSortChip == SortChip.Name,
                        onCheckedChange = { browseViewModel.setSortChipSort(SortChip.Name) },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_sort_by_alpha), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Name", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.selectedSortChip == SortChip.Album,
                        onCheckedChange = { browseViewModel.setSortChipSort(SortChip.Album) },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_album), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Album", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.selectedSortChip == SortChip.Artist,
                        onCheckedChange = { browseViewModel.setSortChipSort(SortChip.Artist) },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_person), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Artist", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = browseViewModel.selectedSortChip == SortChip.DateAdded,
                        onCheckedChange = { browseViewModel.setSortChipSort(SortChip.DateAdded) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_today), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Date added", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text("Order", style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 30.dp, bottom = 8.dp, start = 10.dp, end = 10.dp), color = MaterialTheme.colorScheme.onSurface)
                FlowRow(
                    Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ToggleButton(
                        checked = !browseViewModel.reverseSortOrder,
                        onCheckedChange = { browseViewModel.onReverseSortOrderChanged(false) },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_down), contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    ToggleButton(
                        checked = browseViewModel.reverseSortOrder,
                        onCheckedChange = { browseViewModel.onReverseSortOrderChanged(true) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_arrow_up), contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {onDismissRequest()}
            ) {
                Text("Done", style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}