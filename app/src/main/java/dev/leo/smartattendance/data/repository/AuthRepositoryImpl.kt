package dev.leo.smartattendance.data.repository

import dev.leo.smartattendance.core.result.Resource
import dev.leo.smartattendance.data.datasource.auth.FirebaseAuthDataSource
import dev.leo.smartattendance.data.datasource.firestore.FirebaseFirestoreDataSource
import dev.leo.smartattendance.data.dto.UserDto
import dev.leo.smartattendance.data.mapper.toDomain
import dev.leo.smartattendance.domain.model.User
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreDataSource: FirebaseFirestoreDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val firebaseUser = authDataSource.login(email, password)
            if (firebaseUser != null) {
                val userDto = firestoreDataSource.getUser(firebaseUser.uid)
                val user = userDto?.toDomain(firebaseUser.uid) ?: User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    firstName = firebaseUser.displayName?.substringBefore(" ") ?: "Usuario",
                    role = UserRole.STUDENT
                )
                Resource.Success(user)
            } else {
                Resource.Error("No se pudo iniciar sesión")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error de autenticación")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val firebaseUser = authDataSource.register(email, password)
            if (firebaseUser != null) {
                val dto = UserDto(
                    uid = firebaseUser.uid,
                    firstName = name.substringBefore(" "),
                    lastName = name.substringAfter(" ", ""),
                    email = email,
                    role = "STUDENT",
                    status = "ACTIVE"
                )
                firestoreDataSource.saveUser(firebaseUser.uid, dto)
                Resource.Success(dto.toDomain(firebaseUser.uid))
            } else {
                Resource.Error("No se pudo crear la cuenta")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error en el registro")
        }
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            authDataSource.sendPasswordReset(email)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error al enviar correo")
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = authDataSource.getCurrentUser() ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            firstName = firebaseUser.displayName?.substringBefore(" ") ?: "Usuario",
            role = UserRole.STUDENT
        )
    }

    override fun logout() {
        authDataSource.logout()
    }
}
