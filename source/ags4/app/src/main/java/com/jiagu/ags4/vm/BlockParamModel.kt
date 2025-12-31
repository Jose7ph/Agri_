package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.IWorkMapMarker
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.WorkMapMarkerImpl
import com.jiagu.ags4.vm.work.BlockParam
import com.jiagu.ags4.vm.work.IWorkParameter
import com.jiagu.ags4.vm.work.IWorkParameterTemplate
import com.jiagu.ags4.vm.work.IWorkPlan
import com.jiagu.ags4.vm.work.IWorkRouteParameter
import com.jiagu.ags4.vm.work.IWorkSeedMaterial
import com.jiagu.ags4.vm.work.WorkParameterImpl
import com.jiagu.ags4.vm.work.WorkParameterTemplateImpl
import com.jiagu.ags4.vm.work.WorkPlanImpl
import com.jiagu.ags4.vm.work.WorkRouteParameterImpl
import com.jiagu.ags4.vm.work.WorkSeedMaterialImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.tools.v9sdk.RouteModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

class BlockParamModel(app: Application) : AndroidViewModel(app),
    IWorkPlan by WorkPlanImpl(),
    IWorkParameter by WorkParameterImpl(),
    IWorkSeedMaterial by WorkSeedMaterialImpl(),
    IWorkParameterTemplate by WorkParameterTemplateImpl(),
    IWorkMapMarker by WorkMapMarkerImpl(),
    IWorkRouteParameter by WorkRouteParameterImpl() {
    private val context = getApplication<Application>()
    private val config = Config(context)
    private val appConfig = AppConfig(context)
    var isInit = false

    //中转点
    val auxPoints = mutableStateListOf<GeoHelper.LatLng>()
    val auxPointsFlow = MutableStateFlow<List<GeoHelper.LatLng>>(emptyList())
    var calcAuxPoints = mutableListOf<GeoHelper.LatLng>()

    var showDeletePointPanel by mutableStateOf(false)
    var showMoveRoutePanel by mutableStateOf(false)
    var selectedDeletePointIndexes by mutableStateOf(Pair<Int, Int>(-1, -1))
    var selectedDeletePointIndexesFlow = MutableStateFlow<List<Int>>(emptyList())

    //当前track中marker点数量
    var curMarkerCount by mutableIntStateOf(1)
    var curAllMarkers = linkedMapOf<String, GeoHelper.LatLng>()

    //当前plan类型 0-新规划 1-开始作业
    var curPlanType = RouteModel.PLAN_NEW

    fun initBlockPlanAndParam(blockPlan: BlockPlan, isNewPlan: Boolean) {
        initWorkParam()
        initPlanParam(blockPlan)
        exeTask {
            setBlockPlan(blockPlan)
            curPlanType = if (isNewPlan) RouteModel.PLAN_NEW else RouteModel.PLAN_LAST_WORK
            if (curPlanType == RouteModel.PLAN_LAST_WORK) {
                DroneModel.blockPlan.emit(blockPlan)
            } else {
                DroneModel.blockPlan.emit(null)
            }
            DroneModel.blockId = blockPlan.blockId
            DroneModel.localBlockId = blockPlan.localBlockId
            DroneModel.workRoutePoint.clear()
        }
    }

    private fun initWorkParam() {
        relativeHeight = config.workHeight
        speed = AptypeUtil.getABSpeed()
        width = config.workRidge
        mode = AptypeUtil.getPumpMode()
        rotationalSpeed = AptypeUtil.getCenAndSeedSpeed().toInt()
        pumpOrValveSize = AptypeUtil.getPumpAndValve().toInt()
        sprayOrSeedMu = AptypeUtil.getSprayMu()
    }

    private fun initPlanParam(blockPlan: BlockPlan) {
        blockPlan.plan?.let {
            workPlan = it
            it.param?.let { param ->
                updatePlanParamByCurrentParam(param)
            }
        }
    }

    fun updatePlanParamByCurrentParam(param: PlanParamInfo) {
        Log.v("shero", "作业参数 ${param.toLog()}")
        relativeHeight = param.height
        speed = param.speed
        width = param.curRidge
        //水泵模式的值是 1/2，如果保存的参数是0时，使用飞控当前参数
        mode = when {
            param.pumpMode != 0 -> param.pumpMode
            param.seedMode != 0 -> param.seedMode
            else -> {
                AptypeUtil.getPumpMode()
            }
        }
        rotationalSpeed = when {
            param.centrifugalSize != 0 -> param.centrifugalSize
            param.seedRotateSpeed != 0 -> param.seedRotateSpeed
            else -> {
                AptypeUtil.getCenAndSeedSpeed().toInt()
            }
        }
        pumpOrValveSize = when {
            param.valveSize != 0 -> param.valveSize
            param.pumpSize != 0f -> param.pumpSize.toInt()
            else -> AptypeUtil.getPumpAndValve().toInt()
        }
        sprayOrSeedMu = when {
            param.sprayMu != 0f -> param.sprayMu
            param.seedMu != 0f -> param.seedMu
            else -> AptypeUtil.getSprayMu()
        }
    }

    fun markerSelect(mIdx: Int) {
        if (mIdx < 0) {
            return
        }
        clearDeletePointMarker()
        val point = auxPoints[mIdx]
        changeSelectedMarkerIndex(mIdx)
        showMarkerPanel = true
        initMarker(GeoHelper.LatLngAlt(point.latitude, point.longitude, 0.0))
    }

    //nextIdx 不是null说明是通过输入框传入的，不排序
    fun markerDeletePointSelect(mIdx: Int, nextIdx: Int? = null) {
        clearMarker()
        val (start, end) = selectedDeletePointIndexes
        if (nextIdx == null) { //从地图点击航点选择
            when {
                start >= 0 && end < 0 -> {//起点有值 终点是空
                    selectedDeletePointIndexes = handleDeletePoint(start, mIdx)
                }

                start < 0 && end >= 0 -> {//起点是空 终点有值
                    selectedDeletePointIndexes = handleDeletePoint(mIdx, end)
                }

                start < 0 && end < 0 -> {//起点和终点都是空 根据当前点直接生成2个点
                    selectedDeletePointIndexes = handleDeletePoint(mIdx)
                }
            }
        } else {//输入框选择
            selectedDeletePointIndexes = when {
                mIdx < 0 && nextIdx < 0 -> {// 未选择任何点 (删除了全不点)
                    Pair(-1, -1)
                }

                mIdx >= 0 && nextIdx < 0 -> {// 仅选择了起点 (删除了终点)
                    Pair(mIdx, -1)
                }

                mIdx < 0 && nextIdx >= 0 -> {// 仅选择了终点 (删除了起点)
                    Pair(-1, nextIdx)
                }

                else -> { // 选择了起点和终点
                    handleDeletePoint(mIdx, nextIdx)
                }
            }
        }
        emitDeletePointMarker()
    }

    private fun handleDeletePoint(start: Int, end: Int? = null): Pair<Int, Int> {
        val startPair = if (start % 2 == 0) {
            Pair(start, start + 1)
        } else {
            Pair(start - 1, start)
        }
        val endPair = if (end != null) {
            if (end % 2 == 0) {
                Pair(end, end + 1)
            } else {
                Pair(end - 1, end)
            }
        } else {
            null
        }
        return if (endPair != null && end != null) {
            if (start < end) {
                Pair(startPair.first, endPair.second)
            } else {
                Pair(startPair.second, endPair.first)
            }
        } else {
            startPair
        }
    }

    fun emitDeletePointMarker() {
        val (start, end) = selectedDeletePointIndexes
        selectedDeletePointIndexesFlow.value = when {
            start < 0 && end < 0 -> emptyList()       // 未选择任何点
            start >= 0 && end < 0 -> listOf(start)    // 仅选择了起点
            start < 0 && end >= 0 -> listOf(end)      // 仅选择了终点
            else -> (minOf(start, end)..maxOf(start, end)).toList() // 选择了起点和终点
        }
        showDeletePointPanel = true
    }

    fun clearDeletePointMarker() {
        selectedDeletePointIndexes = Pair(-1, -1)
        selectedDeletePointIndexesFlow.value = emptyList()
        showDeletePointPanel = false
    }

    fun updatePoint(pt: GeoHelper.LatLngAlt) {
        if (selectedMarkerIndex < 0 || selectedMarkerIndex >= auxPoints.size) return
        auxPoints[selectedMarkerIndex] = pt
        exeTask {
            auxPointsFlow.emit(auxPoints.toList())
        }
    }

    fun removePointByIndex() {
        auxPoints.removeAt(selectedMarkerIndex)
        exeTask {
            auxPointsFlow.emit(auxPoints.toList())
        }
        clearMarker()
    }

    fun changeMarkerPoint(isNext: Boolean = false) {
        if (auxPoints.size <= 1) {
            return
        }
        var markerPosition: GeoHelper.LatLngAlt? = null
        var curIdx = selectedMarkerIndex
        curIdx = if (isNext) {
            if (curIdx == auxPoints.lastIndex) 0 else curIdx + 1
        } else {
            if (curIdx == 0) auxPoints.lastIndex else curIdx - 1
        }
        val pt = auxPoints[curIdx]
        markerPosition = GeoHelper.LatLngAlt(pt.latitude, pt.longitude, 0.0)
        changeSelectedMarkerIndex(curIdx)
        initMarker(markerPosition)
    }

    fun removeTemplate(paramId: Long, complete: (Boolean) -> Unit) {
        exeTask {
            deleteTemplate(paramId = paramId, complete = complete)
        }
    }

    fun saveTemplateData(name: String, complete: (Boolean) -> Unit) {
        exeTask {
            val templateData = buildTemplatePlan()
            saveTemplateData(
                name = name,
                type = Constants.TYPE_PARAM_PLAN,
                templateData = templateData,
                complete = complete
            )
        }
    }

    fun getTemplateParamList(complete: (Boolean, List<TemplateParam>?) -> Unit) {
        exeTask {
            getTemplateParamList(type = Constants.TYPE_PARAM_PLAN, complete = complete)
        }
    }

    fun setTemplate(
        templateParam: TemplateParam,
        complete: (Boolean, List<() -> Unit>?) -> Unit,
    ) {
        exeTask {
            val blockParam = BlockParam(
                blockType = Block.TYPE_BLOCK,
                rotationalSpeed = rotationalSpeed,
                sprayOrSeedMu = sprayOrSeedMu,
                pumpOrValveSize = pumpOrValveSize,
                mode = mode
            )
            setTemplate(
                blockParam = blockParam, templateParam = templateParam
            ) { success, bp ->
                val commands = bp?.let { buildCommands(it) }
                complete(success, commands)
            }
        }
    }

    private fun buildCommands(blockParam: BlockParam): List<() -> Unit> {
        val commands = mutableListOf<() -> Unit>()
        commands.add { AptypeUtil.setPumpMode(blockParam.mode) }
        commands.add { AptypeUtil.setABWidth(blockParam.width) }
        commands.add { AptypeUtil.setABSpeed(blockParam.speed) }
        commands.add { AptypeUtil.setPumpAndValve(blockParam.pumpOrValveSize.toFloat()) }
        commands.add { AptypeUtil.setSprayMu(blockParam.sprayOrSeedMu) }
        commands.add { AptypeUtil.setCenAndSeedSpeed(blockParam.rotationalSpeed.toFloat()) }
        commands.add {
            DroneModel.activeDrone?.setNaviProperties(toUploadData(blockParam))
        }
        return commands
    }

    fun addAuxPoint(pt: GeoHelper.LatLng) {
        for (aux in auxPoints) {
            if (abs(aux.latitude - pt.latitude) < 1E-6 || abs(aux.longitude - pt.longitude) < 1E-6) {
                return
            }
        }
        auxPoints.add(pt)
        exeTask {
            auxPointsFlow.emit(auxPoints.toList())
        }
    }

    fun addAuxWarn() {
        NewUserWarnTool.addWarn(
            NewWarnTool.WarnStringData(
                NewWarnTool.WARN_TYPE_WARN,
                context.getString(com.jiagu.v9sdk.R.string.user_warn_aux_error),
                "",
                NewWarnTool.WARN_DELAY_TIME,
                System.currentTimeMillis(),
                context.getString(com.jiagu.v9sdk.R.string.voice_aux_error)
            )
        )
    }

    fun saveOrUpdatePlan(isUpdate: Boolean, complete: () -> Unit) {
        exeTask {
            workPlan?.let {
                if (!isUpdate) {
                    savePlan(it, complete)
                } else {
                    updatePlan2(it, complete)
                }
            }
        }
    }

    fun getBlockPlan(complete: (BlockPlan?) -> Unit) {
        exeTask {
            getLocalBlockPlan {
                complete(it)
            }
        }
    }

    fun blockWorking(complete: () -> Unit = {}) {
        exeTask {
            selectedLocalBlockId?.let {
                blockWorking(it, complete = complete)
            }
        }
    }

    fun processUserData(data: IProtocol.UserData) {
        stopDataMonitor()
        //飞控数据跟列表数据比较，发现参数一直则默认选中，若没有参数一致的则不选中
        val param = readDroneDataParam(data)
        exeTask {
            initMaterialList { success, msg ->
                if (success) {
                    for ((i, chartItem) in materialList.withIndex()) {
                        if (param == chartItem.chartData.param) { //如果参数一样则以第一个发现的物料为准
                            Log.d("zhy", "发现相同物料......:${chartItem.chartData.name} ")
                            materialIndex = i
                            materialId = chartItem.chartData.id
                            materialName = chartItem.chartData.name
                            break
                        }
                    }
                }
            }
        }
        DroneModel.activeDrone?.getParameters()
    }
}