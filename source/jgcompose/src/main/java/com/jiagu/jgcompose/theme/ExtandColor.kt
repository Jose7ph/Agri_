package com.jiagu.jgcompose.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 定义扩展颜色
data class ExtendedColors(
    val warning: Color,
    val normal: Color,
    val customBackground: Color,
    val buttonDisabled: Color,
    val cancel: Color,
    val groupButton: Color,
    val groupButtonDisabled: Color,
)


// 创建 CompositionLocal
val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        warning = Color(0xFFFFFF00),
        normal = Color(0xFF00FF00),
        customBackground = Color(0xFF000000),
        buttonDisabled = Color(0xA3AFAFAF),
        cancel = Color(0xFFBEB9B9),
        groupButton = Color(0xFFF7F2F2),
        groupButtonDisabled = Color(0xFF8C8C8C),
    )
}