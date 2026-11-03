package dev.leo.smartattendance.data.dto

data class SessionDto(
    val id: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val title: String = "",
    val room: String = "",
    val teacherId: String = "",
    val status: String = "ACTIVE",
    val token: String = "",
    val startsAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 15 * 60 * 1000
)
