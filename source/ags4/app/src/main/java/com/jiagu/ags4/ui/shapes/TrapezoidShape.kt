package com.jiagu.ags4.ui.shapes

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class TrapezoidShape(private val offset: Float, private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            Path().apply {
                moveTo(cornerRadius + offset, 0f)
                lineTo(size.width - cornerRadius - offset, 0f)
                lineTo(size.width - cornerRadius - offset, size.height)
                lineTo(cornerRadius, size.height)
                arcTo(
                    Rect(
                        left = 0f,
                        top = size.height - 2 * cornerRadius,
                        right = 2 * cornerRadius,
                        bottom = size.height
                    ), 90f, 90f, false
                )
                lineTo(offset, cornerRadius)
                arcTo(
                    Rect(
                        left = offset,
                        top = 0f,
                        right = offset + 2 * cornerRadius,
                        bottom = 2 * cornerRadius
                    ), 180f, 90f, false
                )
                close()
            }
        )
    }
}


class TrapezoidShape1(private val percent: Float, private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val x = 0f
        return Outline.Generic(
            Path().apply {
                moveTo(cornerRadius, 0f)
                lineTo(size.width * percent - cornerRadius, 0f)
                arcTo(
                    Rect(
                        left = size.width * percent - cornerRadius,
                        top = 0f,
                        right = size.width * percent + cornerRadius,
                        bottom = 2 * cornerRadius
                    ), 270f, 90f, false
                )
                lineTo(size.width - cornerRadius, size.height)
                arcTo(
                    Rect(
                        left = size.width - 2 * cornerRadius,
                        top = size.height - 2 * cornerRadius,
                        right = size.width,
                        bottom = size.height
                    ), 0f, 90f, false
                )
                lineTo(cornerRadius, size.height)
                arcTo(
                    Rect(
                        left = 0f,
                        top = size.height - 2 * cornerRadius,
                        right = 2 * cornerRadius,
                        bottom = size.height
                    ), 90f, 90f, false
                )
                lineTo(0f, cornerRadius)
                arcTo(
                    Rect(
                        left = 0f,
                        top = 0f,
                        right = 2 * cornerRadius,
                        bottom = 2 * cornerRadius
                    ), 180f, 90f, false
                )
                close()
            }
        )
    }
}