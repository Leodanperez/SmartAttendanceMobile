package dev.leo.smartattendance.core.navigation

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object Register : NavRoutes("register")
    data object TeacherHome : NavRoutes("teacher_home")
    data object StudentHome : NavRoutes("student_home")
    data object Courses : NavRoutes("courses")
    data object CourseDetail : NavRoutes("course_detail")
    data object Sessions : NavRoutes("sessions")
    data object Reports : NavRoutes("reports")
    data object Profile : NavRoutes("profile")
}
