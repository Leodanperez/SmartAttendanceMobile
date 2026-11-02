package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.UserDto
import dev.leo.smartattendance.domain.model.User

fun UserDto.toDomain(uid: String): User {
    return User(
        uid = uid,
        name = name,
        email = email,
        role = role,
        department = department
    )
}
