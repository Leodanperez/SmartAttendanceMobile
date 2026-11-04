package dev.leo.smartattendance.feature.teacherhome

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.domain.model.TeacherActivity
import dev.leo.smartattendance.domain.model.scheduleSummary
import dev.leo.smartattendance.ui.theme.AppTheme
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val ScreenHorizontal = 16.dp
private val CardShape = RoundedCornerShape(20.dp)

// ─────────────────────────────────────────────────────────────
// Pantalla
// ─────────────────────────────────────────────────────────────

@Composable
fun TeacherHomeScreen(
    userName: String = "Prof. Morales",
    userRole: String = "Docente",
    courses: List<Course> = emptyList(),
    activeSession: Session? = null,
    activeEnrolledCount: Int = 0,
    activePresentCount: Int = 0,
    recentActivity: List<TeacherActivity> = emptyList(),
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
                bottom = innerPadding.calculateBottomPadding() + 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { GreetingSection(userName = userName) }

            item {
                ActiveSessionCard(
                    activeSession = activeSession,
                    presentCount = activePresentCount,
                    enrolledCount = activeEnrolledCount,
                    onVerQrClick = onOpenActiveQr,
                    onNewSessionClick = onNewSessionClick
                )
            }

            item {
                QuickActionsGrid(
                    assignedCount = courses.size,
                    onNuevaSesionClick = onNewSessionClick,
                    onMisCursosClick = { onNavigateToRoute(NavRoutes.Courses) },
                    onReportesClick = { onNavigateToRoute(NavRoutes.Reports) },
                    onAjustesClick = {
                        Toast.makeText(
                            context,
                            "Ajustes de asistencia y tolerancias",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            item {
                AssignedCoursesSection(
                    courses = courses,
                    onVerTodosClick = { onNavigateToRoute(NavRoutes.Courses) }
                )
            }

            item { RecentActivitySection(activity = recentActivity) }
        }
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
    name.removePrefix("Prof.").trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifEmpty { "D" }

@Composable
fun GreetingSection(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontal),
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
                text = userName,
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
                text = initialsOf(userName),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sesión activa
// ─────────────────────────────────────────────────────────────

@Composable
fun ActiveSessionCard(
    activeSession: Session?,
    presentCount: Int,
    enrolledCount: Int,
    onVerQrClick: () -> Unit,
    onNewSessionClick: () -> Unit
) {
    if (activeSession == null) {
        EmptySessionCard(onNewSessionClick = onNewSessionClick)
        return
    }

    val quorum = if (enrolledCount > 0) (presentCount.toFloat() / enrolledCount).coerceIn(0f, 1f) else 0f
    val quorumPct = (quorum * 100).toInt()
    val animatedQuorum by animateFloatAsState(
        targetValue = quorum,
        animationSpec = tween(durationMillis = 900),
        label = "quorum"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontal)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiveBadge()
                Text(
                    text = activeSession.title.ifBlank { "Sesión de asistencia" },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = activeSession.courseName.ifBlank { "Curso" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$presentCount",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (enrolledCount > 0) " / $enrolledCount" else "",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = if (enrolledCount > 0) "estudiantes presentes" else "registros recibidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 8.dp.toPx()
                        val inset = stroke / 2
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = Color.White.copy(alpha = 0.22f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke)
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedQuorum,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$quorumPct%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onVerQrClick,
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
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver QR / Modo Proyector",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = LucideIcons.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
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

    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4ADE80).copy(alpha = dotAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "EN VIVO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun EmptySessionCard(onNewSessionClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = AppTheme.colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontal)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.primaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.ScanLine,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(30.dp)
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
                text = "Inicia una sesión para generar el código QR de asistencia en el aula.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onNewSessionClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primaryContainer,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.SquarePlus,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar sesión de asistencia",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Encabezado de sección reutilizable
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    caption: String? = null,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = CircleShape, color = AppTheme.colors.surfaceContainerHighest) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        when {
            actionText != null -> Surface(
                onClick = onActionClick,
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.colors.primary
                    )
                    Icon(
                        imageVector = LucideIcons.ChevronRight,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            caption != null -> Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Acciones rápidas
// ─────────────────────────────────────────────────────────────

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
            .padding(horizontal = ScreenHorizontal)
    ) {
        SectionHeader(title = "Acciones rápidas", caption = "Accesos frecuentes")

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionTile(
                title = "Nueva sesión",
                subtitle = "Código dinámico",
                icon = LucideIcons.SquarePlus,
                highlighted = true,
                modifier = Modifier.weight(1f),
                onClick = onNuevaSesionClick
            )
            QuickActionTile(
                title = "Mis cursos",
                subtitle = if (assignedCount == 1) "1 activo asignado" else "$assignedCount activos asignados",
                icon = LucideIcons.BookOpen,
                iconBg = AppTheme.colors.secondaryFixed,
                iconTint = AppTheme.colors.secondary,
                modifier = Modifier.weight(1f),
                onClick = onMisCursosClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionTile(
                title = "Reportes",
                subtitle = "Resumen semanal",
                icon = LucideIcons.ChartLine,
                iconBg = AppTheme.colors.primaryFixed,
                iconTint = AppTheme.colors.primary,
                modifier = Modifier.weight(1f),
                onClick = onReportesClick
            )
            QuickActionTile(
                title = "Ajustes",
                subtitle = "Tolerancia y reglas",
                icon = LucideIcons.Settings,
                iconBg = AppTheme.colors.surfaceContainerHigh,
                iconTint = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                onClick = onAjustesClick
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    iconBg: Color = Color.Unspecified,
    iconTint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val container = if (highlighted) AppTheme.colors.primary else AppTheme.colors.surfaceContainerLowest
    val titleColor = if (highlighted) Color.White else AppTheme.colors.onSurface
    val subtitleColor = if (highlighted) Color.White.copy(alpha = 0.75f) else AppTheme.colors.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = CardShape,
        color = container,
        border = if (highlighted) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = if (highlighted) 4.dp else 1.dp,
        modifier = modifier.height(112.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (highlighted) Color.White.copy(alpha = 0.2f) else iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (highlighted) Color.White else iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Cursos asignados
// ─────────────────────────────────────────────────────────────

@Composable
fun AssignedCoursesSection(
    courses: List<Course> = emptyList(),
    onVerTodosClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontal)
    ) {
        SectionHeader(
            title = "Cursos asignados",
            count = courses.size,
            actionText = "Ver todos",
            onActionClick = onVerTodosClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (courses.isEmpty()) {
            Surface(
                shape = CardShape,
                color = AppTheme.colors.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = LucideIcons.BookOpen,
                        contentDescription = null,
                        tint = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Aún no tienes cursos asignados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                courses.forEach { course ->
                    CourseCardItem(
                        code = course.code,
                        sectionInfo = course.section,
                        courseTitle = course.name,
                        attendanceRate = course.attendanceRate,
                        schedule = course.schedule.scheduleSummary()
                    )
                }
            }
        }
    }
}

@Composable
fun CourseCardItem(
    code: String,
    sectionInfo: String,
    courseTitle: String,
    attendanceRate: Int,
    schedule: String,
    onDetailClick: () -> Unit = {}
) {
    val hasData = attendanceRate > 0
    val (pillBg, pillText, barColor) = when {
        !hasData -> Triple(
            AppTheme.colors.surfaceContainerHigh,
            AppTheme.colors.onSurfaceVariant,
            AppTheme.colors.surfaceContainerHighest
        )
        attendanceRate >= 85 -> Triple(
            AppTheme.colors.successBg,
            AppTheme.colors.successText,
            AppTheme.colors.successText
        )
        attendanceRate >= 70 -> Triple(
            AppTheme.colors.warningBg,
            AppTheme.colors.warningText,
            AppTheme.colors.warningText
        )
        else -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )
    }

    Surface(
        onClick = onDetailClick,
        shape = CardShape,
        color = AppTheme.colors.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barra de acento lateral
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(barColor)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (code.isNotBlank()) {
                                Surface(shape = CircleShape, color = AppTheme.colors.secondaryFixed) {
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AppTheme.colors.secondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = sectionInfo,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = courseTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(shape = CircleShape, color = pillBg) {
                        Text(
                            text = if (hasData) "$attendanceRate% asist." else "Sin datos",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = pillText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (hasData) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (attendanceRate / 100f).coerceIn(0f, 1f) },
                        color = barColor,
                        trackColor = AppTheme.colors.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LucideIcons.Clock,
                            contentDescription = "Horario",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = schedule,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Detalle",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = LucideIcons.ArrowRight,
                            contentDescription = null,
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Actividad reciente (línea de tiempo)
// ─────────────────────────────────────────────────────────────

private val activityTimeFormat = SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("es"))

@Composable
fun RecentActivitySection(
    activity: List<TeacherActivity> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenHorizontal)
    ) {
        SectionHeader(title = "Actividad reciente")

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = CardShape,
            color = AppTheme.colors.surfaceContainerLowest,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activity.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LucideIcons.Clock,
                            contentDescription = null,
                            tint = AppTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Aún no hay actividad. Las sesiones que inicies o cierres aparecerán aquí.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceVariant
                        )
                    }
                } else {
                    activity.forEachIndexed { index, item ->
                        TeacherActivityRowItem(item = item, isLast = index == activity.lastIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherActivityRowItem(item: TeacherActivity, isLast: Boolean) {
    val isClosed = item.status.equals("CLOSED", ignoreCase = true)
    val whenMillis = item.closedAtMillis ?: item.startsAtMillis
    val whenText = if (whenMillis != null && whenMillis > 0L)
        activityTimeFormat.format(Date(whenMillis)) else "—"

    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Columna del timeline: icono + línea conectora
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isClosed) AppTheme.colors.surfaceContainerHigh
                        else AppTheme.colors.secondaryFixed
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isClosed) LucideIcons.CircleCheckBig else LucideIcons.ScanLine,
                    contentDescription = null,
                    tint = if (isClosed) AppTheme.colors.primary else AppTheme.colors.secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .weight(1f)
                        .clip(CircleShape)
                        .background(AppTheme.colors.surfaceContainerHigh)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = (if (isClosed) "Sesión cerrada" else "Sesión activa"),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isClosed) AppTheme.colors.onSurfaceVariant else AppTheme.colors.secondary
                )
                Text(
                    text = whenText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
            }

            Text(
                text = item.courseName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = AppTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.title.ifBlank { "Sesión de asistencia" },
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusChip(
                    text = "${item.presentCount} presentes",
                    bg = AppTheme.colors.successBg,
                    fg = AppTheme.colors.successText
                )
                if (item.lateCount > 0) {
                    StatusChip(
                        text = "${item.lateCount} tardanzas",
                        bg = AppTheme.colors.warningBg,
                        fg = AppTheme.colors.warningText
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, bg: Color, fg: Color) {
    Surface(shape = CircleShape, color = bg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = fg
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun TeacherHomeScreenPreview() {
    SmartAttendanceMobileTheme {
        TeacherHomeScreen()
    }
}