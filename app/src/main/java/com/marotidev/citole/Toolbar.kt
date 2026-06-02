package com.marotidev.citole

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow

enum class SheetState { Dismissed, Collapsed, Expanded }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFloatingToolbar(
    playerViewModel: PlayerViewModel,
    navController: NavController
) {
    playerViewModel.currentlyPlaying?.let { playing ->

        val scope = rememberCoroutineScope()
        val density = LocalDensity.current
        val configuration = LocalConfiguration.current
        val haptic = LocalHapticFeedback.current

        val screenHeightDp = remember (configuration){ configuration.screenHeightDp.dp }
        val screenWidthDp = remember (configuration){ configuration.screenWidthDp.dp }

        val screenHeightPx = remember(density, configuration) {
            with(density) { screenHeightDp.toPx() }
        }
        val collapsedVisibleHeightPx = remember(density) {
            with(density) { 104.dp.toPx() }
        }
        val collapsedOffsetPx = remember(screenHeightPx, collapsedVisibleHeightPx) {
            screenHeightPx - collapsedVisibleHeightPx
        }

        val state = remember(screenHeightPx) {
            AnchoredDraggableState(
                initialValue = SheetState.Collapsed,
                anchors = DraggableAnchors {
                    SheetState.Expanded at 0f
                    SheetState.Collapsed at collapsedOffsetPx
                    SheetState.Dismissed at screenHeightPx
                },
            )
        }

        var backProgress by remember { mutableFloatStateOf(0f) }
        var backXDir by remember { mutableIntStateOf(0) }

        val fraction by remember(collapsedOffsetPx) {
            derivedStateOf {
                val currentOffsetPx = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
                (currentOffsetPx / collapsedOffsetPx + backProgress).coerceIn(0f, 1f)
            }
        }

        val dismissFraction by remember(collapsedVisibleHeightPx) {
            derivedStateOf {
                val currentOffsetPx = if (state.offset.isNaN()) collapsedOffsetPx else state.offset
                ((currentOffsetPx - collapsedOffsetPx) / collapsedVisibleHeightPx).coerceIn(0f, 1f)
            }
        }

        LaunchedEffect(state.settledValue) {
            if (state.settledValue == SheetState.Dismissed) {
                playerViewModel.dismissPlayer()
            }
        }

        LaunchedEffect(state.currentValue) {
            if (state.currentValue == SheetState.Dismissed) {
                haptic.performHapticFeedback(HapticFeedbackType.Reject)
            }
        }

        PredictiveBackHandler(enabled = state.currentValue == SheetState.Expanded) { progressFlow ->
            try {
                progressFlow.collect { backEvent ->
                    backProgress = backEvent.progress
                    backXDir = backEvent.swipeEdge * 2 - 1
                }
                scope.launch {
                    state.animateTo(SheetState.Collapsed,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f))
                }.invokeOnCompletion {
                    backProgress = 0f
                    backXDir = 0
                }
            } catch (e: CancellationException) {
                backProgress = 0f
                backXDir = 0
            }
        }

        val toolbarTopPadding = with(density) {((screenHeightDp - 104.dp) * fraction + 104.dp * dismissFraction).toPx()}
        val toolbarHeight = 64.dp + (screenHeightDp - 104.dp + 40.dp) * (1 - fraction)
        val toolbarCornerRadius = 32.dp * fraction
        val toolbarElevation = 2.dp * fraction
        val toolbarWidth = lerp(screenWidthDp, 205.dp, fraction)
        val toolbarXOffset = lerp(0.dp - (backXDir * 100f * fraction.toDouble().pow(0.7)).dp, -(56.dp + 8.dp) / 2, fraction)

        val fabXOffset = with(density) { lerp(150.dp, toolbarWidth / 2 + 8.dp / 2, fraction).toPx()}
        val fabAlpha = lerp(0f, 1f, fraction)
        val fabShadow = lerp(0.dp, 4.dp, fraction)

        val collapsedAlpha = ((fraction - 0.8f) / 0.2f).coerceIn(0f, 1f)
        val expandedAlpha = ((0.5f - fraction) / 0.5f).coerceIn(0f, 1f)

        val fabInteractionSource = remember { MutableInteractionSource() }
        var fabIsPressed by remember { mutableStateOf(false) }
        var releasePressJob by remember { mutableStateOf<Job?>(null) }
        //val fabIsPressed by fabInteractionSource.collectIsPressedAsState()

        val fabCornerRadius by animateDpAsState(
            targetValue = if (fabIsPressed || playerViewModel.playing) 16.dp else 28.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val fabPushOffset by animateDpAsState(
            targetValue = if (fabIsPressed) 12.dp else 0.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy)
        )

        val getContainerColor = rememberUpdatedState(MaterialTheme.colorScheme.primaryContainer)
        val getTertiaryContainerColor = rememberUpdatedState(MaterialTheme.colorScheme.tertiaryContainer)
        val getOnTertiaryContainerColor = rememberUpdatedState(MaterialTheme.colorScheme.onTertiaryContainer)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(translationY = toolbarTopPadding)
        ) {

            Box(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .graphicsLayer(translationX = fabXOffset)
                    .align(Alignment.BottomCenter)
                    .size(56.dp + fabPushOffset, 56.dp)
                    .graphicsLayer {
                        alpha = fabAlpha
                        shadowElevation = fabShadow.toPx()
                        shape = RoundedCornerShape(fabCornerRadius)
                        clip = true
                    }
                    .drawBehind {
                        drawRect(getTertiaryContainerColor.value)
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            releasePressJob?.cancel()
                            fabIsPressed = true

                            val up = waitForUpOrCancellation()

                            releasePressJob = scope.launch {
                                delay(100)
                                fabIsPressed = false
                            }

                            if (up != null) {
                                up.consume()
                                playerViewModel.togglePlayPause()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = if (playerViewModel.playing) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = null,
                    tint = getOnTertiaryContainerColor.value,
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .anchoredDraggable(state, Orientation.Vertical)
                    .graphicsLayer {
                        translationX = (toolbarXOffset - fabPushOffset / 2).toPx()
                        shadowElevation = toolbarElevation.toPx()
                        shape = RoundedCornerShape(toolbarCornerRadius)
                        clip = true
                    }
                    .height(toolbarHeight)
                    .width(toolbarWidth - fabPushOffset / 2)
                    .drawBehind { drawRect(getContainerColor.value) }
            ) {
                ToolbarCollapsedState(collapsedAlpha, scope, state, playing, playerViewModel)
                ToolbarExpandedState(playerViewModel, expandedAlpha, fraction, playing,
                    navController, onPlayerClose = {scope.launch {
                        state.animateTo(SheetState.Collapsed,
                            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f))
                    }})
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolbarCollapsedState(
    collapsedAlpha : Float, scope: CoroutineScope, state: AnchoredDraggableState<SheetState>,
    currentlyPlaying: AudioHelper.AudioData, playerViewModel: PlayerViewModel
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .graphicsLayer { alpha = collapsedAlpha }
            .clickable(onClick = {
                scope.launch {
                    state.animateTo(SheetState.Expanded)
                }},
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
                .size(44.dp)
                .clip(RoundedCornerShape(30.dp)),
            model = currentlyPlaying.artworkUri,
            contentDescription = "Album Art",
            error = painterResource(R.drawable.ic_library),
            contentScale = ContentScale.Crop
        )
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                currentlyPlaying.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
            )
            Text(
                currentlyPlaying.artist,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = {
                playerViewModel.skipNext()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            modifier = Modifier.padding(end = 6.dp),
        ) { Icon(painterResource(R.drawable.ic_skip_next), contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
    }

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ToolbarExpandedState(
    playerViewModel: PlayerViewModel,
    expandedAlpha: Float,
    fraction: Float,
    currentlyPlaying: AudioHelper.AudioData,
    navController: NavController,
    onPlayerClose: () -> Unit
) {
    if (fraction < 0.5) {
        Box(
            modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = expandedAlpha
                translationY = fraction * 50f
            }
            .padding(24.dp)
        ) {
            PlayerScreen(playerViewModel, currentlyPlaying, navController, onPlayerClose)
        }
    }
}

@Composable
fun CustomCarouselDemo() {
    val colors = listOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFB74D),
        Color(0xFFBA68C8)
    )

    val pagerState = rememberPagerState(pageCount = { colors.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(colors[page]),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Page ${page + 1}",
                fontSize = 24.sp,
            )
        }
    }
}