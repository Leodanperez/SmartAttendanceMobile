package dev.leo.smartattendance.domain.model

enum class AttendanceStatus {
    PRESENT,
    LATE,
    ABSENT,
    JUSTIFIED
}

data class Attendance(
    val id: String = "",
    val sessionId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val institutionalCode: String = "",
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val registeredAt: Long = System.currentTimeMillis(),
    val source: String = "QR"
)
