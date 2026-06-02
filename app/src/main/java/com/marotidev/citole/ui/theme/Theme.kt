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

package com.marotidev.citole.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DynamicAppTheme(seedColor: Color, content: @Composable () -> Unit) {
    DynamicMaterialExpressiveTheme (
        seedColor = seedColor,
        style = PaletteStyle.TonalSpot,
        animate = true,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        content = content,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        typography = Typography,
    )
}

object M3ExpressiveTransitions {

    const val WIDTH = 0.25f

    private val SpatialSpring = spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = 0.82f,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )

    private val FadeSpring = spring<Float>(
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * WIDTH).toInt() },
            animationSpec = SpatialSpring
        ) + fadeIn(animationSpec = FadeSpring)
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -(fullWidth * WIDTH).toInt() },
            animationSpec = SpatialSpring
        ) + fadeOut(animationSpec = FadeSpring)
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * WIDTH).toInt() },
            animationSpec = SpatialSpring
        ) + fadeIn(animationSpec = FadeSpring)
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> (fullWidth * WIDTH).toInt() },
            animationSpec = SpatialSpring
        ) + fadeOut(animationSpec = FadeSpring)
    }
}
