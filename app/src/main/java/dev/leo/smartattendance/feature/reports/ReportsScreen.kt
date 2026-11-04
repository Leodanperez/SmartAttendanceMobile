package dev.leo.smartattendance.feature.reports
import dev.leo.smartattendance.ui.theme.AppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.domain.model.AttendanceStatus
import dev.leo.smartattendance.domain.model.RosterEntry
import dev.leo.smartattendance.domain.model.Session

/**
 * Reporte de asistencia en vivo del docente, con datos reales de Firestore.
 * Muestra la sesión activa, su resumen (presentes/tardanzas/faltas/pendientes) y el roster en vivo.
 */
@Composable
fun ReportsScreen(
    activeSession: Session? = null,
    roster: List<RosterEntry> = emptyList()
) {
    val present = roster.count { it.status == AttendanceStatus.PRESENT }
    val late = roster.count { it.status == AttendanceStatus.LATE }
    val justified = roster.count { it.status == AttendanceStatus.JUSTIFIED }
    val pending = roster.count { it.status == null }
    val absent = roster.count { it.status == AttendanceStatus.ABSENT }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Reporte de Asistencia en Vivo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
            Text(
                text = if (activeSession != null)
                    "${activeSession.courseName.ifBlank { "Curso" }} • ${activeSession.title.ifBlank { "Sesión" }}"
                else
                    "No tienes una sesión activa en este momento.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (activeSession == null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppTheme.colors.surfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Inicia una sesión desde Inicio para ver aquí, en tiempo real, quién va registrando su asistencia.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            return@LazyColumn
        }

        // Resumen en vivo.
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill("Presentes", present.toString(), AppTheme.colors.successText, AppTheme.colors.successBg, Modifier.weight(1f))
                SummaryPill("Tardanzas", late.toString(), AppTheme.colors.warningText, AppTheme.colors.warningBg, Modifier.weight(1f))
                SummaryPill("Pendientes", pending.toString(), AppTheme.colors.onSurfaceVariant, AppTheme.colors.surfaceContainerHigh, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill("Faltas", absent.toString(), MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
                SummaryPill("Justificadas", justified.toString(), AppTheme.colors.primary, AppTheme.colors.primaryFixed, Modifier.weight(1f))
                SummaryPill("Inscritos", roster.size.toString(), AppTheme.colors.onSurface, AppTheme.colors.surfaceContainerHigh, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Lista de Estudiantes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (roster.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay estudiantes inscritos en este curso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }
        } else {
            items(roster) { entry ->
                RosterItemCard(entry)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SummaryPill(
    title: String,
    count: String,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
fun RosterItemCard(entry: RosterEntry) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface
                    )
                    Text(
                        text = entry.code.ifBlank { "Sin código" },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }
            }

            val (statusText, statusBg, statusColor) = when (entry.status) {
                AttendanceStatus.PRESENT -> Triple("PRESENTE", AppTheme.colors.successBg, AppTheme.colors.successText)
                AttendanceStatus.LATE -> Triple("TARDANZA", AppTheme.colors.warningBg, AppTheme.colors.warningText)
                AttendanceStatus.ABSENT -> Triple("FALTA", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                AttendanceStatus.JUSTIFIED -> Triple("JUSTIFICADO", AppTheme.colors.primaryFixed, AppTheme.colors.primary)
                null -> Triple("PENDIENTE", AppTheme.colors.surfaceContainerHigh, AppTheme.colors.onSurfaceVariant)
            }

            Surface(shape = CircleShape, color = statusBg) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
