package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.repo.db.AgsDB.syncNoFlyZone
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.utils.Validator
import com.jiagu.api.helper.CountryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

val LocalAccountModel =
    compositionLocalOf<AccountModel> { error("No AccountModel provided") }

class AccountModel(app: Application) : AndroidViewModel(app) {
    val type = mutableIntStateOf(0)
    val code = mutableIntStateOf(0)
    val text = mutableStateOf("")
    val password = mutableStateOf("")

    //验证码
    var verificationCode by mutableStateOf("")

    //姓名
    var name by mutableStateOf("")

    var accountValid by mutableStateOf(false)
    var passwordValid by mutableStateOf(false)

    init {
        val acc = UserConfig(app).account
        if (Validator.checkEmail(acc)) {
            type.intValue = 1
            text.value = acc
        } else {
            type.intValue = 0
            val strs = acc.split("-")
            if (strs.size > 1) {
                code.intValue = CountryHelper.COUNTRY_CODE.indexOf(strs[0])
                text.value = strs[1]
            } else {
                code.intValue = 0
                text.value = acc
            }
        }
    }

    val account: String
        get() {
            return if (type.intValue == 1) text.value
            else "${CountryHelper.COUNTRY_CODE[code.intValue]}-${text.value}"
        }

    fun checkAccount(): Boolean {
        return if (type.value == 0) {
            val len = CountryHelper.COUNTRY_PHONE_LEN[code.value]
            text.value.length == len && Validator.checkPhoneNumber(text.value)
        } else Validator.checkEmail(text.value)
    }

    val userLogged = MutableStateFlow("")
    fun loginPass() {
        viewModelScope.launch {
            AgsNet.login(account, password.value).networkFlow { userLogged.value = it }
                .collectLatest { user ->
                    UserConfig(getApplication()).let {
                        it.account = account
                        it.user = user
                        AgsUser.userInfo = user
                        AgsNet.setApiToken(user.jToken, user.refreshToken)
                        syncNoFlyZone()
                    }
                    userLogged.emit("OK")
                }
        }
    }

    /**
     * 验证码登陆
     */
    fun loginSms() {
        viewModelScope.launch {
            AgsNet.loginSms(account, verificationCode).networkFlow {
                userLogged.value = it
            }.collectLatest { user ->
                UserConfig(getApplication()).let {
                    it.account = account
                    it.user = user
                    AgsUser.userInfo = user
                    AgsNet.setApiToken(user.jToken, user.refreshToken)
                    syncNoFlyZone()
                }
                userLogged.emit("OK")
            }
        }
    }

    /**
     * 获取验证码
     */
    fun getVerifyCode(verifyCodeType: VerifyCodeInfo.VerifyCodeTypeEnum, awaitTime: Long) {
        viewModelScope.launch {
            AgsNet.getVerifyCode(account, verifyCodeType.codeType).networkFlow {
                userLogged.value = it
            }.collectLatest {
                awaitFlag = true
                startCountdown(awaitTime)
            }
        }
    }

    //获取验证码按钮禁用
    var awaitFlag by mutableStateOf(false)

    // 状态，记录冷却时间
    var awaitTime = MutableStateFlow(0L)
    var remainingTime: StateFlow<Long> = awaitTime
    private fun startCountdown(a1: Long) {
        awaitTime.value = a1
        // 启动倒计时协程
        viewModelScope.launch {
            while (true) {
                awaitTime.value--
                if (awaitTime.value <= 0) {
                    awaitFlag = false // 倒计时结束后，重置冷却状态
                    break // 倒计时结束，退出循环
                }
                delay(1000) // 暂停一秒钟
            }
        }
    }

    /**
     * 注册
     */
    fun register() {
        viewModelScope.launch {
            AgsNet.registerAccount(
                phone = if (type.intValue == 0) account else "",
                email = if (type.intValue == 1) account else "",
                password = password.value,
                verifyCode = verificationCode,
                managerName = name
            ).networkFlow {
                userLogged.value = it
            }.collectLatest { user ->
                UserConfig(getApplication()).let {
                    it.account = account
                    it.user = user
                    AgsUser.userInfo = user
                    AgsNet.setApiToken(user.jToken, user.refreshToken)
                    syncNoFlyZone()
                }
                userLogged.emit("OK")
            }
        }
    }

    /**
     * 忘记密码(重置密码)
     */
    fun resetPassword(verifyCodeType: VerifyCodeInfo.VerifyCodeTypeEnum) {
        viewModelScope.launch {
            AgsNet.resetPassword(account, password.value, verificationCode)
                .networkFlow {
                    userLogged.value = it
                }.collectLatest {
                    userLogged.emit("OK")
                }
        }
    }
}

/**
 * 登陆类型
 */
enum class LoginTypeEnum {
    PHONE_NUMBER,
    EMAIL,
}
