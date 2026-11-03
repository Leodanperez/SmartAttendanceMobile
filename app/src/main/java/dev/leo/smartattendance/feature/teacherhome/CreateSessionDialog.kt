package dev.leo.smartattendance.feature.teacherhome

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Datos capturados para crear la sesión (equivalente al formulario del panel web). */
data class NewSessionData(
    val title: String,
    val startsAtMillis: Long,
    val durationMinutes: Int,
    val lateAfterMinutes: Int?
)

private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"))

/**
 * Formulario de creación de sesión para un curso ya elegido (mismos campos que el panel Angular):
 * título, fecha y hora, duración (min) y tardanza tras (min, opcional).
 */
@Composable
fun CreateSessionDialog(
    course: Course,
    submitting: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (NewSessionData) -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var startsAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var durationText by remember { mutableStateOf("90") }
    var lateAfterText by remember { mutableStateOf("15") }
    var error by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.outlineVariant,
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        cursorColor = colors.primary,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.onSurfaceVariant,
        focusedContainerColor = colors.surfaceContainerLowest,
        unfocusedContainerColor = colors.surfaceContainerLowest
    )

    fun pickDateTime() {
        val cal = Calendar.getInstance().apply { timeInMillis = startsAt }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val picked = Calendar.getInstance().apply {
                            set(year, month, day, hour, minute, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        startsAt = picked.timeInMillis
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Crear sesión",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.name + (if (course.code.isNotBlank()) " · ${course.code}" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("Título o tema") },
                    placeholder = { Text("Semana 1 - Introducción") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fecha y hora (selector nativo).
                Text(
                    text = "Fecha y hora",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceContainerLow,
                    onClick = { pickDateTime() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateTimeFormat.format(Date(startsAt)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { c -> c.isDigit() } },
                        label = { Text("Duración (min)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lateAfterText,
                        onValueChange = { lateAfterText = it.filter { c -> c.isDigit() } },
                        label = { Text("Tardanza tras (min)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { if (!submitting) onDismiss() }) {
                        Text("Cancelar", color = colors.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            error = null
                            val t = title.trim()
                            val duration = durationText.toIntOrNull()
                            val late = lateAfterText.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                            when {
                                t.isEmpty() -> error = "Ingresa el título o tema."
                                duration == null || duration < 5 || duration > 600 ->
                                    error = "La duración debe estar entre 5 y 600 minutos."
                                late != null && (late < 0 || late > 600) ->
                                    error = "La tardanza debe estar entre 0 y 600 minutos."
                                late != null && late > duration ->
                                    error = "La tardanza no puede superar la duración."
                                else -> onCreate(
                                    NewSessionData(
                                        title = t,
                                        startsAtMillis = startsAt,
                                        durationMinutes = duration,
                                        lateAfterMinutes = late
                                    )
                                )
                            }
                        },
                        enabled = !submitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryContainer,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (submitting) "Creando…" else "Crear y mostrar QR")
                    }
                }
            }
        }
    }
}
