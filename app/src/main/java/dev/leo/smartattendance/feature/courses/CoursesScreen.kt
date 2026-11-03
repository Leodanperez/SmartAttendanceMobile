package dev.leo.smartattendance.feature.courses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.feature.teacherhome.CourseCardItem
import dev.leo.smartattendance.ui.theme.OnSurface
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Primary
import dev.leo.smartattendance.ui.theme.PrimaryFixed
import dev.leo.smartattendance.ui.theme.Secondary
import dev.leo.smartattendance.ui.theme.SecondaryFixed
import dev.leo.smartattendance.ui.theme.SurfaceContainerHigh
import dev.leo.smartattendance.ui.theme.Tertiary

@Composable
fun CoursesScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Mis Cursos",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )
                Text(
                    text = "Gestión de asignaturas y secciones para la asistencia",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            CourseCardItem(
                code = "CS201",
                codeBg = SecondaryFixed,
                codeTextColor = Secondary,
                sectionInfo = "Lab 4 • 28 alumnos",
                courseTitle = "Algoritmos y Estructuras de Datos",
                attendancePercentage = "92% Asist.",
                schedule = "Lun / Mié 08:00 - 10:00"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            CourseCardItem(
                code = "CS304",
                codeBg = PrimaryFixed,
                codeTextColor = Primary,
                sectionInfo = "Aula 102 • 32 alumnos",
                courseTitle = "Bases de Datos Avanzadas",
                attendancePercentage = "88% Asist.",
                schedule = "Mar / Jue 10:00 - 12:00"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            CourseCardItem(
                code = "CS402",
                codeBg = SurfaceContainerHigh,
                codeTextColor = Tertiary,
                sectionInfo = "Pab. Robótica • 22 alumnos",
                courseTitle = "Inteligencia Artificial",
                attendancePercentage = "95% Asist.",
                schedule = "Vie 14:00 - 18:00"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
