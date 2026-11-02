package dev.leo.smartattendance.core.designsystem

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
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
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Primary
import dev.leo.smartattendance.ui.theme.Secondary
import dev.leo.smartattendance.ui.theme.SecondaryFixed
import dev.leo.smartattendance.ui.theme.SurfaceContainerLowest

data class NavigationTabItem(
    val route: NavRoutes,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val hasBadge: Boolean = false
)

val DefaultNavigationTabs = listOf(
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

@Composable
fun SmartAttendanceBottomBar(
    currentRoute: NavRoutes,
    onTabSelected: (NavRoutes) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<NavigationTabItem> = DefaultNavigationTabs
) {
    NavigationBar(
        containerColor = SurfaceContainerLowest,
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
            selectedIconColor = Secondary,
            selectedTextColor = Primary,
            indicatorColor = SecondaryFixed,
            unselectedIconColor = OnSurfaceVariant,
            unselectedTextColor = OnSurfaceVariant
        )
    )
}
