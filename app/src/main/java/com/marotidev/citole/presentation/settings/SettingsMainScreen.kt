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

package com.marotidev.citole.presentation.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.marotidev.citole.R
import com.marotidev.citole.presentation.app.SettingsShuffleEngineViewDestination
import com.marotidev.citole.presentation.app.SettingsViewDestination
import com.materialkolor.ktx.harmonize

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsMainScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {

    val appearanceIconContainer = Color(0xFF3FDAEE).harmonize(other = MaterialTheme.colorScheme.primaryContainer, true)
    val appearanceIconContent = Color(0xFF3FDAEE).harmonize(other = MaterialTheme.colorScheme.primary, true)

    val shuffleEngineIconContainer = Color(0xFF85B7FA).harmonize(other = MaterialTheme.colorScheme.primaryContainer, true)
    val shuffleEngineIconContent = Color(0xFF85B7FA).harmonize(other = MaterialTheme.colorScheme.primary, true)

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
                    text = "Settings",
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
                        text = "Appearance",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                supportingContent = {
                    Text(
                        text = "Color theme, Palette behaviour",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_palette),
                        contentDescription = "Palette",
                        modifier = Modifier.background(appearanceIconContainer, CircleShape).padding(10.dp),
                        tint = appearanceIconContent
                    )
                }
            )
            SegmentedListItem(
                modifier = Modifier.padding(vertical = 1.dp),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 14.dp),
                onClick = {
                    navController.navigate(SettingsShuffleEngineViewDestination)
                },
                shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                content = {
                    Text(
                        text = "Shuffle Engine",
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
                supportingContent = {
                    Text(
                        text = "Discovery radius, Anchor mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_graph),
                        contentDescription = "Engine",
                        modifier = Modifier.background(shuffleEngineIconContainer, CircleShape).padding(10.dp),
                        tint = shuffleEngineIconContent
                    )
                }
            )
        }
    }
}