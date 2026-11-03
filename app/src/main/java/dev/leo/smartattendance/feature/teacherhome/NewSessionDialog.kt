package dev.leo.smartattendance.feature.teacherhome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.ui.theme.AppTheme

/**
 * Diálogo para iniciar una sesión: el docente elige uno de sus cursos.
 * Al tocar un curso se crea la sesión y se abre el QR (lo hace el llamador).
 */
@Composable
fun NewSessionDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onCourseSelected: (Course) -> Unit
) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Iniciar sesión de asistencia",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Elige el curso para generar su código QR.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                if (courses.isEmpty()) {
                    Text(
                        text = "No tienes cursos asignados.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        courses.forEach { course ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surfaceContainerLow,
                                onClick = { onCourseSelected(course) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = course.name,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = course.code.ifBlank { "Sin código" } +
                                            if (course.section.isNotBlank()) " · Sección ${course.section}" else "",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = colors.primary)
                    }
                }
            }
        }
    }
}
