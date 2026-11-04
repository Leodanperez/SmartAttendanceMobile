package dev.leo.smartattendance.core.designsystem
import dev.leo.smartattendance.core.icons.LucideIcons
import dev.leo.smartattendance.core.icons.*
import dev.leo.smartattendance.ui.theme.AppTheme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
                icon = LucideIcons.House,
                selectedIcon = LucideIcons.House
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Mis cursos",
                icon = LucideIcons.GraduationCap
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Escanear QR",
                icon = LucideIcons.ScanLine,
                hasBadge = true
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Historial",
                icon = LucideIcons.ChartColumn
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = LucideIcons.User
            )
        )

        UserRole.TEACHER -> listOf(
            NavigationTabItem(
                route = NavRoutes.TeacherHome,
                label = "Inicio",
                icon = LucideIcons.House,
                selectedIcon = LucideIcons.House
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Mis cursos",
                icon = LucideIcons.GraduationCap
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Sesiones",
                icon = LucideIcons.ScanLine,
                hasBadge = true
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Reportes",
                icon = LucideIcons.ChartColumn
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = LucideIcons.User
            )
        )

        UserRole.ADMIN -> listOf(
            NavigationTabItem(
                route = NavRoutes.TeacherHome,
                label = "Inicio",
                icon = LucideIcons.House,
                selectedIcon = LucideIcons.House
            ),
            NavigationTabItem(
                route = NavRoutes.Courses,
                label = "Cursos",
                icon = LucideIcons.GraduationCap
            ),
            NavigationTabItem(
                route = NavRoutes.Sessions,
                label = "Usuarios",
                icon = LucideIcons.Users
            ),
            NavigationTabItem(
                route = NavRoutes.Reports,
                label = "Reportes",
                icon = LucideIcons.ChartColumn
            ),
            NavigationTabItem(
                route = NavRoutes.Profile,
                label = "Perfil",
                icon = LucideIcons.User
            )
        )
    }
}
/**
 * Barra de navegación flotante (estilo "pill" tipo iOS): una tarjeta redondeada separada de los
 * bordes, con sombra, y la pestaña activa resaltada con una píldora de fondo.
 */
@Composable
fun SmartAttendanceBottomBar(
    currentRoute: NavRoutes,
    onTabSelected: (NavRoutes) -> Unit,
    modifier: Modifier = Modifier,
    userRole: UserRole = UserRole.TEACHER,
    tabs: List<NavigationTabItem> = getNavigationTabsForRole(userRole)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = AppTheme.colors.surfaceContainerLowest,
            shadowElevation = 12.dp,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
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
    }
}

/**
 * Pestaña flotante: ícono centrado y, cuando está activa, una píldora de fondo que se expande
 * para mostrar el texto. Las inactivas muestran solo el ícono.
 */
@Composable
fun RowScope.SmartAttendanceNavItem(
    item: NavigationTabItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // La pestaña activa resalta con una píldora redonda detrás del ícono; el texto va siempre debajo.
    val pillColor by animateColorAsState(
        targetValue = if (isSelected) AppTheme.colors.primaryContainer else Color.Transparent,
        animationSpec = tween(220),
        label = "pill",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else AppTheme.colors.onSurfaceVariant,
        animationSpec = tween(220),
        label = "icon",
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
        animationSpec = tween(220),
        label = "label",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Ícono dentro de una píldora redonda.
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(pillColor)
                .padding(horizontal = 18.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            val iconVector = if (isSelected) item.selectedIcon else item.icon
            if (item.hasBadge) {
                BadgedBox(
                    badge = { Badge(containerColor = MaterialTheme.colorScheme.error, modifier = Modifier.size(7.dp)) }
                ) {
                    Icon(iconVector, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            } else {
                Icon(iconVector, contentDescription = item.label, tint = iconColor, modifier = Modifier.size(22.dp))
            }
        }

        // Texto siempre visible, debajo del ícono (estilo iOS).
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = labelColor,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}
