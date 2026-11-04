package dev.leo.smartattendance.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

// Íconos de Lucide usados en la app. Paths transcritos de los SVG oficiales (lucide.dev).
// Cada propiedad se cachea tras el primer uso.

// ---------------------------------------------------------------- navegación / home

private var _house: ImageVector? = null
val LucideIcons.House: ImageVector
    get() = _house ?: lucide("house") {
        path { moveTo(15f, 21f); verticalLineToRelative(-8f); arcToRelative(1f, 1f, 0f, false, false, -1f, -1f); horizontalLineToRelative(-4f); arcToRelative(1f, 1f, 0f, false, false, -1f, 1f); verticalLineToRelative(8f) }
        path { moveTo(3f, 10f); arcToRelative(2f, 2f, 0f, false, true, 0.709f, -1.528f); lineToRelative(7f, -5.999f); arcToRelative(2f, 2f, 0f, false, true, 2.582f, 0f); lineToRelative(7f, 5.999f); arcTo(2f, 2f, 0f, false, true, 21f, 10f); verticalLineToRelative(9f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); close() }
    }.also { _house = it }

private var _user: ImageVector? = null
val LucideIcons.User: ImageVector
    get() = _user ?: lucide("user") {
        path { moveTo(19f, 21f); verticalLineToRelative(-2f); arcToRelative(4f, 4f, 0f, false, false, -4f, -4f); horizontalLineTo(9f); arcToRelative(4f, 4f, 0f, false, false, -4f, 4f); verticalLineToRelative(2f) }
        path { moveTo(16f, 7f); arcToRelative(4f, 4f, 0f, true, true, -8f, 0f); arcToRelative(4f, 4f, 0f, false, true, 8f, 0f); close() }
    }.also { _user = it }

private var _users: ImageVector? = null
val LucideIcons.Users: ImageVector
    get() = _users ?: lucide("users") {
        path { moveTo(16f, 21f); verticalLineToRelative(-2f); arcToRelative(4f, 4f, 0f, false, false, -4f, -4f); horizontalLineTo(6f); arcToRelative(4f, 4f, 0f, false, false, -4f, 4f); verticalLineToRelative(2f) }
        path { moveTo(13f, 7f); arcToRelative(4f, 4f, 0f, true, true, -8f, 0f); arcToRelative(4f, 4f, 0f, false, true, 8f, 0f); close() }
        path { moveTo(22f, 21f); verticalLineToRelative(-2f); arcToRelative(4f, 4f, 0f, false, false, -3f, -3.87f) }
        path { moveTo(16f, 3.13f); arcToRelative(4f, 4f, 0f, false, true, 0f, 7.75f) }
    }.also { _users = it }

private var _graduationCap: ImageVector? = null
val LucideIcons.GraduationCap: ImageVector
    get() = _graduationCap ?: lucide("graduation-cap") {
        path { moveTo(21.42f, 10.922f); arcToRelative(1f, 1f, 0f, false, false, -0.019f, -1.838f); lineTo(12.83f, 5.18f); arcToRelative(2f, 2f, 0f, false, false, -1.66f, 0f); lineTo(2.6f, 9.08f); arcToRelative(1f, 1f, 0f, false, false, 0f, 1.832f); lineToRelative(8.57f, 3.908f); arcToRelative(2f, 2f, 0f, false, false, 1.66f, 0f); close() }
        path { moveTo(22f, 10f); verticalLineToRelative(6f) }
        path { moveTo(6f, 12.5f); verticalLineTo(16f); arcToRelative(6f, 3f, 0f, false, false, 12f, 0f); verticalLineToRelative(-3.5f) }
    }.also { _graduationCap = it }

private var _bookOpen: ImageVector? = null
val LucideIcons.BookOpen: ImageVector
    get() = _bookOpen ?: lucide("book-open") {
        path { moveTo(12f, 7f); verticalLineToRelative(14f) }
        path { moveTo(3f, 18f); arcToRelative(1f, 1f, 0f, false, true, -1f, -1f); verticalLineTo(4f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); horizontalLineToRelative(5f); arcToRelative(4f, 4f, 0f, false, true, 4f, 4f); arcToRelative(4f, 4f, 0f, false, true, 4f, -4f); horizontalLineToRelative(5f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); verticalLineToRelative(13f); arcToRelative(1f, 1f, 0f, false, true, -1f, 1f); horizontalLineToRelative(-6f); arcToRelative(3f, 3f, 0f, false, false, -3f, 3f); arcToRelative(3f, 3f, 0f, false, false, -3f, -3f); close() }
    }.also { _bookOpen = it }

// ---------------------------------------------------------------- QR / escáner / reportes

private var _qrCode: ImageVector? = null
val LucideIcons.QrCode: ImageVector
    get() = _qrCode ?: lucide("qr-code") {
        path { moveTo(5f, 3f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(17f, 3f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineToRelative(-2f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(5f, 15f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineToRelative(-2f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(21f, 21f); verticalLineToRelative(0.01f) }
        path { moveTo(12f, 7f); verticalLineToRelative(3f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(7f) }
        path { moveTo(3f, 12f); horizontalLineToRelative(0.01f) }
        path { moveTo(12f, 3f); horizontalLineToRelative(0.01f) }
        path { moveTo(12f, 16f); verticalLineToRelative(0.01f) }
        path { moveTo(16f, 12f); horizontalLineToRelative(1f) }
        path { moveTo(21f, 12f); verticalLineToRelative(0.01f) }
        path { moveTo(12f, 21f); verticalLineToRelative(-1f) }
    }.also { _qrCode = it }

private var _scanLine: ImageVector? = null
val LucideIcons.ScanLine: ImageVector
    get() = _scanLine ?: lucide("scan-line") {
        path { moveTo(3f, 7f); verticalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(2f) }
        path { moveTo(17f, 3f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(2f) }
        path { moveTo(21f, 17f); verticalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineToRelative(-2f) }
        path { moveTo(7f, 21f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineToRelative(-2f) }
        path { moveTo(7f, 12f); horizontalLineToRelative(10f) }
    }.also { _scanLine = it }

private var _chartColumn: ImageVector? = null
val LucideIcons.ChartColumn: ImageVector
    get() = _chartColumn ?: lucide("chart-column") {
        path { moveTo(3f, 3f); verticalLineToRelative(16f); arcToRelative(2f, 2f, 0f, false, false, 2f, 2f); horizontalLineToRelative(16f) }
        path { moveTo(18f, 17f); verticalLineTo(9f) }
        path { moveTo(13f, 17f); verticalLineTo(5f) }
        path { moveTo(8f, 17f); verticalLineToRelative(-3f) }
    }.also { _chartColumn = it }

private var _chartLine: ImageVector? = null
val LucideIcons.ChartLine: ImageVector
    get() = _chartLine ?: lucide("chart-line") {
        path { moveTo(3f, 3f); verticalLineToRelative(16f); arcToRelative(2f, 2f, 0f, false, false, 2f, 2f); horizontalLineToRelative(16f) }
        path { moveTo(19f, 9f); lineToRelative(-5f, 5f); lineToRelative(-4f, -4f); lineToRelative(-3f, 3f) }
    }.also { _chartLine = it }
