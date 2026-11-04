package dev.leo.smartattendance.feature.qrscanner
import dev.leo.smartattendance.core.icons.LucideIcons
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.ui.theme.AppTheme

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
    //  - Éxito: NO se reactiva el escáner (evita registros infinitos). Se cierra solo tras 2 s.
    //  - Error: se libera para reintentar.
    // Mientras hay un resultado en pantalla, el escáner no vuelve a leer.
    // El cierre lo decide el usuario al tocar "Aceptar" en cada diálogo (abajo).

    Scaffold(
        topBar = {
            SmartAttendanceHeader(
                title = "Escanear",
                subtitle = "SmartAttendance M3",
                badgeText = "ESTUDIANTE",
                showNotification = false,
                onProfileClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Camera Viewport & Scrim Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color(0xFF243143))
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

                    // Scrim Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF243143).copy(alpha = 0.85f),
                                        Color(0xFF243143).copy(alpha = 0.55f),
                                        Color(0xFF243143).copy(alpha = 0.90f)
                                    )
                                )
                            )
                    )

                    // In-View Overlay Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
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
                                    text = "Visor en Vivo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isFlashOn = !isFlashOn
                                activeCamera?.cameraControl?.enableTorch(isFlashOn)
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFlashOn) AppTheme.colors.secondaryContainer else Color.White.copy(alpha = 0.15f)
                                )
                        ) {
                            Icon(
                                imageVector = if (isFlashOn) LucideIcons.Flashlight else LucideIcons.FlashlightOff,
                                contentDescription = "Linterna",
                                tint = if (isFlashOn) AppTheme.colors.onSurface else Color.White
                            )
                        }
                    }

                    // Scanner Mask & Animated Reticle
                    val infiniteTransition = rememberInfiniteTransition(label = "laser")
                    val laserY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 200f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "laserY"
                    )

                    // Color leído en contexto composable para usarlo dentro del Canvas.
                    val reticleColor = AppTheme.colors.secondaryContainer
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .align(Alignment.Center)
                    ) {
                        // Corner Brackets
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 3.5.dp.toPx()
                            val bracketSize = 28.dp.toPx()
                            val cornerRadius = 16.dp.toPx()
                            val bracketColor = reticleColor

                            // Top-Left
                            drawArc(
                                color = bracketColor,
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset.Zero,
                                size = Size(cornerRadius * 2, cornerRadius * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            drawLine(bracketColor, Offset(cornerRadius, 0f), Offset(bracketSize, 0f), strokeWidth)
                            drawLine(bracketColor, Offset(0f, cornerRadius), Offset(0f, bracketSize), strokeWidth)

                            // Top-Right
                            drawArc(
                                color = bracketColor,
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(size.width - cornerRadius * 2, 0f),
                                size = Size(cornerRadius * 2, cornerRadius * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            drawLine(bracketColor, Offset(size.width - bracketSize, 0f), Offset(size.width - cornerRadius, 0f), strokeWidth)
                            drawLine(bracketColor, Offset(size.width, cornerRadius), Offset(size.width, bracketSize), strokeWidth)

                            // Bottom-Left
                            drawArc(
                                color = bracketColor,
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(0f, size.height - cornerRadius * 2),
                                size = Size(cornerRadius * 2, cornerRadius * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            drawLine(bracketColor, Offset(0f, size.height - bracketSize), Offset(0f, size.height - cornerRadius), strokeWidth)
                            drawLine(bracketColor, Offset(cornerRadius, size.height), Offset(bracketSize, size.height), strokeWidth)

                            // Bottom-Right
                            drawArc(
                                color = bracketColor,
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(size.width - cornerRadius * 2, size.height - cornerRadius * 2),
                                size = Size(cornerRadius * 2, cornerRadius * 2),
                                style = Stroke(width = strokeWidth)
                            )
                            drawLine(bracketColor, Offset(size.width - bracketSize, size.height), Offset(size.width - cornerRadius, size.height), strokeWidth)
                            drawLine(bracketColor, Offset(size.width, size.height - bracketSize), Offset(size.width, size.height - cornerRadius), strokeWidth)

                            // Laser Sweep Line
                            drawLine(
                                color = reticleColor.copy(alpha = 0.85f),
                                start = Offset(12.dp.toPx(), laserY.dp.toPx()),
                                end = Offset(size.width - 12.dp.toPx(), laserY.dp.toPx()),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }

                        Icon(
                            imageVector = LucideIcons.ScanLine,
                            contentDescription = "Target",
                            tint = AppTheme.colors.secondaryContainer.copy(alpha = 0.25f),
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                    }

                    // Scrim Bottom Status & Geofence Chips
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Text(
                            text = "Apunta la cámara al código QR dinámico proyectado en el aula",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.MapPin,
                                        contentDescription = "GPS",
                                        tint = AppTheme.colors.secondaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Aula B-204 (GPS OK)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ShieldCheck,
                                        contentDescription = "Seguridad",
                                        tint = AppTheme.colors.secondaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Anti-rebote • sa1.*",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White
                                    )
                                }
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
                        Text(
                            text = "Se requiere permiso de cámara para escanear el QR de asistencia",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary)
                        ) {
                            Text("Conceder Permiso", color = Color.White)
                        }
                    }
                }
            }

            // Information & Direct Action Deck
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Instant Auto-Submit Information Notice
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppTheme.colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.secondaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Zap,
                                contentDescription = "Rápido",
                                tint = AppTheme.colors.secondary,
                                modifier = Modifier.size(20.dp)
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
                                text = "No necesitas tocar botones; registra tu asistencia al detectar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Classroom Session Context Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppTheme.colors.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppTheme.colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CS3",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = activeCourseName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AppTheme.colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "$activeTeacherName • Sesión Activa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppTheme.colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = CircleShape,
                            color = AppTheme.colors.surfaceContainerLowest
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.secondary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "EN VIVO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AppTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Flashlight Toggle AppTheme.colors.primary Pill Button
                Button(
                    onClick = {
                        isFlashOn = !isFlashOn
                        activeCamera?.cameraControl?.enableTorch(isFlashOn)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) LucideIcons.FlashlightOff else LucideIcons.Flashlight,
                        contentDescription = "Flash",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFlashOn) "Desactivar Linterna" else "Activar Linterna del Aula",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Manual Backup Code / Assistance Action
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppTheme.colors.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { showManualCodeDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LucideIcons.Keyboard,
                            contentDescription = "Teclado",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ingresar código de respaldo o ayuda",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Footnote
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(horizontal = 4.dp)
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

            // A11 --- Resultado real del registro
            when (val state = registrationState) {
                is AttendanceUiState.Success -> AttendanceResultDialog(
                    payload = state.payload,
                    // Aceptar cierra el escáner (o se cierra solo a los 2 s).
                    onDismiss = {
                        onResultDismissed()
                        onNavigateBack()
                    }
                )
                is AttendanceUiState.AlreadyRegistered -> AttendanceInfoDialog(
                    message = "Ya registraste tu asistencia en esta sesión.",
                    // Ya está registrado: Aceptar cierra el escáner.
                    onDismiss = {
                        onResultDismissed()
                        onNavigateBack()
                    }
                )
                is AttendanceUiState.Error -> AttendanceErrorDialog(
                    message = state.message,
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

            // Manual Code Prompt Dialog
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

@Composable
fun CameraPreviewView(
    isFlashOn: Boolean,
    onCameraBound: (Camera) -> Unit,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
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
                        it.setSurfaceProvider(previewView.surfaceProvider)
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

@Composable
fun AttendanceResultDialog(
    payload: QrPayload,
    onDismiss: () -> Unit
) {
    val currentTime = SimpleDateFormat("HH:mm:ss - dd/MM/yyyy", Locale.ROOT).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLowest,
        titleContentColor = AppTheme.colors.onSurface,
        textContentColor = AppTheme.colors.onSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.successBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.CircleCheck,
                        contentDescription = "Éxito",
                        tint = AppTheme.colors.successText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Asistencia Registrada",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Curso: ${payload.courseName}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
                if (payload.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sesión: ${payload.title}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Estado: PRESENTE (Verificado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.successText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fecha y hora: $currentTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Aceptar", color = Color.White)
            }
        }
    )
}

@Composable
fun AttendanceErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLowest,
        titleContentColor = AppTheme.colors.onSurface,
        textContentColor = AppTheme.colors.onSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Error",
                        tint = AppTheme.colors.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "No se registró",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
            }
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Entendido", color = Color.White)
            }
        }
    )
}

@Composable
fun AttendanceInfoDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLowest,
        titleContentColor = AppTheme.colors.onSurface,
        textContentColor = AppTheme.colors.onSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.primaryContainer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.CircleCheck,
                        contentDescription = "Información",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Asistencia ya registrada",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.colors.onSurface
                )
            }
        },
        text = {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurface)
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Aceptar", color = Color.White)
            }
        }
    )
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
        title = {
            Text(
                text = "Código de Respaldo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "Ingresa el código alfanumérico temporal mostrado por el docente (Ej. SA1-9482):",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    placeholder = { Text("Ej: SA1-9482") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Validar", color = Color.White)
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
