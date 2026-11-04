package dev.leo.smartattendance.feature.reports

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.domain.model.AttendanceStatus
import dev.leo.smartattendance.domain.model.RosterEntry
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.ui.theme.AppTheme

private val CardShape = RoundedCornerShape(20.dp)

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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reporte en vivo",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
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
                }
                if (activeSession != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    LiveBadge()
                }
            }
        }

        if (activeSession == null) {
            item {
                Surface(
                    shape = CardShape,
                    color = AppTheme.colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.primaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.ChartLine,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Sin sesión activa",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Inicia una sesión desde Inicio para ver aquí, en tiempo real, quién va registrando su asistencia.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@LazyColumn
        }

        // Resumen general con barra de distribución.
        item {
            val total = roster.size
            val attended = present + late
            val pct = if (total > 0) attended * 100 / total else 0

            val segments = listOf(
                present to AppTheme.colors.successText,
                late to AppTheme.colors.warningText,
                justified to AppTheme.colors.primary,
                absent to MaterialTheme.colorScheme.error,
                pending to AppTheme.colors.surfaceContainerHighest
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ASISTENCIA ACTUAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AppTheme.colors.secondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$attended de $total estudiantes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surfaceContainerHighest)
                    ) {
                        segments.forEach { (count, color) ->
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(count.toFloat())
                                        .fillMaxHeight()
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Resumen en vivo.
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryPill("Presentes", present.toString(), AppTheme.colors.successText, AppTheme.colors.successBg, Modifier.weight(1f))
                    SummaryPill("Tardanzas", late.toString(), AppTheme.colors.warningText, AppTheme.colors.warningBg, Modifier.weight(1f))
                    SummaryPill("Pendientes", pending.toString(), AppTheme.colors.onSurfaceVariant, AppTheme.colors.surfaceContainerHigh, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryPill("Faltas", absent.toString(), MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
                    SummaryPill("Justificadas", justified.toString(), AppTheme.colors.primary, AppTheme.colors.primaryFixed, Modifier.weight(1f))
                    SummaryPill("Inscritos", roster.size.toString(), AppTheme.colors.onSurface, AppTheme.colors.surfaceContainerHigh, Modifier.weight(1f))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lista de estudiantes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = CircleShape, color = AppTheme.colors.surfaceContainerHighest) {
                    Text(
                        text = "${roster.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
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
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    val transition = rememberInfiniteTransition(label = "live")
    val dotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Surface(shape = CircleShape, color = AppTheme.colors.successBg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.successText.copy(alpha = dotAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "EN VIVO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = AppTheme.colors.successText
            )
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
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RosterItemCard(entry: RosterEntry) {
    val (statusText, statusBg, statusColor) = when (entry.status) {
        AttendanceStatus.PRESENT -> Triple("PRESENTE", AppTheme.colors.successBg, AppTheme.colors.successText)
        AttendanceStatus.LATE -> Triple("TARDANZA", AppTheme.colors.warningBg, AppTheme.colors.warningText)
        AttendanceStatus.ABSENT -> Triple("FALTA", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        AttendanceStatus.JUSTIFIED -> Triple("JUSTIFICADO", AppTheme.colors.primaryFixed, AppTheme.colors.primary)
        null -> Triple("PENDIENTE", AppTheme.colors.surfaceContainerHigh, AppTheme.colors.onSurfaceVariant)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.code.ifBlank { "Sin código" },
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(shape = CircleShape, color = statusBg) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}