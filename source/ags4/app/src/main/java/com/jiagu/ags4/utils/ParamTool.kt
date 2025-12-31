package com.jiagu.ags4.utils

import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.Constants.TYPE_PARAM_DRONE
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.LocalParam
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.AgsNet.process2
import com.jiagu.ags4.repo.net.model.DroneParam
import com.jiagu.ags4.vm.DroneObject
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.api.viewmodel.WaitTask
import com.jiagu.v9sdk.R
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import kotlin.math.pow

object ParamTool {

    fun indexedToString(p: IProtocol.IndexedParam): String {
        return "${p.index}:${p.ivalue}"
    }

    fun indexedParamsToString(params: List<IProtocol.IndexedParam>): String {
        val sb = StringBuilder(indexedToString(params[0]))
        for (i in 1 until params.size) {
            sb.append(",").append(indexedToString(params[i]))
        }
        return sb.toString()
    }

    private fun flowToString(data: VKAg.FlowData): String {
        return "${data.param},${data.qty}"
    }

    private fun parseIndexedParams(string: String): List<IProtocol.IndexedParam>? {
        return try {
            val out = mutableListOf<IProtocol.IndexedParam>()
            val segs = string.split(",")
            for (s in segs) {
                val p = s.split(":")
                out.add(IProtocol.IndexedParam(p[0].toInt(), p[1].toInt()))
            }
            out
        } catch (e: Throwable) {
            null
        }
    }

    //获取飞机参数详情
    suspend fun getDroneParam(id: Long): Pair<DroneParam?, String?> {
        var param: DroneParam? = null
        var msg: String? = null
        process2({
            AgsNet.getDroneParam(id).networkFlow {
                msg = it
            }.collectLatest {
                param = it
            }
        }, {
            val paramLocal = AgsDB.getParamById(id)
            param = DroneParam(
                paramLocal.type,
                paramLocal.paramName,
                paramLocal.param,
                paramLocal.isLocal,
                paramLocal.paramId,
                paramLocal._id
            )
        })
        return param to msg
    }

    private fun parseFlowData(string: String): VKAg.FlowData? {
        try {
            val segs = string.split(",")
            val flow = VKAg.FlowData()
            flow.param = segs[0].toFloat()
            flow.qty = segs[1].toFloat()
            return flow
        } catch (e: Throwable) {
            return null
        }
    }

    fun saveIndexedParams(params: List<IProtocol.IndexedParam>, file: File) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write(indexedParamsToString(params))
        writer.flush()
        writer.close()
    }

    fun loadIndexedParamsFromServer(param: String): List<IProtocol.IndexedParam> {
        return parseIndexedParams(param)!!
    }


    suspend fun saveIndexedParamsToServer(
        params: List<IProtocol.IndexedParam>,
        type: Int,
        name: String,
        complete: (Long?, String?) -> Unit
    ) {
        val (id, err) = uploadDroneParam(type, name, indexedParamsToString(params))
        complete(id, err)
    }

    suspend fun uploadDroneParam(type: Int, name: String, param: String): Pair<Long?, String?> {
        var result: String? = null
        var id: Long = 0
        process2({
            AgsNet.uploadDroneParam(DroneParam(type, name, param)).networkFlow {
                result = it
            }.collectLatest {
                id = it
            }
        }, {
            val result = LocalParam(0, type, name, param)
            result.userId = AgsUser.userInfo?.userId!!
            id = AgsDB.insertParam(result)
        })
        return id to result
    }

    suspend fun updateIndexedParamsToServer(
        id: Long,
        params: List<IProtocol.IndexedParam>,
        type: Int,
        name: String,
        complete: (String?) -> Unit
    ) {
        val err = updateDroneParam(id, type, name, indexedParamsToString(params))
        complete(err)
    }

    suspend fun updateDroneParam(id: Long, type: Int, name: String, param: String): String? {
        var msg: String? = null
        process2({
            val paramObj = AgsDB.getParamByParamId(id)
            if (paramObj != null) {
                paramObj.type = type
                paramObj.paramName = name
                paramObj.param = param
                paramObj.updateTime = System.currentTimeMillis()
                AgsDB.updateParamById(
                    paramObj._id,
                    name,
                    param,
                    paramObj.isLocal,
                    if (paramObj.isLocal) false else true,
                    paramObj.isDelete
                )
            }
            val d = DroneParam(type, name, param)
            d.paramId = paramObj.paramId
            AgsNet.updateDroneParam(d).networkFlow {
                msg = it
            }.collectLatest { }
        }, {
            val paramObj = AgsDB.getParamById(id)
            if (paramObj != null) {
                if (paramObj.isLocal) {
                    AgsDB.updateParamById(
                        paramObj._id,
                        name,
                        param,
                        paramObj.isLocal,
                        isUploaded = false,
                        isDelete = false
                    )
                }
            }
        })
        return msg
    }

    class DownloadParamTask(val device: DroneObject, val file: File, val name: String) :
        ProgressTask() {
        private var aptype: VKAg.APTYPEData? = null
        private var pid: VKAg.PIDData? = null
        private var flow: VKAg.FlowData? = null
        private var progress = 0
        private val dataListener = { p: Any ->
            when (p) {
                is VKAg.APTYPEData -> {
                    aptype = p
                    device.getPidParameters()
                }

                is VKAg.PIDData -> {
                    pid = p
                    device.getBumpParam()
                }

                is VKAg.FlowData -> {
                    flow = p
                    progress = 1
                }
            }
        }

        private fun saveParam(): Pair<Boolean, String?> {
            return try {
                val writer = BufferedWriter(FileWriter(file))
                writer.write(indexedParamsToString(aptype!!.params))
                writer.newLine()
                writer.write(indexedParamsToString(pid!!.params))
                writer.newLine()
                writer.write(flowToString(flow!!))
                writer.newLine()
                writer.flush()
                writer.close()
                true to getString(R.string.ver_param_file_save_ok)
            } catch (e: IOException) {
                e.printStackTrace()
                false to getString(R.string.ver_param_file_save_fail)
            }
        }

        private suspend fun saveParamToServer(param: String): Pair<Boolean, String?> {
            var result = true to ""
            AgsNet.uploadDroneParam(DroneParam(TYPE_PARAM_DRONE, name, param)).networkFlow {
                result = false to it
            }.collectLatest {
                result = true to getString(R.string.ver_param_file_save_ok)
            }
            return result
        }

        private fun getParams(): String? {
            return try {
                val stringWriter = StringWriter()
                val writer = BufferedWriter(stringWriter)
                writer.write(indexedParamsToString(aptype!!.params))
                writer.newLine()
                writer.write(indexedParamsToString(pid!!.params))
                writer.newLine()
                writer.write(flowToString(flow!!))
                writer.newLine()
                writer.flush()
                writer.close()
                val param = stringWriter.toString()
                param
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override suspend fun start(): Pair<Boolean, String?> {
            postProgress(getString(R.string.ver_param_downloading))
            device.startDataMonitor(dataListener)
            device.getParameters()
            var count = 80
            withContext(Dispatchers.Default) {
                while (progress == 0 && count > 0) {
                    delay(100)
                    count--
                }
            }
            device.stopDataMonitor(dataListener)
            return if (count > 0) {
                val param = getParams()
                if (param != null) {
                    saveParamToServer(param)
                } else false to getString(R.string.ver_param_download_fail)
            } else false to getString(R.string.ver_param_download_fail)
        }
    }

    class UploadParamTask(val device: DroneObject, val file: File, val param: String) : WaitTask() {
        private val listener = { done: Boolean -> resume(if (done) 1 else -1) }

        private fun getFileReader(): BufferedReader {
            return BufferedReader(FileReader(file))
        }

        private fun getStringReader(): BufferedReader {
            return BufferedReader(StringReader(param))
        }

        override fun before(): Pair<Boolean, String?> {
            val aptype: List<IProtocol.IndexedParam>?
            val pid: List<IProtocol.IndexedParam>?
            val flow: VKAg.FlowData?
            try {
                val reader = BufferedReader(getStringReader())
                var text = reader.readLine()
                aptype = parseIndexedParams(text)
                text = reader.readLine()
                pid = parseIndexedParams(text)
                text = reader.readLine()
                flow = parseFlowData(text)
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return false to getString(R.string.ver_param_file_read_fail)
            }

            if (aptype == null || pid == null || flow == null) {
                return false to getString(R.string.ver_param_file_read_fail)
            }

            postProgress(getString(R.string.ver_param_uploading))
            device.startMonitorCommand(listener)
            device.setBumpParam(flow.qty, flow.param)
            uploadAPType(aptype)
            uploadPid(pid)
            return true to null
        }

        override fun done(result: Int): Pair<Boolean, String?> {
            device.stopMonitorCommand(listener)
            return if (result > 0) true to getString(R.string.ver_param_upload_ok)
            else false to getString(R.string.ver_param_upload_fail)
        }

        private fun uploadPid(params: List<IProtocol.IndexedParam>) {
            for (p in params) {
                device.sendPidParameter(p.index, p.ivalue)
            }
        }

        private fun uploadAPType(params: List<IProtocol.IndexedParam>) {
            for (p in params) {
                device.sendIndexedParameter(p.index, p.ivalue)
            }
        }
    }

    /**
     * bitInt转数组索引
     */
    fun BitIntToArrayIndexes(bitInt: Int): List<Int> {
        val indexes = mutableListOf<Int>()
        val binaryStr = bitInt.toString(2).reversed()
        for ((index, bit) in binaryStr.withIndex()) {
            if (bit == '1') {
                indexes.add(2.0.pow(index.toDouble()).toInt())
            }
        }
        return indexes
    }

    /**
     * 数组索引转bitInt
     */
    fun ArrayIndexesToBitInt(indexes: List<Int>): Int {
        var binaryStr = "0"
        indexes.forEach {
            val binary = it.toString(2)
            binaryStr = addBinary(binaryStr, binary)
        }
        return binaryStr.toInt(2)
    }

    private fun addBinary(a: String, b: String): String {
        val maxLength = maxOf(a.length, b.length)
        val result = StringBuilder()
        var carry = 0

        // 从右向左遍历
        for (i in 0 until maxLength) {
            var sum = carry
            if (i < a.length) sum += a[a.length - 1 - i] - '0'
            if (i < b.length) sum += b[b.length - 1 - i] - '0'

            // 计算当前位的结果
            result.append(sum % 2)
            // 计算进位
            carry = sum / 2
        }

        // 如果最后还有进位，将其添加到结果的最前面
        if (carry > 0) {
            result.append(carry)
        }

        // 由于我们是从最低位开始计算的，所以需要将结果反转
        return result.reversed().toString()
    }
}