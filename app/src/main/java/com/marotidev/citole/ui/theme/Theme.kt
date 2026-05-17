package com.marotidev.citole.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.dynamiccolor.ColorSpec

@Composable
fun DynamicAppTheme(seedColor: Color, content: @Composable () -> Unit) {
    DynamicMaterialTheme(
        seedColor = seedColor,
        animate = true,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        content = content,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        typography = Typography
    )
}
