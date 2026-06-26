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

package com.marotidev.citole.presentation.settings.subpages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.marotidev.citole.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShuffleEnginePage(
    navController: NavController,
) {
    var localSliderValue by remember { mutableFloatStateOf(0f) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.statusBarsPadding().height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { navController.popBackStack() },
                    shapes = IconButtonDefaults.shapes(
                        shape = CircleShape,
                        pressedShape = MaterialTheme.shapes.medium
                    ),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    modifier = Modifier.padding(start = 18.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                    )
                }

                Text(
                    text = "Shuffle Engine",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 50.dp),
        ) {
            SegmentedListItem(
                modifier = Modifier.padding(vertical = 1.dp),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
                onClick = {},
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                content = {
                    Text(
                        text = "Discovery Radius",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                supportingContent = {
                    Slider(
                        value = localSliderValue,
                        onValueChange = { localSliderValue = it },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(40.dp),
                                trackCornerSize = 12.dp,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        },
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_podcast),
                        contentDescription = "Palette",
                    )
                }
            )
        }
    }
}