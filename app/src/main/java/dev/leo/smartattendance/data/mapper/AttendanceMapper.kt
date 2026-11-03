package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.AttendanceDto
import dev.leo.smartattendance.domain.model.Attendance
import dev.leo.smartattendance.domain.model.AttendanceStatus

fun AttendanceDto.toDomain(documentId: String = ""): Attendance {
    val parsedStatus = try {
        AttendanceStatus.valueOf(status.uppercase().trim())
    } catch (e: Exception) {
        AttendanceStatus.PRESENT
    }

    return Attendance(
        id = id.ifEmpty { documentId },
        sessionId = sessionId,
        courseId = courseId,
        courseName = courseName,
        studentId = studentId,
        studentName = studentName,
        institutionalCode = institutionalCode,
        status = parsedStatus,
        registeredAt = registeredAt,
        source = source
    )
}

fun Attendance.toDto(): AttendanceDto {
    return AttendanceDto(
        id = id,
        sessionId = sessionId,
        courseId = courseId,
        courseName = courseName,
        studentId = studentId,
        studentName = studentName,
        institutionalCode = institutionalCode,
        status = status.name,
        registeredAt = registeredAt,
        source = source
    )
}
