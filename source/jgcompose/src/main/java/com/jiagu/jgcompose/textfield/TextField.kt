package com.jiagu.jgcompose.textfield

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.theme.ComposeTheme

/**
 * 普通输入框
 *
 * @param modifier 输入框modifier
 * @param text 文本
 * @param onValueChange 输入回调
 * @param enabled 输入框状态 默认true
 * @param readOnly 只读状态 默认false
 * @param showClearIcon 是否显示清除按钮 默认 true
 * @param backgroundColor 背景色
 * @param borderColor 边框颜色 默认
 * @param textStyle 文字样式
 * @param hint 提示文本
 * @param hintPosition 提示文本显示位置 TextAlign.Start
 * @param hintTextStyle 提示文本样式
 * @param keyboardOptions ime键盘选项
 * @param keyboardActions ime键盘动作
 * @param isLengthLimit 长度限制开关
 * @param showLengthLimit 显示长度限制 默认跟isLengthLimit一致
 * @param maxInputLength 最大文本长度
 */
@Composable
fun NormalTextField(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    showClearIcon: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    hint: String = "",
    hintPosition: TextAlign = TextAlign.Start,
    hintTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isLengthLimit: Boolean = false,
    showLengthLimit: Boolean = isLengthLimit,
    maxInputLength: Int = 10,
) {
    TextField(
        modifier = modifier,
        text = text,
        backgroundColor = backgroundColor,
        onValueChange = onValueChange,
        hint = hint,
        enabled = enabled,
        readOnly = readOnly,
        borderColor = borderColor,
        textStyle = textStyle,
        hintPosition = hintPosition,
        hintTextStyle = hintTextStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        showClearIcon = showClearIcon,
        isLengthLimit = isLengthLimit,
        showLengthLimit = showLengthLimit,
        maxInputLength = maxInputLength,
    )
}

/**
 *密码输入框
 *
 * @param modifier 输入框modifier
 * @param text 文本
 * @param onValueChange 输入回调
 * @param enabled 输入框状态 默认true
 * @param readOnly 只读状态 默认false
 * @param showClearIcon  是否显示清除按钮 默认 true
 * @param backgroundColor 背景色
 * @param borderColor 边框颜色 默认
 * @param textStyle textStyle 文字样式
 * @param hint hint 提示文本
 * @param hintPosition hintPosition 提示文本显示位置 TextAlign.Start
 * @param hintTextStyle hintTextStyle 提示文本样式
 * @param keyboardOptions ime键盘选项
 * @param keyboardActions ime键盘动作
 * @param isLengthLimit 长度限制开关
 * @param showLengthLimit 显示长度限制 默认跟isLengthLimit一致
 * @param maxInputLength 最大文本长度
 */
@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    showClearIcon: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    hint: String = "",
    hintPosition: TextAlign = TextAlign.Start,
    hintTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isLengthLimit: Boolean = false,
    showLengthLimit: Boolean = isLengthLimit,
    maxInputLength: Int = 10,
) {
    var passwordEncryption by remember {
        mutableStateOf(true)
    }
    TextField(
        modifier = modifier,
        text = text,
        backgroundColor = backgroundColor,
        onValueChange = onValueChange,
        hint = hint,
        enabled = enabled,
        readOnly = readOnly,
        borderColor = borderColor,
        textStyle = textStyle,
        hintPosition = hintPosition,
        hintTextStyle = hintTextStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        showClearIcon = showClearIcon,
        showEyeIcon = true,
        isLengthLimit = isLengthLimit,
        showLengthLimit = showLengthLimit,
        maxInputLength = maxInputLength,
        dataEncryption = passwordEncryption,
        dataEncryptionChange = {
            passwordEncryption = it
        },
        visualTransformation = if (passwordEncryption) PasswordVisualTransformation() else VisualTransformation.None,
    )
}

/**
 * 左侧图标输入框
 *
 * @param modifier 输入框modifier
 * @param text 文本
 * @param onValueChange 输入回调
 * @param leftIcon 左侧图标
 * @param enabled 输入框状态 默认true
 * @param readOnly 只读状态 默认false
 * @param showClearIcon  是否显示清除按钮 默认 true
 * @param backgroundColor 背景色
 * @param borderColor 边框颜色
 * @param textStyle 文字样式
 * @param hint 提示文本
 * @param hintPosition 提示文本显示位置 TextAlign.Start
 * @param hintTextStyle 提示文本样式
 * @param keyboardOptions ime键盘选项
 * @param keyboardActions ime键盘动作
 * @param showEyeIcon 明/密文图标显示 默认显示
 * @param isLengthLimit 长度限制开关
 * @param showLengthLimit 显示长度限制 默认跟isLengthLimit一致
 * @param maxInputLength 最大文本长度
 * @param dataEncryption 当前数据是否加密 true 加密 false 不加密
 * @param dataEncryptionChange 加密状态变化回调
 */
@Composable
fun LeftIconTextField(
    modifier: Modifier = Modifier,
    text: String,
    onValueChange: (String) -> Unit,
    leftIcon: @Composable () -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    showClearIcon: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    hint: String = "",
    hintPosition: TextAlign = TextAlign.Start,
    hintTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showEyeIcon: Boolean = false,
    isLengthLimit: Boolean = false,
    showLengthLimit: Boolean = isLengthLimit,
    maxInputLength: Int = 10,
    dataEncryption: Boolean = false,
    dataEncryptionChange: (Boolean) -> Unit = {},
) {
    TextField(
        modifier = modifier,
        text = text,
        backgroundColor = backgroundColor,
        onValueChange = onValueChange,
        hint = hint,
        enabled = enabled,
        readOnly = readOnly,
        borderColor = borderColor,
        textStyle = textStyle,
        hintPosition = hintPosition,
        hintTextStyle = hintTextStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        showClearIcon = showClearIcon,
        leftIcon = leftIcon,
        showEyeIcon = showEyeIcon,
        dataEncryption = dataEncryption,
        dataEncryptionChange = dataEncryptionChange,
        isLengthLimit = isLengthLimit,
        showLengthLimit = showLengthLimit,
        maxInputLength = maxInputLength,
        visualTransformation =
            if (dataEncryption) PasswordVisualTransformation() else VisualTransformation.None,
    )
}

/**
 * base text field
 *
 * @param modifier 输入框modifier
 * @param text 文本
 * @param backgroundColor 背景色
 * @param onValueChange 输入回调
 * @param hint 提示文本
 * @param leftIcon 左侧图标
 * @param enabled 输入框状态 默认true
 * @param readOnly 只读状态 默认false
 * @param borderColor 边框颜色
 * @param showClearIcon 是否显示清除按钮 默认 true
 * @param focusState 光标状态
 * @param textStyle 文字样式
 * @param hintPosition 提示文本显示位置 TextAlign.Start
 * @param hintTextStyle 提示文本样式
 * @param keyboardOptions ime键盘选项
 * @param keyboardActions ime键盘动作
 * @param visualTransformation 数据视觉
 * @param cursorBrush 光标样式
 * @param showEyeIcon 明/密文图标显示 默认显示
 * @param dataEncryption 当前数据是否加密 true 加密 false 不加密
 * @param isLengthLimit 长度限制开关
 * @param showLengthLimit 显示长度限制 默认跟isLengthLimit一致
 * @param maxInputLength 最大文本长度
 * @param dataEncryptionChange 加密状态变化回调
 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = Color.Transparent,
    onValueChange: (String) -> Unit,
    hint: String = "",
    leftIcon: @Composable () -> Unit = {},
    enabled: Boolean = true,
    readOnly: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    showClearIcon: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    hintPosition: TextAlign = TextAlign.Start,
    hintTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    showEyeIcon: Boolean = false,
    dataEncryption: Boolean = false,
    isLengthLimit: Boolean = false,
    showLengthLimit: Boolean = isLengthLimit,
    maxInputLength: Int = 10,
    dataEncryptionChange: (Boolean) -> Unit = {},
) {
    BasicTextField(
        value = text,
        onValueChange = {
            var value = it
            //长度限制
            if (isLengthLimit && value.length > maxInputLength) {
                value = value.substring(0, maxInputLength)
            }
            onValueChange(value)
        },
        modifier = modifier,
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        cursorBrush = cursorBrush,
        decorationBox = @Composable { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor, shape = RoundedCornerShape(
                            topStart = 10.dp, bottomStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp
                        )
                    )
                    .border(
                        border = BorderStroke(1.dp, color = borderColor),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                //左侧图标
                leftIcon()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 6.dp),
                    contentAlignment = when (textStyle.textAlign) {
                        //根据textAlign动态调整box content位置
                        TextAlign.End -> Alignment.CenterEnd
                        TextAlign.Center -> Alignment.Center
                        else -> Alignment.CenterStart
                    },
                ) {
                    // 当空字符时, 显示hint 并且没有焦点
                    if (text.isEmpty()) {
                        Text(
                            text = hint,
                            color = Color.Gray,
                            style = hintTextStyle,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = hintPosition,
                            maxLines = 1
                        )
                    } else {
                        // 原本输入框的内容
                        innerTextField()
                    }
                }
                //显示 清除 或者 眼睛 图标
                if (showEyeIcon || showClearIcon) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        //加密数据
                        if (showEyeIcon) {
                            val eyeImage =
                                if (!dataEncryption) R.drawable.eye_open else R.drawable.eye_close
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(30.dp)
                                    .clickable {
                                        dataEncryptionChange(!dataEncryption)
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = eyeImage),
                                    contentDescription = "eye state",
                                    modifier = Modifier.fillMaxSize(0.6f)
                                )
                            }
                        }
                        // 存在焦点 且 有输入内容时. 显示叉号
                        if (text.isNotEmpty() && showClearIcon && enabled && !readOnly) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(30.dp)
                                    .clickable { onValueChange.invoke("") },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    imageVector = Icons.Filled.Clear, // 清除图标
                                    contentDescription = "clear",
                                    modifier = Modifier.fillMaxSize(0.7f)
                                )
                            }
                        }
                    }
                }

                if (showLengthLimit) {
                    Text(
                        text = "${text.length}/${maxInputLength}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    )

}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun TextFieldPreview() {
    var text1 by remember {
        mutableStateOf("")
    }
    var text2 by remember {
        mutableStateOf("")
    }
    var text3 by remember {
        mutableStateOf("")
    }
    var text4 by remember {
        mutableStateOf("")
    }
    ComposeTheme {
        Column {
            NormalTextField(
                modifier = Modifier.height(30.dp),
                text = text1,
                onValueChange = {
                    text1 = it
                },
                backgroundColor = Color.White,
                hint = "普通输入框",
            )
            PasswordTextField(
                modifier = Modifier.height(30.dp),
                text = text2,
                onValueChange = { text2 = it },
                backgroundColor = Color.White,
                hint = "密码输入框"
            )
            LeftIconTextField(modifier = Modifier.height(30.dp), text = text3, onValueChange = {
                text3 = it
            }, backgroundColor = Color.White, hint = "左侧图标输入框", leftIcon = {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "icon")
            })
            TextField(
                modifier = Modifier.height(30.dp), text = text4,
                onValueChange = {
                    text4 = it
                },
                backgroundColor = Color.White,
                hint = "基础输入框",
                isLengthLimit = true,
            )
        }
    }
}