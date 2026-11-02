package dev.leo.smartattendance.domain.usecase

import dev.leo.smartattendance.core.result.Resource
import dev.leo.smartattendance.domain.model.User
import dev.leo.smartattendance.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Los campos no pueden estar vacíos")
        }
        return repository.login(email, password)
    }
}
