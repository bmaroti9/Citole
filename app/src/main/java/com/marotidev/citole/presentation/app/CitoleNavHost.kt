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

package com.marotidev.citole.presentation.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.marotidev.citole.R
import com.marotidev.citole.presentation.browse.BrowseViewModel
import com.marotidev.citole.presentation.browse.FilterDialog
import com.marotidev.citole.presentation.browse.FixedTopBar
import com.marotidev.citole.presentation.home.album.AlbumDetailScreen
import com.marotidev.citole.presentation.home.album.AlbumListPage
import com.marotidev.citole.presentation.home.artist.ArtistDetailScreen
import com.marotidev.citole.presentation.home.artist.ArtistListPage
import com.marotidev.citole.presentation.home.forYou.ForYouListPage
import com.marotidev.citole.presentation.home.track.TrackListPage
import com.marotidev.citole.presentation.onboard.OnboardScreen
import com.marotidev.citole.presentation.player.CustomFloatingToolbar
import com.marotidev.citole.presentation.player.PlayerViewModel
import com.marotidev.citole.presentation.settings.SettingsMainScreen
import com.marotidev.citole.presentation.settings.shuffleEngine.ShuffleEnginePage
import com.marotidev.citole.ui.theme.M3ExpressiveTransitions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

enum class Page {
    ForYou,
    Tracks,
    Albums,
    Artists,
}

@Composable
fun CustomNavigationDrawerItem(
    selected: Boolean,
    onSelected: () -> Unit,
    iconId: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 2.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = {onSelected()})
            .background(
                if (selected) {MaterialTheme.colorScheme.secondaryContainer}
                else {Color.Transparent}
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "Songs",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp, end = 4.dp))
    }
}

@Serializable
object LibraryViewDestination

@Serializable
data class AlbumViewDestination(
    val albumId: Long,
)

@Serializable
data class ArtistViewDestination(
    val artistName: String,
)

@Serializable
object OnboardViewDestination

@Serializable
object SettingsViewDestination


@Serializable
object SettingsShuffleEngineViewDestination


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CitoleNavHost(
    playerViewModel: PlayerViewModel,
    appViewModel: AppViewModel = hiltViewModel(),
    browseViewModel: BrowseViewModel = hiltViewModel()
) {

    val focusManager = LocalFocusManager.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var openAlertDialog by remember { mutableStateOf(false) }

    var selectedPage by remember { mutableStateOf(Page.ForYou) }

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, //don't want the drawer to randomly open on different screens
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(30.dp))
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.ForYou,
                    onSelected =  {
                        selectedPage = Page.ForYou
                        scope.launch {
                            delay(200.milliseconds)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_explore,
                    text = "For You"
                )
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Tracks,
                    onSelected =  {
                        selectedPage = Page.Tracks
                        scope.launch {
                            delay(200.milliseconds)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_graphic_eq,
                    text = "Tracks"
                )
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Albums,
                    onSelected =  {
                        selectedPage = Page.Albums
                        scope.launch {
                            delay(200.milliseconds)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_album,
                    text = "Albums"
                )
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Artists,
                    onSelected =  {
                        selectedPage = Page.Artists
                        scope.launch {
                            delay(200.milliseconds)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_person,
                    text = "Artists"
                )

                CustomNavigationDrawerItem(
                    selected = false,
                    onSelected =  {
                        navController.navigate(SettingsViewDestination)
                        scope.launch {
                            delay(200.milliseconds)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_settings,
                    text = "Settings"
                )
            }
        },
    ) {
        Box (
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
        ){

            NavHost(
                navController = navController,
                startDestination = appViewModel.startDestination,
                enterTransition = M3ExpressiveTransitions.enter,
                exitTransition = M3ExpressiveTransitions.exit,
                popEnterTransition = M3ExpressiveTransitions.popEnter,
                popExitTransition = M3ExpressiveTransitions.popExit
            ) {
                composable<LibraryViewDestination> {
                    Scaffold(
                        topBar = {
                            FixedTopBar(
                                onMenuClick = {
                                    scope.launch { drawerState.open() }
                                },
                                onPrimaryClick = {
                                    openAlertDialog = true
                                }
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.5.dp)
                    ) { paddingValues ->
                        AnimatedContent(
                            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
                            targetState = selectedPage,
                            label = "ScreenTransition",
                            transitionSpec = {
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                            }
                        ) { targetPage ->
                            when (targetPage) {
                                Page.ForYou -> ForYouListPage(playerViewModel, paddingValues, navController)
                                Page.Tracks -> TrackListPage(playerViewModel, browseViewModel, paddingValues, navController)
                                Page.Albums -> AlbumListPage(playerViewModel, paddingValues, navController)
                                Page.Artists -> ArtistListPage(playerViewModel, paddingValues, navController)
                            }
                        }
                    }
                }

                composable <AlbumViewDestination> {
                    AlbumDetailScreen(playerViewModel, navController)
                }

                composable <ArtistViewDestination> {
                    ArtistDetailScreen(playerViewModel, navController)
                }

                composable <OnboardViewDestination> {
                    OnboardScreen(navController)
                }

                composable <SettingsViewDestination> {
                    SettingsMainScreen(navController)
                }

                composable <SettingsShuffleEngineViewDestination> {
                    ShuffleEnginePage(navController)
                }
            }
            CustomFloatingToolbar(playerViewModel, navController)
            if (openAlertDialog) {
                FilterDialog(
                    onDismissRequest = { openAlertDialog = false },
                    browseViewModel
                )
            }
        }
    }
}