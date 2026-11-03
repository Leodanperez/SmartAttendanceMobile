package dev.leo.smartattendance.data.dto

data class EnrollmentDto(
    val id: String = "",
    val courseId: String = "",
    val studentId: String = "",
    val status: String = "ACTIVE",
    val enrolledAt: Long = System.currentTimeMillis()
)
