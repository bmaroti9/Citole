package com.marotidev.citole.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
        motionScheme = MotionScheme.expressive()
    )
}
