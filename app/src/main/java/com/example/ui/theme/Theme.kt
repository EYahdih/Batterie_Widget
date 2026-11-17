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

val VoltDarkColorScheme =
  darkColorScheme(
    primary = VoltEmeraldDark,
    onPrimary = VoltOnEmeraldDark,
    primaryContainer = VoltEmeraldContainerDark,
    onPrimaryContainer = VoltOnEmeraldContainerDark,
    secondary = VoltSageDark,
    onSecondary = VoltOnSageDark,
    secondaryContainer = VoltSageContainerDark,
    onSecondaryContainer = VoltOnSageContainerDark,
    tertiary = VoltSkyDark,
    tertiaryContainer = VoltSkyContainerDark,
    background = VoltBackgroundDark,
    surface = VoltSurfaceDark,
    surfaceVariant = VoltSurfaceVariantDark,
    onSurface = VoltOnSurfaceDark,
    onSurfaceVariant = VoltOnSurfaceVariantDark,
    outline = VoltOutlineDark,
    error = VoltErrorDark,
    onError = VoltOnErrorDark,
    errorContainer = VoltErrorContainerDark,
    onErrorContainer = VoltOnErrorContainerDark,
  )

val VoltLightColorScheme =
  lightColorScheme(
    primary = VoltEmeraldLight,
    onPrimary = VoltOnEmeraldLight,
    primaryContainer = VoltEmeraldContainerLight,
    onPrimaryContainer = VoltOnEmeraldContainerLight,
    secondary = VoltSageLight,
    onSecondary = VoltOnSageLight,
    secondaryContainer = VoltSageContainerLight,
    onSecondaryContainer = VoltOnSageContainerLight,
    tertiary = VoltSkyLight,
    tertiaryContainer = VoltSkyContainerLight,
    background = VoltBackgroundLight,
    surface = VoltSurfaceLight,
    surfaceVariant = VoltSurfaceVariantLight,
    onSurface = VoltOnSurfaceLight,
    onSurfaceVariant = VoltOnSurfaceVariantLight,
    outline = VoltOutlineLight,
    error = VoltErrorLight,
    onError = VoltOnErrorLight,
    errorContainer = VoltErrorContainerLight,
    onErrorContainer = VoltOnErrorContainerLight,
  )

@Composable
fun VoltTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> VoltDarkColorScheme
      else -> VoltLightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = VoltTypography, content = content)
}
