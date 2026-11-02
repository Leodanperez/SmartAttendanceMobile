package dev.leo.smartattendance.core.common

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
