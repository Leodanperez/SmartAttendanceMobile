package dev.leo.smartattendance.data.repository

import dev.leo.smartattendance.domain.model.ActiveSession
import dev.leo.smartattendance.domain.model.ActivityType
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.RecentActivity
import dev.leo.smartattendance.domain.repository.CourseRepository

class CourseRepositoryImpl : CourseRepository {

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
        return listOf(
            Course(
                id = "c1",
                code = "CS201",
                name = "Algoritmos y Estructuras",
                section = "Lab 4 • 28 alumnos",
                studentCount = 28,
                attendanceRate = 92,
                schedule = "Lun / Mié 08:00 - 10:00"
            ),
            Course(
                id = "c2",
                code = "CS304",
                name = "Bases de Datos Avanzadas",
                section = "Aula 102 • 32 alumnos",
                studentCount = 32,
                attendanceRate = 88,
                schedule = "Mar / Jue 10:00 - 12:00"
            ),
            Course(
                id = "c3",
                code = "CS402",
                name = "Inteligencia Artificial",
                section = "Pab. Robótica • 22 alumnos",
                studentCount = 22,
                attendanceRate = 95,
                schedule = "Vie 14:00 - 18:00"
            )
        )
    }

    override suspend fun getRecentActivities(): List<RecentActivity> {
        return listOf(
            RecentActivity(
                id = "a1",
                title = "Sesión cerrada: Bases de Datos",
                time = "11:45",
                description = "29/32 presentes, con 2 registros catalogados en tardanza.",
                type = ActivityType.SESSION_CLOSED,
                chips = listOf("29 Presentes", "2 Tardanzas")
            ),
            RecentActivity(
                id = "a2",
                title = "Asistencia corregida manualmente",
                time = "11:50",
                description = "Carlos Mendoza actualizado tras comprobante de decanatura.",
                type = ActivityType.ATTENDANCE_CORRECTED,
                chips = listOf("Justificado Médico")
            )
        )
    }
}
