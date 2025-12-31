package com.jiagu.ags4.repo.net

import com.jiagu.ags4.bean.AllFirm
import com.jiagu.ags4.bean.DroneState
import com.jiagu.ags4.bean.FlyHistoryLocusWarper
import com.jiagu.ags4.bean.Region
import com.jiagu.ags4.bean.SortieItem
import com.jiagu.ags4.bean.TrackBrief
import com.jiagu.ags4.bean.UserInfo
import com.jiagu.ags4.bean.UserStatistic
import com.jiagu.ags4.repo.net.model.AddGroupMemberInfo
import com.jiagu.ags4.repo.net.model.AppLog
import com.jiagu.ags4.repo.net.model.AppUIConfig
import com.jiagu.ags4.repo.net.model.BindInfo
import com.jiagu.ags4.repo.net.model.BlockInfo
import com.jiagu.ags4.repo.net.model.BlockItem
import com.jiagu.ags4.repo.net.model.BlockPlanBrief
import com.jiagu.ags4.repo.net.model.BlockPlanInfo
import com.jiagu.ags4.repo.net.model.ChangeHeader
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
import com.jiagu.ags4.repo.net.model.SortieInfo
import com.jiagu.ags4.repo.net.model.SquareBlock
import com.jiagu.ags4.repo.net.model.Team
import com.jiagu.ags4.repo.net.model.TeamEmployee
import com.jiagu.ags4.repo.net.model.TeamWorkReport
import com.jiagu.ags4.repo.net.model.TransferLeaderInfo
import com.jiagu.ags4.repo.net.model.UpdateDroneNameInfo
import com.jiagu.ags4.repo.net.model.UpdateGroupNameInfo
import com.jiagu.ags4.repo.net.model.UpdateZzDroneNameInfo
import com.jiagu.ags4.repo.net.model.UploadResult
import com.jiagu.ags4.repo.net.model.UserWorkStatic
import com.jiagu.ags4.vm.FlyHistoryDetail
import com.jiagu.ags4.vm.UserSortieCount
import com.jiagu.ags4.vm.UserSortieCountDetail
import com.jiagu.ags4.vm.UserSortieQueryParams
import com.jiagu.tools.http.RetrofitClient.Response
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // 刷新token
    @POST("user/baseaccount/refreshToken")
    fun refreshToken(@Body info: RefreshInfo): Call<UserInfo>

    // 注册
    @POST("user/baseaccount/reg")
    fun register(@Body info: RegisterInfo): Call<UserInfo>

    //获取验证码
    @GET("user/baseaccount/getVerifyCode")
    fun getVerifyCode(
        @Query("phoneNum") phone: String,
        @Query("codeType") codeType: Int,
        @Query("sender") sender: String
    ): Call<Unit>

    /**
     * 检测账号验证码
     * @param phoneNum 手机号
     * @param code 验证码
     * @param checkOpr
     */
    @GET("user/baseaccount/checkVerifyCode")
    fun checkVerifyCode(
        @Query("phoneNum") phoneNum: String,
        @Query("code") code: String,
        @Query("checkOpr") checkOpr: Int
    ): Call<Unit>

    // 重置密码
    @PUT("user/baseaccount/resetPassword2")
    fun resetPassword(@Body info: ResetPasswordCode): Call<Unit>

    // 密码登陆
    @POST("user/baseaccount/loginPdApp")
    fun login(@Body info: LoginInfo): Call<UserInfo>

    //验证码登陆
    @POST("user/baseaccount/loginSmS")
    fun loginSms(@Body info: LoginInfo): Call<UserInfo>

    @DELETE("user/user/{userId}")
    fun deleteUser(@Path("userId") userId: Long): Call<Response<String?>>

    //修改密码
    @PUT("user/user/manage/updateUserPassword")
    fun updateUserPassword(@Body info: ChangePassword): Call<Unit>

    //修改/绑定 手机号or密码
    @PUT("user/user/manage/phone/email")
    fun updatePhoneEmail(@Body info: BindInfo): Call<UserInfo>

    @PUT("user/user/{userId}/avatar")
    fun changeHeader(@Path("userId") userId: Long, @Body info: ChangeHeader)

    //上传文件
    @Multipart
    @POST("common/file/{objectKey}")
    fun uploadFile(
        @Path("objectKey") objectKey: String, @Part file: MultipartBody.Part
    ): Call<Response<String?>>


    @GET("oper/square-block-list/v3")
    fun nearbyBlockLists(
        @Query("maxLat") maxLat: Double,
        @Query("minLat") minLat: Double,
        @Query("maxLng") maxLng: Double,
        @Query("minLng") minLng: Double,
        @Query("startTime") from: Long,
        @Query("endTime") to: Long,
        @Query("blockType") blockType: String,
    ): Call<List<BlockPlanBrief>>

    @POST("oper/square-block-info/v2")
    fun nearbyBlocksInfo(@Body info: SquareBlock): Call<List<BlockPlanInfo>>

    @DELETE("work/block")
    fun deleteBlocks(@Query("blockIds") blockIds: String): Call<Void>

    @GET("oper/blocks/{blockId}")
    fun getBlockDetail(@Path("blockId") blockId: Long): Call<BlockInfo>

    @POST("oper/plans")
    fun uploadPlan(@Body plan: PlanInfo): Call<PlanInfo>//上传规划要返回规划ID,同步离线数据时使用

    @PUT("oper/plans")
    fun updatePlan(@Body plan: PlanInfo): Call<PlanInfo>

    @POST("oper/sorties/simulate")
    suspend fun uploadSimulateSortie(@Body sortie: SortieInfo)

    @POST("oper/sorties")
    suspend fun uploadSortie(@Body sortie: SortieInfo)

    @GET("oper/plans/{planId}")
    fun getPlan(@Path("id") planId: Long): Call<PlanInfo>

    @GET("oper/regions/allCountries")
    fun allRegions(): Call<List<Region>>

    //地块列表
    @GET("oper/blocks/v2")
    fun getBlocksList(
        @Query("pageIndex") pageIndex: Int,//页码下标
        @Query("size") size: Int,//每页显示数据量
        @Query("startTime") startTime: Long?,//开始时间
        @Query("endTime") endTime: Long?,//结束时间
        @Query("region") region: Int?,//区域编码
        @Query("search") search: String?//搜索框输入
    ): Call<com.jiagu.jgcompose.paging.Page<BlockItem>>

    //我的-作业数据
    @GET("drone/datastat/manage/getUserStatistic")
    fun getUserStatistic(): Call<UserStatistic>

    @GET("drone/datastat/manage/getUserStatistic")
    fun getUserStatistic(@Query("userId") userId: Long): Call<UserStatistic>

    // 添加地块
    @POST("oper/blocks")
    fun uploadBlocks(@Body info: List<BlockInfo>): Call<List<Long>>

    //修改地块
    @PUT("oper/blocks")
    fun updateBlocks(@Body info: BlockInfo): Call<Void?>

    @POST("oper/userSortie/count")
    fun userSortieCount(@Body body: UserSortieQueryParams): Call<UserSortieCount>

    @POST("oper/userSortie/countByDayPage")
    fun userSortieCountPage(@Body body: UserSortieQueryParams): Call<com.jiagu.jgcompose.paging.Page<UserSortieCount>>

    @POST("oper/userSortie/sortieInfoByDay")
    fun userSortieInfoByDay(@Body body: UserSortieQueryParams): Call<List<UserSortieCountDetail>>

    //架次详情
    @GET("oper/sorties/{droneId}/{sortieId}")
    fun getSortieDetail(
        @Path("droneId") droneId: String,
        @Path("sortieId") sortieId: Long,
        @Query("startTime") startTime: Long,
    ): Call<FlyHistoryDetail>

    @POST("oper/trackData")
    suspend fun updateState(@Body state: DroneState)

    //获取飞机参数
    @GET("drone/params/{type}")
    fun getDroneParams(@Path("type") type: Int): Call<List<DroneParam>>

    @POST("drone/param2")
    fun uploadDroneParam(@Body body: DroneParam): Call<Long>

    @GET("drone/param/{paramId}")
    fun getDroneParam(@Path("paramId") id: Long): Call<DroneParam>

    //架次轨迹详情
    //删除参数
    @DELETE("drone/param/{paramId}")
    fun deleteDroneParam(@Path("paramId") id: Long): Call<Unit>

    //修改参数
    @PUT("drone/param")
    fun updateDroneParam(@Body info: DroneParam): Call<Unit>

    /**
     * 架次轨迹详情
     */
    @GET("oper/sorties-tracks/{droneId}/{sortieId}")
    fun getSortieTrack(
        @Path("droneId") droneId: String,
        @Path("sortieId") sortieId: Long,
        @Query("startTime") startTime: Long,
    ): Call<FlyHistoryLocusWarper>

    @Multipart
    @POST("oper/upload/appLog")
    fun uploadAppLog(@Part file: MultipartBody.Part): Call<UploadResult>

    @POST("oper/appLog")
    fun uploadAppLog(@Body log: AppLog): Call<Void?>

    /**
     * 上传文件
     */
    @Multipart
    @POST("user/upload")
    fun uploadFile(@Part file: MultipartBody.Part): Call<UploadResult>

    @Multipart
    @POST("oper/upload/droneFiles")
    fun uploadLogFile(@Part file: MultipartBody.Part): Call<UploadResult>

    @POST("oper/droneFiles")
    fun uploadLog(@Body log: DroneLog): Call<Void?>

    @GET("http://cloud.jiagutech.com/upgrade/{file}")
    suspend fun getAllFirm(@Path("file") file: String): AllFirm

    @GET("http://cloud.jiagutech.com/upgrade/{file}")
    suspend fun upgradeAppOrFirm(@Path("file") file: String): Map<String, Any>

    @GET("user/account/manage/theme/{accountId}")
    fun getAppUIConfig(@Path("accountId") accountId: Long): Call<AppUIConfig>

    @GET("drone/device/manage/getDroneList")
    fun getDroneList(
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("droneId") droneId: String?,
    ): Call<DroneList>

    /**
     * 设备详情
     */
    @GET("drone/device/manage/getDroneDetail")
    fun getDroneDetail(@Query("droneId") droneId: String): Call<DroneDetail>

    // 飞机架次列表
    @GET("oper/drones-sorties/{droneId}")
    fun getFlyDetailHistory(
        @Path("droneId") droneId: String,
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int,
        @Query("groupId") groupId: Long?,
        @Query("userId") userId: Long?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("region") region: Int?
    ): Call<com.jiagu.jgcompose.paging.Page<SortieItem>>

    /**
     * 飞机架次统计
     */
    @GET("oper/drones-sorties/statistics/{droneId}")
    fun getFlyDetailHistoryStatic(
        @Path("droneId") droneId: String,
        @Query("groupId") groupId: Long?,
        @Query("userId") userId: Long?,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("region") region: Int?
    ): Call<FlyHistoryStatic>

    /**
     * 飞机的飞行记录获取团队
     */
    @GET("oper/drones/{droneId}/groups")
    fun getSelectTeams(@Path("droneId") droneId: String): Call<List<SelectTeam>>

    /**
     * 飞机的飞行记录获取队员
     */
    @GET("oper/drones/{droneId}/users")
    fun getSelectPersons(@Path("droneId") droneId: String): Call<List<SelectOper>>

    // 设备名称修改
    @PUT("oper/drones/updateDroneName")
    fun updateDroneName(@Body info: UpdateDroneNameInfo): Call<Unit>

    // 制造商ID修改
    @PUT("drone/device/manage/{droneId}/zzDroneNum")
    fun updateZzDroneName(
        @Path("droneId") droneId: String,
        @Body info: UpdateZzDroneNameInfo
    ): Call<Unit>

    /**
     * 锁定/解锁无人机
     */
    @PUT("drone/device/manage/lockOpr")
    fun lockOpr(@Body info: DroneLockInfo): Call<Unit>

    @GET("drone/device/manage/sorties")
    fun getSortieInfo(@Header("ids") ids: String): Call<List<DeviceSortieInfo>>

    // 架次轨迹
    @GET("drone/device/manage/tracks")
    fun getTrackBrief(@Header("ids") ids: String): Call<List<TrackBrief>>

    /**
     * 团队管理-团队一览
     */
    @GET("oper/groups")
    fun getGroups(
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int
    ): Call<com.jiagu.jgcompose.paging.Page<Team>>

    /**
     * 团队管理-创建团队
     */
    @POST("oper/groups")
    fun createGroup(@Body info: CreateGroupInfo): Call<Unit>

    //保存/更新设备使用记录
    @POST("device/deviceUseRecord")
    fun saveDeviceUseRecord(@Body deviceUseRecord: DeviceUseRecord): Call<Unit>

    //获取禁飞区
    @GET("drone/nofly/manage/getNoflyList")
    fun getNoflyList(
        @Query("currentPage") currentPage: Int,
        @Query("pageSize") pageSize: Int,
        @Query("noflyType") noflyType: Int,
        @Query("detailAddress") detailAddress: String,
    ): Call<com.jiagu.jgcompose.paging.Page<NoFlyZoneInfo>>

    //用户实名认证
    @POST("user/user/manage/identityVerify")
    fun identityVerify(@Body identityInfo: IdentityInfo): Call<Boolean>

    //获取实名信息
    @GET("user/user/manage/checkVerify")
    fun checkVerify(): Call<IdentityVerify>

    /**
     * 团队详情
     */
    @GET("oper/groups/{groupId}")
    fun groupDetail(@Path("groupId") groupId: Long): Call<GroupDetail>

    /**
     * 团队成员一览
     */
    @GET("oper/groups/userList/{groupId}")
    fun groupUserList(
        @Path("groupId") groupId: Long,
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int
    ): Call<com.jiagu.jgcompose.paging.Page<Member>>

    /**
     * 解散团队
     */
    @DELETE("oper/groups/{groupId}")
    fun deleteGroup(@Path("groupId") groupId: Long): Call<Unit>

    /**
     * 退出团队
     */
    @DELETE("oper/groups/leaveGroup/{groupId}")
    fun leaveGroup(@Path("groupId") groupId: Long): Call<Unit>

    /**
     * 编辑团队名称
     */
    @PUT("oper/groups/updateName")
    fun updateGroupName(@Body info: UpdateGroupNameInfo): Call<Unit>

    /**
     * 团队成员加入
     */
    @POST("oper/groups-users")
    fun addGroupMembers(@Body info: AddGroupMemberInfo): Call<Unit>

    /**
     * 团队成员加入---可加入员工一览
     */
    @GET("oper/groups/addableUserList/{groupId}")
    fun addableUserList(
        @Path("groupId") groupId: Long,
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int
    ): Call<com.jiagu.jgcompose.paging.Page<TeamEmployee>>

    /**
     * 删除团队成员
     */
    @DELETE("oper/groups-users/{groupId}/{userId}")
    fun deleteGroupMembers(@Path("groupId") groupId: Long, @Path("userId") userId: Long): Call<Unit>

    /**
     * 转让队长
     */
    @PUT("oper/groups/transferLeader")
    fun transferLeader(@Body info: TransferLeaderInfo): Call<Unit>

    /**
     * 用户架次统计
     */
    @GET("oper/users-sorties/statistics/{groupId}/{userId}")
    fun getUserWorkStatic(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long
    ): Call<UserWorkStatic>

    /**
     * 用户架次列表-分页
     */
    @GET("oper/users-sorties/{groupId}/{userId}")
    fun getUserWorkReport(
        @Path("groupId") groupId: Long,
        @Path("userId") userId: Long,
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long
    ): Call<com.jiagu.jgcompose.paging.Page<MemberReportDetail>>

    /**
     * 团队统计
     */
    @GET("oper/groups-sorties/statistics/{groupId}")
    fun getTeamWorkStatic(
        @Path("groupId") groupId: Long,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("userIds") userIds: Array<Long>?
    ): Call<FlyHistoryStatic>

    /**
     * 团队架次列表-分页
     */
    @GET("oper/groups-sorties/{groupId}")
    fun getTeamWorkReport(
        @Path("groupId") groupId: Long,
        @Query("pageIndex") pageIndex: Int,
        @Query("size") size: Int,
        @Query("startTime") startTime: Long?,
        @Query("endTime") endTime: Long?,
        @Query("userIds") userIds: Array<Long>?
    ): Call<com.jiagu.jgcompose.paging.Page<TeamWorkReport>>

    /**
     * 激活设备
     */
    @PUT("oper/drones/active/{droneId}")
    fun activeDrone(@Path("droneId") droneId: String): Call<Unit>
}