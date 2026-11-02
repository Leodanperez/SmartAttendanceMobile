package dev.leo.smartattendance.domain.model

enum class ActivityType {
    SESSION_CLOSED,
    ATTENDANCE_CORRECTED
}

data class RecentActivity(
    val id: String,
    val title: String,
    val time: String,
    val description: String,
    val type: ActivityType,
    val chips: List<String>
)
