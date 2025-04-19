package com.example.csks_creatives.ui.theme.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val LeaveRequestIcon: ImageVector
    get() {
        if (_undefined != null) {
            return _undefined!!
        }
        _undefined = ImageVector.Builder(
            name = "RequestChanges",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(10.7099f, 1.29f)
                lineTo(13.7099f, 4.29f)
                lineTo(13.9999f, 5f)
                verticalLineTo(14f)
                lineTo(12.9999f, 15f)
                horizontalLineTo(3.99994f)
                lineTo(2.99994f, 14f)
                verticalLineTo(2f)
                lineTo(3.99994f, 1f)
                horizontalLineTo(9.99994f)
                lineTo(10.7099f, 1.29f)
                close()
                moveTo(3.99994f, 14f)
                horizontalLineTo(12.9999f)
                verticalLineTo(5f)
                lineTo(9.99994f, 2f)
                horizontalLineTo(3.99994f)
                verticalLineTo(14f)
                close()
                moveTo(8f, 6f)
                horizontalLineTo(6f)
                verticalLineTo(7f)
                horizontalLineTo(8f)
                verticalLineTo(9f)
                horizontalLineTo(9f)
                verticalLineTo(7f)
                horizontalLineTo(11f)
                verticalLineTo(6f)
                horizontalLineTo(9f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(6f)
                close()
                moveTo(6f, 11f)
                horizontalLineTo(11f)
                verticalLineTo(12f)
                horizontalLineTo(6f)
                verticalLineTo(11f)
                close()
            }
        }.build()
        return _undefined!!
    }

private var _undefined: ImageVector? = null
