package com.jiagu.ags4.vm

import android.content.Context
import android.util.Base64
import android.util.Log
import com.jiagu.api.helper.DigestHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.tools.http.FileDownloader
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

object WhiteList {
    enum class WhiteListType {
        RADAR, TERRAIN, BATTERY
    }
    private var salt = "ffzz123"
    private var radarList = setOf<String>()
    private var terrainList = setOf<String>()
    private var batteryList = setOf<String>()

    fun initWhiteList(context: Context) {
        val dir = context.getExternalFilesDir("whitelist")

        radarList = loadList(File(dir!!, "radar.gz"))
        terrainList = loadList(File(dir, "terrain.gz"))
        batteryList = loadList(File(dir, "battery.gz"))
        LogFileHelper.log("init batteryList: ${batteryList.size}")
//        radarList = downloadList(dir!!, 1, "radar", radarList)
//        terrainList = downloadList(dir, 2, "terrain", terrainList)
        DroneModel.verData.value?.let {
            LogFileHelper.log("init network")
            it.serial?.let { serial ->
                radarList = downloadList(dir, 1, "radar", radarList, serial)
                LogFileHelper.log("init network radarList: ${radarList.size}")
                terrainList = downloadList(dir, 2, "terrain", terrainList, serial)
                LogFileHelper.log("init network terrainList: ${terrainList.size}")
                batteryList = downloadList(dir, 3, "battery", batteryList, serial)
                LogFileHelper.log("init network batteryList: ${batteryList.size}")
            }
        }
    }

    private fun loadList(file: File): Set<String> {
        val out = mutableSetOf<String>()
        try {
            val gzip = GZIPInputStream(file.inputStream())
            val reader = BufferedReader(InputStreamReader(gzip))
            while (true) {
                val line = reader.readLine()
                val id = line.replace("\n", "")
                out.add(id)
            }
        } catch (e: Exception) {
            Log.v("lee", e.message.toString())
            Log.d("yuhang", "fail to load $file")
        }
        if (out.isEmpty()) {
            Log.d("yuhang", "empty list $file")
        }
        return out
    }

    private fun downloadList(
        dir: File,
        type: Int,
        name: String,
        default: Set<String>,
        droneId: String
    ):
            Set<String> {
        val file = File(dir, "$name.gz")
        val downloader = FileDownloader()
        if (downloader.downloadWithoutLength(
                "http://ag.jiagutech" +
                        ".com/api/device/getDeviceWhite?type=${type}&droneId=${droneId}",
                file
            )
        ) {
            return loadList(file)
        } else {
            LogFileHelper.log("cannot download fcu list")
            return default
        }
    }

    private fun calcDigest(id: String): String {
        val data = DigestHelper.md5sum_binary((id + salt).toByteArray())
        return Base64.encodeToString(data.slice(0..14).toByteArray(), Base64.NO_WRAP)
    }

    // return: -1 - invalid, 1 - valid, 0 - TBD
    private fun checkWhiteList(id: String, whiteList: Set<String>): Int {
        return if (whiteList.isEmpty()) 1
        else if (whiteList.contains(calcDigest(id))) 1
        else -1
    }

    fun needCheck(type: WhiteListType): Boolean {
        return when (type) {
            WhiteListType.RADAR -> radarList.isNotEmpty()
            WhiteListType.TERRAIN -> terrainList.isNotEmpty()
            WhiteListType.BATTERY -> batteryList.isNotEmpty()
        }
    }

    fun checkRadar(id: String) = checkWhiteList(id, radarList)
    fun checkTerrain(id: String) = checkWhiteList(id, terrainList)
    fun checkBattery(id: String) = checkWhiteList(id, batteryList)
}