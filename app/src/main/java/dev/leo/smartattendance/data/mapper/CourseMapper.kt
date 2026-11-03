package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.CourseDto
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.ScheduleBlock

private val WEEK_ORDER = mapOf("MON" to 1, "TUE" to 2, "WED" to 3, "THU" to 4, "FRI" to 5, "SAT" to 6, "SUN" to 7)

private fun List<Map<String, Any?>>.toScheduleBlocks(): List<ScheduleBlock> =
    mapNotNull { raw ->
        val day = (raw["day"] as? String)?.uppercase() ?: return@mapNotNull null
        val start = raw["start"] as? String ?: return@mapNotNull null
        val end = raw["end"] as? String ?: return@mapNotNull null
        if (day !in WEEK_ORDER) return@mapNotNull null
        ScheduleBlock(day = day, start = start, end = end)
    }.sortedWith(compareBy({ WEEK_ORDER[it.day] ?: 99 }, { it.start }))

private fun List<ScheduleBlock>.toScheduleMaps(): List<Map<String, Any?>> =
    map { mapOf("day" to it.day, "start" to it.start, "end" to it.end) }

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
        // Lo calcula el DataSource; -1 = sin datos todavía.
        attendanceRate = attendanceRate,
        schedule = schedule.toScheduleBlocks(),
        careerName = careerName,
        semester = semester,
        academicPeriodName = academicPeriodName
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
        careerName = careerName,
        semester = semester,
        academicPeriodName = academicPeriodName,
        schedule = schedule.toScheduleMaps()
    )
}
