package dev.leo.smartattendance.domain.model

/**
 * Entrada del roster en vivo de una sesión (reporte del docente).
 * [status] null = pendiente (inscrito que aún no registró).
 */
data class RosterEntry(
    val studentId: String,
    val name: String,
    val code: String,
    val status: AttendanceStatus?,
    val registeredAtMillis: Long?,
)
