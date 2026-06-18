package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val ScaleDarkColorScheme = darkColorScheme(
    primary = ScaleGreenAccent,
    onPrimary = ScaleBlack,
    secondary = ScalePurpleAccent,
    background = ScaleBlack,
    surface = ScaleCardBlack,
    onBackground = ScaleTextPrimary,
    onSurface = ScaleTextPrimary,
    outline = ScaleBorderGray
)

private val ScaleLightColorScheme = lightColorScheme(
    primary = ScaleEvergreenAccent,
    onPrimary = Color.White,
    secondary = ScalePurpleAccent,
    background = ScaleLightSection,
    surface = Color.White,
    onBackground = ScaleLightTextPrimary,
    onSurface = ScaleLightTextPrimary,
    outline = Color(0xFFE2E2E6)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark modern Scale brand style by default
  dynamicColor: Boolean = false, // Keep consistent branding colors by default
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ScaleDarkColorScheme else ScaleLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
