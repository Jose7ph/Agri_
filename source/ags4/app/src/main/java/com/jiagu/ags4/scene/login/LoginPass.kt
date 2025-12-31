package com.jiagu.ags4.scene.login

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.ags4.ui.components.LoginTypeCombo
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.goto
import com.jiagu.ags4.vm.LocalAccountModel

@Composable
fun LoginPass() {
    val navController = LocalNavController.current
    val activity = LocalContext.current as Activity
    val vm = LocalAccountModel.current
    MainContent(
        title = stringResource(id = R.string.login),
        breakAction = { if (!navController.popBackStack()) activity.finish() },
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
                            "login_register",
                            "login_pass"
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.register),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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
            PasswordRow(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .height(loginGlobalRowHeight),
                vm = vm,
                width = loginGlobalTitleWidth
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.reset_password),
                    modifier = Modifier.clickable { navController.navigate("reset_password") },
                    style = MaterialTheme.typography.bodyMedium
                )
                AutoScrollingText(
                    modifier = Modifier.clickable {
                        navController.goto(
                            "login_verify_code",
                            "login_pass"
                        )
                    },
                    text = stringResource(id = R.string.verification_code_login),
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
                Button(
                    onClick = {
                        vm.loginPass()
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = vm.accountValid && vm.passwordValid,
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
