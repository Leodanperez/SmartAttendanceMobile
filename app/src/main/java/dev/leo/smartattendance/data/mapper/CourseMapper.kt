package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.CourseDto
import dev.leo.smartattendance.domain.model.Course

fun CourseDto.toDomain(documentId: String = ""): Course {
    return Course(
        id = id.ifEmpty { documentId },
        code = courseCode,
        name = name,
        description = description,
        section = section,
        teacherIds = teacherIds,
        teacherName = teacherName,
        studentCount = studentCount,
        attendanceRate = 90,
        schedule = schedule
    )
}

fun Course.toDto(): CourseDto {
    return CourseDto(
        id = id,
        name = name,
        description = description,
        courseCode = code,
        section = section,
        teacherIds = teacherIds,
        teacherName = teacherName,
        status = "ACTIVE",
        studentCount = studentCount,
        schedule = schedule
    )
}
