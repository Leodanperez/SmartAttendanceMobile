package dev.leo.smartattendance.feature.reports

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.domain.model.Attendance
import dev.leo.smartattendance.domain.model.AttendanceStatus
import dev.leo.smartattendance.ui.theme.Amber50
import dev.leo.smartattendance.ui.theme.Amber900
import dev.leo.smartattendance.ui.theme.Emerald50
import dev.leo.smartattendance.ui.theme.Emerald800
import dev.leo.smartattendance.ui.theme.OnSurface
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Primary
import dev.leo.smartattendance.ui.theme.SurfaceContainerHigh
import dev.leo.smartattendance.ui.theme.SurfaceContainerLowest

val sampleRoster = listOf(
    Attendance("1", "s1", "c1", "Algoritmos y Estructuras", "st1", "Carlos Mendoza", "EST-2024-01", AttendanceStatus.PRESENT),
    Attendance("2", "s1", "c1", "Algoritmos y Estructuras", "st2", "Ana López", "EST-2024-02", AttendanceStatus.PRESENT),
    Attendance("3", "s1", "c1", "Algoritmos y Estructuras", "st3", "Luis Gómez", "EST-2024-03", AttendanceStatus.LATE),
    Attendance("4", "s1", "c1", "Algoritmos y Estructuras", "st4", "María Torres", "EST-2024-04", AttendanceStatus.PRESENT),
    Attendance("5", "s1", "c1", "Algoritmos y Estructuras", "st5", "Juan Pérez", "EST-2024-05", AttendanceStatus.ABSENT)
)

@Composable
fun ReportsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Reporte de Asistencia en Vivo",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Text(
                text = "Algoritmos y Estructuras de Datos • Grupo B",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Summary Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(title = "Presentes", count = "18", color = Emerald800, bg = Emerald50, modifier = Modifier.weight(1f))
                SummaryPill(title = "Tardanzas", count = "2", color = Amber900, bg = Amber50, modifier = Modifier.weight(1f))
                SummaryPill(title = "Faltas", count = "4", color = MaterialTheme.colorScheme.error, bg = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Lista de Estudiantes",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(sampleRoster) { item ->
            RosterItemCard(attendance = item)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SummaryPill(
    title: String,
    count: String,
    color: androidx.compose.ui.graphics.Color,
    bg: androidx.compose.ui.graphics.Color,
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
fun RosterItemCard(attendance: Attendance) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = attendance.studentName.take(1),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = attendance.studentName,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                    Text(
                        text = attendance.institutionalCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            val (statusText, statusBg, statusColor) = when (attendance.status) {
                AttendanceStatus.PRESENT -> Triple("PRESENTE", Emerald50, Emerald800)
                AttendanceStatus.LATE -> Triple("TARDANZA", Amber50, Amber900)
                AttendanceStatus.ABSENT -> Triple("FALTA", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                AttendanceStatus.JUSTIFIED -> Triple("JUSTIFICADO", SurfaceContainerHigh, Primary)
            }

            Surface(
                shape = CircleShape,
                color = statusBg
            ) {
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
