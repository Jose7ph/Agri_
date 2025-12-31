package com.jiagu.ags4.scene.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.ui.components.LoginTypeCombo
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.goto
import com.jiagu.ags4.vm.AccountModel
import com.jiagu.ags4.vm.LocalAccountModel
import com.jiagu.api.helper.AssetHelper
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import dev.jeziellago.compose.markdowntext.MarkdownText

private const val AGREEMENT_FILE_NAME = "agreement.md"

/**
 * 注册
 */
@Composable
fun LoginRegister() {
    val navController = LocalNavController.current
    val vm = LocalAccountModel.current
    val verifyCodeType = VerifyCodeInfo.VerifyCodeTypeEnum.ACCOUNT_REGISTER
    val context = LocalContext.current
    var selected by remember {
        mutableStateOf(false)
    }

    val agreementExists = AssetHelper.isFileExist(context, AGREEMENT_FILE_NAME)
    MainContent(
        title = stringResource(id = R.string.register),
        breakAction = { if (!navController.popBackStack()) (context as LoginActivity).finish() },
        barAction = {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(26.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .clickable {
                        navController.goto(
                            "login_pass", "login_register"
                        )
                    }, contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.login),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }) {
        Column(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .fillMaxSize()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LoginTypeCombo(
                modifier = Modifier.height(loginGlobalRowHeight),
                vm = vm,
                height = loginGlobalRowHeight,
                width = loginGlobalTitleWidth,
                imeAction = ImeAction.Next
            )
            //验证码
            VerificationCodeRow(
                modifier = Modifier.height(loginGlobalRowHeight),
                width = loginGlobalTitleWidth,
                vm = vm,
                verifyCodeType = verifyCodeType,
                imeAction = ImeAction.Next
            )
            //姓名
            NameRow(
                modifier = Modifier, vm = vm, imeAction = ImeAction.Next
            )
            //密码
            PasswordRow(
                modifier = Modifier.height(loginGlobalRowHeight),
                vm,
                width = loginGlobalTitleWidth,
            )
            //用户协议显示判断
            if (agreementExists) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(loginGlobalRowHeight - 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = selected, onClick = {
                            selected = !selected
                        }, modifier = Modifier.width(20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.register_agreement),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(id = R.string.register_privacy_agreement),
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable {
                                context.showDialog {
                                    val contexts =
                                        AssetHelper.readLines(context, AGREEMENT_FILE_NAME)
                                    ScreenPopup(width = 560.dp, content = {
                                        Box(
                                            modifier = Modifier
                                                .padding(20.dp)
                                                .fillMaxWidth()
                                        ) {
                                            LazyColumn(
                                                modifier = Modifier.heightIn(
                                                    40.dp, 200.dp
                                                )
                                            ) {
                                                item {
                                                    MarkdownText(
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .fillMaxWidth(),
                                                        markdown = contexts.joinToString(
                                                            separator = "\n"
                                                        ),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                }
                                            }
                                        }
                                    }, onDismiss = {
                                        context.hideDialog()
                                    }, onConfirm = {
                                        selected = true
                                        context.hideDialog()
                                    })
                                }
                            })
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            //当 agreementExists = true 时 必须selected = true 才可以注册
            val checkAgreement = if (agreementExists) {
                selected
            }
            //agreementExists = false 直接注册
            else {
                true
            }
            val enabled =
                vm.accountValid && vm.passwordValid && vm.name.isNotBlank() && vm.verificationCode.isNotBlank() && checkAgreement
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        //注册
                        vm.register()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = enabled,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(id = R.string.register),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

    }
}

@Composable
fun NameRow(
    modifier: Modifier = Modifier, vm: AccountModel, imeAction: ImeAction = ImeAction.Done,
) {
    var name by remember { mutableStateOf("") }
    vm.name = name
    Row(
        modifier = modifier.height(loginGlobalRowHeight),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShadowFrame {
            Box(
                modifier = Modifier
                    .width(loginGlobalTitleWidth)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.name),
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        ShadowFrame {
            NormalTextField(
                text = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(color = Color.White, shape = MaterialTheme.shapes.small),
                hint = stringResource(id = R.string.name),
                textStyle = MaterialTheme.typography.bodyMedium,
                borderColor = Color.Transparent,
                keyboardOptions = KeyboardOptions(imeAction = imeAction)
            )
        }
    }
}