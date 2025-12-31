package com.jiagu.ags4.scene.device

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.factory.FactoryModel
import com.jiagu.ags4.scene.factory.LocalFactoryModel
import com.jiagu.ags4.utils.LocalBtDeviceModel
import com.jiagu.ags4.utils.V9Util.isGnssV3
import com.jiagu.ags4.utils.openBinFileManager
import com.jiagu.ags4.vm.DeviceModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocalDeviceModel
import com.jiagu.ags4.vm.LocalLocationModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.ags4.vm.task.UpgradeFirmwareTask
import com.jiagu.api.ext.toastLong
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.device.controller.Controller.Companion.CONNECTED
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceManagementActivity : BaseComponentActivity() {

    private val deviceModel: DeviceModel by viewModels()
    private val deviceSeederModel: DeviceSeederModel by viewModels()
    private val factoryModel: FactoryModel by viewModels()
    private val btDeviceModel: BtDeviceModel by viewModels()
    private val locationModel: LocationModel by viewModels()

    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            CompositionLocalProvider(
                LocalDeviceModel provides deviceModel,
                LocalDeviceSeederModel provides deviceSeederModel,
                LocalFactoryModel provides factoryModel,
                LocalBtDeviceModel provides btDeviceModel,
                LocalLocationModel provides locationModel
            ) {
                NavHost(navController = navController, startDestination = "device_management") {
                    //设备管理
                    composable("device_management") { DeviceManagement { finish() } }
                    //设备详情
                    composable("device_rc") { DeviceRc() }
                    composable("device_fc") { DeviceFc() }
                    composable("device_battery") { DeviceBattery() }
                    composable("device_motor") { DeviceMotor() }
                    composable("device_360radar") { Device360Radar() }
                    composable("device_radar") { DeviceRadar() }
                    composable("device_seeder") { DeviceSeeder() }
                    composable("device_sprayer") { DeviceSprayer() }
                    composable("device_weight") { DeviceWeight() }
                    composable("device_engine") { DeviceEngine() }
                    composable("device_gnss") { DeviceGNSS() }
                    composable("device_rtk") { DeviceRTK() }
                    composable("device_camera_gimbal") { DeviceCameraGimbal() }
                    composable("device_locator") { DeviceLocator() }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addObserver()
        DroneModel.readControllerParam()
        DroneModel.readControllerId()
        DroneModel.activeDrone?.getChannelMapping()
    }

    fun radarType(firmwareType: FirmwareTypeEnum, version: String, manufacturer: String): Int {
        when (firmwareType) {
            FirmwareTypeEnum.T_RADAR -> {
                if (version.startsWith("DS1O") && manufacturer == "26") {
                    return VKAgCmd.COMPONENT_RADAR
                } else if (version.startsWith("DS1N") && manufacturer == "26") {
                    return VKAgCmd.COMPONENT_FRADAR
                } else {
                    return VKAgCmd.COMPONENT_RADAR
                }
            }

            FirmwareTypeEnum.F_RADAR -> {
                if (version.startsWith("DW1O") && manufacturer == "27") {
                    return VKAgCmd.COMPONENT_RADAR
                } else if (version.startsWith("DW1N") && manufacturer == "27") {
                    return VKAgCmd.COMPONENT_FRADAR
                } else {
                    return VKAgCmd.COMPONENT_FRADAR
                }
            }

            FirmwareTypeEnum.B_RADAR -> return VKAgCmd.COMPONENT_BRADAR
            else -> return VKAgCmd.COMPONENT_RADAR
        }
    }

    /**
     * 固件升级
     * fromNet true 网络升级 false 本地升级
     */
    fun upgrade(
        firmwareType: FirmwareTypeEnum,
        fromNet: Boolean = true,
        manufacturer: String = "",
        sn: String = "",
        version: String = "",
    ) {
        val drone = DroneModel.activeDrone ?: return
//        val ver = DroneModel.verData.value

        val device = when (firmwareType) {
            FirmwareTypeEnum.MAIN_CTRL -> drone.getUpgradable(VKAgCmd.COMPONENT_MAIN_CTRL)
            FirmwareTypeEnum.PMU -> drone.getUpgradable(VKAgCmd.COMPONENT_PMU)
            FirmwareTypeEnum.T_RADAR, FirmwareTypeEnum.F_RADAR, FirmwareTypeEnum.B_RADAR ->
                drone.getUpgradable(radarType(firmwareType, version, manufacturer))

            FirmwareTypeEnum.SEED -> drone.getUpgradable(VKAgCmd.COMPONENT_SEED)
            FirmwareTypeEnum.WEIGHT -> drone.getUpgradable(VKAgCmd.COMPONENT_WEIGHT)
            FirmwareTypeEnum.GPS1 -> drone.getUpgradable(VKAgCmd.COMPONENT_GPS1)
            FirmwareTypeEnum.GPS2 -> drone.getUpgradable(VKAgCmd.COMPONENT_GPS2)
            else -> return
        }
        when (firmwareType) {
            FirmwareTypeEnum.MAIN_CTRL -> {
                if (fromNet) {
                    val task = UpgradeFirmwareTask(device, "fmu-v9", 0, true)
                    startProgress(task) { _, _ ->
                        DroneModel.activeDrone?.getVersionInfo()
                        false
                    }
                } else {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("FMU")) {
                            val task = UpgradeFirmwareTask(device, "fmu-v9", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }

            }

            FirmwareTypeEnum.PMU -> {
                if (fromNet) {
                    val task = UpgradeFirmwareTask(device, "pmu-v9", 0, true)
                    startProgress(task) { _, _ ->
                        DroneModel.activeDrone?.getVersionInfo()
                        false
                    }
                } else {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("PMU")) {
                            val task =
                                UpgradeFirmwareTask(device, "pmu-v9", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }
            }

            FirmwareTypeEnum.GPS1 -> {
                var sw = 0
                try {
                    sw = version.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                if (fromNet) {
                    if (sn.isEmpty()) {
                        showDialog {
                            ScreenPopup(content = {
                                Box(modifier = Modifier.padding(20.dp)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CardButton(text = "V2") {
                                            val task =
                                                UpgradeFirmwareTask(device, "gnss_v2", 0, true)
                                            startProgress(task) { _, _ ->
                                                DroneModel.activeDrone?.getVersionInfo()
                                                false
                                            }
                                            hideDialog()
                                        }
                                        CardButton(text = "V3") {
                                            val task =
                                                UpgradeFirmwareTask(device, "gnss_v3", 0, true)
                                            startProgress(task) { _, _ ->
                                                DroneModel.activeDrone?.getVersionInfo()
                                                false
                                            }
                                            hideDialog()
                                        }

                                    }
                                }
                            }, showCancel = true, showConfirm = false, onDismiss = {
                                hideDialog()
                            })
                        }
                    } else {
                        val task = UpgradeFirmwareTask(
                            device,
                            if (isGnssV3(sw)) "gnss_v3" else "gnss_v2",
                            0,
                            true
                        )
                        startProgress(task) { _, _ ->
                            DroneModel.activeDrone?.getVersionInfo()
                            false
                        }
                    }

                } else {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("GNSS_V3")) {
                            val task =
                                UpgradeFirmwareTask(device, "gnss_v3", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else if (it.exists() && it.name.contains("GNSS_V2")) {
                            val task =
                                UpgradeFirmwareTask(device, "gnss_v2", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }
            }

            FirmwareTypeEnum.GPS2 -> {
                var sw = 0
                try {
                    sw = version.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                if (fromNet) {
                    if (sn.isEmpty()) {
                        showDialog {
                            ScreenPopup(content = {
                                Box(modifier = Modifier.padding(20.dp)) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CardButton(text = "V2") {
                                            val task =
                                                UpgradeFirmwareTask(device, "gnss_v2", 0, true)
                                            startProgress(task) { _, _ ->
                                                DroneModel.activeDrone?.getVersionInfo()
                                                false
                                            }
                                            hideDialog()
                                        }
                                        CardButton(text = "V3") {
                                            val task =
                                                UpgradeFirmwareTask(device, "gnss_v3", 0, true)
                                            startProgress(task) { _, _ ->
                                                DroneModel.activeDrone?.getVersionInfo()
                                                false
                                            }
                                            hideDialog()
                                        }

                                    }
                                }
                            }, showCancel = true, showConfirm = false, onDismiss = {
                                hideDialog()
                            })
                        }
                    } else {
                        val task = UpgradeFirmwareTask(
                            device,
                            if (isGnssV3(sw)) "gnss_v3" else "gnss_v2",
                            0,
                            true
                        )
                        startProgress(task) { _, _ ->
                            DroneModel.activeDrone?.getVersionInfo()
                            false
                        }
                    }

                } else {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("GNSS_V3")) {
                            val task =
                                UpgradeFirmwareTask(device, "gnss_v3", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else if (it.exists() && it.name.contains("GNSS_V2")) {
                            val task =
                                UpgradeFirmwareTask(device, "gnss_v2", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }
            }

            FirmwareTypeEnum.T_RADAR -> {
                if (!fromNet) {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("T_RADAR")) {
                            val task =
                                UpgradeFirmwareTask(device, "tradar", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                } else if (version.startsWith("DS1") && manufacturer == "26") {
                    val task = UpgradeFirmwareTask(device, "vrt24-s1", 0, true)
                    startProgress(task) { _, _ ->
                        DroneModel.activeDrone?.getVersionInfo()
                        false
                    }
                }
            }

            FirmwareTypeEnum.F_RADAR -> {
                if (!fromNet) {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("F_RADAR")) {
                            val task =
                                UpgradeFirmwareTask(device, "fradar", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                } else if (version.startsWith("DW1") && manufacturer == "27") {
                    val task = UpgradeFirmwareTask(device, "vrt24-w1", 0, true)
                    startProgress(task) { _, _ ->
                        DroneModel.activeDrone?.getVersionInfo()
                        false
                    }
                }
            }

            FirmwareTypeEnum.B_RADAR -> {
                if (!fromNet) {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("B_RADAR")) {
                            val task =
                                UpgradeFirmwareTask(device, "bradar", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }
            }

            FirmwareTypeEnum.WEIGHT -> {
                if (!fromNet) {
                    openBinFileManager {
                        if (it.exists() && it.name.contains("WEIGHT")) {
                            val task =
                                UpgradeFirmwareTask(device, "weight", 0, false, it.absolutePath)
                            startProgress(task) { _, _ ->
//                                DroneModel.activeDrone?.getVersionInfo()
                                false
                            }
                        } else {
                            toastLong(getString(R.string.error_file_format))
                        }
                    }
                }
            }

            else -> {

            }
        }
    }

    var lastWarnCheckTime = System.currentTimeMillis()
    private fun addObserver() {
        DroneModel.newDevInfoData.observe(this) {
            lastWarnCheckTime = System.currentTimeMillis()
            it?.let {
                deviceModel.deviceWarns.clear()
                deviceModel.errorCards.clear()
                deviceModel.warnCards.clear()
                for (dev in it) {
                    val type = dev.value.getDevType(dev.key)[0]
                    var warnType = NewWarnTool.WARN_TYPE_CONN
                    dev.value.warns?.let {
                        it.forEach { warnData ->
                            if (warnData.warnType == NewWarnTool.WARN_TYPE_ERROR) {
                                warnType = NewWarnTool.WARN_TYPE_ERROR
                                return@let
                            }
                            if (warnData.warnType == NewWarnTool.WARN_TYPE_WARN) {
                                warnType = NewWarnTool.WARN_TYPE_WARN
                            }

                        }
                    }
                    if (dev.value.warns == null || dev.value.warns.isEmpty()) {
                        continue
                    }
                    when (type) {
                        VKAgCmd.DEVINFO_FCU, VKAgCmd.DEVINFO_WARN_IMU, VKAgCmd.DEVINFO_WARN_LIST -> {
                            if (warnType < deviceModel.fcuWarnType) {
                                deviceModel.fcuWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_BOOM -> {
                            if (warnType < deviceModel.fcuWarnType) {
                                deviceModel.fcuWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_BATTERY -> {
                            if (warnType < deviceModel.batteryWarnType) {
                                deviceModel.batteryWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_TERRAIN -> {
                            if (warnType < deviceModel.radarWarnType) {
                                deviceModel.radarWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_OBSTACLE -> {
                            if (warnType < deviceModel.radarWarnType) {
                                deviceModel.radarWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_SEED -> {
                            if (warnType < deviceModel.seedWarnType) {
                                deviceModel.seedWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_PUMP -> {
                            if (warnType < deviceModel.pumpWarnType) {
                                deviceModel.pumpWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_CENTRIFUGAL -> {
                            if (warnType < deviceModel.pumpWarnType) {
                                deviceModel.pumpWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_FLOW -> {
                            if (warnType < deviceModel.pumpWarnType) {
                                deviceModel.pumpWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_MOTOR -> {
                            if (warnType < deviceModel.motorWarnType) {
                                deviceModel.motorWarnType = warnType
                            }
                        }

                        VKAgCmd.DEVINFO_RTK -> {
                            if (warnType < deviceModel.rtkWarnType) {
                                deviceModel.rtkWarnType = warnType
                            }
                        }
                    }
                    deviceModel.deviceWarns.addAll(dev.value.warns)
                }
            }
        }
        var getControlMapping = true
        DroneModel.rcafData.observe(this) {
            getControlMapping = false
        }
        DroneModel.controllerConnectionState.observe(this) {
            if (ControllerFactory.deviceModel == "PHONE" && it == CONNECTED) {
                lifecycleScope.launch {
                    while (getControlMapping) {
                        DroneModel.activeDrone?.getChannelMapping()
//                        DroneModel.readControllerParam()
                        delay(1000)
                    }
                }
            }
        }
        // 新增定时检查任务 用于检查最后报警时间 超过2秒则当作没有报警处理
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(2000) // 每2秒检查一次
                    val currentTime = System.currentTimeMillis()
                    // 如果超过5秒没有新数据
                    if (currentTime - lastWarnCheckTime > 2000) {
                        withContext(Dispatchers.Main) {
                            deviceModel.deviceWarns.clear()
                            deviceModel.errorCards.clear()
                            deviceModel.warnCards.clear()
                            // 重置所有警告类型
                            deviceModel.fcuWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.batteryWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.radarWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.seedWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.pumpWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.motorWarnType = NewWarnTool.WARN_TYPE_DISC
                            deviceModel.rtkWarnType = NewWarnTool.WARN_TYPE_DISC
                        }
                    }
                }
            }
        }
    }
}

enum class FirmwareTypeEnum {
    MAIN_CTRL, //FMU
    PMU,
    IMU,
    T_RADAR,
    F_RADAR,
    B_RADAR,
    SEED,
    WEIGHT,
    GPS1,
    GPS2
}

