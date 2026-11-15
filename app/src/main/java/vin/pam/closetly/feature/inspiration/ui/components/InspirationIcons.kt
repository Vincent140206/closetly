package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Bookmark icons drawn as vectors so the feature does not depend on material-icons-extended. */
internal object InspirationIcons {
    val BookmarkFilled: ImageVector by lazy { bookmark(filled = true) }
    val BookmarkOutline: ImageVector by lazy { bookmark(filled = false) }

    private fun bookmark(filled: Boolean): ImageVector =
        ImageVector.Builder(
            name = if (filled) "BookmarkFilled" else "BookmarkOutline",
            defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
        ).path(
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6f, 3f)
            horizontalLineTo(18f)
            verticalLineTo(21f)
            lineTo(12f, 16.5f)
            lineTo(6f, 21f)
            close()
        }.build()
}
