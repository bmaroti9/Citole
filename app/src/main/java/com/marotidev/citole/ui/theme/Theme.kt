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
