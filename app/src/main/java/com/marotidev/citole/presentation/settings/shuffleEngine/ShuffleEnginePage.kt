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

package com.marotidev.citole.presentation.settings.shuffleEngine

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.marotidev.citole.R
import com.materialkolor.ktx.harmonize
import kotlinx.coroutines.flow.map
import kotlin.math.min

@Composable
fun ShuffleEnginePage(
    navController: NavController,
    shuffleEngineViewModel: ShuffleEngineViewModel = hiltViewModel()
) {
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
            DiscoveryRadiusItem(shuffleEngineViewModel)
            QueueTrajectoryItem(shuffleEngineViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DiscoveryRadiusItem(
    shuffleEngineViewModel: ShuffleEngineViewModel
) {
    val haptic = LocalHapticFeedback.current

    val label = when((shuffleEngineViewModel.discoveryRadiusValue * 4).toInt()) {
        0 -> "Local"
        1 -> "District"
        2 -> "Regional"
        else -> "Global"
    }

    val subLabel = when((shuffleEngineViewModel.discoveryRadiusValue * 4).toInt()) {
        0 -> "stays within the immediate album or artist"
        1 -> "branches out to highly related albums or artists"
        2 -> "bridges genres using your past queue history"
        else -> "spans your entire library for maximum variety"
    }

    LaunchedEffect((shuffleEngineViewModel.discoveryRadiusValue * 4).toInt().coerceIn(0, 3)) {
        haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }

    Box(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .background(MaterialTheme.colorScheme.surface,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
            .padding(vertical = 14.dp, horizontal = 14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_radar),
                    contentDescription = "Radar",
                    modifier = Modifier.size(26.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Discovery Radius",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            DiscoveryRadiusVisualization(
                shuffleEngineViewModel.discoveryRadiusValue,
                modifier = Modifier.size(160.dp).padding(vertical = 20.dp)
            )
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 32.dp, bottom = 8.dp)
            ) {
                Text("Strict", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                Slider(
                    value = shuffleEngineViewModel.discoveryRadiusValue,
                    onValueChange = {shuffleEngineViewModel.updateDiscoveryRadiusSliderValue(it)},
                    onValueChangeFinished = {shuffleEngineViewModel.updateDataStoreDiscoveryRadiusSliderValue()},
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
                    modifier = Modifier.weight(1f).padding(start = 16.dp, end = 12.dp)
                )
                Text("Lenient", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun DiscoveryRadiusVisualization(originalValue: Float, modifier: Modifier = Modifier) {

    //add some resistance
    val value by animateFloatAsState(
        targetValue = originalValue * 0.7f + 0.3f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    )

    val points = remember {
        listOf(
            Offset(0.5f, 0.5f), Offset(0.58f, 0.6f),
            Offset(0.35f, 0.6f), Offset(0.7f, 0.4f),
            Offset(0.2f, 0.3f), Offset(0.78f, 0.78f),
            Offset(0.25f, 0.75f), Offset(0.58f, 0.6f),
            Offset(0.62f, 0.15f),
        )
    }

    val outsidePointColor = MaterialTheme.colorScheme.outlineVariant
    val insidePointColor = MaterialTheme.colorScheme.primary
    val circleFill = MaterialTheme.colorScheme.primaryContainer
    val circleOutline = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

        val maxRadius = min(canvasWidth, canvasHeight) / 2f

        val dynamicRadius = value * maxRadius

        if (dynamicRadius > 0f) {
            drawCircle(
                color = circleFill,
                radius = dynamicRadius,
                center = center
            )
            drawCircle(
                color = circleOutline,
                radius = dynamicRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        points.forEach { normalizedOffset ->
            val drawOffset = Offset(
                normalizedOffset.x * canvasWidth,
                normalizedOffset.y * canvasHeight
            )
            if (drawOffset.minus(center).getDistance() > dynamicRadius) {
                drawCircle(color = outsidePointColor, radius = 3.dp.toPx(), center = drawOffset)
            } else {
                drawCircle(color = insidePointColor, radius = 3.5.dp.toPx(), center = drawOffset)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueTrajectoryItem(
    shuffleEngineViewModel: ShuffleEngineViewModel
) {
    val haptic = LocalHapticFeedback.current

    val label = when((shuffleEngineViewModel.queueTrajectoryValue * 4).toInt()) {
        0 -> "Static"
        1 -> "Focused"
        2 -> "Adaptive"
        else -> "Evolving"
    }

    val subLabel = when((shuffleEngineViewModel.queueTrajectoryValue * 4).toInt()) {
        0 -> "locks the playlist flow tightly to your initial track"
        1 -> "favors your session's starting vibe with subtle variations"
        2 -> "balances your current mood with organic changes"
        else -> "allows the music to continuously evolve into new styles"
    }

    LaunchedEffect((shuffleEngineViewModel.queueTrajectoryValue * 4).toInt().coerceIn(0, 3)) {
        haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }

    val colors = listOf(
        Color(0xFF82B9F5).harmonize(MaterialTheme.colorScheme.primary),
        Color(0xFFB882F5).harmonize(MaterialTheme.colorScheme.primary),
        Color(0xFFF5B082).harmonize(MaterialTheme.colorScheme.primary),
        Color(0xFFB7DA6C).harmonize(MaterialTheme.colorScheme.primary),
    )

    val colorFlows = listOf(
        listOf(0, 0, 0, 0),
        listOf(0, 1, 1, 0),
        listOf(0, 1, 2, 1),
        listOf(0, 1, 2, 3)
    )

    val below = colorFlows[(shuffleEngineViewModel.queueTrajectoryValue * 3).toInt().coerceIn(0, 3)]
    val above = colorFlows[((shuffleEngineViewModel.queueTrajectoryValue * 3).toInt() + 1).coerceIn(0, 3)]

    val fraction = (shuffleEngineViewModel.queueTrajectoryValue * 3) % 1

    val gradientColors = listOf(
        lerp(colors[below[0]], colors[above[0]], fraction),
        lerp(colors[below[1]], colors[above[1]], fraction),
        lerp(colors[below[2]], colors[above[2]], fraction),
        lerp(colors[below[3]], colors[above[3]], fraction)
    )

    Box(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .background(MaterialTheme.colorScheme.surface,
                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(vertical = 14.dp, horizontal = 14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_hourglass_arrow),
                    contentDescription = "Hourglass",
                    modifier = Modifier.size(26.dp).padding(end = 6.dp)
                )
                Text(
                    text = "Queue trajectory",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 62.dp, end = 62.dp, top = 38.dp, bottom = 24.dp)
                    .height(6.dp)
            ) {
                val gradientBrush = Brush.linearGradient(
                    0.0f to gradientColors[0],
                    0.1f to gradientColors[0],

                    0.3f to gradientColors[1],
                    0.4f to gradientColors[1],

                    0.6f to gradientColors[2],
                    0.7f to gradientColors[2],

                    0.9f to gradientColors[3],
                    1.0f to gradientColors[3]
                )

                drawLine(
                    brush = gradientBrush,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    cap = StrokeCap.Round,
                    strokeWidth = size.height
                )
            }
            Text(label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 32.dp, bottom = 8.dp)
            ) {
                Text("Lock", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                Slider(
                    value = shuffleEngineViewModel.queueTrajectoryValue,
                    onValueChange = {shuffleEngineViewModel.updateQueueTrajectorySliderValue(it)},
                    onValueChangeFinished = {shuffleEngineViewModel.updateDataStoreQueueTrajectorySliderValue()},
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
                    modifier = Modifier.weight(1f).padding(start = 16.dp, end = 12.dp)
                )
                Text("Drift", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
            }
        }
    }
}