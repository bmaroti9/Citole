package com.marotidev.citole


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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
    libraryViewModel: LibraryViewModel
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
                Text("Filter", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface)
                FlowRow(
                    Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ToggleButton(
                        checked = libraryViewModel.showSongs,
                        onCheckedChange = { libraryViewModel.onShowSongsChanged() },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_music), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Songs", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.showPodcasts,
                        onCheckedChange = { libraryViewModel.onShowPodcastsChanged() },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_podcast), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Podcasts", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.showAudiobooks,
                        onCheckedChange = { libraryViewModel.onShowAudiobooksChanged() },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_book), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Audiobooks", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.showOther,
                        onCheckedChange = { libraryViewModel.onShowOtherChanged() },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_unknown_document), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Other", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text("Sort by", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 30.dp, bottom = 8.dp, start = 10.dp, end = 10.dp), color = MaterialTheme.colorScheme.onSurface)
                FlowRow(
                    Modifier.padding(horizontal = 8.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    ToggleButton(
                        checked = libraryViewModel.selectedSortChip == SortChip.Name,
                        onCheckedChange = { libraryViewModel.onSelectedSortChipChanged(SortChip.Name) },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_sort_by_alpha), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Name", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.selectedSortChip == SortChip.Album,
                        onCheckedChange = { libraryViewModel.onSelectedSortChipChanged(SortChip.Album) },
                        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_album), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Album", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.selectedSortChip == SortChip.Artist,
                        onCheckedChange = { libraryViewModel.onSelectedSortChipChanged(SortChip.Artist) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_person), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Artist", style = MaterialTheme.typography.labelSmall)
                    }
                    ToggleButton(
                        checked = libraryViewModel.selectedSortChip == SortChip.DateAdded,
                        onCheckedChange = { libraryViewModel.onSelectedSortChipChanged(SortChip.DateAdded) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        colors = ToggleButtonDefaults.toggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(painterResource(R.drawable.ic_today), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text("Date added", style = MaterialTheme.typography.labelSmall)
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