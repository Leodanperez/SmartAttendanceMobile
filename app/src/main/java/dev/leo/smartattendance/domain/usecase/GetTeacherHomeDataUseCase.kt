package dev.leo.smartattendance.domain.usecase

import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.RecentActivity
import dev.leo.smartattendance.domain.repository.CourseRepository

data class TeacherHomeData(
    val activeSession: ActiveSession?,
    val courses: List<Course>,
    val recentActivities: List<RecentActivity>
)

class GetTeacherHomeDataUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(): TeacherHomeData {
        return TeacherHomeData(
            activeSession = repository.getActiveSession(),
            courses = repository.getAssignedCourses(),
            recentActivities = repository.getRecentActivities()
        )
    }
}
