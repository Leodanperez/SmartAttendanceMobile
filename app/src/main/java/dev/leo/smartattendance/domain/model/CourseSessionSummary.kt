package dev.leo.smartattendance.domain.model

/** Resumen de una sesión de un curso para el detalle del estudiante. */
data class CourseSessionSummary(
    val id: String,
    val title: String,
    /** ACTIVE | CLOSED | … */
    val status: String,
    /** Época en milisegundos (null si no se pudo leer). */
    val startsAtMillis: Long?,
    /** Estado del registro del estudiante (PRESENT/LATE/JUSTIFIED/ABSENT) o null si no registró. */
    val myStatus: String?
)
