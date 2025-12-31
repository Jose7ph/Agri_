package com.jiagu.ags4.scene.device

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_FLOW
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_FLOW_CLEAR_BG
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_FLOW_RESET
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.ui.components.ProgressBar
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.ParamAckTask
import com.jiagu.ags4.vm.task.WaterLinePumpCalibrationTask
import com.jiagu.ags4.vm.task.WaterLinePumpChartTask
import com.jiagu.ags4.vm.task.WaterLinePumpConfigTask
import com.jiagu.ags4.vm.task.WaterPumpCalibrationTask
import com.jiagu.api.ext.toString
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.chart.PumpLineChart
import com.jiagu.jgcompose.chart.PumpLineChartData
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.LeftIconTextField
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper
import com.jiagu.tools.utils.NumberUtil
import java.math.RoundingMode
import java.util.Locale

class WaterPump(
    var speed: String, var flow: String
)

class CentrifugalNozzle(
    val speed: String,
)

class LinePumpValue(val index: Int, val flow: Float, val rpm: Int, val pwm: Int, val kp: Float) {
    override fun toString(): String {
        return String.format(Locale.US, "index: %d, flow: %f, rpm: %d, pwm: %d, kp: %f", index, flow, rpm, pwm, kp)
    }
}
class LinePumpChart {
    var kp1: Float = 0f
    var kp2: Float = 0f
    var kp3: Float = 0f
    var kp4: Float = 0f
    var kp5: Float = 0f
    val data = mutableListOf<LinePumpValue>()
}


/**
 * 喷洒器
 */
@Composable
fun DeviceSprayer() {
    val navController = LocalNavController.current
    val progressModel = LocalProgressModel.current
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    val manufactureId = deviceFlowData?.manufacture_id ?: VKAgCmd.DEVINFO_FLOW_YELUN.toInt()

    MainContent(title = stringResource(id = R.string.device_management_sprayer),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { DeviceSprayerFlow() }
                item {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (deviceLinePumpData == null) DeviceSprayerPump()
                                deviceLinePumpData?.let { DeviceSprayerLinePump() }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) { DeviceSprayerCentrifugal() }
                    }
                }
            }
            VerticalDivider(
                thickness = 1.dp, modifier = Modifier, color = Color.Gray
            )
            LazyColumn(
                modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (deviceLinePumpData == null) {
                            when (manufactureId) {
                                VKAgCmd.DEVINFO_FLOW_YELUN.toInt() -> {
                                    YeLunFlowAction(progressModel = progressModel)
                                    false
                                }
                                VKAgCmd.DEV_FLOW_EZ_SINGLE.toInt(), VKAgCmd.DEV_FLOW_QF_SINGLE.toInt() -> {
                                    DeviceSprayerFlowAction(false)
                                    false
                                }
                                VKAgCmd.DEV_FLOW_EZ_DOUBLE.toInt(), VKAgCmd.DEV_FLOW_QF_DOUBLE.toInt() -> {
                                    DeviceSprayerFlowAction(true)
                                    true
                                }
                                else -> false
                            }
                            DeviceSprayerPumpAction()
                        }
                        deviceLinePumpData?.let {//线性水泵时，不装流量计
                            DeviceSprayerLinePumpAction(progressModel)
                        }

                        AutoScrollingText(
                            text = stringResource(R.string.dual_pump_flow_rate_synchronization_switch),
                            modifier = Modifier,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        SwitchButton(
                            defaultChecked = (aptypeData?.getValue(VKAg.APTYPE_DOUBLE_PUMP_FLOW_SYNC_SWITCH) == 1f),
                            backgroundColors = listOf(
                                Color.LightGray,
                                MaterialTheme.colorScheme.primary
                            ),
                        ) {
                            sendParameter(VKAg.APTYPE_DOUBLE_PUMP_FLOW_SYNC_SWITCH, if (it) 1f else 0f)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 水泵卡片
 */
@Composable
fun WaterPumpCard(
    modifier: Modifier = Modifier,
    devicePumpData: VKAg.EFTPumpData?,
    deviceFlowData: VKAg.EFTFlowData?,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = Color.Black
) {
    val waterPumpList = buildWaterPumpData(devicePumpData, deviceFlowData)
    val rowHeight = 30.dp
    val numberWidth = 40.dp
    Column(
        modifier = modifier.border(
            width = 1.dp,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary
        ), verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.water_pump_real_data),
                modifier = Modifier.fillMaxWidth(),
                style = style,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            Box(
                modifier = Modifier
                    .width(numberWidth)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_number),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_speed),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(
                        R.string.real_data_flow, UnitHelper.capacityUnit()
                    ), modifier = Modifier.fillMaxWidth(), style = style, color = textColor
                )
            }
        }
        for ((i, v) in waterPumpList.withIndex()) {
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
            ) {
                Box(
                    modifier = Modifier
                        .width(numberWidth)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = "${i + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
                VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = v.speed,
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
                VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = v.flow,
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * 构建水泵数据
 */
fun buildWaterPumpData(
    devicePumpData: VKAg.EFTPumpData?, deviceFlowData: VKAg.EFTFlowData?,
): List<WaterPump> {
    val waterPumpList = mutableListOf(
        WaterPump(
            speed = EMPTY_TEXT, flow = EMPTY_TEXT
        ), WaterPump(
            speed = EMPTY_TEXT, flow = EMPTY_TEXT
        )
    )
    if (devicePumpData == null && deviceFlowData == null) {
        return waterPumpList
    }
    waterPumpList.clear()
    devicePumpData?.let {
        waterPumpList.add(
            WaterPump(
                speed = it.p1_speed.toString(), flow = EMPTY_TEXT
            )
        )
        waterPumpList.add(
            WaterPump(
                speed = it.p2_speed.toString(), flow = EMPTY_TEXT
            )
        )
    }
    deviceFlowData?.let {
        //防止devicePumpData ==null 但 deviceFlowData != null 的情况
        if (waterPumpList.isEmpty()) {
            waterPumpList.add(
                WaterPump(
                    speed = EMPTY_TEXT, flow = UnitHelper.transCapacity((it.speed_flow1 / 1000f))
                )
            )
            waterPumpList.add(
                WaterPump(
                    speed = EMPTY_TEXT, flow = UnitHelper.transCapacity((it.speed_flow2 / 1000f))
                )
            )
        } else {
            waterPumpList[0].flow = (it.speed_flow1 / 1000f).toString()
            waterPumpList[1].flow = (it.speed_flow2 / 1000f).toString()
        }
    }
    return waterPumpList
}

/**
 * 离心喷头卡片
 */
@Composable
fun CentrifugalNozzleCard(
    modifier: Modifier = Modifier,
    deviceCentrifugalData: VKAg.EFTCentrifugalData?,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = Color.Black
) {
    val centrifugalNozzleList = buildCentrifugalNozzleData(deviceCentrifugalData)
    val rowHeight = 30.dp
    val numberWidth = 40.dp
    Column(
        modifier = modifier.border(
            width = 1.dp,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary
        ), verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.centrifugal_nozzle_real_data),
                modifier = Modifier.fillMaxWidth(),
                style = style,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            Box(
                modifier = Modifier
                    .width(numberWidth)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_number),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_speed),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
        }
        for ((i, v) in centrifugalNozzleList.withIndex()) {
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
            ) {
                Box(
                    modifier = Modifier
                        .width(numberWidth)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = "${i + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
                VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = v.speed,
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * 构建离心喷头数据
 */
fun buildCentrifugalNozzleData(deviceCentrifugalData: VKAg.EFTCentrifugalData?): List<CentrifugalNozzle> {
    val centrifugalNozzleList = mutableListOf(
        CentrifugalNozzle(
            speed = EMPTY_TEXT
        ), CentrifugalNozzle(
            speed = EMPTY_TEXT
        )
    )
    if (deviceCentrifugalData == null) {
        return centrifugalNozzleList
    }
    centrifugalNozzleList.clear()
    deviceCentrifugalData.let {
        centrifugalNozzleList.add(
            CentrifugalNozzle(
                speed = it.c1_speed.toString()
            )
        )
        centrifugalNozzleList.add(
            CentrifugalNozzle(
                speed = it.c2_speed.toString()
            )
        )
        centrifugalNozzleList.add(
            CentrifugalNozzle(
                speed = it.c3_speed.toString()
            )
        )
        centrifugalNozzleList.add(
            CentrifugalNozzle(
                speed = it.c4_speed.toString()
            )
        )
    }
    return centrifugalNozzleList
}

/**
 * 流量校准
 */
@Composable
fun FlowCalibrationPopup(
    progressModel: ProgressModel, double: Boolean, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val flowCalibrationTip1 = stringResource(id = R.string.flow_calibration_tip1)
    val flowCalibrationTip2 = stringResource(id = R.string.flow_calibration_tip2)
    val flowCalibrationTip3 = stringResource(id = R.string.flow_calibration_tip3) + "(ml)"
    val flowCalibrationHint = stringResource(id = R.string.flow_calibration_hint)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)

    var lText by remember { mutableStateOf("") }
    var rText by remember { mutableStateOf("") }
    val lInputValid = Validator.checkNumerical(lText) || lText.isBlank()
    val rInputValid = Validator.checkNumerical(rText) || rText.isBlank()

    var step by remember { mutableIntStateOf(1) }
    val calibrationTip = when (step) {
        1 -> flowCalibrationTip1
        2 -> flowCalibrationTip2
        3 -> flowCalibrationTip3
        4 -> inCalibrationTip
        5 -> calibrationFailTip
        6 -> calibrationSuccessTip
        else -> ""
    }
    val confirmText = when (step) {
        1 -> R.string.start
        2 -> R.string.stop
        5 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                6
            } else {
                5
            }
        }
    }

    val iconBoxWidth = 30.dp
    ScreenPopup(width = 360.dp, confirmText = confirmText, content = {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .width(330.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.flow_calibration),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )
                if (step == 3) {
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LeftIconTextField(
                        onValueChange = {
                            lText = it
                        },
                        text = lText,
                        modifier = Modifier
                            .width(200.dp)
                            .height(30.dp),
                        borderColor = if (lInputValid) Color.LightGray else MaterialTheme.colorScheme.error,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (lInputValid) Color.Black else MaterialTheme.colorScheme.error
                        ),
                        hint = flowCalibrationHint,
                        hintTextStyle = MaterialTheme.typography.labelMedium,
                        leftIcon = {
                            if (double) {
                                FlowCalibrationStartIcon(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(iconBoxWidth),
                                    iconText = "L"
                                )
                                VerticalDivider(
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxHeight(),
                                    color = Color.LightGray
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        showClearIcon = false
                    )
                    if (double) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LeftIconTextField(
                            onValueChange = {
                                rText = it
                            },
                            text = rText,
                            modifier = Modifier
                                .width(200.dp)
                                .height(30.dp),
                            borderColor = if (rInputValid) Color.LightGray else MaterialTheme.colorScheme.error,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (rInputValid) Color.Black else MaterialTheme.colorScheme.error
                            ),
                            hint = flowCalibrationHint,
                            hintTextStyle = MaterialTheme.typography.labelMedium,
                            leftIcon = {
                                FlowCalibrationStartIcon(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(iconBoxWidth),
                                    iconText = "R"
                                )
                                VerticalDivider(
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxHeight(),
                                    color = Color.LightGray
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            showClearIcon = false
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 26.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            color = if (step == 5) Color.Red else Color.Black,
                            textAlign = if (step in 4..6) TextAlign.Center else TextAlign.Start
                        )
                    }
                }
            }
        }
    }, onDismiss = {
        progressModel.done()
        onDismiss()
    }, onConfirm = {
        when (step) {
            1 -> {
                step = 2
                DroneModel.activeDrone?.apply { startCalibEFTFlow() }
            }

            2 -> {
                step = 3
                DroneModel.activeDrone?.apply { endCalibEFTFlow() }
            }

            3 -> {
                step = 4
                context.startProgress(
                    task = SprayerCalibrationTask(
                        type = ID_FLOW, params = intArrayOf(lText.toInt(), if (rText.isBlank()) 0 else rText.toInt())
                    ),
                )
            }

            5 -> {
                step = 1
                progressModel.done()
            }

            6 -> {
                step = 1
                progressModel.done()
                onDismiss()
            }
        }
    }, showConfirm = step != 4, showCancel = step != 4, confirmEnable = if (step == 3) {
        if (double) lInputValid && rInputValid && lText.isNotEmpty() && rText.isNotEmpty()
        else lInputValid && lText.isNotEmpty()
    } else {
        true
    }
    )
}

/**
 * 流量计重置
 */
@Composable
fun FlowmeterResetPopup(
    progressModel: ProgressModel, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val flowmeterResetTip = stringResource(id = R.string.flowmeter_reset_tip)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
    val calibrationSuccessTip = stringResource(id = R.string.flowmeter_reset_complete)

    var step by remember {
        mutableIntStateOf(1)
    }

    val calibrationTip = when (step) {
        1 -> flowmeterResetTip
        2 -> inCalibrationTip
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }
    val confirmText = when (step) {
        3 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                4
            } else {
                3
            }
        }
    }

    ScreenPopup(width = 360.dp, confirmText = confirmText, content = {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .width(330.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.flowmeter_reset),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = calibrationTip, style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 26.sp
                        ), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center
                    )
                }
            }
        }
    }, onDismiss = {
        progressModel.done()
        onDismiss()
    }, onConfirm = {
        when (step) {
            1 -> {
                step = 2
                context.startProgress(
                    task = SprayerCalibrationTask(
                        type = ID_FLOW_RESET
                    ),
                )
            }

            3 -> {
                step = 1
                progressModel.done()
            }

            4 -> {
                step = 1
                progressModel.done()
                onDismiss()
            }
        }

    }, showConfirm = step != 2, showCancel = step != 2
    )
}

/**
 * 流量计背景清零
 */
@Composable
fun FlowmeterBackgroundClearPopup(
    progressModel: ProgressModel, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val flowmeterClearTip = stringResource(id = R.string.flowmeter_background_clear_tip)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
    val calibrationSuccessTip = stringResource(id = R.string.flowmeter_reset_complete)

    var step by remember {
        mutableIntStateOf(1)
    }

    val calibrationTip = when (step) {
        1 -> flowmeterClearTip
        2 -> inCalibrationTip
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }
    val confirmText = when (step) {
        3 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                4
            } else {
                3
            }
        }
    }

    ScreenPopup(width = 360.dp, confirmText = confirmText, content = {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .width(330.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.flowmeter_background_clear),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = calibrationTip,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 26.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (step == 1) TextAlign.Start else TextAlign.Center
                    )
                }
            }
        }
    }, onDismiss = {
        progressModel.done()
        onDismiss()
    }, onConfirm = {
        when (step) {
            1 -> {
                step = 2
                context.startProgress(
                    task = SprayerCalibrationTask(
                        type = ID_FLOW_CLEAR_BG
                    ),
                )
            }

            3 -> {
                step = 1
            }

            4 -> {
                step = 1
                progressModel.done()
                onDismiss()
            }
        }
    }, showConfirm = step != 2, showCancel = step != 2
    )
}


/**
 * 水泵校准
 */
@Composable
fun WaterPumpCalibrationPopup(
    progressModel: ProgressModel, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val waterPumpCalibrationTip = stringResource(id = R.string.water_pump_calibration_tip)
    val inCalibration = stringResource(id = R.string.in_calibration)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)

    var step by remember {
        mutableIntStateOf(1)
    }
    val calibrationTip = when (step) {
        1 -> waterPumpCalibrationTip
        2 -> inCalibration
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }

    val confirmText = when (step) {
        1 -> R.string.start
        3 -> R.string.retry
        else -> R.string.confirm
    }

    var progressValue by remember {
        mutableFloatStateOf(0f)
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
            //进度条阶段
            if (step == 2) {
                progressValue = msg.text.toFloat()
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                4
            } else {
                3
            }
        }
    }


    ScreenPopup(
        width = 360.dp, confirmText = confirmText,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(330.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.water_pump_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        textAlign = TextAlign.Center
                    )
                    when (step) {
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.width(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 26.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start,
                                    )
                                }
                            }
                        }

                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                ProgressBar(
                                    progress = progressValue,
                                    progressHeight = 10.dp,
                                    progressWidth = 200.dp
                                )
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AutoScrollingText(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (step == 3) Color.Red else Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        onDismiss = {
            step = 1
            progressValue = 0f
            progressModel.done()
            onDismiss()
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    context.startProgress(
                        task = WaterPumpCalibrationTask(),
                    )
                }

                3 -> {
                    step = 1
                    progressValue = 0f
                    progressModel.done()
                }

                4 -> {
                    step = 1
                    progressValue = 0f
                    progressModel.done()
                    onDismiss()
                }
            }
        },
        showConfirm = step != 2,
        showCancel = step != 2,
    )
}

/**
 * 流量校准输入框前置图标
 */
@Composable
private fun FlowCalibrationStartIcon(
    modifier: Modifier = Modifier, iconText: String,
) {
    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .background(
                color = Color.White, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            ), contentAlignment = Alignment.Center
    ) {
        Text(
            text = iconText,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold
            ),
            color = Color.Black,
        )
    }
}

/**
 * 叶轮流量参数
 */
@Composable
fun YeLunFlowParamPopup(progressModel: ProgressModel, complete: () -> Unit) {
    val context = LocalContext.current
    var tankVolume by remember { mutableStateOf(DroneModel.flowData.value?.qty?.toString(1) ?: "") }
    var flowmeterCoefficient by remember { mutableStateOf(DroneModel.flowData.value?.param?.toString(1) ?: "") }

    val tankVolumeValid = Validator.checkNumerical(tankVolume) || tankVolume.isBlank()
    val flowmeterCoefficientValid =
        Validator.checkNumerical(flowmeterCoefficient) || flowmeterCoefficient.isBlank()

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            if (result.success) {
                context.toast(stringResource(id = R.string.success))
                context.hideDialog()
                complete()
            } else {
                context.toast(stringResource(id = R.string.fail))
            }
        }
    }

    ScreenPopup(
        width = 360.dp,
        content = {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 10.dp, horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.spray_stop_drug_flowmeter) + stringResource(
                            id = R.string.factory_settings_parameter
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.8f)) {
                            AutoScrollingText(text = stringResource(id = R.string.tank_volume) + ":")
                        }
                        NormalTextField(
                            modifier = Modifier.weight(1f),
                            text = tankVolume,
                            onValueChange = {
                                tankVolume = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            borderColor = if (tankVolumeValid) Color.Gray else MaterialTheme.colorScheme.error
                        )
                    }
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.8f)) {
                            AutoScrollingText(text = stringResource(id = R.string.flowmeter_coefficient) + ":")
                        }
                        NormalTextField(
                            modifier = Modifier.weight(1f),
                            text = flowmeterCoefficient,
                            onValueChange = {
                                flowmeterCoefficient = it
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            borderColor = if (flowmeterCoefficientValid) Color.Gray else MaterialTheme.colorScheme.error

                        )
                    }
                }
            }
        },
        onDismiss = {
            context.hideDialog()
            complete()
        },
        onConfirm = {
            DroneModel.activeDrone?.let {
                context.startProgress(ParamAckTask {
                    it.setBumpParam(
                        tankVolume.replace(",", ".").toFloat(),
                        flowmeterCoefficient.replace(",", ".").toFloat()
                    )
                })
            }
        },
        confirmEnable = tankVolumeValid && flowmeterCoefficientValid && tankVolume.isNotEmpty() && flowmeterCoefficient.isNotEmpty()

    )
}

@Composable
fun YeLunFlowAction(progressModel: ProgressModel) {
    val context = LocalContext.current
    DeviceDetailsCommonButton(text = stringResource(id = R.string.spray_stop_drug_flowmeter) + stringResource(id = R.string.factory_settings_parameter)) {
        context.showDialog {
            YeLunFlowParamPopup(progressModel = progressModel) {
                DroneModel.activeDrone?.getBumpParam()
            }
        }
    }
    DeviceDetailsCommonButton(text = stringResource(id = R.string.flow_calibration)) {
        context.showDialog {
            //启动校准
            YeLunFlowCalibrationPopup()
        }
    }
}

/**
 * 叶轮流量校准
 */
@Composable
fun YeLunFlowCalibrationPopup() {
    val context = LocalContext.current
    var tankVolume by remember { mutableStateOf(DroneModel.flowData.value?.qty?.toString(1) ?: "") }
    val tankVolumeValid = Validator.checkNumerical(tankVolume) || tankVolume.isBlank()
    var step by remember { mutableIntStateOf(1) }
    ScreenPopup(
        width = 360.dp,
        content = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.flow_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    if (step == 1) {
                        Text(
                            text = stringResource(id = R.string.bump_calib_detail),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, start = 4.dp),
                            textAlign = TextAlign.Start,
                        )
                        Row(
                            modifier = Modifier
                                .height(35.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(0.8f)) {
                                AutoScrollingText(text = stringResource(id = R.string.tank_volume) + ":")
                            }
                            NormalTextField(
                                modifier = Modifier.weight(1f),
                                text = tankVolume,
                                onValueChange = {
                                    tankVolume = it
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                borderColor = if (tankVolumeValid) Color.Gray else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (step == 2) {
                        Box(
                            modifier = Modifier
                                .heightIn(min = 60.dp)
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(text = stringResource(R.string.bump_calib_tip))
                        }
                    }
                }
            }
        },
        onDismiss = {
            DroneModel.activeDrone?.stopYeLunCalibFlowmeter(tankVolume.replace(",", ".").toFloat())
            context.hideDialog()
        },
        onConfirm = {
            when (step) {
                1 -> {
                    DroneModel.activeDrone?.calibYeLunFlowmeter(tankVolume.replace(",", ".").toFloat())
                    step = 2
                }
                2 -> {
                    DroneModel.activeDrone?.stopYeLunCalibFlowmeter(tankVolume.replace(",", ".").toFloat())
                    context.hideDialog()
                }
            }
        },
        showCancel = true,
        confirmEnable = tankVolumeValid && tankVolume.isNotEmpty()
    )
}
//流量计校准 流量计恢复出厂设置 流量计背景清零
@Composable
fun DeviceSprayerFlowAction(double: Boolean = true) {
    val context = LocalContext.current
    val progressModel = LocalProgressModel.current
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    DeviceDetailsCommonButton(text = stringResource(id = R.string.flow_calibration)) {
        context.showDialog {
            FlowCalibrationPopup(progressModel = progressModel,
                double = double,
                onDismiss = {
                    context.hideDialog()
                })
        }
    }
    DeviceDetailsCommonButton(
        text = stringResource(id = R.string.flowmeter_reset)
    ) {
        context.showDialog {
            FlowmeterResetPopup(progressModel = progressModel, onDismiss = {
                context.hideDialog()
            })
        }
    }
    //恩曌品牌显示背景清除按钮
    when (deviceFlowData?.manufacture_id) {
        VKAgCmd.DEV_FLOW_EZ_SINGLE.toInt(), VKAgCmd.DEV_FLOW_EZ_DOUBLE.toInt() -> {
            DeviceDetailsCommonButton(
                text = stringResource(id = R.string.flowmeter_background_clear)
            ) {
                context.showDialog {
                    FlowmeterBackgroundClearPopup(progressModel = progressModel,
                        onDismiss = {
                            context.hideDialog()
                        })
                }
            }
        }
    }
}

/**
 * 构建线性水泵数据
 */
fun buildWaterLinePumpData(
    devicePumpData: VKAg.LinePumpGroup?,
    deviceFlowData: VKAg.EFTFlowData?,
): List<WaterPump> {
    val size = 2
    val waterPumpList = mutableListOf<WaterPump>()
    for (i in 0 until size) {
        waterPumpList.add(WaterPump(speed = EMPTY_TEXT, flow = EMPTY_TEXT))
    }
    devicePumpData?.data?.let {
        if (it.size > size) {
            waterPumpList.clear()
            for (p in it) {
                waterPumpList.add(WaterPump(speed = p.rotate_speed.toString(), flow = EMPTY_TEXT))
            }
        } else {
            for (p in it) {
                waterPumpList[p.devNum - 1].speed = p.rotate_speed.toString()
            }
        }
    }
    deviceFlowData?.let {
        waterPumpList[0].flow = (it.speed_flow1 / 1000f).toString()
        waterPumpList[1].flow = (it.speed_flow2 / 1000f).toString()
    }
    return waterPumpList
}

/**
 * 线性水泵卡片
 */
@Composable
fun WaterLinePumpCard(
    modifier: Modifier = Modifier,
    devicePumpData: VKAg.LinePumpGroup?,
    deviceFlowData: VKAg.EFTFlowData?,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    textColor: Color = Color.Black
) {
    val waterPumpList = buildWaterLinePumpData(devicePumpData, deviceFlowData)
    val rowHeight = 30.dp
    val numberWidth = 40.dp
    Column(
        modifier = modifier.border(
            width = 1.dp,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary
        ), verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.water_line_pump_real_data),
                modifier = Modifier.fillMaxWidth(),
                style = style,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight)
        ) {
            Box(
                modifier = Modifier
                    .width(numberWidth)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_number),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.real_data_speed),
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = textColor
                )
            }
            VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(rowHeight),
                contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(
                        R.string.real_data_flow, UnitHelper.capacityUnit()
                    ), modifier = Modifier.fillMaxWidth(), style = style, color = textColor
                )
            }
        }
        for ((i, v) in waterPumpList.withIndex()) {
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.fillMaxWidth())
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
            ) {
                Box(
                    modifier = Modifier
                        .width(numberWidth)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = "${i + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
                VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = v.speed,
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
                VerticalDivider(thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(rowHeight),
                    contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(
                        text = v.flow,
                        modifier = Modifier.fillMaxWidth(),
                        style = style,
                        color = textColor
                    )
                }
            }
        }
    }
}

//线性水泵校准
@Composable
fun DeviceSprayerLinePumpAction(progressModel: ProgressModel) {
    val context = LocalContext.current
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    deviceLinePumpData?.data?.let {
        for (i in 0 until it.size) {
            DeviceDetailsCommonButton(
                text = "${stringResource(id = R.string.water_pump_calibration)}(${i+1})"
            ) {
                progressModel.done()
                context.showDialog {
                    WaterLinePumpCalibrationPopup(progressModel = progressModel,
                        index = i+1,
                        onDismiss = {
                            context.hideDialog()
                        })
                }
            }
            DeviceDetailsCommonButton(
                text = "${stringResource(id = R.string.view_curves)}(${i+1})"
            ) {
                progressModel.done()
                val task = WaterLinePumpChartTask(i+1)
                context.startProgress(task = task)
                context.showDialog {
                    WaterLinePumpChatPopup(progressModel) {
                        context.hideDialog()
                    }
                }
            }
        }

        DeviceDetailsCommonButton(
            text = stringResource(id = R.string.setting_line_pump_set)
        ) {
            progressModel.done()
            context.showDialog {
                WaterLinePumpConfigPopup(progressModel,
                    onDismiss = { context.hideDialog() }
                )
            }
        }
    }
}

/**
 * 线性水泵校准
 */
@Composable
fun WaterLinePumpCalibrationPopup(
    progressModel: ProgressModel, index: Int, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val waterPumpCalibrationTip = stringResource(id = R.string.water_line_pump_calibration_tip)
    val inCalibration = stringResource(id = R.string.in_calibration)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    var medicineBoxLoadCapacityValue by remember { mutableStateOf(EMPTY_TEXT) }
    deviceWeightData?.let { medicineBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight) }

    var step by remember { mutableIntStateOf(1) }
    val calibrationTip = when (step) {
        1 -> waterPumpCalibrationTip
        2 -> inCalibration
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }

    val confirmText = when (step) {
        1 -> R.string.start
        3 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            Log.v("shero", "校准 ${result.success} ${result.msg}")
            if (step > 1) {
                Log.d("zhy", "ProgressResult: success = ${result.success},msg = ${result.msg} ")
                progressModel.done()
                step = if (result.success) {
                    Log.d("zhy", "线性水泵校准任务结束:result = success, 跳转step = 4")
                    4
                } else {
                    Log.d("zhy", "线性水泵校准任务结束:result = fail, 跳转step = 3")
                    3
                }
            }
        }
    }


    ScreenPopup(
        width = 360.dp, confirmText = confirmText,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.water_line_pump_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        textAlign = TextAlign.Center
                    )
                    when (step) {
                        1 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.width(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            lineHeight = 26.sp
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Start,
                                    )
                                }
                            }
                        }

                        2 -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 60.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                ParameterDataCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = stringResource(
                                        R.string.medicine_box_load_capacity, UnitHelper.weightUnit()
                                    ), content = medicineBoxLoadCapacityValue
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 30.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AutoScrollingText(
                                        text = calibrationTip,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = if (step == 3) Color.Red else Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        onDismiss = {
            progressModel.cancel()
            progressModel.done()

            if (step == 2) {
                DroneModel.activeDrone?.apply {//2-结束校准
                    calibLinePump(2, pumpIndex = index)
                }
            }
            onDismiss()
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    context.startProgress(
                        task = WaterLinePumpCalibrationTask(index),
                    )
                }

                3 -> {
                    step = 1
                    progressModel.done()
                }

                4 -> {
                    progressModel.done()
                    onDismiss()
                }
            }
        },
        showConfirm = step != 2,
    )
}

/**
 * 线性水泵配置
 */
@Composable
fun WaterLinePumpConfigPopup(
    progressModel: ProgressModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val progress by progressModel.progress.observeAsState()
    var step by remember { mutableIntStateOf(1) }
    var configStr = "UNKOWN"
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = (progress as ProgressModel.ProgressMessage)
        }
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            step = 4
            result.msg?.let { configStr = getConfigStr(it, context) }
        }
    }
    val waitting = stringResource(R.string.waitting)
    val tip = stringResource(R.string.setting_line_pump_confirm)
    val curTip = when (step) {
        1 -> tip
        2 -> waitting
        3 -> configStr
        4 -> configStr
        else -> ""
    }
    val modifier = Modifier.fillMaxWidth()
    ScreenPopup(
        width = 300.dp, confirmText = R.string.confirm,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                when (step) {
                    1 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = modifier,
                                text = curTip,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }

                    2 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = modifier,
                                text = curTip,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = modifier,
                                text = curTip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (step == 3) Color.Red else Color.Black,
                            )
                        }
                    }
                }
            }
        },
        onDismiss = {
            progressModel.cancel()
            progressModel.done()
            onDismiss()
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    context.startProgress(task = WaterLinePumpConfigTask(),)
                }

                3 -> {
                    step = 1
                }

                4 -> {
                    onDismiss()
                }
            }
        },
        showConfirm = step != 2,
    )
}

fun getConfigStr(msg: String, context: Context): String {
    return try {
        getStr(msg.toInt(), context)
    } catch (e: Throwable) {
        "UNKOWN"
    }
}

//1-水泵1成功 水泵2失败
//2-水泵2成功 水泵1失败
//3-水泵12成功
fun getStr(status: Int, context: Context): String {
    return when (status) {
        1 -> context.getString(R.string.config_line_pump_1_success)
        2 -> context.getString(R.string.config_line_pump_2_success)
        3 -> context.getString(R.string.config_line_pump_success)
        else -> "UNKOWN"
    }
}

/**
 * 线性水泵曲线
 */
@Composable
fun WaterLinePumpChatPopup(progressModel: ProgressModel, onDismiss: () -> Unit) {
    var linePumpData by remember { mutableStateOf<IProtocol.LinePumpData?>(null) }
    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            if (result.success && result.msg != null) {
                try {
                    linePumpData = Gson().fromJson(result.msg, IProtocol.LinePumpData::class.java)
                } catch (e: Throwable) {
                    Log.e("shero", "Illegal param:${result.msg}")
                }
            }
        }
    }
    ScreenPopup(width = 400.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                linePumpData?.let { data ->
                    val linePumpChart = LinePumpChart()
                    val r = MemoryHelper.MemoryReader(data.data, 0, data.data.size)
//                    linePumpChart.data.clear()
                    linePumpChart.kp1 = r.readLEShort() / 100f
                    linePumpChart.kp2 = r.readLEShort() / 100f
                    linePumpChart.kp3 = r.readLEShort() / 100f
                    linePumpChart.kp4 = r.readLEShort() / 100f
                    linePumpChart.kp5 = r.readLEShort() / 100f
                    val kps = listOf(linePumpChart.kp1, linePumpChart.kp1, linePumpChart.kp2, linePumpChart.kp3, linePumpChart.kp4, linePumpChart.kp5)
                    for (i in 0..5) {
                        val flow = NumberUtil.round((r.readLEUShort() / 1000f), 2, RoundingMode.HALF_DOWN).toFloat()
                        val rpm = r.readLEUShort()
                        val pwm = r.readLEUShort()
                        linePumpChart.data.add(LinePumpValue(i + 1, flow, rpm, pwm, kps[i]))
                    }
                    linePumpChart.data.reverse()//发的数据是从大到小发的，所以要反一下
                    linePumpChart.data.add(0, LinePumpValue(0, 0f, 0, 0, 0f))

                    val lineData = mutableListOf<PumpLineChartData>()
                    for (d in linePumpChart.data) {
//                        Log.v("shero", "rpm:${d.rpm} pwm:${d.pwm} flow:${d.flow}")
                        lineData.add(
                            PumpLineChartData(
                            d.rpm.toFloat(), d.flow,
                            xAxisUnit = "rpm", yAxisUnit = "L/min",
                            third = d.pwm.toFloat(), thirdAxisUnit = "pwm")
                        )
                    }
                    PumpLineChart(modifier = Modifier, data = lineData,
                        xLabelCount = lineData.size) { }
                }
            }
        },
        onConfirm = {
            progressModel.cancel()
            progressModel.done()
            onDismiss()
        },
        onDismiss = {},
        showCancel = false,
        showConfirm = true
    )
}

class LinePumpK(val title: String, var value: String)

@Composable
fun DeviceSprayerLinePumpK() {
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val size = 2
        val list = mutableListOf<LinePumpK>()
        for (i in 0 until size) {
            list.add(LinePumpK(stringResource(com.jiagu.v9sdk.R.string.eft_warn_pump_error) + "(${i + 1}) K", EMPTY_TEXT))
        }
        deviceLinePumpData?.data?.let {
            if (it.size > size) {
                list.clear()
                for (p in it) {
                    list.add(LinePumpK(stringResource(com.jiagu.v9sdk.R.string.eft_warn_pump_error) + "(${p.devNum}) K", p.k.toString()))
                }
            } else {
                for (p in it) {
                    list[p.devNum - 1].value = p.k.toString()
                }
            }
        }
        for (p in list) {
            ParameterDataCard(
                modifier = Modifier.weight(1f),
                title = p.title,
                content = p.value
            )
        }
    }
}



//水泵校准
@Composable
fun DeviceSprayerPumpAction() {
    val context = LocalContext.current
    val progressModel = LocalProgressModel.current
    DeviceDetailsCommonButton(
        text = stringResource(id = R.string.water_pump_calibration)
    ) {
        context.showDialog {
            WaterPumpCalibrationPopup(progressModel = progressModel,
                onDismiss = {
                    context.hideDialog()
                })
        }
    }
}

@Composable
fun DeviceSprayerCentrifugal() {
    val deviceCentrifugalData by DroneModel.deviceCentrifugalData.observeAsState()
    CentrifugalNozzleCard(
        modifier = Modifier.fillMaxWidth(),
        deviceCentrifugalData = deviceCentrifugalData)
}

@Composable
fun DeviceSprayerPump() {
    val devicePumpData by DroneModel.devicePumpData.observeAsState()
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    WaterPumpCard(
        modifier = Modifier.fillMaxWidth(),
        devicePumpData = devicePumpData,
        deviceFlowData = deviceFlowData
    )
}

@Composable
fun DeviceSprayerLinePump() {
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    WaterLinePumpCard(
        modifier = Modifier.fillMaxWidth(),
        devicePumpData = deviceLinePumpData,
        deviceFlowData = deviceFlowData
    )
}

@Composable
fun DeviceSprayerFlow() {
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    var totalFlowRateValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    var sprayedAmountValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    var medicineBoxLoadCapacityValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    var levelGaugeStatusValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val cardModifier = Modifier.weight(1f)
        ParameterDataCard(
            modifier = cardModifier, title = stringResource(
                R.string.total_flow_rate, UnitHelper.capacityUnit()
            ), content = totalFlowRateValue
        )
        ParameterDataCard(
            modifier = cardModifier, title = stringResource(
                R.string.sprayed_amount, UnitHelper.capacityUnit()
            ), content = sprayedAmountValue
        )
        ParameterDataCard(
            modifier = cardModifier, title = stringResource(
                R.string.medicine_box_load_capacity, UnitHelper.weightUnit()
            ), content = medicineBoxLoadCapacityValue
        )
        ParameterDataCard(
            modifier = cardModifier,
            title = stringResource(id = R.string.level_gauge_status),
            content = levelGaugeStatusValue
        )
    }

}