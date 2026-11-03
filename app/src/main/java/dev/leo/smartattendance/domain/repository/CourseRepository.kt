package dev.leo.smartattendance.domain.repository

import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.Attendance
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.CourseSessionSummary
import dev.leo.smartattendance.domain.model.QrSession
import dev.leo.smartattendance.domain.model.RecentActivity
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.domain.model.TeacherActivity
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun getTeacherCoursesFlow(teacherId: String): Flow<List<Course>>
    fun getStudentCoursesFlow(studentId: String): Flow<List<Course>>
    fun getActiveSessionFlow(teacherId: String): Flow<Session?>
    fun getLiveAttendancesFlow(sessionId: String): Flow<List<Attendance>>
    suspend fun createSession(session: Session): String
    suspend fun closeSession(sessionId: String)
    /** @param rawToken token en claro leído del QR, para validarlo contra sessionTokens. */
    suspend fun registerAttendance(attendance: Attendance, rawToken: String): Result<Boolean>
    suspend fun enrollStudentByCode(code: String, studentId: String): Result<Boolean>
    suspend fun seedInitialCoursesIfEmpty(teacherId: String)

    suspend fun getActiveSession(): ActiveSession?
    suspend fun getAssignedCourses(): List<Course>
    suspend fun getRecentActivities(): List<RecentActivity>

    /** Sesiones recientes de un curso con el estado del registro del estudiante. */
    suspend fun getRecentCourseSessions(courseId: String, studentId: String): List<CourseSessionSummary>

    // --- Sesión + QR del docente (mismo contrato que el panel web) ---
    /** Crea una sesión ACTIVE y su primer token de QR. Devuelve null si falla. */
    suspend fun createSessionWithToken(
        courseId: String,
        courseName: String,
        title: String,
        teacherId: String,
        startsAtMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 90,
        lateAfterMinutes: Int? = null,
        qrSeconds: Int = 60
    ): QrSession?

    /** Renueva el token del QR de la sesión. Devuelve el nuevo token y su expiración, o null. */
    suspend fun rotateSessionToken(sessionId: String, courseId: String, qrSeconds: Int = 60): QrSession?

    /** Nº de inscritos ACTIVE en el curso (quórum de la sesión). */
    suspend fun getEnrolledCount(courseId: String): Int

    /** Actividad reciente del docente. */
    suspend fun getTeacherRecentActivity(teacherId: String): List<TeacherActivity>
}
