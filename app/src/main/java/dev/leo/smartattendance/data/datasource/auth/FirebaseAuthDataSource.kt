package dev.leo.smartattendance.data.datasource.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthDataSource(private val auth: FirebaseAuth) {
    fun login(email: String, password: String, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> onComplete(result.user, null) }
            .addOnFailureListener { exception -> onComplete(null, exception) }
    }

    fun register(email: String, password: String, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result -> onComplete(result.user, null) }
            .addOnFailureListener { exception -> onComplete(null, exception) }
    }

    fun sendPasswordReset(email: String, onComplete: (Boolean, Exception?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { exception -> onComplete(false, exception) }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logout() = auth.signOut()
}
