package dev.leo.smartattendance.feature.auth
import dev.leo.smartattendance.core.icons.LucideIcons
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.ui.theme.AppTheme

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme

@Composable
fun LoginScreen(
    auth: FirebaseAuth?,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberSession by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val performLogin = {
        focusManager.clearFocus()
        errorMessage = null
        successMessage = null

        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty()) {
            errorMessage = "Por favor, ingresa tu correo institucional."
        } else if (cleanPassword.isEmpty()) {
            errorMessage = "Por favor, ingresa tu contraseña."
        } else {
            isLoading = true
            if (auth != null) {
                auth.signInWithEmailAndPassword(cleanEmail, cleanPassword)
                    .addOnSuccessListener {
                        isLoading = false
                        Toast.makeText(context, "¡Sesión iniciada con éxito!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException -> "No existe una cuenta registrada con este correo."
                            is FirebaseAuthInvalidCredentialsException -> "Contraseña o correo incorrecto."
                            else -> exception.localizedMessage ?: "Error al iniciar sesión. Inténtalo de nuevo."
                        }
                    }
            } else {
                isLoading = false
                Toast.makeText(context, "Modo vista previa: $cleanEmail", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
        }
    }

    val performPasswordReset = {
        focusManager.clearFocus()
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            errorMessage = "Ingresa tu correo institucional arriba para enviarte las instrucciones."
        } else {
            isLoading = true
            if (auth != null) {
                auth.sendPasswordResetEmail(cleanEmail)
                    .addOnSuccessListener {
                        isLoading = false
                        errorMessage = null
                        successMessage = "Se enviaron las instrucciones de recuperación a $cleanEmail."
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        errorMessage = "Error al enviar correo de recuperación: ${exception.localizedMessage}"
                    }
            } else {
                isLoading = false
                errorMessage = null
                successMessage = "Vista previa: Instrucciones enviadas a $cleanEmail."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AppTheme.colors.background Ambient Glow Accents
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopStart)
                .padding(top = 10.dp)
                .blur(60.dp)
                .background(AppTheme.colors.primaryFixed.copy(alpha = 0.35f), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .padding(top = 20.dp)
                .blur(60.dp)
                .background(AppTheme.colors.secondaryFixed.copy(alpha = 0.35f), shape = CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // imePadding + bringIntoView hacen que el campo enfocado suba sobre el teclado.
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Spacer(modifier = Modifier.height(12.dp))

            // App Emblem Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AppTheme.colors.primary, AppTheme.colors.primaryContainer)
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .background(AppTheme.colors.primaryContainer.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Shield,
                            contentDescription = "Escudo SmartAttendance",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Pulse indicator dot
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(AppTheme.colors.secondaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
            }

            // Title
            Text(
                text = "SmartAttendance",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Institutional Verified Pill Badge
            Surface(
                shape = CircleShape,
                color = AppTheme.colors.surfaceContainerHigh,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.ShieldCheck,
                        contentDescription = "Verificado",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Acceso Institucional Seguro",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AppTheme.colors.primary
                    )
                }
            }

            // Subtitle
            Text(
                text = "Control y registro de asistencia académica en tiempo real con credencial digital y QR dinámico",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Central Form Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppTheme.colors.surfaceContainerLowest,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Unified Portal Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AppTheme.colors.surfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = LucideIcons.GraduationCap,
                                    contentDescription = "Portal",
                                    tint = AppTheme.colors.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Portal Único Centralizado",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = AppTheme.colors.onSurface
                                    )
                                    Text(
                                        text = "Estudiantes, Catedráticos y Administrativos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTheme.colors.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Surface(
                                shape = CircleShape,
                                color = AppTheme.colors.primaryFixed
                            ) {
                                Text(
                                    text = "SSO Activo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AppTheme.colors.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Field 1: Institutional Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Correo Institucional",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = AppTheme.colors.onSurface
                        )
                        Text(
                            text = "Requerido",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = AppTheme.colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = {
                            Text(
                                text = "usuario@universidad.edu.pe",
                                color = AppTheme.colors.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = LucideIcons.AtSign,
                                contentDescription = "Correo",
                                tint = AppTheme.colors.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (email.isNotEmpty()) {
                                IconButton(onClick = { email = "" }) {
                                    Icon(
                                        imageVector = LucideIcons.CircleX,
                                        contentDescription = "Limpiar correo",
                                        tint = AppTheme.colors.outline,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.surfaceContainerLowest,
                            unfocusedContainerColor = AppTheme.colors.surfaceContainerLow,
                            focusedBorderColor = AppTheme.colors.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingresa con tu correo de dominio @universidad.edu.pe",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2: Password
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.colors.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text(
                                text = "••••••••••••",
                                color = AppTheme.colors.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = LucideIcons.Lock,
                                contentDescription = "Contraseña",
                                tint = AppTheme.colors.outline,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) LucideIcons.EyeOff else LucideIcons.Eye,
                                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = AppTheme.colors.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { performLogin() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppTheme.colors.surfaceContainerLowest,
                            unfocusedContainerColor = AppTheme.colors.surfaceContainerLow,
                            focusedBorderColor = AppTheme.colors.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Controls: Remember session & Password reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberSession = !rememberSession }
                        ) {
                            Checkbox(
                                checked = rememberSession,
                                onCheckedChange = { rememberSession = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppTheme.colors.primary,
                                    uncheckedColor = AppTheme.colors.outline
                                ),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Recordar mi sesión",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurface
                            )
                        }

                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.colors.primary,
                            modifier = Modifier.clickable { performPasswordReset() }
                        )
                    }

                    // Dynamic Error / Success Messages
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.CircleAlert,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = successMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        successMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.colors.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = AppTheme.colors.primary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AppTheme.colors.primary Action Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Verificando credenciales...",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            Icon(
                                imageVector = LucideIcons.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Sesión",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = LucideIcons.ArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Enlace de ayuda. Las cuentas las crea el administrador: no hay auto-registro.
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        Toast.makeText(context, "Soporte TI: soporte@universidad.edu.pe", Toast.LENGTH_LONG).show()
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = LucideIcons.CircleHelp,
                    contentDescription = "Soporte",
                    tint = AppTheme.colors.outline,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Centro de ayuda institucional y soporte TI",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppTheme.colors.outline
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Institutional Security & Compliance Footer
            Surface(
                shape = CircleShape,
                color = AppTheme.colors.surfaceContainerLow,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.ShieldCheck,
                        contentDescription = "Seguridad",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cifrado TLS 1.3 • Conexión Institucional Protegida",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.primary
                    )
                }
            }

            Text(
                text = "Facultad de Ingeniería • Versión 2.4.2 (Compilación Oficial)",
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colors.outline
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SmartAttendanceMobileTheme {
        LoginScreen(auth = null)
    }
}
