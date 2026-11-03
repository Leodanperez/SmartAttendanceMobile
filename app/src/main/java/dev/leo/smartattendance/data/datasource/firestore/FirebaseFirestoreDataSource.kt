package dev.leo.smartattendance.data.datasource.firestore

import com.google.firebase.firestore.FirebaseFirestore
import dev.leo.smartattendance.data.dto.AttendanceDto
import dev.leo.smartattendance.data.dto.CourseDto
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

/** Resultado de crear una sesión con su primer token de QR. */
data class CreatedSession(
    val sessionId: String,
    val courseId: String,
    val token: String,
    val tokenExpiresAtMillis: Long,
    val startsAtMillis: Long
)

/** Resultado de renovar el token del QR. */
data class RotatedToken(
    val token: String,
    val tokenExpiresAtMillis: Long
)

/** Fila de actividad reciente del docente. */
data class TeacherActivityRow(
    val sessionId: String,
    val courseName: String,
    val title: String,
    val status: String,
    val startsAtMillis: Long?,
    val closedAtMillis: Long?,
    val presentCount: Int,
    val lateCount: Int
)

class FirebaseFirestoreDataSource(private val db: FirebaseFirestore) {

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

                // Solo los cursos asignados a este docente (debe estar en teacherIds).
                val filtered = list.filter { course ->
                    teacherId.isNotBlank() && course.teacherIds.contains(teacherId)
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

                            // El nombre del docente viene denormalizado en el curso (teacherName);
                            // el estudiante NO lee users/{uid} (las reglas ya no lo permiten).
                            // Solo se calcula el % de asistencia real del estudiante.
                            launch {
                                val resolved = enrolledCourses.map { c ->
                                    c.copy(attendanceRate = computeAttendanceRate(c.id, studentId))
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
                        section = "A",
                        room = "Lab 4",
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
                        section = "A",
                        room = "Aula 102",
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
                        section = "A",
                        room = "Pab. Robótica",
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
    /**
     * Autoinscripción del estudiante con un CÓDIGO DE INSCRIPCIÓN (modo demo, sin Cloud Functions).
     * El código lo genera el ADMIN en el panel; solo se guarda su hash SHA-256 en enrollmentCodes.
     * Aquí se busca por hash, se valida (ACTIVE, vigente, con cupo), se crea la inscripción con
     * source=CODE y codeId, y se incrementa 'uses' del código. Las reglas validan todo.
     */
    suspend fun enrollStudentByCode(code: String, studentId: String): Result<Boolean> {
        return try {
            if (studentId.isBlank()) {
                return Result.failure(Exception("Tu sesión expiró. Vuelve a iniciar sesión."))
            }
            val codeHash = sha256Hex(code.trim().uppercase())
            val codeQuery = db.collection("enrollmentCodes")
                .whereEqualTo("codeHash", codeHash)
                .whereEqualTo("status", "ACTIVE")
                .limit(1)
                .get()
                .await()
            val codeDoc = codeQuery.documents.firstOrNull()
                ?: return Result.failure(Exception("El código no es válido o fue deshabilitado."))

            // Validaciones locales (mensajes claros); las reglas las re-verifican al escribir.
            val expiresAt = when (val raw = codeDoc.get("expiresAt")) {
                is com.google.firebase.Timestamp -> raw.toDate().time
                is Number -> raw.toLong()
                else -> 0L
            }
            if (expiresAt in 1 until System.currentTimeMillis()) {
                return Result.failure(Exception("El código ya expiró."))
            }
            val uses = (codeDoc.getLong("uses") ?: 0L)
            val maxUses = (codeDoc.getLong("maxUses") ?: 0L)
            if (maxUses in 1..uses) {
                return Result.failure(Exception("El código alcanzó su número máximo de usos."))
            }
            val courseId = codeDoc.getString("courseId")
                ?: return Result.failure(Exception("El código no está asociado a un curso."))

            val enrollmentId = "${courseId}_$studentId"
            val enrollmentRef = db.collection("enrollments").document(enrollmentId)
            if (enrollmentRef.get().await().exists()) {
                return Result.failure(Exception("Ya estás inscrito en este curso."))
            }

            val enrollmentData = hashMapOf(
                "id" to enrollmentId,
                "courseId" to courseId,
                "studentId" to studentId,
                "status" to "ACTIVE",
                "source" to "CODE",
                "codeId" to codeDoc.id,
                "enrolledBy" to studentId,
                "enrolledAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            // Inscripción + incremento de 'uses' en un batch atómico.
            val batch = db.batch()
            batch.set(enrollmentRef, enrollmentData)
            batch.update(codeDoc.reference, "uses", uses + 1)
            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SESSION OPERATIONS ---
    /** Lee un campo de fecha tolerando Timestamp (panel web) o Long (app). */
    private fun readMillis(raw: Any?): Long = when (raw) {
        is com.google.firebase.Timestamp -> raw.toDate().time
        is Number -> raw.toLong()
        else -> 0L
    }

    /**
     * Sesiones ACTIVE que pertenecen a alguno de los cursos del estudiante y cuya ventana de
     * asistencia está vigente (startsAt <= ahora <= expiresAt). Se usa para avisar al alumno
     * en su Home que el docente inició una sesión y puede escanear el QR.
     */
    fun getActiveSessionsForCoursesFlow(courseIds: List<String>): Flow<List<SessionDto>> = callbackFlow {
        if (courseIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val idSet = courseIds.toSet()
        val listener = db.collection("sessions")
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val now = System.currentTimeMillis()
                val list = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val courseId = doc.getString("courseId") ?: return@mapNotNull null
                    if (courseId !in idSet) return@mapNotNull null
                    val startsAt = readMillis(doc.get("startsAt"))
                    val expiresAt = readMillis(doc.get("expiresAt"))
                    // Solo sesiones cuya ventana está vigente ahora mismo.
                    if (startsAt in 1..now && (expiresAt == 0L || expiresAt >= now)) {
                        SessionDto(
                            id = doc.id,
                            courseId = courseId,
                            courseName = doc.getString("courseName") ?: "",
                            title = doc.getString("title") ?: "",
                            room = doc.getString("room") ?: "",
                            teacherId = doc.getString("teacherId") ?: "",
                            status = "ACTIVE",
                            token = "",
                            startsAt = startsAt,
                            expiresAt = expiresAt
                        )
                    } else null
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

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
                // Lectura manual: startsAt/expiresAt pueden venir como Timestamp (web) o Long (app).
                val session = activeDoc?.let { doc ->
                    SessionDto(
                        id = doc.id,
                        courseId = doc.getString("courseId") ?: "",
                        courseName = doc.getString("courseName") ?: "",
                        title = doc.getString("title") ?: "",
                        room = doc.getString("room") ?: "",
                        teacherId = doc.getString("teacherId") ?: "",
                        status = doc.getString("status") ?: "ACTIVE",
                        token = doc.getString("token") ?: "",
                        startsAt = readMillis(doc.get("startsAt")),
                        expiresAt = readMillis(doc.get("expiresAt"))
                    )
                }
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
            // Al cerrar, invalida también los tokens activos del QR.
            db.collection("sessions").document(sessionId)
                .update(
                    mapOf(
                        "status" to "CLOSED",
                        "closedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                )
                .await()
            revokeActiveTokens(sessionId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cierra la sesión y marca ABSENT (falta) a los inscritos ACTIVE del curso que no registraron.
     * Modo demo sin Cloud Functions: lo ejecuta el docente. Cada falta lleva motivo para cumplir las reglas.
     * Devuelve cuántas faltas se registraron.
     */
    suspend fun closeSessionAndMarkAbsent(sessionId: String, courseId: String): Int {
        return try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Inscritos ACTIVE del curso.
            val enrolled = db.collection("enrollments")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("status", "ACTIVE")
                .get().await()
                .documents.mapNotNull { it.getString("studentId") }

            // Estudiantes que ya tienen registro en esta sesión.
            val registered = db.collection("attendances")
                .whereEqualTo("sessionId", sessionId)
                .get().await()
                .documents.mapNotNull { it.getString("studentId") }
                .toSet()

            val absentees = enrolled.filter { it !in registered }

            val batch = db.batch()
            // Cerrar la sesión.
            batch.update(
                db.collection("sessions").document(sessionId),
                mapOf(
                    "status" to "CLOSED",
                    "closedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            // Marcar falta a cada inscrito sin registro.
            for (studentId in absentees) {
                val id = "${sessionId}_$studentId"
                val data = hashMapOf(
                    "id" to id,
                    "sessionId" to sessionId,
                    "courseId" to courseId,
                    "studentId" to studentId,
                    "status" to "ABSENT",
                    "source" to "MANUAL",
                    "registeredAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedBy" to uid,
                    "updateReason" to "Cierre de sesión: sin registro de asistencia.",
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                batch.set(db.collection("attendances").document(id), data)
            }
            batch.commit().await()
            revokeActiveTokens(sessionId)
            absentees.size
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: al menos cerrar la sesión.
            closeSession(sessionId)
            0
        }
    }

    /**
     * Crea una sesión ACTIVE para el docente y genera el primer token del QR (igual que el panel web).
     * Guarda solo el hash SHA-256 del token en sessionTokens; el token en claro vuelve una vez para
     * dibujar el QR. Devuelve null si algo falla.
     *
     * @param durationMinutes duración total de la sesión.
     * @param qrSeconds vigencia del token del QR (se renueva periódicamente desde la UI).
     */
    suspend fun createSessionWithToken(
        courseId: String,
        courseName: String,
        title: String,
        teacherId: String,
        startsAtMillis: Long = System.currentTimeMillis(),
        durationMinutes: Int = 90,
        lateAfterMinutes: Int? = null,
        qrSeconds: Int = 60
    ): CreatedSession? {
        return try {
            val start = startsAtMillis
            val sessionRef = db.collection("sessions").document()
            val sessionId = sessionRef.id
            val lateAfter = lateAfterMinutes?.let {
                com.google.firebase.Timestamp(java.util.Date(start + it * 60_000L))
            }
            val sessionData = hashMapOf(
                "id" to sessionId,
                "courseId" to courseId,
                "courseName" to courseName,
                "title" to title,
                "teacherId" to teacherId,
                "status" to "ACTIVE",
                "startsAt" to com.google.firebase.Timestamp(java.util.Date(start)),
                "expiresAt" to com.google.firebase.Timestamp(java.util.Date(start + durationMinutes * 60_000L)),
                "lateAfter" to lateAfter,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "closedAt" to null
            )
            sessionRef.set(sessionData).await()

            val token = generateToken()
            val tokenExpiresAt = writeToken(sessionId, courseId, token, qrSeconds, teacherId)
            CreatedSession(
                sessionId = sessionId,
                courseId = courseId,
                token = token,
                tokenExpiresAtMillis = tokenExpiresAt,
                startsAtMillis = start
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renueva el token del QR de una sesión: expira los activos y crea uno nuevo.
     * Devuelve el token en claro y su expiración, o null si falla.
     */
    suspend fun rotateSessionToken(
        sessionId: String,
        courseId: String,
        qrSeconds: Int = 60
    ): RotatedToken? {
        return try {
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            revokeActiveTokens(sessionId)
            val token = generateToken()
            val expiresAt = writeToken(sessionId, courseId, token, qrSeconds, uid)
            RotatedToken(token = token, tokenExpiresAtMillis = expiresAt)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Escribe un token (solo su hash) en sessionTokens y devuelve su expiración en millis.
     * createdBy debe ser el uid del docente (lo exigen las reglas de Firestore).
     */
    private suspend fun writeToken(sessionId: String, courseId: String, token: String, qrSeconds: Int, createdBy: String): Long {
        val expiresAt = System.currentTimeMillis() + qrSeconds * 1000L
        val data = hashMapOf(
            "sessionId" to sessionId,
            "courseId" to courseId,
            "tokenHash" to sha256Hex(token),
            "status" to "ACTIVE",
            "expiresAt" to com.google.firebase.Timestamp(java.util.Date(expiresAt)),
            "createdBy" to createdBy,
            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )
        db.collection("sessionTokens").add(data).await()
        return expiresAt
    }

    /** Revoca (status REVOKED) los tokens ACTIVE de una sesión. Las reglas solo permiten este cambio. */
    private suspend fun revokeActiveTokens(sessionId: String) {
        try {
            val active = db.collection("sessionTokens")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            for (doc in active.documents) {
                doc.reference.update("status", "REVOKED").await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Token aleatorio de 10 caracteres (A-Z0-9), mismo espacio que randomCode del web. */
    private fun generateToken(): String {
        val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val rnd = java.security.SecureRandom()
        return (1..10).map { alphabet[rnd.nextInt(alphabet.length)] }.joinToString("")
    }

    /** Nº de inscritos ACTIVE en un curso (para el quórum de la sesión). */
    suspend fun getEnrolledCount(courseId: String): Int {
        return try {
            db.collection("enrollments")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Sesiones recientes del docente con un resumen (presentes/tardanzas) de cada una.
     * Se usa para "Actividad reciente" del panel del docente.
     */
    suspend fun getTeacherRecentActivity(teacherId: String, limit: Int = 6): List<TeacherActivityRow> {
        return try {
            val sessionsSnap = db.collection("sessions")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()

            val rows = sessionsSnap.documents.map { doc ->
                val startsAt: Long? = when (val raw = doc.get("startsAt")) {
                    is com.google.firebase.Timestamp -> raw.toDate().time
                    is Number -> raw.toLong()
                    else -> null
                }
                val closedAt: Long? = when (val raw = doc.get("closedAt")) {
                    is com.google.firebase.Timestamp -> raw.toDate().time
                    is Number -> raw.toLong()
                    else -> null
                }
                val sessionId = doc.id
                // Resumen de asistencia de la sesión.
                var present = 0
                var late = 0
                try {
                    val att = db.collection("attendances")
                        .whereEqualTo("sessionId", sessionId)
                        .get()
                        .await()
                    for (a in att.documents) {
                        when (a.getString("status")?.uppercase()) {
                            "PRESENT" -> present++
                            "LATE" -> late++
                        }
                    }
                } catch (_: Exception) { }
                TeacherActivityRow(
                    sessionId = sessionId,
                    courseName = doc.getString("courseName")?.ifBlank { "Curso" } ?: "Curso",
                    title = doc.getString("title")?.ifBlank { "Sesión" } ?: "Sesión",
                    status = doc.getString("status")?.uppercase() ?: "ACTIVE",
                    startsAtMillis = startsAt,
                    closedAtMillis = closedAt,
                    presentCount = present,
                    lateCount = late
                )
            }
            rows.sortedByDescending { (it.closedAtMillis ?: it.startsAtMillis) ?: 0L }.take(limit)
        } catch (e: Exception) {
            emptyList()
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

            // Validar la ventana de la sesión para dar mensajes claros (las reglas lo re-verifican).
            val sessionDoc = db.collection("sessions").document(attendanceDto.sessionId).get().await()
            if (!sessionDoc.exists()) {
                return Result.failure(Exception("La sesión no existe."))
            }
            val sessionStatus = sessionDoc.getString("status")?.uppercase() ?: "ACTIVE"
            val startsAt = readMillis(sessionDoc.get("startsAt"))
            val expiresAt = readMillis(sessionDoc.get("expiresAt"))
            val lateAfter = readMillis(sessionDoc.get("lateAfter"))
            val now = System.currentTimeMillis()

            if (sessionStatus == "CLOSED") {
                return Result.failure(Exception("La sesión ya fue cerrada por el docente."))
            }
            if (startsAt > 0 && now < startsAt) {
                val hhmm = java.text.SimpleDateFormat("HH:mm", java.util.Locale.forLanguageTag("es"))
                    .format(java.util.Date(startsAt))
                return Result.failure(Exception("La sesión aún no ha iniciado. Empieza a las $hhmm."))
            }
            if (expiresAt > 0 && now > expiresAt) {
                val finHhmm = java.text.SimpleDateFormat("HH:mm", java.util.Locale.forLanguageTag("es"))
                    .format(java.util.Date(expiresAt))
                return Result.failure(
                    Exception("Se venció la hora permitida para registrar asistencia (cerró a las $finHhmm).")
                )
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
                ?: return Result.failure(Exception("El código QR no es válido o expiró. Pide al docente que lo renueve."))

            // PRESENT por defecto; LATE si ya pasó el umbral de tardanza (lateAfter).
            val status = if (lateAfter > 0 && now > lateAfter) "LATE" else "PRESENT"

            val data = hashMapOf(
                "id" to attendanceId,
                "sessionId" to attendanceDto.sessionId,
                "courseId" to attendanceDto.courseId,
                "studentId" to attendanceDto.studentId,
                "studentName" to attendanceDto.studentName,
                "studentCode" to attendanceDto.studentCode,
                "status" to status,
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
