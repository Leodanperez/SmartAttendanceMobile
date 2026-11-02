package dev.leo.smartattendance.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.leo.smartattendance.ui.theme.OnSurfaceVariant
import dev.leo.smartattendance.ui.theme.Primary
import dev.leo.smartattendance.ui.theme.PrimaryContainer
import dev.leo.smartattendance.ui.theme.Secondary
import dev.leo.smartattendance.ui.theme.SecondaryFixed
import dev.leo.smartattendance.ui.theme.SurfaceContainerHigh

@Composable
fun SmartAttendanceHeader(
    title: String = "Inicio",
    subtitle: String = "SmartAttendance M3",
    badgeText: String? = "DOCENTE",
    showNotification: Boolean = true,
    hasUnreadNotifications: Boolean = false,
    showProfile: Boolean = true,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Shield Emblem Logo Box
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Primary, PrimaryContainer)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Logo SmartAttendance",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Primary
                        )
                        if (!badgeText.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = SecondaryFixed
                            ) {
                                Text(
                                    text = badgeText.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = Secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showNotification) {
                    IconButton(onClick = onNotificationClick) {
                        if (hasUnreadNotifications) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = OnSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notificaciones",
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                }

                if (showProfile) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Primary.copy(alpha = 0.3f), CircleShape)
                            .background(SurfaceContainerHigh)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Perfil",
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
