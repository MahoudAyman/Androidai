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

private val VaultColorScheme = darkColorScheme(
    primary = VaultPrimary,
    onPrimary = VaultOnPrimary,
    secondary = VaultSecondary,
    onSecondary = VaultOnSecondary,
    tertiary = VaultTertiary,
    onTertiary = VaultOnTertiary,
    background = VaultBackground,
    onBackground = VaultOnBackground,
    surface = VaultSurface,
    onSurface = VaultOnBackground,
    surfaceVariant = VaultSurfaceVariant,
    onSurfaceVariant = VaultOnSurfaceVariant,
    error = VaultError,
    outline = VaultOutline,
    surfaceContainer = VaultSurfaceVariant,
    surfaceContainerHighest = VaultSurface
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = VaultColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
