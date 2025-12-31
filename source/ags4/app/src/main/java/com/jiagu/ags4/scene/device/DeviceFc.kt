package com.jiagu.ags4.scene.device

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.checkAPP
import com.jiagu.ags4.utils.checkFcu
import com.jiagu.ags4.utils.checkPmu
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.PackageHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.counter.FloatChangeAskCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText

class DeviceFlightControl(
    val deviceName: String,
    val serialNumber: String? = null,
    val version: String?,
    val firmwareType: FirmwareTypeEnum? = null,
    val upgrade: Boolean = false,
)

@Composable
fun DeviceFc() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val deviceList by DroneModel.deviceList.observeAsState()
    val progressModel = LocalProgressModel.current
    val progress by progressModel.progress.observeAsState()
    val config = Config(context)
    var autoCheckUpgrade by remember { mutableStateOf(config.autoCheckUpgrade) }
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
            val processed = (context as DeviceManagementActivity).taskComplete?.invoke(
                result.success,
                result.msg
            ) ?: false
            if (!processed && result.msg != null) {
                if (result.msg!!.contains("apk")) context.installApk(
                    result.msg!!
                )
                else context.toast(result.msg!!)
            }
            context.hideDialog()
            progressModel.done()
        }
    }
    val group = filterDeviceByTypes(
        idListData = deviceList,
        filterNum = listOf(VKAgCmd.DEVINFO_PMU)
    )
    val deviceFcList = buildDeviceFcData(context, group)

    MainContent(
        title = stringResource(id = R.string.device_management_fc),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        val appDev = deviceFcList[0]
        var fmuDev: DeviceFlightControl? = null
        var pmuDev: DeviceFlightControl? = null
        if (deviceFcList.size == 3) {
            fmuDev = deviceFcList[1]
            pmuDev = deviceFcList[2]
        }
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //APP
                CardFrame(
                    modifier = Modifier,
                    title = appDev.deviceName,
                    showUpgradeLog = true,
                    firmwareType = appDev.firmwareType,
                    upgrade = appDev.upgrade,
                    showLocalUpgrade = false,
                    content = {
                        appDev.version?.let { ver ->
                            CardUpgradeTextRow(
                                title = stringResource(id = R.string.device_details_version),
                                text = ver,
                                upgrade = appDev.upgrade
                            )
                        }
                    },
                    customUpgrade = {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AutoScrollingText(
                                text = stringResource(R.string.check_updates) + ":",
                                modifier = Modifier.width(90.dp),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            SwitchButton(
                                width = 50.dp,
                                height = 25.dp,
                                defaultChecked = autoCheckUpgrade
                            ) {
                                autoCheckUpgrade = it
                                config.autoCheckUpgrade = it
                            }
                        }
                    }
                )
                //PMU
                pmuDev?.let { pmu ->
                    CardFrame(
                        modifier = Modifier.weight(1f),
                        title = pmu.deviceName,
                        firmwareType = pmu.firmwareType,
                        upgrade = pmu.upgrade,
                        content = {
                            pmu.serialNumber?.let { sn ->
                                CardUpgradeTextRow(
                                    title = stringResource(id = R.string.device_details_serial_number),
                                    text = sn,
                                    upgrade = false
                                )
                            }
                            pmu.version?.let { ver ->
                                CardUpgradeTextRow(
                                    title = stringResource(id = R.string.device_details_version),
                                    text = ver,
                                    upgrade = pmu.upgrade
                                )
                            }
                        }
                    )
                }
            }
            //FMU
            fmuDev?.let { fmu ->
                CardFrame(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    title = fmu.deviceName,
                    firmwareType = fmu.firmwareType,
                    upgrade = fmu.upgrade,
                    content = {
                        fmu.serialNumber?.let { sn ->
                            CardUpgradeTextRow(
                                title = stringResource(id = R.string.device_details_serial_number),
                                text = sn,
                                upgrade = false
                            )
                            DeviceConfig(context).rackNoMap?.get(fmu.serialNumber)?.let { rackNo ->
                                CardUpgradeTextRow(
                                    title = stringResource(id = R.string.rack_no),
                                    text = rackNo,
                                    upgrade = false
                                )
                            }
                        }

                        fmu.version?.let { ver ->
                            CardUpgradeTextRow(
                                title = stringResource(id = R.string.device_details_version),
                                text = ver,
                                upgrade = fmu.upgrade
                            )
                        }
                    },
                    afterContent = {
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                        //地磁校准
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.geomagnetic_calibration) + ":",
                                modifier = Modifier.width(100.dp),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                CardButton(text = stringResource(id = R.string.calibration)) {
                                    context.showDialog {
                                        GeomagneticCalibrationPopup(onDismiss = {
                                            context.hideDialog()
                                        })
                                    }

                                }
                            }
                        }
                        //磁偏角校准
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.geomagnetic_declination_calibration) + ":",
                                modifier = Modifier.width(100.dp),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            val number by remember {
                                mutableFloatStateOf(
                                    DroneModel.aptypeData.value?.getValue(VKAg.APTYPE_CIPIANJIAO)
                                        ?: 0f
                                )
                            }
                            FloatChangeAskCounter(
                                modifier = Modifier
                                    .height(30.dp)
                                    .weight(1f),
                                number = number,
                                min = -15f,
                                max = 15f,
                                step = 1f,
                                fraction = 0,
                                onConfirm = {
                                    DroneModel.activeDrone?.sendParameter(
                                        VKAg.APTYPE_CIPIANJIAO, it
                                    )
                                    context.toast(context.getString(R.string.calibration_success))
                                },
                                onDismiss = {})
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
private fun buildDeviceFcData(
    context: Context, devMap: MutableMap<Short, MutableList<VKAg.IDListData>>,
): List<DeviceFlightControl> {
    val deviceFlightControlList = mutableListOf<DeviceFlightControl>()
    var pmuSerialNumber: String? = null
    if (devMap.isNotEmpty()) {
        devMap.forEach { (_, v) ->
            if (v[0].devType == VKAgCmd.DEVINFO_PMU) {
                pmuSerialNumber = v[0].hwId
            }
        }
    }
    deviceFlightControlList.add(
        DeviceFlightControl(
            deviceName = context.getString(R.string.remote_control_app),
            version = PackageHelper.getAppVersionName(context).toString(),
            upgrade = context.checkAPP(),
        )
    )
    /*deviceFlightControlList.add(
        DeviceFlightControl(
            deviceName = context.getString(R.string.unmanned_drone_flight_control),
            serialNumber = "aaaaaaaaaaa",
            version = "aaaaaaaaaaa",
            firmwareType = FirmwareTypeEnum.MAIN_CTRL
        )
    )
    deviceFlightControlList.add(
        DeviceFlightControl(
            deviceName = context.getString(R.string.ver_pmu_name),
            serialNumber = "aaaaaaaaaaaaaaaaa",
            version = "aaaaaaaaaaa",
            firmwareType = FirmwareTypeEnum.PMU
        )
    )*/
    DroneModel.verData.value?.let {
        deviceFlightControlList.add(
            DeviceFlightControl(
                deviceName = context.getString(R.string.unmanned_drone_flight_control),
                serialNumber = it.serial,
                version = it.fwVer.toString(),
                upgrade = if (DroneModel.verData.value != null) checkFcu(it) else false,
                firmwareType = FirmwareTypeEnum.MAIN_CTRL
            )
        )
        deviceFlightControlList.add(
            DeviceFlightControl(
                deviceName = context.getString(R.string.ver_pmu_name),
                serialNumber = pmuSerialNumber,
                version = it.pmuVer.toString(),
                upgrade = if (DroneModel.verData.value != null) checkPmu(it) else false,
                firmwareType = FirmwareTypeEnum.PMU
            )
        )
    }
    return deviceFlightControlList
}

/**
 * 磁罗盘校准弹窗
 */
@Composable
private fun GeomagneticCalibrationPopup(
    onDismiss: () -> Unit,
) {
    val imuData by DroneModel.imuData.observeAsState()
    val context = LocalContext.current
    val calibrationTip1 = stringResource(id = R.string.magnetic_compass_calibration_tip1)
    val calibrationTip2 = stringResource(id = R.string.magnetic_compass_calibration_tip2)
    val calibrationTip3 = stringResource(id = R.string.magnetic_compass_calibration_tip3)

    val calibrationSuccessTip = stringResource(id = R.string.calibration_success)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)

    var calibrationTip by remember {
        mutableStateOf(calibrationTip1)
    }

    var step by remember {
        mutableIntStateOf(1)
    }
    var confirmText = R.string.confirm
    when (step) {
        1 -> {
            calibrationTip = calibrationTip1
            confirmText = R.string.start_calibration
        }

        2 -> {
            calibrationTip = calibrationTip2
        }

        3 -> {
            calibrationTip = calibrationTip3
        }
        //失败
        4 -> {
            calibrationTip = calibrationFailTip
            confirmText = R.string.retry
        }
        //成功
        5 -> {
            calibrationTip = calibrationSuccessTip
        }
    }

    imuData?.let {
        val voiceStr = when (it.alertReason.toInt()) {
            21 -> {
                step = 2
                context.getString(com.jiagu.v9sdk.R.string.voice_warn_magnet_horz)
            }

            22 -> {
                step = 3
                context.getString(com.jiagu.v9sdk.R.string.voice_warn_magnet_vert)
            }

            23 -> {
                step = 4
                context.getString(com.jiagu.v9sdk.R.string.voice_warn_magnet_fail)
            }

            82 -> {
                step = 5
                context.getString(com.jiagu.v9sdk.R.string.voice_magnetic_calib_finish)
            }

            else -> ""
        }
        VoiceMessage.emit(voiceStr)
    }

    ScreenPopup(
        width = 360.dp, confirmText = confirmText, content = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.geomagnetic_calibration),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(vertical = 20.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = calibrationTip,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = when (step) {
                        4 -> MaterialTheme.colorScheme.error
                        5 -> MaterialTheme.colorScheme.primary
                        else -> Color.Black
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = when (step) {
                        4, 5 -> TextAlign.Center
                        else -> TextAlign.Start
                    },
                )
                when (step) {
                    2 -> {
                        Image(
                            painter = painterResource(id = R.drawable.geomagnetic_calibration1),
                            contentDescription = "gc1",
                        )
                    }

                    3 -> {
                        Image(
                            painter = painterResource(id = R.drawable.geomagnetic_calibration2),
                            contentDescription = "gc2",
                        )
                    }
                }
            }
        }
    }, onDismiss = {
        onDismiss()
    }, onConfirm = {
        when (step) {
            1 -> {
                DroneModel.activeDrone?.calibMagnet()
            }
            //失败
            4 -> {
                step = 1

            }
            //成功
            5 -> {
                onDismiss()
            }
        }
    }, showConfirm = step != 2 && step != 3, showCancel = step != 5
    )
}