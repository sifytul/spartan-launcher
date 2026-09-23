package com.spartan.launcer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.spartan.launcer.data.model.FontMode

val Typography = Typography(
    displayMedium = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = (-1.5).sp
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)

fun fontFamilyFor(mode: FontMode): FontFamily = when (mode) {
    FontMode.SYSTEM -> FontFamily.Default
    FontMode.SANS -> FontFamily.SansSerif
    FontMode.SERIF -> FontFamily.Serif
    FontMode.MONO -> FontFamily.Monospace
}

fun Typography.withFont(fontFamily: FontFamily, scale: Float): Typography {
    fun scaleStyle(textStyle: TextStyle): TextStyle = textStyle.copy(
        fontFamily = fontFamily,
        fontSize = textStyle.fontSize * scale
    )
    return copy(
        displayMedium = scaleStyle(displayMedium),
        headlineSmall = scaleStyle(headlineSmall),
        titleLarge = scaleStyle(titleLarge),
        bodyLarge = scaleStyle(bodyLarge),
        bodyMedium = scaleStyle(bodyMedium),
        labelLarge = scaleStyle(labelLarge)
    )
}