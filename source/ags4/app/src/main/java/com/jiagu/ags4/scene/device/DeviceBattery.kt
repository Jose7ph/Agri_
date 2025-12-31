package com.jiagu.ags4.scene.device

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.utils.volt2percent
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.battery.Battery
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import kotlin.math.abs

@Composable
fun DeviceBattery() {
    val navController = LocalNavController.current
    val context = LocalContext.current

    val batteryData by DroneModel.batteryData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val deviceList by DroneModel.deviceList.observeAsState()
    var bias = 0f
    var volt: Float? = null
    val batteryType = when (imuData?.energyType) {
        VKAg.TYPE_SMART_BATTERY -> VKAg.TYPE_SMART_BATTERY
        VKAg.TYPE_HYDROGEN_BATTERY -> VKAg.TYPE_HYDROGEN_BATTERY
        else -> VKAg.TYPE_BATTERY
    }
    imuData?.let {
        val energy = it.energy
        volt = energy - bias
    }
    aptypeData?.let {
        bias = it.getValue(VKAg.APTYPE_VOLT_BIAS)
    }
    val batteryGroup =
        filterDeviceByTypes(
            idListData = deviceList,
            filterNum = listOf(VKAgCmd.DEVINFO_BATTERY, VKAgCmd.DEVINFO_HYDROGEN_BATTERY)
        )

    MainContent(title = stringResource(id = R.string.device_management_battery),
        barAction = {
            if (batteryType == VKAg.TYPE_BATTERY) {
                TopBarBottom(
                    text = stringResource(id = R.string.voltage_calibration) + if (volt != null) "(${volt}V)" else ""

                ) {
                    context.showDialog {
                        VoltageCalibrationPopup(
                            volt = volt
                        ) {
                            context.hideDialog()
                        }
                    }
                }
            }
        },
        breakAction = {
            navController.popBackStack()
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        ) {
            when (batteryType) {
                VKAg.TYPE_SMART_BATTERY -> {
                    batteryData?.let { battery ->
                        items(battery.batteries.size) {
                            SmartBatteryCard(
                                batteryData = battery.batteries[it],
                                batteryIds = batteryGroup[VKAgCmd.DEVINFO_BATTERY]
                            )
                        }
                    }
                }

                VKAg.TYPE_HYDROGEN_BATTERY -> {
                    item {
                        HydrogenBatteryCard(
                            hydrogenBatteryData1 = hydrogenBatteryData1,
                            hydrogenBatteryData2 = hydrogenBatteryData2,
                            batteryIds = batteryGroup[VKAgCmd.DEVINFO_HYDROGEN_BATTERY]
                        )
                    }
                }
            }
        }
    }
}

/**
 * Smart battery card
 *
 * @param batteryData
 */
@Composable
private fun SmartBatteryCard(batteryData: VKAg.BatteryData, batteryIds: List<VKAg.IDListData>?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .border(width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //标题
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var batteryId = stringResource(id = R.string.na)
            batteryIds?.forEach { battery ->
                if (battery.devNum == batteryData.devNum) {
                    batteryId = battery.hwId
                }
            }
            //id
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = stringResource(id = R.string.battery_id) + ":${batteryId}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            //type
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = stringResource(id = R.string.device_weight_manufactor) + ":" + batteryData.factoryName.ifBlank {
                        stringResource(
                            id = R.string.na
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        //电芯
        LazyHorizontalGrid(
            modifier = Modifier
                .height(130.dp)
                .fillMaxWidth(),
            rows = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(batteryData.cellVolt.size) {
                val cv = batteryData.cellVolt[it]
                Battery(
                    modifier = Modifier
                        .width(30.dp)
                        .height(10.dp),
                    battery = cv.toString(1),
                    voltageRatio = volt2percent(cv)
                )
            }
        }
        //参数
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val boxModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            DeviceBatteryParameterBox(
                title = stringResource(id = R.string.battery_voltage),
                value = batteryData.voltage.toString(1),
                modifier = boxModifier,
                image = R.drawable.default_battery_voltage
            )
            DeviceBatteryParameterBox(
                title = stringResource(id = R.string.battery_current),
                value = abs(batteryData.current).toString(1),
                modifier = boxModifier,
                image = R.drawable.default_battery_current
            )
            DeviceBatteryParameterBox(
                title = stringResource(id = R.string.battery_temperature),
                value = batteryData.temperature.toString(1),
                modifier = boxModifier,
                image = R.drawable.default_battery_tempeture
            )
            DeviceBatteryParameterBox(
                title = stringResource(id = R.string.battery_number_of_cycles),
                value = batteryData.cycle.toString(),
                modifier = boxModifier,
                image = R.drawable.default_battery_cycle_count
            )
        }
    }
}

@Composable
private fun HydrogenBatteryCard(
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?,
    batteryIds: List<VKAg.IDListData>?
) {
    var hydrogenBatteryId1 = "N/A"
    var hydrogenBatteryId2 = "N/A"
    batteryIds?.let {
        if (it.isNotEmpty()) {
            hydrogenBatteryId1 = it[0].swId
        }
        if (it.size > 1) {
            hydrogenBatteryId2 = it[1].swId
        }
    }
    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //状态
            HydrogenBatteryStateCard(
                modifier = Modifier
                    .weight(1f),
                hydrogenBatteryData1 = hydrogenBatteryData1,
                hydrogenBatteryData2 = hydrogenBatteryData2,
                batteryId1 = hydrogenBatteryId1,
                batteryId2 = hydrogenBatteryId2,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //压力
            HydrogenBatteryPressureCard(
                modifier = Modifier
                    .weight(1f),
                hydrogenBatteryData1 = hydrogenBatteryData1,
                hydrogenBatteryData2 = hydrogenBatteryData2,
                batteryId1 = hydrogenBatteryId1,
                batteryId2 = hydrogenBatteryId2,
            )
            //温度
            HydrogenBatteryTemperatureCard(
                modifier = Modifier
                    .weight(1f),
                hydrogenBatteryData1 = hydrogenBatteryData1,
                hydrogenBatteryData2 = hydrogenBatteryData2,
                batteryId1 = hydrogenBatteryId1,
                batteryId2 = hydrogenBatteryId2,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //电压
            HydrogenBatteryVoltageCard(
                modifier = Modifier
                    .weight(1f),
                hydrogenBatteryData1 = hydrogenBatteryData1,
                hydrogenBatteryData2 = hydrogenBatteryData2,
                batteryId1 = hydrogenBatteryId1,
                batteryId2 = hydrogenBatteryId2,
            )
            //电流
            HydrogenBatteryCurrentCard(
                modifier = Modifier
                    .weight(0.9f),
                hydrogenBatteryData1 = hydrogenBatteryData1,
                hydrogenBatteryData2 = hydrogenBatteryData2,
                batteryId1 = hydrogenBatteryId1,
                batteryId2 = hydrogenBatteryId2,
            )
        }
    }
}

/**
 * 电池参数BOX
 */
@Composable
private fun DeviceBatteryParameterBox(
    modifier: Modifier = Modifier, title: String, value: String, image: Int
) {
    val imageSize = 24.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier.size(imageSize),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Column(modifier = Modifier) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier,
                textAlign = TextAlign.Start
            )
            AutoScrollingText(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier,
                textAlign = TextAlign.Start
            )
        }
    }
}

/**
 * 电压校准弹窗
 */
@Composable
fun VoltageCalibrationPopup(
    volt: Float?,
    onDismiss: () -> Unit
) {
    val voltageCalibrationTip = stringResource(id = R.string.voltage_calibration_input_tip)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)

    var inputVoltage by remember { mutableStateOf("") }
    var calibrationStep by remember {
        mutableIntStateOf(1)
    }
    var calibrationTip by remember {
        mutableStateOf(voltageCalibrationTip)
    }
    calibrationTip = when (calibrationStep) {
        1 -> voltageCalibrationTip
        2 -> inCalibrationTip
        3 -> calibrationSuccessTip
        else -> ""
    }
    val confirmText = when (calibrationStep) {
        1 -> R.string.start_calibration
        else -> R.string.confirm
    }

    val inputValid = Validator.checkNumerical(inputVoltage) || inputVoltage.isBlank()
    ScreenPopup(
        width = 310.dp,
        confirmText = confirmText,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.voltage_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                if (calibrationStep == 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.voltage_calibration_input_tip),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        NormalTextField(
                            text = inputVoltage,
                            modifier = Modifier
                                .width(200.dp)
                                .height(30.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                ),
                            onValueChange = { inputVoltage = it },
                            borderColor = if (inputValid) Color.LightGray else MaterialTheme.colorScheme.error
                        )
                    }

                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = if (calibrationStep == 3) Color.Red else Color.Black
                        )
                    }
                }
            }
        },
        onDismiss = {
            onDismiss()
        },
        onConfirm = {
            when (calibrationStep) {
                1 -> {
                    DroneModel.activeDrone?.let { drone ->
                        calibrationStep = 2
                        calibrationTip = inCalibrationTip
                        //计算真实电压
                        volt?.let {
                            if (inputVoltage.isNotBlank()) {
                                val realVoltage = realVoltageCalculate(
                                    it, inputVoltage.toFloat()
                                )
                                drone.calibVolt(realVoltage)
                                calibrationStep = 3
                                calibrationTip = calibrationSuccessTip
                            }
                        }
                    }
                }

                3 -> {
                    onDismiss()
                }
            }
        },
        confirmEnable = if (calibrationStep == 1) {
            inputValid && inputVoltage.isNotBlank()
        } else {
            true
        },
        showConfirm = calibrationStep != 2,
        showCancel = calibrationStep != 3,
    )
}

/**
 * 计算真实电压
 * volt：energy - bias
 * inputVolt：输入的电压
 */
private fun realVoltageCalculate(volt: Float, inputVolt: Float): Float {
    var setInputVolt = inputVolt
    val min = volt - 5
    val max = volt + 5
    if (setInputVolt < min) {
        setInputVolt = min
    } else if (setInputVolt > max) {
        setInputVolt = max
    }
    setInputVolt -= volt
    return setInputVolt
}

/**
 * 氢电池电压卡片
 *
 * @param modifier
 * @param hydrogenBatteryData1
 * @param hydrogenBatteryData2
 */
@Composable
fun HydrogenBatteryVoltageCard(
    modifier: Modifier = Modifier,
    batteryId1: String,
    batteryId2: String,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?
) {
    Column(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small
        )
    ) {
        val batteryNameRowWidth = 60.dp
        //title
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //电池ID
            ParamCardTextBox(
                modifier = Modifier.width(batteryNameRowWidth),
                text = "ID",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电池电压
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.battery_voltage1) + "(V)",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电堆电压
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.stack_voltage) + "(V)",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电机电压
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.motor_voltage) + "(V)",
                color = Color.White
            )
        }
        //battery1
        hydrogenBatteryData1?.let {
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId1
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电池电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.batteryVoltage.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电堆电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.stackVoltage.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电机电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.motorVoltage.toString(1),
                )
            }
        }
        //battery2
        hydrogenBatteryData2?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId2
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电池电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.batteryVoltage.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电堆电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.stackVoltage.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电机电压
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.motorVoltage.toString(1),
                )
            }
        }
    }
}

/**
 * 氢电池电流卡片
 *
 * @param modifier
 * @param hydrogenBatteryData1
 * @param hydrogenBatteryData2
 */
@Composable
fun HydrogenBatteryCurrentCard(
    modifier: Modifier = Modifier,
    batteryId1: String,
    batteryId2: String,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?
) {
    Column(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small
        )
    ) {
        val batteryNameRowWidth = 60.dp
        //title
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //电池ID
            ParamCardTextBox(
                modifier = Modifier.width(batteryNameRowWidth),
                text = "ID",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电机电流
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.motor_current) + "(A)",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电池补能电流
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.battery_recharge_current) + "(A)",
                color = Color.White
            )
        }
        //battery1
        hydrogenBatteryData1?.let {
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId1
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电机电流
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.motorCurrent.toString(1)
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电池补能电流
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.rechargeCurrent.toString(1)
                )
            }
        }
        //battery2
        hydrogenBatteryData2?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池名
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId2
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电机电流
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.motorCurrent.toString(1)
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电池补能电流
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.rechargeCurrent.toString(1)
                )
            }
        }
    }
}

/**
 * 氢电池压力卡片
 *
 * @param modifier
 * @param hydrogenBatteryData1
 * @param hydrogenBatteryData2
 */
@Composable
fun HydrogenBatteryPressureCard(
    modifier: Modifier = Modifier,
    batteryId1: String,
    batteryId2: String,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?
) {
    Column(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small
        )
    ) {
        val batteryNameRowWidth = 60.dp
        //title
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //电池ID
            ParamCardTextBox(
                modifier = Modifier.width(batteryNameRowWidth),
                text = "ID",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //气罐压力
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.tank_pressure) + "(Mpa)",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //管道压力
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.pipeline_pressure) + "(Mpa)",
                color = Color.White
            )
        }
        //battery1
        hydrogenBatteryData1?.let {
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId1
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //气罐压力
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.gasCylinderPressure.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //管道压力
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = abs(it.pipelinePressure).toString(1)
                )
            }
        }
        //battery2
        hydrogenBatteryData2?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId2
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //气罐压力
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.gasCylinderPressure.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //管道压力
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = abs(it.pipelinePressure).toString(1)
                )
            }
        }
    }
}

/**
 * 氢电池温度卡片
 *
 * @param modifier
 * @param hydrogenBatteryData1
 * @param hydrogenBatteryData2
 */
@Composable
fun HydrogenBatteryTemperatureCard(
    modifier: Modifier = Modifier,
    batteryId1: String,
    batteryId2: String,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?
) {
    Column(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.small
        )
    ) {
        val batteryNameRowWidth = 60.dp
        //title
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //电池ID
            ParamCardTextBox(
                modifier = Modifier.width(batteryNameRowWidth),
                text = "ID",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //主板温度
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.mainboard_temperature) + "(℃)",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //电堆温度
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.stack_temperature) + "(℃)",
                color = Color.White
            )
        }
        //battery1
        hydrogenBatteryData1?.let {
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId1
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //主板温度
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.mainBoardTemperature.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电堆温度
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.stackTemperature.toString(1),
                )
            }
        }
        //battery2
        hydrogenBatteryData2?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId2
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //主板温度
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.mainBoardTemperature.toString(1),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //电堆温度
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = it.stackTemperature.toString(1),
                )
            }
        }
    }
}

/**
 * 氢电池状态卡片
 *
 * @param modifier
 * @param hydrogenBatteryData1
 * @param hydrogenBatteryData2
 */
@Composable
fun HydrogenBatteryStateCard(
    modifier: Modifier = Modifier,
    batteryId1: String,
    batteryId2: String,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    ) {
        val batteryNameRowWidth = 60.dp
        //title
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //电池ID
            ParamCardTextBox(
                modifier = Modifier.width(batteryNameRowWidth),
                text = "ID",
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //开机状态
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.booting_state),
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //充电状态
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.charging_state),
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //放电状态
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.discharging_state),
                color = Color.White
            )
            VerticalDivider(thickness = 1.dp, color = Color.White)
            //自检状态
            ParamCardTextBox(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.self_check_status),
                color = Color.White
            )
        }
        //battery1
        hydrogenBatteryData1?.let {
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId1
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //开机状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.booting) stringResource(id = R.string.power_on) else
                        stringResource(id = R.string.power_off)
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //充电状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.charging) stringResource(id = R.string.charging) else
                        stringResource(id = R.string.not_charging),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //放电状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.discharging) stringResource(id = R.string.discharging) else
                        stringResource(id = R.string.not_discharging),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //自检状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.selfCheckStatus.toInt() == 0) stringResource(id = R.string.not_passed) else
                        stringResource(id = R.string.pass),
                )
            }
        }
        //battery2
        hydrogenBatteryData2?.let {
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .height(30.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //电池ID
                ParamCardTextBox(
                    modifier = Modifier.width(batteryNameRowWidth),
                    text = batteryId2
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //开机状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.booting) stringResource(id = R.string.power_on) else
                        stringResource(id = R.string.power_off)
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //充电状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.charging) stringResource(id = R.string.charging) else
                        stringResource(id = R.string.not_charging),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //放电状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.discharging) stringResource(id = R.string.discharging) else
                        stringResource(id = R.string.not_discharging),
                )
                VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                //自检状态
                ParamCardTextBox(
                    modifier = Modifier.weight(1f),
                    text = if (it.selfCheckStatus.toInt() == 0) stringResource(id = R.string.not_passed) else
                        stringResource(id = R.string.pass),
                )
            }
        }
    }
}

@Composable
private fun ParamCardTextBox(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Black
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AutoScrollingText(
            text = text,
            color = color,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}