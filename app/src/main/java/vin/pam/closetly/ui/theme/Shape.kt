package vin.pam.closetly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape

val ClosetlyShapes = Shapes(
    extraSmall = RoundedCornerShape(ClosetlyRadius.Sm),
    small = RoundedCornerShape(ClosetlyRadius.Md),
    medium = RoundedCornerShape(ClosetlyRadius.Lg),
    large = RoundedCornerShape(ClosetlyRadius.Xl),
    extraLarge = RoundedCornerShape(ClosetlyRadius.Xl),
)

val ClosetlyFullShape: Shape = RoundedCornerShape(50)
