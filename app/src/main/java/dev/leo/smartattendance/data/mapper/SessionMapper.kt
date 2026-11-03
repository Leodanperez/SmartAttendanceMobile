package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.SessionDto
import dev.leo.smartattendance.domain.model.Session
import dev.leo.smartattendance.domain.model.SessionStatus

fun SessionDto.toDomain(documentId: String = ""): Session {
    val parsedStatus = try {
        SessionStatus.valueOf(status.uppercase().trim())
    } catch (e: Exception) {
        SessionStatus.ACTIVE
    }

    return Session(
        id = id.ifEmpty { documentId },
        courseId = courseId,
        courseName = courseName,
        title = title,
        room = room,
        teacherId = teacherId,
        status = parsedStatus,
        token = token,
        startTime = startsAt,
        expiresAt = expiresAt
    )
}

fun Session.toDto(): SessionDto {
    return SessionDto(
        id = id,
        courseId = courseId,
        courseName = courseName,
        title = title,
        room = room,
        teacherId = teacherId,
        status = status.name,
        token = token,
        startsAt = startTime,
        expiresAt = expiresAt
    )
}
