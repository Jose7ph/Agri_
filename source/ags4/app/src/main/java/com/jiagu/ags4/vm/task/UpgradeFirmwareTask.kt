package com.jiagu.ags4.vm.task

import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.logToFile
import com.jiagu.ags4.vm.UpgradeHelper
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.IUpgradable
import com.jiagu.device.task.FirmwareTask
import com.jiagu.tools.vm.DownloadTask

open class UpgradeFirmwareTask(
    val device: IUpgradable,
    val type: String,
    private val localVer: Int,
    val fromNet: Boolean,
    val path: String = ""
) : ProgressTask() {

    protected open fun preparingTask(path: String): ProgressTask? = null
    protected open fun finishTask(): ProgressTask? = null

    override suspend fun start(): Pair<Boolean, String?> {
        val nameId = when (type) {
            "fcu" -> R.string.ver_mc_name
            "imu" -> R.string.ver_imu_name
            "radar-mb0", "radar-mb1" -> R.string.ver_radar_name
            "fradar-mb0", "fradar-mb1" -> R.string.ver_fradar_name
            "bradar-mb0", "bradar-mb1" -> R.string.ver_bradar_name
            "bs" -> R.string.ver_bs_name
            "fmu-v9" -> R.string.ver_mc_name
            "pmu-v9" -> R.string.ver_pmu_name
            "tradar","radar-v9-mb0", "radar-v9-mb1", "ez-radar-t-87", "ez-radar-t-07" -> R.string.ver_radar_name
            "fradar","fradar-v9-mb0", "fradar-v9-mb1", "ez-radar-f-87", "ez-radar-f-07" -> R.string.ver_fradar_name
            "bradar","bradar-v9-mb0", "bradar-v9-mb1", "ez-radar-b-87", "ez-radar-b-07" -> R.string.ver_bradar_name
            "seed" -> R.string.ver_seed_name
            "weight" -> R.string.device_management_weight
            "gpsa" -> R.string.device_management_gnss
            "gpsb" -> R.string.device_management_gnss
            "vrt24-s1" -> R.string.ver_radar_name
            "vrt24-w1" -> R.string.ver_fradar_name
            "gnss_v2" -> R.string.device_management_gnss
            "gnss_v3" -> R.string.device_management_gnss
            else -> return false to null
        }
        val componentName = getString(nameId)
        val binFile = if (fromNet) {
            postProgress(getString(R.string.checking))
            val fname = findFirmwareName(type) ?: return false to null
            val (code, version) = UpgradeHelper.checkFirmware(type, localVer)
            if (code <= 0) {
                return true to getString(R.string.ver_no_new_ver)
            }
            if (postNotice(getString(R.string.ver_found_new_ver), version!!.content) != 1) {
                return true to null
            }
            val (r, file) = startChild(
                DownloadTask(
                    version.url,
                    context.getString(R.string.ver_newver_name),
                    null,
                    "${fname}.bin"
                )
            )
            if (!r || file == null || !file.endsWith("bin")) {
                return false to null
            }
            file
        } else {
            path
        }
        val notice = if (fromNet) getString(R.string.ver_ask_upgrade, componentName)
        else getString(R.string.ver_ask_upgrade2, componentName)
        if (postNotice(notice, null) == 1) {
            val injected = preparingTask(binFile)
            if (injected != null) {
                val (r, e) = startChild(injected)
                if (!r) {
                    return false to e
                }
            }
            val (r, e) = startChild(FirmwareTask(device, componentName, binFile))
            if (!r) return false to e
            val finished = finishTask() ?: return true to null
            return startChild(finished)
        }

        return true to null
    }

    private fun findFirmwareName(type: String): String? {
        val t = firmwareName(type)
        return if (t == null) null
        else "${AgsUser.firmPrefix.uppercase()}_${firmwareName(type)}"
    }

    private fun firmwareName(type: String): String? {
//        Log.v("shero", "firmwareName $type")
        logToFile("firmwareName $type")
        return when (type) {
            "fcu" -> "V7_AG"
            "imu" -> "IMU_AG"
            "radar-mb0" -> "V7_RADAR_MB0"
            "radar-mb1" -> "V7_RADAR_MB1"
            "fradar-mb0" -> "V7_FRADAR_MB0"
            "fradar-mb1" -> "V7_FRADAR_MB1"
            "bradar-mb0" -> "V7_BRADAR_MB0"
            "bradar-mb1" -> "V7_BRADAR_MB1"
            "bs" -> "RTKBS"
            "fmu-v9" -> "V9_AG_FMU"
            "pmu-v9" -> "V9_AG_PMU"
            "radar-v9-mb0" -> "V9_RADAR_MB0"
            "radar-v9-mb1" -> "V9_RADAR_MB1"
            "fradar-v9-mb0" -> "V9_FRADAR_MB0"
            "fradar-v9-mb1" -> "V9_FRADAR_MB1"
            "bradar-v9-mb0" -> "V9_BRADAR_MB0"
            "bradar-v9-mb1" -> "V9_BRADAR_MB1"
            "seed" -> "SEED"
            "weight" -> "VK_WEIGHT"
            "gpsa" -> "VK_GPSA"
            "gpsb" -> "VK_GPSB"
            "vrt24-s1" -> "VRT24_S1"
            "vrt24-w1" -> "VRT24_W1"
            "gnss_v2" -> "GNSS_V2"
            "gnss_v3" -> "GNSS_V3"
            else -> null
        }
    }

}