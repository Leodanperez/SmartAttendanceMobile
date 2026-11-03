package dev.leo.smartattendance.domain.model

/** Bloque de horario: día de la semana y rango de horas (HH:mm). */
data class ScheduleBlock(
    val day: String = "MON",
    val start: String = "",
    val end: String = ""
)

/** Etiqueta en español para el día del bloque (coincide con el panel web). */
fun ScheduleBlock.dayLabel(): String = when (day.uppercase()) {
    "MON" -> "Lunes"
    "TUE" -> "Martes"
    "WED" -> "Miércoles"
    "THU" -> "Jueves"
    "FRI" -> "Viernes"
    "SAT" -> "Sábado"
    "SUN" -> "Domingo"
    else -> day
}

/** Etiqueta corta del día (Lun, Mar, …). */
fun ScheduleBlock.dayShort(): String = when (day.uppercase()) {
    "MON" -> "Lun"
    "TUE" -> "Mar"
    "WED" -> "Mié"
    "THU" -> "Jue"
    "FRI" -> "Vie"
    "SAT" -> "Sáb"
    "SUN" -> "Dom"
    else -> day
}

/** Resumen en una línea del horario: "Lun 08:00–10:00 · Mié 10:00–12:00" o "Sin horario". */
fun List<ScheduleBlock>.scheduleSummary(): String =
    if (isEmpty()) "Sin horario"
    else joinToString(" · ") { "${it.dayShort()} ${it.start}–${it.end}" }

data class Course(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val section: String = "A",
    val room: String = "",
    val teacherIds: List<String> = emptyList(),
    val teacherName: String = "",
    val studentCount: Int = 0,
    val attendanceRate: Int = 0,
    /** Horario semanal del curso. Puede tener varios bloques o estar vacío. */
    val schedule: List<ScheduleBlock> = emptyList(),
    val careerName: String = "",
    /** Ciclo de la carrera (1..n). 0 = no definido. */
    val semester: Int = 0,
    val academicPeriodName: String = ""
)
