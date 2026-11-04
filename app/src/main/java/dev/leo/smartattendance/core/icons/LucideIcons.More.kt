package dev.leo.smartattendance.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

// Resto de íconos de Lucide usados en la app. Paths de los SVG oficiales (lucide.dev).

// ---------------------------------------------------------------- seguridad / cuenta

private var _lock: ImageVector? = null
val LucideIcons.Lock: ImageVector
    get() = _lock ?: lucide("lock") {
        path { moveTo(7f, 11f); verticalLineTo(7f); arcToRelative(5f, 5f, 0f, false, true, 10f, 0f); verticalLineToRelative(4f) }
        path { moveTo(5f, 11f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(7f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineToRelative(-7f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
    }.also { _lock = it }

private var _lockOpen: ImageVector? = null
val LucideIcons.LockOpen: ImageVector
    get() = _lockOpen ?: lucide("lock-open") {
        path { moveTo(7f, 11f); verticalLineTo(7f); arcToRelative(5f, 5f, 0f, false, true, 9.9f, -1f) }
        path { moveTo(5f, 11f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(7f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineToRelative(-7f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
    }.also { _lockOpen = it }

private var _shield: ImageVector? = null
val LucideIcons.Shield: ImageVector
    get() = _shield ?: lucide("shield") {
        path { moveTo(20f, 13f); curveToRelative(0f, 5f, -3.5f, 7.5f, -7.66f, 8.95f); arcToRelative(1f, 1f, 0f, false, true, -0.67f, -0.01f); curveTo(7.5f, 20.5f, 4f, 18f, 4f, 13f); verticalLineTo(6f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); curveToRelative(2f, 0f, 4.5f, -1.2f, 6.24f, -2.72f); arcToRelative(1.17f, 1.17f, 0f, false, true, 1.52f, 0f); curveTo(14.51f, 3.81f, 17f, 5f, 19f, 5f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); close() }
    }.also { _shield = it }

private var _shieldCheck: ImageVector? = null
val LucideIcons.ShieldCheck: ImageVector
    get() = _shieldCheck ?: lucide("shield-check") {
        path { moveTo(20f, 13f); curveToRelative(0f, 5f, -3.5f, 7.5f, -7.66f, 8.95f); arcToRelative(1f, 1f, 0f, false, true, -0.67f, -0.01f); curveTo(7.5f, 20.5f, 4f, 18f, 4f, 13f); verticalLineTo(6f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); curveToRelative(2f, 0f, 4.5f, -1.2f, 6.24f, -2.72f); arcToRelative(1.17f, 1.17f, 0f, false, true, 1.52f, 0f); curveTo(14.51f, 3.81f, 17f, 5f, 19f, 5f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); close() }
        path { moveTo(9f, 12f); lineToRelative(2f, 2f); lineToRelative(4f, -4f) }
    }.also { _shieldCheck = it }

private var _idCard: ImageVector? = null
val LucideIcons.IdCard: ImageVector
    get() = _idCard ?: lucide("id-card") {
        path { moveTo(16f, 10f); horizontalLineTo(18f) }
        path { moveTo(16f, 14f); horizontalLineTo(18f) }
        path { moveTo(6.17f, 15f); arcToRelative(3f, 3f, 0f, false, true, 5.66f, 0f) }
        path { moveTo(9f, 11f); arcToRelative(2f, 2f, 0f, true, true, 0f, -0.01f); close() }
        path { moveTo(5f, 4f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(12f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
    }.also { _idCard = it }

// ---------------------------------------------------------------- comunicación / contacto

private var _mail: ImageVector? = null
val LucideIcons.Mail: ImageVector
    get() = _mail ?: lucide("mail") {
        path { moveTo(22f, 7f); verticalLineToRelative(10f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(4f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(7f) }
        path { moveTo(2f, 7f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(16f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); lineToRelative(-8.97f, 5.7f); arcToRelative(1.94f, 1.94f, 0f, false, true, -2.06f, 0f); close() }
    }.also { _mail = it }

private var _atSign: ImageVector? = null
val LucideIcons.AtSign: ImageVector
    get() = _atSign ?: lucide("at-sign") {
        path { moveTo(16f, 12f); arcToRelative(4f, 4f, 0f, true, true, -8f, 0f); arcToRelative(4f, 4f, 0f, false, true, 8f, 0f); close() }
        path { moveTo(16f, 8f); verticalLineToRelative(5f); arcToRelative(3f, 3f, 0f, false, false, 6f, 0f); verticalLineToRelative(-1f); arcToRelative(10f, 10f, 0f, true, false, -4f, 8f) }
    }.also { _atSign = it }

private var _phone: ImageVector? = null
val LucideIcons.Phone: ImageVector
    get() = _phone ?: lucide("phone") {
        path { moveTo(13.832f, 16.568f); arcToRelative(1f, 1f, 0f, false, false, 1.213f, -0.303f); lineToRelative(0.355f, -0.465f); arcTo(2f, 2f, 0f, false, true, 17f, 15f); horizontalLineToRelative(3f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(3f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); arcTo(18f, 18f, 0f, false, true, 2f, 4f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(3f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(3f); arcToRelative(2f, 2f, 0f, false, true, -0.8f, 1.6f); lineToRelative(-0.468f, 0.351f); arcToRelative(1f, 1f, 0f, false, false, -0.292f, 1.233f); arcToRelative(14f, 14f, 0f, false, false, 6.392f, 6.384f); close() }
    }.also { _phone = it }

private var _mapPin: ImageVector? = null
val LucideIcons.MapPin: ImageVector
    get() = _mapPin ?: lucide("map-pin") {
        path { moveTo(20f, 10f); curveToRelative(0f, 4.993f, -5.539f, 10.193f, -7.399f, 11.799f); arcToRelative(1f, 1f, 0f, false, true, -1.202f, 0f); curveTo(9.539f, 20.193f, 4f, 14.993f, 4f, 10f); arcToRelative(8f, 8f, 0f, false, true, 16f, 0f); close() }
        path { moveTo(12f, 10f); arcToRelative(3f, 3f, 0f, true, true, 0f, -0.01f); close() }
    }.also { _mapPin = it }

private var _building2: ImageVector? = null
val LucideIcons.Building2: ImageVector
    get() = _building2 ?: lucide("building-2") {
        path { moveTo(6f, 22f); verticalLineTo(4f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(8f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(18f); close() }
        path { moveTo(6f, 12f); horizontalLineTo(4f); arcToRelative(2f, 2f, 0f, false, false, -2f, 2f); verticalLineToRelative(6f); arcToRelative(2f, 2f, 0f, false, false, 2f, 2f); horizontalLineToRelative(2f) }
        path { moveTo(18f, 9f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(9f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineToRelative(-2f) }
        path { moveTo(10f, 6f); horizontalLineTo(14f) }
        path { moveTo(10f, 10f); horizontalLineTo(14f) }
        path { moveTo(10f, 14f); horizontalLineTo(14f) }
        path { moveTo(10f, 18f); horizontalLineTo(14f) }
    }.also { _building2 = it }

private var _bell: ImageVector? = null
val LucideIcons.Bell: ImageVector
    get() = _bell ?: lucide("bell") {
        path { moveTo(10.268f, 21f); arcToRelative(2f, 2f, 0f, false, false, 3.464f, 0f) }
        path { moveTo(3.262f, 15.326f); arcTo(1f, 1f, 0f, false, false, 4f, 17f); horizontalLineToRelative(16f); arcToRelative(1f, 1f, 0f, false, false, 0.74f, -1.673f); curveTo(19.41f, 13.956f, 18f, 12.499f, 18f, 8f); arcToRelative(6f, 6f, 0f, false, false, -12f, 0f); curveToRelative(0f, 4.499f, -1.411f, 5.956f, -2.738f, 7.326f) }
    }.also { _bell = it }

// ---------------------------------------------------------------- estados / feedback

private var _circleCheck: ImageVector? = null
val LucideIcons.CircleCheck: ImageVector
    get() = _circleCheck ?: lucide("circle-check") {
        path { moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f); close() }
        path { moveTo(9f, 12f); lineToRelative(2f, 2f); lineToRelative(4f, -4f) }
    }.also { _circleCheck = it }

private var _circleCheckBig: ImageVector? = null
val LucideIcons.CircleCheckBig: ImageVector
    get() = _circleCheckBig ?: lucide("circle-check-big") {
        path { moveTo(21.801f, 10f); arcTo(10f, 10f, 0f, true, true, 17f, 3.335f) }
        path { moveTo(9f, 11f); lineToRelative(3f, 3f); lineTo(22f, 4f) }
    }.also { _circleCheckBig = it }

private var _circleAlert: ImageVector? = null
val LucideIcons.CircleAlert: ImageVector
    get() = _circleAlert ?: lucide("circle-alert") {
        path { moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f); close() }
        path { moveTo(12f, 8f); verticalLineToRelative(4f) }
        path { moveTo(12f, 16f); horizontalLineToRelative(0.01f) }
    }.also { _circleAlert = it }

private var _triangleAlert: ImageVector? = null
val LucideIcons.TriangleAlert: ImageVector
    get() = _triangleAlert ?: lucide("triangle-alert") {
        path { moveTo(21.73f, 18f); lineTo(13.73f, 4f); arcToRelative(2f, 2f, 0f, false, false, -3.48f, 0f); lineToRelative(-8f, 14f); arcToRelative(2f, 2f, 0f, false, false, 1.74f, 3f); horizontalLineToRelative(16f); arcToRelative(2f, 2f, 0f, false, false, 1.74f, -3f); close() }
        path { moveTo(12f, 9f); verticalLineToRelative(4f) }
        path { moveTo(12f, 17f); horizontalLineToRelative(0.01f) }
    }.also { _triangleAlert = it }

private var _taskCheck: ImageVector? = null
val LucideIcons.ClipboardCheck: ImageVector
    get() = _taskCheck ?: lucide("clipboard-check") {
        path { moveTo(16f, 4f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(2f) }
        path { moveTo(9f, 2f); horizontalLineToRelative(6f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); verticalLineToRelative(2f); arcToRelative(1f, 1f, 0f, false, true, -1f, 1f); horizontalLineTo(9f); arcToRelative(1f, 1f, 0f, false, true, -1f, -1f); verticalLineTo(3f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); close() }
        path { moveTo(9f, 14f); lineToRelative(2f, 2f); lineToRelative(4f, -4f) }
    }.also { _taskCheck = it }

private var _clipboardList: ImageVector? = null
val LucideIcons.ClipboardList: ImageVector
    get() = _clipboardList ?: lucide("clipboard-list") {
        path { moveTo(16f, 4f); horizontalLineToRelative(2f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(2f) }
        path { moveTo(9f, 2f); horizontalLineToRelative(6f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); verticalLineToRelative(2f); arcToRelative(1f, 1f, 0f, false, true, -1f, 1f); horizontalLineTo(9f); arcToRelative(1f, 1f, 0f, false, true, -1f, -1f); verticalLineTo(3f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); close() }
        path { moveTo(12f, 11f); horizontalLineToRelative(4f) }
        path { moveTo(12f, 16f); horizontalLineToRelative(4f) }
        path { moveTo(8f, 11f); horizontalLineToRelative(0.01f) }
        path { moveTo(8f, 16f); horizontalLineToRelative(0.01f) }
    }.also { _clipboardList = it }

private var _squarePen: ImageVector? = null
val LucideIcons.SquarePen: ImageVector
    get() = _squarePen ?: lucide("square-pen") {
        path { moveTo(12f, 3f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, false, -2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, false, 2f, 2f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, false, 2f, -2f); verticalLineToRelative(-7f) }
        path { moveTo(18.375f, 2.625f); arcToRelative(1f, 1f, 0f, false, true, 3f, 3f); lineToRelative(-9.013f, 9.014f); arcToRelative(2f, 2f, 0f, false, true, -0.853f, 0.505f); lineToRelative(-2.873f, 0.84f); arcToRelative(0.5f, 0.5f, 0f, false, true, -0.62f, -0.62f); lineToRelative(0.84f, -2.873f); arcToRelative(2f, 2f, 0f, false, true, 0.506f, -0.852f); close() }
    }.also { _squarePen = it }

// ---------------------------------------------------------------- acciones / controles

private var _x: ImageVector? = null
val LucideIcons.X: ImageVector
    get() = _x ?: lucide("x") {
        path { moveTo(18f, 6f); lineTo(6f, 18f) }
        path { moveTo(6f, 6f); lineToRelative(12f, 12f) }
    }.also { _x = it }

private var _circleX: ImageVector? = null
val LucideIcons.CircleX: ImageVector
    get() = _circleX ?: lucide("circle-x") {
        path { moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f); close() }
        path { moveTo(15f, 9f); lineToRelative(-6f, 6f) }
        path { moveTo(9f, 9f); lineToRelative(6f, 6f) }
    }.also { _circleX = it }

private var _plus: ImageVector? = null
val LucideIcons.Plus: ImageVector
    get() = _plus ?: lucide("plus") {
        path { moveTo(5f, 12f); horizontalLineToRelative(14f) }
        path { moveTo(12f, 5f); verticalLineToRelative(14f) }
    }.also { _plus = it }

private var _squarePlus: ImageVector? = null
val LucideIcons.SquarePlus: ImageVector
    get() = _squarePlus ?: lucide("square-plus") {
        path { moveTo(5f, 3f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(8f, 12f); horizontalLineToRelative(8f) }
        path { moveTo(12f, 8f); verticalLineToRelative(8f) }
    }.also { _squarePlus = it }

private var _refreshCw: ImageVector? = null
val LucideIcons.RefreshCw: ImageVector
    get() = _refreshCw ?: lucide("refresh-cw") {
        path { moveTo(3f, 12f); arcToRelative(9f, 9f, 0f, false, true, 9f, -9f); arcToRelative(9.75f, 9.75f, 0f, false, true, 6.74f, 2.74f); lineTo(21f, 8f) }
        path { moveTo(21f, 3f); verticalLineToRelative(5f); horizontalLineToRelative(-5f) }
        path { moveTo(21f, 12f); arcToRelative(9f, 9f, 0f, false, true, -9f, 9f); arcToRelative(9.75f, 9.75f, 0f, false, true, -6.74f, -2.74f); lineTo(3f, 16f) }
        path { moveTo(8f, 16f); horizontalLineTo(3f); verticalLineToRelative(5f) }
    }.also { _refreshCw = it }

private var _eye: ImageVector? = null
val LucideIcons.Eye: ImageVector
    get() = _eye ?: lucide("eye") {
        path { moveTo(2.062f, 12.348f); arcToRelative(1f, 1f, 0f, false, true, 0f, -0.696f); arcToRelative(10.75f, 10.75f, 0f, false, true, 19.876f, 0f); arcToRelative(1f, 1f, 0f, false, true, 0f, 0.696f); arcToRelative(10.75f, 10.75f, 0f, false, true, -19.876f, 0f); close() }
        path { moveTo(15f, 12f); arcToRelative(3f, 3f, 0f, true, true, -6f, 0f); arcToRelative(3f, 3f, 0f, false, true, 6f, 0f); close() }
    }.also { _eye = it }

private var _eyeOff: ImageVector? = null
val LucideIcons.EyeOff: ImageVector
    get() = _eyeOff ?: lucide("eye-off") {
        path { moveTo(10.733f, 5.076f); arcToRelative(10.744f, 10.744f, 0f, false, true, 11.205f, 6.575f); arcToRelative(1f, 1f, 0f, false, true, 0f, 0.696f); arcToRelative(10.747f, 10.747f, 0f, false, true, -1.444f, 2.49f) }
        path { moveTo(14.084f, 14.158f); arcToRelative(3f, 3f, 0f, false, true, -4.242f, -4.242f) }
        path { moveTo(17.479f, 17.499f); arcToRelative(10.75f, 10.75f, 0f, false, true, -15.417f, -5.151f); arcToRelative(1f, 1f, 0f, false, true, 0f, -0.696f); arcToRelative(10.75f, 10.75f, 0f, false, true, 4.446f, -5.143f) }
        path { moveTo(2f, 2f); lineToRelative(20f, 20f) }
    }.also { _eyeOff = it }

private var _settings: ImageVector? = null
val LucideIcons.Settings: ImageVector
    get() = _settings ?: lucide("settings") {
        path { moveTo(12.22f, 2f); horizontalLineToRelative(-0.44f); arcToRelative(2f, 2f, 0f, false, false, -2f, 2f); verticalLineToRelative(0.18f); arcToRelative(2f, 2f, 0f, false, true, -1f, 1.73f); lineToRelative(-0.43f, 0.25f); arcToRelative(2f, 2f, 0f, false, true, -2f, 0f); lineToRelative(-0.15f, -0.08f); arcToRelative(2f, 2f, 0f, false, false, -2.73f, 0.73f); lineToRelative(-0.22f, 0.38f); arcToRelative(2f, 2f, 0f, false, false, 0.73f, 2.73f); lineToRelative(0.15f, 0.1f); arcToRelative(2f, 2f, 0f, false, true, 1f, 1.72f); verticalLineToRelative(0.51f); arcToRelative(2f, 2f, 0f, false, true, -1f, 1.74f); lineToRelative(-0.15f, 0.09f); arcToRelative(2f, 2f, 0f, false, false, -0.73f, 2.73f); lineToRelative(0.22f, 0.38f); arcToRelative(2f, 2f, 0f, false, false, 2.73f, 0.73f); lineToRelative(0.15f, -0.08f); arcToRelative(2f, 2f, 0f, false, true, 2f, 0f); lineToRelative(0.43f, 0.25f); arcToRelative(2f, 2f, 0f, false, true, 1f, 1.73f); verticalLineTo(20f); arcToRelative(2f, 2f, 0f, false, false, 2f, 2f); horizontalLineToRelative(0.44f); arcToRelative(2f, 2f, 0f, false, false, 2f, -2f); verticalLineToRelative(-0.18f); arcToRelative(2f, 2f, 0f, false, true, 1f, -1.73f); lineToRelative(0.43f, -0.25f); arcToRelative(2f, 2f, 0f, false, true, 2f, 0f); lineToRelative(0.15f, 0.08f); arcToRelative(2f, 2f, 0f, false, false, 2.73f, -0.73f); lineToRelative(0.22f, -0.39f); arcToRelative(2f, 2f, 0f, false, false, -0.73f, -2.73f); lineToRelative(-0.15f, -0.08f); arcToRelative(2f, 2f, 0f, false, true, -1f, -1.74f); verticalLineToRelative(-0.5f); arcToRelative(2f, 2f, 0f, false, true, 1f, -1.74f); lineToRelative(0.15f, -0.09f); arcToRelative(2f, 2f, 0f, false, false, 0.73f, -2.73f); lineToRelative(-0.22f, -0.38f); arcToRelative(2f, 2f, 0f, false, false, -2.73f, -0.73f); lineToRelative(-0.15f, 0.08f); arcToRelative(2f, 2f, 0f, false, true, -2f, 0f); lineToRelative(-0.43f, -0.25f); arcToRelative(2f, 2f, 0f, false, true, -1f, -1.73f); verticalLineTo(4f); arcToRelative(2f, 2f, 0f, false, false, -2f, -2f); close() }
        path { moveTo(15f, 12f); arcToRelative(3f, 3f, 0f, true, true, -6f, 0f); arcToRelative(3f, 3f, 0f, false, true, 6f, 0f); close() }
    }.also { _settings = it }

private var _timer: ImageVector? = null
val LucideIcons.Timer: ImageVector
    get() = _timer ?: lucide("timer") {
        path { moveTo(10f, 2f); horizontalLineToRelative(4f) }
        path { moveTo(12f, 14f); lineToRelative(3f, -3f) }
        path { moveTo(12f, 14f); arcToRelative(6f, 6f, 0f, true, false, 0f, -0.01f); close() }
    }.also { _timer = it }

private var _clock: ImageVector? = null
val LucideIcons.Clock: ImageVector
    get() = _clock ?: lucide("clock") {
        path { moveTo(12f, 6f); verticalLineToRelative(6f); lineToRelative(4f, 2f) }
        path { moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f); close() }
    }.also { _clock = it }

private var _calendar: ImageVector? = null
val LucideIcons.Calendar: ImageVector
    get() = _calendar ?: lucide("calendar") {
        path { moveTo(8f, 2f); verticalLineToRelative(4f) }
        path { moveTo(16f, 2f); verticalLineToRelative(4f) }
        path { moveTo(5f, 4f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(3f, 10f); horizontalLineToRelative(18f) }
    }.also { _calendar = it }

private var _calendarDays: ImageVector? = null
val LucideIcons.CalendarDays: ImageVector
    get() = _calendarDays ?: lucide("calendar-days") {
        path { moveTo(8f, 2f); verticalLineToRelative(4f) }
        path { moveTo(16f, 2f); verticalLineToRelative(4f) }
        path { moveTo(5f, 4f); horizontalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(14f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(6f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
        path { moveTo(3f, 10f); horizontalLineToRelative(18f) }
        path { moveTo(8f, 14f); horizontalLineToRelative(0.01f) }
        path { moveTo(12f, 14f); horizontalLineToRelative(0.01f) }
        path { moveTo(16f, 14f); horizontalLineToRelative(0.01f) }
        path { moveTo(8f, 18f); horizontalLineToRelative(0.01f) }
        path { moveTo(12f, 18f); horizontalLineToRelative(0.01f) }
        path { moveTo(16f, 18f); horizontalLineToRelative(0.01f) }
    }.also { _calendarDays = it }

// ---------------------------------------------------------------- tema / flecha / varios

private var _sun: ImageVector? = null
val LucideIcons.Sun: ImageVector
    get() = _sun ?: lucide("sun") {
        path { moveTo(12f, 8f); arcToRelative(4f, 4f, 0f, true, false, 0f, 8f); arcToRelative(4f, 4f, 0f, false, false, 0f, -8f); close() }
        path { moveTo(12f, 2f); verticalLineToRelative(2f) }
        path { moveTo(12f, 20f); verticalLineToRelative(2f) }
        path { moveTo(4.93f, 4.93f); lineToRelative(1.41f, 1.41f) }
        path { moveTo(17.66f, 17.66f); lineToRelative(1.41f, 1.41f) }
        path { moveTo(2f, 12f); horizontalLineToRelative(2f) }
        path { moveTo(20f, 12f); horizontalLineToRelative(2f) }
        path { moveTo(6.34f, 17.66f); lineToRelative(-1.41f, 1.41f) }
        path { moveTo(19.07f, 4.93f); lineToRelative(-1.41f, 1.41f) }
    }.also { _sun = it }

private var _moon: ImageVector? = null
val LucideIcons.Moon: ImageVector
    get() = _moon ?: lucide("moon") {
        path { moveTo(20.985f, 12.486f); arcToRelative(9f, 9f, 0f, true, true, -9.47f, -9.472f); arcToRelative(0.5f, 0.5f, 0f, false, true, 0.554f, 0.6f); arcToRelative(6f, 6f, 0f, false, false, 8.318f, 8.318f); arcToRelative(0.5f, 0.5f, 0f, false, true, 0.598f, 0.554f); close() }
    }.also { _moon = it }

private var _sunMoon: ImageVector? = null
val LucideIcons.SunMoon: ImageVector
    get() = _sunMoon ?: lucide("sun-moon") {
        path { moveTo(12f, 8f); arcToRelative(4f, 4f, 0f, false, true, 3.17f, 6.43f) }
        path { moveTo(12f, 2f); verticalLineToRelative(2f) }
        path { moveTo(12f, 20f); verticalLineToRelative(2f) }
        path { moveTo(4.93f, 4.93f); lineToRelative(1.41f, 1.41f) }
        path { moveTo(17.66f, 17.66f); lineToRelative(1.41f, 1.41f) }
        path { moveTo(2f, 12f); horizontalLineToRelative(2f) }
        path { moveTo(20f, 12f); horizontalLineToRelative(2f) }
        path { moveTo(6.34f, 17.66f); lineToRelative(-1.41f, 1.41f) }
        path { moveTo(19.07f, 4.93f); lineToRelative(-1.41f, 1.41f) }
        path { moveTo(22f, 12f); arcTo(10f, 10f, 0f, false, true, 12f, 22f) }
    }.also { _sunMoon = it }

private var _arrowRight: ImageVector? = null
val LucideIcons.ArrowRight: ImageVector
    get() = _arrowRight ?: lucide("arrow-right") {
        path { moveTo(5f, 12f); horizontalLineToRelative(14f) }
        path { moveTo(12f, 5f); lineToRelative(7f, 7f); lineToRelative(-7f, 7f) }
    }.also { _arrowRight = it }

private var _arrowLeft: ImageVector? = null
val LucideIcons.ArrowLeft: ImageVector
    get() = _arrowLeft ?: lucide("arrow-left") {
        path { moveTo(19f, 12f); horizontalLineTo(5f) }
        path { moveTo(12f, 19f); lineToRelative(-7f, -7f); lineToRelative(7f, -7f) }
    }.also { _arrowLeft = it }

private var _chevronRight: ImageVector? = null
val LucideIcons.ChevronRight: ImageVector
    get() = _chevronRight ?: lucide("chevron-right") {
        path { moveTo(9f, 18f); lineToRelative(6f, -6f); lineToRelative(-6f, -6f) }
    }.also { _chevronRight = it }

private var _chevronDown: ImageVector? = null
val LucideIcons.ChevronDown: ImageVector
    get() = _chevronDown ?: lucide("chevron-down") {
        path { moveTo(6f, 9f); lineToRelative(6f, 6f); lineToRelative(6f, -6f) }
    }.also { _chevronDown = it }

private var _logOut: ImageVector? = null
val LucideIcons.LogOut: ImageVector
    get() = _logOut ?: lucide("log-out") {
        path { moveTo(16f, 17f); lineToRelative(5f, -5f); lineToRelative(-5f, -5f) }
        path { moveTo(21f, 12f); horizontalLineTo(9f) }
        path { moveTo(9f, 21f); horizontalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(5f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); horizontalLineToRelative(4f) }
    }.also { _logOut = it }

private var _circleHelp: ImageVector? = null
val LucideIcons.CircleHelp: ImageVector
    get() = _circleHelp ?: lucide("circle-help") {
        path { moveTo(12f, 2f); arcToRelative(10f, 10f, 0f, true, false, 0.01f, 0f); close() }
        path { moveTo(9.09f, 9f); arcToRelative(3f, 3f, 0f, false, true, 5.83f, 1f); curveToRelative(0f, 2f, -3f, 3f, -3f, 3f) }
        path { moveTo(12f, 17f); horizontalLineToRelative(0.01f) }
    }.also { _circleHelp = it }

private var _keyboard: ImageVector? = null
val LucideIcons.Keyboard: ImageVector
    get() = _keyboard ?: lucide("keyboard") {
        path { moveTo(10f, 8f); horizontalLineToRelative(0.01f) }
        path { moveTo(12f, 12f); horizontalLineToRelative(0.01f) }
        path { moveTo(14f, 8f); horizontalLineToRelative(0.01f) }
        path { moveTo(16f, 12f); horizontalLineToRelative(0.01f) }
        path { moveTo(18f, 8f); horizontalLineToRelative(0.01f) }
        path { moveTo(6f, 8f); horizontalLineToRelative(0.01f) }
        path { moveTo(7f, 16f); horizontalLineToRelative(10f) }
        path { moveTo(8f, 12f); horizontalLineToRelative(0.01f) }
        path { moveTo(4f, 5f); horizontalLineToRelative(16f); arcToRelative(2f, 2f, 0f, false, true, 2f, 2f); verticalLineToRelative(10f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineTo(4f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(7f); arcToRelative(2f, 2f, 0f, false, true, 2f, -2f); close() }
    }.also { _keyboard = it }

private var _flashlight: ImageVector? = null
val LucideIcons.Flashlight: ImageVector
    get() = _flashlight ?: lucide("flashlight") {
        path { moveTo(18f, 6f); curveToRelative(0f, 2f, -2f, 2f, -2f, 4f); verticalLineToRelative(10f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineToRelative(-4f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(10f); curveToRelative(0f, -2f, -2f, -2f, -2f, -4f); verticalLineTo(3f); arcToRelative(1f, 1f, 0f, false, true, 1f, -1f); horizontalLineToRelative(12f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); close() }
        path { moveTo(6f, 6f); horizontalLineToRelative(12f) }
        path { moveTo(12f, 12f); horizontalLineToRelative(0.01f) }
    }.also { _flashlight = it }

private var _flashlightOff: ImageVector? = null
val LucideIcons.FlashlightOff: ImageVector
    get() = _flashlightOff ?: lucide("flashlight-off") {
        path { moveTo(16f, 16f); verticalLineToRelative(4f); arcToRelative(2f, 2f, 0f, false, true, -2f, 2f); horizontalLineToRelative(-4f); arcToRelative(2f, 2f, 0f, false, true, -2f, -2f); verticalLineTo(10f); curveToRelative(0f, -2f, -2f, -2f, -2f, -4f) }
        path { moveTo(7f, 2f); horizontalLineToRelative(11f); arcToRelative(1f, 1f, 0f, false, true, 1f, 1f); verticalLineToRelative(3f); curveToRelative(0f, 2f, -2f, 2f, -2f, 4f); verticalLineToRelative(1f) }
        path { moveTo(2f, 2f); lineToRelative(20f, 20f) }
    }.also { _flashlightOff = it }

private var _zap: ImageVector? = null
val LucideIcons.Zap: ImageVector
    get() = _zap ?: lucide("zap") {
        path { moveTo(4f, 14f); arcToRelative(1f, 1f, 0f, false, true, -0.78f, -1.63f); lineToRelative(9.9f, -10.2f); arcToRelative(0.5f, 0.5f, 0f, false, true, 0.86f, 0.46f); lineToRelative(-1.92f, 6.02f); arcTo(1f, 1f, 0f, false, false, 13f, 10f); horizontalLineToRelative(7f); arcToRelative(1f, 1f, 0f, false, true, 0.78f, 1.63f); lineToRelative(-9.9f, 10.2f); arcToRelative(0.5f, 0.5f, 0f, false, true, -0.86f, -0.46f); lineToRelative(1.92f, -6.02f); arcTo(1f, 1f, 0f, false, false, 11f, 14f); close() }
    }.also { _zap = it }
