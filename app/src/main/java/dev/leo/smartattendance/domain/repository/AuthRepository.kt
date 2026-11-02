package dev.leo.smartattendance.domain.repository

import dev.leo.smartattendance.core.result.Resource
import dev.leo.smartattendance.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<User>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    fun getCurrentUser(): User?
    fun logout()
}
