package dev.leo.smartattendance.domain.model

/**
 * Sesión de QR creada por el docente (contrato compartido con el panel web).
 * El payload del QR se arma como JSON { v:1, sessionId, courseId, token, courseName, title }.
 */
data class QrSession(
    val sessionId: String,
    val courseId: String,
    val courseName: String,
    val title: String,
    val token: String,
    val tokenExpiresAtMillis: Long
)

/** Actividad reciente del docente (sesión + resumen de asistencia). */
data class TeacherActivity(
    val sessionId: String,
    val courseName: String,
    val title: String,
    val status: String,
    val startsAtMillis: Long?,
    val closedAtMillis: Long?,
    val presentCount: Int,
    val lateCount: Int
)
