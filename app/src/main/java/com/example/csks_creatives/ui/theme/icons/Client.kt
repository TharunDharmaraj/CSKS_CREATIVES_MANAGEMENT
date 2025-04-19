package com.example.csks_creatives.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ClientIcon: ImageVector
    get() {
        if (_undefined != null) {
            return _undefined!!
        }
        _undefined = ImageVector.Builder(
            name = "Connect_without_contact",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(640f, 880f)
                verticalLineToRelative(-90f)
                quadToRelative(-56f, -18f, -94f, -64f)
                reflectiveQuadToRelative(-44f, -106f)
                horizontalLineToRelative(80f)
                quadToRelative(8f, 43f, 40.5f, 71.5f)
                reflectiveQuadTo(700f, 720f)
                horizontalLineToRelative(120f)
                quadToRelative(25f, 0f, 42.5f, 17.5f)
                reflectiveQuadTo(880f, 780f)
                verticalLineToRelative(100f)
                close()
                moveToRelative(120f, -200f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(680f, 600f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(760f, 520f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(840f, 600f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(760f, 680f)
                moveTo(360f, 560f)
                quadToRelative(0f, -150f, 105f, -255f)
                reflectiveQuadToRelative(255f, -105f)
                verticalLineToRelative(80f)
                quadToRelative(-117f, 0f, -198.5f, 81.5f)
                reflectiveQuadTo(440f, 560f)
                close()
                moveToRelative(160f, 0f)
                quadToRelative(0f, -83f, 58.5f, -141.5f)
                reflectiveQuadTo(720f, 360f)
                verticalLineToRelative(80f)
                quadToRelative(-50f, 0f, -85f, 35f)
                reflectiveQuadToRelative(-35f, 85f)
                close()
                moveTo(80f, 440f)
                verticalLineToRelative(-100f)
                quadToRelative(0f, -25f, 17.5f, -42.5f)
                reflectiveQuadTo(140f, 280f)
                horizontalLineToRelative(120f)
                quadToRelative(45f, 0f, 77.5f, -28.5f)
                reflectiveQuadTo(378f, 180f)
                horizontalLineToRelative(80f)
                quadToRelative(-6f, 60f, -44f, 106f)
                reflectiveQuadToRelative(-94f, 64f)
                verticalLineToRelative(90f)
                close()
                moveToRelative(120f, -200f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 160f)
                reflectiveQuadToRelative(23.5f, -56.5f)
                reflectiveQuadTo(200f, 80f)
                reflectiveQuadToRelative(56.5f, 23.5f)
                reflectiveQuadTo(280f, 160f)
                reflectiveQuadToRelative(-23.5f, 56.5f)
                reflectiveQuadTo(200f, 240f)
            }
        }.build()
        return _undefined!!
    }

private var _undefined: ImageVector? = null
