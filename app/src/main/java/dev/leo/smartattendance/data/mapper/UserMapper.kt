package dev.leo.smartattendance.data.mapper

import dev.leo.smartattendance.data.dto.UserDto
import dev.leo.smartattendance.domain.model.User
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.domain.model.UserStatus

fun UserDto.toDomain(documentId: String = ""): User {
    val effectiveUid = uid.ifEmpty { documentId }
    val effectiveFirstName = firstName.ifEmpty { name.substringBefore(" ") }
    val effectiveLastName = lastName.ifEmpty { name.substringAfter(" ", "") }

    val parsedRole = try {
        UserRole.valueOf(role.uppercase().trim())
    } catch (e: Exception) {
        UserRole.STUDENT
    }

    val parsedStatus = try {
        UserStatus.valueOf(status.uppercase().trim())
    } catch (e: Exception) {
        UserStatus.ACTIVE
    }

    return User(
        uid = effectiveUid,
        firstName = effectiveFirstName,
        lastName = effectiveLastName,
        email = email,
        institutionalCode = institutionalCode,
        role = parsedRole,
        status = parsedStatus,
        department = department
    )
}
