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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.content.ContextCompat
import com.marotidev.citole.ui.theme.DynamicAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            val playerViewModel = remember { PlayerViewModel(context) }

            val systemDynamicPrimary = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).primary
                else dynamicLightColorScheme(context).primary
            } else {
                MaterialTheme.colorScheme.primary
            }

            LaunchedEffect(systemDynamicPrimary) {
                playerViewModel.updateDefaultColor(systemDynamicPrimary)
            }

            val currentSeedColor by playerViewModel.themeColor.collectAsState()

            DynamicAppTheme(currentSeedColor) {
                CitoleScreen(playerViewModel)
            }
        }
    }
}

enum class Page {
    Songs,
    Albums,
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
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun CitoleScreen(
    playerViewModel: PlayerViewModel
) {

    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var openAlertDialog by remember { mutableStateOf(false) }

    var selectedPage by remember { mutableStateOf(Page.Songs) }

    val scope = rememberCoroutineScope()

    val libraryViewModel = remember { LibraryViewModel() }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch { libraryViewModel.loadSongs(context) }
        } else {
            Log.e("Permissions", "Permission Denied")
        }
    }

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, permission)
        if (check == PackageManager.PERMISSION_GRANTED) {
            libraryViewModel.loadSongs(context)
        } else {
            launcher.launch(permission)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 2. This is what shows up inside the drawer
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(30.dp))
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Songs,
                    onSelected =  {selectedPage = Page.Songs},
                    iconId = R.drawable.ic_music,
                    text = "Songs"
                )
                CustomNavigationDrawerItem(
                    selected = selectedPage == Page.Albums,
                    onSelected =  {selectedPage = Page.Albums},
                    iconId = R.drawable.ic_album,
                    text = "Albums"
                )
            }
        }
    ) {
        Box (
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
        ){

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
                        Page.Songs -> SongsPage(libraryViewModel, playerViewModel, paddingValues)
                        Page.Albums -> AlbumsPage(libraryViewModel, playerViewModel, paddingValues)
                    }
                }
            }
            CustomFloatingToolbar(playerViewModel)
            if (openAlertDialog) {
                FilterDialog (
                    onDismissRequest = {openAlertDialog = false},
                    libraryViewModel
                )
            }
        }
    }
}