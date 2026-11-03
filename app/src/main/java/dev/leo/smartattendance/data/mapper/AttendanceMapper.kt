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
        courseName = "",
        studentId = studentId,
        studentName = studentName,
        institutionalCode = studentCode ?: "",
        status = parsedStatus,
        // El Timestamp del servidor a millis para la UI de historial/reportes.
        registeredAt = registeredAt?.toDate()?.time ?: 0L,
        source = source
    )
}

fun Attendance.toDto(): AttendanceDto {
    return AttendanceDto(
        id = id,
        sessionId = sessionId,
        courseId = courseId,
        studentId = studentId,
        studentName = studentName,
        studentCode = institutionalCode.ifEmpty { null },
        status = status.name,
        source = source,
        // registeredAt lo pone el DataSource con serverTimestamp(); tokenId se pasa aparte.
        registeredAt = null
    )
}
