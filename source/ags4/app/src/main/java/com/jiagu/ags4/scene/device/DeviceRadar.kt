package com.jiagu.ags4.scene.device

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.card.CardFrameSwitchButtonRow
import com.jiagu.jgcompose.card.CardFrameTitleCounterRow
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup

class DeviceRadar(
    val deviceName: String,
    var serialNumber: String,
    var version: String,
    val firmwareType: FirmwareTypeEnum? = null,
    var upgrade: Boolean = false,
    var manufacturer: String,
)

@Composable
fun DeviceRadar() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val deviceList by DroneModel.deviceList.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val deviceTerrainData by DroneModel.deviceTerrainData.observeAsState()
    val deviceRadarData by DroneModel.deviceRadarData.observeAsState()
    val group = filterDeviceByTypes(
        idListData = deviceList,
        filterNum = listOf(VKAgCmd.DEVINFO_TERRAIN, VKAgCmd.DEVINFO_OBSTACLE)
    )
    val deviceRadarList = buildDeviceRadarData(context, group, deviceTerrainData, deviceRadarData)

    val progressModel = LocalProgressModel.current
    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = progress as ProgressModel.ProgressMessage
            context.showDialog {
                PromptPopup(
                    content = message.text,
                    showConfirm = false,
                    onConfirm = {},
                    onDismiss = {
                        progressModel.next(0)
                        context.hideDialog()
                    })
            }
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
            context.showDialog {
                PromptPopup(
                    content = notice.title,
                    onConfirm = { progressModel.next(1) },
                    onDismiss = {
                        progressModel.next(0)
                        context.hideDialog()
                    })
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = progress as ProgressModel.ProgressResult
            context.hideDialog()
            progressModel.done()
        }
    }
    //dm雷达显示盲区和能量，其余不显示
    val dmRadarManufacturerIds = listOf("26", "27", "28")

    MainContent(
        title = stringResource(id = R.string.device_management_radar),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        LazyVerticalGrid(
            modifier = Modifier,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(deviceRadarList.size) {
                val radar = deviceRadarList[it]
                CardFrame(
                    modifier = Modifier,
                    title = radar.deviceName + "(${radar.manufacturer})",
                    firmwareType = radar.firmwareType,
                    manufacturer = radar.manufacturer,
                    sn = radar.serialNumber,
                    version = radar.version,
                    showOnlineUpgrade = true,
                    upgrade = radar.upgrade,
                    content = {
                        CardUpgradeTextRow(
                            title = stringResource(id = R.string.device_details_serial_number),
                            text = radar.serialNumber,
                            upgrade = false,
                        )
                        CardUpgradeTextRow(
                            title = stringResource(id = R.string.device_details_version),
                            text = radar.version,
                            upgrade = false
                        )
                    },
                    afterContent = {
                        //仅dm雷达显示盲区和能量 && 不是后避障
                        if (dmRadarManufacturerIds.contains(radar.manufacturer) && radar.firmwareType != FirmwareTypeEnum.B_RADAR) {
                            var blindZone = 0f
                            var energy = 0f
                            if (radar.firmwareType == FirmwareTypeEnum.T_RADAR) {
                                blindZone =
                                    aptypeData?.getValue(VKAg.APTYPE_T_RADAR_BLIND_ZONE) ?: 0.3f
                                energy = aptypeData?.getValue(VKAg.APTYPE_T_RADAR_POWER) ?: 0f
                            } else {
                                blindZone =
                                    aptypeData?.getValue(VKAg.APTYPE_OA_RADAR_BLIND_ZONE) ?: 0.3f
                                energy = aptypeData?.getValue(VKAg.APTYPE_OA_RADAR_POWER) ?: 0f
                            }
                            //盲区
                            CardFrameTitleCounterRow(
                                title = stringResource(R.string.blind_zone_setting) + ":",
                                min = 0.3f,
                                max = 3f,
                                number = blindZone,
                                fraction = 1,
                                step = 0.1f,
                                scales = floatArrayOf(0.8f, 1f),
                                onValueChange = { v ->
                                    if (radar.firmwareType == FirmwareTypeEnum.T_RADAR) {
                                        sendParameter(VKAg.APTYPE_T_RADAR_BLIND_ZONE, v)
                                    } else sendParameter(VKAg.APTYPE_OA_RADAR_BLIND_ZONE, v)
                                }
                            )
                            //能量设置
                            val min = 2000f
                            val max = 20000f
                            CardFrameTitleCounterRow(
                                title = stringResource(R.string.energy_settings) + ":",
                                min = min,
                                max = max,
                                number = energy,
                                fraction = 0,
                                step = 1f,
                                scales = floatArrayOf(0.8f, 1f),
                                onValueChange = { v ->
                                    if (radar.firmwareType == FirmwareTypeEnum.T_RADAR) {
                                        sendParameter(VKAg.APTYPE_T_RADAR_POWER, v)
                                    } else {
                                        sendParameter(VKAg.APTYPE_OA_RADAR_POWER, v)
                                    }
                                }
                            )



                        }
                        if (dmRadarManufacturerIds.contains(radar.manufacturer) && radar.firmwareType == FirmwareTypeEnum.F_RADAR) {
                            // DM避障原始数据开关
                            CardFrameSwitchButtonRow(
                                title = stringResource(R.string.original_data_switch),
                                backgroundColors = listOf(
                                    Color.LightGray,
                                    MaterialTheme.colorScheme.primary
                                ),
                                defaultChecked = (aptypeData?.getValue(VKAg.APTYPE_DM_OA_RADAR_ORIGINAL_DATA_SWITCH) == 1f),
                            ) {
                                sendParameter(VKAg.APTYPE_DM_OA_RADAR_ORIGINAL_DATA_SWITCH, if (it) 1f else 0f)

                            }
                        }
                        if (dmRadarManufacturerIds.contains(radar.manufacturer) && radar.firmwareType == FirmwareTypeEnum.T_RADAR) {
                            // DM防地原始数据开关
                            CardFrameSwitchButtonRow(
                                title = stringResource(R.string.original_data_switch),
                                backgroundColors = listOf(
                                    Color.LightGray,
                                    MaterialTheme.colorScheme.primary
                                ),
                                defaultChecked = (aptypeData?.getValue(VKAg.APTYPE_DM_T_RADAR_ORIGINAL_DATA_SWITCH) == 1f),
                            ) {
                                sendParameter(VKAg.APTYPE_DM_T_RADAR_ORIGINAL_DATA_SWITCH, if (it) 1f else 0f)
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * 构建飞控数据
 */
fun buildDeviceRadarData(
    context: Context, devMap: MutableMap<Short, MutableList<VKAg.IDListData>>,
    terrainInfo: VKAg.TerrainInfo?,
    radarObstacleInfo: VKAg.RadarObstacleInfo?,
): List<DeviceRadar> {
    val deviceRadarList = mutableListOf(
         DeviceRadar(
            deviceName = context.getString(R.string.ver_fradar_name),
            serialNumber = "",
            version = "",
            firmwareType = FirmwareTypeEnum.F_RADAR,
            manufacturer = ""
        ), DeviceRadar(
            deviceName = context.getString(R.string.ver_bradar_name),
            serialNumber = "",
            version = "",
            firmwareType = FirmwareTypeEnum.B_RADAR,
            manufacturer = ""
        ), DeviceRadar(
            deviceName = context.getString(R.string.ground_simulation_radar),
            serialNumber = "",
            version = "",
            firmwareType = FirmwareTypeEnum.T_RADAR,
            manufacturer = ""
        )
    )
    if (devMap.isNotEmpty()) {
        for ((_, dev) in devMap) {
            when (dev[0].devType) {
                // 仿地
                VKAgCmd.DEVINFO_TERRAIN -> {
                    val radarCard = deviceRadarList.find { it.firmwareType == FirmwareTypeEnum.T_RADAR }
                    setRadarCardInfo(radarCard, dev[0].hwId, dev[0].swId, terrainInfo?.manufacture_id.toString())
                    if (terrainInfo?.manufacture_id?.toInt() == 26){
                        radarCard?.upgrade = true // DM仿地雷达需要升级
                    }
                }
                // 避障
                VKAgCmd.DEVINFO_OBSTACLE -> {
                    dev.forEachIndexed { index, data ->
                        val firmwareType = when (data.devNum.toInt()) {
                            1 -> FirmwareTypeEnum.F_RADAR
                            2 -> FirmwareTypeEnum.B_RADAR
                            else -> null
                        }
                        if (firmwareType != null) {
                            val radarCard = deviceRadarList.find { it.firmwareType == firmwareType }
                            setRadarCardInfo(radarCard, data.hwId, data.swId, radarObstacleInfo?.manufacture_id.toString())
                            if (radarObstacleInfo?.manufacture_id?.toInt() == 27 && firmwareType == FirmwareTypeEnum.F_RADAR) {
                                radarCard?.upgrade = true // DM仿地雷达需要升级
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
    return deviceRadarList
}

// 提取设置 DeviceRadar 属性的方法
fun setRadarCardInfo(radarCard: DeviceRadar?, hwId: String, swId: String, manufactureId: String?) {
    radarCard?.serialNumber = hwId
    radarCard?.version = swId
    radarCard?.manufacturer = manufactureId ?: ""
}