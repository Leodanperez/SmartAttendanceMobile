package dev.leo.smartattendance.data.dto

data class AttendanceDto(
    val id: String = "",
    val sessionId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val institutionalCode: String = "",
    val status: String = "PRESENT",
    val registeredAt: Long = System.currentTimeMillis(),
    val source: String = "QR"
)
