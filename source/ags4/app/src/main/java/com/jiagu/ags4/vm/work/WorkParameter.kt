package com.jiagu.ags4.vm.work

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.TemplateData
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.LocalParam
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.AgsNet.process2
import com.jiagu.ags4.repo.net.model.ChartData
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.ags4.scene.device.ChartItem
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.ags4.utils.ParamTool.uploadDroneParam
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.flow.collectLatest


interface IWorkParameter {
    var rotationalSpeed: Int //喷头转速
    var sprayOrSeedMu: Float //亩用量
    var pumpOrValveSize: Int //流量大小
    var mode: Int //喷洒模式
    var width: Float //行距
    var speed: Float //速度
    var relativeHeight: Float //相对作物高度
    var materialIndex: Int //当前物料索引
    var materialId: Long //当前物料id
    var materialName: String //当前物料名称
    var materialList: List<ChartItem> //物料列表
    suspend fun initMaterialList(complete: (Boolean, String?) -> Unit)
    fun setMaterialInfoByMaterialId(materialId: Long)
    fun setMaterialInfoByIndex(index: Int)
    fun clearMaterialInfo()
    fun buildTemplatePlan(): TemplateData
    fun setAptypeData(aptypeData: VKAg.APTYPEData)
    fun setUserData(chartData: ChartData)
}

class WorkParameterImpl() : IWorkParameter {
    override var rotationalSpeed by mutableIntStateOf(50)
    override var sprayOrSeedMu by mutableFloatStateOf(1000f)
    override var pumpOrValveSize by mutableIntStateOf(50)
    override var mode by mutableIntStateOf(VKAg.BUMP_MODE_FIXED)
    override var width by mutableFloatStateOf(4f)
    override var speed by mutableFloatStateOf(5f)
    override var relativeHeight by mutableFloatStateOf(0f)
    override var materialIndex by mutableIntStateOf(-1)
    override var materialId by mutableLongStateOf(0L)
    override var materialName by mutableStateOf("")
    override var materialList by mutableStateOf<List<ChartItem>>(emptyList())

    override fun setMaterialInfoByMaterialId(mId: Long) {
        if (materialList.isEmpty() || mId == 0L) {
            clearMaterialInfo()
            return
        }
        materialList.withIndex().find { (_, item) ->
            item.chartData.id == mId
        }?.let { (index, material) ->
            materialIndex = index
            materialId = material.chartData.id
            materialName = material.chartData.name
        }
    }

    override fun setMaterialInfoByIndex(index: Int) {
        if (materialList.isEmpty() || index < 0) {
            clearMaterialInfo()
            return
        }
        if (materialList.size > index) {
            materialIndex = index
            materialId = materialList[index].chartData.id
            materialName = materialList[index].chartData.name
            setUserData(materialList[index].chartData)
        }
    }

    override fun clearMaterialInfo() {
        materialIndex = -1
        materialName = ""
        materialId = 0L
    }

    override fun buildTemplatePlan(): TemplateData {
        //喷洒/播撒参数都设置，方便后续设置时自动根据喷洒/播撒取值
        val templateData = TemplateData()

        templateData.width = width
        templateData.speed = speed
        templateData.height = relativeHeight

        templateData.materialId = materialId
        templateData.materialName = materialName

        templateData.pumpMode = mode
        templateData.seedMode = mode
        templateData.pumpSize = pumpOrValveSize.toFloat()
        templateData.valveSize = pumpOrValveSize
        templateData.sprayMu = sprayOrSeedMu
        templateData.seedMu = sprayOrSeedMu
        templateData.centrifugalSize = rotationalSpeed
        templateData.seedRotateSpeed = rotationalSpeed
        return templateData
    }

    override fun setAptypeData(aptypeData: VKAg.APTYPEData) {
        mode = aptypeData.getIntValue(VKAg.APTYPE_PUMP_MODE)
        pumpOrValveSize = aptypeData.getValue(VKAg.APTYPE_PUMP_FIXED_VALUE).toInt()
        sprayOrSeedMu = aptypeData.getValue(VKAg.APTYPE_MUYONGLIANG) * 1000
        rotationalSpeed = aptypeData.getValue(VKAg.APTYPE_CENTRIFUGAL_SIZE).toInt()
        width = aptypeData.getValue(VKAg.APTYPE_AB_WIDTH)
        speed = aptypeData.getValue(VKAg.APTYPE_AB_MAX_SPEED)
    }

    override fun setUserData(chartData: ChartData) {
        DroneModel.activeDrone?.apply {
            if (chartData.type != null && chartData.data != null) {
                setUserData(chartData.type, chartData.data.data)
            }
        }
    }

    override suspend fun initMaterialList(
        complete: (Boolean, String?) -> Unit,
    ) {
        val type = Constants.TYPE_PARAM_SEED
        process2(work = {
            AgsDB.removeRemoteParamsByType(type)
            var localParams = AgsDB.getParamsByType(type)
            val mList = mutableListOf<DroneParam>()
            for (param in localParams) {
                if (param.isLocal) {
                    uploadDroneParam(param.type, param.paramName, param.param)
                    AgsDB.removeParam(param._id)
                }
            }
            AgsNet.getDroneParams(type).networkFlow {
                complete(false, it)
            }.collectLatest {
                for (param in it) {
                    val result = LocalParam(0, type, param.paramName, param.param!!)
                    result.userId = AgsUser.userInfo?.userId!!
                    result.isLocal = false
                    result.paramId = param.paramId
                    AgsDB.insertParam(result)
                }
                localParams = AgsDB.getParamsByType(type)
                for (param in localParams) {
                    mList.add(
                        DroneParam(
                            param.type,
                            param.paramName,
                            param.param,
                            param.isLocal,
                            if (param.paramId == 0L) param._id else param.paramId,
                            param._id
                        )
                    )
                }
                buildChartDataList(mList)
                complete(true, null)
            }
        }, local = {
            val localParams = AgsDB.getParamsByType(type)
            val mList = mutableListOf<DroneParam>()
            for (param in localParams) {
                mList.add(
                    DroneParam(
                        param.type,
                        param.paramName,
                        param.param,
                        param.isLocal,
                        if (param.paramId == 0L) param._id else param.paramId,
                        param._id
                    )
                )
            }
            buildChartDataList(mList)
            complete(true, null)
        })
    }

    /**
     * 构建物料数据
     */
    private fun buildChartDataList(mList: MutableList<DroneParam>) {
        val datas = mutableListOf<ChartItem>()
        if (mList.isNotEmpty()) {
            mList.forEach { droneParam ->
                droneParam.param?.let {
                    val userData = readParam(it)
//                    val isCheck = configMaterialId == droneParam.paramId
                    val chartItem = ChartItem(
                        false, ChartData(
                            droneParam.paramId, droneParam.paramName, it, userData?.type, userData
                        ), droneParam.isLocal, droneParam.localId
                    )
                    datas.add(chartItem)
                }
            }
        }
        materialList = datas.toList()
    }

    private fun readParam(param: String): IProtocol.UserData? {
        val params = ParamTool.loadIndexedParamsFromServer(param)
        DroneModel.activeDrone?.apply {
            //params.size=6(6个参数+1个ID)
            val userData = ByteArray((params.size - 1) * 2)
            val type = params[0].ivalue
            var off = 0
            for (i in 1 until params.size) {
                val p = params[i]
                MemoryHelper.LittleEndian.putShort(userData, off, p.ivalue.toShort())
                off += 2
            }
            return IProtocol.UserData(type, userData)
        }
        return null
    }

}
