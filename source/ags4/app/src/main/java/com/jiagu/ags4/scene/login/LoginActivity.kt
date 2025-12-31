package com.jiagu.ags4.scene.login

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.vm.AccountModel
import com.jiagu.ags4.vm.LocalAccountModel
import com.jiagu.api.ext.toastLong

class LoginActivity : BaseComponentActivity() {

    private val accountModel: AccountModel by viewModels()

    @Composable
    override fun Content() {
        CompositionLocalProvider(
            LocalAccountModel provides accountModel
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                NavHost(navController = navController, startDestination = "login_pass") {
                    composable("login_pass") { LoginPass() }
                    composable("login_verify_code") { LoginVerifyCode() }
                    composable("login_register") { LoginRegister() }
                    composable("reset_password") { ResetPassword() }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(accountModel.userLogged) {
            if (it == "OK") finish()
            else if (it.isNotBlank()) toastLong(it)
            accountModel.userLogged.value = ""
        }
    }
}
