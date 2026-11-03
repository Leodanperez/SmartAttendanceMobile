package dev.leo.smartattendance.data.dto

data class CourseDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val courseCode: String = "",
    val section: String = "A",
    val room: String = "",
    val teacherIds: List<String> = emptyList(),
    val teacherName: String = "",
    val status: String = "ACTIVE",
    val studentCount: Int = 0,
    val careerName: String = "",
    val semester: Int = 0,
    val academicPeriodName: String = "",
    /**
     * Horario semanal: lista de bloques { day, start, end } (igual que el panel web).
     * Firestore lo entrega como List<Map<String, Any?>>.
     */
    val schedule: List<Map<String, @JvmSuppressWildcards Any?>> = emptyList(),
    // No se guarda en Firestore: lo calcula el DataSource para el estudiante en sesión.
    // -1 = aún no hay sesiones cerradas (sin datos).
    @get:com.google.firebase.firestore.Exclude
    val attendanceRate: Int = -1
)
