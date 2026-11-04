package dev.leo.smartattendance.feature.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.ui.theme.AppTheme
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

// Los diálogos de resultado respetan el tema claro/oscuro de la app.

data class QrPayload(
    val sessionId: String,
    val courseId: String,
    val courseName: String,
    val title: String,
    val rawToken: String
)

/**
 * Formato del QR (contrato compartido con el panel web):
 *   JSON { "v": 1, "sessionId": "...", "courseId": "...", "token": "..." }
 *
 * Se aceptan también formatos antiguos (pipe "SA1|sessionId|token" y texto plano) por compatibilidad.
 * Devuelve null si el QR no tiene sessionId ni courseId (QR ajeno o inválido).
 */
fun parseQrPayload(raw: String, fallbackCourseName: String): QrPayload? {
    val clean = raw.trim()
    if (clean.isBlank()) return null
    return try {
        when {
            clean.startsWith("{") && clean.endsWith("}") -> {
                val json = JSONObject(clean)
                val sessionId = json.optString("sessionId", json.optString("id", ""))
                val courseId = json.optString("courseId", "")
                if (sessionId.isBlank() || courseId.isBlank()) return null
                QrPayload(
                    sessionId = sessionId,
                    courseId = courseId,
                    courseName = json.optString("courseName", fallbackCourseName),
                    title = json.optString("title", "Sesión de asistencia"),
                    rawToken = json.optString("token", "")
                )
            }
            // Formato antiguo: SA1|sessionId|token  (courseId no venía, no se puede usar)
            clean.startsWith("SA1|") -> {
                val parts = clean.split("|")
                if (parts.size < 3) return null
                QrPayload(
                    sessionId = parts[1],
                    courseId = "",
                    courseName = fallbackCourseName,
                    title = "Sesión de asistencia",
                    rawToken = parts[2]
                )
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/** Estado del registro de asistencia, para mostrar el resultado real (no solo leer el QR). */
sealed interface AttendanceUiState {
    data object Idle : AttendanceUiState
    data object Loading : AttendanceUiState
    data class Success(val payload: QrPayload) : AttendanceUiState
    /** Ya tenía asistencia en esta sesión: también cierra el escáner (reintentar no sirve). */
    data object AlreadyRegistered : AttendanceUiState
    data class Error(val message: String) : AttendanceUiState
}

private val ScannerBg = Color(0xFF243143)

@Composable
fun QrScannerScreen(
    activeCourseName: String = "Desarrollo de Sistemas",
    activeTeacherName: String = "Prof. Morales",
    registrationState: AttendanceUiState = AttendanceUiState.Idle,
    onNavigateBack: () -> Unit = {},
    onRegisterAttendance: (QrPayload) -> Unit = {},
    onResultDismissed: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFlashOn by remember { mutableStateOf(false) }
    var scannedResult by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showManualCodeDialog by remember { mutableStateOf(false) }
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    var invalidQr by remember { mutableStateOf(false) }

    // Al leer un QR: parsear y, si es válido, pedir el registro. El padre actualiza registrationState.
    LaunchedEffect(scannedResult) {
        val raw = scannedResult ?: return@LaunchedEffect
        val payload = parseQrPayload(raw, activeCourseName)
        if (payload == null) {
            invalidQr = true
            isProcessing = false
            scannedResult = null
        } else {
            onRegisterAttendance(payload)
        }
    }

    // Registro terminado:
    //  - Éxito: NO se reactiva el escáner (evita registros infinitos).
    //  - Error: se libera para reintentar.
    // El cierre lo decide el usuario al tocar "Aceptar" en cada diálogo (abajo).

    val toggleFlash = {
        isFlashOn = !isFlashOn
        activeCamera?.cameraControl?.enableTorch(isFlashOn)
        Unit
    }

    Scaffold(
        topBar = {
            SmartAttendanceHeader(
                title = "Escanear",
                subtitle = "SmartAttendance M3",
                badgeText = "ESTUDIANTE",
                showNotification = false,
                onProfileClick = {}
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Visor de cámara ─────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(ScannerBg)
            ) {
                if (hasCameraPermission) {
                    CameraPreviewView(
                        isFlashOn = isFlashOn,
                        onCameraBound = { camera -> activeCamera = camera },
                        onQrScanned = { code ->
                            // Solo se acepta una lectura a la vez: nada si ya se está procesando,
                            // si hay un resultado en pantalla o si fue un QR inválido sin descartar.
                            if (!isProcessing && scannedResult == null &&
                                registrationState == AttendanceUiState.Idle && !invalidQr
                            ) {
                                isProcessing = true
                                scannedResult = code
                            }
                        }
                    )

                    // Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        ScannerBg.copy(alpha = 0.85f),
                                        ScannerBg.copy(alpha = 0.45f),
                                        ScannerBg.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    // Controles superiores
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = LucideIcons.X,
                            description = "Cerrar",
                            onClick = onNavigateBack
                        )

                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.secondaryContainer)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Visor en vivo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }

                        GlassIconButton(
                            icon = if (isFlashOn) LucideIcons.Flashlight else LucideIcons.FlashlightOff,
                            description = "Linterna",
                            active = isFlashOn,
                            onClick = { toggleFlash() }
                        )
                    }

                    // Retícula animada
                    val infiniteTransition = rememberInfiniteTransition(label = "laser")
                    val laserFraction by infiniteTransition.animateFloat(
                        initialValue = 0.05f,
                        targetValue = 0.95f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laserY"
                    )
                    val reticleColor = AppTheme.colors.secondaryContainer

                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .align(Alignment.Center)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawBrackets(
                                color = reticleColor,
                                strokeWidth = 4.dp.toPx(),
                                length = 34.dp.toPx(),
                                radius = 18.dp.toPx()
                            )
                            val y = size.height * laserFraction
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        reticleColor.copy(alpha = 0.95f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(8.dp.toPx(), y),
                                end = Offset(size.width - 8.dp.toPx(), y),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Texto inferior + chip de seguridad
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                    ) {
                        Text(
                            text = "Apunta la cámara al código QR dinámico proyectado en el aula",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ShieldCheck,
                                    contentDescription = "Seguridad",
                                    tint = AppTheme.colors.secondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "QR dinámico • validación segura",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Registrando…
                    if (registrationState is AttendanceUiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Registrando asistencia…",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.ScanLine,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Se requiere permiso de cámara para escanear el QR de asistencia",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = AppTheme.colors.primary
                            )
                        ) {
                            Text("Conceder permiso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Panel inferior ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Contexto de la sesión
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(AppTheme.colors.primary, AppTheme.colors.primaryContainer)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = courseInitials(activeCourseName),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeCourseName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$activeTeacherName • Sesión activa",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(shape = CircleShape, color = AppTheme.colors.successBg) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.successText)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "EN VIVO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AppTheme.colors.successText
                                )
                            }
                        }
                    }
                }

                // Aviso de envío automático
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppTheme.colors.secondaryFixed.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.secondaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Zap,
                                contentDescription = "Rápido",
                                tint = AppTheme.colors.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Envío automático e instantáneo",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = AppTheme.colors.onSurface
                            )
                            Text(
                                text = "No necesitas tocar botones; tu asistencia se registra al detectar el código.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // Acciones
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { toggleFlash() },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryContainer),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) LucideIcons.FlashlightOff else LucideIcons.Flashlight,
                            contentDescription = "Flash",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isFlashOn) "Apagar linterna" else "Linterna",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { showManualCodeDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Keyboard,
                            contentDescription = "Teclado",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Código manual",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.primary
                        )
                    }
                }

                // Nota de seguridad
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.ShieldCheck,
                        contentDescription = "Verificado",
                        tint = AppTheme.colors.outline,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SmartAttendance utiliza cifrado de payload temporal y validación cruzada de red institucional para certificar tu presencia física en el aula.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.outline
                    )
                }
            }

            // Resultado real del registro
            when (registrationState) {
                is AttendanceUiState.Success -> AttendanceResultDialog(
                    payload = registrationState.payload,
                    onDismiss = {
                        onResultDismissed()
                        onNavigateBack()
                    }
                )
                is AttendanceUiState.AlreadyRegistered -> AttendanceInfoDialog(
                    message = "Ya registraste tu asistencia en esta sesión.",
                    onDismiss = {
                        onResultDismissed()
                        onNavigateBack()
                    }
                )
                is AttendanceUiState.Error -> AttendanceErrorDialog(
                    message = registrationState.message,
                    // Error reintentable: Aceptar cierra el diálogo y libera el escáner.
                    onDismiss = {
                        onResultDismissed()
                        isProcessing = false
                        scannedResult = null
                    }
                )
                else -> Unit
            }

            // QR no reconocido (no es de SmartAttendance).
            if (invalidQr) {
                AttendanceErrorDialog(
                    message = "El código escaneado no es un QR de asistencia válido.",
                    onDismiss = { invalidQr = false }
                )
            }

            if (showManualCodeDialog) {
                ManualCodeDialog(
                    onDismiss = { showManualCodeDialog = false },
                    onSubmitCode = { code ->
                        scannedResult = code
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Helpers visuales
// ─────────────────────────────────────────────────────────────

private fun courseInitials(name: String): String =
    name.split(" ")
        .filter { it.length > 2 }
        .take(2)
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .uppercase()
        .ifEmpty { name.take(2).uppercase().ifEmpty { "CU" } }

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    active: Boolean = false
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (active) AppTheme.colors.secondaryContainer else Color.White.copy(alpha = 0.15f)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (active) AppTheme.colors.onSurface else Color.White
        )
    }
}

/** Dibuja las cuatro esquinas redondeadas de la retícula. */
private fun DrawScope.drawBrackets(color: Color, strokeWidth: Float, length: Float, radius: Float) {
    val w = size.width
    val h = size.height
    val style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun bracket(x: Float, y: Float, dx: Float, dy: Float) {
        val path = Path().apply {
            moveTo(x, y + dy * length)
            lineTo(x, y + dy * radius)
            quadraticBezierTo(x, y, x + dx * radius, y)
            lineTo(x + dx * length, y)
        }
        drawPath(path = path, color = color, style = style)
    }

    bracket(0f, 0f, 1f, 1f)
    bracket(w, 0f, -1f, 1f)
    bracket(0f, h, 1f, -1f)
    bracket(w, h, -1f, -1f)
}

// ─────────────────────────────────────────────────────────────
// Cámara
// ─────────────────────────────────────────────────────────────

@Composable
fun CameraPreviewView(
    isFlashOn: Boolean,
    onCameraBound: (Camera) -> Unit,
    onQrScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val isInspection = androidx.compose.ui.platform.LocalInspectionMode.current

    if (isInspection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Vista Previa de Cámara", color = Color.White)
        }
    } else {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val barcodeScanner = BarcodeScanning.getClient()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy, onQrScanned)
                    }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        onCameraBound(camera)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onQrScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_TEXT || barcode.valueType == Barcode.TYPE_URL || barcode.rawValue != null) {
                        barcode.rawValue?.let { value ->
                            onQrScanned(value)
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

// ─────────────────────────────────────────────────────────────
// Diálogos
// ─────────────────────────────────────────────────────────────

@Composable
private fun ResultDialog(
    icon: ImageVector,
    iconDescription: String,
    iconBg: Color,
    iconTint: Color,
    title: String,
    confirmText: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppTheme.colors.surfaceContainerLowest,
        titleContentColor = AppTheme.colors.onSurface,
        textContentColor = AppTheme.colors.onSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = { content() },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AttendanceResultDialog(
    payload: QrPayload,
    onDismiss: () -> Unit
) {
    val currentTime = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.ROOT).format(Date())

    ResultDialog(
        icon = LucideIcons.CircleCheck,
        iconDescription = "Éxito",
        iconBg = AppTheme.colors.successBg,
        iconTint = AppTheme.colors.successText,
        title = "Asistencia registrada",
        confirmText = "Aceptar",
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = payload.courseName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )
            if (payload.title.isNotBlank()) {
                Text(
                    text = payload.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Surface(shape = CircleShape, color = AppTheme.colors.successBg) {
                Text(
                    text = "PRESENTE · Verificado",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.successText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    ResultDialog(
        icon = LucideIcons.X,
        iconDescription = "Error",
        iconBg = AppTheme.colors.errorContainer,
        iconTint = AppTheme.colors.error,
        title = "No se registró",
        confirmText = "Entendido",
        onDismiss = onDismiss
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AttendanceInfoDialog(
    message: String,
    onDismiss: () -> Unit
) {
    ResultDialog(
        icon = LucideIcons.CircleCheck,
        iconDescription = "Información",
        iconBg = AppTheme.colors.primaryFixed,
        iconTint = AppTheme.colors.primary,
        title = "Asistencia ya registrada",
        confirmText = "Aceptar",
        onDismiss = onDismiss
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ManualCodeDialog(
    onDismiss: () -> Unit,
    onSubmitCode: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppTheme.colors.surfaceContainerLowest,
        title = {
            Text(
                text = "Código de respaldo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "Ingresa el código alfanumérico temporal mostrado por el docente (Ej. SA1-9482):",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    placeholder = { Text("Ej: SA1-9482") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        Toast.makeText(context, "Validando código $code...", Toast.LENGTH_SHORT).show()
                        onSubmitCode(code)
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Ingresa un código válido", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Validar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun QrScannerScreenPreview() {
    SmartAttendanceMobileTheme {
        QrScannerScreen()
    }
}