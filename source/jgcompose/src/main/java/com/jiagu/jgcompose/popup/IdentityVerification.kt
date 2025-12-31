package com.jiagu.jgcompose.popup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.jgcompose.R
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.theme.ComposeTheme
import com.jiagu.jgcompose.utils.Validator

/**
 * 身份验证弹窗
 * todo 部分提示未作国际化
 *
 * @param onConfirm 确定回调 (身份认证接口调用)
 * @param onDismiss 取消回调 (关闭回调)
 */
@Composable
fun IdentityVerificationPopup(
    onConfirm: (String, String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }
    var idNumber by remember {
        mutableStateOf("")
    }
    //step:1->开始验证 2->验证成功 3->验证失败
    var step by remember {
        mutableIntStateOf(1)
    }

    var errorMsg by remember {
        mutableStateOf("认证失败, 请重试")
    }
    ScreenPopup(
        width = 360.dp,
        content = {
            Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "实名认证", style = MaterialTheme.typography.titleLarge)
                }
                when (step) {
                    1 -> {
                        Row(
                            modifier = Modifier.height(30.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "真实姓名:",
                                modifier = Modifier.width(110.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            NormalTextField(
                                modifier = Modifier.weight(1f),
                                text = name,
                                onValueChange = {
                                    name = it
                                },
                                hint = "请输入真实姓名",
                                showClearIcon = false,
                            )
                        }
                        Row(
                            modifier = Modifier.height(30.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "身份证号码:",
                                modifier = Modifier.width(110.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            NormalTextField(
                                modifier = Modifier.weight(1f),
                                text = idNumber, onValueChange = {
                                    idNumber = it
                                },
                                hint = "请输入18位身份证号",
                                showClearIcon = false,
                                borderColor = if (idNumber.isEmpty() || Validator.checkIDNumber(
                                        idNumber
                                    )
                                ) {
                                    MaterialTheme.colorScheme.outline
                                } else {
                                    Color.Red
                                }
                            )
                        }
                    }

                    2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "认证中, 请稍等",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    3 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMsg,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    4 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "认证通过",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    onConfirm(name, idNumber) { success, msg ->
                        if (success) { //成功
                            step = 4
                        } else {//失败
                            step = 3
                            if (msg.isNotBlank()) {
                                errorMsg = msg
                            }
                        }
                    }
                }

                3 -> step = 1
                4 -> onDismiss()
            }
        },
        onDismiss = onDismiss,
        confirmText = when (step) {
            3 -> R.string.retry
            else -> R.string.confirm
        },
        confirmEnable = when (step) {
            1 -> Validator.checkIDNumber(idNumber) && name.isNotEmpty()
            else -> true
        },
    )
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun IdentityVerificationPreview() {
    ComposeTheme {
        Column {
            IdentityVerificationPopup(onDismiss = {}, onConfirm = { _, _, _ ->

            })
        }
    }
}