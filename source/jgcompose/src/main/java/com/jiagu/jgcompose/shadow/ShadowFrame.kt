package com.jiagu.jgcompose.shadow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * Shadow frame
 *
 * @param modifier
 * @param backgroundColor
 * @param shape
 * @param shadowShape
 * @param elevation
 * 阴影（2dp - 4dp）：适合轻量级的卡片或元素，阴影效果较为微妙。
 * 等阴影（6dp - 8dp）：适合普通卡片或需要一定层次感的元素。
 * 阴影（12dp - 16dp）：适合需要强烈视觉效果的元素。
 * @param content
 * @receiver
 */
@Composable
fun ShadowFrame(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    shadowShape: CornerBasedShape = shape,
    elevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.shadow(elevation = elevation, shape = shadowShape),
        color = backgroundColor,
        shape = shape
    ) {
        content()
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ShadowFramePreview() {
    ComposeTheme {
        Column {
            ShadowFrame(
                modifier = Modifier
                    .width(100.dp)
                    .height(40.dp)
            ) {

            }
        }
    }
}