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

package com.marotidev.citole

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.marotidev.citole.pages.AlbumListPage
import com.marotidev.citole.pages.AlbumDetailScreen
import com.marotidev.citole.pages.ArtistDetailScreen
import com.marotidev.citole.pages.ArtistListPage
import com.marotidev.citole.pages.TrackListPage
import com.marotidev.citole.ui.theme.DynamicAppTheme
import com.marotidev.citole.ui.theme.M3ExpressiveTransitions
import com.marotidev.citole.viewmodels.LibraryViewModel
import com.marotidev.citole.viewmodels.PlayerViewModel
import com.marotidev.citole.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current

            val systemDynamicPrimary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).primary
                else dynamicLightColorScheme(context).primary
            } else {
                MaterialTheme.colorScheme.primary
            }

            HomeSetup(systemDynamicPrimary = systemDynamicPrimary)
        }
    }
}

@Composable
fun HomeSetup(
    playerViewModel: PlayerViewModel = viewModel(),
    libraryViewModel: LibraryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    systemDynamicPrimary : Color
) {

    LaunchedEffect(systemDynamicPrimary) {
        playerViewModel.updateDefaultColor(systemDynamicPrimary)
    }

    val currentSeedColor by playerViewModel.themeColor.collectAsState()

    DynamicAppTheme(currentSeedColor) {
        CitoleScreen(playerViewModel, libraryViewModel, settingsViewModel)
    }
}

enum class Page {
    Tracks,
    Albums,
    Artists
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
            modifier = Modifier.padding(start = 4.dp, end = 4.dp))
    }
}

@Serializable
object TracksViewDestination

@Serializable
data class AlbumViewDestination(
    val albumId: Long,
)

@Serializable
data class ArtistViewDestination(
    val artistName: String,
)


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CitoleScreen(
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    settingsViewModel: SettingsViewModel
) {

    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var openAlertDialog by remember { mutableStateOf(false) }

    var selectedPage by remember { mutableStateOf(Page.Tracks) }

    val scope = rememberCoroutineScope()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch { libraryViewModel.loadTracks(context) }
        } else {
            Log.e("Permissions", "Permission Denied")
        }
    }

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, permission)
        if (check == PackageManager.PERMISSION_GRANTED) {
            libraryViewModel.loadTracks(context)
        } else {
            launcher.launch(permission)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen, //don't want the drawer to randomly open on different screens
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(30.dp))
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Tracks,
                    onSelected =  {
                        selectedPage = Page.Tracks
                        scope.launch {
                            delay(200)
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
                            delay(200)
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
                            delay(200)
                            drawerState.close()
                        }
                    },
                    iconId = R.drawable.ic_person,
                    text = "Artists"
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
                startDestination = TracksViewDestination,
                enterTransition = M3ExpressiveTransitions.enter,
                exitTransition = M3ExpressiveTransitions.exit,
                popEnterTransition = M3ExpressiveTransitions.popEnter,
                popExitTransition = M3ExpressiveTransitions.popExit
            ) {
                composable<TracksViewDestination> {
                    Scaffold(
                        topBar = {
                            FixedTopBar(
                                libraryViewModel,
                                playerViewModel,
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
                                Page.Tracks -> TrackListPage(libraryViewModel, playerViewModel, paddingValues, navController)
                                Page.Albums -> AlbumListPage(libraryViewModel, playerViewModel, paddingValues, navController)
                                Page.Artists -> ArtistListPage(libraryViewModel, playerViewModel, paddingValues, navController)
                            }
                        }
                    }
                }

                composable <AlbumViewDestination> { backStackEntry ->
                    val args = backStackEntry.toRoute<AlbumViewDestination>()
                    AlbumDetailScreen(args.albumId, libraryViewModel, playerViewModel, navController)
                }

                composable <ArtistViewDestination> { backStackEntry ->
                    val args = backStackEntry.toRoute<ArtistViewDestination>()
                    ArtistDetailScreen(args.artistName, libraryViewModel, playerViewModel, navController)
                }
            }
            CustomFloatingToolbar(playerViewModel, navController)
            if (openAlertDialog) {
                FilterDialog (
                    onDismissRequest = {openAlertDialog = false},
                    libraryViewModel
                )
            }
        }
    }
}