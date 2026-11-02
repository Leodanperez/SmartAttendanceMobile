package dev.leo.smartattendance.domain.model

data class Course(
    val id: String,
    val code: String,
    val name: String,
    val section: String,
    val studentCount: Int,
    val attendanceRate: Int,
    val schedule: String
)
