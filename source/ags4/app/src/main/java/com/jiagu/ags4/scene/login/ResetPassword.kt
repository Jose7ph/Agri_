package com.jiagu.ags4.scene.login

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
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.LocalAccountModel

/**
 * 重置密码
 */
@Composable
fun ResetPassword() {
    val navController = LocalNavController.current
    val vm = LocalAccountModel.current
    val verifyCodeType = VerifyCodeInfo.VerifyCodeTypeEnum.RESET_PASSWORD
    MainContent(
        title = stringResource(id = R.string.reset_password),
        breakAction = { navController.popBackStack() }) {

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
                verifyCodeType = verifyCodeType,
                imeAction = ImeAction.Next
            )
            //密码
            PasswordRow(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(loginGlobalRowHeight),
                vm = vm,
                width = loginGlobalTitleWidth
            )
            val enabled = vm.accountValid && vm.verificationCode.isNotBlank() && vm.passwordValid
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        vm.resetPassword(verifyCodeType)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = enabled,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

    }
}