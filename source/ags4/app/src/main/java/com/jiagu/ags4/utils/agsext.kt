package com.jiagu.ags4.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BaseActivity
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.utils.V9Util.canGnssUpgrade
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.PackageHelper
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.controller.PhoneController
import com.jiagu.device.controller.PhoneVideo
import com.jiagu.device.vkprotocol.VKAg
import com.leon.lfilepickerlibrary.LFilePicker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

fun formatSecond(time: Long, showSecond: Boolean): String {
    val sb = StringBuilder()
    val hour = time / 3600
    val minute = time % 3600 / 60
    val second = time % 60
    if (hour > 0) sb.append("$hour:")
    if (hour > 0) sb.append(String.format("%02d", minute))
    else sb.append(minute.toString())
    if (showSecond) {
        sb.append(String.format(":%02d", second))
    }
    return sb.toString()
}

fun runTask(task: suspend () -> Unit) {
    GlobalScope.launch { task() }
}

fun logToFile(data: String) {
    LogFileHelper.log(data)
    Log.v("shero", data)
}

fun isSeedWorkType(): Boolean {
    return DroneModel.activeDrone?.isSeeder() == true
}

fun Context.checkAPP(): Boolean {
    val code = PackageHelper.getAppVersionCode(this)
    return AgsUser.appVersion > code
}

fun checkFcu(ver: VKAg.VERData?): Boolean {
    if (!V9Util.canUpgrade(DroneModel.verData.value?.serial)) {
        return false
    }
    return if (ver?.fwVer == null || !DroneModel.isV9) false else (AgsUser.allFirm?.fmu
        ?: 0) > (ver.fwVer)
}

fun checkPmu(ver: VKAg.VERData?): Boolean {
    if (!V9Util.canUpgrade(DroneModel.verData.value?.serial)) {
        return false
    }
    return if (ver?.pmuVer == null || !DroneModel.isV9) false else (AgsUser.allFirm?.pmu
        ?: 0) > (ver.pmuVer)
}

/*
40xx的是新GPS需要升级V3版本的，强制推送。
其他20xx，1xxx的是V2版本的不提示升级，
其他获取不了版本的不提示升级，客户如果要升级支持离线手动选择固件。
 */
fun checkGNSS(ver: String): Boolean {
    try {
        val sw = ver.toInt()
        if (canGnssUpgrade(sw)) {
            return true
        }
        return false
    }catch (e: NumberFormatException) {
        Log.e("shero", "checkGNSS ver toInt error: ${e.message}")
        return false
    }

}

private val pattern1 = """\d+(\.\d+)?%""".toRegex()
private val pattern2 = """\d+/\d+""".toRegex()
fun parsePercent(str: String): Float {
    val percent = pattern1.find(str)
    if (percent != null) {
        return percent.value.replace("%", "").toFloat() / 100
    }
    val fraction = pattern2.find(str)
    if (fraction != null) {
        val parts = fraction.value.split("/")
        return parts[0].toFloat() / parts[1].toFloat()
    }
    return 0f
}

fun BaseActivity.openFileManager(complete: (File) -> Unit) {
    val dir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
    LFilePicker().withActivity(this)
        .withRequestCode(1000)
        .withStartPath(dir.absolutePath)
        .withFileSize(2 * 1024 * 1024L)
        .withIsGreater(false)
        .withMutilyMode(false)
        .withFileFilter(arrayOf(".kml", ".kmz", ".csv"))
        .withMaxNum(1)
        .start()
    resultMap[1000] = { i ->
        i?.getStringArrayListExtra("paths")?.let {
            complete(File(it[0]))
        }
    }
}

fun BaseComponentActivity.openBinFileManager(complete: (File) -> Unit) {
    val dir = File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOWNLOADS)
    LFilePicker().withActivity(this)
        .withRequestCode(1000)
        .withStartPath(dir.absolutePath)
        .withFileSize(2 * 1024 * 1024L)
        .withIsGreater(false)
        .withMutilyMode(false)
        .withFileFilter(arrayOf(".bin"))
        .withMaxNum(1)
        .start()
    resultMap[1000] = { i ->
        i?.getStringArrayListExtra("paths")?.let {
            complete(File(it[0]))
        }
    }
}

fun ControllerFactory.registerPhone() {
    registerController("PHONE") { l, _ -> PhoneController(l) }
    registerVideo("PHONE") { c, _ ->
        PhoneVideo(c)
    }
}