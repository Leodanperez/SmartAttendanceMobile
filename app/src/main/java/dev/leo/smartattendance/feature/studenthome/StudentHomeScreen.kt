package dev.leo.smartattendance.feature.studenthome

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.feature.enrollment.EnrollCourseDialog
import dev.leo.smartattendance.ui.theme.Emerald50
import dev.leo.smartattendance.ui.theme.Emerald800
import dev.leo.smartattendance.ui.theme.OnSurface
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Primary
import dev.leo.smartattendance.ui.theme.PrimaryContainer
import dev.leo.smartattendance.ui.theme.Secondary
import dev.leo.smartattendance.ui.theme.SurfaceContainerHigh
import dev.leo.smartattendance.ui.theme.SurfaceContainerLowest

@Composable
fun StudentHomeScreen(
    studentName: String = "Ana López",
    courses: List<Course> = emptyList(),
    currentRoute: NavRoutes = NavRoutes.StudentHome,
    onNavigateToRoute: (NavRoutes) -> Unit = {},
    onEnrollStudent: (String) -> Unit = {}
) {
    var showEnrollDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmartAttendanceHeader(
                title = "Inicio",
                subtitle = "SmartAttendance M3",
                badgeText = "ESTUDIANTE",
                onProfileClick = { onNavigateToRoute(NavRoutes.Profile) }
            )
        },
        bottomBar = {
            SmartAttendanceBottomBar(
                currentRoute = currentRoute,
                userRole = UserRole.STUDENT,
                onTabSelected = { route -> onNavigateToRoute(route) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                Column {
                    Text(
                        text = "PANEL DE ESTUDIANTE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Secondary
                    )
                    Text(
                        text = "Hola, $studentName",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Action Card: Scan QR
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Asistencia del Día",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = OnSurface
                        )
                        Text(
                            text = "Escanea el código QR proyectado en el aula (Android o Angular)",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onNavigateToRoute(NavRoutes.Sessions) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear QR con Cámara", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Enroll Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Cursos Inscritos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = OnSurface
                    )

                    OutlinedButton(
                        onClick = { showEnrollDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Unirme",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unirme a Curso")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }

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
                                text = "Aún no estás inscrito en ninguna asignatura. Presiona 'Unirme a Curso' e ingresa el código proporcionado por tu docente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(courses) { course ->
                    StudentCourseCard(
                        code = course.code.ifEmpty { "DS-2026" },
                        name = course.name,
                        teacher = course.teacherName.ifEmpty { "Docente Asignado" },
                        attendanceRate = "${course.attendanceRate.takeIf { it > 0 } ?: 100}%"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    if (showEnrollDialog) {
        EnrollCourseDialog(
            onDismiss = { showEnrollDialog = false },
            onEnrollSuccess = { code ->
                onEnrollStudent(code)
            }
        )
    }
}

@Composable
fun StudentCourseCard(
    code: String,
    name: String,
    teacher: String,
    attendanceRate: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = SurfaceContainerHigh
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = OnSurface
                )

                Text(
                    text = "Docente: $teacher",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = Emerald50
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Asistencia",
                        tint = Emerald800,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = attendanceRate,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Emerald800
                    )
                }
            }
        }
    }
}
