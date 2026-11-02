package dev.leo.smartattendance.domain.repository

import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.RecentActivity

interface CourseRepository {
    suspend fun getActiveSession(): ActiveSession?
    suspend fun getAssignedCourses(): List<Course>
    suspend fun getRecentActivities(): List<RecentActivity>
}
