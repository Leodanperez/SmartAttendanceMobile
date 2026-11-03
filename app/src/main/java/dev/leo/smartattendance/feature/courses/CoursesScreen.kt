package dev.leo.smartattendance.feature.courses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.feature.teacherhome.CourseCardItem
import dev.leo.smartattendance.ui.theme.OnSurface
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Secondary
import dev.leo.smartattendance.ui.theme.SecondaryFixed
import dev.leo.smartattendance.ui.theme.SurfaceContainerLowest

@Composable
fun CoursesScreen(
    courses: List<Course> = emptyList()
) {
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

        if (courses.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin Cursos Inscritos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Solo verás los cursos en los que estás matriculado activamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(courses) { course ->
                CourseCardItem(
                    code = course.code.ifEmpty { "CS201" },
                    codeBg = SecondaryFixed,
                    codeTextColor = Secondary,
                    sectionInfo = course.section.ifEmpty { "Lab 4 • 28 alumnos" },
                    courseTitle = course.name,
                    attendancePercentage = "${course.attendanceRate.takeIf { it > 0 } ?: 92}% Asist.",
                    schedule = course.schedule.ifEmpty { "Lun / Mié 08:00 - 10:00" }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
