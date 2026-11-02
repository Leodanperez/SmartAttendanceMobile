package dev.leo.smartattendance.data.dto

data class UserDto(
    val name: String = "",
    val email: String = "",
    val role: String = "Docente",
    val department: String = "Facultad de Ingeniería"
)
