package dev.leo.smartattendance.data.datasource.firestore

import com.google.firebase.firestore.FirebaseFirestore
import dev.leo.smartattendance.data.dto.AttendanceDto
import dev.leo.smartattendance.data.dto.CourseDto
import dev.leo.smartattendance.data.dto.EnrollmentDto
import dev.leo.smartattendance.data.dto.SessionDto
import dev.leo.smartattendance.data.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/** El estudiante ya tiene asistencia en esta sesión. No tiene sentido reintentar. */
class AlreadyRegisteredException : Exception("Ya registraste tu asistencia en esta sesión")

/** Fila de sesión reciente para el detalle del curso del estudiante. */
data class RecentSessionRow(
    val id: String,
    val title: String,
    val status: String,
    val startsAtMillis: Long?,
    /** Estado del registro del estudiante en esta sesión (PRESENT/LATE/…), o null si no registró. */
    val myStatus: String?
)

class FirebaseFirestoreDataSource(private val db: FirebaseFirestore) {

    // Cache de nombres de docente por uid (evita releer users en cada snapshot).
    private val teacherNameCache = mutableMapOf<String, String>()

    /** Nombre del docente desde users/{uid}. Vacío si no se puede leer. */
    private suspend fun resolveTeacherName(teacherId: String): String {
        teacherNameCache[teacherId]?.let { return it }
        return try {
            val user = db.collection("users").document(teacherId).get().await()
            val first = user.getString("firstName") ?: ""
            val last = user.getString("lastName") ?: ""
            val name = "$first $last".trim().ifBlank {
                user.getString("displayName") ?: user.getString("name") ?: ""
            }
            if (name.isNotBlank()) teacherNameCache[teacherId] = name
            name
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Porcentaje de asistencia del estudiante en un curso (misma regla que el panel web):
     *   % = (presentes + tardanzas) / (sesiones CERRADAS del curso − justificadas del estudiante)
     * Devuelve -1 si aún no hay sesiones cerradas (sin datos).
     */
    private suspend fun computeAttendanceRate(courseId: String, studentId: String): Int {
        return try {
            // Sesiones cerradas del curso.
            val closedSessions = db.collection("sessions")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("status", "CLOSED")
                .get()
                .await()
            val closedCount = closedSessions.size()
            if (closedCount == 0) return -1

            // Asistencias del estudiante en este curso.
            val myAttendances = db.collection("attendances")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            var attended = 0   // PRESENT o LATE
            var justified = 0  // JUSTIFIED (se excluye del total)
            for (doc in myAttendances.documents) {
                when (doc.getString("status")?.uppercase()) {
                    "PRESENT", "LATE" -> attended++
                    "JUSTIFIED" -> justified++
                }
            }

            val denominator = closedCount - justified
            if (denominator <= 0) return -1
            ((attended.toDouble() / denominator) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Sesiones recientes de un curso (máx. [limit]) con el estado del registro del estudiante.
     * Lee startsAt de forma tolerante: el panel web lo escribe como Timestamp y la app lo trataba
     * como Long, así que aquí se admiten ambos. Las sesiones se ordenan por fecha descendente.
     */
    suspend fun getRecentCourseSessions(
        courseId: String,
        studentId: String,
        limit: Int = 8
    ): List<RecentSessionRow> {
        return try {
            val sessionsSnap = db.collection("sessions")
                .whereEqualTo("courseId", courseId)
                .get()
                .await()

            // Registros del estudiante en el curso, indexados por sessionId.
            val myAttendance = db.collection("attendances")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            val statusBySession = myAttendance.documents.associate { doc ->
                (doc.getString("sessionId") ?: "") to (doc.getString("status")?.uppercase() ?: "")
            }

            sessionsSnap.documents.map { doc ->
                val startsAt: Long? = when (val raw = doc.get("startsAt")) {
                    is com.google.firebase.Timestamp -> raw.toDate().time
                    is Number -> raw.toLong()
                    else -> null
                }
                RecentSessionRow(
                    id = doc.id,
                    title = doc.getString("title")?.ifBlank { "Sesión" } ?: "Sesión",
                    status = doc.getString("status")?.uppercase() ?: "ACTIVE",
                    startsAtMillis = startsAt,
                    myStatus = statusBySession[doc.id]?.takeIf { it.isNotBlank() }
                )
            }.sortedByDescending { it.startsAtMillis ?: 0L }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- USER OPERATIONS ---
    suspend fun getUser(uid: String): UserDto? {
        return try {
            val snapshot = db.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserDto::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveUser(uid: String, userDto: UserDto): Boolean {
        return try {
            db.collection("users").document(uid).set(userDto).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- COURSES OPERATIONS ---
    fun getTeacherCoursesFlow(teacherId: String): Flow<List<CourseDto>> = callbackFlow {
        val listener = db.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CourseDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                val filtered = list.filter { course ->
                    course.teacherIds.isEmpty() || course.teacherIds.contains(teacherId)
                }
                trySend(filtered)
            }
        awaitClose { listener.remove() }
    }

    fun getStudentEnrolledCoursesFlow(studentId: String): Flow<List<CourseDto>> = callbackFlow {
        if (studentId.isBlank()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val enrollmentListener = db.collection("enrollments")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { enrollmentSnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val enrolledCourseIds = enrollmentSnapshot?.documents?.mapNotNull { doc ->
                    doc.getString("courseId")
                }?.distinct() ?: emptyList()

                if (enrolledCourseIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    db.collection("courses")
                        .addSnapshotListener { courseSnapshot, courseError ->
                            if (courseError != null) {
                                close(courseError)
                                return@addSnapshotListener
                            }

                            val enrolledCourses = courseSnapshot?.documents?.mapNotNull { doc ->
                                if (enrolledCourseIds.contains(doc.id)) {
                                    doc.toObject(CourseDto::class.java)?.copy(id = doc.id)
                                } else null
                            } ?: emptyList()

                            // Se enriquece cada curso: nombre del docente (users/{uid}) y
                            // porcentaje de asistencia real del estudiante.
                            launch {
                                val resolved = enrolledCourses.map { c ->
                                    val teacher = if (c.teacherName.isBlank() && c.teacherIds.isNotEmpty()) {
                                        resolveTeacherName(c.teacherIds.first())
                                    } else c.teacherName
                                    c.copy(
                                        teacherName = teacher,
                                        attendanceRate = computeAttendanceRate(c.id, studentId)
                                    )
                                }
                                trySend(resolved)
                            }
                        }
                }
            }

        awaitClose { enrollmentListener.remove() }
    }

    fun getAllCoursesFlow(): Flow<List<CourseDto>> = callbackFlow {
        val listener = db.collection("courses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CourseDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun seedInitialCoursesIfEmpty(teacherId: String) {
        try {
            val snapshot = db.collection("courses").get().await()
            if (snapshot.isEmpty) {
                val initialCourses = listOf(
                    CourseDto(
                        id = "c1",
                        name = "Algoritmos y Estructuras de Datos",
                        description = "Curso de estructuras de datos complejas",
                        courseCode = "CS201",
                        section = "Lab 4",
                        teacherIds = listOf(teacherId),
                        teacherName = "Prof. Morales",
                        studentCount = 28,
                        careerName = "Desarrollo de Sistemas de Información",
                        semester = 3,
                        academicPeriodName = "2026-II",
                        schedule = listOf(
                            mapOf("day" to "MON", "start" to "08:00", "end" to "10:00"),
                            mapOf("day" to "WED", "start" to "08:00", "end" to "10:00")
                        )
                    ),
                    CourseDto(
                        id = "c2",
                        name = "Bases de Datos Avanzadas",
                        description = "Modelado relacional y NoSQL",
                        courseCode = "CS304",
                        section = "Aula 102",
                        teacherIds = listOf(teacherId),
                        teacherName = "Prof. Morales",
                        studentCount = 32,
                        careerName = "Desarrollo de Sistemas de Información",
                        semester = 4,
                        academicPeriodName = "2026-II",
                        schedule = listOf(
                            mapOf("day" to "TUE", "start" to "10:00", "end" to "12:00"),
                            mapOf("day" to "THU", "start" to "10:00", "end" to "12:00")
                        )
                    ),
                    CourseDto(
                        id = "c3",
                        name = "Inteligencia Artificial",
                        description = "Redes neuronales y machine learning",
                        courseCode = "CS402",
                        section = "Pab. Robótica",
                        teacherIds = listOf(teacherId),
                        teacherName = "Prof. Morales",
                        studentCount = 22,
                        careerName = "Desarrollo de Sistemas de Información",
                        semester = 5,
                        academicPeriodName = "2026-II",
                        schedule = listOf(
                            mapOf("day" to "FRI", "start" to "14:00", "end" to "18:00")
                        )
                    )
                )
                for (course in initialCourses) {
                    db.collection("courses").document(course.id).set(course).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- ENROLLMENT OPERATIONS ---
    suspend fun enrollStudentByCode(code: String, studentId: String): Result<Boolean> {
        return try {
            val query = db.collection("courses")
                .whereEqualTo("courseCode", code)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("No se encontró ningún curso con el código $code"))
            }

            val courseDoc = query.documents.first()
            val courseId = courseDoc.id
            val enrollmentId = "${courseId}_$studentId"

            val enrollmentDto = EnrollmentDto(
                id = enrollmentId,
                courseId = courseId,
                studentId = studentId,
                status = "ACTIVE"
            )

            db.collection("enrollments").document(enrollmentId).set(enrollmentDto).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SESSION OPERATIONS ---
    fun getActiveSessionFlow(teacherId: String): Flow<SessionDto?> = callbackFlow {
        val listener = db.collection("sessions")
            .whereEqualTo("teacherId", teacherId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val activeDoc = snapshot?.documents?.firstOrNull()
                val session = activeDoc?.toObject(SessionDto::class.java)?.copy(id = activeDoc.id)
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createSession(sessionDto: SessionDto): String {
        return try {
            val docRef = if (sessionDto.id.isNotBlank()) {
                db.collection("sessions").document(sessionDto.id)
            } else {
                db.collection("sessions").document()
            }
            val finalDto = sessionDto.copy(id = docRef.id)
            docRef.set(finalDto).await()
            docRef.id
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun closeSession(sessionId: String) {
        try {
            db.collection("sessions").document(sessionId)
                .update("status", "CLOSED")
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- ATTENDANCE OPERATIONS (REAL-TIME SNAPSHOT LISTENERS) ---
    fun getLiveAttendancesFlow(sessionId: String): Flow<List<AttendanceDto>> = callbackFlow {
        val listener = db.collection("attendances")
            .whereEqualTo("sessionId", sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val attendances = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AttendanceDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(attendances)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Registra la asistencia por QR (modo demo, sin Cloud Functions).
     * Busca el token válido por su hash, arma el documento con el Timestamp del servidor
     * y lo escribe con el id determinístico sessionId_studentId. Las reglas validan todo.
     *
     * @param rawToken el token en claro leído del QR.
     */
    suspend fun registerAttendance(attendanceDto: AttendanceDto, rawToken: String): Result<Boolean> {
        return try {
            val attendanceId = "${attendanceDto.sessionId}_${attendanceDto.studentId}"
            val docRef = db.collection("attendances").document(attendanceId)

            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                return Result.failure(AlreadyRegisteredException())
            }

            // Buscar el token vigente por su hash (mismo SHA-256 que usa el panel web).
            val tokenHash = sha256Hex(rawToken.trim())
            val tokenQuery = db.collection("sessionTokens")
                .whereEqualTo("tokenHash", tokenHash)
                .whereEqualTo("sessionId", attendanceDto.sessionId)
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get()
                .await()
            val tokenDoc = tokenQuery.documents.firstOrNull()
                ?: return Result.failure(Exception("El código QR no es válido o expiró"))

            val data = hashMapOf(
                "id" to attendanceId,
                "sessionId" to attendanceDto.sessionId,
                "courseId" to attendanceDto.courseId,
                "studentId" to attendanceDto.studentId,
                "studentName" to attendanceDto.studentName,
                "studentCode" to attendanceDto.studentCode,
                "status" to attendanceDto.status,
                "source" to "QR",
                "tokenId" to tokenDoc.id,
                "registeredAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "updatedBy" to null,
                "updateReason" to null
            )
            docRef.set(data).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sha256Hex(text: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getStudentAttendanceHistoryFlow(studentId: String): Flow<List<AttendanceDto>> = callbackFlow {
        val listener = db.collection("attendances")
            .whereEqualTo("studentId", studentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AttendanceDto::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}
