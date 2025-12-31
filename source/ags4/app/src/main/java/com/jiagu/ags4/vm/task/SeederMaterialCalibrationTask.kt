package com.jiagu.ags4.vm.task

import android.util.Log
import com.jiagu.ags4.Constants
import com.jiagu.ags4.scene.device.DeviceSeederModel
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAgCmd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * 播撒物料新增/更新校准 task
 * @param name 物料名称
 * @param isEdit true 更新校准 false 新增物料
 */
class SeederMaterialCalibrationTask(
    private val name: String,
    private val seederModel: DeviceSeederModel,
    private val isEdit: Boolean = false,
) : ProgressTask() {

    private var newChartName: String = "新曲线"
    private var paramFile: File? = null
    private val PARAM_EXT = ".seed"

    //monitorType = 1： 校准进度监控 2：saveFile后userData数据监控
    private var monitorType = 0
    private var userData: IProtocol.UserData? = null

    //获取到的answerData结果 1:成功 -1:失败
    private var answerDataResult = 0

    private var getUserDataResult = false

    private val monitor = { data: Any ->
        Log.d(
            "zhy",
            "data is UserData:${data is IProtocol.UserData},data is AnswerData : ${data is IProtocol.AnswerData}"
        )
        when (data) {
            is IProtocol.UserData -> {
                Log.d(
                    "zhy",
                    "monitor  UserData monitorType=${monitorType},data.calib_status=${data.calib_status},progress = ${data.calib_progress}"
                )
                if (monitorType == 1) {
                    if (data.calib_status == 2) {
                        postProgress(data.calib_progress.toString())
                    }
                }
                if (monitorType == 2) {
                    Log.d("zhy", "monitor  UserData monitorType = 2......")
                    userData = data
                    getUserDataResult = true
                }
            }

            is IProtocol.AnswerData -> {
                if (monitorType == 1) {
                    Log.d("zhy", "monitor  AnswerData monitorType=1, data.param2=${data.param2}")
                    if (data.msgid == VKAgCmd.MSG_CAL.toInt() && data.cmd == VKAgCmd.CAL_SEED.toInt() && data.param1 == 0) {
                        if (data.param2 == 1) answerDataResult = -1
                        else if (data.param2 == 0) answerDataResult = 1
                    }
                }
            }
        }
    }

    fun startCalibFlow() {//0开始 1取消
        DroneModel.activeDrone?.apply {
            startDataMonitor(monitor)//监听userData
            calibSeederFlow(0)
        }
    }

    fun stopCalibFlow() {
        DroneModel.activeDrone?.apply {
            calibSeederFlow(1)
            stopDataMonitor(monitor)
        }
    }

    private suspend fun calibFlow(): Int {
        monitorType = 1
        Log.d("zhy", "monitorType = ${monitorType}，startCalibFlow")
        startCalibFlow()
        // 超时 5 分钟
        val r = withTimeoutOrNull(300_000L) {
            while (true) {
                Log.d("zhy", "get answerData...... ")
                if (answerDataResult != 0) {
                    Log.d("zhy", "get answerData success!, answerDataResult:${answerDataResult} ")
                    break
                }

                Log.d("zhy", "get answerData await 500 ms")
                delay(500)
            }
            1
        } ?: -1
        monitorType = 0
        val step = if (answerDataResult < 0 || r < 0) {
            stopCalibFlow() //校准失败停止校准
            4
        } else 5
        //用于告知组件切换当前step
        postNotice(step.toString(), null)
        return step
    }

    private suspend fun saveData(): Pair<Boolean, String?> {
        monitorType = 2
        Log.d("zhy", "current monitorType = 2 ,start get save data ")
        while (true) {
            Log.d("zhy", "send getUserData command.......")
            DroneModel.activeDrone?.getParameters()
            DroneModel.activeDrone?.getUserData(1)
            if (getUserDataResult) {
                Log.d("zhy", "get save data success, stopCalibFlow")
                break
            }
            Log.d("zhy", "get UserData await 500 ms")
            delay(500)
        }
        monitorType = 0
        stopCalibFlow()
        Log.d("zhy", "start startCalibChart, name = ${name}, isEdit = ${isEdit}")
        startCalibChart(name = name, isEdit = isEdit)
        if (userData != null) {
            handleFile(userData!!)
            return true to "保存成功"
        } else {
            return false to "保存失败, userData is null"
        }
    }

    override suspend fun start(): Pair<Boolean, String?> {
        val step = calibFlow()
        //step 4说明校准失败只能取消或者重新校准 所以task直接结束
        if (step == 4) {
            return true to "校准失败"
        }
        Log.d("zhy", "校准成功，开始保存数据 ")
        val result = saveData()
        Log.d("zhy", "保存结果: ${result} ")
        return result
    }

    private fun startCalibChart(name: String, isEdit: Boolean = false) {
        seederModel.isEditParam = isEdit
        DroneModel.activeDrone?.let {
            newChartName = name
            saveFile(getParamFile(name))
        }
    }

    /**
     *  显示图表
     */

    private suspend fun handleFile(data: IProtocol.UserData) {
        if (paramFile == null) {
            return
        }
        val r = MemoryHelper.MemoryReader(data.data, 0, data.data.size)
        val list = mutableListOf<IProtocol.IndexedParam>()
        list.add(IProtocol.IndexedParam(1, data.type))//0 ID 1个字节
        for (i in 0..5) {
            list.add(IProtocol.IndexedParam(1, r.readLEUShort()))
        }
        withContext(Dispatchers.IO) {
            if (seederModel.isEditParam && seederModel.curReCalibPos != -1) {
                Log.d("zhy", "物料校准更新.....")
                val id = seederModel.chartDatas[seederModel.curReCalibPos].chartData.id
                ParamTool.updateIndexedParamsToServer(
                    id,
                    list,
                    Constants.TYPE_PARAM_SEED,
                    newChartName
                ) {
                    if (it != null) context.toast(it)
                    else seederModel.updateParamFromId(id) {}
                }
            } else {
                Log.d("zhy", "物料校准新增.....")
                val (id, err) = ParamTool.uploadDroneParam(
                    Constants.TYPE_PARAM_SEED,
                    newChartName,
                    ParamTool.indexedParamsToString(list)
                )
                if (err != null) {
                    context.toast(err)
                } else if (id != null) {
                    seederModel.initDroneParamList { }
                }
            }
            paramFile = null
            DroneModel.activeDrone?.getParameters()
        }
    }

    private fun saveFile(file: File) {
        paramFile = file
    }

    private fun getParamFile(fn: String): File {
        val file = context.getExternalFilesDir("param")
        return File(file, "${fn}${PARAM_EXT}")
    }
}