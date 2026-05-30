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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.marotidev.citole.R

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex_variable_font,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(400)
        )
    )
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSansFlexFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp
    ),
    /* Other default text styles to override
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)