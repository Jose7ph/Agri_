package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.work.BlockParam
import com.jiagu.ags4.vm.work.IWorkParameter
import com.jiagu.ags4.vm.work.IWorkParameterTemplate
import com.jiagu.ags4.vm.work.WorkParameterImpl
import com.jiagu.ags4.vm.work.WorkParameterTemplateImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.VKAg.IMUData
import com.jiagu.device.vkprotocol.VKAgTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt


class EnhancedManualModel(app: Application) : AndroidViewModel(app),
    IWorkParameter by WorkParameterImpl(),
    IWorkParameterTemplate by WorkParameterTemplateImpl() {
    val context = getApplication<Application>()
    var flyMode by mutableIntStateOf(0)
    private var firstPoint: GeoHelper.LatLngAlt? = null
    private var secondPoint: GeoHelper.LatLngAlt? = null
    var curAngle = MutableStateFlow<Int?>(0)

    //航线变更按钮启用判断
    var routeChangeFlag by mutableStateOf(false)

    private val converter = GeoHelper.GeoCoordConverter()
    fun checkFlyModel(imuData: IMUData) {
        if (flyMode != imuData.flyMode.toInt()) {
            flyMode = imuData.flyMode.toInt()
            if (VKAgTool.isGpsMode(flyMode)) {
                firstPoint = null
                secondPoint = null
                exeTask { curAngle.emit(null) }
            }
        }
        if (firstPoint != null && secondPoint != null) {
            val p1 = converter.convertLatLng(firstPoint)
            val p2 = converter.convertLatLng(secondPoint)
            val l = p1.distance(p2)
            val cosx = (p2.x - p1.x) / l
            val sinx = (p2.y - p1.y) / l
            val angle = Math.toDegrees(Math.atan2(sinx, cosx))
            val curA = if (angle.isNaN()) 0.0 else angle
            exeTask { curAngle.emit(convertMathAngleToAircraft(curA).roundToInt()) }
        } else if (firstPoint != null) {
            exeTask { curAngle.emit(imuData.yaw.roundToInt()) }
        }
        checkRouteChange(imuData)
    }

    fun setAPoint(imuData: IMUData) {
        routeChangeFlag = false
        firstPoint = GeoHelper.LatLngAlt(imuData.lat, imuData.lng, imuData.alt.toDouble())
    }

    fun setBPoint(imuData: IMUData) {
        if (secondPoint != null) return
        secondPoint = GeoHelper.LatLngAlt(imuData.lat, imuData.lng, imuData.alt.toDouble())
    }

    private fun convertMathAngleToAircraft(angle: Double): Double {
        val raw = 90.0 - angle
        return ((raw + 180) % 360 + 360) % 360 - 180
    }

    //检测是否可以启用航线变更按钮
    fun checkRouteChange(imuData: IMUData) {
        //不是m+模式，航线变更按钮不启用
        if (!VKAgTool.isEnhanceMode(imuData.flyMode.toInt())) {
            routeChangeFlag = false
        }
        //firstPoint已存在 && 当前routeChangeFlag是false
        firstPoint?.let { fp ->
            if (!routeChangeFlag) {
                routeChangeFlag =
                    GeoHelper.distance(imuData.lat, imuData.lng, fp.latitude, fp.longitude) >= 3
            }
        }
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

    fun setTemplate(templateParam: TemplateParam, complete: (Boolean, List<() -> Unit>?) -> Unit) {
        exeTask {
            val blockParam =
                BlockParam(
                    blockType = Block.TYPE_BLOCK,
                    rotationalSpeed = rotationalSpeed,
                    sprayOrSeedMu = sprayOrSeedMu,
                    pumpOrValveSize = pumpOrValveSize,
                    mode = mode
                )
            setTemplate(
                blockParam = blockParam,
                templateParam = templateParam,
            ) { success, bp ->
                val commands = bp?.let { buildCommands(it) }
                complete(success, commands)
            }
        }
    }

    private fun buildCommands(blockParam: BlockParam): List<() -> Unit> {
        val commands = mutableListOf<() -> Unit>()
        commands.add { AptypeUtil.setPumpMode(blockParam.mode) }
        commands.add { AptypeUtil.setPumpAndValve(blockParam.pumpOrValveSize.toFloat()) }
        commands.add { AptypeUtil.setSprayMu(blockParam.sprayOrSeedMu) }
        commands.add { AptypeUtil.setCenAndSeedSpeed(blockParam.rotationalSpeed.toFloat()) }
        return commands
    }
}