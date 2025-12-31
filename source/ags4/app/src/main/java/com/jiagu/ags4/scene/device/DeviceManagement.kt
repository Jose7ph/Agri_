package com.jiagu.ags4.scene.device

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.scene.mine.LogManagementActivity
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.DeviceCardInfo
import com.jiagu.ags4.vm.DeviceModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocalDeviceModel
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.PackageHelper
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.card.Card
import com.jiagu.jgcompose.card.WarnTypeEnum
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper

const val EMPTY_TEXT = "————"

@Composable
fun DeviceManagement(finish: () -> Unit) {
    val navController = LocalNavController.current
    val deviceModel = LocalDeviceModel.current
    val deviceSeedModel = LocalDeviceSeederModel.current
    val context = LocalContext.current
    val deviceList by DroneModel.deviceList.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val batteryData by DroneModel.batteryData.observeAsState()
    val engineData by DroneModel.engineData.observeAsState()
    val controllerPn by DroneModel.controllerPn.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val deviceSeedData by DroneModel.deviceSeedData.observeAsState()
    val motorData by DroneModel.motorData.observeAsState()
    val controllerType by DroneModel.controllerType.observeAsState()
    val remoteIdData by DroneModel.remoteIdData.observeAsState()
    val rtkData by DroneModel.deviceRTKData.observeAsState()
    val canGALVData by DroneModel.canGALVInfo.observeAsState()
    val deviceCardList = buildDeviceCardList(
        context = context,
        devices = deviceList,
        imuData = imuData,
        batteryData = batteryData,
        engineData = engineData,
        controllerPn = controllerPn,
        hydrogenBatteryData1 = hydrogenBatteryData1,
        hydrogenBatteryData2 = hydrogenBatteryData2,
        deviceFlowData = deviceFlowData,
        deviceWeightData = deviceWeightData,
        deviceSeedData = deviceSeedData,
        motorData = motorData,
        remoteIdData = remoteIdData,
        rtkData = rtkData,
        controllerType = controllerType,
        canGALVData = canGALVData,
    )
    var warnDisplay by remember { mutableStateOf(false) }
    MainContent(title = stringResource(id = R.string.device_management), breakAction = {
        if (!navController.popBackStack()) finish()
    }, barAction = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //日志管理
            TopBarBottom(
                text = stringResource(id = R.string.mine_log_management),
                buttonColor = Color.White,
                textColor = Color.Black
            ) {
                (context as DeviceManagementActivity).startActivity(LogManagementActivity::class.java)
            }
            WarnButton(
                color = if (deviceModel.deviceWarns.isNotEmpty()) {
                    Color.Red
                } else {
                    Color.White
                }
            ) {
                warnDisplay = !warnDisplay
            }
        }
    }) {
        val warnTypeConvert = { warnType: Int ->
            when (warnType) {
                NewWarnTool.WARN_TYPE_ERROR -> WarnTypeEnum.WARN_TYPE_ERROR
                NewWarnTool.WARN_TYPE_WARN -> WarnTypeEnum.WARN_TYPE_WARN
                else -> WarnTypeEnum.WARN_TYPE_INFO
            }
        }
        LazyVerticalGrid(
            modifier = Modifier,
            contentPadding = PaddingValues(20.dp),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(deviceCardList.size) {
                val deviceCard = deviceCardList[it]
                val deviceName = stringResource(id = deviceCard.deviceType)
                Card(
                    modifier = Modifier
                        .width(240.dp)
                        .height(130.dp)
                        .shadow(
                            elevation = 16.dp, shape = MaterialTheme.shapes.medium
                        )
                        .clickable {
                            //remote id 不跳转页面 弹出dialog
                            if (deviceCard.target == CardTargetEnum.REMOTE_ID.target) {
                                context.showDialog {
                                    RemoteIdPopup(
                                        remoteId = remoteIdData?.remoteId ?: "",
                                        selfId = remoteIdData?.selfId ?: "",
                                        operatorId = remoteIdData?.operatorId ?: "",
                                        onDismiss = {
                                            context.hideDialog()
                                        },
                                        onConfirm = { type, id ->
                                            DroneModel.activeDrone?.setRemoteId(type, id)
                                        })
                                }
                            } else {
                                when (deviceCard.target) {
                                    CardTargetEnum.RC.target -> {
                                        DroneModel.activeDrone?.getChannelMapping()
                                    }

                                    CardTargetEnum.SEEDER.target -> {
                                        deviceSeedModel.initDroneParamList { }
                                    }
                                }
                                navController.navigate(deviceCard.target)
                            }
                        },
                    image = deviceCard.deviceImage,
                    title = deviceName,
                    content = deviceCard.content,
                    warnType = when (deviceCard.deviceCardType) {
                        VKAgCmd.DEVINFO_OBSTACLE, VKAgCmd.DEVINFO_TERRAIN -> {
                            warnTypeConvert(deviceModel.radarWarnType)
                        }

                        VKAgCmd.DEVINFO_FLOW -> {
                            warnTypeConvert(deviceModel.pumpWarnType)
                        }

                        VKAgCmd.DEVINFO_SEED -> {
                            warnTypeConvert(deviceModel.seedWarnType)
                        }

                        VKAgCmd.DEVINFO_MOTOR -> {
                            warnTypeConvert(deviceModel.motorWarnType)
                        }

                        VKAgCmd.DEVINFO_FCU -> {
                            warnTypeConvert(deviceModel.fcuWarnType)
                        }

                        VKAgCmd.DEVINFO_BATTERY -> {
                            warnTypeConvert(deviceModel.batteryWarnType)
                        }

                        else -> WarnTypeEnum.WARN_TYPE_INFO
                    }
                )
            }
        }
    }
    if (warnDisplay) {
        Box(
            modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd
        ) {
            DeviceWarningBox(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .padding(top = SIMPLE_BAR_HEIGHT),
                warnInfoList = deviceModel.deviceWarns,
                onClose = {
                    warnDisplay = false
                })
        }
    }
}

/**
 * 设备卡片处理
 */
fun buildDeviceCardList(
    context: Context,
    devices: List<VKAg.IDListData>?,
    imuData: VKAg.IMUData?,
    batteryData: VKAg.BatteryGroup?,
    engineData: VKAg.EngineData?,
    controllerPn: String?,
    controllerType: String?,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?,
    deviceFlowData: VKAg.EFTFlowData?,
    deviceWeightData: VKAg.EFTWeightData?,
    deviceSeedData: VKAg.EFTSeedData?,
    motorData: VKAg.MotorGroup?,
    remoteIdData: VKAg.RemoteIdInfo?,
    rtkData: VKAg.RTKInfo?,
    canGALVData: VKAg.CANGALVInfo?,
): List<DeviceCardInfo> {
    val deviceConfig = DeviceConfig(context)
    //默认显示卡片
    val cards = defaultDeviceCard(
        devices = devices,
        context = context,
        deviceConfig = deviceConfig,
        imuData = imuData,
        batteryData = batteryData,
        controllerType = controllerType,
        controllerPn = controllerPn,
        hydrogenBatteryData1 = hydrogenBatteryData1,
        hydrogenBatteryData2 = hydrogenBatteryData2,
        engineData = engineData,
        remoteIdData = remoteIdData,
        canGALVData = canGALVData,
        motorData = motorData,
        deviceWeightData = deviceWeightData,
        deviceSeedData = deviceSeedData,
    )
    //避障/仿地雷达卡片展示内容
    val radarCardContent = @Composable { dev: MutableList<VKAg.IDListData> ->
        when (dev[0].devType) {
            //仿地
            VKAgCmd.DEVINFO_TERRAIN -> {
                DeviceCardContent(
                    fraction = 0.6f, title = stringResource(
                        id = R.string.ver_radar_name
                    ) + ":", value = dev[0].swId
                )
            }
            //避障 可能多个
            VKAgCmd.DEVINFO_OBSTACLE -> {
                repeat(dev.size) {
                    val radarName = when (dev[it].devNum.toInt()) {
                        1 -> context.getString(R.string.ver_fradar_name)
                        2 -> context.getString(R.string.ver_bradar_name)
                        else -> ""
                    }
                    DeviceCardContent(
                        fraction = 0.6f, title = "$radarName:", value = dev[it].swId
                    )
                }
            }
        }
    }
    devices?.let {
        val group = filterDeviceByTypes(
            idListData = it
        )
        for ((_, devs) in group) {
            var radarCard: DeviceCardInfo?
            //如果有避障/仿地雷达 先构建卡片
            if (devs[0].devType == VKAgCmd.DEVINFO_OBSTACLE || devs[0].devType == VKAgCmd.DEVINFO_TERRAIN) {
                //判断是否已经添加过卡片 修改radarCard 的 content内容
                if (cards.any { card -> card.target == "device_radar" }) {
                    radarCard = cards.find { card -> card.target == "device_radar" }!!
                    val existsContent = radarCard.content
                    radarCard.content = {
                        existsContent()
                        radarCardContent(devs)
                    }
                }
                //没有雷达卡片先添加
                else {
                    radarCard = DeviceCardInfo(
                        deviceCardType = VKAgCmd.DEVINFO_OBSTACLE,
                        deviceType = R.string.device_management_radar,
                        deviceImage = R.drawable.default_radar,
                        target = CardTargetEnum.RADAR.target,
                        content = {
                            radarCardContent(devs)
                        })
                    cards.add(radarCard)
                }
            }
            //非雷达卡片处理
            else {
                buildDeviceCardInfo(
                    devs = devs,
                    imuData = imuData,
                    deviceFlowData = deviceFlowData,
                    deviceWeightData = deviceWeightData,
                    deviceSeedData = deviceSeedData,
                    motorData = motorData,
                    rtkData = rtkData,
                    remoteIdData = remoteIdData
                )?.let { c ->
                    if (cards.none { it.deviceCardType == c.deviceCardType }) {
                        cards.add(c)
                    }
                }
            }
        }
    }
    return cards
}

fun buildDeviceCardInfo(
    devs: List<VKAg.IDListData>,
    imuData: VKAg.IMUData?,
    deviceFlowData: VKAg.EFTFlowData?,
    deviceWeightData: VKAg.EFTWeightData?,
    deviceSeedData: VKAg.EFTSeedData?,
    motorData: VKAg.MotorGroup?,
    rtkData: VKAg.RTKInfo?,
    remoteIdData: VKAg.RemoteIdInfo?,
): DeviceCardInfo? {
    return when (devs[0].devType) {
//        VKAgCmd.DEVINFO_360RADAR -> DeviceCardInfo(
//            VKAgCmd.DEVINFO_OBSTACLE,
//            deviceType = R.string.device_management_radar,
//            deviceImage = R.drawable.default_radar,
//            serialNumber = dev[0].hwId,
//            version = dev[0].swId,
//            devs = dev,
//            target = "device_360radar"
//        )
        //线性水泵
//        VKAgCmd.DEVINFO_LINE_PUMP -> DeviceCardInfo(
//            deviceCardType = VKAgCmd.DEVINFO_LINE_PUMP,
//            deviceType = R.string.setting_line_pump,
//            deviceImage = R.drawable.default_device_sprayer,
//            target = CardTargetEnum.LINE_PUMP.target,
//            content = {
//                var totalFlowRateValue = EMPTY_TEXT
//                var sprayedAmountValue = EMPTY_TEXT
//                var medicineBoxLoadCapacityValue = EMPTY_TEXT
//                var levelGaugeStatusValue = EMPTY_TEXT
//
//                deviceFlowData?.let {
//                    totalFlowRateValue =
//                        UnitHelper.transCapacity(it.speed_flow1 / 1000f + it.speed_flow2 / 1000f)
//                }
//                imuData?.let {
//                    sprayedAmountValue = UnitHelper.transCapacity(it.YiYongYaoLiang)
//                    levelGaugeStatusValue = it.waterLevel.toString(1)
//                }
//                deviceWeightData?.let {
//                    medicineBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
//                }
//                DeviceCardContent(
//                    fraction = 0.6f, title = stringResource(
//                        R.string.total_flow_rate, UnitHelper.capacityUnit()
//                    ) + ":", value = totalFlowRateValue
//                )
//                DeviceCardContent(
//                    fraction = 0.6f, title = stringResource(
//                        R.string.sprayed_amount, UnitHelper.capacityUnit()
//                    ) + ":", value = sprayedAmountValue
//                )
//                DeviceCardContent(
//                    fraction = 0.75f, title = stringResource(
//                        R.string.medicine_box_load_capacity, UnitHelper.weightUnit()
//                    ) + ":", value = medicineBoxLoadCapacityValue
//                )
//                DeviceCardContent(
//                    fraction = 0.6f,
//                    title = stringResource(id = R.string.level_gauge_status) + ":",
//                    value = levelGaugeStatusValue
//                )
//            }
//        )
        //流量计
        VKAgCmd.DEVINFO_PUMP, VKAgCmd.DEVINFO_FLOW -> DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_FLOW,
            deviceType = R.string.device_management_sprayer,
            deviceImage = R.drawable.default_device_sprayer,
            target = CardTargetEnum.SPRAYER.target,
            content = {
                var totalFlowRateValue = EMPTY_TEXT
                var sprayedAmountValue = EMPTY_TEXT
                var medicineBoxLoadCapacityValue = EMPTY_TEXT
                var levelGaugeStatusValue = EMPTY_TEXT

                deviceFlowData?.let {
                    totalFlowRateValue =
                        UnitHelper.transCapacity(it.speed_flow1 / 1000f + it.speed_flow2 / 1000f)
                }
                imuData?.let {
                    sprayedAmountValue = UnitHelper.transCapacity(it.YiYongYaoLiang)
                    levelGaugeStatusValue = it.waterLevel.toString(1)
                }
                deviceWeightData?.let {
                    medicineBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
                }
                DeviceCardContent(
                    fraction = 0.6f, title = stringResource(
                        R.string.total_flow_rate, UnitHelper.capacityUnit()
                    ) + ":", value = totalFlowRateValue
                )
                DeviceCardContent(
                    fraction = 0.6f, title = stringResource(
                        R.string.sprayed_amount, UnitHelper.capacityUnit()
                    ) + ":", value = sprayedAmountValue
                )
                DeviceCardContent(
                    fraction = 0.75f, title = stringResource(
                        R.string.medicine_box_load_capacity, UnitHelper.weightUnit()
                    ) + ":", value = medicineBoxLoadCapacityValue
                )
                DeviceCardContent(
                    fraction = 0.6f,
                    title = stringResource(id = R.string.level_gauge_status) + ":",
                    value = levelGaugeStatusValue
                )
            })
        //播撒器
        VKAgCmd.DEVINFO_SEED -> DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_SEED,
            deviceType = R.string.device_management_seeder,
            deviceImage = R.drawable.default_device_seeder,
            target = CardTargetEnum.SEEDER.target,
            content = {
                var materialBoxLoadCapacityValue = EMPTY_TEXT
                var valveOpeningValue = EMPTY_TEXT
                var swingingSpeedValue = EMPTY_TEXT
                deviceWeightData?.let {
                    materialBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
                }
                deviceSeedData?.let {
                    valveOpeningValue = it.valve.toString()
                    swingingSpeedValue = it.speed.toString()
                }
                DeviceCardContent(
                    title = stringResource(
                        R.string.material_box_load_capacity, UnitHelper.weightUnit()
                    ) + ":", value = materialBoxLoadCapacityValue
                )
                DeviceCardContent(
                    title = stringResource(id = R.string.valve_opening), value = valveOpeningValue
                )
                DeviceCardContent(
                    title = stringResource(id = R.string.swinging_speed), value = swingingSpeedValue
                )
            })

        //称重传感器
        VKAgCmd.DEVINFO_WEIGHT -> DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_WEIGHT,
            deviceType = R.string.device_management_weight,
            deviceImage = R.drawable.default_device_weight,
            target = CardTargetEnum.WEIGHT.target,
            content = {
                var workModeShow = false
                if (deviceWeightData?.manufacture_id == VKAgCmd.DEVINFO_SEED_QIFEI.toInt()) {
                    workModeShow = true
                }
                var weight: VKAg.IDListData?
                devs.let {
                    weight = it[0]
                }
                DeviceCardContent(
                    title = stringResource(id = R.string.device_weight_manufactor) + ":",
                    value = deviceWeightData?.manufacture_name ?: ""
                )
                DeviceCardContent(
                    title = stringResource(id = R.string.device_management_firmware_version) + ":",
                    value = weight?.swId ?: ""
                )
                DeviceCardContent(
                    title = stringResource(
                        R.string.device_weight_weight, UnitHelper.weightUnit()
                    ) + ":",
                    value = if (deviceWeightData?.remain_weight != null) UnitHelper.transWeight(
                        deviceWeightData.remain_weight
                    ) else EMPTY_TEXT
                )
                DeviceCardContent(
                    fraction = 0.8f,
                    title = stringResource(
                        R.string.device_weight_weight_rate, UnitHelper.weightUnit()
                    ) + ":",
                    value = if (deviceWeightData?.ratio_weight != null) UnitHelper.transWeight(
                        deviceWeightData.ratio_weight
                    ) else EMPTY_TEXT
                )
                if (workModeShow) {
                    val content =
                        if (isSeedWorkType()) stringResource(id = R.string.device_weight_work_mode_seed) else stringResource(
                            id = R.string.device_weight_work_mode_spray
                        )
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_weight_work_mode) + ":",
                        value = content
                    )
                }
            })
        //GNSS
//        VKAgCmd.DEVINFO_GNSS -> DeviceCardInfo(
//            deviceCardType = VKAgCmd.DEVINFO_GNSS,
//            deviceType = R.string.device_management_gnss,
//            deviceImage = R.drawable.default_device_gnss,
//            target = CardTargetEnum.GNSS.target,
//            content = {
//                var gnssA: VKAg.IDListData? = null
//                var gnssB: VKAg.IDListData? = null
//                devs.let {
//                    gnssA = it[0]
//                    if (it.size > 1) {
//                        gnssB = it[1]
//                    }
//                }
//                //GNSS-A
//                gnssA?.let {
//                    //GNSS-A SN
//                    DeviceCardContent(
//                        fraction = 0.8f,
//                        title = "GNSS-A" + stringResource(R.string.device_details_serial_number),
//                        value = it.hwId
//                    )
//                    //GNSS-A version
//                    DeviceCardContent(
//                        fraction = 0.8f,
//                        title = "GNSS-A" + stringResource(R.string.device_details_version),
//                        value = it.swId
//                    )
//                }
//                //GNSS-B
//                gnssB?.let {
//                    //GNSS-B SN
//                    DeviceCardContent(
//                        fraction = 0.8f,
//                        title = "GNSS-B" + stringResource(R.string.device_details_serial_number),
//                        value = it.hwId
//                    )
//                    //GNSS-A version
//                    DeviceCardContent(
//                        fraction = 0.8f,
//                        title = "GNSS-B" + stringResource(R.string.device_details_version),
//                        value = it.swId
//                    )
//                }
//            })
        //RTK
        VKAgCmd.DEVINFO_RTK -> DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_RTK,
            deviceType = R.string.device_rtk,
            deviceImage = R.drawable.default_rtk,
            target = CardTargetEnum.RTK.target,
            content = {
                //设备状态
                DeviceCardContent(
                    fraction = 0.8f,
                    title = stringResource(R.string.device_status),
                    value = when (rtkData?.status?.toInt()) {
                        1 -> stringResource(R.string.normal)
                        2 -> stringResource(R.string.disconnected)
                        else -> EMPTY_TEXT
                    }
                )
                //定位类型
                DeviceCardContent(
                    fraction = 0.8f,
                    title = stringResource(R.string.position_type),
                    value = when (rtkData?.location_type?.toInt()) {
                        1 -> stringResource(R.string.loc_info_type_1)
                        2 -> stringResource(R.string.loc_info_type_5)
                        3 -> stringResource(R.string.loc_info_type_4)
                        else -> EMPTY_TEXT
                    }
                )
                //ANT1星数
                DeviceCardContent(
                    fraction = 0.8f,
                    title = "ANT1" + stringResource(R.string.star_count),
                    value = rtkData?.satellite_ant1?.toString() ?: EMPTY_TEXT
                )
                //ANT2星数
                DeviceCardContent(
                    fraction = 0.8f,
                    title = "ANT2" + stringResource(R.string.star_count),
                    value = rtkData?.satellite_ant2?.toString() ?: EMPTY_TEXT
                )
            })

        VKAgCmd.DEVINFO_REMOTE_ID -> DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_REMOTE_ID,
            deviceType = R.string.device_remote_id,
            deviceImage = R.drawable.default_deivce_remote_id,
            target = CardTargetEnum.REMOTE_ID.target,
            content = {
                DeviceCardContent(
                    title = stringResource(id = R.string.device_uas_id) + ":",
                    value = remoteIdData?.remoteId ?: ""
                )
                DeviceCardContent(
                    title = stringResource(id = R.string.device_self_id) + ":",
                    value = remoteIdData?.selfId ?: ""
                )
                DeviceCardContent(
                    title = stringResource(id = R.string.device_operator_id) + ":",
                    value = remoteIdData?.operatorId ?: ""
                )
            })

        else -> null
    }
}

@Composable
fun DeviceCardContent(fraction: Float = 0.5f, title: String, value: String) {
    DeviceCardContentRow(title = title, fraction = fraction, content = {
        AutoScrollingText(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = LocalContentColor.current
        )
    })
}


@Composable
fun DeviceCardContentRow(
    title: String, fraction: Float = 0.5f, content: @Composable () -> Unit,
) {
    val textSpacer = 4.dp
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AutoScrollingText(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.fillMaxWidth(fraction),
            textAlign = TextAlign.Start,
            color = LocalContentColor.current,
        )
        Spacer(modifier = Modifier.width(textSpacer))
        content()
    }
}

/**
 * Default device card
 */
fun defaultDeviceCard(
    devices: List<VKAg.IDListData>?,
    context: Context,
    deviceConfig: DeviceConfig,
    imuData: VKAg.IMUData?,
    controllerPn: String?,
    controllerType: String?,
    batteryData: VKAg.BatteryGroup?,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?,
    engineData: VKAg.EngineData?,
    remoteIdData: VKAg.RemoteIdInfo?,
    canGALVData: VKAg.CANGALVInfo?,
    motorData: VKAg.MotorGroup?,
    deviceWeightData: VKAg.EFTWeightData?,
    deviceSeedData: VKAg.EFTSeedData?,
): MutableList<DeviceCardInfo> {


    //默认显示卡片
    val list = mutableListOf(
        //遥控器
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_RC,
            deviceType = R.string.device_management_rc,
            deviceImage = R.drawable.default_remote_control,
            target = CardTargetEnum.RC.target,
            content = {
                val rockerMode = DroneModel.rcModeIndex(controllerType ?: "")
                val rockerValue = when (rockerMode) {
                    0 -> stringResource(id = R.string.rocker_mode_hand_jp)
                    1 -> stringResource(id = R.string.rocker_mode_hand_us)
                    2 -> stringResource(id = R.string.rocker_mode_hand_cn)
                    else -> EMPTY_TEXT
                }
                //型号
                DeviceCardContent(
                    title = stringResource(id = R.string.model) + ":",
                    value = ControllerFactory.deviceModel
                )
                //摇杆模式
                DeviceCardContent(
                    title = stringResource(id = R.string.rocker_mode) + ":", value = rockerValue
                )
                //序列号
                DeviceCardContent(
                    title = stringResource(id = R.string.device_engine_serial_number) + ":",
                    value = controllerPn ?: EMPTY_TEXT,
                    fraction = 0.4f
                )
            }),
        //飞控
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_FCU,
            deviceType = R.string.device_management_fc,
            deviceImage = R.drawable.default_flight_control,
            target = CardTargetEnum.FC.target,
            content = {
                val appVersion = PackageHelper.getAppVersionName(context)
                var fcuSn = EMPTY_TEXT
                var fcuVersion = EMPTY_TEXT
                var pmuVersion = EMPTY_TEXT
                DroneModel.verData.value?.let {
                    fcuSn = it.serial
                    fcuVersion = it.fwVer.toString()
                    pmuVersion = it.pmuVer.toString()
                }
                //APP版本
                DeviceCardContent(
                    title = stringResource(id = R.string.device_management_app_version),
                    value = appVersion
                )
                //FCU序列号
                DeviceCardContent(
                    title = stringResource(id = R.string.device_management_fcu_serial_number),
                    value = fcuSn
                )
                //FCU版本号
                DeviceCardContent(
                    title = stringResource(id = R.string.device_management_fcu_version),
                    value = fcuVersion
                )
                //PMU版本号
                DeviceCardContent(
                    title = stringResource(id = R.string.device_management_pmu_version),
                    value = pmuVersion
                )
            }),
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_GNSS,
            deviceType = R.string.device_management_gnss,
            deviceImage = R.drawable.default_device_gnss,
            target = CardTargetEnum.GNSS.target,
            content = {
                var gnssA: VKAg.IDListData? = null
                var gnssB: VKAg.IDListData? = null
                devices?.let {
                    val gnssList =
                        filterDeviceByTypes(
                            idListData = devices,
                            filterNum = listOf(VKAgCmd.DEVINFO_GNSS)
                        )
                    gnssList[VKAgCmd.DEVINFO_GNSS]?.let { gnss ->
                        gnssA = gnss[0]
                        if (gnss.size > 1) {
                            gnssB = gnss[1]
                        }
                    }
                }


                //GNSS-A
                gnssA?.let {
                    //GNSS-A SN
                    DeviceCardContent(
                        fraction = 0.8f,
                        title = "GNSS-A" + stringResource(R.string.device_details_serial_number),
                        value = it.hwId
                    )
                    //GNSS-A version
                    DeviceCardContent(
                        fraction = 0.8f,
                        title = "GNSS-A" + stringResource(R.string.device_details_version),
                        value = it.swId
                    )
                }
                //GNSS-B
                gnssB?.let {
                    //GNSS-B SN
                    DeviceCardContent(
                        fraction = 0.8f,
                        title = "GNSS-B" + stringResource(R.string.device_details_serial_number),
                        value = it.hwId
                    )
                    //GNSS-A version
                    DeviceCardContent(
                        fraction = 0.8f,
                        title = "GNSS-B" + stringResource(R.string.device_details_version),
                        value = it.swId
                    )
                }
            }),
        //电池
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_BATTERY,
            deviceType = R.string.device_management_battery,
            deviceImage = R.drawable.default_device_battery,
            target = CardTargetEnum.BATTERY.target
        ) {
            val batteryGroup = filterDeviceByTypes(
                idListData = devices,
                filterNum = listOf(VKAgCmd.DEVINFO_BATTERY, VKAgCmd.DEVINFO_HYDROGEN_BATTERY)
            )
            val canCurrentList = filterDeviceByTypes(
                idListData = devices,
                filterNum = listOf(VKAgCmd.DEVINFO_CURRENT)
            )
            //智能电池
            if (imuData?.energyType == VKAg.TYPE_SMART_BATTERY) {
                val idData = batteryGroup[VKAgCmd.DEVINFO_BATTERY]
                if (batteryData != null) {
                    for (battery in batteryData.batteries) {
                        var batteryId = EMPTY_TEXT
                        idData?.forEach { id ->
                            if (battery.devNum == id.devNum) {
                                batteryId = id.hwId
                            }

                        }
                        //序列号
                        DeviceCardContent(
                            title = stringResource(id = R.string.device_engine_serial_number) + ":",
                            value = batteryId,
                        )
                        //厂家
                        DeviceCardContent(
                            title = stringResource(id = R.string.device_weight_manufactor) + ":",
                            value = battery.factoryName.toString()
                        )
                        //电压
                        DeviceCardContent(
                            title = stringResource(id = R.string.battery_voltage) + ":",
                            value = battery.voltage.toString(1)
                        )
                        //循环次数
                        DeviceCardContent(
                            title = stringResource(id = R.string.battery_number_of_cycles) + ":",
                            value = battery.cycle.toString()
                        )
                    }
                }
            } else if (imuData?.energyType == VKAg.TYPE_HYDROGEN_BATTERY) {//氢能源电池
                hydrogenBatteryData1?.let {
                    //厂家
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_weight_manufactor) + ":",
                        value = it.manufacturer.toString()
                    )
                    //电压
                    DeviceCardContent(
                        title = stringResource(id = R.string.battery_voltage) + ":",
                        value = it.batteryVoltage.toString(1)
                    )
                }
                hydrogenBatteryData2?.let {
                    //厂家
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_weight_manufactor) + ":",
                        value = it.manufacturer.toString()
                    )
                    //电压
                    DeviceCardContent(
                        title = stringResource(id = R.string.battery_voltage) + ":",
                        value = it.batteryVoltage.toString(1)
                    )
                }
            } else {//非智能电池
                //电压
                DeviceCardContent(
                    title = stringResource(id = R.string.battery_voltage) + ":",
                    value = imuData?.energy?.toString(1) ?: EMPTY_TEXT,
                )
                if (canCurrentList.isNotEmpty()) {//电流计
                    //电流
                    DeviceCardContent(
                        title = stringResource(id = R.string.battery_current) + ":",
                        value = canGALVData?.current?.toString(1) ?: EMPTY_TEXT,
                    )
                    //温度
                    DeviceCardContent(
                        title = stringResource(id = R.string.battery_temperature) + ":",
                        value = canGALVData?.temperature?.toString(1) ?: EMPTY_TEXT,
                    )
                }
            }
        },
        //动力系统
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_MOTOR,
            deviceType = R.string.device_management_dynamic_system,
            deviceImage = R.drawable.default_motor,
            target = CardTargetEnum.MOTOR.target,
            content = {
                val motorCount = motorData?.motors?.count { it.voltage.toInt() != 0 } ?: 0
                //电机数量
                DeviceCardContent(
                    title = stringResource(id = R.string.device_motor_count) + ":",
                    value = motorCount.toString()
                )
                //电压
                DeviceCardContent(
                    title = stringResource(id = R.string.battery_voltage) + ":",
                    value = if (motorCount == 0) EMPTY_TEXT else (motorData?.motors?.map {
                        it.voltage
                    }?.sum()?.div(motorCount)?.toString(1)) ?: "0"
                )
                //电流
                DeviceCardContent(
                    title = stringResource(id = R.string.battery_current) + ":",
                    value = if (motorCount == 0) EMPTY_TEXT
                    else (motorData?.motors?.map {
                        it.current
                    }?.sum()?.div(motorCount)?.toString(1)) ?: "0"
                )
                //时长
                DeviceCardContent(
                    title = stringResource(id = R.string.time) + "(min):",
                    value = if (motorCount == 0) EMPTY_TEXT else (motorData?.motors?.sumOf {
                        it.duration
                    }?.div(motorCount)).toString()
                )
            }),
        //摄像头/云台
        DeviceCardInfo(
            deviceCardType = DeviceModel.DeviceTypeExpand.DEVICE_CAMERA_GIMBAL.type,
            deviceType = R.string.device_camera_gimbal,
            deviceImage = R.drawable.default_device_camera,
            target = CardTargetEnum.CAMERA_GIMBAL.target
        ) {
            //类型
            DeviceCardContent(
                title = stringResource(id = R.string.device_engine_type) + ":",
                value = deviceConfig.rtspType
            )
            //地址
            AutoScrollingText(
                text = deviceConfig.rtspurl,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = LocalContentColor.current
            )
        },
        DeviceCardInfo(
            deviceCardType = VKAgCmd.DEVINFO_OBSTACLE,
            deviceType = R.string.device_management_radar,
            deviceImage = R.drawable.default_radar,
            target = CardTargetEnum.RADAR.target,
            content = {

            }),
        //打点器
        DeviceCardInfo(
            deviceCardType = DeviceModel.DeviceTypeExpand.DEVICE_LOCATOR.type,
            deviceType = R.string.locate_type_locator,
            deviceImage = R.drawable.default_lost,
            target = CardTargetEnum.LOCATOR.target,
            content = {}),
//        DeviceCardInfo(
//            deviceCardType = VKAgCmd.DEVINFO_SEED,
//            deviceType = R.string.device_management_seeder,
//            deviceImage = R.drawable.default_device_seeder,
//            target = CardTargetEnum.SEEDER.target,
//            content = {
//                var materialBoxLoadCapacityValue = EMPTY_TEXT
//                var valveOpeningValue = EMPTY_TEXT
//                var swingingSpeedValue = EMPTY_TEXT
//                deviceWeightData?.let {
//                    materialBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
//                }
//                deviceSeedData?.let {
//                    valveOpeningValue = it.valve.toString()
//                    swingingSpeedValue = it.speed.toString()
//                }
//                DeviceCardContent(
//                    title = stringResource(
//                        R.string.material_box_load_capacity, UnitHelper.weightUnit()
//                    ) + ":", value = materialBoxLoadCapacityValue
//                )
//                DeviceCardContent(
//                    title = stringResource(id = R.string.valve_opening), value = valveOpeningValue
//                )
//                DeviceCardContent(
//                    title = stringResource(id = R.string.swinging_speed), value = swingingSpeedValue
//                )
//            })
    )
    //remote id todo idList 没有 使用这里 但是需要添加条件
//    if (BuildConfig.REMOTE_ID) {
//        list.add(
//            DeviceCardInfo(deviceCardType = VKAgCmd.DEVINFO_REMOTE_ID,
//                deviceType = R.string.device_remote_id,
//                deviceImage = R.drawable.default_deivce_remote_id,
//                target = CardTargetEnum.REMOTE_ID.target,
//                content = {
////                    DeviceCardContent(
////                        title = stringResource(id = R.string.connection_status) + ":",
////                        value = when (remoteIdData?.connectState) {
////                            0 -> stringResource(id = R.string.main_device_disconnected)
////                            1 -> stringResource(id = R.string.main_device_connected)
////                            2 -> stringResource(id = R.string.lost)
////                            else -> ""
////                        }
////                    )
//                    DeviceCardContent(
//                        title = stringResource(id = R.string.device_uas_id) + ":",
//                        value = remoteIdData?.remoteId ?: ""
//                    )
//                    DeviceCardContent(
//                        title = stringResource(id = R.string.device_self_id) + ":",
//                        value = remoteIdData?.selfId ?: ""
//                    )
//                    DeviceCardContent(
//                        title = stringResource(id = R.string.device_operator_id) + ":",
//                        value = remoteIdData?.operatorId ?: ""
//                    )
//
//                })
//        )
//    }
    //debug卡片显示
    if (BuildConfig.DEBUG && DroneModel.droneConnectionState.value == null) {
        list.addAll(debugCard)
    }
    //如果时油电混发动机则单独添加 引擎卡片
    if (imuData?.energyType == VKAg.TYPE_ENGINE) {
        list.add(
            DeviceCardInfo(
                deviceCardType = VKAgCmd.DEVINFO_BATTERY,
                deviceType = R.string.device_management_engine,
                deviceImage = R.drawable.default_device_engine,
                target = CardTargetEnum.ENGINE.target,
                content = {
                    val engineTypes = stringArrayResource(id = R.array.engine_type)
                    val engineRunningStates = stringArrayResource(id = R.array.engine_running_state)
                    val engineTypeName = when (engineData?.type?.toInt()) {
                        1 -> engineTypes[0]
                        2 -> engineTypes[1]
                        3 -> engineTypes[2]
                        4 -> engineTypes[3]
                        else -> EMPTY_TEXT
                    }
                    val engineRunningStateName = when (engineData?.status?.toInt()) {
                        0 -> engineRunningStates[0]
                        1 -> engineRunningStates[1]
                        2 -> engineRunningStates[2]
                        3 -> engineRunningStates[3]
                        4 -> engineRunningStates[4]
                        else -> EMPTY_TEXT
                    }
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_engine_type) + ":",
                        value = engineTypeName
                    )
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_engine_brand) + ":",
                        value = engineData?.brand ?: EMPTY_TEXT
                    )
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_engine_serial_number) + ":",
                        value = engineData?.serial ?: EMPTY_TEXT
                    )
                    DeviceCardContent(
                        title = stringResource(id = R.string.device_engine_running_state) + ":",
                        value = engineRunningStateName
                    )
                })
        )
    }
    return list
}

val debugCard = listOf(

    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_FLOW,
        deviceType = R.string.device_management_sprayer,
        deviceImage = R.drawable.default_device_sprayer,
        target = CardTargetEnum.SPRAYER.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_SEED,
        deviceType = R.string.device_management_seeder,
        deviceImage = R.drawable.default_device_seeder,
        target = CardTargetEnum.SEEDER.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_WEIGHT,
        deviceType = R.string.device_management_weight,
        deviceImage = R.drawable.default_device_weight,
        target = CardTargetEnum.WEIGHT.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_BATTERY,
        deviceType = R.string.device_management_engine,
        deviceImage = R.drawable.default_device_engine,
        target = CardTargetEnum.ENGINE.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_GNSS,
        deviceType = R.string.device_management_gnss,
        deviceImage = R.drawable.default_device_gnss,
        target = CardTargetEnum.GNSS.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_REMOTE_ID,
        deviceType = R.string.device_uas_id,
        deviceImage = R.drawable.default_deivce_remote_id,
        target = CardTargetEnum.REMOTE_ID.target,
        content = {}),
    DeviceCardInfo(
        deviceCardType = VKAgCmd.DEVINFO_RTK,
        deviceType = R.string.device_rtk,
        deviceImage = R.drawable.default_rtk,
        target = CardTargetEnum.RTK.target,
        content = {}),
)

/**
 * 卡片对应页面enum
 *
 * @property target 对应页面tag
 */
enum class CardTargetEnum(val target: String) {
    MOTOR("device_motor"),
    RADAR("device_radar"),
    SPRAYER("device_sprayer"),
    SEEDER("device_seeder"),
    WEIGHT("device_weight"),
    RC("device_rc"),
    FC("device_fc"),
    BATTERY("device_battery"),
    CAMERA_GIMBAL("device_camera_gimbal"),
    ENGINE("device_engine"),
    GNSS("device_gnss"),
    REMOTE_ID("device_remote_id"),
    RTK("device_rtk"),
    LINE_PUMP("device_line_pump"),
    LOCATOR("device_locator")
}

/**
 * Remote id 弹窗
 */
@Composable
private fun RemoteIdPopup(
    remoteId: String,
    operatorId: String,
    selfId: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit,
) {
    val auth = AgsUser.userInfo?.canTuneAdvParam() ?: false

    ScreenPopup(
        width = 380.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (auth) {
                    RemoteIdRow(
                        title = stringResource(id = R.string.device_uas_id) + ":",
                        defaultId = remoteId,
                        type = 1,
                        onConfirm = onConfirm
                    )
                }
                RemoteIdRow(
                    title = stringResource(id = R.string.device_self_id) + ":",
                    defaultId = selfId,
                    type = 2,
                    onConfirm = onConfirm
                )
                RemoteIdRow(
                    title = stringResource(id = R.string.device_operator_id) + ":",
                    defaultId = operatorId,
                    type = 3,
                    onConfirm = onConfirm
                )
            }
        },
        onDismiss = onDismiss,
        showConfirm = false,
    )
}

@Composable
private fun RemoteIdRow(
    title: String, defaultId: String, type: Int, onConfirm: (Int, String) -> Unit,
) {
    var editState by remember {
        mutableStateOf(false)
    }
    var id by remember {
        mutableStateOf(defaultId)
    }
    val idLengthValid = id.length == 20
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier) {
            AutoScrollingText(
                text = title,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(100.dp)
            )
        }
        //编辑
        if (editState) {
            NormalTextField(
                text = id,
                onValueChange = {
                    id = it
                },
                modifier = Modifier.weight(1f),
                showClearIcon = false,
                borderColor = if (id.isEmpty() || idLengthValid) {
                    MaterialTheme.colorScheme.outline
                } else {
                    Color.Red
                }
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .clickable {
                        onConfirm(type, id)
                        editState = false
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "save rid",
                    tint = Color.White
                )
            }
        } else {//查看
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = id,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .clickable {
                        editState = true
                    }, contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "edit oid",
                    tint = Color.White
                )
            }
        }
    }
}