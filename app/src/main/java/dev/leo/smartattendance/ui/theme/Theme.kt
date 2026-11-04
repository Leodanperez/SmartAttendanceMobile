package dev.leo.smartattendance.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Esquema Material 3 derivado de la paleta de la app (para componentes M3 nativos). */
private fun materialScheme(c: AppColors) =
    if (c.isDark) {
        darkColorScheme(
            primary = c.primary,
            primaryContainer = c.primaryContainer,
            onPrimary = c.onPrimary,
            secondary = c.secondary,
            secondaryContainer = c.secondaryContainer,
            background = c.background,
            surface = c.surface,
            surfaceContainerLowest = c.surfaceContainerLowest,
            surfaceContainerLow = c.surfaceContainerLow,
            surfaceContainerHigh = c.surfaceContainerHigh,
            surfaceContainerHighest = c.surfaceContainerHighest,
            onBackground = c.onSurface,
            onSurface = c.onSurface,
            onSurfaceVariant = c.onSurfaceVariant,
            outline = c.outline,
            outlineVariant = c.outlineVariant,
            error = c.error,
            errorContainer = c.errorContainer,
        )
    } else {
        lightColorScheme(
            primary = c.primary,
            primaryContainer = c.primaryContainer,
            onPrimary = c.onPrimary,
            secondary = c.secondary,
            secondaryContainer = c.secondaryContainer,
            background = c.background,
            surface = c.surface,
            surfaceContainerLowest = c.surfaceContainerLowest,
            surfaceContainerLow = c.surfaceContainerLow,
            surfaceContainerHigh = c.surfaceContainerHigh,
            surfaceContainerHighest = c.surfaceContainerHighest,
            onBackground = c.onSurface,
            onSurface = c.onSurface,
            onSurfaceVariant = c.onSurfaceVariant,
            outline = c.outline,
            outlineVariant = c.outlineVariant,
            error = c.error,
            errorContainer = c.errorContainer,
        )
    }

@Composable
fun SmartAttendanceMobileTheme(
    mode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = materialScheme(appColors)

    // Iconos de la barra de estado (hora, batería) legibles según el fondo.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? android.app.Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content
        )
    }
}
