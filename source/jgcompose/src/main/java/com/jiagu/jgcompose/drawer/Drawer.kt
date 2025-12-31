package com.jiagu.jgcompose.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 抽屉
 *
 * @param color 背景色
 * @param context 抽屉内容
 * @param isShow 是否展开
 * @param onShow 点击展开事件
 * @param onClose 点击关闭事件
 * @receiver
 */
@Composable
fun Drawer(
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    context: @Composable BoxScope.() -> Unit = {},
    isShow: Boolean = false,
    onShow: () -> Unit,
    onClose: () -> Unit
) {
    if (isShow) {
        Row(
            modifier = modifier
                .clickable(false) { },
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.background(
                    color = color,
                )
            ) {
                context()
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "disable",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(30.dp)
                    .height(60.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .noEffectClickable {
                        onClose()
                    })
        }
    } else {
        Icon(
            imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
            contentDescription = "show",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier
                .padding(top = 10.dp)
                .width(30.dp)
                .height(60.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                )
                .noEffectClickable {
                    onShow()
                }
        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DrawerPreview() {
    var isShow by remember { mutableStateOf(false) }
    ComposeTheme {
        Column {
            Drawer(
                context = {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(120.dp)
                    )
                },
                onShow = {
                    isShow = true
                }, onClose = {
                    isShow = false
                }, isShow = isShow,
                color = Color.Gray
            )
        }
    }
}