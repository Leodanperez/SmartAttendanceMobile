package dev.leo.smartattendance.data.repository

import dev.leo.smartattendance.data.datasource.firestore.FirebaseFirestoreDataSource
import dev.leo.smartattendance.data.mapper.toDomain
import dev.leo.smartattendance.data.mapper.toDto
import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.Attendance
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.RecentActivity
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepositoryImpl(
    private val firestoreDataSource: FirebaseFirestoreDataSource
) : CourseRepository {

    override fun getTeacherCoursesFlow(teacherId: String): Flow<List<Course>> {
        return firestoreDataSource.getTeacherCoursesFlow(teacherId).map { dtos ->
            dtos.map { it.toDomain(it.id) }
        }
    }

    override fun getStudentCoursesFlow(studentId: String): Flow<List<Course>> {
        return firestoreDataSource.getStudentEnrolledCoursesFlow(studentId).map { dtos ->
            dtos.map { it.toDomain(it.id) }
        }
    }

    override fun getActiveSessionFlow(teacherId: String): Flow<Session?> {
        return firestoreDataSource.getActiveSessionFlow(teacherId).map { dto ->
            dto?.toDomain(dto.id)
        }
    }

    override fun getLiveAttendancesFlow(sessionId: String): Flow<List<Attendance>> {
        return firestoreDataSource.getLiveAttendancesFlow(sessionId).map { dtos ->
            dtos.map { it.toDomain(it.id) }
        }
    }

    override suspend fun createSession(session: Session): String {
        return firestoreDataSource.createSession(session.toDto())
    }

    override suspend fun closeSession(sessionId: String) {
        firestoreDataSource.closeSession(sessionId)
    }

    override suspend fun registerAttendance(attendance: Attendance): Result<Boolean> {
        return firestoreDataSource.registerAttendance(attendance.toDto())
    }

    override suspend fun enrollStudentByCode(code: String, studentId: String): Result<Boolean> {
        return firestoreDataSource.enrollStudentByCode(code, studentId)
    }

    override suspend fun seedInitialCoursesIfEmpty(teacherId: String) {
        firestoreDataSource.seedInitialCoursesIfEmpty(teacherId)
    }

    override suspend fun getActiveSession(): ActiveSession {
        return ActiveSession(
            id = "sess_01",
            room = "AULA Magna B-201",
            courseName = "Algoritmos y Estructuras de Datos",
            group = "Grupo B",
            isLive = true,
            durationMinutes = 12,
            verifiedCount = 18,
            totalCount = 24
        )
    }

    override suspend fun getAssignedCourses(): List<Course> {
        return emptyList()
    }

    override suspend fun getRecentActivities(): List<RecentActivity> {
        return emptyList()
    }
}
