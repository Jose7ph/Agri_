package com.jiagu.ags4.scene.mine

import android.app.Application
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.FlyHistoryLocus
import com.jiagu.ags4.bean.UserInfo
import com.jiagu.ags4.bean.UserStatistic
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.BindInfo
import com.jiagu.ags4.repo.net.model.BlockItem
import com.jiagu.ags4.repo.net.model.VerifyCodeInfo
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.vm.FlyHistoryDetail
import com.jiagu.api.ext.toMillis
import com.jiagu.api.helper.CountryHelper
import com.jiagu.api.model.MapBlock
import com.jiagu.jgcompose.paging.Paging
import com.jiagu.jgcompose.picker.Address
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

val LocalMineModel = compositionLocalOf<MineModel> {
    error("No MineModel provided")
}

class MineModel(app: Application) : AndroidViewModel(app) {
    val type = mutableIntStateOf(0)
    val code = mutableIntStateOf(0)
    val text = mutableStateOf("")

    //验证码
    var verificationCode by mutableStateOf("")

    var startTime by mutableStateOf<String?>(null)
    var endTime by mutableStateOf<String?>(null)
    var region by mutableStateOf<Address?>(null)
    var search by mutableStateOf<String?>(null)

    var landBlock = MutableLiveData<Block>(null)

    var userStatistic: UserStatistic? by mutableStateOf(null)

    var landBlockPageList by mutableStateOf<Flow<PagingData<BlockItem>>>(emptyFlow())
    val track = MutableLiveData<FlyHistoryLocus?>(null)
    val block = MutableLiveData<MapBlock>()
    var blockType = Block.TYPE_BLOCK
    val info = MutableLiveData<FlyHistoryDetail>()

    var flowSpeedValue = mutableStateOf(0.0f)
    var heightValue = mutableStateOf(0.0f)
    var speedValue = mutableStateOf(0.0f)

    fun refresh() {
        val landBlockPage =
            LandBlockPage(startTime?.toMillis(), endTime?.toMillis(), region?.code, search)
        landBlockPageList = landBlockPage.load()
    }

    class LandBlockPage(
        private val startTime: Long? = null,
        private val endTime: Long? = null,
        val region: Int? = null,
        val search: String? = null
    ) : Paging<BlockItem>(pageSize = 20, api = { params ->
        AgsNet.getBlocksList(
            pageIndex = params.key ?: 1,
            size = params.loadSize,
            startTime = startTime,
            endTime = endTime,
            region = region,
            search = search
        )
    })

    //绑定 or 修改 手机号 判断
    var bindPhoneNumber by mutableStateOf(true)

    //绑定 or 修改 邮箱 判断
    var bindEmail by mutableStateOf(false)

    var oldPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")

    var oldPasswordValid by mutableStateOf(false)
    var newPasswordValid by mutableStateOf(false)

    var accountValid by mutableStateOf(false)

    val mineLogged = MutableStateFlow("")

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

    /**
     * 修改密码
     */
    fun changePassword() {
        viewModelScope.launch {
            AgsNet.updateUserPassword(oldPassword, newPassword).networkFlow {
                mineLogged.value = it
            }.collectLatest {
                clearUserInfo()
                mineLogged.value = "OK"
            }
        }
    }

    /**
     * 修改/绑定 手机号or邮箱
     */
    fun updatePhoneEmail(account: String, type: Int, isBind: Boolean) {
        viewModelScope.launch {
            AgsNet.updatePhoneEmail(account, type, verificationCode).networkFlow {
                mineLogged.value = it
            }.collectLatest {
                //绑定不需要 修改需要重新登陆
                //手机号 && 绑定
                if (type == BindInfo.TYPE_PHONE && isBind) {
                    updateUserInfo(it)
                } else {
                    //修改邮箱
                    clearUserInfo()
                }
                //邮箱 && 绑定
                if (type == BindInfo.TYPE_EMAIL && isBind) {
                    updateUserInfo(it)
                } else {
                    //修改邮箱
                    clearUserInfo()
                }

                mineLogged.value = "OK"
            }
        }
    }

    /**
     * 更新用户信息
     */
    private fun updateUserInfo(user: UserInfo) {
        UserConfig(getApplication()).let {
            it.user = user
//            it.account = account
            AgsUser.userInfo = user
            AgsNet.setApiToken(user.jToken, user.refreshToken)
        }
    }

    //清除登陆信息
    private fun clearUserInfo() {
        UserConfig(getApplication()).let {
            it.user = null
            AgsUser.clearUser()
            AgsNet.setApiToken("", "")
        }
    }

    /**
     * 获取验证码
     */
    fun getVerifyCode(verifyCodeType: VerifyCodeInfo.VerifyCodeTypeEnum, awaitTime: Long) {
        viewModelScope.launch {
            AgsNet.getVerifyCode(account, verifyCodeType.codeType).networkFlow {
                mineLogged.value = it
            }.collectLatest {
                awaitFlag = true
                startCountdown(awaitTime)
            }
        }
    }


    /**
     * 获取用户作业信息
     */
    suspend fun getUserStatistic() {
        val userId = AgsUser.userInfo?.userId
        viewModelScope.launch {
            AgsNet.getUserStatistic(userId = userId).networkFlow {
                mineLogged.value = it
            }.collectLatest {
                userStatistic = it
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

    fun landBlockVmDataClean() {
        startTime = null
        endTime = null
    }

    fun getLandBlockDetail(blockId: Long) {
        viewModelScope.launch {
            AgsNet.getBlockDetail(blockId).networkFlow {
                Log.v("lee", "error: $it")
                mineLogged.value = it
            }.collectLatest {
                Log.v("lee", "getLandBlockDetail: $it")
                landBlock.value = it
            }
        }
    }

    fun getSortieDetail(droneId: String, sortieId: Long, startTime: Long) {
        viewModelScope.launch {
            AgsNet.getSortieDetail(droneId, sortieId, startTime).networkFlow {
                mineLogged.value = it
            }.collectLatest {
                info.value = it
                if (it.blockId != 0L) {
                    AgsNet.getBlockDetail(it.blockId).networkFlow {
                        Log.v("lee", "getBlockDetail: $it")
                    }.collectLatest {
                        block.value = it.boundary
                        blockType = it.blockType
                    }
                }

                AgsNet.getSortieTrack(droneId, sortieId, startTime).networkFlow { err ->
                    Log.v("lee", "getSortieTrack err: $err")
                }.collectLatest { it2 ->
                    Log.v("lee", "getSortieTrack: $track")
                    it2.sortie.let { sortieTrack ->
                        track.value = sortieTrack
                    }
                }
            }

        }
    }

    fun sync() {
        viewModelScope.launch {
            AgsDB.sync()
        }
    }
}

enum class MineMenuType(val title: Int, val image: Int) {
    SORTIE_MANAGEMENT(
        title = R.string.mine_sortie_management,
        image = R.drawable.default_mine_sortie_management
    ),//架次管理
    LAND_MANAGEMENT(
        title = R.string.mine_land_management,
        image = R.drawable.default_mine_land_management
    ),//地块管理
    MINE_DEVICE(title = R.string.mine_device, image = R.drawable.default_mine_device),//我的设备
    TEAM_MANAGEMENT(
        title = R.string.team_management,
        image = R.drawable.default_mine_team_management
    ),//团队管理
    LOG_MANAGEMENT(
        title = R.string.mine_log_management,
        image = R.drawable.default_mine_log_management
    ),//日志管理
    GENERAL_SETTINGS(
        title = R.string.mine_general_settings,
        image = R.drawable.default_mine_general_settings
    ),//通用设置
    NO_FLY_ZONE(title = R.string.no_fly_zone, image = R.drawable.default_mine_no_fly_zone),//禁飞区
    ACCOUNT_SECURITY(
        title = R.string.mine_account_security,
        image = R.drawable.default_mine_account_safe
    ),//账户与安全
}