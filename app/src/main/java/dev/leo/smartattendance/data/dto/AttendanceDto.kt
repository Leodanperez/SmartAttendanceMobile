package dev.leo.smartattendance.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Documento attendances/{sessionId_studentId}.
 * Contrato compartido con el panel web y con firestore.rules:
 * studentCode (no institutionalCode), registeredAt = Timestamp del servidor, tokenId, source.
 */
data class AttendanceDto(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    @get:PropertyName("sessionId") @set:PropertyName("sessionId")
    var sessionId: String = "",
    @get:PropertyName("courseId") @set:PropertyName("courseId")
    var courseId: String = "",
    @get:PropertyName("studentId") @set:PropertyName("studentId")
    var studentId: String = "",
    @get:PropertyName("studentName") @set:PropertyName("studentName")
    var studentName: String = "",
    @get:PropertyName("studentCode") @set:PropertyName("studentCode")
    var studentCode: String? = null,
    @get:PropertyName("status") @set:PropertyName("status")
    var status: String = "PRESENT",
    @get:PropertyName("source") @set:PropertyName("source")
    var source: String = "QR",
    @get:PropertyName("tokenId") @set:PropertyName("tokenId")
    var tokenId: String? = null,
    // Timestamp del servidor. Lo escribe el DataSource con FieldValue.serverTimestamp().
    @get:PropertyName("registeredAt") @set:PropertyName("registeredAt")
    var registeredAt: Timestamp? = null,
    @get:PropertyName("updatedBy") @set:PropertyName("updatedBy")
    var updatedBy: String? = null,
    @get:PropertyName("updateReason") @set:PropertyName("updateReason")
    var updateReason: String? = null
)
