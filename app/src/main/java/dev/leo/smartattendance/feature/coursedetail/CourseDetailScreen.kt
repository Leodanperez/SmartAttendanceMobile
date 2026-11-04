package dev.leo.smartattendance.feature.coursedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.CourseSessionSummary
import dev.leo.smartattendance.domain.model.dayLabel
import dev.leo.smartattendance.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun CourseDetailScreen(
    course: Course,
    sessions: List<CourseSessionSummary> = emptyList(),
    isLoadingSessions: Boolean = false,
    isTeacher: Boolean = false,
    onCreateSession: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // ── Cabecera con degradado ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.linearGradient(listOf(colors.primary, colors.primaryContainer)))
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp)
                    .padding(top = 4.dp, bottom = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Detalle del curso",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp)) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Text(
                            text = course.code.ifBlank { "—" },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (course.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = course.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (course.careerName.isNotBlank()) HeroChip(course.careerName)
                        if (course.semester > 0) HeroChip("${course.semester}º ciclo")
                        if (course.academicPeriodName.isNotBlank()) HeroChip("Periodo ${course.academicPeriodName}")
                        HeroChip("Sección ${course.section.ifBlank { "—" }}")
                        if (course.room.isNotBlank()) HeroChip("Aula ${course.room}")
                        if (course.teacherName.isNotBlank()) HeroChip("Docente: ${course.teacherName}")
                    }
                }
            }
        }

        // ── Contenido ───────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isTeacher) {
                // Docente: botón para crear sesión de asistencia.
                item {
                    Button(
                        onClick = onCreateSession,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryContainer,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.SquarePlus,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Crear sesión de asistencia",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            } else {
                // Alumno: tarjeta de asistencia.
                item { AttendanceCard(rate = course.attendanceRate) }
            }

            // Horario.
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(icon = LucideIcons.Clock, text = "Horario semanal")
            }
            item {
                Surface(
                    shape = CardShape,
                    color = colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colors.primaryFixed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = LucideIcons.Clock,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = block.dayLabel(),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.onSurface
                                        )
                                    }
                                    Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                                        Text(
                                            text = "${block.start} – ${block.end}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (index < course.schedule.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Sesiones recientes.
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle(icon = LucideIcons.CalendarDays, text = "Sesiones recientes")
            }

            if (isLoadingSessions) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cargando sesiones…",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            } else if (sessions.isEmpty()) {
                item {
                    Surface(
                        shape = CardShape,
                        color = colors.surfaceContainerLowest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = LucideIcons.CalendarDays,
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Este curso aún no tiene sesiones.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(sessions) { session ->
                    SessionRow(session = session, showMyStatus = !isTeacher)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Componentes
// ─────────────────────────────────────────────────────────────

@Composable
private fun HeroChip(text: String) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AttendanceCard(rate: Int) {
    val colors = AppTheme.colors
    val hasData = rate >= 0

    val (bg, fg) = when {
        !hasData -> colors.surfaceContainerLowest to colors.onSurfaceVariant
        rate >= 85 -> colors.successBg to colors.successText
        rate >= 70 -> colors.warningBg to colors.warningText
        else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        shape = CardShape,
        color = bg,
        border = if (hasData) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = LucideIcons.CircleCheck,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mi asistencia",
                        style = MaterialTheme.typography.labelMedium,
                        color = fg.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (hasData) "$rate%" else "Sin datos",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = fg
                    )
                }
            }
            if (hasData) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { (rate / 100f).coerceIn(0f, 1f) },
                    color = fg,
                    trackColor = fg.copy(alpha = 0.18f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    icon: ImageVector,
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
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface
        )
    }
}

@Composable
private fun SessionRow(session: CourseSessionSummary, showMyStatus: Boolean) {
    val colors = AppTheme.colors
    val status = session.status.uppercase()
    val (icon, iconBg, iconTint) = when (status) {
        "ACTIVE" -> Triple(LucideIcons.ScanLine, colors.successBg, colors.successText)
        "CLOSED" -> Triple(LucideIcons.CircleCheckBig, colors.surfaceContainerHigh, colors.primary)
        else -> Triple(LucideIcons.Clock, colors.warningBg, colors.warningText)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDate(session.startsAtMillis) + " · " + sessionStatusLabel(session.status),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }

            if (showMyStatus) {
                Spacer(modifier = Modifier.width(8.dp))
                MyStatusChip(session.myStatus)
            }
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