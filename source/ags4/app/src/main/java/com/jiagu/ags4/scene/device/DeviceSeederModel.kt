package com.jiagu.ags4.scene.device

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.Constants
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.LocalParam
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.AgsNet.process2
import com.jiagu.ags4.repo.net.model.ChartData
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.ags4.utils.ParamTool.getDroneParam
import com.jiagu.ags4.utils.ParamTool.updateDroneParam
import com.jiagu.ags4.utils.ParamTool.uploadDroneParam
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.work.IWorkSeedMaterial
import com.jiagu.ags4.vm.work.WorkSeedMaterialImpl
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.math.Point2D
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


data class ChartItem(
    var isCheck: Boolean, var chartData: ChartData, var isLocal: Boolean, var localId: Long,
)

val LocalDeviceSeederModel =
    compositionLocalOf<DeviceSeederModel> { error("No DeviceSeederModel provided") }

class DeviceSeederModel(app: Application) : AndroidViewModel(app),
    IWorkSeedMaterial by WorkSeedMaterialImpl() {
    val context = getApplication<Application>()
    var chartDatas = mutableStateListOf<ChartItem>()
    private val appConfig = AppConfig(context)
    var preCheckedPosition by mutableIntStateOf(-1)
    var curReCalibPos = -1
    var isEditParam = false
    var currentSeederMaterial by mutableStateOf<ChartItem?>(null)
    var initScrollToItemFlag = true
    var currentIndex by mutableIntStateOf(-1)

    /**
     * 点击按钮校验
     */
    fun onClickCheck(context: Context, chartItem: ChartItem): Boolean {
        if (!AgsUser.netIsConnect && !chartItem.isLocal) {
            context.toast(context.getString(R.string.err_network))
            return false
        }
        return true
    }

    /**
     * 获取飞机参数列表
     */
    fun initDroneParamList(complete: (Boolean) -> Unit) {
        chartDatas.clear()
        viewModelScope.launch {
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
                    context.toast(it)
                    complete(false)
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
                    complete(true)
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
                complete(true)
            })
        }
    }

    /**
     * 删除播撒物料
     */
    fun deleteDroneParam(id: Long, complete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val param = AgsDB.getParamById(id)
            if (param != null) {
                process2({
                    AgsDB.removeParam(param._id)
                    if (!param.isLocal) {
                        AgsNet.deleteDroneParam(param.paramId).networkFlow {
                            context.toast(it)
                            complete(false)
                            return@networkFlow
                        }.collectLatest {
                            context.toast("success")
                        }
                    }
                }, {
                    if (param.isLocal) {
                        AgsDB.removeParam(param._id)
                    }
                })
            }
            val delChartItem = chartDatas[curReCalibPos]
            chartDatas.removeAt(curReCalibPos)
            //删除的物料 == 已选择的物料 重置appConfig.curMaterialId
            if (curReCalibPos == preCheckedPosition) {
                currentIndex = preCheckedPosition - 1
                preCheckedPosition = -1
                //当前物料名称清除
                currentSeederMaterial = delChartItem.apply {
                    chartData.name = ""
                }
            }
            curReCalibPos = -1
            complete(true)
        }
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
                    val chartItem = ChartItem(
                        false, ChartData(
                            droneParam.paramId, droneParam.paramName, it, userData?.type, userData
                        ), droneParam.isLocal, droneParam.localId
                    )
                    datas.add(chartItem)
                }
            }
        }
        chartDatas.addAll(datas)
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

    /**
     * 重命名物料
     */
    fun renameSeederMaterial(rename: String, chartItem: ChartItem, complete: (Boolean) -> Unit) {
        val id = chartItem.chartData.id
        val type = Constants.TYPE_PARAM_SEED
        val param = chartItem.chartData.param!!
        viewModelScope.launch {
            //更新飞控数据
            val err = updateDroneParam(id, type, rename, param)
            if (err != null) {
                context.toast(err)
                complete(false)
                return@launch
            }
            if (chartItem.isLocal) {
                updateLocalParamFromId(chartItem.localId, complete)
            } else {
                updateParamFromId(id, complete)
            }
        }
    }

    private fun updateLocalParamFromId(id: Long, complete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val paramLocal = AgsDB.getParamById(id)
            val param = DroneParam(
                paramLocal.type,
                paramLocal.paramName,
                paramLocal.param,
                paramLocal.isLocal,
                paramLocal.paramId,
                paramLocal._id
            )
            param.apply { updateParam(curReCalibPos, this) }
            complete(true)
        }
    }

    fun updateParamFromId(id: Long, complete: (Boolean) -> Unit) {
        exeTask {
            val (param, err) = getDroneParam(id)
            if (err != null) {
                context.toast(err)
                complete(false)
            } else {
                param?.apply {
                    updateParam(curReCalibPos, this)
                }
                complete(true)
            }
        }
    }


    private fun updateParam(pos: Int, s: DroneParam) {
        val oldChartDatas = mutableListOf<ChartItem>()
        oldChartDatas.addAll(chartDatas)
        s.param?.let {
            val userData = readParam(it)
            val chartItem = ChartItem(
                false,
                ChartData(s.paramId, s.paramName, it, userData?.type, userData),
                s.isLocal,
                s.localId
            )
            oldChartDatas[pos] = chartItem
            chartDatas.clear()
            chartDatas.addAll(oldChartDatas)
            curReCalibPos = -1
            isEditParam = false
        }
    }

    fun handleChart(data: IProtocol.UserData): Pair<MutableList<Pair<Float, Float>>, MutableList<Pair<Float, Float>>>? {
        var chartData: Pair<MutableList<Pair<Float, Float>>, MutableList<Pair<Float, Float>>>? =
            null
        val r = MemoryHelper.MemoryReader(data.data, 0, data.data.size)
        val pts = mutableListOf<Point2D>()
        for (i in 0..5) {
            val v = r.readLEShort()
            if (i < 3) {
                pts.add(Point2D(v.toDouble(), 0.0))
            } else {
                pts[i - 3].y = v.toDouble()
            }
        }
        pts.add(0, Point2D(1000.00, 0.00))
        var indexFlow = -1
        DroneModel.aptypeData.value?.let {
            val mu = it.getValue(VKAg.APTYPE_MUYONGLIANG) * 1000
            val speed = it.getValue(VKAg.APTYPE_AB_MAX_SPEED)
            val width = it.getValue(VKAg.APTYPE_AB_WIDTH)
            val flow = mu / 667.0000 * speed * width
            for ((i, pt) in pts.withIndex()) {
                if (pt.y > flow) {
                    indexFlow = i
                    break
                }
            }
            if (indexFlow > pts.size - 1) indexFlow = pts.size - 1
            if (indexFlow < 0) indexFlow = 0
            if (indexFlow >= 1) {
                val x2 = pts[indexFlow].x
                val y2 = pts[indexFlow].y
                val x1 = pts[indexFlow - 1].x
                val y1 = pts[indexFlow - 1].y
                val y = flow
                val x = (((x1 - x2) * (y - y1)) / (y1 - y2)) + x1
                pts.add(indexFlow, Point2D(x, flow))
            } else {
                if (pts.last().y < flow) {
                    pts.add(Point2D(pts.last().x, flow))
                    indexFlow = pts.size - 1
                } else pts.add(indexFlow, Point2D(pts.first().x, flow))
            }
            chartData = showChart(pts, indexFlow)
        }
        return chartData
    }

    private fun showChart(
        lines: List<Point2D>, hightIndex: Int,
    ): Pair<MutableList<Pair<Float, Float>>, MutableList<Pair<Float, Float>>> {
        val entries = mutableListOf<Pair<Float, Float>>()
        for (v in lines) {
            entries.add(Pair(v.x.toFloat(), v.y.toFloat()))
        }
        val flowEntries = mutableListOf<Pair<Float, Float>>()
        val hightX = lines[hightIndex].x.toFloat()
        val hightY = lines[hightIndex].y.toFloat()
        flowEntries.add(Pair(hightX, hightY))
        return entries to flowEntries
    }

    fun setUserData(chartData: ChartData) {
        DroneModel.activeDrone?.apply {
            if (chartData.type != null && chartData.data != null) {
                setUserData(chartData.type, chartData.data.data)
            }
        }
    }

    fun processUserData(data: IProtocol.UserData) {
        stopDataMonitor()
        //飞控数据跟列表数据比较，发现参数一直则默认选中，若没有参数一致的则不选中
        val param = readDroneDataParam(data)
        for ((i, chartItem) in chartDatas.withIndex()) {
            if (param == chartItem.chartData.param) { //如果参数一样则以第一个发现的物料为准
                Log.d("zhy", "发现相同物料......:${chartItem.chartData.name} ")
                preCheckedPosition = i
                currentSeederMaterial = chartItem
                break
            }
        }
        //没有找到参数一样的
        if (currentSeederMaterial == null) {
            currentSeederMaterial = ChartItem(
                isCheck = true,
                chartData = ChartData(
                    0,
                    "",
                    param,
                    data.type,
                    data
                ),
                isLocal = false,
                localId = 0L,
            )
        }
        DroneModel.activeDrone?.getParameters()
    }
}








