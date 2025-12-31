package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg

/**
 * 雷达设置
 */
@Composable
fun RadarSettings(modifier: Modifier = Modifier) {
    val aptypeData = DroneModel.aptypeData.observeAsState(initial = null)
    val s = aptypeData.value?.getIntValue(VKAg.APTYPE_SWITCHER)
    var gRadarOpen = false
    var fRadarOpen = false
    var hRadarOpen = false
    if (s != null) {
        gRadarOpen = s and 0x1 == 1//仿地开关
        fRadarOpen = (s and 0x2) != 0//前避障开关
        hRadarOpen = (s and 0x4) != 0//手动控制高度
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(settingsGlobalColumnSpacer)
    ) {

        item {
            FrameColumn {
                //避障雷达 0-关，1-开
                SwitchButtonRow(
                    title = R.string.obstacle_avoidance_radar, defaultChecked = fRadarOpen
                ) {
                    fRadarOpen = it
                    val s1 = if (gRadarOpen) 1 else 0//仿地开关
                    val s2 = if (fRadarOpen) 1 else 0//雷达开关
                    val s3 = if (hRadarOpen) 1 else 0//高精度监测
                    val v = s1 or (s2 shl 1) or (s3 shl 2)
                    DroneModel.activeDrone?.sendIndexedParameter(VKAg.APTYPE_SWITCHER, v)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //避障雷达探测距离 1 ~ 20
                CounterRow(
                    title = R.string.obstacle_avoidance_radar_detection_distance,
                    counterType = COUNTER_TYPE_INT,
                    intMin = 2,
                    intMax = 20,
                    intStep = 1,
                    intDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_OBSTACLE_DIST)
                        ?.toInt()
                        ?: 10
                ) {
                    DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_OBSTACLE_DIST, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //探测到障碍物 1:悬停 2:绕行(暂时无法实现)
                val names = listOf(
                    stringResource(id = R.string.stop_drug_protect_hover),
                    stringResource(id = R.string.stop_drug_protect_detour),
                )
                val values = listOf(1, 2)
                GroupButtonRow(
                    title = R.string.detected_obstacles,
                    names = names,
                    values = values,
                    defaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_BIZHANG)?.toInt() ?: 0
                ) {
                    DroneModel.activeDrone?.sendIndexedParameter(VKAg.APTYPE_BIZHANG, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //前避障雷达灵敏度
                CounterRow(
                    title = R.string.start_obstacle_avoidance_radar_sensitivity,
                    counterType = COUNTER_TYPE_INT,
                    intMin = 1,
                    intMax = 100,
                    intStep = 5,
                    intDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_SENSE_F)?.toInt() ?: 50
                ) {
                    DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_SENSE_F, it)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //后避障雷达灵敏度
                CounterRow(
                    title = R.string.end_obstacle_avoidance_radar_sensitivity,
                    counterType = COUNTER_TYPE_INT,
                    intMin = 1,
                    intMax = 100,
                    intStep = 5,
                    intDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_SENSE_B)?.toInt()
                        ?: 50
                ) {
                    DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_SENSE_B, it)
                }
            }
        }

        item {
            FrameColumn {
                //仿地雷达
                SwitchButtonRow(
                    title = R.string.ground_simulation_radar, defaultChecked = gRadarOpen
                ) {
                    gRadarOpen = it
                    val s1 = if (gRadarOpen) 1 else 0//仿地开关
                    val s2 = if (fRadarOpen) 1 else 0//雷达开关
                    val s3 = if (hRadarOpen) 1 else 0//高精度监测
                    val v = s1 or (s2 shl 1) or (s3 shl 2)
                    DroneModel.activeDrone?.sendIndexedParameter(VKAg.APTYPE_SWITCHER, v)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                SwitchButtonRow(
                    title = R.string.assisted_landing, defaultChecked = (aptypeData.value?.getValue(VKAg.APTYPE_ASSISTED_LANDING) == 1f)
                ) {
                    sendParameter(VKAg.APTYPE_ASSISTED_LANDING, if (it) 1f else 0f)
                }
                Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                //仿地雷达灵敏度 0 ~ 100 0为关闭 所以1 ~ 100
                CounterRow(
                    title = R.string.ground_simulation_radar_sensitivity,
                    counterType = COUNTER_TYPE_INT,
                    intMin = 1,
                    intMax = 100,
                    intStep = 5,
                    intDefaultNumber = aptypeData.value?.getValue(VKAg.APTYPE_RADAR_WEIGHT)?.toInt()
                        ?: 50
                ) {
                    DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_RADAR_WEIGHT, it)
                }
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Composable
fun RadarSettingsPreview(modifier: Modifier = Modifier) {
    ComposeTheme {
        RadarSettings(modifier = modifier)
    }
}