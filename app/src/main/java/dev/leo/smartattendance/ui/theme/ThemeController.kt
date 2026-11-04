package dev.leo.smartattendance.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

/** Preferencia de tema elegida por el usuario. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Guarda y expone la preferencia de tema. Persiste en SharedPreferences.
 * Es un estado observable por Compose (mutableStateOf).
 */
class ThemeController(context: Context) {
    private val prefs = context.getSharedPreferences("sa_prefs", Context.MODE_PRIVATE)

    var mode by mutableStateOf(readMode())
        private set

    fun updateMode(newMode: ThemeMode) {
        mode = newMode
        prefs.edit { putString(KEY, newMode.name) }
    }

    private fun readMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(prefs.getString(KEY, ThemeMode.SYSTEM.name)!!) }
            .getOrDefault(ThemeMode.SYSTEM)

    private companion object {
        const val KEY = "theme_mode"
    }
}
