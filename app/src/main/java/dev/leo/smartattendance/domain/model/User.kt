package dev.leo.smartattendance.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Docente",
    val department: String = "Facultad de Ingeniería"
)
