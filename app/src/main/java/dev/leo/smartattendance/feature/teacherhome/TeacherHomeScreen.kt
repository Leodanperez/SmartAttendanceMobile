package dev.leo.smartattendance.feature.teacherhome
import dev.leo.smartattendance.core.icons.LucideIcons
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.domain.model.scheduleSummary
import dev.leo.smartattendance.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme

@Composable
fun TeacherHomeScreen(
    userName: String = "Prof. Morales",
    userRole: String = "Docente",
    courses: List<dev.leo.smartattendance.domain.model.Course> = emptyList(),
    activeSession: dev.leo.smartattendance.domain.model.Session? = null,
    activeEnrolledCount: Int = 0,
    activePresentCount: Int = 0,
    recentActivity: List<dev.leo.smartattendance.domain.model.TeacherActivity> = emptyList(),
    currentRoute: NavRoutes = NavRoutes.TeacherHome,
    onNavigateToRoute: (NavRoutes) -> Unit = {},
    onNewSessionClick: () -> Unit = {},
    onOpenActiveQr: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SmartAttendanceHeader(
                title = "Inicio",
                subtitle = "SmartAttendance M3",
                badgeText = userRole,
                onNotificationClick = {
                    Toast.makeText(context, "No hay notificaciones nuevas", Toast.LENGTH_SHORT).show()
                },
                onProfileClick = { onNavigateToRoute(NavRoutes.Profile) }
            )
        },
        bottomBar = {
            SmartAttendanceBottomBar(
                currentRoute = currentRoute,
                onTabSelected = { route -> onNavigateToRoute(route) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            item {
                // Contextual Greeting Header
                GreetingSection(userName = userName)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                // Active Session Card (real) or empty state.
                ActiveSessionCard(
                    activeSession = activeSession,
                    presentCount = activePresentCount,
                    enrolledCount = activeEnrolledCount,
                    onVerQrClick = onOpenActiveQr,
                    onNewSessionClick = onNewSessionClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Quick Actions Grid
                QuickActionsGrid(
                    assignedCount = courses.size,
                    onNuevaSesionClick = onNewSessionClick,
                    onMisCursosClick = { onNavigateToRoute(NavRoutes.Courses) },
                    onReportesClick = { onNavigateToRoute(NavRoutes.Reports) },
                    onAjustesClick = {
                        Toast.makeText(context, "Ajustes de asistencia y tolerancias", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Assigned Courses List
                AssignedCoursesSection(
                    courses = courses,
                    onVerTodosClick = { onNavigateToRoute(NavRoutes.Courses) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                // Recent Activity Section (real)
                RecentActivitySection(activity = recentActivity)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GreetingSection(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PANEL ACADÉMICO",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.secondary
                )
                Text(
                    text = "Buenos días, $userName",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LucideIcons.Building2,
                contentDescription = "Panel",
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Gestión de asistencia",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveSessionCard(
    activeSession: dev.leo.smartattendance.domain.model.Session?,
    presentCount: Int,
    enrolledCount: Int,
    onVerQrClick: () -> Unit,
    onNewSessionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (activeSession == null) {
                // Estado vacío: no hay sesión activa.
                Text(
                    text = "Sin sesión activa",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Inicia una sesión para generar el código QR de asistencia en el aula.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onNewSessionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primaryContainer,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.SquarePlus,
                        contentDescription = "Nueva sesión",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciar sesión de asistencia",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                return@Column
            }

            val quorum = if (enrolledCount > 0) presentCount.toFloat() / enrolledCount else 0f
            val quorumPct = (quorum * 100).toInt().coerceIn(0, 100)

            // Top Row: título de sesión & Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppTheme.colors.surfaceContainerHigh
                ) {
                    Text(
                        text = activeSession.title.ifBlank { "Sesión de asistencia" },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.colors.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = AppTheme.colors.successBg
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.successText)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EN VIVO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.successText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = activeSession.courseName.ifBlank { "Curso" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quórum real (presentes / inscritos)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppTheme.colors.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$presentCount",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.onSurface
                            )
                            Text(
                                text = if (enrolledCount > 0) " / $enrolledCount estudiantes" else " registrados",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }

                        Text(
                            text = if (enrolledCount > 0) "$quorumPct% quórum registrado" else "Registros recibidos",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.secondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { quorum },
                            color = AppTheme.colors.primaryContainer,
                            trackColor = AppTheme.colors.surfaceContainerHighest,
                            strokeCap = StrokeCap.Round,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val trackColor = AppTheme.colors.surfaceContainerHighest
                    val progressColor = AppTheme.colors.primaryContainer
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 10f
                            drawCircle(color = trackColor, style = Stroke(width = stroke))
                            drawArc(
                                color = progressColor,
                                startAngle = -90f,
                                sweepAngle = 360f * quorum,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$quorumPct%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onVerQrClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primaryContainer,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LucideIcons.ScanLine,
                            contentDescription = "Scanner",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ver QR / Modo Proyector",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowRight,
                            contentDescription = "Forward",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    assignedCount: Int = 0,
    onNuevaSesionClick: () -> Unit,
    onMisCursosClick: () -> Unit,
    onReportesClick: () -> Unit,
    onAjustesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
            Text(
                text = "Accesos frecuentes",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Nueva Sesión (AppTheme.colors.primary Highlight)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.primary,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .clickable { onNuevaSesionClick() }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.SquarePlus,
                            contentDescription = "Nueva Sesión",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Nueva Sesión",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Código dinámico",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.primaryFixed.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 2. Mis Cursos
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .clickable { onMisCursosClick() }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.BookOpen,
                            contentDescription = "Mis Cursos",
                            tint = AppTheme.colors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Mis Cursos",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Text(
                            text = if (assignedCount == 1) "1 activo asignado" else "$assignedCount activos asignados",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3. Reportes
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .clickable { onReportesClick() }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.colors.primaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.ChartLine,
                            contentDescription = "Reportes",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Reportes",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Text(
                            text = "Resumen semanal",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }

            // 4. Ajustes
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(104.dp)
                    .clickable { onAjustesClick() }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.colors.secondaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Settings,
                            contentDescription = "Ajustes",
                            tint = AppTheme.colors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ajustes",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Text(
                            text = "Tolerancia y reglas",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AssignedCoursesSection(
    courses: List<dev.leo.smartattendance.domain.model.Course> = emptyList(),
    onVerTodosClick: () -> Unit
) {
    val displayCourses = if (courses.isNotEmpty()) courses else listOf(
        dev.leo.smartattendance.domain.model.Course(
            "c1", "CS201", "Algoritmos y Estructuras", "Lab 4 • 28 alumnos",
            schedule = listOf(
                dev.leo.smartattendance.domain.model.ScheduleBlock("MON", "08:00", "10:00"),
                dev.leo.smartattendance.domain.model.ScheduleBlock("WED", "08:00", "10:00")
            )
        ),
        dev.leo.smartattendance.domain.model.Course(
            "c2", "CS304", "Bases de Datos Avanzadas", "Aula 102 • 32 alumnos",
            schedule = listOf(
                dev.leo.smartattendance.domain.model.ScheduleBlock("TUE", "10:00", "12:00"),
                dev.leo.smartattendance.domain.model.ScheduleBlock("THU", "10:00", "12:00")
            )
        ),
        dev.leo.smartattendance.domain.model.Course(
            "c3", "CS402", "Inteligencia Artificial", "Pab. Robótica • 22 alumnos",
            schedule = listOf(
                dev.leo.smartattendance.domain.model.ScheduleBlock("FRI", "14:00", "18:00")
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Cursos Asignados",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = AppTheme.colors.surfaceContainerHighest
                ) {
                    Text(
                        text = "${displayCourses.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onVerTodosClick() }
            ) {
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.primary
                )
                Icon(
                    imageVector = LucideIcons.ChevronRight,
                    contentDescription = "Ver todos",
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        displayCourses.forEach { course ->
            CourseCardItem(
                code = course.code.ifEmpty { "CS201" },
                codeBg = AppTheme.colors.secondaryFixed,
                codeTextColor = AppTheme.colors.secondary,
                sectionInfo = course.section.ifEmpty { "Lab 4 • 28 alumnos" },
                courseTitle = course.name,
                attendancePercentage = "${course.attendanceRate.takeIf { it > 0 } ?: 92}% Asist.",
                schedule = course.schedule.scheduleSummary()
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CourseCardItem(
    code: String,
    codeBg: Color,
    codeTextColor: Color,
    sectionInfo: String,
    courseTitle: String,
    attendancePercentage: String,
    schedule: String,
    onDetailClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.surfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = codeBg
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = codeTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sectionInfo,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = courseTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = AppTheme.colors.successBg
                ) {
                    Text(
                        text = attendancePercentage,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.successText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LucideIcons.Clock,
                        contentDescription = "Horario",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = schedule,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppTheme.colors.surfaceContainerHigh,
                    modifier = Modifier.clickable { onDetailClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Detalle",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = LucideIcons.ArrowRight,
                            contentDescription = "Detalle",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(
    activity: List<dev.leo.smartattendance.domain.model.TeacherActivity> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Actividad Reciente",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppTheme.colors.surfaceContainerLowest,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (activity.isEmpty()) {
                    Text(
                        text = "Aún no hay actividad. Las sesiones que inicies o cierres aparecerán aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                } else {
                    activity.forEachIndexed { index, item ->
                        TeacherActivityRowItem(item)
                        if (index < activity.lastIndex) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(AppTheme.colors.surfaceContainerLow)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

private val activityTimeFormat = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.forLanguageTag("es"))

@Composable
private fun TeacherActivityRowItem(item: dev.leo.smartattendance.domain.model.TeacherActivity) {
    val isClosed = item.status.equals("CLOSED", ignoreCase = true)
    val whenMillis = item.closedAtMillis ?: item.startsAtMillis
    val whenText = if (whenMillis != null && whenMillis > 0L)
        activityTimeFormat.format(java.util.Date(whenMillis)) else "—"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isClosed) AppTheme.colors.surfaceContainerHigh else AppTheme.colors.secondaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isClosed) LucideIcons.CircleCheckBig else LucideIcons.ScanLine,
                contentDescription = null,
                tint = if (isClosed) AppTheme.colors.primary else AppTheme.colors.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = (if (isClosed) "Sesión cerrada: " else "Sesión activa: ") + item.courseName,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = whenText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }

            Text(
                text = item.title.ifBlank { "Sesión de asistencia" },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = CircleShape, color = AppTheme.colors.successBg) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.successText)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.presentCount} Presentes",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = AppTheme.colors.successText
                        )
                    }
                }

                if (item.lateCount > 0) {
                    Surface(shape = CircleShape, color = AppTheme.colors.warningBg) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.warningText)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${item.lateCount} Tardanzas",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = AppTheme.colors.warningText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherHomeScreenPreview() {
    SmartAttendanceMobileTheme {
        TeacherHomeScreen()
    }
}
