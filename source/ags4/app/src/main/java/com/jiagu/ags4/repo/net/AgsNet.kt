package com.jiagu.ags4.repo.net

import android.app.Application
import android.util.Log
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.AllFirm
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.DroneState
import com.jiagu.ags4.bean.FlyHistoryLocusWarper
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.Region
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieItem
import com.jiagu.ags4.bean.TrackBrief
import com.jiagu.ags4.bean.TrackNode
import com.jiagu.ags4.bean.UserInfo
import com.jiagu.ags4.bean.UserStatistic
import com.jiagu.ags4.repo.net.model.AddGroupMemberInfo
import com.jiagu.ags4.repo.net.model.AppLog
import com.jiagu.ags4.repo.net.model.AppUIConfig
import com.jiagu.ags4.repo.net.model.BindInfo
import com.jiagu.ags4.repo.net.model.BlockItem
import com.jiagu.ags4.repo.net.model.ChangePassword
import com.jiagu.ags4.repo.net.model.CreateGroupInfo
import com.jiagu.ags4.repo.net.model.DeviceSortieInfo
import com.jiagu.ags4.repo.net.model.DeviceUseRecord
import com.jiagu.ags4.repo.net.model.DroneDetail
import com.jiagu.ags4.repo.net.model.DroneList
import com.jiagu.ags4.repo.net.model.DroneLockInfo
import com.jiagu.ags4.repo.net.model.DroneLog
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.ags4.repo.net.model.FlyHistoryStatic
import com.jiagu.ags4.repo.net.model.GroupDetail
import com.jiagu.ags4.repo.net.model.IdentityInfo
import com.jiagu.ags4.repo.net.model.IdentityVerify
import com.jiagu.ags4.repo.net.model.LoginInfo
import com.jiagu.ags4.repo.net.model.Member
import com.jiagu.ags4.repo.net.model.MemberReportDetail
import com.jiagu.ags4.repo.net.model.NoFlyZoneInfo
import com.jiagu.ags4.repo.net.model.PlanInfo
import com.jiagu.ags4.repo.net.model.RefreshInfo
import com.jiagu.ags4.repo.net.model.RegisterInfo
import com.jiagu.ags4.repo.net.model.ResetPasswordCode
import com.jiagu.ags4.repo.net.model.SelectOper
import com.jiagu.ags4.repo.net.model.SelectTeam
import com.jiagu.ags4.repo.net.model.SquareBlock
import com.jiagu.ags4.repo.net.model.Team
import com.jiagu.ags4.repo.net.model.TeamEmployee
import com.jiagu.ags4.repo.net.model.TeamWorkReport
import com.jiagu.ags4.repo.net.model.TransferLeaderInfo
import com.jiagu.ags4.repo.net.model.UpdateDroneNameInfo
import com.jiagu.ags4.repo.net.model.UpdateGroupNameInfo
import com.jiagu.ags4.repo.net.model.UpdateZzDroneNameInfo
import com.jiagu.ags4.repo.net.model.UserWorkStatic
import com.jiagu.ags4.repo.net.model.toBlockInfo
import com.jiagu.ags4.repo.net.model.toPlanInfo
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.repo.sp.UserConfig
import com.jiagu.ags4.vm.FlyHistoryDetail
import com.jiagu.ags4.vm.UserSortieCount
import com.jiagu.ags4.vm.UserSortieCountDetail
import com.jiagu.ags4.vm.UserSortieQueryParams
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.PackageHelper
import com.jiagu.tools.http.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.await
import java.io.File
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException

object AgsNet : RetrofitClient<ApiService>(ApiService::class.java) {

    private lateinit var app: Application
    fun initialize(application: Application) {
        app = application
        if (AgsUser.userInfo != null) setToken(
            AgsUser.userInfo!!.jToken, AgsUser.userInfo!!.refreshToken
        )
    }

    private var sender = ""
    private var _url = BuildConfig.SERVER_URL
    override val baseUrl: String
        get() = _url

    fun setBaseUrl(url: String) {
        _url = url
    }

    fun setSmsSender(smsSender: String) {
        sender = smsSender
    }

    override fun addHeader(req: Request.Builder) {
        req.addHeader("Terminal-Version", PackageHelper.getAppVersionCode(app).toString())
    }

    override fun refreshAccessToken(token: String): Boolean {
        val user = service.refreshToken(RefreshInfo(token)).execute().body() ?: return false
        val config = UserConfig(app)
        config.user = user
        setToken(user.jToken, user.refreshToken)
        AgsUser.userInfo = user
        if (user.refreshToken.isNotBlank()) {
            return true
        }
        return false
    }

    private fun formatNetworkException(e: NetworkException): String {
        return when (e.code) {
            1 -> app.getString(R.string.err_network)
            500 -> app.getString(R.string.err_server)
            503 -> app.getString(R.string.err_server)
            401 -> app.getString(R.string.relogin)
            else -> e.message!!
        }
    }

    fun <T> Flow<T>.networkFlow(block: (String) -> Unit): Flow<T> {
        return catch {
            if (it is NetworkException) {
                block(formatNetworkException(it))
            }
        }
    }

    private suspend fun mapExec(work: suspend () -> Unit) {
        try {
            work()
        } catch (e: Throwable) {
            Log.v("shero", "mapExec failed: $e")
            throw parseError(e)
        }
    }

    private suspend fun <T> map(
        checkNet: Boolean = true, work: suspend () -> T
    ): Pair<T?, String?> {
        if (checkNet) {
            if (!AgsUser.netIsConnect) return null to app.getString(R.string.err_network)
        }
        var result: T? = null
        var err: String? = null
        withContext(Dispatchers.IO) {
            try {
                result = work()
                LogFileHelper.log("net success")
            } catch (e: NetworkException) {
                err = formatNetworkException(e)
                LogFileHelper.log("net error: $err")
            } catch (e: Throwable) {
                e.printStackTrace()
                if (e !is CancellationException) {
                    LogFileHelper.log("net error: $e")
                    err = app.getString(R.string.err_network)
                }
            }
        }
        return result to err
    }

    private suspend fun mapExec0(work: suspend () -> Unit) {
        try {
            work()
        } catch (e: Throwable) {
            throw parseError(e)
        }
    }

    fun getVerifyCode(phone: String, codeType: Int): Flow<Unit> {
        return flow0(service.getVerifyCode(phone, codeType, sender))
    }

    fun checkVerifyCode(
        phoneNum: String, code: String, checkOpr: Int
    ): Flow<Unit> {
        return flow0(service.checkVerifyCode(phoneNum, code, checkOpr))
    }

    fun resetPassword(phone: String, passwd: String, verifyCode: String): Flow<Unit?> {
        return flow0(service.resetPassword(ResetPasswordCode(phone, passwd, verifyCode)))
    }

    fun login(account: String, passwd: String): Flow<UserInfo> {
        return flow0(service.login(LoginInfo(account, passwd)))
    }

    fun loginSms(account: String, code: String): Flow<UserInfo> {
        return flow0(service.loginSms(LoginInfo(account, null, code)))
    }

    fun setApiToken(access: String, refresh: String) {
        setToken(access, refresh)
    }

    // 注册
    fun registerAccount(
        phone: String, email: String, password: String, verifyCode: String, managerName: String
    ): Flow<UserInfo> {
        val info = RegisterInfo(
            phone, email, password, verifyCode, managerName
        )
        return flow0(service.register(info)).map {
            it.apply {
                setToken(jToken, refreshToken)
            }
        }
    }

    // 上传文件
    private fun prepareFilePart(
        partName: String, file: File, mediaType: String = "image/*"
    ): MultipartBody.Part {
        val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    fun uploadFile(path: String): Flow<String?> {
        return flow(service.uploadFile(path, prepareFilePart("file", File(path))))
    }

    fun updateUserPassword(oldPass: String, newPass: String): Flow<Unit> {
        return flow0(service.updateUserPassword(ChangePassword(oldPass, newPass)))
    }

    fun updatePhoneEmail(phoneEmail: String, type: Int, verifyCode: String): Flow<UserInfo> {
        return flow0(service.updatePhoneEmail(BindInfo(phoneEmail, type, verifyCode)))
    }

    fun deleteUser(userId: Long): Flow<String?> {
        return flow(service.deleteUser(userId))
    }

    fun getRegionList(): Flow<List<Region>> {
        return flow0(service.allRegions())
    }

    fun <T : Any, R : Any> map0(call: Call<T>, mapper: suspend (T) -> R): Flow<R> {
        return flow { emit(call.await()) }.map(mapper).flowOn(Dispatchers.IO)
            .catch { throw parseError(it) }
    }

    suspend fun <T : Any, R : Any> mapExec(call: Call<T>, mapper: suspend (T) -> R): R {
        try {
            return mapper(call.await())
        } catch (e: Throwable) {
            throw parseError(e)
        }
    }

    suspend fun nearbyBlockInfo(
        blockIds: Array<Long>, startTime: Long, endTime: Long
    ): List<BlockPlan> {
        return mapExec(service.nearbyBlocksInfo(SquareBlock(blockIds, startTime, endTime))) {
            it.map { bp -> bp.toBlockPlan() }
        }
    }

    suspend fun nearbyBlocks(
        n: Double, s: Double, e: Double, w: Double, from: Long, to: Long, blockType: String
    ): List<BlockPlan> {
        return mapExec(service.nearbyBlockLists(n, s, e, w, from, to, blockType)) {
            it.map { bp -> bp.toBlockPlan() }
        }
    }

    /**
     * 地块列表
     */
    fun getBlocksList(
        pageIndex: Int,
        size: Int,
        startTime: Long?,
        endTime: Long?,
        region: Int?,
        search: String? = null
    ): Flow<com.jiagu.jgcompose.paging.Page<BlockItem>> {
        return flow0(service.getBlocksList(pageIndex, size, startTime, endTime, region, search))
    }

    fun getBlockDetail(blockId: Long): Flow<Block> {
        return flow0(service.getBlockDetail(blockId)).map { it.toBlock() }
    }

    fun getUserStatistic(userId: Long? = null): Flow<UserStatistic> {
        return if (userId == null) {
            flow0(
                service.getUserStatistic()
            )
        } else {
            flow0(service.getUserStatistic(userId))
        }
    }
//    fun getLock(fcNumber: String): Flow<Device> {
//        return flow(service.getDeviceLock(fcNumber))
//    }

    fun getPlan(planId: Long): Flow<PlanInfo> {
        return flow0(service.getPlan(planId))
    }

    suspend fun uploadBlockSync(block: Block) {
        exec0(service.uploadBlocks(listOf(block.toBlockInfo()))).let {
            block.blockId = it[0]
        }
    }

    suspend fun updateBlocksSync(block: Block) {
        exec_(service.updateBlocks(block.toBlockInfo()))
    }

    suspend fun uploadPlanSync(plan: Plan) {
        exec0(service.uploadPlan(plan.toPlanInfo())).let {
            plan.planId = it.planId
            plan.updateTime = it.updateTime
        }
    }

    suspend fun updatePlanSync(plan: Plan) {
        exec0(service.updatePlan(plan.toPlanInfo())).let {
            plan.planId = it.planId
            plan.updateTime = it.updateTime
        }
    }

    suspend fun uploadSortieSimulate(sortie: Sortie) {
        mapExec0 { service.uploadSimulateSortie(sortie.toSortieInfo()) }
    }

    suspend fun uploadSortieSync(plan: Plan?, sortie: Sortie) {
        mapExec0 { service.uploadSortie(sortie.toSortieInfo()) }
    }

    fun userSortieCount(info: UserSortieQueryParams): Flow<UserSortieCount> {
        return flow0(service.userSortieCount(info))
    }

    fun getDateUserSortieCountPage(info: UserSortieQueryParams): Flow<com.jiagu.jgcompose.paging.Page<UserSortieCount>> {
        return flow0(service.userSortieCountPage(info))
    }

    fun getDateUserSortieByDay(info: UserSortieQueryParams): Flow<List<UserSortieCountDetail>> {
        return flow0(service.userSortieInfoByDay(info))
    }

    //架次详情
    fun getSortieDetail(droneId: String, sortieId: Long, start: Long): Flow<FlyHistoryDetail> {
        return flow0(service.getSortieDetail(droneId, sortieId, start))
    }

    suspend fun updateDroneState(
        droneCode: String, sortie: Int, totalArea: Float, live: Boolean, state: TrackNode
    ) {
        mapExec {
            val ds = DroneState(
                droneCode, sortie.toLong(), totalArea, if (live) 1 else 0, state.toString()
            )
            val err = service.updateState(ds)
//            Log.e("shero", "fail to update state: $err")
        }
    }

    fun getSortieTrack(droneId: String, sortieId: Long, start: Long): Flow<FlyHistoryLocusWarper> {
        return flow0(service.getSortieTrack(droneId, sortieId, start))
    }

    fun getDroneParams(type: Int): Flow<List<DroneParam>> {
        return flow0(service.getDroneParams(type))
    }

    fun uploadDroneParam(droneParam: DroneParam): Flow<Long> {
        return flow0(service.uploadDroneParam(droneParam))
    }

    fun getDroneParam(id: Long): Flow<DroneParam> {
        return flow0(service.getDroneParam(id))
    }

    fun deleteDroneParam(id: Long): Flow<Unit> {
        return flow0(service.deleteDroneParam(id))
    }

    fun updateDroneParam(droneParam: DroneParam): Flow<Unit> {
        return flow0(service.updateDroneParam(droneParam))
    }

    suspend fun <T> process(work: suspend () -> T, local: suspend () -> T): T? {
        var out: T? = null
        withContext(Dispatchers.IO) {
            try {
                out = if (!AgsUser.netIsConnect) local() else work()
            } catch (e: Exception) {
                e.printStackTrace()
                if (e !is CancellationException) {
                    out = local()
                }
            }
        }
        return out
    }

    suspend fun process2(work: suspend () -> Unit, local: suspend () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (!AgsUser.netIsConnect) local()
                else work()
            } catch (e: Exception) {
                e.printStackTrace()
                if (e !is CancellationException) {
                    local()
                }
            }
        }
    }

    suspend fun uploadAppLog(file: File, log: AppLog) {
        mapExec {
            val result =
                service.uploadAppLog(prepareFilePart("file", file, "application/octet-stream"))
                    .await()
            log.fileUrl = result.url
            service.uploadAppLog(log).await()
        }
    }

    suspend fun uploadLog(droneId: String, timestamp: Long, file: File) {
        mapExec {
            val result =
                service.uploadLogFile(prepareFilePart("file", file, "application/octet-stream"))
                    .await()
            service.uploadLog(DroneLog(timestamp, result.url, droneId)).await()
        }
    }

    suspend fun getAllFirm(file: String): Pair<AllFirm?, String?> {
        return map(false) { service.getAllFirm(file) }
    }

    suspend fun upgrade(file: String): Pair<Map<String, Any?>?, String?> {
        return map {
            val out = mutableMapOf<String, Any>()
            val ver = service.upgradeAppOrFirm(file)
            for ((k, v) in ver) {
                out[k] = v
            }
            val lang = Locale.getDefault().language
            val suffix = when (lang) {
                "zh" -> ""
                else -> "-${lang}"
            }
            val key = "content$suffix"
            val content =
                if (out.containsKey(key) && out[key] != null) out[key] else out["content-en"]
            mapOf(
                "version" to out["version"], "url" to out["url"], "content" to content
            )
        }
    }

    fun getAppUIConfig(accountId: Long): Flow<AppUIConfig> {
        return flow0(service.getAppUIConfig(accountId))
    }

    fun getDroneList(page: Int, size: Int, droneId: String?): Flow<DroneList> {
        return flow0(service.getDroneList(page, size, droneId))
    }

    fun getDroneDetail(droneId: String): Flow<DroneDetail> {
        return flow0(service.getDroneDetail(droneId))
    }

    suspend fun getLock(droneId: String): DroneDetail {
        val detail = exec0(service.getDroneDetail(droneId))
        detail.staticInfo.rackNo?.let {
            if (it.isNotBlank()) {
                val rackNoMap = HashMap(DeviceConfig(app).rackNoMap ?: emptyMap())
                rackNoMap[droneId] = it
                DeviceConfig(app).rackNoMap = rackNoMap
            }
        }
        return detail
    }

    fun getFlyDetailHistory(
        droneId: String,
        pageIndex: Int,
        size: Int,
        groupId: Long? = null,
        userId: Long? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        region: Int? = null
    ): Flow<com.jiagu.jgcompose.paging.Page<SortieItem>> {
        return flow0(
            service.getFlyDetailHistory(
                droneId, pageIndex, size, groupId, userId, startTime, endTime, region
            )
        )
    }

    fun getFlyDetailHistoryStatist(
        droneId: String,
        groupId: Long? = null,
        userId: Long? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        region: Int? = null
    ): Flow<FlyHistoryStatic> {
        return flow0(
            service.getFlyDetailHistoryStatic(
                droneId, groupId, userId, startTime, endTime, region
            )
        )
    }

    fun getSelectTeams(droneId: String): Flow<List<SelectTeam>> {
        return flow0(service.getSelectTeams(droneId))
    }

    fun getSelectPersons(droneId: String): Flow<List<SelectOper>> {
        return flow0(service.getSelectPersons(droneId))
    }

    // 设备名称修改
    fun updateDroneName(droneId: String, droneName: String): Flow<Unit> {
        val info = UpdateDroneNameInfo(droneId, droneName)
        return flow0(service.updateDroneName(info))
    }

    // 制造商ID修改
    fun updateZzDroneName(droneId: String, droneName: String): Flow<Unit> {
        val info = UpdateZzDroneNameInfo(droneName)
        return flow0(service.updateZzDroneName(droneId, info))
    }

    /**
     * 锁定/解锁无人机
     */
    fun lockOpr(droneId: String, opr: Int): Flow<Unit> {
        val info = DroneLockInfo(droneId, opr)
        return flow0(service.lockOpr(info))
    }

    fun getSortieInfo(ids: List<Long>): Flow<List<DeviceSortieInfo>> {
        return flow0(service.getSortieInfo(ids.joinToString(",")))
    }

    // 架次轨迹
    fun getTrackBrief(ids: List<Long>): Flow<List<TrackBrief>> {
        return flow0(service.getTrackBrief(ids.joinToString(",")))
    }

    /**
     * 团队管理-创建团队
     */
    fun createGroup(groupName: String): Flow<Unit> {
        val info = CreateGroupInfo(groupName)
        return flow0(service.createGroup(info))
    }

    /**
     * 团队管理-团队一览
     */
    fun getGroups(pageIndex: Int, size: Int): Flow<com.jiagu.jgcompose.paging.Page<Team>> {
        return flow0(service.getGroups(pageIndex, size))
    }

    fun saveDeviceUseRecord(deviceId: String, userId: Long?, appVersion: String): Flow<Unit> {
        return flow0(service.saveDeviceUseRecord(DeviceUseRecord(deviceId, userId, appVersion)))
    }

    fun getNoflyList(
        currentPage: Int,
        pageSize: Int,
        noflyType: Int,
        detailAddress: String,
    ): Flow<com.jiagu.jgcompose.paging.Page<NoFlyZoneInfo>> {
        return flow0(service.getNoflyList(currentPage, pageSize, noflyType, detailAddress))
    }

    fun identityVerify(idcardNum: String, contactName: String): Flow<Boolean> {
        return flow0(service.identityVerify(IdentityInfo(idcardNum, contactName)))
    }

    fun checkVerify(): Flow<IdentityVerify> {
        return flow0(service.checkVerify())
    }

    fun groupDetail(groupId: Long): Flow<GroupDetail> {
        return flow0(service.groupDetail(groupId))
    }

    fun groupUserList(
        groupId: Long,
        pageIndex: Int,
        size: Int
    ): Flow<com.jiagu.jgcompose.paging.Page<Member>> {
        return flow0(service.groupUserList(groupId, pageIndex, size))
    }

    fun deleteGroup(
        groupId: Long,
    ): Flow<Unit> {
        return flow0(service.deleteGroup(groupId))
    }

    fun leaveGroup(
        groupId: Long,
    ): Flow<Unit> {
        return flow0(service.leaveGroup(groupId))
    }

    fun updateGroupName(
        groupName: String, groupId: Long
    ): Flow<Unit> {
        return flow0(service.updateGroupName(UpdateGroupNameInfo(groupName, groupId)))
    }

    fun addGroupMembers(
        groupId: Long, userIds: String?, phones: String?
    ): Flow<Unit> {
        return flow0(service.addGroupMembers(AddGroupMemberInfo(groupId, userIds, phones)))
    }

    fun addableUserList(
        groupId: Long,
        pageIndex: Int,
        size: Int
    ): Flow<com.jiagu.jgcompose.paging.Page<TeamEmployee>> {
        return flow0(service.addableUserList(groupId, pageIndex, size))
    }

    fun deleteGroupMembers(
        groupId: Long, userId: Long
    ): Flow<Unit> {
        return flow0(service.deleteGroupMembers(groupId, userId))
    }

    fun transferLeader(
        groupId: Long, userId: Long
    ): Flow<Unit> {
        return flow0(service.transferLeader(TransferLeaderInfo(groupId, userId)))
    }

    fun getUserWorkStatic(
        groupId: Long,
        userId: Long,
        startTime: Long,
        endTime: Long
    ): Flow<UserWorkStatic> {
        return flow0(service.getUserWorkStatic(groupId, userId, startTime, endTime))
    }

    fun getUserWorkReport(
        groupId: Long,
        userId: Long,
        pageIndex: Int,
        size: Int,
        startTime: Long,
        endTime: Long
    ): Flow<com.jiagu.jgcompose.paging.Page<MemberReportDetail>> {
        return flow0(
            service.getUserWorkReport(
                groupId,
                userId,
                pageIndex,
                size,
                startTime,
                endTime
            )
        )
    }

    fun getTeamWorkStatic(
        groupId: Long,
        startTime: Long?,
        endTime: Long?,
        userIds: Array<Long>? = null
    ): Flow<FlyHistoryStatic> {
        return flow0(service.getTeamWorkStatic(groupId, startTime, endTime, userIds))
    }

    fun getTeamWorkReport(
        groupId: Long,
        pageIndex: Int,
        size: Int,
        startTime: Long? = null,
        endTime: Long? = null,
        userIds: Array<Long>? = null
    ): Flow<com.jiagu.jgcompose.paging.Page<TeamWorkReport>> {
        return flow0(
            service.getTeamWorkReport(groupId, pageIndex, size, startTime, endTime, userIds)
        )
    }

    /**
     * 激活设备
     */
    fun activeDrone(droneId: String): Flow<Unit>  {
        return flow0(service.activeDrone(droneId))
    }
}