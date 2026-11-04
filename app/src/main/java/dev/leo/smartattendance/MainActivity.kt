package dev.leo.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.data.datasource.auth.FirebaseAuthDataSource
import dev.leo.smartattendance.data.datasource.firestore.FirebaseFirestoreDataSource
import dev.leo.smartattendance.data.repository.AuthRepositoryImpl
import dev.leo.smartattendance.data.repository.CourseRepositoryImpl
import dev.leo.smartattendance.domain.model.User
import dev.leo.smartattendance.domain.model.UserRole
import dev.leo.smartattendance.feature.auth.LoginScreen
import dev.leo.smartattendance.domain.model.Course
import dev.leo.smartattendance.domain.model.CourseSessionSummary
import dev.leo.smartattendance.domain.model.QrSession
import dev.leo.smartattendance.domain.model.TeacherActivity
import dev.leo.smartattendance.feature.coursedetail.CourseDetailScreen
import dev.leo.smartattendance.feature.courses.CoursesScreen
import dev.leo.smartattendance.feature.teacherhome.CreateSessionDialog
import dev.leo.smartattendance.feature.teacherhome.NewSessionDialog
import dev.leo.smartattendance.feature.profile.ProfileScreen
import dev.leo.smartattendance.feature.qrgenerator.TeacherQrDisplayScreen
import dev.leo.smartattendance.feature.qrscanner.QrScannerScreen
import dev.leo.smartattendance.feature.reports.ReportsScreen
import dev.leo.smartattendance.feature.studenthome.StudentHomeScreen
import dev.leo.smartattendance.feature.teacherhome.TeacherHomeScreen
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme
import dev.leo.smartattendance.ui.theme.ThemeController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Único splash: el del sistema (Android 12+) con nuestro tema (fondo de marca + escudo).
        // Al terminar entra directo al login/home, sin un segundo splash de Compose.
        installSplashScreen()
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val authDataSource = FirebaseAuthDataSource(auth)
        val firestoreDataSource = FirebaseFirestoreDataSource(db)
        val authRepository = AuthRepositoryImpl(authDataSource, firestoreDataSource)
        val courseRepository = CourseRepositoryImpl(firestoreDataSource)
        val themeController = ThemeController(applicationContext)

        enableEdgeToEdge()
        setContent {
            SmartAttendanceMobileTheme(mode = themeController.mode) {
                var currentUser by remember { mutableStateOf<User?>(null) }
                val scope = rememberCoroutineScope()

                var currentRoute by remember {
                    mutableStateOf<NavRoutes>(NavRoutes.Login)
                }

                // Estado del registro de asistencia por QR.
                var attendanceState by remember {
                    mutableStateOf<dev.leo.smartattendance.feature.qrscanner.AttendanceUiState>(
                        dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Idle
                    )
                }

                // Curso seleccionado para la pantalla de detalle y sus sesiones recientes.
                var selectedCourse by remember { mutableStateOf<Course?>(null) }
                var detailSessions by remember { mutableStateOf<List<CourseSessionSummary>>(emptyList()) }
                var loadingDetailSessions by remember { mutableStateOf(false) }
                // Ruta desde la que se abrió el detalle, para volver a ella con el botón atrás.
                var detailOrigin by remember { mutableStateOf<NavRoutes>(NavRoutes.StudentHome) }

                // --- Sesión + QR del docente ---
                // Sesión QR actualmente mostrada (creada desde el móvil).
                var teacherQrSession by remember { mutableStateOf<QrSession?>(null) }
                // Inscritos en el curso de la sesión mostrada (para el quórum del QR).
                var teacherQrEnrolled by remember { mutableIntStateOf(0) }
                // Diálogo de "nueva sesión".
                var showNewSessionDialog by remember { mutableStateOf(false) }
                var creatingSession by remember { mutableStateOf(false) }
                // Actividad reciente del docente.
                var teacherActivity by remember { mutableStateOf<List<TeacherActivity>>(emptyList()) }
                // Formulario de "crear sesión" desde el detalle del curso (docente).
                var showCreateSessionForm by remember { mutableStateOf(false) }

                // Check session & load user role from Firestore
                LaunchedEffect(auth.currentUser) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        scope.launch {
                            // Seed initial courses if empty
                            firestoreDataSource.seedInitialCoursesIfEmpty(firebaseUser.uid)

                            val userDto = firestoreDataSource.getUser(firebaseUser.uid)
                            if (userDto != null) {
                                val role = try {
                                    UserRole.valueOf(userDto.role.uppercase().trim())
                                } catch (e: Exception) {
                                    UserRole.STUDENT
                                }
                                currentUser = User(
                                    uid = firebaseUser.uid,
                                    firstName = userDto.firstName.ifEmpty { userDto.name.substringBefore(" ") },
                                    lastName = userDto.lastName.ifEmpty { userDto.name.substringAfter(" ", "") },
                                    email = firebaseUser.email ?: "",
                                    institutionalCode = userDto.institutionalCode,
                                    role = role,
                                    department = userDto.department
                                )
                                currentRoute = when (role) {
                                    UserRole.STUDENT -> NavRoutes.StudentHome
                                    UserRole.TEACHER -> NavRoutes.TeacherHome
                                    UserRole.ADMIN -> NavRoutes.TeacherHome
                                }
                            } else {
                                currentRoute = NavRoutes.StudentHome
                            }
                        }
                    } else {
                        currentRoute = NavRoutes.Login
                    }
                }

                run {
                    val effectiveRole = currentUser?.role ?: UserRole.STUDENT
                    val teacherCourses = courseRepository.getTeacherCoursesFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList()).value
                    val studentCourses = courseRepository.getStudentCoursesFlow(currentUser?.uid ?: "").collectAsState(initial = emptyList()).value

                    // Sesiones activas y vigentes en los cursos del estudiante (aviso en su Home).
                    val studentCourseIds = studentCourses.map { it.id }
                    val activeSessionsFlow = remember(studentCourseIds) {
                        courseRepository.getActiveSessionsForCoursesFlow(studentCourseIds)
                    }
                    val studentActiveSessions = activeSessionsFlow
                        .collectAsState(initial = emptyList()).value

                    // Sesión activa real del docente y su asistencia en vivo.
                    val teacherActiveSession = courseRepository
                        .getActiveSessionFlow(currentUser?.uid ?: "")
                        .collectAsState(initial = null).value
                    val teacherActiveLive = if (teacherActiveSession != null)
                        courseRepository.getLiveAttendancesFlow(teacherActiveSession.id)
                            .collectAsState(initial = emptyList()).value
                    else emptyList()
                    var teacherActiveEnrolled by remember { mutableIntStateOf(0) }

                    // Inscritos del curso de la sesión activa (para el quórum del panel).
                    LaunchedEffect(teacherActiveSession?.courseId) {
                        teacherActiveEnrolled = teacherActiveSession?.courseId?.let {
                            courseRepository.getEnrolledCount(it)
                        } ?: 0
                    }

                    // Roster en vivo de la sesión activa del docente (reporte de asistencia en vivo).
                    val rosterFlow = remember(teacherActiveSession?.id, teacherActiveSession?.courseId) {
                        val s = teacherActiveSession
                        if (s != null) courseRepository.getSessionRosterFlow(s.id, s.courseId)
                        else kotlinx.coroutines.flow.flowOf(emptyList())
                    }
                    val teacherRoster = rosterFlow.collectAsState(initial = emptyList()).value

                    // Actividad reciente del docente (se recarga cuando cambia la sesión activa).
                    LaunchedEffect(currentUser?.uid, teacherActiveSession?.id) {
                        val uid = currentUser?.uid
                        if (uid != null && (effectiveRole == UserRole.TEACHER || effectiveRole == UserRole.ADMIN)) {
                            teacherActivity = courseRepository.getTeacherRecentActivity(uid)
                        }
                    }

                    when (currentRoute) {
                        NavRoutes.Login -> {
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                LoginScreen(
                                    auth = auth,
                                    modifier = Modifier.padding(innerPadding),
                                    onLoginSuccess = {
                                        val firebaseUser = auth.currentUser
                                        if (firebaseUser != null) {
                                            scope.launch {
                                                firestoreDataSource.seedInitialCoursesIfEmpty(firebaseUser.uid)
                                                val userDto = firestoreDataSource.getUser(firebaseUser.uid)
                                                val role = try {
                                                    UserRole.valueOf((userDto?.role ?: "STUDENT").uppercase().trim())
                                                } catch (e: Exception) {
                                                    UserRole.STUDENT
                                                }
                                                currentUser = User(
                                                    uid = firebaseUser.uid,
                                                    email = firebaseUser.email ?: "",
                                                    role = role
                                                )
                                                currentRoute = when (role) {
                                                    UserRole.STUDENT -> NavRoutes.StudentHome
                                                    UserRole.TEACHER -> NavRoutes.TeacherHome
                                                    UserRole.ADMIN -> NavRoutes.TeacherHome
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        NavRoutes.TeacherHome -> {
                            TeacherHomeScreen(
                                userName = currentUser?.fullName?.ifEmpty { "Prof. Morales" } ?: "Prof. Morales",
                                userRole = currentUser?.role?.name ?: "DOCENTE",
                                courses = teacherCourses,
                                activeSession = teacherActiveSession,
                                activeEnrolledCount = teacherActiveEnrolled,
                                activePresentCount = teacherActiveLive.size,
                                recentActivity = teacherActivity,
                                currentRoute = currentRoute,
                                onNavigateToRoute = { route -> currentRoute = route },
                                onNewSessionClick = { showNewSessionDialog = true },
                                onOpenActiveQr = {
                                    val active = teacherActiveSession
                                    if (active != null) {
                                        // Abre el QR de la sesión activa; renueva token para tener uno vigente.
                                        creatingSession = true
                                        scope.launch {
                                            val rotated = courseRepository.rotateSessionToken(active.id, active.courseId)
                                            teacherQrEnrolled = courseRepository.getEnrolledCount(active.courseId)
                                            teacherQrSession = QrSession(
                                                sessionId = active.id,
                                                courseId = active.courseId,
                                                courseName = active.courseName,
                                                title = active.title,
                                                token = rotated?.token ?: "",
                                                tokenExpiresAtMillis = rotated?.tokenExpiresAtMillis
                                                    ?: (System.currentTimeMillis() + 60_000),
                                                startsAtMillis = active.startTime
                                            )
                                            creatingSession = false
                                            currentRoute = NavRoutes.Sessions
                                        }
                                    }
                                },
                                onLogout = {
                                    authRepository.logout()
                                    currentUser = null
                                    currentRoute = NavRoutes.Login
                                }
                            )
                        }

                        NavRoutes.StudentHome -> {
                            StudentHomeScreen(
                                studentName = currentUser?.fullName?.ifEmpty { "Ana López" } ?: "Ana López",
                                courses = studentCourses,
                                activeSessions = studentActiveSessions,
                                currentRoute = currentRoute,
                                onNavigateToRoute = { route -> currentRoute = route },
                                onEnrollStudent = { code ->
                                    scope.launch {
                                        val result = courseRepository.enrollStudentByCode(code, currentUser?.uid ?: "")
                                        val msg = if (result.isSuccess) "Te inscribiste correctamente."
                                            else result.exceptionOrNull()?.message ?: "No se pudo inscribir."
                                        android.widget.Toast.makeText(
                                            this@MainActivity, msg, android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                onCourseClick = { course ->
                                    selectedCourse = course
                                    detailSessions = emptyList()
                                    loadingDetailSessions = true
                                    detailOrigin = NavRoutes.StudentHome
                                    currentRoute = NavRoutes.CourseDetail
                                    scope.launch {
                                        detailSessions = courseRepository.getRecentCourseSessions(
                                            course.id,
                                            currentUser?.uid ?: ""
                                        )
                                        loadingDetailSessions = false
                                    }
                                }
                            )
                        }

                        NavRoutes.CourseDetail -> {
                            val course = selectedCourse
                            if (course == null) {
                                currentRoute = NavRoutes.StudentHome
                            } else {
                                val teacherView = effectiveRole == UserRole.TEACHER || effectiveRole == UserRole.ADMIN
                                CourseDetailScreen(
                                    course = course,
                                    sessions = detailSessions,
                                    isLoadingSessions = loadingDetailSessions,
                                    isTeacher = teacherView,
                                    onCreateSession = { showCreateSessionForm = true },
                                    onNavigateBack = { currentRoute = detailOrigin }
                                )
                            }
                        }

                        NavRoutes.Sessions -> {
                            if (effectiveRole == UserRole.TEACHER || effectiveRole == UserRole.ADMIN) {
                                val qr = teacherQrSession
                                if (qr != null) {
                                    TeacherQrDisplayScreen(
                                        session = qr,
                                        enrolledCount = teacherQrEnrolled,
                                        courseRepository = courseRepository,
                                        onBackToHome = {
                                            // No cierra la sesión: el QR sigue activo y se reabre desde Inicio.
                                            currentRoute = NavRoutes.TeacherHome
                                        },
                                        onCloseSession = {
                                            teacherQrSession = null
                                            currentRoute = NavRoutes.TeacherHome
                                        }
                                    )
                                } else {
                                    // Llegó a Sesiones sin una sesión abierta: vuelve al inicio y ofrece crear una.
                                    LaunchedEffect(Unit) {
                                        currentRoute = NavRoutes.TeacherHome
                                        showNewSessionDialog = true
                                    }
                                }
                            } else {
                                QrScannerScreen(
                                    activeCourseName = studentCourses.firstOrNull()?.name ?: "Desarrollo de Sistemas",
                                    activeTeacherName = studentCourses.firstOrNull()?.teacherName ?: "Prof. Morales",
                                    registrationState = attendanceState,
                                    onNavigateBack = {
                                        attendanceState = dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Idle
                                        currentRoute = NavRoutes.StudentHome
                                    },
                                    onRegisterAttendance = { payload ->
                                        attendanceState = dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Loading
                                        scope.launch {
                                            val user = currentUser
                                            if (user == null) {
                                                attendanceState = dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Error(
                                                    "Tu sesión expiró. Vuelve a iniciar sesión."
                                                )
                                                return@launch
                                            }
                                            val attendance = dev.leo.smartattendance.domain.model.Attendance(
                                                sessionId = payload.sessionId,
                                                courseId = payload.courseId,
                                                courseName = payload.courseName,
                                                studentId = user.uid,
                                                studentName = user.fullName,
                                                institutionalCode = user.institutionalCode,
                                                status = dev.leo.smartattendance.domain.model.AttendanceStatus.PRESENT
                                            )
                                            val result = courseRepository.registerAttendance(attendance, payload.rawToken)
                                            val error = result.exceptionOrNull()
                                            attendanceState = when {
                                                result.isSuccess ->
                                                    dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Success(payload)
                                                error is dev.leo.smartattendance.data.datasource.firestore.AlreadyRegisteredException ->
                                                    dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.AlreadyRegistered
                                                else ->
                                                    dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Error(
                                                        error?.message ?: "No se pudo registrar la asistencia."
                                                    )
                                            }
                                        }
                                    },
                                    onResultDismissed = {
                                        attendanceState = dev.leo.smartattendance.feature.qrscanner.AttendanceUiState.Idle
                                    }
                                )
                            }
                        }

                        NavRoutes.Courses,
                        NavRoutes.Reports,
                        NavRoutes.Profile -> {
                            val screenTitle = when (currentRoute) {
                                NavRoutes.Courses -> if (effectiveRole == UserRole.STUDENT) "Mis Cursos" else "Cursos"
                                NavRoutes.Reports -> if (effectiveRole == UserRole.STUDENT) "Historial" else "Reportes"
                                NavRoutes.Profile -> "Perfil"
                                else -> "SmartAttendance"
                            }

                            Scaffold(
                                topBar = {
                                    SmartAttendanceHeader(
                                        title = screenTitle,
                                        subtitle = "SmartAttendance M3",
                                        badgeText = effectiveRole.name,
                                        onProfileClick = { currentRoute = NavRoutes.Profile }
                                    )
                                },
                                bottomBar = {
                                    SmartAttendanceBottomBar(
                                        currentRoute = currentRoute,
                                        userRole = effectiveRole,
                                        onTabSelected = { route -> currentRoute = route }
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentRoute) {
                                        NavRoutes.Courses -> CoursesScreen(
                                            courses = if (effectiveRole == UserRole.STUDENT) studentCourses else teacherCourses,
                                            onCourseClick = { course ->
                                                selectedCourse = course
                                                detailSessions = emptyList()
                                                loadingDetailSessions = true
                                                detailOrigin = NavRoutes.Courses
                                                currentRoute = NavRoutes.CourseDetail
                                                scope.launch {
                                                    detailSessions = courseRepository.getRecentCourseSessions(
                                                        course.id,
                                                        currentUser?.uid ?: ""
                                                    )
                                                    loadingDetailSessions = false
                                                }
                                            }
                                        )
                                        NavRoutes.Reports ->
                                            if (effectiveRole == UserRole.STUDENT) {
                                                ReportsScreen()
                                            } else {
                                                ReportsScreen(
                                                    activeSession = teacherActiveSession,
                                                    roster = teacherRoster,
                                                )
                                            }
                                        NavRoutes.Profile -> ProfileScreen(
                                            user = currentUser ?: User(role = effectiveRole),
                                            themeMode = themeController.mode,
                                            onThemeChange = { themeController.updateMode(it) },
                                            onLogout = {
                                                authRepository.logout()
                                                currentUser = null
                                                currentRoute = NavRoutes.Login
                                            }
                                        )
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }

                    // Diálogo "nueva sesión": elegir curso -> abre el formulario de creación.
                    if (showNewSessionDialog) {
                        NewSessionDialog(
                            courses = teacherCourses,
                            onDismiss = { showNewSessionDialog = false },
                            onCourseSelected = { course ->
                                selectedCourse = course
                                showNewSessionDialog = false
                                showCreateSessionForm = true
                            }
                        )
                    }

                    // Formulario de creación de sesión (mismos campos que el panel web).
                    val formCourse = selectedCourse
                    if (showCreateSessionForm && formCourse != null) {
                        CreateSessionDialog(
                            course = formCourse,
                            submitting = creatingSession,
                            onDismiss = { if (!creatingSession) showCreateSessionForm = false },
                            onCreate = { data ->
                                if (creatingSession) return@CreateSessionDialog
                                creatingSession = true
                                scope.launch {
                                    val created = courseRepository.createSessionWithToken(
                                        courseId = formCourse.id,
                                        courseName = formCourse.name,
                                        title = data.title,
                                        teacherId = currentUser?.uid ?: "",
                                        startsAtMillis = data.startsAtMillis,
                                        durationMinutes = data.durationMinutes,
                                        lateAfterMinutes = data.lateAfterMinutes
                                    )
                                    creatingSession = false
                                    showCreateSessionForm = false
                                    if (created != null) {
                                        teacherQrEnrolled = courseRepository.getEnrolledCount(formCourse.id)
                                        teacherQrSession = created
                                        currentRoute = NavRoutes.Sessions
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
