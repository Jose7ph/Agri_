package com.jiagu.ags4.scene.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.vm.AccountModel
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.textfield.PasswordTextField

//全局行高
val loginGlobalRowHeight = 36.dp

//全局title宽度
val loginGlobalTitleWidth = 120.dp

@Composable
fun PasswordRow(
    modifier: Modifier = Modifier, vm: AccountModel, width: Dp,
    imeAction: ImeAction = ImeAction.Done
) {
    //密码有效校验
    val passwordValid =
        (Validator.checkPassword(vm.password.value) && vm.password.value.isNotBlank())
    vm.passwordValid = passwordValid
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowFrame {
            Box(
                modifier = Modifier
                    .width(width)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.password), color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ShadowFrame {
            PasswordTextField(
                text = vm.password.value,
                onValueChange = { vm.password.value = it },
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .background(color = Color.White, shape = MaterialTheme.shapes.small),
                hint = stringResource(id = R.string.password),
                textStyle = TextStyle(
                    color = if (!passwordValid) MaterialTheme.colorScheme.error else Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                borderColor = if (!passwordValid && vm.password.value.isNotBlank()) MaterialTheme.colorScheme.error else Color.Transparent,
            )
        }
    }
}

/**
 * 验证码
 */
@Composable
fun VerificationCodeRow(
    modifier: Modifier = Modifier,
    width: Dp,
    vm: AccountModel,
    verifyCodeType: VerifyCodeInfo.VerifyCodeTypeEnum,
    imeAction: ImeAction = ImeAction.Done
) {
    var verificationCode by remember { mutableStateOf("") }
    // 使用 collectAsState 来获取最新的倒计时时间
    val remainingTime by vm.remainingTime.collectAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowFrame {
            Box(
                modifier = Modifier
                    .width(width)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.verification_code), color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ShadowFrame(modifier = Modifier.weight(1f)) {
            NormalTextField(
                text = verificationCode,
                onValueChange = {
                    verificationCode = it
                    vm.verificationCode = it
                },
                modifier = Modifier

                    .wrapContentHeight(),
                hint = stringResource(id = R.string.verification_code),
                textStyle = MaterialTheme.typography.bodyMedium,
                hintTextStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                borderColor = Color.Transparent
            )
        }
        val backgroundColor = if (!vm.awaitFlag) MaterialTheme.colorScheme.primary else Color.Gray
        Box(
            modifier = Modifier
                .width(width + 30.dp)
                .fillMaxHeight()
                .background(
                    color = backgroundColor, shape = MaterialTheme.shapes.small
                )
                .clickable(!vm.awaitFlag) {
                    if (!vm.awaitFlag) {
                        vm.getVerifyCode(verifyCodeType, 60L)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            val text = if (vm.awaitFlag && remainingTime > 0) {
                // 如果处于冷却状态，显示倒计时文本
                remainingTime.toString() + stringResource(id = R.string.second_after_be_resent)
            } else {
                // 否则，显示按钮文本
                stringResource(id = R.string.get_verification_code)
            }
            AutoScrollingText(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
