package dev.leo.smartattendance.feature.coursedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.CourseSessionSummary
import dev.leo.smartattendance.domain.model.dayLabel
import dev.leo.smartattendance.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CourseDetailScreen(
    course: Course,
    sessions: List<CourseSessionSummary> = emptyList(),
    isLoadingSessions: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Barra superior con botón atrás.
        Surface(
            color = colors.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Detalle del curso",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Encabezado del curso.
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                            Text(
                                text = course.code.ifBlank { "—" },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.onSurface
                        )
                        if (course.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (course.careerName.isNotBlank()) Chip(course.careerName)
                            if (course.semester > 0) Chip("${course.semester}º ciclo")
                            if (course.academicPeriodName.isNotBlank()) Chip("Periodo ${course.academicPeriodName}")
                            Chip("Sección ${course.section.ifBlank { "—" }}")
                            if (course.teacherName.isNotBlank()) Chip("Docente: ${course.teacherName}")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Tarjeta de asistencia.
            item {
                val hasData = course.attendanceRate >= 0
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (hasData) colors.successBg else colors.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (hasData) colors.successText else colors.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mi asistencia",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant
                            )
                            Text(
                                text = if (hasData) "${course.attendanceRate}%" else "Sin datos",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (hasData) colors.successText else colors.onSurface
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Horario.
            item {
                SectionTitle(icon = Icons.Outlined.Schedule, text = "Horario semanal")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (course.schedule.isEmpty()) {
                            Text(
                                text = "Sin horario definido.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        } else {
                            course.schedule.forEachIndexed { index, block ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = block.dayLabel(),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "${block.start} – ${block.end}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant
                                    )
                                }
                                if (index < course.schedule.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(14.dp)) }

            // Sesiones recientes.
            item {
                SectionTitle(icon = Icons.Outlined.CalendarMonth, text = "Sesiones recientes")
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoadingSessions) {
                item {
                    Text(
                        text = "Cargando sesiones…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            } else if (sessions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainerLowest,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Este curso aún no tiene sesiones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                sessions.forEach { session ->
                    item {
                        SessionRow(session)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    val colors = AppTheme.colors
    Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface
        )
    }
}

@Composable
private fun SessionRow(session: CourseSessionSummary) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = colors.onSurface
                )
                Text(
                    text = formatDate(session.startsAtMillis) + " · " + sessionStatusLabel(session.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            MyStatusChip(session.myStatus)
        }
    }
}

@Composable
private fun MyStatusChip(status: String?) {
    val colors = AppTheme.colors
    val (label, bg, fg) = when (status?.uppercase()) {
        "PRESENT" -> Triple("Presente", colors.successBg, colors.successText)
        "LATE" -> Triple("Tardanza", colors.warningBg, colors.warningText)
        "JUSTIFIED" -> Triple("Justificado", colors.infoBg, colors.infoText)
        "ABSENT" -> Triple("Ausente", colors.errorContainer, colors.error)
        else -> Triple("—", colors.surfaceContainerHigh, colors.onSurfaceVariant)
    }
    Surface(shape = CircleShape, color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun sessionStatusLabel(status: String): String = when (status.uppercase()) {
    "ACTIVE" -> "Activa"
    "CLOSED" -> "Cerrada"
    "SCHEDULED" -> "Programada"
    else -> status
}

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es"))

private fun formatDate(millis: Long?): String =
    if (millis == null || millis <= 0L) "Sin fecha" else dateFormat.format(Date(millis))
