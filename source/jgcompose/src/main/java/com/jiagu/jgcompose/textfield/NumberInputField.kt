package com.jiagu.jgcompose.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.theme.ComposeTheme

private const val CLEAR = "Clear"
private const val BACKSPACE = "BackSpace"
private const val SIGN = "+/-"
private const val CONFORM = "Conform"
private const val POINT = "."
private const val MINUS = "-"
private const val DEFAULT_VALUE = "0"

/**
 * Number input field
 * 计算器样式
 *
 * @param modifier
 * @param number
 * @param onConform
 * @receiver
 */
@Composable
fun NumberInputField(modifier: Modifier = Modifier, number: String, onConform: (String) -> Unit) {
    val keyBoxHeight = 30.dp
    val keySpace = 6.dp
    val optKeyColor = Color.White
    val keyShape = MaterialTheme.shapes.extraSmall
    var text by remember {
        mutableStateOf(number)
    }
    Column(
        modifier = modifier
            .background(
                color = Color.White, shape = MaterialTheme.shapes.small
            )
            .clip(shape = MaterialTheme.shapes.small)
            .padding(10.dp)
            .clickable(enabled = false) { }, verticalArrangement = Arrangement.spacedBy(keySpace)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyBoxHeight)
                .background(color = Color.Gray, shape = keyShape)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text, modifier = Modifier.fillMaxWidth(), color = Color.White
            )
        }
        Row(
            modifier = Modifier.height(keyBoxHeight),
            horizontalArrangement = Arrangement.spacedBy(keySpace)
        ) {
            KeyBox(modifier = Modifier.weight(1f), key = "7", clickProcess = {
                clickProcess(addText = "7", text = text) {
                    text = it
                }
            })
            KeyBox(modifier = Modifier.weight(1f), key = "8", clickProcess = {
                clickProcess(addText = "8", text = text) {
                    text = it
                }
            })
            KeyBox(modifier = Modifier.weight(1f), key = "9", clickProcess = {
                clickProcess(addText = "9", text = text) {
                    text = it
                }
            })
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.tertiary, shape = keyShape)
                    .clickable {
                        clickProcess(addText = BACKSPACE, text = text) {
                            text = it
                        }
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = BACKSPACE,
                    tint = optKeyColor
                )
            }
        }
        Row(
            modifier = Modifier.height(keyBoxHeight),
            horizontalArrangement = Arrangement.spacedBy(keySpace)
        ) {
            KeyBox(modifier = Modifier.weight(1f), key = "4", clickProcess = {
                clickProcess(addText = "4", text = text) {
                    text = it
                }
            })
            KeyBox(modifier = Modifier.weight(1f), key = "5", clickProcess = {
                clickProcess(addText = "5", text = text) {
                    text = it
                }
            })
            KeyBox(modifier = Modifier.weight(1f), key = "6", clickProcess = {
                clickProcess(addText = "6", text = text) {
                    text = it
                }
            })
            KeyBox(modifier = Modifier.weight(1f),
                key = "C",
                keyBackgroundColor = MaterialTheme.colorScheme.error,
                clickProcess = {
                    clickProcess(addText = "Clear", text = text) {
                        text = it
                    }
                })
        }
        Row(
            modifier = Modifier.height(keyBoxHeight * 2 + keySpace),
            horizontalArrangement = Arrangement.spacedBy(keySpace)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(keySpace)
            ) {
                KeyBox(modifier = Modifier.weight(1f), key = "1", clickProcess = {
                    clickProcess(addText = "1", text = text) {
                        text = it
                    }
                })
                KeyBox(modifier = Modifier.weight(1f), key = SIGN, clickProcess = {
                    clickProcess(addText = SIGN, text = text) {
                        text = it
                    }
                })

            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(keySpace)
            ) {
                KeyBox(modifier = Modifier.weight(1f), key = "2", clickProcess = {
                    clickProcess(addText = "2", text = text) {
                        text = it
                    }
                })
                KeyBox(modifier = Modifier.weight(1f), key = "0", clickProcess = {
                    clickProcess(addText = "0", text = text) {
                        text = it
                    }
                })
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(keySpace)
            ) {
                KeyBox(modifier = Modifier.weight(1f), key = "3", clickProcess = {
                    clickProcess(addText = "3", text = text) {
                        text = it
                    }
                })
                KeyBox(modifier = Modifier.weight(1f), key = POINT, clickProcess = {
                    clickProcess(addText = POINT, text = text) {
                        text = it
                    }
                })
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(color = MaterialTheme.colorScheme.primary, shape = keyShape)
                    .clickable {
                        if (text.endsWith(POINT)) {
                            text += DEFAULT_VALUE
                        }
                        onConform(text)
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = CONFORM,
                    tint = optKeyColor
                )
            }
        }
    }
}

/**
 * 按键点击处理
 */
private fun clickProcess(text: String, addText: String, onClick: (String) -> Unit) {
    //当前值 = "0"
    if (text == DEFAULT_VALUE) {
        when (addText) {
            //. -> 直接拼接
            POINT -> {
                onClick(text + addText)
            }
            //清除,回退，正负号 -> 无效果
            CLEAR, BACKSPACE, SIGN -> {
                return
            }
            //其他值 -> 直接修改
            else -> {
                onClick(addText)
            }
        }
    } else {
        when (addText) {
            //. -> 判断是否已经存在.
            //存在 -> 无效果
            //不存在 -> 直接拼接
            POINT -> {
                if (text.contains(POINT)) {
                    return
                }
                onClick(text + addText)

            }
            //+/- -> 判断当前值是否以-开头
            //以-开头 -> 删除-
            //不存在- -> 添加-
            SIGN -> {
                if (text.startsWith(MINUS)) {
                    onClick(text.substring(1))
                } else {
                    onClick(MINUS + text)
                }
            }
            //清除 -> 直接设置为0
            CLEAR -> {
                onClick(DEFAULT_VALUE)
            }
            //回退 -> 删除最后一个字符
            BACKSPACE -> {
                if (text.length == 1 || (text.length == 2 && text.startsWith(
                        MINUS
                    ))
                ) {
                    onClick(DEFAULT_VALUE)
                } else {
                    onClick(text.substring(0, text.length - 1))
                }
            }
            //其他值 -> 直接拼接
            else -> {
                onClick(text + addText)

            }
        }
    }
}

@Composable
fun KeyBox(
    modifier: Modifier = Modifier,
    key: String,
    keyBackgroundColor: Color = Color.Gray,
    keyShape: Shape = MaterialTheme.shapes.extraSmall,
    clickProcess: () -> Unit
) {
    val keyTextColor = Color.White
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color = keyBackgroundColor, shape = keyShape)
            .clickable {
                clickProcess()
            }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = key,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = keyTextColor
        )
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun CustomImePreview() {
    ComposeTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NumberInputField(number = "10") {

            }
        }
    }
}