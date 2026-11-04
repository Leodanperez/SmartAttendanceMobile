package dev.leo.smartattendance.feature.qrgenerator

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.core.common.QrCodeGenerator
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.domain.model.QrSession
import dev.leo.smartattendance.domain.repository.CourseRepository
import dev.leo.smartattendance.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

/** Vigencia del token del QR en segundos (igual que el panel web). */
private const val QR_SECONDS = 60

/** Payload del QR: JSON { v:1, sessionId, courseId, token, courseName, title }. */
private fun buildQrPayload(session: QrSession): String =
    JSONObject()
        .put("v", 1)
        .put("sessionId", session.sessionId)
        .put("courseId", session.courseId)
        .put("token", session.token)
        .put("courseName", session.courseName)
        .put("title", session.title)
        .toString()

@Composable
fun TeacherQrDisplayScreen(
    session: QrSession,
    enrolledCount: Int = 0,
    courseRepository: CourseRepository? = null,
    onBackToHome: () -> Unit = {},
    onCloseSession: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Sesión/token vigente (se actualiza al renovar).
    var currentSession by remember { mutableStateOf(session) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var secondsRemaining by remember { mutableIntStateOf(QR_SECONDS) }
    var closing by remember { mutableStateOf(false) }

    val liveAttendances = courseRepository
        ?.getLiveAttendancesFlow(currentSession.sessionId)
        ?.collectAsState(initial = emptyList())?.value ?: emptyList()
    val presentCount = liveAttendances.size

    // Dibuja el QR con el payload real cada vez que cambia el token.
    LaunchedEffect(currentSession.token) {
        qrBitmap = QrCodeGenerator.generateQrBitmap(buildQrPayload(currentSession), 600, 600)
    }

    // Cuenta regresiva del token; al llegar a 0 renueva automáticamente (como el web).
    LaunchedEffect(currentSession.token) {
        val expiresInMs = currentSession.tokenExpiresAtMillis - System.currentTimeMillis()
        secondsRemaining = (expiresInMs / 1000L).toInt().coerceIn(0, QR_SECONDS)
        while (secondsRemaining > 0) {
            delay(1000.milliseconds)
            secondsRemaining -= 1
        }
        // Expiró: renueva el token automáticamente.
        val rotated = courseRepository?.rotateSessionToken(
            currentSession.sessionId,
            currentSession.courseId,
            QR_SECONDS
        )
        if (rotated != null) {
            currentSession = currentSession.copy(
                token = rotated.token,
                tokenExpiresAtMillis = rotated.tokenExpiresAtMillis
            )
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val tokenProgress by animateFloatAsState(
        targetValue = (secondsRemaining / QR_SECONDS.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "tokenProgress"
    )
    val timerColor = if (secondsRemaining <= 10) MaterialTheme.colorScheme.error else AppTheme.colors.primary

    Scaffold(
        topBar = {
            SmartAttendanceHeader(
                title = "QR de Sesión",
                subtitle = "Proyector de Asistencia",
                badgeText = "DOCENTE"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Volver al inicio SIN cerrar la sesión: el QR sigue activo y se puede reabrir luego.
            TextButton(
                onClick = onBackToHome,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    imageVector = LucideIcons.ArrowLeft,
                    contentDescription = "Volver al inicio",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Volver al inicio", style = MaterialTheme.typography.labelLarge)
            }

            // Aún no inicia la ventana de asistencia: avisar al docente.
            val notStarted = currentSession.startsAtMillis > 0 &&
                    System.currentTimeMillis() < currentSession.startsAtMillis
            if (notStarted) {
                val startHhmm = remember(currentSession.startsAtMillis) {
                    java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.forLanguageTag("es"))
                        .format(java.util.Date(currentSession.startsAtMillis))
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppTheme.colors.warningBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LucideIcons.Clock,
                            contentDescription = null,
                            tint = AppTheme.colors.warningText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "La sesión inicia el $startHhmm. Los estudiantes podrán registrar asistencia a partir de esa hora.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.warningText
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LiveBadge()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentSession.courseName.ifBlank { "Curso" },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currentSession.title.ifBlank { "Sesión de asistencia" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // QR
                    val qrShape = RoundedCornerShape(24.dp)
                    Box(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .shadow(8.dp, qrShape, ambientColor = AppTheme.colors.primary.copy(alpha = 0.2f))
                            .clip(qrShape)
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), qrShape)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Código QR de asistencia",
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Icon(
                            imageVector = LucideIcons.QrCode,
                            contentDescription = "QR",
                            modifier = Modifier.size(100.dp),
                            tint = AppTheme.colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Cuenta regresiva del token
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Timer,
                                contentDescription = "Timer",
                                tint = timerColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Renueva en $timeFormatted",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.onSurface
                            )
                        }

                        Surface(shape = CircleShape, color = AppTheme.colors.primaryFixed) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.CircleCheck,
                                    contentDescription = null,
                                    tint = AppTheme.colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (enrolledCount > 0) "$presentCount / $enrolledCount registrados"
                                    else "$presentCount registrados",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AppTheme.colors.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { tokenProgress },
                        color = timerColor,
                        trackColor = AppTheme.colors.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Acciones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val rotated = courseRepository?.rotateSessionToken(
                                        currentSession.sessionId,
                                        currentSession.courseId,
                                        QR_SECONDS
                                    )
                                    if (rotated != null) {
                                        currentSession = currentSession.copy(
                                            token = rotated.token,
                                            tokenExpiresAtMillis = rotated.tokenExpiresAtMillis
                                        )
                                        Toast.makeText(context, "QR renovado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No se pudo renovar el QR", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = LucideIcons.RefreshCw,
                                contentDescription = "Renovar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Renovar QR", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (closing) return@Button
                                closing = true
                                scope.launch {
                                    val faltas = courseRepository?.closeSessionAndMarkAbsent(
                                        currentSession.sessionId,
                                        currentSession.courseId
                                    ) ?: 0
                                    val msg = if (faltas > 0) "Sesión cerrada. $faltas falta(s) registrada(s)."
                                    else "Sesión cerrada."
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    onCloseSession()
                                }
                            },
                            enabled = !closing,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            if (closing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = LucideIcons.X,
                                    contentDescription = "Cerrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Cerrar sesión",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "La sesión sigue activa. Puedes navegar y volver a abrir el QR desde Inicio.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.successText.copy(alpha = dotAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SESIÓN ACTIVA · EN VIVO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = AppTheme.colors.successText
            )
        }
    }
}