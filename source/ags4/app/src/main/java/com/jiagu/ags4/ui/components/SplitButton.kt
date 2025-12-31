package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.ComposeTheme


@Composable
fun SplitButton(
    modifier: Modifier,
    leftButton: @Composable (() -> Unit),
    rightButton: @Composable (() -> Unit)
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        leftButton()

        VerticalDivider(
            modifier = Modifier
                .width(2.dp)
                .height(60.dp) // 设置竖线的高度
                .background(Color.White), // 设置竖线的颜色
        )
        rightButton()
    }

}

@Preview(showBackground = false)
@Composable
fun SplitButtonPreview() {
    ComposeTheme {
        SplitButton(modifier = Modifier.width(242.dp), leftButton = {
            ImageButton(
                title = "Button1",
                image = R.drawable.default_lost,
                type = ImageButtonType.Vertical,
                onClick = {},
                modifier = Modifier
                    .width(120.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))

            )
        }, rightButton = {
            ImageButton(
                title = "Button1",
                image = R.drawable.default_lost,
                type = ImageButtonType.Vertical,
                onClick = {},
                modifier = Modifier
                    .width(120.dp)
                    .height(70.dp)
                    .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))

            )
        })
    }
}