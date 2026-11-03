package dev.leo.smartattendance.data.dto

data class UserDto(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val name: String = "",
    val email: String = "",
    val institutionalCode: String = "",
    val role: String = "STUDENT",
    val status: String = "ACTIVE",
    val department: String = "Facultad de Ingeniería"
)
