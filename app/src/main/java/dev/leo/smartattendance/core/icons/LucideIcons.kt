package dev.leo.smartattendance.core.icons

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Íconos de Lucide (lucide.dev) dibujados a mano como ImageVector, para que la app Android use el
 * mismo set de íconos que el panel web Angular sin depender de una librería externa.
 *
 * Todos los paths provienen de los SVG oficiales de Lucide: viewport 24x24, trazo de 2, sin relleno,
 * con extremos y uniones redondeados. Se tiñen con el color que se pase al componente `Icon(...)`.
 * Uso: `Icon(imageVector = LucideIcons.House, contentDescription = ...)`.
 */
object LucideIcons

/** Construye un ImageVector de Lucide (24x24, stroke 2, redondeado) a partir de sus sub-paths. */
internal fun lucide(name: String, block: LucidePaths.() -> Unit): ImageVector {
    val builder = ImageVector.Builder(
        name = "Lucide.$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    val paths = LucidePaths(builder)
    paths.block()
    return builder.build()
}

/** Pequeño ayudante para añadir cada sub-path con el estilo estándar de Lucide. */
internal class LucidePaths(private val builder: ImageVector.Builder) {
    fun path(block: PathBuilder.() -> Unit) {
        builder.path(
            stroke = SolidColor(androidx.compose.ui.graphics.Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block,
        )
    }
}
