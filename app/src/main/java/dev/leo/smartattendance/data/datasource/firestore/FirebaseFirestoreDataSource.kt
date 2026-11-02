package dev.leo.smartattendance.data.datasource.firestore

import com.google.firebase.firestore.FirebaseFirestore
import dev.leo.smartattendance.data.dto.UserDto

class FirebaseFirestoreDataSource(private val db: FirebaseFirestore) {
    fun saveUser(uid: String, userDto: UserDto, onComplete: (Boolean, Exception?) -> Unit) {
        db.collection("users").document(uid).set(userDto)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { exception -> onComplete(false, exception) }
    }
}
