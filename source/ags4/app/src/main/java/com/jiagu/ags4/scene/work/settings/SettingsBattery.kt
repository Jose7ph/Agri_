package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.factory.sendIndexedParameter
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.utils.volt2percent
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.battery.Battery
import com.jiagu.jgcompose.text.AutoScrollingText
import kotlin.math.abs

const val SMART_BATTERY_UNIT = "%"
const val NOT_SMART_BATTERY_UNIT = "V"
const val HY_BATTERY_UNIT = "Mpa"
const val CURRENT_UNIT = "A"

/**
 * 电池设置
 */
@Composable
fun BatterySettings(
    modifier: Modifier = Modifier
) {
    BatteryParameterCard(modifier)
}

@Composable
fun BatteryParameterCard(modifier: Modifier, textColor: Color = MaterialTheme.colorScheme.onPrimary) {
    val batteryData by DroneModel.batteryData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val deviceList by DroneModel.deviceList.observeAsState()
    var lowBattery by remember { mutableStateOf(0) }
    var veryLowBattery by remember { mutableStateOf(0) }
    //单位
    val unit = imuData?.let {
        if (it.energyType == VKAg.TYPE_SMART_BATTERY) { SMART_BATTERY_UNIT }
        else { NOT_SMART_BATTERY_UNIT } } ?: NOT_SMART_BATTERY_UNIT
    aptypeData?.let {
        lowBattery = it.getIntValue(VKAg.APTYPE_PROTECT_ACT1)
        veryLowBattery = it.getIntValue(VKAg.APTYPE_PROTECT_ACT2)
        DroneModel.activeDrone?.getParameters()
    }
    var counterType = COUNTER_TYPE_FLOAT
    imuData?.let {
        if (it.energyType == VKAg.TYPE_SMART_BATTERY) {
            counterType = COUNTER_TYPE_INT
        }
    }
    val batteryGroup =
        filterDeviceByTypes(
            idListData = deviceList,
            filterNum = listOf(VKAgCmd.DEVINFO_BATTERY, VKAgCmd.DEVINFO_HYDROGEN_BATTERY)
        )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(settingsGlobalColumnSpacer)
    ) {
        if (isSmartBattery(imuData?.energyType)) {
            //电池实时数据
            batteryData?.let { battery ->
                items(battery.batteries.size) {
                    SmartBatteryCard(
                        batteryData = battery.batteries[it],
                        batteryIds = batteryGroup[VKAgCmd.DEVINFO_BATTERY],
                        textColor = textColor
                    )
                }
            }
        }
        if (isHyBattery(imuData?.energyType)) {
            item {
                FrameColumn(borderColor = textColor) {
                    var hydrogenBatteryId1 = ""
                    var hydrogenBatteryId2 = ""
                    if (batteryGroup.containsKey(VKAgCmd.DEVINFO_HYDROGEN_BATTERY)) {
                        val hydrogenBatterys = batteryGroup[VKAgCmd.DEVINFO_HYDROGEN_BATTERY]
                        hydrogenBatterys?.let {
                            if (it.size > 0) {
                                hydrogenBatteryId1 = it[0].swId
                            }
                            if (it.size > 1) {
                                hydrogenBatteryId2 = it[1].swId
                            }
                        }
                    }
                    hydrogenBatteryData1?.let {
                        HydrogenBatteryDataCard(
                            modifier = Modifier.fillMaxWidth(),
                            batteryData = it,
                            batteryId = hydrogenBatteryId1,
                            textColor = textColor
                        )
                    }
                    hydrogenBatteryData2?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        HydrogenBatteryDataCard(
                            modifier = Modifier.fillMaxWidth(),
                            batteryData = it,
                            batteryId = hydrogenBatteryId2,
                            textColor = textColor
                        )
                    }

                }
            }
        }
        when (imuData?.energyType) {
            //智能电池
            VKAg.TYPE_SMART_BATTERY, VKAg.TYPE_BATTERY, VKAg.TYPE_ENGINE -> {
                //低电量card
                item {
                    FrameColumn(borderColor = textColor) {
                        //标题
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            SettingsGlobalRowText(
                                text = stringResource(id = R.string.low_battery),
                                style = MaterialTheme.typography.titleSmall,
                                textColor = textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        //油电混
                        if (imuData?.energyType == VKAg.TYPE_ENGINE) {
                            BatteryVoltageCounter(
                                cardType = 1,
                                unit = SMART_BATTERY_UNIT,
                                counterType = COUNTER_TYPE_INT,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    sendParameter(VKAg.APTYPE_PROTECT_CAP1, value)
                                }
                            }
                            Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                            BatteryVoltageCounter(
                                cardType = 1,
                                unit = NOT_SMART_BATTERY_UNIT,
                                counterType = COUNTER_TYPE_FLOAT,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    sendParameter(VKAg.APTYPE_PROTECT_LV1, value)
                                }
                            }
                        }
                        //非油电混 (智能/非智能)
                        else {
                            BatteryVoltageCounter(
                                cardType = 1,
                                unit = unit,
                                counterType = counterType,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    if (it.energyType == VKAg.TYPE_SMART_BATTERY) {
                                        sendParameter(VKAg.APTYPE_PROTECT_CAP1, value)
                                    } else {
                                        sendParameter(VKAg.APTYPE_PROTECT_LV1, value)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        /**
                         * 0-警告
                         * 1-悬停
                         * 2-返航
                         * 3-降落
                         */
                        val lowBatteryButtonNames = listOf(
                            stringResource(id = R.string.battery_behavior_warning),
                            stringResource(id = R.string.battery_behavior_hover),
                            stringResource(id = R.string.battery_behavior_returning),
                        )
                        val lowBatteryButtonValues = mutableListOf<Int>()
                        for (i in lowBatteryButtonNames.indices) {
                            lowBatteryButtonValues.add(i)
                        }
                        GroupButtonRow(
                            title = R.string.trigger_action,
                            defaultNumber = lowBattery,
                            names = lowBatteryButtonNames,
                            values = lowBatteryButtonValues,
                            textColor = textColor
                        ) {
                            lowBattery = it
                            sendIndexedParameter(VKAg.APTYPE_PROTECT_ACT1, it)
                        }
                    }
                }
                //严重低电量card
                item {
                    FrameColumn(borderColor = textColor) {
                        //标题
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            SettingsGlobalRowText(
                                text = stringResource(id = R.string.severe_low_battery),
                                style = MaterialTheme.typography.titleSmall,
                                textColor = textColor
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        //油电混
                        if (imuData?.energyType == VKAg.TYPE_ENGINE) {
                            BatteryVoltageCounter(
                                cardType = 2,
                                unit = SMART_BATTERY_UNIT,
                                counterType = COUNTER_TYPE_INT,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    sendParameter(VKAg.APTYPE_PROTECT_CAP2, value)
                                }
                            }
                            Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                            BatteryVoltageCounter(
                                cardType = 2,
                                unit = NOT_SMART_BATTERY_UNIT,
                                counterType = COUNTER_TYPE_FLOAT,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    sendParameter(VKAg.APTYPE_PROTECT_LV2, value)
                                }
                            }
                        }
                        //非油电混 (智能/非智能)
                        else {
                            BatteryVoltageCounter(
                                cardType = 2,
                                unit = unit,
                                counterType = counterType,
                                aptypeData = aptypeData,
                                textColor = textColor
                            ) { value ->
                                imuData?.let {
                                    if (it.energyType == VKAg.TYPE_SMART_BATTERY) {
                                        sendParameter(VKAg.APTYPE_PROTECT_CAP2, value)
                                    } else {
                                        sendParameter(VKAg.APTYPE_PROTECT_LV2, value)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        /**
                         * 0-警告
                         * 1-悬停
                         * 2-返航
                         * 3-降落
                         */
                        val severeLowBatteryButtonNames = listOf(
                            stringResource(id = R.string.battery_behavior_hover),
                            stringResource(id = R.string.battery_behavior_returning),
                            stringResource(id = R.string.battery_behavior_touch_down),
                        )
                        val severeLowBatteryButtonValues = mutableListOf<Int>()
                        for (i in severeLowBatteryButtonNames.indices) {
                            severeLowBatteryButtonValues.add(i + 1)
                        }
                        GroupButtonRow(
                            title = R.string.trigger_action,
                            defaultNumber = veryLowBattery,
                            names = severeLowBatteryButtonNames,
                            values = severeLowBatteryButtonValues,
                            textColor = textColor
                        ) {
                            lowBattery = it
                            sendIndexedParameter(VKAg.APTYPE_PROTECT_ACT2, it)
                        }
                    }
                }
            }
            //氢能源电池
            VKAg.TYPE_HYDROGEN_BATTERY -> {
                //低气压card
                item {
                    FrameColumn(borderColor = textColor) {
                        //标题
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            SettingsGlobalRowText(
                                text = stringResource(id = R.string.low_pressure),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        //低气压
                        BatteryVoltageCounter(
                            cardType = 3,
                            unit = "Mpa",
                            counterType = counterType,
                            aptypeData = aptypeData,
                            textColor = textColor
                        ) { value ->
                            DroneModel.activeDrone?.sendParameter(
                                VKAg.APTYPE_PROTECT_HYDROGEN_LV1, value
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        /**
                         * 0-警告
                         * 1-悬停
                         * 2-返航
                         * 3-降落
                         */
                        val lowBatteryButtonNames = listOf(
                            stringResource(id = R.string.battery_behavior_warning),
                            stringResource(id = R.string.battery_behavior_hover),
                            stringResource(id = R.string.battery_behavior_returning),
                        )
                        val lowBatteryButtonValues = mutableListOf<Int>()
                        for (i in lowBatteryButtonNames.indices) {
                            lowBatteryButtonValues.add(i)
                        }
                        GroupButtonRow(
                            title = R.string.trigger_action,
                            defaultNumber = lowBattery,
                            names = lowBatteryButtonNames,
                            values = lowBatteryButtonValues,
                            textColor = textColor
                        ) {
                            lowBattery = it
                            DroneModel.activeDrone?.sendIndexedParameter(
                                VKAg.APTYPE_PROTECT_ACT1, it
                            )
                        }
                    }

                }
                // 严重低气压card
                item {
                    FrameColumn(borderColor = textColor) {
                        //标题
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            SettingsGlobalRowText(
                                text = stringResource(id = R.string.severe_low_pressure),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        BatteryVoltageCounter(
                            cardType = 4,
                            unit = "Mpa",
                            counterType = counterType,
                            aptypeData = aptypeData,
                            textColor = textColor
                        ) { value ->
                            DroneModel.activeDrone?.sendParameter(
                                VKAg.APTYPE_PROTECT_HYDROGEN_LV2, value
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        /**
                         * 0-警告
                         * 1-悬停
                         * 2-返航
                         * 3-降落
                         */
                        val severeLowBatteryButtonNames = listOf(
                            stringResource(id = R.string.battery_behavior_hover),
                            stringResource(id = R.string.battery_behavior_returning),
                            stringResource(id = R.string.battery_behavior_touch_down),
                        )
                        val severeLowBatteryButtonValues = mutableListOf<Int>()
                        for (i in severeLowBatteryButtonNames.indices) {
                            severeLowBatteryButtonValues.add(i + 1)
                        }
                        GroupButtonRow(
                            title = R.string.trigger_action,
                            defaultNumber = veryLowBattery,
                            names = severeLowBatteryButtonNames,
                            values = severeLowBatteryButtonValues,
                            textColor = textColor
                        ) {
                            lowBattery = it
                            DroneModel.activeDrone?.sendIndexedParameter(
                                VKAg.APTYPE_PROTECT_ACT2, it
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 电池参数BOX
 */
@Composable
private fun BatteryParameterBox(
    modifier: Modifier = Modifier, title: String, value: String,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Row(
        modifier = modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            SettingsGlobalRowText(
                text = title, style = MaterialTheme.typography.labelMedium, textColor = textColor
            )
        }
        Box(
            modifier = Modifier.weight(0.4f)
        ) {
            SettingsGlobalRowText(
                modifier = Modifier.fillMaxWidth(),
                text = value,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                textColor = textColor
            )
        }
    }
}

/**
 * 氢电池卡片
 */
@Composable
fun HydrogenBatteryDataCard(
    modifier: Modifier = Modifier,
    batteryData: VKAg.HydrogenBatteryData,
    batteryId: String,
    textColor: Color = Color.White
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HydrogenBatteryDataTitle(modifier = modifier, batteryData = batteryData, batteryId = batteryId, textColor = textColor)
        HydrogenBatteryParameters(modifier = modifier, batteryData = batteryData, textColor = textColor)
        //报警状态
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.alarm_status) + ":",
                modifier = Modifier,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
            val warns = NewWarnTool.getHydrogenBatteryWarns(context = context, batteryData.status)
            AutoScrollingText(
                text = warns.joinToString(","),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
    }
}

/**
 * 氢电池数据标题
 */
@Composable
private fun HydrogenBatteryDataTitle(
    modifier: Modifier = Modifier, batteryData: VKAg.HydrogenBatteryData, batteryId: String,
    textColor: Color = Color.White
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = "ID:$batteryId",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
            // 自检状态
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.self_check_status) + ":" + if (batteryData.selfCheckStatus.toInt() == 0) stringResource(
                        id = R.string.not_passed
                    ) else stringResource(id = R.string.pass),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }
        }
    }
}

/**
 * 电压范围计数器
 * cardType 1:低电量 2:严重低电量 3:低气压 4：严重低气压
 */
@Composable
fun BatteryVoltageCounter(
    cardType: Int,
    unit: String,
    counterType: String,
    aptypeData: VKAg.APTYPEData?,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    onChange: (Float) -> Unit
) {
    //低电压
    // VKAg.TYPE_BATTERY 非智能电池 V
    val lowFloatMin = 10f
    val lowFloatMax = 90f
    val lowFloatStep = 0.1f
    //VKAg.TYPE_SMART_BATTERY 智能电池 %
    val lowIntMin = 20
    val lowIntMax = 30
    val lowIntStep = 1
    //严重低电压
    // VKAg.TYPE_BATTERY 非智能电池 V
    val veryLowFloatMin = 10f
    val veryLowFloatMax = 90f
    val veryLowFloatStep = 0.1f
    //VKAg.TYPE_SMART_BATTERY 智能电池 %
    val veryLowIntMin = 5
    val veryLowIntMax = 19
    val veryLowIntStep = 1
    //低气压
    val lowPressureMin = 2f
    val lowPressureMax = 20f
    val lowPressureStep = 0.1f
    //严重低气压
    val veryLowPressureMin = 0.5f
    val veryLowPressureMax = 10f
    val veryLowPressureStep = 0.1f

    var lowIntDefaultNumber = lowIntMin
    var lowFloatDefaultNumber = lowFloatMin
    var veryLowIntDefaultNumber = veryLowIntMin
    var veryLowFloatDefaultNumber = veryLowFloatMin
    var lowPressure = lowPressureMin
    var veryLowPressure = veryLowPressureMin
    aptypeData?.let {
        lowIntDefaultNumber = it.getValue(VKAg.APTYPE_PROTECT_CAP1).toInt()
        lowFloatDefaultNumber = it.getValue(VKAg.APTYPE_PROTECT_LV1)
        veryLowIntDefaultNumber = it.getValue(VKAg.APTYPE_PROTECT_CAP2).toInt()
        veryLowFloatDefaultNumber = it.getValue(VKAg.APTYPE_PROTECT_LV2)
        lowPressure = it.getValue(VKAg.APTYPE_PROTECT_HYDROGEN_LV1)
        veryLowPressure = it.getValue(VKAg.APTYPE_PROTECT_HYDROGEN_LV2)
    }
    when (cardType) {
        //低电量
        1 -> {
            val minNumber =
                if (counterType == COUNTER_TYPE_FLOAT) lowFloatMin.toString(1) else lowIntMin.toString()
            val maxNumber =
                if (counterType == COUNTER_TYPE_FLOAT) lowFloatMax.toString(1) else lowIntMax.toString()
            CounterRow(
                titleString = "$minNumber ~ $maxNumber $unit",
                counterType = counterType,
                intMin = lowIntMin,
                intMax = lowIntMax,
                intStep = lowIntStep,
                intDefaultNumber = lowIntDefaultNumber,
                floatMin = lowFloatMin,
                floatMax = lowFloatMax,
                floatStep = lowFloatStep,
                floatDefaultNumber = lowFloatDefaultNumber,
                textColor = textColor,
            ) { value ->
                onChange(value)
            }
        }
        //严重低电量
        2 -> {
            val minNumber =
                if (counterType == COUNTER_TYPE_FLOAT) veryLowFloatMin.toString() else veryLowIntMin.toString()
            val maxNumber =
                if (counterType == COUNTER_TYPE_FLOAT) veryLowFloatMax.toString() else veryLowIntMax.toString()
            CounterRow(
                titleString = "$minNumber ~ $maxNumber $unit",
                counterType = counterType,
                intMin = veryLowIntMin,
                intMax = veryLowIntMax,
                intStep = veryLowIntStep,
                intDefaultNumber = veryLowIntDefaultNumber,
                floatMin = veryLowFloatMin,
                floatMax = veryLowFloatMax,
                floatStep = veryLowFloatStep,
                floatDefaultNumber = veryLowFloatDefaultNumber,
                floatDecimal = 1,
                textColor = textColor
            ) { value ->
                onChange(value)
            }
        }
        //低气压
        3 -> {
            CounterRow(
                titleString = "$lowPressureMin ~ $lowPressureMax $unit",
                counterType = COUNTER_TYPE_FLOAT,
                floatMin = lowPressureMin,
                floatMax = lowPressureMax,
                floatStep = lowPressureStep,
                floatDefaultNumber = lowPressure,
                floatDecimal = 1,
                textColor = textColor
            ) { value ->
                onChange(value)
            }
        }
        //严重低气压
        4 -> {
            CounterRow(
                titleString = "$veryLowPressureMin ~ $veryLowPressureMax $unit",
                counterType = COUNTER_TYPE_FLOAT,
                floatMin = veryLowPressureMin,
                floatMax = veryLowPressureMax,
                floatStep = veryLowPressureStep,
                floatDefaultNumber = veryLowPressure,
                floatDecimal = 1,
                textColor = textColor
            ) { value ->
                onChange(value)
            }
        }
    }

}

/**
 * 氢电池参数
 */
@Composable
private fun HydrogenBatteryParameters(
    modifier: Modifier = Modifier, batteryData: VKAg.HydrogenBatteryData,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val boxModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        // 电池电压
        BatteryParameterBox(
            title = stringResource(id = R.string.battery_voltage1) + "(V):",
            value = batteryData.batteryVoltage.toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 电堆电压
        BatteryParameterBox(
            title = stringResource(id = R.string.stack_voltage) + "(V):",
            value = batteryData.stackVoltage.toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 电机电压
        BatteryParameterBox(
            title = stringResource(id = R.string.motor_voltage) + "(V):",
            value = batteryData.motorVoltage.toString(),
            modifier = boxModifier,
            textColor = textColor
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val boxModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        // 电机电流
        BatteryParameterBox(
            title = stringResource(id = R.string.motor_current) + "(A):",
            value = abs(batteryData.motorCurrent).toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 电池补能电流
        BatteryParameterBox(
            title = stringResource(id = R.string.battery_recharge_current) + "(A):",
            value = batteryData.rechargeCurrent.toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 气罐压力
        BatteryParameterBox(
            title = stringResource(id = R.string.tank_pressure) + "(Mpa):",
            value = batteryData.gasCylinderPressure.toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val boxModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        // 管道压力
        BatteryParameterBox(
            title = stringResource(id = R.string.pipeline_pressure) + "(Mpa):",
            value = abs(batteryData.pipelinePressure).toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 主板温度
        BatteryParameterBox(
            title = stringResource(id = R.string.mainboard_temperature) + "(℃):",
            value = batteryData.mainBoardTemperature.toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        // 电堆温度
        BatteryParameterBox(
            title = stringResource(id = R.string.stack_temperature) + "(℃):",
            value = batteryData.stackTemperature.toString(),
            modifier = boxModifier,
            textColor = textColor
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val boxModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        //开机状态
        BatteryParameterBox(
            title = stringResource(id = R.string.booting_state) + ":",
            value = if (batteryData.booting) stringResource(id = R.string.power_on) else stringResource(
                id = R.string.power_off
            ),
            modifier = boxModifier,
            textColor = textColor
        )
        //放电状态
        BatteryParameterBox(
            title = stringResource(id = R.string.discharging_state) + ":",
            value = if (batteryData.discharging) stringResource(id = R.string.discharging) else stringResource(
                id = R.string.not_discharging
            ),
            modifier = boxModifier,
            textColor = textColor
        )
        //充电状态
        BatteryParameterBox(
            title = stringResource(id = R.string.charging_state) + ":",
            value = if (batteryData.charging) stringResource(id = R.string.charging) else stringResource(
                id = R.string.not_charging
            ),
            modifier = boxModifier,
            textColor = textColor
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val boxModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        // 电堆功率 电堆功率是（总电流（电机电流）-补电电流）*电堆电压
        BatteryParameterBox(
            title = stringResource(id = R.string.stack_power) + ":",
            value = ((abs(batteryData.motorCurrent) - batteryData.rechargeCurrent) * batteryData.stackVoltage).toString(1),
            modifier = boxModifier,
            textColor = textColor
        )
        Spacer(modifier = boxModifier)
        Spacer(modifier = boxModifier)
    }
}

/**
 * Smart battery card
 *
 * @param batteryData
 */
@Composable
fun SmartBatteryCard(batteryData: VKAg.BatteryData, batteryIds: List<VKAg.IDListData>?, textColor: Color = Color.White) {
    FrameColumn(borderColor = textColor) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
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
                items(batteryData.cellVolt?.size ?: 0) {
                    val cv = batteryData.cellVolt[it]
                    Battery(
                        modifier = Modifier
                            .width(30.dp)
                            .height(10.dp),
                        battery = cv.toString(1),
                        voltageRatio = volt2percent(cv),
                        textColor = textColor
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
                    image = R.drawable.default_battery_voltage,
                    textColor = textColor
                )
                DeviceBatteryParameterBox(
                    title = stringResource(id = R.string.battery_current),
                    value = abs(batteryData.current).toString(1),
                    modifier = boxModifier,
                    image = R.drawable.default_battery_current,
                    textColor = textColor
                )
                DeviceBatteryParameterBox(
                    title = stringResource(id = R.string.battery_temperature),
                    value = batteryData.temperature.toString(1),
                    modifier = boxModifier,
                    image = R.drawable.default_battery_tempeture,
                    textColor = textColor
                )
                DeviceBatteryParameterBox(
                    title = stringResource(id = R.string.battery_number_of_cycles),
                    value = batteryData.cycle.toString(),
                    modifier = boxModifier,
                    image = R.drawable.default_battery_cycle_count,
                    textColor = textColor
                )
            }
        }
    }
}

/**
 * 电池参数BOX
 */
@Composable
private fun DeviceBatteryParameterBox(
    modifier: Modifier = Modifier, title: String, value: String, image: Int, textColor: Color = Color.White
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
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier,
                textAlign = TextAlign.Start,
                color = textColor
            )
            AutoScrollingText(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier,
                textAlign = TextAlign.Start,
                color = textColor
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360, backgroundColor = 0xFF000000)
@Composable
fun DeviceRadarPreview() {
    val batterys = getTestBattery()
    DroneModel.batteryData.postValue(batterys)
    val imu = VKAg.IMUData()
    imu.energyType = VKAg.TYPE_HYDROGEN_BATTERY
    DroneModel.imuData.postValue(imu)
    DroneModel.hydrogenBatteryData1.postValue(getTestHyBattery())
    DroneModel.hydrogenBatteryData2.postValue(getTestHyBattery())
    ComposeTheme {
        BatterySettings()
    }

}

fun getTestBattery(): VKAg.BatteryGroup {
    val battery = VKAg.BatteryData()
    battery.batId = "000000001"
    battery.devNum = 10
    battery.factoryId = 10
    battery.cycle = 20
    battery.voltage = 60.7f
    battery.current = 20.3f
    battery.temperature = 30.4f
    battery.percent = 80
    battery.status1 = 2
    battery.status2 = 4
    battery.out_charge = 1
    battery.out_discharge = 0
    battery.out_temperature = 70
    battery.out_current = 30
    battery.cellVolt = floatArrayOf(10.1f, 10.2f, 10.3f, 10.4f, 10.5f, 10.6f, 10.7f, 10.8f, 10.9f, 11.0f, 11.1f, 11.2f, 11.3f, 11.4f, 11.5f, 11.6f, 11.7f, 11.8f, 11.9f, 12.0f, 12.1f)

    val battery2 = VKAg.BatteryData()
    battery2.batId = "000000002"
    battery2.devNum = 10
    battery2.factoryId = 11
    battery2.cycle = 25
    battery2.voltage = 60.9f
    battery2.current = 20.7f
    battery2.temperature = 32.4f
    battery2.percent = 70
    battery2.status1 = 3
    battery2.status2 = 5
    battery2.out_charge = 2
    battery2.out_discharge = 4
    battery2.out_temperature = 80
    battery2.out_current = 40
    battery2.cellVolt = floatArrayOf(10.1f, 10.2f, 10.3f, 10.4f, 10.5f, 10.6f, 10.7f, 10.8f, 10.9f, 11.0f, 11.1f, 11.2f, 11.3f, 11.4f, 11.5f, 11.6f, 11.7f, 11.8f, 11.9f, 12.0f,)

    return VKAg.BatteryGroup(listOf(battery, battery2))
}
fun getTestHyBattery(): VKAg.HydrogenBatteryData {
    return VKAg.HydrogenBatteryData.createTestData()
}

fun isHyBattery(energyType: Int?): Boolean {
    return energyType == VKAg.TYPE_HYDROGEN_BATTERY
}

fun isSmartBattery(energyType: Int?): Boolean {
    return energyType == VKAg.TYPE_SMART_BATTERY
}

