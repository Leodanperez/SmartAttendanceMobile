package dev.leo.smartattendance.domain.model

enum class SessionStatus {
    ACTIVE,
    CLOSED,
    EXPIRED
}

data class Session(
    val id: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val title: String = "",
    val room: String = "",
    val teacherId: String = "",
    val status: SessionStatus = SessionStatus.ACTIVE,
    val token: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 15 * 60 * 1000,
    val presentCount: Int = 0,
    val totalEnrolledCount: Int = 24
)
