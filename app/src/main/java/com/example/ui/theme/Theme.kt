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

private val DarkColorScheme =
  darkColorScheme(
    primary = MintNeon,
    onPrimary = SlateBg,
    secondary = IceBlue,
    onSecondary = SlateBg,
    tertiary = SunsetOrange,
    background = SlateBg,
    onBackground = SoftSilver,
    surface = GlassSurface,
    onSurface = SoftSilver,
    surfaceVariant = CharcoalDark,
    onSurfaceVariant = MutedText
  )

private val LightColorScheme = DarkColorScheme // Standardize on our premium dark theme for this prototype as requested

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme by default
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
