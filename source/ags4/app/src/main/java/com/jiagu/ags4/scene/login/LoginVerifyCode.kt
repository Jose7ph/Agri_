package com.jiagu.ags4.scene.login

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.ui.components.LoginTypeCombo
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.goto
import com.jiagu.ags4.vm.LocalAccountModel
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 验证码登陆
 */
@Composable
fun LoginVerifyCode() {
    val navController = LocalNavController.current
    val activity = LocalActivity.current as LoginActivity
    val vm = LocalAccountModel.current
    val verifyCodeType = VerifyCodeInfo.VerifyCodeTypeEnum.MOBILE_LOGIN
    MainContent(
        title = stringResource(id = R.string.verification_code_login),
        breakAction = { if (!navController.popBackStack()) activity.finish() },
        barAction = {
            AutoScrollingText(
                modifier = Modifier.clickable {
                    navController.goto(
                        "login_register",
                        "login_verify_code"
                    )
                },
                text = stringResource(id = R.string.sign_up),
                style = MaterialTheme.typography.bodyMedium
            )
        }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(30.dp),
        ) {
            LoginTypeCombo(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(loginGlobalRowHeight),
                vm = vm,
                height = loginGlobalRowHeight,
                width = loginGlobalTitleWidth,
                imeAction = ImeAction.Next
            )
            //验证码
            VerificationCodeRow(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(loginGlobalRowHeight),
                width = loginGlobalTitleWidth,
                vm = vm,
                verifyCodeType = verifyCodeType
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoScrollingText(
                    modifier = Modifier.clickable {
                        navController.goto(
                            "login_pass",
                            "login_verify_code"
                        )
                    },
                    text = stringResource(id = R.string.password_login),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier, verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    val enabled = vm.accountValid && vm.verificationCode.isNotBlank()
                    Button(
                        onClick = {
                            //验证码登陆不需要校验验证码
//                            vm.checkVerifyCode(verifyCodeType)
                            vm.loginSms()
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = enabled,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(id = R.string.login),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}