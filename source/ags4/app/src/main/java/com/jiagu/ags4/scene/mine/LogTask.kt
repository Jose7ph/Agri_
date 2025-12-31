package com.jiagu.ags4.scene.mine

import com.jiagu.api.ext.millisToDate
import com.jiagu.api.viewmodel.WaitTask
import com.jiagu.v9sdk.R
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKDevice
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

abstract class BaseLogTask(protected val device: VKDevice) : WaitTask() {
    private val progressListener = { p: Any ->//mcu 主控读取完日志后，再传给app(正在传输)
        when (p) {
            is IProtocol.ProgressData -> {
                val percent = p.progress.toFloat() / p.total * 100
//                Log.v("shero", "正在传输日志:$percent(${p.progress}/${p.total})")
//                LogFileHelper.log("正在传输日志:$percent(${p.progress}/${p.total})")
                if (percent <= 100f) postProgress(
                    getString(
                        R.string.log_downloading3,
                        percent.toInt()
                    ), true
                )
            }

            is IProtocol.Progress2Data -> {
                val percent = p.progress.toFloat() / p.total * 100
//                Log.v("shero", "正在读取日志:$percent(${p.progress}/${p.total})")
//                LogFileHelper.log("正在读取日志:$percent(${p.progress}/${p.total})")
                if (percent < 100f) postProgress(
                    getString(
                        R.string.log_downloading2,
                        percent.toInt()
                    ), true
                )
                else {
                    postProgress(getString(R.string.log_downloading2, percent.toInt()), true)
                }
            }

            is VKAg.LOGData -> recvLog(p)
        }
    }

    private fun recvLog(log: VKAg.LOGData) {
        try {
            val file = getFile(log)
            val output = FileOutputStream(file)
            log.data.forEach { output.write(it) }
//            Log.v("shero", "下载日志文件:${file.name}")
            output.close()
            resume(1)
        } catch (e: IOException) {
            e.printStackTrace()
            resume(2)
        }
    }

    override fun before(): Pair<Boolean, String?> {
        postProgress(getString(R.string.log_downloading), true)
        device.startDataMonitor(progressListener)
        startRead()
        return true to null
    }

    override fun done(result: Int): Pair<Boolean, String?> {
        device.stopTimeConsumingTask("read_log")
        device.stopDataMonitor(progressListener)
        return when (result) {
            -1 -> false to getString(R.string.log_cancel_read)
            2 -> false to null
            else -> true to null
        }
    }

    abstract fun startRead()
    abstract fun getFile(log: VKAg.LOGData): File
}

class LogTask(device: VKDevice, val all: Boolean, val folderName: String = "log") :
    BaseLogTask(device) {
    override fun startRead() {
        device.startTimeConsumingTask("read_log", !all)
    }

    override fun getFile(log: VKAg.LOGData): File {
        val fn = "${log.droneId}-${log.timestamp.millisToDate("yyyyMMddHHmm")}.dat"
        return File(context.getExternalFilesDir(folderName), fn)
    }
}

class LogV9Task(device: VKDevice, val index: Int, val fileName: String) : BaseLogTask(device) {
    override fun startRead() {
        device.startTimeConsumingTask("read_log", index)
    }

    override fun getFile(log: VKAg.LOGData): File {
        return if (index == 0) {
            val fn = "${log.droneId}-0-${log.timestamp.millisToDate("yyyyMMddHHmm")}.dat"
            File(context.getExternalFilesDir("log"), fn)
        } else File(context.getExternalFilesDir("log"), fileName)
    }
}