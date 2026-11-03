package dev.leo.smartattendance.data.dto

data class CourseDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val courseCode: String = "",
    val section: String = "A",
    val teacherIds: List<String> = emptyList(),
    val teacherName: String = "",
    val status: String = "ACTIVE",
    val studentCount: Int = 0,
    val schedule: String = "",
    // No se guarda en Firestore: lo calcula el DataSource para el estudiante en sesión.
    // -1 = aún no hay sesiones cerradas (sin datos).
    @get:com.google.firebase.firestore.Exclude
    val attendanceRate: Int = -1
)
