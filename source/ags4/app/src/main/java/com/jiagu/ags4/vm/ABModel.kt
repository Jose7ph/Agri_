package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.exeComplexTask
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.work.BlockParam
import com.jiagu.ags4.vm.work.IWorkParameter
import com.jiagu.ags4.vm.work.IWorkParameterTemplate
import com.jiagu.ags4.vm.work.WorkParameterImpl
import com.jiagu.ags4.vm.work.WorkParameterTemplateImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MapTrack
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.tools.v9sdk.ABUtils
import kotlinx.coroutines.flow.MutableStateFlow

class ABModel(app: Application) : AndroidViewModel(app), IWorkParameter by WorkParameterImpl(),
    IWorkParameterTemplate by WorkParameterTemplateImpl() {

    //左右箭头
    var toLeft by mutableStateOf(false)

    val ptA = MutableStateFlow<GeoHelper.LatLng?>(null)
    val ptB = MutableStateFlow<GeoHelper.LatLng?>(null)
    val abLine = MutableStateFlow<MapTrack?>(null)
    val lineA = MutableStateFlow<MapRing?>(null)
    val lineB = MutableStateFlow<MapRing?>(null)

    var selectBreakIndex = -1
    private var imuTime = 0L

    //打b点后，修改为true
    var setPointBFlag = false

    fun processImuData(imuData: VKAg.IMUData) {
        setFlyMode(imuData)
        checkABModel(imuData.flyMode.toInt())
        checkNaviDone(imuData)
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

    fun removeTemplate(paramId: Long, complete: (Boolean) -> Unit) {
        exeTask {
            deleteTemplate(paramId = paramId, complete = complete)
        }
    }

    fun setTemplate(templateParam: TemplateParam, complete: (Boolean, List<() -> Unit>?) -> Unit) {
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

    fun clearAB() {
        setPointBFlag = false
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

    private var preABMode = 0

    //判断飞行状态，用于获取断点
    fun checkABModel(mode: Int) {
        if (VKAgTool.isABMode(mode)) preABMode = mode
        if (VKAgTool.isABMode(preABMode) && mode != preABMode) {
            DroneModel.activeDrone?.apply {
                getABInfo()
            }
            preABMode = mode
        }
    }

    fun setFlyMode(imuData: VKAg.IMUData) {
        val droneLocation = GeoHelper.LatLng(imuData.lat, imuData.lng)
        checkFlyMode(imuData)
        if (showBreak && imuData.airFlag == VKAg.AIR_FLAG_ON_AIR && imuData.ABStatus == VKAgCmd.AB_STA_LOCK.toShort()) {
            val current = System.currentTimeMillis()
            if (current - imuTime > 1000 && abWp != null && !abLine.value.isNullOrEmpty()) {
                imuTime = current
                emitBreakPoints(ABUtils.generateABBreakPoint(droneLocation, abWp!!))
            }
            if (showBreak && VKAgTool.isABMode(imuData.flyMode.toInt())) {
                clearABBreak()//手动拨摇杆，开始AB模式，清除断点，停止计算
                if(!flyPause){ //当前面是是ab模式，但是flyPause=true，说明是通过拨杆继续，需要清除计算的AB断点
                    clearBreakIndexAndList()
                    flyPause = false
                }
            }
        } else {
            if (!showBreak) return
            clearABBreak()//落地后，停止断点计算
        }
        if (abWpFlow.value != null && imuData.ABStatus.toByte() == VKAgCmd.AB_A_POS) {
            abWpFlow.value = null
        }
    }

    fun collectABData(
        abData: VKAg.ABData,
        flyMode: Short?,
        complete: () -> Unit,
    ) {
        if (DroneModel.abplData.value == null || flyMode == VKAgCmd.FLYSTATUS_AB) {
            setAB(abData.lat, abData.lng, abData.idx.toInt(), width, abData.anglea, abData.angleb) {
                if (abData.idx.toInt() == 2 && !setPointBFlag) {
                    setPointBFlag = false
                    changeABDir()
                }
            }
        }
        if (calcBreaks.value.isNotEmpty() && selectBreakIndex != -1 && abData.idx.toInt() > calcBreaks.value[selectBreakIndex].index) {//AB断点=目标点
            clearBreakIndexAndList()
            clearBreakpoint()
            complete()
        }
        //清除br断点
        abWp?.let {
            if (abData.idx.toInt() > it.index) {
                clearBreakpoint()
            }
        }
    }

    fun clearBreakIndexAndList() {
        selectBreakIndex = -1
        calcBreaks.value = listOf()
    }

    //修改ab规划方向 打B点和修改方向的时候各触发一次
    fun changeABDir() {
        val (aa, ab) = ABUtils.changeABDir(toLeft)
        if (aa != null || ab != null) {
            val dir = if (toLeft) VKAgCmd.AB_DIR_LEFT else VKAgCmd.AB_DIR_RIGHT
            DroneModel.activeDrone?.setABDirection(dir.toInt(), aa!!, ab!!)
        }
    }

    private fun emitBreakPoints(data: List<VKAg.ABPLData>) {
        exeTask { calcBreaks.emit(data) }
    }

    fun clearModel() {
        exeTask {
            ABUtils.clearAB()
            ptA.emit(null)
            ptB.emit(null)
            abLine.emit(null)
            toLeft = false
            lineA.emit(null)
            lineB.emit(null)
            clearAB()
            clearABBreak()//清除AB点
            clearBreakIndexAndList()
            DroneModel.abplData.value = null //清除livedata中的AB断点数据，防止收到过断点数据，起飞后，组件第一次监听到数据，会直接获取上次的飞控断点数据
            clearBreakpoint()
        }
    }

    fun setAB(
        lat: Double,
        lng: Double,
        idx: Int,
        width: Float,
        aY: Float,
        bY: Float,
        complete: () -> Unit,
    ) {
        exeComplexTask {
            ABUtils.setAB(lat, lng, idx, width, aY, bY) {
                ptA.emit(it.pA)
                ptB.emit(it.pB)
                if (it.pAAngleLine == null) {
                    lineA.emit(null)
                } else {
                    lineA.emit(listOf(it.pAAngleLine!!.first, it.pAAngleLine!!.second))
                }
                if (it.pBAngleLine == null) {
                    lineB.emit(null)
                } else {
                    lineB.emit(listOf(it.pBAngleLine!!.first, it.pBAngleLine!!.second))
                }
                abLine.emit(it.abLine)
                toLeft = it.toLeft
                complete()
            }
        }
    }

    var showBreak by mutableStateOf(false)
    val calcBreaks = MutableStateFlow<List<VKAg.ABPLData>>(listOf())
    var curCalcBK: VKAg.ABPLData? = null
    var flyPause = false

    private var preFlyRouteMode = -1

    fun clearCalcBreaks() {
        preFlyRouteMode = -1
        showBreak = false
        flyPause = false
    }

    //清除AB断点，停止计算
    fun clearABBreak() {
        imuTime = 0
        clearCalcBreaks()
    }

    fun clearBreakpoint() {
        abWpFlow.value = null
        abWp = null
    }

    private fun checkFlyMode(imuData: VKAg.IMUData) {
        if (imuData.airFlag == VKAg.AIR_FLAG_ON_GROUND) return
        if (VKAgTool.isABMode(imuData.flyMode.toInt())) {
            preFlyRouteMode = imuData.flyMode.toInt()
        }
        //之前是航线模式/AB模式 现在是GPS模式 && 是起飞后的页面 开始计算断点
        if (VKAgTool.isABMode(preFlyRouteMode) && VKAgTool.isGpsMode(imuData.flyMode.toInt())) {
            flyPause = true
            showBreak = true
        }
    }

    //AB断点
    val abWpFlow = MutableStateFlow<VKAg.ABPLData?>(null)

    var abWp: VKAg.ABPLData? = null
    private fun emitBreakPoint() {
        exeTask { abWpFlow.emit(abWp) }
    }

    fun clearCalcBK() {
        selectBreakIndex = -1
        curCalcBK = null
    }

    fun setABPLData(bp: VKAg.ABPLData?) {
        abWp = bp

        if (abLine.value.isNullOrEmpty() || abWp == null || abWp!!.index >= abLine.value!!.size) {
            emitBreakPoint()
            return
        } else {
            val pt = ABUtils.getFormatABPLData(abWp!!.index.toInt(), abWp!!.break_lat, abWp!!.break_lng)
            if (pt == null) {
                emitBreakPoint()
            } else {
                abWp!!.break_lat = pt.latitude
                abWp!!.break_lng = pt.longitude
                emitBreakPoint()
            }
        }
    }

    //检查航线是否完成
    private fun checkNaviDone(data: VKAg.IMUData) {
        if (data.airFlag == VKAg.AIR_FLAG_ON_AIR && !abLine.value.isNullOrEmpty()) {
            if ((data.flyMode == VKAgCmd.FLYSTATUS_GCSFANHANG && data.returnReason == VKAgCmd.GOHOME_REASON_HANGXIANWANCHENG) ||//返航，地面站主动返航
                (data.flyMode == VKAgCmd.FLYSTATUS_GCSXUANTING && data.hoverReason == VKAgCmd.HOVER_REASON_AB_HANGXIANWANCHENG)
            ) {//悬停，航线完成悬停
                clearModel()//AB点航线完成
            }
        }
    }
}
