package dev.leo.smartattendance.domain.model

enum class UserRole {
    STUDENT,
    TEACHER,
    ADMIN
}

enum class UserStatus {
    PENDING,
    ACTIVE,
    DISABLED
}

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val institutionalCode: String = "",
    val role: UserRole = UserRole.STUDENT,
    val status: UserStatus = UserStatus.ACTIVE,
    val department: String = "Facultad de Ingeniería"
) {
    val fullName: String
        get() = if (firstName.isNotBlank() || lastName.isNotBlank()) "$firstName $lastName".trim() else email.substringBefore("@")
}
