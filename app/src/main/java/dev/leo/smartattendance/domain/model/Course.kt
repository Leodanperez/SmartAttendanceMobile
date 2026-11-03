package dev.leo.smartattendance.domain.model

data class Course(
    val id: String = "",
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val section: String = "A",
    val teacherIds: List<String> = emptyList(),
    val teacherName: String = "",
    val studentCount: Int = 0,
    val attendanceRate: Int = 0,
    val schedule: String = ""
)
