package com.jiagu.ags4.bean

import androidx.annotation.Keep

// 用户信息
@Keep
data class UserInfo(
    val userId: Long,
    var userName: String = "",   // 姓名
    val adminPhone: String,   // 手机（帐号）
    var userHeadUrl: String = "",   // 头像

    val jToken: String,   // token
    val refreshToken: String,   // token
    val accountType: Int,   // token
    val accountId: Long,
    val userType: Int,
    val teamType: Int,
    var checkStatus: Int,   // 0=资料不完善  1=待审核  2=审核通过  3=审核不通过
    var personStatus: Int,   // 0=资料不完善  1=待审核  2=审核通过  3=审核不通过

    val userAuth: String?,   // 用户权限
    var concatPhone: String?,   // 联系手机号
    val email: String?,

    val enterpriseName: String?,       //公司名称
    val enterpriseAddress: String?,   //公司地址
    val creditCode: String?,          //社会信用号
    val userPhone: String?,          //用户手机号
) {
    companion object {
        // 身份认证
        const val INFO_MISSING = 0
        const val CHECKING = 1
        const val PASS = 2
        const val FAIL = 3

        const val ACCOUNT_TOP = 1
        const val ACCOUNT_GOVER = 2
        const val ACCOUNT_FACTORY = 3//制造商
        const val ACCOUNT_SERVICE = 4//运营商

        //1=登陆 2=注册 3=忘记密码
        const val CODE_LOGIN = 1
        const val CODE_REGISTER = 2
        const val CODE_PASSWORD = 3
    }

    fun isTop(): Boolean {
        return accountType == ACCOUNT_TOP
    }

    fun isService(): Boolean {
        return accountType == ACCOUNT_SERVICE
    }

    fun hasEployeePermission(): Boolean {
        return false
    }

    fun hasUserManagePermission(): Boolean {
        return false
    }

    fun hasTeamManagePermission(): Boolean {
        return accountType == ACCOUNT_SERVICE || accountType == ACCOUNT_FACTORY
    }

    fun canTuneAdvParam(): Boolean {
        return accountType == ACCOUNT_FACTORY || accountType == ACCOUNT_TOP
    }
    fun isMaker(): Boolean {
        return accountType == ACCOUNT_FACTORY
    }

}

@Keep
class UserStatistic(
    val allFlyTime: Float,   // 作业小时
    val allSprayMu: Float,   // 作业亩数
    val allFlyNum: Int    // 飞行架次
)
@Keep
data class AllFirm(
    val radar: String, val fradar: String, val bradar: String,
    val fmu: Long, val pmu: Long,
    val battery: String, val locator: String, val seed: String, val bs: String
)