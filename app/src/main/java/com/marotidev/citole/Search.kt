package com.marotidev.citole

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel
) {

    val slowSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val searchBarState = rememberContainedSearchBarState(
        animationSpecForExpand = slowSpring,
        animationSpecForCollapse = slowSpring
    )
    val scope = rememberCoroutineScope()

    val inputField = @Composable {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.titleSmall
        ) {
            SearchBarDefaults.InputField(
                query = libraryViewModel.searchQuery,
                onQueryChange = { query -> libraryViewModel.onSearchQueryChanged(query)},
                onSearch = { /* handle search */ },
                expanded = searchBarState.currentValue == SearchBarValue.Expanded,
                onExpandedChange = { scope.launch { searchBarState.animateToExpanded() }},
                placeholder = { Text("Search...", style = MaterialTheme.typography.titleSmall) },
            )
        }
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = {
            SearchBar(
                state = searchBarState,
                inputField = inputField,
            )
        },
        navigationIcon = {
            IconButton(onClick = {  }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            FilledTonalIconButton (
                onClick = {  },
                shapes = IconButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.medium // Morphs to rounded square
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 6.dp)
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp)
        ) {
            items(libraryViewModel.filteredSongs) { song ->
                SongItem(
                    song,
                    playerViewModel
                ) {
                    playerViewModel.addToQueue(song)
                    scope.launch {
                        libraryViewModel.onSearchQueryChanged("")
                        searchBarState.animateToCollapsed()
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedTopBar(
    libraryViewModel: LibraryViewModel,
    playerViewModel: PlayerViewModel,
    onMenuClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    BackHandler(enabled = isFocused) {
        libraryViewModel.onSearchQueryChanged("")
        focusManager.clearFocus()
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        title = {
            TextField(
                value = libraryViewModel.searchQuery,
                onValueChange = { query -> libraryViewModel.onSearchQueryChanged(query)},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                placeholder = { Text("Search...", style = MaterialTheme.typography.titleSmall) },
                textStyle = MaterialTheme.typography.titleSmall,
                shape = RoundedCornerShape(28.dp),
                trailingIcon = {
                    AnimatedVisibility(
                        modifier = Modifier.padding(end = 2.dp),
                        visible = libraryViewModel.searchQuery.isNotEmpty(),
                        enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                        exit = scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioLowBouncy)),
                    ) {
                        IconButton(
                            onClick = {
                                libraryViewModel.onSearchQueryChanged("")
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (libraryViewModel.filteredSongs.isNotEmpty()) {
                            val topSong = libraryViewModel.filteredSongs.first()
                            playerViewModel.addToQueue(topSong)
                        }
                        libraryViewModel.onSearchQueryChanged("")
                        focusManager.clearFocus()
                    },
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = {onMenuClick()}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            FilledTonalIconButton (
                onClick = {  },
                shapes = IconButtonDefaults.shapes(
                    shape = CircleShape,
                    pressedShape = MaterialTheme.shapes.medium // Morphs to rounded square
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shuffle),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 6.dp)
    )
}