package dev.leo.smartattendance.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import dev.leo.smartattendance.feature.auth.LoginScreen as FeatureLoginScreen

@Composable
fun LoginScreen(
    auth: FirebaseAuth?,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    FeatureLoginScreen(
        auth = auth,
        modifier = modifier,
        onLoginSuccess = onLoginSuccess,
        onNavigateToRegister = onNavigateToRegister
    )
}
