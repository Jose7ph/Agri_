package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.api.ext.formatFileSize
import com.jiagu.api.ext.millisToDate
import com.jiagu.api.ext.secondToDate
import com.jiagu.api.ext.toMillis
import com.jiagu.device.vkprotocol.VKAg
import java.io.File

val LocalLogModel = compositionLocalOf<LogModel> {
    error("No LocalLogModel provided")
}

class LogFile(
    val fileName: String,
    val fileSize: String,
    val displayName: String,
    val timestamp: Long
)

class Logging(var isCheck: Boolean, var fcIdx: Int, var downloaded: Boolean, var file: String)

class LogModel(app: Application) : AndroidViewModel(app) {

    var loading by mutableStateOf(false)
    var curPage by mutableStateOf(LOG_APP)
    private var refreshing = false
    var appLogs by mutableStateOf<List<LogFile>>(arrayListOf())
    var logList = mutableStateListOf<Logging>()

    var taskType by mutableStateOf(0)
    var taskProcess by mutableStateOf("")

    companion object {
        const val APP_LOG_PREFIX = "ags4-"

        const val NAME_LOG_APP = "applog"
        const val NAME_LOG_CRASH = "tombstones"
        const val NAME_LOG_CRASH2 = "tombstone_"

        const val LOG_APP = 0
        const val LOG_APP_CRASH = 1
        const val LOG_APP_FCU = 2
        private fun fileParser(f: String): Pair<Long, Int> {
            val v = f.split("-")
            var sortie = 0
            val time = if (v.size > 2) v[2].toLong() else v[1].toLong()
            if (v.size > 2) sortie = v[1].toInt()
            return time to sortie
        }

        private val fileComparator = Comparator<Logging> { f1, f2 ->
            val (t1, s1) = fileParser(f1.file)
            val (t2, s2) = fileParser(f2.file)
            if (t1 > t2) -1
            else if (t1 < t2) 1
            else s2 - s1
        }
    }

    fun getData() {
        when (curPage) {
            LOG_APP -> getLogs()
            LOG_APP_CRASH -> getCrashLogs()
            LOG_APP_FCU -> getAppFcuLogs()
        }
    }

    fun getLogs() {
        val dir = getApplication<Application>().getExternalFilesDir(NAME_LOG_APP)
        val out = mutableListOf<LogFile>()
        val prefix = APP_LOG_PREFIX
        val suffix = ".log"
        val list = dir?.list { _, str -> str.startsWith(prefix) && str.endsWith(suffix, true) }
        list?.forEach { fn ->
            val name = fn.substring(prefix.length, fn.length - suffix.length)
            val file = File(dir, fn)
            val length = file.length()
            val sizeFormat = formatFileSize(length)
            out.add(LogFile(fn, sizeFormat, name, name.toMillis("yyyyMMdd-HHmmss")))
        }
        out.sortByDescending { it.displayName }
//            _applog.postValue(out)
        appLogs = out
    }

    private fun getCrashLogs() {
        val dir = getApplication<Application>().getExternalFilesDir(NAME_LOG_CRASH)
        val out = mutableListOf<LogFile>()
        val prefix = NAME_LOG_CRASH2
        val suffix = ".xcrash"
        val list = dir?.list { _, str -> str.startsWith(prefix) && str.endsWith(suffix, true) }
        list?.forEach { fn ->
            val file = File(dir, fn)
            val length = file.length()
            val sizeFormat = formatFileSize(length)
            out.add(
                LogFile(
                    fn,
                    sizeFormat,
                    file.lastModified().millisToDate("yyyyMMdd-HHmmss"),
                    file.lastModified()
                )
            )
        }
        out.sortByDescending { it.displayName }
        appLogs = out
    }

    fun getAppFcuLogs() {
        if (loading) return
        DroneModel.activeDrone?.apply {
            loading = true
            startDataMonitor(logListener)
            getLogList()
        }
    }

    private fun stopLoading() {
        DroneModel.activeDrone?.stopDataMonitor(logListener)

    }

    private val logListener: (Any) -> Unit = {
        if (it is VKAg.LogListData) {
            if (it.sortieIndex == it.totalSorties) {
                stopLoading()
                refreshFcuLog(true)
            }
            addV9Log(it)
        }
    }

    fun refreshFcuLog(force: Boolean = false) {
        if (!force && refreshing) return
        refreshing = true
        val file = getApplication<Application>().getExternalFilesDir("log")
        file?.apply {
            if (exists()) {
                val ls = list { _, name -> name.endsWith(".dat", true) }
                ls?.forEach { fn ->
                    val name = fn.substring(0, fn.indexOfLast { it == '.' })
                    addLocalLog(Logging(false, -1, true, name))
                    logList.sortWith(fileComparator)
                }
            }
        }
    }

    private fun addLocalLog(item: Logging) {
        val log = logList.find { it.file == item.file }
        if (log == null) {
            logList.add(item)
            logList.sortWith(fileComparator)
        } else {
            log.downloaded = true
        }
    }

    private fun getV9FileName(log: VKAg.LogListData): String {
        return "${log.droneId}-${log.sortieId}-${(log.unlock_time).secondToDate("yyyyMMddHHmm")}"
    }

    fun addV9Log(item: VKAg.LogListData) {
        val dir = getApplication<Application>().getExternalFilesDir("log")
        val name = getV9FileName(item)
        val log = logList.find { it.file == name }
        if (log == null) {

            logList.add(Logging(false, item.sortieIndex, File(dir, "${name}.dat").exists(), name))
//            logList.sortWith(fileComparator)
        } else if (log.fcIdx < 0) {
            log.fcIdx = item.sortieIndex
        }
        Log.v("shero", "addV9Log: $name logList.size=${logList.size}")
    }

    // indies must be reversed
    fun remove(indies: List<Int>) {
        val dir = getApplication<Application>().getExternalFilesDir("log")
        for (idx in indies) {
            val log = logList[idx]
            if (log.downloaded) {
                File(dir, "${log.file}.dat").delete()
                if (log.fcIdx > 0) {
                    log.downloaded = false
                    log.isCheck = false
                } else {
                    logList.removeAt(idx)
                }
            }
        }
    }
}