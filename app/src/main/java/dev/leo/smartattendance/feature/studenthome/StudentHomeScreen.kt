package dev.leo.smartattendance.feature.studenthome
import dev.leo.smartattendance.core.icons.LucideIcons
import dev.leo.smartattendance.core.icons.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.feature.enrollment.EnrollCourseDialog

@Composable
fun StudentHomeScreen(
    studentName: String = "Ana López",
    courses: List<Course> = emptyList(),
    activeSessions: List<dev.leo.smartattendance.domain.model.Session> = emptyList(),
    currentRoute: NavRoutes = NavRoutes.StudentHome,
    onNavigateToRoute: (NavRoutes) -> Unit = {},
    onEnrollStudent: (String) -> Unit = {},
    onCourseClick: (Course) -> Unit = {}
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
                        color = AppTheme.colors.secondary
                    )
                    Text(
                        text = "Hola, $studentName",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Aviso: el docente inició una sesión de asistencia en uno de mis cursos.
            if (activeSessions.isNotEmpty()) {
                item {
                    ActiveSessionAlert(
                        sessions = activeSessions,
                        onScanClick = { onNavigateToRoute(NavRoutes.Sessions) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Action Card: Scan QR
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Asistencia del Día",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Text(
                            text = "Escanea el código QR proyectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onNavigateToRoute(NavRoutes.Sessions) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.ScanLine,
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
                        color = AppTheme.colors.onSurface
                    )

                    OutlinedButton(
                        onClick = { showEnrollDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Plus,
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
                        color = AppTheme.colors.surfaceContainerLowest,
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
                                color = AppTheme.colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aún no estás inscrito en ninguna asignatura. Presiona 'Unirme a Curso' e ingresa el código proporcionado por tu docente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(courses) { course ->
                    StudentCourseCard(
                        code = course.code.ifEmpty { "—" },
                        name = course.name,
                        teacher = course.teacherName.ifEmpty { "Por asignar" },
                        // -1 = aún no hay sesiones cerradas en el curso.
                        attendanceRate = if (course.attendanceRate < 0) "Sin datos" else "${course.attendanceRate}%",
                        onClick = { onCourseClick(course) }
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
private fun ActiveSessionAlert(
    sessions: List<dev.leo.smartattendance.domain.model.Session>,
    onScanClick: () -> Unit
) {
    val first = sessions.first()
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppTheme.colors.successBg,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Punto "en vivo".
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.successText)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (sessions.size == 1) "Sesión de asistencia activa"
                           else "${sessions.size} sesiones de asistencia activas",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.successText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (sessions.size == 1)
                    "Tu docente inició una sesión en ${first.courseName.ifBlank { "tu curso" }}. Escanea el QR para registrar tu asistencia."
                else
                    "Tienes varias sesiones activas. Escanea el QR que te muestre tu docente para registrar tu asistencia.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.ScanLine,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Escanear QR ahora", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun StudentCourseCard(
    code: String,
    name: String,
    teacher: String,
    attendanceRate: String,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        onClick = onClick,
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
                    color = AppTheme.colors.surfaceContainerHigh
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )

                Text(
                    text = "Docente: $teacher",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }

            val hasData = attendanceRate.endsWith("%")
            Surface(
                shape = CircleShape,
                color = if (hasData) AppTheme.colors.successBg else AppTheme.colors.surfaceContainerHigh
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (hasData) {
                        Icon(
                            imageVector = LucideIcons.CircleCheck,
                            contentDescription = "Asistencia",
                            tint = AppTheme.colors.successText,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = attendanceRate,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (hasData) AppTheme.colors.successText else AppTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
