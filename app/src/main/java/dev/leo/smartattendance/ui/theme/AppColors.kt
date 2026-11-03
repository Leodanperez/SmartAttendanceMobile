package dev.leo.smartattendance.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Paleta de la app que cambia con el tema (claro/oscuro).
 * Las pantallas usan AppTheme.colors.xxx; el valor real lo provee SmartAttendanceMobileTheme.
 */
data class AppColors(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val primaryFixed: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val secondaryFixed: Color,
    val background: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val errorContainer: Color,
    // Estados de asistencia (se adaptan al fondo claro/oscuro).
    val successText: Color,
    val successBg: Color,
    val warningText: Color,
    val warningBg: Color,
    val infoText: Color,
    val infoBg: Color,
    val isDark: Boolean,
)

val LightAppColors = AppColors(
    primary = Color(0xFF1A56DB),
    primaryContainer = Color(0xFF003FB1),
    onPrimary = Color(0xFFFFFFFF),
    primaryFixed = Color(0xFFDBE1FF),
    secondary = Color(0xFF006398),
    secondaryContainer = Color(0xFF5BB8FE),
    secondaryFixed = Color(0xFFCCE5FF),
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF4FF),
    surfaceContainerHigh = Color(0xFFDCE9FF),
    surfaceContainerHighest = Color(0xFFD6E3FB),
    onSurface = Color(0xFF0F1C2D),
    onSurfaceVariant = Color(0xFF434654),
    outline = Color(0xFF737686),
    outlineVariant = Color(0xFFC3C5D7),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    successText = Color(0xFF065F46),
    successBg = Color(0xFFECFDF5),
    warningText = Color(0xFF78350F),
    warningBg = Color(0xFFFFFBEB),
    infoText = Color(0xFF1E40AF),
    infoBg = Color(0xFFEFF6FF),
    isDark = false,
)

val DarkAppColors = AppColors(
    primary = Color(0xFF8C9EFF),
    primaryContainer = Color(0xFF3B4FCC),
    onPrimary = Color(0xFF0A1020),
    primaryFixed = Color(0xFF2A3356),
    secondary = Color(0xFF7FC4FF),
    secondaryContainer = Color(0xFF14517A),
    secondaryFixed = Color(0xFF1C2E40),
    background = Color(0xFF0E1420),
    surface = Color(0xFF0E1420),
    surfaceContainerLowest = Color(0xFF161D2B),
    surfaceContainerLow = Color(0xFF1A2230),
    surfaceContainerHigh = Color(0xFF26303F),
    surfaceContainerHighest = Color(0xFF2F3A4B),
    onSurface = Color(0xFFE6EAF2),
    onSurfaceVariant = Color(0xFFAEB4C2),
    outline = Color(0xFF8A90A0),
    outlineVariant = Color(0xFF3A4252),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    successText = Color(0xFF6EE7B7),
    successBg = Color(0xFF0C2A22),
    warningText = Color(0xFFFCD34D),
    warningBg = Color(0xFF2A2412),
    infoText = Color(0xFF93B4FF),
    infoBg = Color(0xFF14233F),
    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Acceso corto a la paleta del tema actual: AppTheme.colors.onSurface */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current
}
