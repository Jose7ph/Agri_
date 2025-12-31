package com.jiagu.ags4.scene.mine

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.paging.PagingData
import com.jiagu.ags4.bean.UserStatistic
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.FlyHistoryStatic
import com.jiagu.ags4.repo.net.model.GroupDetail
import com.jiagu.ags4.repo.net.model.Member
import com.jiagu.ags4.repo.net.model.MemberReportDetail
import com.jiagu.ags4.repo.net.model.Team
import com.jiagu.ags4.repo.net.model.TeamEmployee
import com.jiagu.ags4.repo.net.model.TeamWorkReport
import com.jiagu.ags4.repo.net.model.UserWorkStatic
import com.jiagu.ags4.utils.exeTask
import com.jiagu.api.ext.toast
import com.jiagu.jgcompose.paging.Paging
import com.jiagu.jgcompose.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import java.util.Date


val LocalTeamManagementModel = compositionLocalOf<TeamManagementModel> {
    error("No TeamManagementModel provided")
}

class TeamManagementModel(app: Application) : AndroidViewModel(app) {
    val context: Application by lazy { getApplication() }
    var groupInfo by mutableStateOf<GroupDetail?>(null)
    private var curGroupId: Long = 0
    var curLeaderId: Long = 0
    var curHasAuth: Boolean = false
    var curInGroup: Boolean = true

    var teamUserList = mutableStateListOf<Member>()

    //团队列表
    var teamPageList by mutableStateOf<Flow<PagingData<Team>>>(emptyFlow())
    fun refreshTeam() {
        val teamPage = TeamPage()
        teamPageList = teamPage.load()
    }

    class TeamPage : Paging<Team>(pageSize = 20, api = { params ->
        AgsNet.getGroups(
            pageIndex = params.key ?: 1,
            size = params.loadSize,
        )
    })

    //创建团队
    fun createTeam(name: String) {
        exeTask {
            AgsNet.createGroup(name).networkFlow {
                context.toast(it)
            }.collectLatest {
                refreshTeam()
            }
        }
    }

    //加载当前团队数据
    fun loadTeamInfos(groupId: Long = 0) {
        if (groupId != 0L) {
            clearTeamInfos()
            curGroupId = groupId
        }
        getTeamInfo()
        getTeamUserList()
    }

    private fun getTeamInfo() {
        exeTask {
            groupInfo = null
            AgsNet.groupDetail(curGroupId).networkFlow {
                context.toast(it)
            }.collectLatest {
                curLeaderId = it.leaderUserId
                curHasAuth = it.hasAuth
                curInGroup = it.inGroup
                groupInfo = it
            }
        }
    }

    private fun getTeamUserList() {
        exeTask {
            teamUserList.clear()
            AgsNet.groupUserList(curGroupId, 1, 1000).networkFlow {
                context.toast(it)
            }.collectLatest {
                teamUserList.addAll(it.list)
            }
        }
    }

    //解散团队
    fun deleteTeam(complete: (Boolean) -> Unit) {
        exeTask {
            AgsNet.deleteGroup(curGroupId).networkFlow {
                context.toast(it)
                complete(false)
            }.collectLatest {
                clearTeamInfos()
                complete(true)
            }
        }
    }

    //退出团队
    fun exitTeam(complete: (Boolean) -> Unit) {
        exeTask {
            AgsNet.leaveGroup(curGroupId).networkFlow {
                context.toast(it)
                complete(false)
            }.collectLatest {
                clearTeamInfos()
                complete(true)
            }
        }
    }

    fun clearTeamInfos() {
        curGroupId = 0
        teamUserList.clear()
        groupInfo = null
        curLeaderId = 0
        curHasAuth = false
        curInGroup = true
    }

    //团队重命名
    fun updateTeamName(groupName: String, complete: (Boolean) -> Unit) {
        exeTask {
            AgsNet.updateGroupName(groupName, curGroupId).networkFlow {
                context.toast(it)
                complete(false)
            }.collectLatest {
                val newDetail = groupInfo
                newDetail?.groupName = groupName
                groupInfo = newDetail
                complete(true)
            }
        }
    }

    //团队当前成员列表
    var teamMemberList by mutableStateOf<Flow<PagingData<Member>>>(emptyFlow())
    fun refreshTeamMember() {
        val teamMemberPage = TeamMemberPage(curGroupId)
        teamMemberList = teamMemberPage.load()
    }

    class TeamMemberPage(
        groupId: Long
    ) : Paging<Member>(pageSize = 20, api = { params ->
        AgsNet.groupUserList(
            groupId = groupId,
            pageIndex = params.key ?: 1,
            size = params.loadSize,
        )
    })

    //0 - 员工列表 1 - 手机号 2 - 邮箱
    var addMemberType = AddMemberTypeEnum.STAFF_LIST

    //添加成员
    fun addTeamMembers(userIds: String? = null, phones: String? = null, complete: () -> Unit) {
        if (userIds.isNullOrEmpty() && phones.isNullOrEmpty()) return
        exeTask {
            AgsNet.addGroupMembers(
                groupId = curGroupId, userIds = userIds, phones = phones
            ).networkFlow {
                context.toast(it)
            }.collectLatest {
                complete()
            }

        }
    }

    //团队添加成员的列表
    var teamStaffList by mutableStateOf<Flow<PagingData<TeamEmployee>>>(emptyFlow())
    fun refreshTeamStaffList() {
        val teamStaffPage = TeamStaffPage(groupId = curGroupId)
        teamStaffList = teamStaffPage.load()
    }

    class TeamStaffPage(
        groupId: Long
    ) : Paging<TeamEmployee>(pageSize = 20, api = { params ->
        AgsNet.addableUserList(
            groupId = groupId,
            pageIndex = params.key ?: 1,
            size = params.loadSize,
        )
    })

    var curStaff: Member? = null
    var staffInfo: UserStatistic? = null
    fun clearStaffInfo() {
        curStaff = null
        staffInfo = null
    }

    //获取成员作业统计数据
    fun getStaffInfo(complete: () -> Unit) {
        if (curStaff == null) return
        exeTask {
            AgsNet.getUserStatistic(curStaff!!.userId).networkFlow {
                context.toast(it)
            }.collectLatest {
                staffInfo = it
                complete()
            }
        }
    }

    //删除团队成员
    fun deleteTeamMember(complete: () -> Unit) {
        if (curStaff == null) return
        exeTask {
            AgsNet.deleteGroupMembers(curGroupId, curStaff!!.userId).networkFlow {
                context.toast(it)
            }.collectLatest {
                complete()
                clearStaffInfo()
            }
        }
    }

    //转让队长
    fun transferLeader(userId: Long, complete: () -> Unit) {
        exeTask {
            AgsNet.transferLeader(groupId = curGroupId, userId = userId).networkFlow {
                context.toast(it)
            }.collectLatest {
                curLeaderId = userId
                complete()
            }
        }
    }

    //个人作业报表
    var workReportStartTime by mutableStateOf(
        DateUtils.getHalfYearAgoCalendar(Date())
    )
    var workReportEndTime by mutableStateOf(
        DateUtils.getDate()
    )

    fun loadPersonWorkReport(userId: Long) {
        personWorkStatic(
            userId = userId, startTime = workReportStartTime.time, endTime = workReportEndTime.time
        )
        refreshPersonWorkReportList(
            userId = userId, startTime = workReportStartTime.time, endTime = workReportEndTime.time
        )
    }

    //个人作业报表 - 统计数据
    var personWorkStatic by mutableStateOf<UserWorkStatic?>(null)
    private fun personWorkStatic(userId: Long, startTime: Long, endTime: Long) {
        exeTask {
            AgsNet.getUserWorkStatic(curGroupId, userId, startTime, endTime).networkFlow {
                context.toast(it)
            }.collectLatest {
                personWorkStatic = it
            }
        }
    }

    //个人作业报表 - 作业报表数据
    var personWorkReportList by mutableStateOf<Flow<PagingData<MemberReportDetail>>>(emptyFlow())
    private fun refreshPersonWorkReportList(
        userId: Long, startTime: Long, endTime: Long
    ) {
        val personWorkReportPage = PersonWorkReportPage(
            groupId = curGroupId, userId = userId, startTime = startTime, endTime = endTime
        )
        personWorkReportList = personWorkReportPage.load()
    }

    class PersonWorkReportPage(
        groupId: Long, userId: Long, startTime: Long, endTime: Long
    ) : Paging<MemberReportDetail>(pageSize = 20, api = { params ->
        AgsNet.getUserWorkReport(
            groupId = groupId,
            userId = userId,
            pageIndex = params.key ?: 1,
            size = params.loadSize,
            startTime = startTime,
            endTime = endTime
        )
    })

    //团队作业报表
    val selectedMembers = mutableStateListOf<Member>()
    fun loadTeamWorkReport() {
        val userIds = if (selectedMembers.isEmpty()) null else selectedMembers.map { it.userId }
            .toTypedArray()
        teamWorkStatic(
            startTime = workReportStartTime.time,
            endTime = workReportEndTime.time,
            userIds = userIds
        )
        refreshTeamWorkReportList(
            startTime = workReportStartTime.time,
            endTime = workReportEndTime.time,
            userIds = userIds
        )
    }

    //团队作业报表 - 统计数据
    var teamWorkStatic by mutableStateOf<FlyHistoryStatic?>(null)
    private fun teamWorkStatic(startTime: Long, endTime: Long, userIds: Array<Long>? = null) {
        exeTask {
            AgsNet.getTeamWorkStatic(curGroupId, startTime, endTime, userIds).networkFlow {
                context.toast(it)
            }.collectLatest {
                teamWorkStatic = it
            }
        }
    }

    //团队作业报表 - 作业报表数据
    var teamWorkReportList by mutableStateOf<Flow<PagingData<TeamWorkReport>>>(emptyFlow())
    private fun refreshTeamWorkReportList(
        startTime: Long, endTime: Long, userIds: Array<Long>? = null
    ) {
        val teamWorkReportPage = TeamWorkReportPage(
            groupId = curGroupId, startTime = startTime, endTime = endTime, userIds = userIds
        )
        teamWorkReportList = teamWorkReportPage.load()
    }

    class TeamWorkReportPage(
        groupId: Long, startTime: Long, endTime: Long, userIds: Array<Long>? = null
    ) : Paging<TeamWorkReport>(pageSize = 20, api = { params ->
        AgsNet.getTeamWorkReport(
            groupId = groupId,
            pageIndex = params.key ?: 1,
            size = params.loadSize,
            startTime = startTime,
            endTime = endTime,
            userIds = userIds
        )
    })
}

enum class AddMemberTypeEnum {
    STAFF_LIST, PHONE, EMAIL
}