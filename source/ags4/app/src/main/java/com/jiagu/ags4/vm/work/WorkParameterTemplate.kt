package com.jiagu.ags4.vm.work

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.ags4.bean.TemplateData
import com.jiagu.ags4.bean.TemplateParam
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.optimizeRoute
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.flow.collectLatest


interface IWorkParameterTemplate {
    var isJobLoading: Boolean

    var templateName: String

    suspend fun deleteTemplate(paramId: Long, complete: (Boolean) -> Unit)
    suspend fun setTemplate(
        blockParam: BlockParam,
        templateParam: TemplateParam,
        complete: (Boolean, BlockParam?) -> Unit,
    )

    suspend fun getTemplateData(id: Long, complete: (Boolean, DroneParam?) -> Unit)
    suspend fun getTemplateParamList(type: Int, complete: (Boolean, List<TemplateParam>?) -> Unit)
    suspend fun saveTemplateData(
        name: String,
        type: Int,
        templateData: TemplateData,
        complete: (Boolean) -> Unit,
    )

    fun readTemplate(str: String?): TemplateData?

    fun toUploadData(blockParam: BlockParam): UploadNaviData
}

class WorkParameterTemplateImpl() : IWorkParameterTemplate {
    override var isJobLoading by mutableStateOf(false)
    override var templateName by mutableStateOf("")
    override suspend fun deleteTemplate(paramId: Long, complete: (Boolean) -> Unit) {
        AgsNet.deleteDroneParam(paramId).networkFlow {
            complete(false)
        }.collectLatest {
            complete(true)
        }
    }

    override suspend fun setTemplate(
        blockParam: BlockParam,
        templateParam: TemplateParam,
        complete: (Boolean, BlockParam?) -> Unit,
    ) {
        if (templateParam.param == null) {
            getTemplateData(templateParam.id) { success, data ->
                if (!success) {
                    complete(false, null)
                    return@getTemplateData
                }
                if (data == null) {
                    complete(false, null)
                    return@getTemplateData
                }
                templateParam.param = readTemplate(data.param)
                data.param?.let { p ->
                    updateTemplatePlan(
                        blockParam = blockParam, templateData = TemplateData.fromString(p)
                    )
                    complete(true, blockParam)
                }
            }
        } else {
            updateTemplatePlan(
                blockParam = blockParam, templateData = templateParam.param!!
            )
            complete(true, blockParam)
        }
    }

    override suspend fun getTemplateData(id: Long, complete: (Boolean, DroneParam?) -> Unit) {
        AgsNet.getDroneParam(id).networkFlow {
            complete(false, null)
        }.collectLatest {
            complete(true, it)
        }
    }

    override suspend fun getTemplateParamList(
        type: Int,
        complete: (Boolean, List<TemplateParam>?) -> Unit,
    ) {
        val templateParamList = mutableListOf<TemplateParam>()
        AgsNet.getDroneParams(type).networkFlow {
            complete(false, null)
        }.collectLatest {
            for (p in it) {
                val t = TemplateParam(p.paramId, p.paramName, readTemplate(p.param))
                t.localId = p.localId
                templateParamList.add(t)
            }
            complete(true, templateParamList.toList())
        }
    }

    override suspend fun saveTemplateData(
        name: String,
        type: Int,
        templateData: TemplateData,
        complete: (Boolean) -> Unit,
    ) {
        val param = templateData.toString()
        AgsNet.uploadDroneParam(DroneParam(type, name, param)).networkFlow {
            complete(false)
        }.collectLatest { id ->
            AgsNet.getDroneParam(id).networkFlow {
                complete(false)
            }.collectLatest {
                complete(true)
            }
        }
    }

    override fun readTemplate(str: String?): TemplateData? {
        return if (str == null) null else TemplateData.fromString(str)
    }

    /**
     * Update template plan
     * 根据当前作业机类型，处理喷洒/播撒参数
     *
     * @param blockParam 地块参数
     * @param templateData 参数模板数据
     */
    fun updateTemplatePlan(blockParam: BlockParam, templateData: TemplateData) {
        blockParam.width = templateData.width
        blockParam.speed = templateData.speed
        blockParam.height = templateData.height
        blockParam.materialId = templateData.materialId
        blockParam.materialName = templateData.materialName

        if (isSeedWorkType()) {
            blockParam.sprayOrSeedMu = templateData.seedMu
            blockParam.rotationalSpeed = templateData.seedRotateSpeed
            blockParam.pumpOrValveSize = templateData.valveSize
            blockParam.mode = templateData.seedMode
        } else {
            blockParam.sprayOrSeedMu = templateData.sprayMu
            blockParam.rotationalSpeed = templateData.centrifugalSize
            blockParam.pumpOrValveSize = templateData.pumpSize.toInt()
            blockParam.mode = templateData.pumpMode
        }
    }

    /**
     * 航线上传数据
     *
     * @param blockParam 地块参数
     * @return
     */
    override fun toUploadData(blockParam: BlockParam): UploadNaviData {
        val uploadNaviData = UploadNaviData(
            height = if (blockParam.height == 0f) 3f else blockParam.height,
            speed = blockParam.speed,
            width = blockParam.width,
            qty = blockParam.sprayOrSeedMu,
            fixed = blockParam.pumpOrValveSize.toFloat(),
            naviId = blockParam.naviId,
            naviType = blockParam.blockType
        )//保存规划的时候 只拿航点的经纬度 计算naviId，用处：当飞控里的航线和缓存时不一样时，需要上传新航线
        val track = optimizeRoute(blockParam.routePoints)
        for (p in track) {
            p.height = blockParam.height
        }
        val cmds = mutableListOf<() -> Unit>()
        cmds.add { DroneModel.activeDrone?.setNaviProperties(uploadNaviData) }
        cmds.add { AptypeUtil.setPumpAndValve(blockParam.pumpOrValveSize.toFloat()) }
        cmds.add { AptypeUtil.setSprayMu(blockParam.sprayOrSeedMu) }
        cmds.add { AptypeUtil.setPumpMode(blockParam.mode) }
        cmds.add { AptypeUtil.setCenAndSeedSpeed(blockParam.rotationalSpeed.toFloat()) }
        uploadNaviData.commands = cmds
        uploadNaviData.route = track
        return uploadNaviData
    }
}

/**
 * 地块参数
 *
 * @property naviId 航线id
 * @property blockType 地块类型
 * @property width 垄距
 * @property mode 喷洒/播撒模式
 * @property sprayOrSeedMu 喷洒/播撒亩用量 （ml/亩）（g/亩）
 * @property speed 速度
 * @property height 高度
 * @property pumpOrValveSize 流量大小 / 阀门开度 (%)
 * @property rotationalSpeed 喷头/甩盘转速 (%)
 * @property materialId 物料ID
 * @property materialName 物料名称
 * @property routePoints 航点列表
 * @constructor Create empty Block param
 */
data class BlockParam(
    var naviId: Int = -1,
    var blockType: Int,
    var width: Float = 3.5f, //planParam.curRidge
    var mode: Int = VKAg.BUMP_MODE_FIXED,
    var sprayOrSeedMu: Float = 1000f,
    var speed: Float = 5f,
    var height: Float = 2f,
    var pumpOrValveSize: Int = 50,
    var rotationalSpeed: Int = 50,//planParam.seedRotateSpeed  planParam.centrifugalSize
    var materialId: Long = 0L,
    var materialName: String? = "",
    var routePoints: List<RoutePoint> = emptyList<RoutePoint>(),


) {
    override fun toString(): String {
        return "BlockParam(naviId=$naviId, blockType=$blockType, width=$width, mode=$mode, sprayOrSeedMu=$sprayOrSeedMu, speed=$speed, height=$height, pumpOrValveSize=$pumpOrValveSize, rotationalSpeed=$rotationalSpeed, materialId=$materialId, materialName=$materialName, routePoints=$routePoints)"
    }
}