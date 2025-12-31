package com.jiagu.jgcompose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 屏幕column
 *
 * @param content 内容
 * @param buttons 条件按钮
 * @param paddingTop 距离顶部的padding(这个值越大组件高度越低)
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 */
@Composable
fun ScreenColumn(
    content: @Composable () -> Unit,
    buttons: @Composable () -> Unit = {},
    paddingTop: Dp = 150.dp,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //幕布
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(color = DrawerDefaults.scrimColor)
                .clickable(false) { }
        )
        Column(
            modifier = Modifier
                .padding(top = paddingTop)
        ) {
            //操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color = Color.White)
                    .padding(horizontal = 30.dp, vertical = 5.dp)
                    .clickable(false) { },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //重置
                OperateButton(
                    text = stringResource(id = R.string.cancel)
                ) {
                    onCancel()
                }
                //筛选条件
                buttons()
                //确定
                OperateButton(text = stringResource(id = R.string.confirm)) {
                    onConfirm()
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .clickable(false) { }, thickness = 1.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
                    .clickable(false) { }
            ) {
                content()
            }
        }
    }
}

/**
 * 屏幕column
 *
 * @param content 内容
 * @param paddingTop 距离顶部的padding(这个值越大组件高度越低)
 * @param onSearch 检索回调 该回调在输入同时触发
 * @param onCancel 取消回调
 * @param onConfirm 确定回调
 */
@Composable
fun ScreenSearchColumn(
    content: @Composable () -> Unit,
    paddingTop: Dp = 150.dp,
    onSearch: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    var search by remember {
        mutableStateOf("")
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //幕布
        Spacer(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = DrawerDefaults.scrimColor)
                .clickable(false) { }
        )
        Column(
            modifier = Modifier
                .padding(top = paddingTop)
        ) {
            //操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color = Color.White)
                    .padding(horizontal = 30.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //重置
                OperateButton(
                    text = stringResource(id = R.string.cancel)
                ) {
                    onCancel()
                }
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NormalTextField(
                        modifier = Modifier.weight(1f),
                        text = search,
                        onValueChange = {
                            search = it
                        },
                        textStyle = TextStyle.Default.copy(
                            textAlign = TextAlign.Start
                        ),
                        showClearIcon = false
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = Modifier.fillMaxHeight().noEffectClickable {
                            onSearch(search)
                        }
                    )
                }
                //确定
                OperateButton(text = stringResource(id = R.string.confirm)) {
                    onConfirm()
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .clickable(false) { }, thickness = 1.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White)
                    .clickable(false) { }
            ) {
                content()
            }
        }
    }
}

/**
 * 操作按钮
 *
 * @param text 按钮名称
 * @param onClick 点击回调
 * @receiver
 */
@Composable
fun OperateButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .widthIn(min = 60.dp, max = 90.dp)
            .height(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(0.dp)
    ) {
        AutoScrollingText(
            text = text,
            modifier = Modifier,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ScreenPreview() {
    ComposeTheme {
        var num by remember {
            mutableStateOf(2)
        }
        Row {
            Button(onClick = {
                num = 1
            }) {
                Text(text = "屏幕")
            }
            Button(onClick = {
                num = 2
            }) {
                Text(text = "带搜索的屏幕")
            }
        }
        when (num) {
            1 -> {
                ScreenColumn(
                    content = {},
                    buttons = {},
                    onCancel = {
                        num = 0
                    },
                    onConfirm = {},
                )
            }

            2 -> {
                ScreenSearchColumn(
                    content = {},
                    onCancel = {
                        num = 0
                    },
                    onConfirm = {},
                    onSearch = {}
                )
            }
        }
    }
}