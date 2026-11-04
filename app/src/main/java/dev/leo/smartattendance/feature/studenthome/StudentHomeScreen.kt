package dev.leo.smartattendance.feature.studenthome

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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.feature.enrollment.EnrollCourseDialog
import dev.leo.smartattendance.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun StudentHomeScreen(
    studentName: String = "Ana López",
    courses: List<Course> = emptyList(),
    activeSessions: List<Session> = emptyList(),
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
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 28.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { StudentGreeting(studentName = studentName) }

            // Aviso: el docente inició una sesión de asistencia en uno de mis cursos.
            if (activeSessions.isNotEmpty()) {
                item {
                    ActiveSessionAlert(
                        sessions = activeSessions,
                        onScanClick = { onNavigateToRoute(NavRoutes.Sessions) }
                    )
                }
            }

            item { ScanQrCard(onScanClick = { onNavigateToRoute(NavRoutes.Sessions) }) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mis cursos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = AppTheme.colors.surfaceContainerHighest) {
                            Text(
                                text = "${courses.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showEnrollDialog = true },
                        shape = CircleShape,
                        color = AppTheme.colors.primaryFixed
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.Plus,
                                contentDescription = "Unirme",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Unirme a curso",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = AppTheme.colors.primary
                            )
                        }
                    }
                }
            }

            if (courses.isEmpty()) {
                item { EmptyCoursesCard() }
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

// ─────────────────────────────────────────────────────────────
// Saludo
// ─────────────────────────────────────────────────────────────

private fun greetingForNow(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Buenos días"
        hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private fun todayLabel(): String =
    SimpleDateFormat("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
        .format(Date())
        .replaceFirstChar { it.uppercase() }

private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifEmpty { "E" }

@Composable
private fun StudentGreeting(studentName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todayLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = AppTheme.colors.secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = greetingForNow(),
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurfaceVariant
            )
            Text(
                text = studentName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(AppTheme.colors.primary, AppTheme.colors.primaryContainer)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(studentName),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Escanear QR
// ─────────────────────────────────────────────────────────────

@Composable
private fun ScanQrCard(onScanClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AppTheme.colors.primary.copy(alpha = 0.25f),
                spotColor = AppTheme.colors.primary.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(AppTheme.colors.primary, AppTheme.colors.primaryContainer)
                )
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ASISTENCIA DEL DÍA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Escanea el código QR proyectado",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.QrCode,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AppTheme.colors.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.ScanLine,
                    contentDescription = "Escanear",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Escanear QR con cámara",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionAlert(
    sessions: List<Session>,
    onScanClick: () -> Unit
) {
    val first = sessions.first()
    val transition = rememberInfiniteTransition(label = "live")
    val dotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Surface(
        shape = CardShape,
        color = AppTheme.colors.successBg,
        border = BorderStroke(1.dp, AppTheme.colors.successText.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.successText.copy(alpha = dotAlpha))
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
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.successText),
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

// ─────────────────────────────────────────────────────────────
// Cursos
// ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyCoursesCard() {
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
                    imageVector = LucideIcons.BookOpen,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Sin cursos inscritos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aún no estás inscrito en ninguna asignatura. Presiona 'Unirme a curso' e ingresa el código proporcionado por tu docente.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
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
    val rate = attendanceRate.removeSuffix("%").trim().toIntOrNull()
    val hasData = rate != null

    val (pillBg, pillText, accent) = when {
        rate == null -> Triple(
            AppTheme.colors.surfaceContainerHigh,
            AppTheme.colors.onSurfaceVariant,
            AppTheme.colors.surfaceContainerHighest
        )
        rate >= 85 -> Triple(AppTheme.colors.successBg, AppTheme.colors.successText, AppTheme.colors.successText)
        rate >= 70 -> Triple(AppTheme.colors.warningBg, AppTheme.colors.warningText, AppTheme.colors.warningText)
        else -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )
    }

    Surface(
        onClick = onClick,
        shape = CardShape,
        color = AppTheme.colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(shape = CircleShape, color = AppTheme.colors.secondaryFixed) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Docente: $teacher",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(shape = CircleShape, color = pillBg) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (hasData) {
                                Icon(
                                    imageVector = LucideIcons.CircleCheck,
                                    contentDescription = "Asistencia",
                                    tint = pillText,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = attendanceRate,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = pillText
                            )
                        }
                    }
                }

                if (rate != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (rate / 100f).coerceIn(0f, 1f) },
                        color = accent,
                        trackColor = AppTheme.colors.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}