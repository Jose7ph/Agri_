package com.jiagu.ags4.scene.mine

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.scene.login.PasswordRow
import com.jiagu.ags4.scene.login.VerificationCodeRow
import com.jiagu.ags4.ui.components.LoginTypeCombo
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.LocalAccountModel
import com.jiagu.jgcompose.container.MainContent

//行高
val forgotPasswordRowHeight = 36.dp

//title宽度
val forgotPasswordTitleWidth = 120.dp

/**
 * 重置密码
 */
@Composable
fun ForgotPassword() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val mineActivity = (context as MineActivity)
    val accountModel = LocalAccountModel.current
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
                    .height(forgotPasswordRowHeight),
                vm = accountModel,
                height = forgotPasswordRowHeight,
                width = forgotPasswordTitleWidth,
            )
            //验证码
            VerificationCodeRow(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(forgotPasswordRowHeight),
                width = forgotPasswordTitleWidth,
                vm = accountModel,
                verifyCodeType = verifyCodeType
            )
            //密码
            PasswordRow(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(forgotPasswordRowHeight),
                vm = accountModel,
                width = forgotPasswordTitleWidth
            )
            val enabled =
                accountModel.accountValid && accountModel.passwordValid && accountModel.verificationCode.isNotBlank()
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        accountModel.resetPassword(verifyCodeType)
                        mineActivity.cleanUserInfo()
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