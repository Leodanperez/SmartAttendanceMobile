package dev.leo.smartattendance.domain.model

data class ActiveSession(
    val id: String,
    val room: String,
    val courseName: String,
    val group: String,
    val isLive: Boolean,
    val durationMinutes: Int,
    val verifiedCount: Int,
    val totalCount: Int
) {
    val quorumPercentage: Int
        get() = if (totalCount > 0) (verifiedCount * 100) / totalCount else 0
}
