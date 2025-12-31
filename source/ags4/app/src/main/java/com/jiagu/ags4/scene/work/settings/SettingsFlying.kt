package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.Constants
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.scene.factory.sendIndexedParameter
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

const val COUNTER_TYPE_INT = "int"

const val COUNTER_TYPE_FLOAT = "float"

//摇杆图片大小
val rockerImageSize = 90.dp

@Composable
fun FlyingSettings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val config = Config(context)
    val aptypeData = DroneModel.aptypeData.observeAsState(initial = null)
    val deviceBoomSensorData = DroneModel.deviceBoomSensorData.observeAsState()

    //在位传感器switch button value
    var inPlaceSensorSwitchButton by remember {
        mutableStateOf(false)
    }
    //在位传感器button
    var inPlaceSensorButtonEnableList: List<Boolean> = listOf(true, true, true, true)

    aptypeData.value?.let {
        inPlaceSensorSwitchButton = it.getIntValue(VKAg.APTYPE_BOOM_WARN_SWITCH) == 1
    }

    deviceBoomSensorData.value?.let {
        inPlaceSensorButtonEnableList = listOf(
            ((it.warn shr 0) and 0x1 == 0),
            ((it.warn shr 1) and 0x1 == 0),
            ((it.warn shr 2) and 0x1 == 0),
            ((it.warn shr 3) and 0x1 == 0)
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(settingsGlobalColumnSpacer)
    ) {
        item {
            FrameColumn {
                //手动控制高度
                //1-锁定 2-可控 3-自动
                val names1 = listOf(
                    stringResource(id = R.string.manual_control_height_close),
                    stringResource(id = R.string.manual_control_height_controlled),
                    stringResource(id = R.string.manual_control_height_automatic)
                )
                val values1 = mutableListOf<Int>()
                for (i in names1.indices) {
                    values1.add(i + 1)
                }
                GroupButtonRow(
                    title = R.string.manual_control_height,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_GAODUKEKONG)
                        ?: 0).toInt(),
                    names = names1,
                    values = values1
                ) {
                    sendParameter(VKAg.APTYPE_GAODUKEKONG, it.toFloat())
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //手动控制方向 1-锁定 2-可控
//                val names2 = listOf(
//                    stringResource(id = R.string.close), stringResource(id = R.string.open)
//                )
//                val values2 = mutableListOf<Int>()
//                for (i in names2.indices) {
//                    values2.add(i + 1)
//                }
//                GroupButtonRow(
//                    title = R.string.manual_control_direction,
//                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_HANGXIANGKEKONG)
//                        ?: 0).toInt(),
//                    names = names2,
//                    values = values2
//                ) {
//                    sendParameter(VKAg.APTYPE_HANGXIANGKEKONG, it.toFloat())
//                }
//                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //机头方向 1，锁定 2，跟随
                val names3 = listOf(
                    stringResource(id = R.string.head_lock),
                    stringResource(id = R.string.head_following)
                )
                val values3 = mutableListOf<Int>()
                for (i in names3.indices) {
                    values3.add(i + 1)
                }
                GroupButtonRow(
                    title = R.string.head_direction,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_HEAD_TYPE)
                        ?: 0).toInt(),
                    names = names3,
                    values = values3
                ) {
                    sendIndexedParameter(VKAg.APTYPE_HEAD_TYPE, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //转弯方式 0:直角 1:U型 2:常规
                val names4 = listOf(
                    stringResource(id = R.string.right_angle_turn),
                    stringResource(id = R.string.u_turn),
                    stringResource(id = R.string.conventional_turning)
                )
                val values4 = mutableListOf<Int>()
                for (i in names4.indices) {
                    values4.add(i)
                }
                GroupButtonRow(
                    title = R.string.turning_method,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_TURN_TYPE)
                        ?: 1).toInt(),
                    names = names4,
                    values = values4
                ) {
                    sendIndexedParameter(VKAg.APTYPE_TURN_TYPE, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //在位传感器
                InPlaceSensorRow(
                    switchCheck = inPlaceSensorSwitchButton,
                    buttonEnableList = inPlaceSensorButtonEnableList
                )
            }
        }
        item {
            FrameColumn {
                //起航/返航高度(m) 3 ~ 30
                CounterRow(
                    titleString = stringResource(R.string.flying_hight, UnitHelper.lengthUnit()),
                    counterType = COUNTER_TYPE_INT,
                    intMin = 3,
                    intMax = 30,
                    intStep = 1,
                    intDefaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT)
                        ?: 3).toInt(),
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_TAKEOFF_HEIGHT, it)
                    sendParameter(VKAg.APTYPE_GOHOME_HEIGHT, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //起航/返航速度(m/s) 1 ~ 10 (dm/s 10 ~ 100)
                CounterRow(
                    titleString = stringResource(R.string.flying_speed, UnitHelper.lengthUnit()),
                    counterType = COUNTER_TYPE_FLOAT,
                    floatMin = 1f,
                    floatMax = 10f,
                    floatStep = 0.5f,
                    floatDecimal = 1,
                    floatDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_START_END_SPEED)
                        ?: 1f,
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_START_END_SPEED, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //最远飞行距离 50 ~ 2000
                CounterRow(
                    titleString = stringResource(
                        R.string.max_flying_distance,
                        UnitHelper.lengthUnit()
                    ),
                    counterType = COUNTER_TYPE_INT,
                    intMin = 50,
                    intMax = 2000,
                    intStep = 1,
                    intDefaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_FENCE_RAIDUS)
                        ?: 0).toInt(),
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_FENCE_RAIDUS, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //最高飞行高度 15 ~ 300
                CounterRow(
                    titleString = stringResource(
                        R.string.max_flying_hight,
                        UnitHelper.lengthUnit()
                    ),
                    counterType = COUNTER_TYPE_INT,
                    intMin = 15,
                    intMax = 300,
                    intStep = 1,
                    intDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_FENCE_HEIGHT)
                        ?.toInt() ?: 0,
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_FENCE_HEIGHT, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //最大速度 0.5 ~ 10 (dm/s 10 ~ 100)
                CounterRow(
                    titleString = stringResource(R.string.max_speed, UnitHelper.lengthUnit()),
                    counterType = COUNTER_TYPE_FLOAT,
                    floatMin = 1f,
                    floatMax = Constants.MAX_SPEED,
                    floatStep = 0.5f,
                    floatDefaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_MAX_HSPEED) ?: 0f),
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_MAX_HSPEED, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //航线末尾抬高
                SwitchButtonRow(
                    title = R.string.end_of_route_climb,
                    defaultChecked = (aptypeData.value?.getValue(VKAg.APTYPE_END_LIFT_UP_SWITCH) == 1f),
                ) {
                    sendParameter(VKAg.APTYPE_END_LIFT_UP_SWITCH, if (it) 1f else 0f)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //航线末尾抬高高度
                CounterRow(
                    titleString = stringResource(R.string.end_of_route_climb_altitude) + "(" + UnitHelper.lengthUnit() + ")",
                    counterType = COUNTER_TYPE_FLOAT,
                    floatMin = 0f,
                    floatMax = 5f,
                    floatStep = 0.1f,
                    floatDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_END_LIFT_UP_HEIGHT)
                        ?: 0f,
                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                ) {
                    sendParameter(VKAg.APTYPE_END_LIFT_UP_HEIGHT, it)
                }
            }
        }
        item {
            FrameColumn {
                //失联后继续作业 1开 2关
                SwitchButtonRow(
                    title = R.string.after_losing_contact_continuing_homework,
                    defaultChecked = (aptypeData.value?.getValue(VKAg.APTYPE_CONTROLLER_LOST_WORK)
                        ?: 2f) == 1f
                ) {
                    sendIndexedParameter(VKAg.APTYPE_CONTROLLER_LOST_WORK, if (it) 1 else 2)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //作业完成动作 0:悬停 1:返航
                val names = listOf(
                    stringResource(id = R.string.homework_completion_action_hover),
                    stringResource(id = R.string.homework_completion_action_returning)
                )
                val values = mutableListOf<Int>()
                for (i in names.indices) {
                    values.add(i)
                }
                GroupButtonRow(
                    title = R.string.homework_completion_action,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_DONE_ACT) ?: 1).toInt(),
                    names = names,
                    values = values
                ) {
                    sendIndexedParameter(VKAg.APTYPE_DONE_ACT, it)
                }
            }
        }
        item {
            FrameColumn {
                //探照灯开关 0关 1 开
                SwitchButtonRow(
                    title = R.string.searchlight_switch,
                    defaultChecked = ((aptypeData.value?.getIntValue(VKAg.APTYPE_LED_SWITCH)
                        ?.shr(3))?.and(0x1)) == 1
                ) {
                    DroneModel.activeDrone?.setLedSwitch(3, it)
                }
//                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
//                //探照灯亮度(%) 0 ~ 100
//                CounterRow(
//                    title = R.string.searchlight_brightness,
//                    counterType = COUNTER_TYPE_INT,
//                    intMin = 0,
//                    intMax = 100,
//                    intStep = 10,
//                    intDefaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_LED_STRENGTH)
//                        ?: 0).toInt()
//                ) {
//                    DroneModel.activeDrone?.sendIndexedParameter(
//                        VKAg.APTYPE_LED_STRENGTH,
//                        it.toInt()
//                    )
//                }
            }
        }
        item {
            FrameColumn {
                //断药保护 1-关闭 2-悬停 3-返航
                val names1 = listOf(
                    stringResource(id = R.string.stop_drug_protect_close),
                    stringResource(id = R.string.stop_drug_protect_hover),
                    stringResource(id = R.string.stop_drug_protect_returning)
                )
                val values1 = mutableListOf<Int>()
                for (i in names1.indices) {
                    values1.add(i + 1)
                }
                GroupButtonRow(
                    title = R.string.stop_drug_protect,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_DRUG_ACT) ?: 1).toInt(),
                    names = names1,
                    values = values1
                ) {
                    sendIndexedParameter(VKAg.APTYPE_DRUG_ACT, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                // 地面站失效保护 1-关闭 2-悬停 3-返航
                val names3 = listOf(
                    stringResource(id = R.string.stop_drug_protect_close),
                    stringResource(id = R.string.remote_control_invalid_protect_hover),
                    stringResource(id = R.string.remote_control_invalid_protect_returning)
                )
                val values3 = listOf(1, 2, 3)
                GroupButtonRow(
                    title = R.string.param_fly_station_protect,
                    defaultNumber = (aptypeData.value?.getValue(VKAg.APTYPE_GS_LOST_ACT)
                        ?: 1).toInt(),
                    names = names3,
                    values = values3
                ) {
                    sendIndexedParameter(VKAg.APTYPE_GS_LOST_ACT, it)
                }
            }
        }
        //智能断药点
        item {
            FrameColumn {
                SwitchButtonRow(
                    title = R.string.intelligent_best_route,
                    defaultChecked = config.smartPlan,
                ) {
                    config.smartPlan = it
                }
                HorizontalDivider(
                    thickness = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                IntelligentStopDrugRow()
            }
        }
    }
}

@Composable
fun IntelligentStopDrugRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 5.dp)
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 5.dp, start = 10.dp)
                    .background(
                        color = Color.Transparent, shape = MaterialTheme.shapes.extraSmall
                    )
                    .fillMaxHeight()
                    .width(100.dp), contentAlignment = Alignment.TopStart
            ) {
                val text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize
                        )
                    ) {
                        append(stringResource(id = R.string.intelligent_stop_drug_before))
                    }
                    append("\n")
                    withStyle(
                        style = SpanStyle(
                            color = Color.LightGray,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                    ) {
                        append(stringResource(id = R.string.intelligent_stop_drug_before_info))
                    }
                }
                Text(
                    text = text,
                    modifier = Modifier,
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_replan_before),
                    contentDescription = null,
                    modifier = Modifier.size(rockerImageSize)
                )
            }

        }
        VerticalDivider(
            modifier = Modifier
                .width(1.dp)
                .height(80.dp) // 设置竖线的高度
                .background(Color.White), // 设置竖线的颜色
        )
        Box(
            modifier = modifier
                .fillMaxSize()
                .weight(1f), contentAlignment = Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 5.dp, start = 10.dp)
                    .background(
                        color = Color.Transparent, shape = MaterialTheme.shapes.extraSmall
                    )
                    .fillMaxHeight()
                    .width(100.dp), contentAlignment = Alignment.TopStart
            ) {
                val text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize
                        )
                    ) {
                        append(stringResource(id = R.string.intelligent_stop_drug_after))
                    }
                    append("\n")
                    withStyle(
                        style = SpanStyle(
                            color = Color.LightGray,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                    ) {
                        append(stringResource(id = R.string.intelligent_stop_drug_after_info))
                    }
                }
                Text(
                    text = text,
                    modifier = Modifier,
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.default_replan_after),
                    contentDescription = null,
                    modifier = Modifier.size(rockerImageSize)
                )
            }

        }
    }
}

/**
 * 传感器校准行
 */
//@Composable
//fun SensorCalibrationRow(modifier: Modifier = Modifier, context: Context) {
//    Row(
//        modifier = modifier
//            .height(settingsGlobalRowHeight)
//            .padding(horizontal = settingsGlobalPaddingHorizontal),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        Box(modifier = Modifier.width(settingsGlobalTitleWidth)) {
//            SettingsGlobalRowText(text = stringResource(id = R.string.sensor_calibration))
//        }
//        Spacer(modifier = Modifier.weight(1f))
//        Row(
//            modifier = Modifier.width(settingsGlobalButtonComponentWidth),
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .height(settingsGlobalButtonHeight)
//                    .weight(1f)
//                    .background(
//                        color = MaterialTheme.colorScheme.primary,
//                        shape = MaterialTheme.shapes.small,
//                    )
//                    .clickable {
//                        (context as MapVideoActivity).let {
//                            it.showDialog {
//                                MagneticCompassCalibrationPopup(it)
//                            }
//                        }
//                    }, contentAlignment = Alignment.Center
//            ) {
//                AutoScrollingText(
//                    text = stringResource(id = R.string.magnetic_compass_calibration),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    style = MaterialTheme.typography.labelLarge,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//            Box(
//                modifier = Modifier
//                    .height(settingsGlobalButtonHeight)
//                    .weight(1f)
//                    .background(
//                        color = MaterialTheme.colorScheme.primary,
//                        shape = MaterialTheme.shapes.small
//                    )
//                    .clickable {
//                        (context as MapVideoActivity).let {
//                            it.showDialog {
//                                HorizontalCalibrationPopup(it)
//                            }
//                        }
//                    }, contentAlignment = Alignment.Center
//            ) {
//                AutoScrollingText(
//                    text = stringResource(id = R.string.horizontal_calibration),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    style = MaterialTheme.typography.labelLarge,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
//    }
//
//}


/**
 * 在位传感器行
 */
@Composable
fun InPlaceSensorRow(
    modifier: Modifier = Modifier, buttonEnableList: List<Boolean>, switchCheck: Boolean = false,
) {
    val names = listOf(
        stringResource(id = R.string.in_place_sensor_m1),
        stringResource(id = R.string.in_place_sensor_m2),
        stringResource(id = R.string.in_place_sensor_m3),
        stringResource(id = R.string.in_place_sensor_m4)
    )

    Row(
        modifier = modifier
            .height(settingsGlobalRowHeight)
            .padding(horizontal = settingsGlobalPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(titleWeight)) {
            SettingsGlobalRowText(text = stringResource(id = R.string.in_place_sensor))
        }
        Row(
            modifier = Modifier.weight(valueWeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for ((i, name) in names.withIndex()) {
                    val enable = buttonEnableList[i]
                    val boxColor = if (enable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    val fontColor = if (enable) MaterialTheme.colorScheme.onPrimary else Color.White
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(settingsGlobalButtonHeight)
                            .background(
                                color = boxColor, shape = MaterialTheme.shapes.small
                            ), contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = name,
                            color = fontColor,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Box(
                modifier = Modifier, contentAlignment = Alignment.CenterEnd
            ) {
                SwitchButton(
                    defaultChecked = switchCheck,
                    width = switchButtonWidth,
                    height = settingsGlobalButtonHeight,
                    onCheckedChange = {
                        sendIndexedParameter(VKAg.APTYPE_BOOM_WARN_SWITCH, if (it) 1 else 0)
                    })
            }
        }
    }
}