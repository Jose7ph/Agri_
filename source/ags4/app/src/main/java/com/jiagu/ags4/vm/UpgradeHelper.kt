package com.jiagu.ags4.vm

import android.content.Context
import android.util.Log
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.utils.logToFile
import com.jiagu.api.helper.PackageHelper

object UpgradeHelper {

    class FileVersion(val version: Any, val url: String, val content: String?)

    private fun makeFileVersion(map: Map<String, Any?>): FileVersion {
        val v = map["version"]
        val ver = when (v) {
            is Double -> v.toInt()
            is Int, is String -> v
            else -> 0
        }
        val url = map["url"] as String? ?: ""
        val content = map["content"] as String?
        return FileVersion(ver, url, content)
    }

    private suspend fun checkUpgradable(curVer: Int, file: String): Pair<Int, FileVersion?> {
        val (version, _) = AgsNet.upgrade(file)
        return if (version != null) {
            val fv = makeFileVersion(version)
            if (fv.version !is Int) -1 to null
            else (if (fv.version > curVer) 1 else 0) to fv
        } else {
            -1 to null
        }
    }

    suspend fun checkFirmware(type: String, curVer: Int): Pair<Int, FileVersion?> {
        val file = when (type) {
            "fcu" -> "firm.txt"
            "imu" -> "vk-imu.txt"
            "radar-mb0" -> "vk-radar-mb0.txt"
            "radar-mb1" -> "vk-radar-mb1.txt"
            "fradar-mb0" -> "vk-f-radar-mb0.txt"
            "fradar-mb1" -> "vk-f-radar-mb1.txt"
            "bradar-mb0" -> "vk-b-radar-mb0.txt"
            "bradar-mb1" -> "vk-b-radar-mb1.txt"
            "bs" -> "vk-bs.txt"
            "fmu-v9" -> "v9-fmu.txt"
            "pmu-v9" -> "v9-pmu.txt"
            "radar-v9-mb0" -> "vk-radar-v9-mb0.txt"
            "radar-v9-mb1" -> "vk-radar-v9-mb1.txt"
            "fradar-v9-mb0" -> "vk-f-radar-v9-mb0.txt"
            "fradar-v9-mb1" -> "vk-f-radar-v9-mb1.txt"
            "bradar-v9-mb0" -> "vk-b-radar-v9-mb0.txt"
            "bradar-v9-mb1" -> "vk-b-radar-v9-mb1.txt"
            "gpsa" -> "vk-gpsa.txt"
            "gpsb" -> "vk-gpsb.txt"
            "vrt24-w1" -> "vrt24-w1.txt"
            "vrt24-s1" -> "vrt24-s1.txt"
            "gnss_v2" -> "gnss-v2.txt"
            "gnss_v3" -> "gnss-v3.txt"
            else -> ""
        }
        val name = "${AgsUser.firmPrefix}-$file"
        Log.v("shero", "checkFirmware type: $type curVer: $curVer fileName: $name")
        logToFile("checkFirmware type: $type curVer: $curVer fileName: $name")
        return checkUpgradable(curVer, name)
    }

    private val APP_NAME = "ags4-${AgsUser.flavor}.txt"

    suspend fun checkAll(context: Context): Pair<Int, FileVersion?> {
        val (vers, _) = AgsNet.getAllFirm("${AgsUser.firmPrefix}-all-firm.txt")
        AgsUser.allFirm = vers
        val (verApp, _) = AgsNet.upgrade(APP_NAME)
        if (verApp != null) {
            val ver = makeFileVersion(verApp)
            val curVer = PackageHelper.getAppVersionCode(context)
            AgsUser.appVersion = ver.version as Int
            AgsUser.appChangeLog = ver.content ?: ""
            return (if (AgsUser.appVersion > curVer) 1 else 0) to ver
        }
        return -1 to null
    }
}