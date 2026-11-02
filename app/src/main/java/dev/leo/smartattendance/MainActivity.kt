package dev.leo.smartattendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.leo.smartattendance.core.designsystem.SmartAttendanceBottomBar
import dev.leo.smartattendance.core.designsystem.SmartAttendanceHeader
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.feature.auth.LoginScreen
import dev.leo.smartattendance.feature.auth.SignUpScreen
import dev.leo.smartattendance.feature.courses.CoursesScreen
import dev.leo.smartattendance.feature.profile.ProfileScreen
import dev.leo.smartattendance.feature.qrscanner.QrScannerScreen
import dev.leo.smartattendance.feature.reports.ReportsScreen
import dev.leo.smartattendance.feature.teacherhome.TeacherHomeScreen
import dev.leo.smartattendance.ui.theme.SmartAttendanceMobileTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        enableEdgeToEdge()
        setContent {
            SmartAttendanceMobileTheme {
                var currentRoute by remember {
                    mutableStateOf<NavRoutes>(
                        if (auth.currentUser != null) NavRoutes.TeacherHome else NavRoutes.Login
                    )
                }

                when (currentRoute) {
                    NavRoutes.Login -> {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            LoginScreen(
                                auth = auth,
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = {
                                    currentRoute = NavRoutes.TeacherHome
                                },
                                onNavigateToRegister = {
                                    currentRoute = NavRoutes.Register
                                }
                            )
                        }
                    }

                    NavRoutes.Register -> {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            SignUpScreen(
                                auth = auth,
                                db = db,
                                modifier = Modifier.padding(innerPadding),
                                onNavigateToLogin = {
                                    currentRoute = NavRoutes.Login
                                }
                            )
                        }
                    }

                    NavRoutes.TeacherHome -> {
                        TeacherHomeScreen(
                            userName = auth.currentUser?.displayName ?: "Prof. Morales",
                            userRole = "Docente",
                            currentRoute = currentRoute,
                            onNavigateToRoute = { route -> currentRoute = route },
                            onLogout = {
                                auth.signOut()
                                currentRoute = NavRoutes.Login
                            }
                        )
                    }

                    NavRoutes.Courses,
                    NavRoutes.Sessions,
                    NavRoutes.Reports,
                    NavRoutes.Profile -> {
                        val screenTitle = when (currentRoute) {
                            NavRoutes.Courses -> "Mis Cursos"
                            NavRoutes.Sessions -> "Sesiones"
                            NavRoutes.Reports -> "Reportes"
                            NavRoutes.Profile -> "Perfil"
                            else -> "SmartAttendance"
                        }

                        Scaffold(
                            topBar = {
                                SmartAttendanceHeader(
                                    title = screenTitle,
                                    subtitle = "SmartAttendance M3",
                                    badgeText = "DOCENTE",
                                    onProfileClick = { currentRoute = NavRoutes.Profile }
                                )
                            },
                            bottomBar = {
                                SmartAttendanceBottomBar(
                                    currentRoute = currentRoute,
                                    onTabSelected = { route -> currentRoute = route }
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { innerPadding ->
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentRoute) {
                                    NavRoutes.Courses -> CoursesScreen()
                                    NavRoutes.Sessions -> QrScannerScreen()
                                    NavRoutes.Reports -> ReportsScreen()
                                    NavRoutes.Profile -> ProfileScreen()
                                    else -> {}
                                }
                            }
                        }
                    }

                    NavRoutes.StudentHome -> {
                        TeacherHomeScreen(
                            currentRoute = currentRoute,
                            onNavigateToRoute = { route -> currentRoute = route }
                        )
                    }
                }
            }
        }
    }
}
