package com.jiagu.ags4.scene.mine

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.AccountModel
import com.jiagu.ags4.vm.CacheModel
import com.jiagu.ags4.vm.LocalAccountModel
import com.jiagu.ags4.vm.LocalSortieManagementVM
import com.jiagu.ags4.vm.SortieManagementVM
import com.jiagu.ags4.vm.UserSortieCountDetail
import com.jiagu.api.ext.toastLong
import kotlinx.coroutines.launch

class MineActivity : BaseComponentActivity() {

    private val mineModel: MineModel by viewModels()
    private val mineDeviceModel: MineDeviceModel by viewModels()
    private val teamManagementModel: TeamManagementModel by viewModels()
    private val noFlyZoneModel: NoFlyZoneModel by viewModels()
    private val accountModel: AccountModel by viewModels()
    private val sortieManagementVM: SortieManagementVM by viewModels()

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            CompositionLocalProvider(
                LocalSortieManagementVM provides sortieManagementVM,
                LocalMineModel provides mineModel,
                LocalMineDeviceModel provides mineDeviceModel,
                LocalTeamManagementModel provides teamManagementModel,
                LocalAccountModel provides accountModel,
                LocalNoFlyZoneModel provides noFlyZoneModel,
            ) {
                NavHost(navController = navController, startDestination = "mine_homepage") {
                    //首页
                    composable("mine_homepage") {
                        MineHomepage() {
                            finish()
                        }
                    }
                    //地块管理
                    composable("mine_land_block_management") {
                        LandBlockManagement()
                    }
                    //我的设备
                    composable("mine_device") {
                        MineDevice()
                    }
                    //设备详情
                    composable("mine_device_detail") {
                        MineDeviceDetail()
                    }
                    //我的团队
                    composable("mine_team_management") {
                        TeamManagement()
                    }
                    //绑定/修改手机号
                    composable("mine_account_bind_phone_number") {
                        AccountSecurityBindPhoneNumber()
                    }
                    //绑定/修改邮箱
                    composable("mine_account_bind_email") {
                        AccountSecurityBindEmail()
                    }
                    //修改密码
                    composable("mine_change_password") {
                        AccountSecurityChangePassword()
                    }
                    //忘记密码/重置密码
                    composable("forgot_password") {
                        ForgotPassword()
                    }

                    composable("mine_sortie_management") {
                        SortieManagement()
                    }
                    composable("no_fly_zone") {
                        NoFlyZone()
                    }
                    //通用设置
                    composable("mine_general_settings") {
                        GeneralSettings()
                    }
                    //我的团队-团队信息
                    composable("team_info") {
                        TeamInfo()
                    }
                    //我的团队-团队信息-团队作业报表
                    composable("team_group_work_report") {
                        TeamGroupWorkReport()
                    }
                    //我的团队-团队信息-个人作业报表
                    composable("team_person_work_report") {
                        TeamPersonWorkReport()
                    }
                    //我的团队-团队信息-团队成员
                    composable("team_member_list") {
                        TeamMemberList()
                    }
                    //我的团队-团队信息-成员信息
                    composable("team_member_info") {
                        TeamMemberInfo()
                    }
                    //我的团队-团队信息-转让队长
                    composable("team_transfer_leader") {
                        TeamTransferLeader()
                    }
                    //我的团队-团队信息-团队成员-添加团队成员(菜单)
                    composable("team_add_member_menu") {
                        TeamAddMemberMenu()
                    }
                    //我的团队-团队信息-团队成员-添加团队成员(员工列表)
                    composable("team_add_member_staff_list") {
                        TeamAddMemberStaffList()
                    }
                    //我的团队-团队信息-团队成员-添加团队成员(邮箱/手机号)
                    composable("team_add_member_number") {
                        TeamAddMemberNumber()
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectFlow(mineModel.mineLogged) {
            if (it == "OK") finish()
            else if (it.isNotBlank()) toastLong(it)
            mineModel.mineLogged.value = ""
        }
        collectFlow(mineDeviceModel.mineDeviceLogged) {
            if (it == "OK") finish()
            else if (it.isNotBlank()) toastLong(it)
            mineDeviceModel.mineDeviceLogged.value = ""
        }
        lifecycleScope.launch {
            mineModel.getUserStatistic()
            CacheModel.loadCountry { mineModel.mineLogged.value = it }
        }
    }

    fun cleanUserInfo() {
        val user = UserConfig(this)
        user.user = null
        AgsUser.clearUser()
        AgsNet.setApiToken("", "")
        finish()
    }

    fun toDetail(detail: UserSortieCountDetail) {
        toDetail(detail.sortieId, detail.droneId, detail.startTime)
    }

    fun toDetail(sortieId: Long, droneId: String, startTime: Long) {
        startActivity(
            SortieDetailActivity::class.java,
            SortieDetailActivity.EXTRA_SORTIE_ID to sortieId,
            SortieDetailActivity.EXTRA_DRONE_ID to droneId,
            SortieDetailActivity.EXTRA_SORTIE_TIME to startTime
        )
    }

    fun toDetailList(sortieIds: List<Long>) {
        startActivity(
            SortieDetailListActivity::class.java,
            SortieDetailListActivity.EXTRA_SORTIE_IDS to sortieIds.toLongArray(),
        )
    }
}