package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.ui.components.OneTypeCombo
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.vm.LoginTypeEnum
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.jgcompose.textfield.PasswordTextField

val accountSecurityBindGlobalRowHeight = 40.dp

val accountSecurityBindGlobalTitleWidth = 120.dp

/**
 * 修改密碼
 */
@Composable
fun UpdatePassword(
    modifier: Modifier = Modifier,
    width: Dp,
    title: String,
    oldPassword: Boolean = false,
    newPassword: Boolean = false,
    vm: MineModel,
) {
    var value by remember {
        mutableStateOf("")
    }
    //密码有效校验
    val passwordValid = Validator.checkPassword(value) && value.isNotBlank()
    if (oldPassword) {
        vm.oldPassword = value
        vm.oldPasswordValid = passwordValid
    }
    if (newPassword) {
        vm.newPassword = value
        vm.newPasswordValid = passwordValid
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowFrame(
            modifier = Modifier
                .width(width)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = title,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
        ) {
            PasswordTextField(
                text = value,
                onValueChange = { value = it },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ),
                hint = title,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = if (!passwordValid) MaterialTheme.colorScheme.error else Color.Black
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                borderColor = if (!passwordValid && value.isNotBlank()) MaterialTheme.colorScheme.error else Color.Transparent,
                hintTextStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun BindEmail(
    modifier: Modifier = Modifier,
    width: Dp,
    vm: MineModel,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OneTypeCombo(
            modifier = Modifier, vm = vm, width = width, type = LoginTypeEnum.EMAIL
        )
    }
}

/**
 * 綁定手机号
 */
@Composable
fun BindPhoneNumber(
    modifier: Modifier = Modifier, vm: MineModel, width: Dp = accountSecurityBindGlobalTitleWidth
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OneTypeCombo(
            modifier = Modifier, vm = vm, width = width, type = LoginTypeEnum.PHONE_NUMBER
        )
    }
}

/**
 * 绑定通用验证码
 */
@Composable
fun BindCommonVerificationCode(
    modifier: Modifier = Modifier,
    width: Dp,
    vm: MineModel,
    verifyCodeType: VerifyCodeInfo.VerifyCodeTypeEnum
) {
    var verificationCode by remember { mutableStateOf("") }
    vm.verificationCode = verificationCode
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
                    text = stringResource(id = R.string.verification_code),
                    color = Color.Black,
                    width = width,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
        ) {
            NormalTextField(
                text = verificationCode,
                onValueChange = { verificationCode = it },
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(),
                borderColor = Color.Transparent,
                hint = stringResource(id = R.string.verification_code),
                textStyle = MaterialTheme.typography.bodyMedium,
                hintTextStyle = MaterialTheme.typography.bodySmall
            )
        }
        val backgroundColor =
            if (!vm.awaitFlag) MaterialTheme.colorScheme.primary else Color.Gray
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