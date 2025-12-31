package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.ags4.vm.work.IWorkBreakpoint
import com.jiagu.ags4.vm.work.IWorkClean
import com.jiagu.ags4.vm.work.IWorkEdit
import com.jiagu.ags4.vm.work.IWorkEditCanvas
import com.jiagu.ags4.vm.work.IWorkPage
import com.jiagu.ags4.vm.work.IWorkPlan
import com.jiagu.ags4.vm.work.WorkBreakpointImpl
import com.jiagu.ags4.vm.work.WorkCleanImpl
import com.jiagu.ags4.vm.work.WorkEditCanvasImpl
import com.jiagu.ags4.vm.work.WorkEditImpl
import com.jiagu.ags4.vm.work.WorkPageImpl
import com.jiagu.ags4.vm.work.WorkPlanImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.GeoHelper.GeoCoordConverter
import com.jiagu.api.math.Point2D
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.tools.v9sdk.CleanUtils

class AreaCleanModel(app: Application) : AndroidViewModel(app),
    IWorkPage by WorkPageImpl(Block.TYPE_AREA_CLEAN), IWorkPlan by WorkPlanImpl(),
    IWorkEdit by WorkEditImpl(), IWorkClean by WorkCleanImpl(),
    IWorkEditCanvas by WorkEditCanvasImpl(), IWorkBreakpoint by WorkBreakpointImpl() {
    val content = getApplication<Application>()
    var dronePosition: GeoHelper.LatLngAlt? = null
    private var preTime = System.currentTimeMillis()

    //isEdit = true 是编辑页面，isStart = true 是起飞页面, 都是 false 是编辑参数页面
    fun processImuData(imuData: VKAg.IMUData, isEdit: Boolean = false) {
        val current = System.currentTimeMillis()
        //飞机位置
        dronePosition = GeoHelper.LatLngAlt(
            imuData.lat, imuData.lng, imuData.height.toDouble()
        )
        checkCleanModel(imuData.flyMode.toInt(), selectedLocalBlockId)
        if (current - preTime > 500) {
            preTime = current
            //编辑
            if (isEdit) {
                updateBlock2DData()
            } else {
                //编辑参数 | 起飞
                generateParam2DData()
            }
        }
        checkNaviDone(imuData)
        checkAndClearBreakpoint(imuData)
    }

    fun generateParam2DData() {
        //编辑参数 | 起飞
        val bk = breakPoint?.let {
            GeoHelper.LatLngAlt(it.lat, it.lng, it.height.toDouble())
        }
        CleanUtils.generateDroneAndBreakPoint2D(dronePosition!!, bk) { droneP, breakP ->
            dronePoint2D = droneP
            breakPoint2D = breakP
        }
    }

    //当飞机飞过断点后，把地图上的断点都清除
    fun checkAndClearBreakpoint(imuData: VKAg.IMUData) {
        breakPoint?.let { bk ->
            if (imuData.target.toInt() > bk.index + 1) {
                // 已经过了断点
                exeTask {
                    clearBK()
                }
            }
        }
    }

    fun refresh(localBlockId: Long? = null) {
        exeTask {
            dronePosition?.let {
                refreshList(
                    position = it, loadLocalComplete = {
                        //本地数据加载完后，处理默认选中
                        localBlockId?.let {
                            localBlocksListFlow.value.find { it.localBlockId == localBlockId }
                                ?.let { bp ->
                                    setBP(blockPlan = bp, initParam = false)
                                }
                        }
                    })
            }
        }
    }

    fun deleteLocalBlock(localBlockId: Long, complete: () -> Unit) {
        exeTask {
            deleteBlock(localBlockId = localBlockId, complete = complete)
        }
    }

    fun setBP(blockPlan: BlockPlan, initParam: Boolean = true) {
        exeTask {
            setBlockPlan(blockPlan)
            if (initParam) {
                //重新选择后初始化参数
                initClean()
            }
            //查询当前地块断点
            getBreakpoint()
        }
    }

    fun clearBP() {
        exeTask {
            clearBlockPlan()
        }
    }

    fun renameLocalBlock(localBlockId: Long, name: String, complete: () -> Unit) {
        exeTask {
            renameBlock(localBlockId = localBlockId, name = name, complete = complete)
        }
    }

    fun removeAllPoint() {
        exeTask {
            clearAllPoint()
            clearWorkPoints()
        }
    }

    fun removeLastPoint() {
        exeTask {
            clearLastPoint()
            if (points.isEmpty()) {
                clearWorkPoints()
            }
        }
    }

    fun saveBlock(name: String, complete: (List<Long>) -> Unit) {
        exeTask {
            buildAndSaveBlock(name = name, buildBlock = {
                val block = Block(
                    Block.TYPE_AREA_CLEAN,
                    name,
                    listOf(points),
                    doubleArrayOf(),
                    0f,
                    DroneModel.groupId
                )
                block.createTime = System.currentTimeMillis()
                block.comment = if (routeYaw == null) "" else Block.makeComment(routeYaw!!)
                block
            }, complete = complete)
        }
    }

    private fun updateBlock2DData() {
        calcBlockPoint()
    }

    fun initParam() {
        selectedBP?.comment?.let { comment ->
            try {
                if (comment.isNotEmpty()) {
                    routeYaw = comment.toFloat()
                } else {
                }
            } catch (e: NumberFormatException) {
                Log.e("zhy", "routeYaw: convert error,${e.message}")
            }
        }
        verticalAB()
    }

    fun verticalAB() {
        CleanUtils.verticalAB(
            list = selectedBP!!.boundary[0],
            scanSpace = width,
            yaw = routeYaw ?: 0f,
            isABModel = false
        ) { boundary, pt2ds, routes ->
            exeTask {
                calcRoutePoint(
                    boundary = boundary,
                    pts = pt2ds,
                    routes = routes,
                )
            }
        }
    }


    fun uploadNavi(): NaviTask? {
        done = false
        return uploadNavi(isABMode = false, breakPoint = this.breakPoint)
    }

    fun makePlan(complete: (Plan) -> Unit) {
        exeTask {
            val planParamInfo = PlanParamInfo()
            planParamInfo.speed = speed
            planParamInfo.curRidge = width
            planParamInfo.repeatCount = repeatCount
            val plan = Plan(
                VKAg.MISSION_UTYPE_AB,
                repeatRoutes,
                0f,
                0f,
                0f,
                0f,
                0,
                selectedBlockId ?: 0,
                planParamInfo
            ).apply {
                localBlockId = selectedLocalBlockId ?: 0
            }
            makePlan(plan = plan, complete = {
                complete(it)
                updateSelectedPlan(it)
            })
        }
    }

    fun blockWorking(complete: () -> Unit = {}) {
        exeTask {
            selectedLocalBlockId?.let {
                blockWorking(it, complete = complete)
            }
        }
    }

    fun blockFinish() {
        exeTask {
            selectedLocalBlockId?.let {
                blockFinish(it)
            }
        }
    }

    fun loadSelectBpParam() {
        initParam()
        selectedBP?.plan?.param?.let { planParamInfo ->
            this.speed = planParamInfo.speed
            this.width = planParamInfo.curRidge
            this.repeatCount = planParamInfo.repeatCount
        }
    }

    private var done = false
    fun checkNaviDone(imuData: VKAg.IMUData) {
        if (repeatRoutes.isEmpty() || selectedLocalBlockId == null || done) return
        val wt = repeatRoutes
        if ((imuData.flyMode == VKAgCmd.FLYSTATUS_GCSFANHANG && imuData.returnReason == VKAgCmd.GOHOME_REASON_HANGXIANWANCHENG && imuData.target >= wt.size) ||//19 & 17
            (imuData.flyMode == VKAgCmd.FLYSTATUS_GCSXUANTING && imuData.hoverReason == VKAgCmd.HOVER_REASON_HANGXIANWANCHENG && imuData.target >= wt.size) ||//18 & 3
            (imuData.target > wt.size)
        ) {
            done = true
            blockFinish()
            getBlockPlan { bp ->
                selectedBP = bp
            }
            //完成后清除断点
            exeTask {
                clearBK()
            }
            //删除本地断点
            deleteLocalBreakpoint()
            refresh(selectedLocalBlockId)
        }
    }

    fun calcBlockPoint() {
        exeTask {
            val converter = GeoCoordConverter()
            val convPoints = points.map { GeoHelper.LatLng(it.latitude, it.longitude) }
            val point2D = mutableListOf<Point2D>()
            converter.convertLatLng(convPoints, point2D)

            val point2D_y = mutableListOf<Point2D>()
            for ((i, d) in point2D.withIndex()) {
                point2D_y.add(Point2D(d.x, points[i].altitude))
            }
            if (dronePosition == null) dronePoint2D = null
            else {
                val d = converter.convertLatLng(dronePosition)
                dronePoint2D = Point2D(d.x, dronePosition!!.altitude)
            }
            blockPoints2D.emit(point2D_y)
        }
    }

    fun getBlockPlan(complete: (BlockPlan?) -> Unit) {
        exeTask {
            getLocalBlockPlan {
                complete(it)
            }
        }
    }

    fun getBreakpoint() {
        //优先判断本地数据库有没有，没有则判断架次有没有，都没有就是没有断点
        getLocalBreakpoint {
            exeTask {
                if (it != null) {
                    setBK(it)
                    return@exeTask
                }
                if (selectedBP?.additional?.bk != null) {
                    setBK(selectedBP?.additional?.bk)
                    return@exeTask
                }
                setBK(null)
            }
        }
    }

    fun clearBreakpoint() {
        exeTask {
            clearBK()
        }
    }

    private fun getLocalBreakpoint(complete: (VKAg.BreakPoint?) -> Unit) {
        exeTask {
            selectedLocalBlockId?.let {
                getLocalBK(localBlockId = it, complete = { bk ->
                    complete(bk)
                })
            }
        }
    }

    fun deleteLocalBreakpoint() {
        exeTask {
            selectedLocalBlockId?.let {
                deleteLocalBK(localBlockId = it)
            }
        }
    }
}