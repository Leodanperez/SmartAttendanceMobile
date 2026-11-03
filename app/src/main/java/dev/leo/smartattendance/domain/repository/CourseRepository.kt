package dev.leo.smartattendance.domain.repository

import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.Attendance
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.RecentActivity
import dev.leo.smartattendance.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getTeacherCoursesFlow(teacherId: String): Flow<List<Course>>
    fun getStudentCoursesFlow(studentId: String): Flow<List<Course>>
    fun getActiveSessionFlow(teacherId: String): Flow<Session?>
    fun getLiveAttendancesFlow(sessionId: String): Flow<List<Attendance>>
    suspend fun createSession(session: Session): String
    suspend fun closeSession(sessionId: String)
    suspend fun registerAttendance(attendance: Attendance): Result<Boolean>
    suspend fun enrollStudentByCode(code: String, studentId: String): Result<Boolean>
    suspend fun seedInitialCoursesIfEmpty(teacherId: String)

    suspend fun getActiveSession(): ActiveSession?
    suspend fun getAssignedCourses(): List<Course>
    suspend fun getRecentActivities(): List<RecentActivity>
}
