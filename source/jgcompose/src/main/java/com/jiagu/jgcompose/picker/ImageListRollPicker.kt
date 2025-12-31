package com.jiagu.jgcompose.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.screen.ScreenColumn
import com.jiagu.jgcompose.theme.ComposeTheme


/**
 * 滚动效果 带图片的List picker
 *
 * @param items 列表项
 * @param onCancel 取消回调
 * @param onConfirm 确定回调 返回已选择的值
 */
@Composable
fun ImageListRollPicker(
    index: Int,
    images: List<Int>,
    items: List<String>,
    imageColor: Color = Color.Black,
    onCancel: () -> Unit = {},
    onConfirm: (Int, String) -> Unit
) {
    var selectedIndex by remember {
        mutableIntStateOf(index)
    }
    var selectedItem by remember {
        mutableStateOf(items.getOrElse(index) { "" })
    }
    ScreenColumn(content = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RollPicker(modifier = Modifier.fillMaxWidth(),
                value = selectedItem,
                item = { index, text ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val image = if (index in images.indices) {
                            images[index]
                        } else {
                            R.drawable.lost
                        }
                        Image(
                            painter = painterResource(id = image),
                            contentDescription = "image:${index}",
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(imageColor)
                        )
                        Label(
                            text = text,
                        )
                    }
                },
                list = items.toList(),
                onValueChange = { i, v ->
                    selectedIndex = i
                    selectedItem = v
                })
            // 中间两道横线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
            ) {
                HorizontalDivider(Modifier.padding(horizontal = 15.dp))
                HorizontalDivider(
                    Modifier
                        .padding(horizontal = 15.dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }, onCancel = onCancel, onConfirm = {
        onConfirm(selectedIndex, selectedItem)
    })
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ImageListRollPickerPreview() {
    ComposeTheme {
        var show by remember {
            mutableStateOf(true)
        }
        Box {
            Row {
                Button(onClick = { show = !show }) {
                    Text(text = "图片文本列表")
                }

            }
            if (show) {
                ImageListRollPicker(index = 1, images = listOf(
                    R.drawable.eye_open,
                    R.drawable.eye_open,
                    R.drawable.eye_open,
                    R.drawable.eye_open,
                ), items = listOf(
                    "111",
                    "222",
                    "333",
                    "444",
                    "555",
                ), onCancel = {}, onConfirm = { _, _ -> })
            }
        }
    }
}