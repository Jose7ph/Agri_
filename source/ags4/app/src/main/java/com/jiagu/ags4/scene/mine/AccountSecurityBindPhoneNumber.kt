package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.BindInfo
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.container.MainContent

@Composable
fun AccountSecurityBindPhoneNumber() {
    val navController = LocalNavController.current
    val mineModel = LocalMineModel.current
    MainContent(
        title = stringResource(id = if (AgsUser.userInfo?.userPhone?.isEmpty() != false) R.string.bind_phone_number else R.string.change_phone_number),
        spaceBy = 20.dp,
        breakAction = { navController.popBackStack() }) {

        BindPhoneNumber(
            modifier = Modifier
                .height(accountSecurityBindGlobalRowHeight)
                .padding(horizontal = 30.dp),
            width = accountSecurityBindGlobalTitleWidth,
            vm = mineModel
        )
        BindCommonVerificationCode(
            width = accountSecurityBindGlobalTitleWidth,
            modifier = Modifier
                .height(accountSecurityBindGlobalRowHeight)
                .padding(horizontal = 30.dp),
            verifyCodeType = VerifyCodeInfo.VerifyCodeTypeEnum.BIND_PHONE_EMAIL,
            vm = mineModel,
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .padding(top = 80.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    mineModel.updatePhoneEmail(
                        mineModel.account, BindInfo.TYPE_PHONE, AgsUser.userInfo?.userPhone?.isEmpty() ?: true
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = mineModel.accountValid && mineModel.verificationCode.isNotBlank(),
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
