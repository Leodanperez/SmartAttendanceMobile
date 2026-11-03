package dev.leo.smartattendance.core.designsystem
import dev.leo.smartattendance.ui.theme.AppTheme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.leo.smartattendance.core.navigation.NavRoutes
import dev.leo.smartattendance.domain.model.UserRole

data class NavigationTabItem(
    val route: NavRoutes,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val hasBadge: Boolean = false
)

fun getNavigationTabsForRole(role: UserRole): List<NavigationTabItem> {
    return when (role) {
        UserRole.STUDENT -> listOf(
            NavigationTabItem(
                route = NavRoutes.StudentHome,
                label = "Inicio",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Mis cursos",
                icon = Icons.Outlined.School
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Escanear QR",
                icon = Icons.Outlined.QrCodeScanner,
                hasBadge = true
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Historial",
                icon = Icons.Outlined.BarChart
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = Icons.Outlined.Person
            )
        )

        UserRole.TEACHER -> listOf(
            NavigationTabItem(
                route = NavRoutes.TeacherHome,
                label = "Inicio",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Mis cursos",
                icon = Icons.Outlined.School
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Sesiones",
                icon = Icons.Outlined.QrCodeScanner,
                hasBadge = true
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Reportes",
                icon = Icons.Outlined.BarChart
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = Icons.Outlined.Person
            )
        )

        UserRole.ADMIN -> listOf(
            NavigationTabItem(
                route = NavRoutes.TeacherHome,
                label = "Inicio",
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Cursos",
                icon = Icons.Outlined.School
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Usuarios",
                icon = Icons.Outlined.Group
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Reportes",
                icon = Icons.Outlined.BarChart
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = Icons.Outlined.Person
            )
        )
    }
}

val DefaultNavigationTabs = getNavigationTabsForRole(UserRole.TEACHER)

@Composable
fun SmartAttendanceBottomBar(
    currentRoute: NavRoutes,
    onTabSelected: (NavRoutes) -> Unit,
    modifier: Modifier = Modifier,
    userRole: UserRole = UserRole.TEACHER,
    tabs: List<NavigationTabItem> = getNavigationTabsForRole(userRole)
) {
    NavigationBar(
        containerColor = AppTheme.colors.surfaceContainerLowest,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        tabs.forEach { tab ->
            SmartAttendanceNavItem(
                item = tab,
                isSelected = currentRoute == tab.route,
                onClick = { onTabSelected(tab.route) }
            )
        }
    }
}

@Composable
fun RowScope.SmartAttendanceNavItem(
    item: NavigationTabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            val iconVector = if (isSelected) item.selectedIcon else item.icon
            if (item.hasBadge) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = item.label
                    )
                }
            } else {
                Icon(
                    imageVector = iconVector,
                    contentDescription = item.label
                )
            }
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = AppTheme.colors.secondary,
            selectedTextColor = AppTheme.colors.primary,
            indicatorColor = AppTheme.colors.secondaryFixed,
            unselectedIconColor = AppTheme.colors.onSurfaceVariant,
            unselectedTextColor = AppTheme.colors.onSurfaceVariant
        )
    )
}
