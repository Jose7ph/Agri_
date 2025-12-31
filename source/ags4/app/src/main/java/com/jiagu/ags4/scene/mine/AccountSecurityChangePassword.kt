package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.text.AutoScrollingText


@Composable
fun AccountSecurityChangePassword() {
    val navController = LocalNavController.current
    val mineModel = LocalMineModel.current
    MainContent(
        title = stringResource(id = R.string.change_password),
        spaceBy = 20.dp,
        breakAction = { navController.popBackStack() }) {

        UpdatePassword(
            modifier = Modifier
                .height(accountSecurityBindGlobalRowHeight)
                .padding(horizontal = 30.dp),
            title = stringResource(id = R.string.old_password),
            width = accountSecurityBindGlobalTitleWidth,
            vm = mineModel,
            oldPassword = true
        )
        UpdatePassword(
            modifier = Modifier
                .height(accountSecurityBindGlobalRowHeight)
                .padding(horizontal = 30.dp),
            title = stringResource(id = R.string.new_password),
            width = accountSecurityBindGlobalTitleWidth,
            newPassword = true,
            vm = mineModel,
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                AutoScrollingText(
                    text = stringResource(id = R.string.forgot_password),
                    modifier = Modifier.clickable {
                        navController.navigate("forgot_password")
                    },
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 30.dp)
                .padding(top = 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    mineModel.changePassword()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = mineModel.oldPasswordValid && mineModel.newPasswordValid,
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