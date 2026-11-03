package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.UserDto
import dev.leo.smartattendance.domain.model.User
import dev.leo.smartattendance.domain.model.UserRole

fun UserDto.toDomain(uid: String): User {
    return User(
        uid = uid,
        firstName = name.substringBefore(" "),
        lastName = name.substringAfter(" ", ""),
        email = email,
        role = try { UserRole.valueOf(role.uppercase()) } catch (e: Exception) { UserRole.TEACHER },
        department = department
    )
}
