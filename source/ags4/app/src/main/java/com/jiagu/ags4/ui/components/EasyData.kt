package com.jiagu.ags4.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog

/**
 * 获取数据按钮
 * 仅测试使用
 */
@Composable
fun EasyDataButtons() {
    val context = LocalContext.current
    val buttonWidth = 40.dp
    val buttonHeight = 30.dp
    Row(
        modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            //工厂模式
            Button(
                onClick = {
                    context.showDialog {
                        FactoryData(onClose = {
                            context.hideDialog()
                        })
                    }
                },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "工厂")
            }
            //设置
            Button(
                onClick = {
                    context.showDialog {
                        SettingData(onClose = {
                            context.hideDialog()
                        })
                    }
                },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "设置")
            }

            //协议
            Button(
                onClick = {
                    context.showDialog {
                        ProtocolData(onClose = {
                            context.hideDialog()
                        })
                    }
                },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "协议")
            }
        }
    }
}

/**
 * 工厂模式数据
 */
@Composable
fun FactoryData(onClose: () -> Unit) {
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val pid by DroneModel.pidData.observeAsState()
    val controllerType by DroneModel.controllerType.observeAsState()

    /**
     * 机型设置
     */
    val mode = aptypeData?.getIntValue(VKAg.APTYPE_MODEL)
    val modeNames = stringArrayResource(id = R.array.advanced_setting_model_type_titles)
    val modeName = when (mode) {
        42 -> modeNames[0]
        41 -> modeNames[1]
        62 -> modeNames[2]
        61 -> modeNames[3]
        82 -> modeNames[4]
        81 -> modeNames[5]
        63 -> modeNames[6]
        64 -> modeNames[7]
        83 -> modeNames[8]
        84 -> modeNames[9]
        46 -> modeNames[10]
        48 -> modeNames[11]
        66 -> modeNames[12]
        67 -> modeNames[13]
        68 -> modeNames[14]
        else -> "null"
    }

    /**
     * 安装设置
     */
    //飞控安装
    val fcValue = aptypeData?.getIntValue(VKAg.APTYPE_SETUP_DIR)
    val fcX = aptypeData?.getIntValue(63)
    val fcY = aptypeData?.getIntValue(64)
    val fcNames = stringArrayResource(id = R.array.flight_control_installation_method)
    val fcName = when (fcValue) {
        1 -> fcNames[0]
        4 -> fcNames[1]
        2 -> fcNames[2]
        3 -> fcNames[3]
        5 -> fcNames[4]
        else -> "null"
    }
    //GNSS安装
    val gnssX = aptypeData?.getIntValue(59)
    val gnssY = aptypeData?.getIntValue(60)
    //RTK安装
    val rtkValue = aptypeData?.getIntValue(VKAg.APTYPE_RTK_POS)
    val rtkX = aptypeData?.getIntValue(VKAg.APTYPE_RTK_X)
    val rtkY = aptypeData?.getIntValue(VKAg.APTYPE_RTK_Y)
    val rtkNames = stringArrayResource(id = R.array.rtk_installation_method)
    val rtkName = when (rtkValue) {
        0 -> rtkNames[0]
        1 -> rtkNames[1]
        2 -> rtkNames[2]
        3 -> rtkNames[3]
        else -> "null"
    }
    //雷达安装
    val radarBefore = aptypeData?.getIntValue(57)
    val radarAfter = aptypeData?.getIntValue(58)
    val radarBottom = aptypeData?.getIntValue(73)

    /**
     * 参数设置
     */
    //滤波带宽设置
    val param1 = aptypeData?.getIntValue(VKAg.APTYPE_NOISE_SUPPRESSION)
    val param1Value = when (param1) {
        0 -> "3"
        1 -> "2"
        2 -> "1"
        5 -> "5"
        6 -> "4"
        else -> "null"
    }
    //电机解锁阈值
    val param2 = aptypeData?.getIntValue(VKAg.APTYPE_IDLE_SPEED)
    val param2names = stringArrayResource(id = R.array.motor_idle)
    val param2Value = when (param2) {
        1100 -> param2names[0]
        1150 -> param2names[1]
        1200 -> param2names[2]
        1250 -> param2names[3]
        1300 -> param2names[4]
        else -> "null"
    }
    //感度设置
    //姿态自稳
    val rBase = pid?.getValue(VKAg.PID_R_BASE)?.toInt()
    //姿态感度
    val rZitai = pid?.getValue(VKAg.PID_R_ZITAI)?.toInt()
    //姿态阻尼
    val rStable = pid?.getValue(VKAg.PID_R_STABLE)?.toInt()
    //航向自稳
    val yBase = pid?.getValue(VKAg.PID_Y_BASE)?.toInt()
    //航向感度
    val yZitai = pid?.getValue(VKAg.PID_Y_ZITAI)?.toInt()
    //航向阻尼
    val yStable = pid?.getValue(VKAg.PID_Y_STABLE)?.toInt()
    //垂直感度
    val vSpeed = pid?.getValue(VKAg.PID_V_SPEED)?.toInt()
    //垂直阻尼
    val vAccel = pid?.getValue(VKAg.PID_V_ACCEL_P)?.toInt()
    //水平感度
    val hSpeed = pid?.getValue(VKAg.PID_H_SPEED)?.toInt()
    //水平阻尼
    val hAccel = pid?.getValue(VKAg.PID_H_ACCEL_P)?.toInt()
    //定位增强
    val position = aptypeData?.getIntValue(53)
    val positionValue = if (position == null) {
        "null"
    } else {
        if (position > 0) {
            "开"
        } else {
            "关"
        }
    }
    //摇杆模式
    val rockerMode = DroneModel.rcModeIndex(controllerType ?: "")
    val rockerNnames: List<String> = listOf(
        stringResource(id = R.string.rocker_mode_hand_jp),
        stringResource(id = R.string.rocker_mode_hand_us),
        stringResource(id = R.string.rocker_mode_hand_cn)
    )
    val rockerValue = when (rockerMode) {
        0 -> rockerNnames[0]
        1 -> rockerNnames[1]
        2 -> rockerNnames[2]
        else -> "null"
    }
    MainContent(title = "工厂", breakAction = {
        onClose()
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //机型设置
            item {
                Text(text = "机型设置: param = ${mode}, paramName = ${modeName}")
            }
            //飞控安装
            item {
                Text(text = "飞控安装")
                Text(text = "安装方向: param = ${fcValue}, paramName = ${fcName}")
                Text(text = "偏差设置: paramX = ${fcX}, paramY = ${fcY}")
            }
            //GNSS安装
            item {
                Text(text = "GNSS安装")
                Text(text = "偏差设置: paramX = ${gnssX}, paramY = ${gnssY}")
            }
            //RTK安装
            item {
                Text(text = "RTK安装")
                Text(text = "安装方向: param = ${rtkValue}, paramName = ${rtkName}")
                Text(text = "偏差设置: paramX = ${rtkX}, paramY = ${rtkY}")
            }
            //雷达安装
            item {
                Text(text = "雷达安装")
                Text(text = "前避障雷达灵敏度: ${radarBefore}, 后避障雷达灵敏度: ${radarAfter}, 仿地雷达灵敏度: ${radarBottom}")
            }
            //参数设置
            item {
                Text(text = "滤波带宽设置: param = ${param1}, paramName = ${param1Value}")
                Text(text = "电机解锁阈值: param = ${param2}, paramName = ${param2Value}")
            }
            //感度设置
            item {
                Text(text = "感度设置")
                Text(text = "姿态自稳: ${rBase}, 姿态感度: ${rZitai}, 姿态阻尼: ${rStable}")
                Text(text = "航向自稳: ${yBase}, 航向感度: ${yZitai}, 航向阻尼: ${yStable}")
                Text(text = "垂直感度: ${vSpeed}, 垂直阻尼: ${vAccel}")
                Text(text = "水平感度: ${hSpeed}, 水平阻尼: ${hAccel}")
                Text(text = "定位增强: param = ${position}, paramName = ${positionValue}")
            }
            //遥控设置
            item {
                Text(text = "摇杆模式: param = ${controllerType}, paramName = ${rockerValue}")
            }
        }
    }

}

/**
 * 设置数据
 */
@Composable
fun SettingData(onClose: () -> Unit) {
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val deviceBoomSensorData = DroneModel.deviceBoomSensorData.observeAsState()
    val hydrogenBattery1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBattery2 by DroneModel.hydrogenBatteryData2.observeAsState()

    /**
     * 飞行设置
     */
    //手动控制高度
    val p1Names = listOf(
        stringResource(id = R.string.manual_control_height_close),
        stringResource(id = R.string.manual_control_height_controlled),
        stringResource(id = R.string.manual_control_height_automatic)
    )
    val p1 = aptypeData?.getValue(VKAg.APTYPE_GAODUKEKONG)?.toInt()
    val p1Value = when (p1) {
        1 -> p1Names[0]
        2 -> p1Names[1]
        3 -> p1Names[2]
        else -> "null"
    }
    //手动控制方向
    val p2Names = listOf(
        stringResource(id = R.string.close), stringResource(id = R.string.open)
    )
    val p2 = aptypeData?.getValue(VKAg.APTYPE_HANGXIANGKEKONG)?.toInt()
    val p2Value = when (p2) {
        1 -> p2Names[0]
        2 -> p2Names[1]
        else -> "null"
    }
    //机头方向
    val p3Names = listOf(
        stringResource(id = R.string.head_lock), stringResource(id = R.string.head_following)
    )
    val p3 = aptypeData?.getValue(VKAg.APTYPE_HEAD_TYPE)?.toInt()
    val p3Value = when (p3) {
        1 -> p3Names[0]
        2 -> p3Names[1]
        else -> "null"
    }
    //转弯方式
    val p4Names = listOf(
        stringResource(id = R.string.right_angle_turn),
        stringResource(id = R.string.u_turn),
        stringResource(id = R.string.conventional_turning)
    )
    val p4 = aptypeData?.getValue(VKAg.APTYPE_TURN_TYPE)?.toInt()
    val p4Value = when (p4) {
        0 -> p4Names[0]
        1 -> p4Names[1]
        2 -> p4Names[2]
        else -> "null"
    }
    //机臂锁定传感器
    var p5ButtonsEnabled: List<Boolean> = listOf(false, false, false, false)
    deviceBoomSensorData.value?.let {
        p5ButtonsEnabled = listOf(
            ((it.warn shr 0) and 0x1 == 0),
            ((it.warn shr 1) and 0x1 == 0),
            ((it.warn shr 2) and 0x1 == 0),
            ((it.warn shr 3) and 0x1 == 0)
        )
    }
    val p5Switch = aptypeData?.getIntValue(VKAg.APTYPE_BOOM_WARN_SWITCH)
    val p5SwitchValue = if (p5Switch == null) {
        "null"
    } else {
        if (p5Switch == 1) {
            "开"
        } else {
            "关"
        }
    }
    //起航/返航高度
    val p6 = aptypeData?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT)?.toInt()
    //起航/返航速度
    val p7 = aptypeData?.getValue(VKAg.APTYPE_START_END_SPEED)
    //最远飞行距离
    val p8 = aptypeData?.getValue(VKAg.APTYPE_FENCE_RAIDUS)?.toInt()
    //最高飞行高度
    val p9 = aptypeData?.getValue(VKAg.APTYPE_FENCE_HEIGHT)?.toInt()
    //最大速度
    val p10 = aptypeData?.getValue(VKAg.APTYPE_MAX_HSPEED)?.toInt()
    //失联后继续作业
    val p11 = aptypeData?.getValue(VKAg.APTYPE_CONTROLLER_LOST_WORK)?.toInt()
    val p11Value = when (p11) {
        1 -> "开"
        else -> "关"
    }
    //作业完成动作
    val p12Names = listOf(
        stringResource(id = R.string.homework_completion_action_hover),
        stringResource(id = R.string.homework_completion_action_returning)
    )
    val p12 = aptypeData?.getValue(VKAg.APTYPE_DONE_ACT)?.toInt()
    val p12Value = when (p12) {
        0 -> p12Names[0]
        1 -> p12Names[1]
        else -> "null"
    }
    //探照灯开关
    val p13 = ((aptypeData?.getIntValue(VKAg.APTYPE_LED_SWITCH)?.shr(3))?.and(0x1))
    val p13Value = when (p13) {
        1 -> "开"
        else -> "关"
    }
    //探照灯亮度
    val p14 = aptypeData?.getValue(VKAg.APTYPE_LED_STRENGTH)?.toInt()
    //断药保护
    val p15Names = listOf(
        stringResource(id = R.string.stop_drug_protect_close),
        stringResource(id = R.string.stop_drug_protect_hover),
        stringResource(id = R.string.stop_drug_protect_returning)
    )
    val p15 = aptypeData?.getValue(VKAg.APTYPE_DRUG_ACT)?.toInt()
    val p15Value = when (p15) {
        1 -> p15Names[0]
        2 -> p15Names[1]
        3 -> p15Names[2]
        else -> "null"
    }
    //遥控器失效保护
    val p16Names = listOf(
        stringResource(id = R.string.remote_control_invalid_protect_hover),
        stringResource(id = R.string.remote_control_invalid_protect_returning)
    )
    val p16 = aptypeData?.getValue(VKAg.APTYPE_RC_LOST_ACT)?.toInt()
    val p16Value = when (p16) {
        1 -> p16Names[0]
        3 -> p16Names[1]
        else -> "null"
    }

    /**
     * 喷洒设置
     */
    //喷洒系统
    val p17 = aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_SEED_SWITCH)
    val p17Value = when (p17) {
        1 -> "开"
        else -> "关"
    }
    //喷头模式
    val p18 = aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_NOZZLE_MODE)
    val p18Names = stringArrayResource(id = R.array.seed_mode)
    val p18Value = when (p18) {
        0 -> p18Names[0]
        1 -> p18Names[1]
        else -> "null"
    }
    //四喷头开关
    val p19 = aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_NOZZLE_MODE)
    val p19Names = stringArrayResource(id = R.array.seed_mode_four_switch)
    val p19Value = when (p19) {
        2 -> p19Names[0]
        1 -> p19Names[1]
        else -> "null"
    }
    //喷洒断药类型
    val p20Names = listOf(
        stringResource(id = R.string.spray_stop_drug_flowmeter),
        stringResource(id = R.string.spray_stop_drug_weighing_module),
        stringResource(id = R.string.spray_stop_drug_liquid_level_gauge)
    )
    val p20 = (aptypeData?.getIntValue(VKAg.APTYPE_DRUG_TYPE) ?: 0) and 0xFF
    val p20Value = when (p20) {
        1 -> p20Names[0]
        4 -> p20Names[1]
        2 -> p20Names[2]
        else -> "null"
    }

    /**
     * 播撒设置
     */
    //播撒系统
    val p21 = aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_SEED_SWITCH)
    val p21Value = when (p21) {
        1 -> "开"
        else -> "关"
    }
    //断药类型
    val p22Names = listOf(
        stringResource(id = R.string.seed_stop_drug_broken_material_radar),
        stringResource(id = R.string.seed_stop_drug_weighing_module)
    )
    val p22 = ((aptypeData?.getIntValue(VKAg.APTYPE_DRUG_TYPE) ?: 0) shr 8) and 0xFF
    val p22Value = when (p22) {
        1 -> p22Names[0]
        2 -> p22Names[1]
        else -> "null"
    }

    /**
     * 雷达设置
     */
    val s = aptypeData?.getIntValue(VKAg.APTYPE_SWITCHER)
    var gRadarOpen = false
    var fRadarOpen = false
    var hRadarOpen = false
    if (s != null) {
        gRadarOpen = s and 0x1 == 1//仿地开关
        fRadarOpen = (s and 0x2) != 0//前避障开关
        hRadarOpen = (s and 0x4) != 0//手动控制高度
    }
    //避障雷达
    val p23 = fRadarOpen
    val p23Value = if (p23) {
        "开"
    } else {
        "关"
    }
    //避障雷达探测距离
    val p24 = aptypeData?.getValue(VKAg.APTYPE_OBSTACLE_DIST)?.toInt()
    //探测到障碍物
    val p25 = aptypeData?.getValue(VKAg.APTYPE_BIZHANG)?.toInt()
    val p25Value = "悬停"
    //前避障雷达灵敏度
    val p26 = aptypeData?.getValue(VKAg.APTYPE_SENSE_F)?.toInt()
    //后避障雷达灵敏度
    val p27 = aptypeData?.getValue(VKAg.APTYPE_SENSE_B)?.toInt()
    //仿地雷达
    val p28 = gRadarOpen
    val p28Value = if (p28) {
        "开"
    } else {
        "关"
    }
    //仿地雷达灵敏度
    val p29 = aptypeData?.getValue(VKAg.APTYPE_RADAR_WEIGHT)?.toInt()
    MainContent(title = "设置", breakAction = {
        onClose()
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //飞行设置
            item {
                Text(text = "飞行设置")
                Text(text = "手动控制高度: param = ${p1}, paramName = ${p1Value}")
                Text(text = "手动控制方向: param = ${p2}, paramName = ${p2Value}")
                Text(text = "机头方向: param = ${p3}, paramName = ${p3Value}")
                Text(text = "转弯方式: param = ${p4}, paramName = ${p4Value}")
                Text(text = "机臂锁定传感器: M1 = ${p5ButtonsEnabled[0]}, M2 = ${p5ButtonsEnabled[1]}, M3 = ${p5ButtonsEnabled[2]}, switch param = ${p5Switch}, switch paramValue = ${p5SwitchValue}")
                Text(text = "起航/返航高度: ${p6}")
                Text(text = "起航/返航速度: ${p7}")
                Text(text = "最远飞行距离: ${p8}")
                Text(text = "最高飞行高度: ${p9}")
                Text(text = "最大速度: ${p10}")
                Text(text = "失联后继续作业: param = ${p11}, paramName = ${p11Value}")
                Text(text = "作业完成动作: param = ${p12}, paramName = ${p12Value}")
                Text(text = "探照灯开关: param = ${p13}, paramName = ${p13Value}")
                Text(text = "探照灯亮度: ${p14}")
                Text(text = "断药保护: param = ${p15}, paramName = ${p15Value}")
                Text(text = "遥控器失效保护: param = ${p16}, paramName = ${p16Value}")
            }
            //喷洒设置
            item {
                Text(text = "喷洒设置")
                Text(text = "喷洒系统: param = ${p17}, paramName = ${p17Value}")
                Text(text = "喷头模式: param = ${p18}, paramName = ${p18Value}")
                Text(text = "四喷头开关: param = ${p19}, paramName = ${p19Value}")
                Text(text = "断药类型: param = ${p20}, paramName = ${p20Value}")
            }
            //播撒设置
            item {
                Text(text = "播撒设置")
                Text(text = "播撒系统: param = ${p21}, paramName = ${p21Value}")
                Text(text = "断药类型: param = ${p22}, paramName = ${p22Value}")
            }
            //雷达设置
            item {
                Text(text = "雷达设置")
                Text(text = "避障雷达: param = ${p23}, paramName = ${p23Value}")
                Text(text = "避障雷达探测距离: ${p24}")
                Text(text = "探测到障碍物: param = ${p25}, paramName = ${p25Value}")
                Text(text = "前避障雷达灵敏度: ${p26}")
                Text(text = "后避障雷达灵敏度: ${p27}")
                Text(text = "仿地雷达: param = ${p28}, paramName = ${p28Value}")
                Text(text = "仿地雷达灵敏度: ${p29}")
            }
            //氢能源电池
            item {
                Text(text = "氢能源电池")
                Text(text = "电池1")
                Text(
                    text = "连接状态:${hydrogenBattery1?.connectionStatus ?: ""}," +
                            "厂商:${hydrogenBattery1?.manufacturer ?: ""}," +
                            "电池电压:${hydrogenBattery1?.batteryVoltage ?: ""}," +
                            "电机电流:${hydrogenBattery1?.motorCurrent ?: ""}," +
                            "电堆电压:${hydrogenBattery1?.stackVoltage ?: ""}," +
                            "电机电压:${hydrogenBattery1?.motorVoltage ?: ""}," +
                            "电池补能电流:${hydrogenBattery1?.rechargeCurrent ?: ""}," +
                            "气罐压力:${hydrogenBattery1?.gasCylinderPressure ?: ""}," +
                            "管道压力:${hydrogenBattery1?.pipelinePressure ?: ""}," +
                            "主板温度:${hydrogenBattery1?.mainBoardTemperature ?: ""}," +
                            "电堆温度:${hydrogenBattery1?.stackTemperature ?: ""}," +
//                        "设备状态:${hydrogenBattery1?.batteryStatus ?: ""}," +
                            "报警状态:${hydrogenBattery1?.status ?: ""}" +
                            "自检状态:${hydrogenBattery1?.selfCheckStatus ?: ""}"
                )
                Text(text = "电池2")
                Text(
                    text = "连接状态:${hydrogenBattery2?.connectionStatus ?: ""}," +
                            "厂商:${hydrogenBattery2?.manufacturer ?: ""}," +
                            "电池电压:${hydrogenBattery2?.batteryVoltage ?: ""}," +
                            "电机电流:${hydrogenBattery2?.motorCurrent ?: ""}," +
                            "电堆电压:${hydrogenBattery2?.stackVoltage ?: ""}," +
                            "电机电压:${hydrogenBattery2?.motorVoltage ?: ""}," +
                            "电池补能电流:${hydrogenBattery2?.rechargeCurrent ?: ""}," +
                            "气罐压力:${hydrogenBattery2?.gasCylinderPressure ?: ""}," +
                            "管道压力:${hydrogenBattery2?.pipelinePressure ?: ""}," +
                            "主板温度:${hydrogenBattery2?.mainBoardTemperature ?: ""}," +
                            "电堆温度:${hydrogenBattery2?.stackTemperature ?: ""}," +
                            "设备状态:${hydrogenBattery2?.booting ?: ""},${hydrogenBattery2?.charging ?: ""},${hydrogenBattery2?.discharging ?: ""}," +
                            "报警状态:${hydrogenBattery2?.status ?: ""}" +
                            "自检状态:${hydrogenBattery2?.selfCheckStatus ?: ""}"
                )
            }
        }
    }
}

/**
 * 设置数据
 */
@Composable
fun ProtocolData(onClose: () -> Unit) {
    val homeData by DroneModel.homeData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val pwmData by DroneModel.pwmData.observeAsState()
    val motorData by DroneModel.motorData.observeAsState()
    val rcafData by DroneModel.rcafData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val pidData by DroneModel.pidData.observeAsState()
    val flowData by DroneModel.flowData.observeAsState()
    val engineData by DroneModel.engineData.observeAsState()
    val batteryData by DroneModel.batteryData.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val abData by DroneModel.abData.observeAsState()
    val abplData by DroneModel.abplData.observeAsState()
    val rssiData by DroneModel.rssiData.observeAsState()
    val verData by DroneModel.verData.observeAsState()
    val pumpData by DroneModel.pumpData.observeAsState()
    val deviceData by DroneModel.deviceData.observeAsState()
    val sortieAreaData by DroneModel.sortieAreaData.observeAsState()
    val workAreaData by DroneModel.workAreaData.observeAsState()
    val posData by DroneModel.posData.observeAsState()
    val warnData by DroneModel.warnData.observeAsState()
    val breakPoint by DroneModel.breakPoint.observeAsState()
    val seedData by DroneModel.seedData.observeAsState()
    val ecuData by DroneModel.ecuData.observeAsState()
    val wlData by DroneModel.wlData.observeAsState()
    val auxData by DroneModel.auxData.observeAsState()
    val deviceList by DroneModel.deviceList.observeAsState()
    val deviceSeedData by DroneModel.deviceSeedData.observeAsState()
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val devicePumpData by DroneModel.devicePumpData.observeAsState()
    val deviceCentrifugalData by DroneModel.deviceCentrifugalData.observeAsState()
    val deviceBoomSensorData by DroneModel.deviceBoomSensorData.observeAsState()
    val deviceMaterialSensorData by DroneModel.deviceMaterialSensorData.observeAsState()
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    val deviceRTKData by DroneModel.deviceRTKData.observeAsState()
    val deviceGPSData by DroneModel.deviceGPSData.observeAsState()
    val deviceTerrainData by DroneModel.deviceTerrainData.observeAsState()
    val deviceRadarData by DroneModel.deviceRadarData.observeAsState()
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    val newWarnListData by DroneModel.newWarnListData.observeAsState()
    val newDevInfoData by DroneModel.newDevInfoData.observeAsState()
    val sortieListData by DroneModel.sortieListData.observeAsState()
    val radarGraphData by DroneModel.radarGraphData.observeAsState()
    val canGALVInfo by DroneModel.canGALVInfo.observeAsState()
    val remoteIdData by DroneModel.remoteIdData.observeAsState()
    MainContent(title = "协议", breakAction = {
        onClose()
    }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(text = "01: idList: ${Gson().toJson(deviceList)}")
            }
            item {
                Text(text = "02: verData: ${Gson().toJson(verData)}")
            }
            item {
                Text(text = "03: homeData: ${Gson().toJson(homeData)}")
            }
            item {
                Text(text = "04: imuData: ${Gson().toJson(imuData)}")
            }
            item {
                Text(text = "05: pwmData: ${Gson().toJson(pwmData)}")
            }
            item {
                Text(text = "06: motorData: ${Gson().toJson(motorData)}")
            }
            item {
                Text(text = "07: rcafData: ${Gson().toJson(rcafData)}")
            }
            item {
                Text(text = "08: aptypeData: ${Gson().toJson(aptypeData)}")
            }
            item {
                Text(text = "09: pidData: ${Gson().toJson(pidData)}")
            }
            item {
                Text(text = "10: flowData: ${Gson().toJson(flowData)}")
            }
            item {
                Text(text = "11: engineData: ${Gson().toJson(engineData)}")
            }
            item {
                Text(text = "12: batteryData: ${Gson().toJson(batteryData)}")
            }
            item {
                Text(text = "13: hydrogenBatteryData1: ${Gson().toJson(hydrogenBatteryData1)}")
            }
            item {
                Text(text = "14: hydrogenBatteryData2: ${Gson().toJson(hydrogenBatteryData2)}")
            }
            item {
                Text(text = "15: abData: ${Gson().toJson(abData)}")
            }
            item {
                Text(text = "16: abplData: ${Gson().toJson(abplData)}")
            }
            item {
                Text(text = "17: rssiData: ${Gson().toJson(rssiData)}")
            }
            item {
                Text(text = "18: pumpData: ${Gson().toJson(pumpData)}")
            }
            item {
                Text(text = "19: deviceData: ${Gson().toJson(deviceData)}")
            }
            item {
                Text(text = "20: sortieAreaData: ${Gson().toJson(sortieAreaData)}")
            }
            item {
                Text(text = "21: workAreaData: ${Gson().toJson(workAreaData)}")
            }
            item {
                Text(text = "22: posData: ${Gson().toJson(posData)}")
            }
            item {
                Text(text = "23: warnData: ${Gson().toJson(warnData)}")
            }
            item {
                Text(text = "24: breakPoint: ${Gson().toJson(breakPoint)}")
            }
            item {
                Text(text = "25: seedData: ${Gson().toJson(seedData)}")
            }
            item {
                Text(text = "26: ecuData: ${Gson().toJson(ecuData)}")
            }
            item {
                Text(text = "27: wlData: ${Gson().toJson(wlData)}")
            }
            item {
                Text(text = "28: auxData: ${Gson().toJson(auxData)}")
            }
            item {
                Text(text = "29: deviceSeedData: ${Gson().toJson(deviceSeedData)}")
            }
            item {
                Text(text = "30: deviceWeightData: ${Gson().toJson(deviceWeightData)}")
            }
            item {
                Text(text = "31: devicePumpData: ${Gson().toJson(devicePumpData)}")
            }
            item {
                Text(text = "32: deviceCentrifugalData: ${Gson().toJson(deviceCentrifugalData)}")
            }
            item {
                Text(text = "33: deviceBoomSensorData: ${Gson().toJson(deviceBoomSensorData)}")
            }
            item {
                Text(text = "34: deviceMaterialSensorData: ${Gson().toJson(deviceMaterialSensorData)}")
            }
            item {
                Text(text = "35: deviceFlowData: ${Gson().toJson(deviceFlowData)}")
            }
            item {
                Text(text = "36: deviceRTKData: ${Gson().toJson(deviceRTKData)}")
            }
            item {
                Text(text = "37: deviceGPSData: ${Gson().toJson(deviceGPSData)}")
            }
            item {
                Text(text = "38: deviceTerrainData: ${Gson().toJson(deviceTerrainData)}")
            }
            item {
                Text(text = "39: deviceRadarData: ${Gson().toJson(deviceRadarData)}")
            }
            item {
                Text(text = "40: deviceLinePumpData: ${Gson().toJson(deviceLinePumpData)}")
            }
            item {
                Text(text = "41: newWarnListData: ${Gson().toJson(newWarnListData)}")
            }
            item {
                Text(text = "42: newDevInfoData: ${Gson().toJson(newDevInfoData)}")
            }
            item {
                Text(text = "43: sortieListData: ${Gson().toJson(sortieListData)}")
            }
            item {
                Text(text = "44: radarGraphData: ${Gson().toJson(radarGraphData)}")
            }
            item {
                Text(text = "46: canGALVInfo: ${Gson().toJson(canGALVInfo)}")
            }
            item {
                Text(text = "47: remoteIdData: ${Gson().toJson(remoteIdData)}")
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun EasyDataButton() {
    ComposeTheme {
        EasyDataButtons()
    }
}