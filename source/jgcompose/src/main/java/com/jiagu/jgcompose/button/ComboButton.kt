package com.jiagu.jgcompose.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.picker.ImageListPicker
import com.jiagu.jgcompose.picker.ImageListRollPicker
import com.jiagu.jgcompose.picker.ListPicker
import com.jiagu.jgcompose.picker.ListRollPicker
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.ComposeTheme


/**
 * 列表选择 button 建议选项>=5使用
 *
 * @param modifier 装饰器
 * @param index 已选择的索引
 * @param value 下拉默认值 默认:按钮文本
 * @param rowItemNum 列表项一行显示个数 默认5
 * @param items 列表项
 * @param itemHeight 列表项高度
 * @param backgroundColor 按钮背景色
 * @param borderColor 按钮边框线条颜色
 * @param shape 按钮形状
 * @param textColor 按钮文本颜色
 * @param textStyle 按钮文本样式
 * @param showIcon 是否显示右侧图标
 * @param iconColor 右侧图标颜色
 * @param onCancel 返回回调
 * @param onConfirm 确定回调
 * @receiver
 * @receiver
 */
@Composable
fun ComboListButton(
    modifier: Modifier = Modifier,
    index: Int,
    value: String,
    rowItemNum: Int = 5,
    items: List<String>,
    itemHeight: Dp = 30.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    showIcon: Boolean = true,
    iconColor: Color = textColor,
    onCancel: () -> Unit = {},
    onConfirm: (Int) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .noEffectClickable {
                context.showDialog {
                    ListPicker(
                        rowItemNum = rowItemNum,
                        selectedIndexes = listOf(index),
                        items = items,
                        isSingleSelect = true,
                        itemHeight = itemHeight,
                        onCancel = {
                            onCancel()
                            context.hideDialog()
                        },
                        onConfirm = {
                            if (it.isEmpty()) onConfirm(0) else onConfirm(it[0])
                            context.hideDialog()
                        })
                }
            }, contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = 6.dp)
            ) {
                AutoScrollingText(
                    text = value,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
            if (showIcon) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "arrow right",
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.aspectRatio(0.6f)
                )
            }
        }
    }
}

/**
 *列表选择 button 建议选项>=5使用
 *
 * @param modifier 装饰器
 * @param index 已选择的索引
 * @param value 下拉默认值 默认:按钮文本
 * @param rowItemNum 列表项一行显示个数 默认5
 * @param items 列表项
 * @param images 列表项对应图片 需要跟items顺序一致
 * @param itemHeight 列表项高度
 * @param backgroundColor 按钮背景色
 * @param borderColor 按钮边框线条颜色
 * @param shape 按钮形状
 * @param textColor 按钮文本颜色
 * @param textStyle 按钮文本样式
 * @param showIcon 是否显示右侧图标
 * @param imageColorChange 图片颜色转换 false时 图片不会根据选中效果改变颜色
 * @param iconColor 右侧图标颜色
 * @param onCancel 返回回调
 * @param onConfirm 确定回调
 */
@Composable
fun ComboImageListButton(
    modifier: Modifier = Modifier,
    index: Int,
    value: String,
    rowItemNum: Int = 5,
    items: List<String>,
    images: List<Int>,
    itemHeight: Dp = 30.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    showIcon: Boolean = true,
    imageColorChange: Boolean = true,
    iconColor: Color = textColor,
    onCancel: () -> Unit = {},
    onConfirm: (Int) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .noEffectClickable {
                context.showDialog {
                    ImageListPicker(
                        rowItemNum = rowItemNum,
                        selectedIndexes = listOf(index),
                        items = items,
                        images = images,
                        isSingleSelect = true,
                        itemHeight = itemHeight,
                        imageColorChange = imageColorChange,
                        onCancel = {
                            onCancel()
                            context.hideDialog()
                        },
                        onConfirm = {
                            if (it.isEmpty()) onConfirm(0) else onConfirm(it[0])
                            context.hideDialog()
                        })
                }
            }, contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(images[index]),
                        contentDescription = value,
                        modifier = Modifier.fillMaxHeight(0.8f)
                    )
                    AutoScrollingText(
                        text = value,
                        color = textColor,
                        modifier = Modifier.fillMaxWidth(),
                        style = textStyle,
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (showIcon) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "arrow right",
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.aspectRatio(0.6f)
                )
            }
        }
    }
}


/**
 * 滚动选择 button 建议选项<=5使用
 *
 * @param modifier 装饰器
 * @param index 已选择的索引
 * @param value 下拉默认值 默认:按钮文本
 * @param comboItems 列表项
 * @param backgroundColor 按钮背景色
 * @param borderColor 按钮边框线条颜色
 * @param shape 按钮形状
 * @param textColor  按钮文本颜色
 * @param textStyle 按钮文本样式
 * @param showIcon 是否显示右侧图标
 * @param iconColor 右侧图标颜色
 * @param onCancel 返回回调
 * @param onConfirm 确定回调
 */
@Composable
fun ComboRollButton(
    modifier: Modifier = Modifier,
    index: Int,
    value: String,
    items: List<String>,
    itemTips: List<String> = listOf(),
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    showIcon: Boolean = true,
    iconColor: Color = textColor,
    onCancel: () -> Unit = {},
    onConfirm: (Int, String) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .noEffectClickable {
                context.showDialog {
                    ListRollPicker(index = index, items = items, itemTips = itemTips, onCancel = {
                        onCancel()
                        context.hideDialog()
                    }, onConfirm = { idx, v ->
                        onConfirm(idx, v)
                        context.hideDialog()
                    })
                }
            }, contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = 6.dp)
            ) {
                AutoScrollingText(
                    text = value,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
            if (showIcon) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "arrow right",
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.aspectRatio(0.6f)
                )
            }
        }
    }
}

/**
 * 滚动选择 button 建议选项<=5使用
 *
 * @param modifier 装饰器
 * @param index 已选择的索引
 * @param value 下拉默认值 默认:按钮文本
 * @param comboItems 列表项
 * @param images 列表项图片 需要跟 comboItems顺序一致
 * @param backgroundColor 按钮背景色
 * @param borderColor 按钮边框线条颜色
 * @param shape 按钮形状
 * @param textColor 按钮文本颜色
 * @param textStyle 按钮文本样式
 * @param showIcon 是否显示右侧图标
 * @param iconColor 右侧图标颜色
 * @param onCancel 返回回调
 * @param onConfirm 确定回调
 * @receiver
 */
@Composable
fun ComboImageRollButton(
    modifier: Modifier = Modifier,
    index: Int,
    value: String,
    comboItems: List<String>,
    images: List<Int>,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.small,
    textColor: Color = Color.White,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    showIcon: Boolean = true,
    iconColor: Color = textColor,
    onCancel: () -> Unit = {},
    onConfirm: (Int, String) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor != null) {
                    Modifier.border(width = 1.dp, color = borderColor, shape = shape)
                } else {
                    Modifier
                }
            )
            .noEffectClickable {
                context.showDialog {
                    ImageListRollPicker(
                        index = index,
                        items = comboItems,
                        images = images,
                        onCancel = {
                            onCancel()
                            context.hideDialog()
                        },
                        onConfirm = { idx, v ->
                            onConfirm(idx, v)
                            context.hideDialog()
                        })
                    ListRollPicker(index = index, items = comboItems, onCancel = {
                        onCancel()
                        context.hideDialog()
                    }, onConfirm = { idx, v ->
                        onConfirm(idx, v)
                        context.hideDialog()
                    })
                }
            }, contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(images[index]),
                    contentDescription = value,
                    modifier = Modifier.fillMaxHeight(0.8f)
                )
                AutoScrollingText(
                    text = value,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(x = 6.dp)
            ) {
                AutoScrollingText(
                    text = value,
                    color = textColor,
                    modifier = Modifier.fillMaxWidth(),
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
            if (showIcon) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "arrow right",
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.aspectRatio(0.6f)
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun ComboButtonPreview() {
    ComposeTheme {
        Column {
            ComboListButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp),
                index = 0,
                value = "11122222222",
                items = listOf("111", "222", "333"),
                onConfirm = { _ -> },
                onCancel = {})

            ComboImageListButton(
                modifier = Modifier
                    .width(100.dp)
                    .height(30.dp),
                index = 1,
                value = "111111111111222",
                items = listOf("11111111111", "222", "333"),
                images = listOf(R.drawable.eye_close, R.drawable.lost, R.drawable.eye_open),
                onConfirm = { _ -> },
                onCancel = {

                })
        }
    }
}