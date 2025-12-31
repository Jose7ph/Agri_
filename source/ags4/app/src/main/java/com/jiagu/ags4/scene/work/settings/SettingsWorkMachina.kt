package com.jiagu.ags4.scene.work.settings

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.work.LocalMapVideoModel
import com.jiagu.ags4.scene.work.MapVideoActivity
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.counter.SliderCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import org.greenrobot.eventbus.EventBus
import kotlin.math.roundToInt

//参数行间距
private val PARAM_COLUMN_SPACE_BY = 10.dp

//比较规则 大于
private const val COMPARE_RULE_LARGE = ">"

//比较规则 小于
private const val COMPARE_RULE_SMALL = "<"

class WorkMachinaSwitch(val workMachinaType: Int)

/**
 * 作业机设置
 */
@Composable
fun WorkMachinaSettings(modifier: Modifier = Modifier, rowShowCount: Int = 3) {
    val context = LocalContext.current
    val mapVideoModel = LocalMapVideoModel.current
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val names = stringArrayResource(id = R.array.work_machine_type)
    val values = listOf(
        VKAgCmd.DRONE_TYPE_NORMAL.toInt(),
        VKAgCmd.DRONE_TYPE_SEED_B.toInt(),
        VKAgCmd.DRONE_TYPE_WASHING.toInt(),
    )
    val letters = mutableListOf<Pair<Int, String>>()
    for (v in values) {
        letters.add(
            when (v) {
                VKAgCmd.DRONE_TYPE_NORMAL.toInt() -> VKAgCmd.DRONE_TYPE_NORMAL.toInt() to "A" //喷洒
                VKAgCmd.DRONE_TYPE_SEED_B.toInt() -> VKAgCmd.DRONE_TYPE_SEED_B.toInt() to "B" //播撒机B 改名 播撒
                VKAgCmd.DRONE_TYPE_WASHING.toInt() -> VKAgCmd.DRONE_TYPE_WASHING.toInt() to "C" //清洗机
                else -> -1 to ""
            }
        )
    }
    var droneType by remember {
        mutableIntStateOf(aptypeData?.getIntValue(VKAg.APTYPE_DRONE_TYPE) ?: -1)
    }
    val letterSize = 40.dp
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(rowShowCount),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
        ) {
            for (i in names.indices) {
                //非DEBUG 、 兆源 app 不显示清洗机
                if (values[i] == VKAgCmd.DRONE_TYPE_WASHING.toInt() && !BuildConfig.CLEAN && !BuildConfig.DEBUG) continue
                item {
                    WorkMachineType(
                        modifier = Modifier.height(60.dp),
                        letter = letters[i].second,
                        letterSize = letterSize,
                        title = names[i],
                        isCheck = droneType == letters[i].first,
                        enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_GROUND
                    ) {
                        context.let {
                            it.showDialog {
                                ScreenPopup(content = {
                                    Box(
                                        modifier = Modifier
                                            .width(300.dp)
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val tip =
                                            stringResource(id = R.string.work_machine_setting_tip)
                                        Text(
                                            text = buildAnnotatedString {
                                                append(tip)
                                                withStyle(SpanStyle(color = Color.Red)) {
                                                    append(names[i])
                                                }
                                                append("?")
                                            }, style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }, onConfirm = {
                                    droneType = letters[i].first
                                    DroneModel.activeDrone?.sendParameter(
                                        VKAg.APTYPE_DRONE_TYPE, letters[i].first.toFloat()
                                    )
                                    DroneModel.activeDrone?.getParameters()
                                    DroneModel.activeDrone?.getPumpData()
                                    EventBus.getDefault().post(WorkMachinaSwitch(droneType))
                                    it.hideDialog()
                                }, onDismiss = {
                                    it.hideDialog()
                                })
                            }
                        }

                    }
                }
            }
        }
        when (droneType) {
            VKAgCmd.DRONE_TYPE_NORMAL.toInt() -> DefaultContent(aptypeData = aptypeData)
            VKAgCmd.DRONE_TYPE_SEED_B.toInt() -> SeederBContent(aptypeData = aptypeData)
            VKAgCmd.DRONE_TYPE_WASHING.toInt() -> WashingContent(aptypeData = aptypeData)
//            VKAgCmd.DRONE_TYPE_DOUBLE_PUMP.toInt() -> DoubleWaterPumpContent(aptypeData = aptypeData)
//            VKAgCmd.DRONE_TYPE_CENTRIFUGAL.toInt() -> CentrifugalNozzleContent(aptypeData = aptypeData)
//            VKAgCmd.DRONE_TYPE_V93.toInt() -> IntegratedControlContent(aptypeData = aptypeData)
            else -> {}
        }
    }
}


@Composable
fun WorkMachineType(
    modifier: Modifier = Modifier,
    letter: String,
    letterSize: Dp,
    title: String,
    isCheck: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.primary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.clickable(enabled) {
                onClick()
            },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(letterSize)
                    .border(
                        width = 2.dp,
                        color = if (isCheck) borderColor else MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    )
                    .clip(shape = MaterialTheme.shapes.small), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
                if (isCheck) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 6.dp, y = 6.dp)
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            drawRoundRect(
                                color = borderColor, cornerRadius = CornerRadius(50f)

                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "check",
                            modifier = Modifier
                                .size(10.dp)
                                .offset(x = 2.dp, y = 2.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Box(modifier = Modifier.width(60.dp)) {
                AutoScrollingText(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = modifier.fillMaxWidth()
                )
            }
        }
    }

}

/**
 * 默认 水泵
 */
@Composable
fun DefaultContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var sprayerCurrentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MIN)?.toInt() ?: 1000)
    }
    var sprayerCurrentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MAX)?.toInt() ?: 1000)
    }
    var sprayerCurrentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MIN)?.toInt() ?: 1000)
    }
    var sprayerCurrentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MAX)?.toInt() ?: 1000)
    }
    //离心喷头
    var centrifugalNozzleCurrentMinValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MIN)?.toInt() ?: 1000)
    }
    var centrifugalNozzleCurrentMaxValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MAX)?.toInt() ?: 1000)
    }
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_min)
            SliderCounterRow(
                title = title,
                defaultValue = sprayerCurrentMinValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = sprayerCurrentMaxValue1,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                sprayerCurrentMinValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MIN, sprayerCurrentMinValue1.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_max)
            SliderCounterRow(
                title = title,
                defaultValue = sprayerCurrentMaxValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = sprayerCurrentMinValue1,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                sprayerCurrentMaxValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MAX, sprayerCurrentMaxValue1.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_min)
            SliderCounterRow(
                title = title,
                defaultValue = sprayerCurrentMinValue2,
                minValue = 1000,
                maxValue = 2000,
                compareValue = sprayerCurrentMaxValue2,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                sprayerCurrentMinValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP2_VALUE_MIN, sprayerCurrentMinValue2.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_max)
            SliderCounterRow(
                title = title,
                defaultValue = sprayerCurrentMaxValue2,
                minValue = 1000,
                maxValue = 2000,
                compareValue = sprayerCurrentMinValue2,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                sprayerCurrentMaxValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP2_VALUE_MAX, sprayerCurrentMaxValue2.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_min)
            SliderCounterRow(
                title = title,
                defaultValue = centrifugalNozzleCurrentMinValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = centrifugalNozzleCurrentMaxValue,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                centrifugalNozzleCurrentMinValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_CENT_MIN, centrifugalNozzleCurrentMinValue.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_max)
            SliderCounterRow(
                title = title,
                defaultValue = centrifugalNozzleCurrentMaxValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = centrifugalNozzleCurrentMinValue,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                centrifugalNozzleCurrentMaxValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_CENT_MAX, centrifugalNozzleCurrentMaxValue.toFloat()
                )
            }
        }
    }
}

/**
 * 双水泵
 */
@Composable
fun DoubleWaterPumpContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var currentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MIN)?.toInt() ?: 1000)
    }
    var currentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MAX)?.toInt() ?: 1000)
    }
    var currentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MIN)?.toInt() ?: 1000)
    }
    var currentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MAX)?.toInt() ?: 1000)
    }
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_min)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMaxValue1,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                currentMinValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MIN, currentMinValue1.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_max)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMinValue1,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                currentMaxValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MAX, currentMaxValue1.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_min)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue2,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMaxValue2,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                currentMinValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP2_VALUE_MIN, currentMinValue2.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_max)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue2,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMinValue2,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                currentMaxValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP2_VALUE_MAX, currentMaxValue2.toFloat()
                )
            }
        }
    }
}

/**
 * 离心喷头
 */
@Composable
fun CentrifugalNozzleContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var currentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MIN)?.toInt() ?: 1000)
    }
    var currentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MAX)?.toInt() ?: 1000)
    }
    var currentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MIN)?.toInt() ?: 1000)
    }
    var currentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MAX)?.toInt() ?: 1000)
    }
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        item {
            val title =
                stringResource(id = R.string.work_machine_param_centrifugal_nozzle_water_pump_min)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMaxValue1,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                currentMinValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MIN, currentMinValue1.toFloat()
                )
            }
        }
        item {
            val title =
                stringResource(id = R.string.work_machine_param_centrifugal_nozzle_water_pump_max)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue1,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMinValue1,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                currentMaxValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MAX, currentMaxValue1.toFloat()
                )
            }
        }
        if (DroneModel.isV9) {
            item {
                val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_min)
                SliderCounterRow(
                    title = title,
                    defaultValue = currentMinValue2,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = currentMaxValue2,
                    compareRule = COMPARE_RULE_LARGE,
                    step = 10f
                ) {
                    currentMinValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_CENT_MIN, currentMinValue2.toFloat()
                    )
                }
            }
            item {
                val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_max)
                SliderCounterRow(
                    title = title,
                    defaultValue = currentMaxValue2,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = currentMinValue2,
                    compareRule = COMPARE_RULE_SMALL,
                    step = 10f
                ) {
                    currentMaxValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_CENT_MAX, currentMaxValue2.toFloat()
                    )
                }
            }
        }
    }
}

/**
 * 一体控
 */
@Composable
fun IntegratedControlContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    //播撒
    var seederCurrentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_THD_MIN)?.toInt() ?: 800)
    }
    var seederCurrentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_THD_MAX)?.toInt() ?: 800)
    }
    var seederCurrentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_SPD_MIN)?.toInt() ?: 800)
    }
    var seederCurrentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_SPD_MAX)?.toInt() ?: 800)
    }
    //喷洒
    var sprayerCurrentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MIN)?.toInt() ?: 1000)
    }
    var sprayerCurrentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MAX)?.toInt() ?: 1000)
    }
    var sprayerCurrentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MIN)?.toInt() ?: 1000)
    }
    var sprayerCurrentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP2_VALUE_MAX)?.toInt() ?: 1000)
    }
    //离心喷头
    var centrifugalNozzleCurrentMinValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MIN)?.toInt() ?: 1000)
    }
    var centrifugalNozzleCurrentMaxValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MAX)?.toInt() ?: 1000)
    }

    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        if (DroneModel.activeDrone?.isSeeder() == true) {
            item {
                val title =
                    stringResource(id = R.string.work_machine_param_integrated_control_planter_valve_min)
                SliderCounterRow(
                    title = title,
                    defaultValue = seederCurrentMinValue1,
                    minValue = 800,
                    maxValue = 2200,
                    compareValue = seederCurrentMaxValue1,
                    compareRule = COMPARE_RULE_LARGE,
                    step = 50f
                ) {
                    seederCurrentMinValue1 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_SEED_THD_MIN, seederCurrentMinValue1.toFloat()
                    )
                }
            }
            item {
                val title =
                    stringResource(id = R.string.work_machine_param_integrated_control_planter_valve_max)
                SliderCounterRow(
                    title = title,
                    defaultValue = seederCurrentMaxValue1,
                    minValue = 800,
                    maxValue = 2200,
                    compareValue = seederCurrentMinValue1,
                    compareRule = COMPARE_RULE_SMALL,
                    step = 50f
                ) {
                    seederCurrentMaxValue1 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_SEED_THD_MAX, seederCurrentMaxValue1.toFloat()
                    )
                }
            }
            item {
                val title =
                    stringResource(id = R.string.work_machine_param_integrated_control_planter_turntable_slow)
                SliderCounterRow(
                    title = title,
                    defaultValue = seederCurrentMinValue2,
                    minValue = 800,
                    maxValue = 2200,
                    compareValue = seederCurrentMaxValue2,
                    compareRule = COMPARE_RULE_LARGE,
                    step = 50f
                ) {
                    seederCurrentMinValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_SEED_SPD_MIN, seederCurrentMinValue2.toFloat()
                    )
                }
            }
            item {
                val title =
                    stringResource(id = R.string.work_machine_param_integrated_control_planter_turntable_fast)
                SliderCounterRow(
                    title = title,
                    defaultValue = seederCurrentMaxValue2,
                    minValue = 800,
                    maxValue = 2200,
                    compareValue = seederCurrentMinValue2,
                    compareRule = COMPARE_RULE_SMALL,
                    step = 50f
                ) {
                    seederCurrentMaxValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_SEED_SPD_MAX, seederCurrentMaxValue2.toFloat()
                    )
                }
            }
        } else {
            item {
                val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_min)
                SliderCounterRow(
                    title = title,
                    defaultValue = sprayerCurrentMinValue1,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = sprayerCurrentMaxValue1,
                    compareRule = COMPARE_RULE_LARGE,
                    step = 10f
                ) {
                    sprayerCurrentMinValue1 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_PUMP_VALUE_MIN, sprayerCurrentMinValue1.toFloat()
                    )
                }
            }
            item {
                val title = stringResource(id = R.string.work_machine_param_double_water_pump_1_max)
                SliderCounterRow(
                    title = title,
                    defaultValue = sprayerCurrentMaxValue1,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = sprayerCurrentMinValue1,
                    compareRule = COMPARE_RULE_SMALL,
                    step = 10f
                ) {
                    sprayerCurrentMaxValue1 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_PUMP_VALUE_MAX, sprayerCurrentMaxValue1.toFloat()
                    )
                }
            }
            item {
                val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_min)
                SliderCounterRow(
                    title = title,
                    defaultValue = sprayerCurrentMinValue2,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = sprayerCurrentMaxValue2,
                    compareRule = COMPARE_RULE_LARGE,
                    step = 10f
                ) {
                    sprayerCurrentMinValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_PUMP2_VALUE_MIN, sprayerCurrentMinValue2.toFloat()
                    )
                }
            }
            item {
                val title = stringResource(id = R.string.work_machine_param_double_water_pump_2_max)
                SliderCounterRow(
                    title = title,
                    defaultValue = sprayerCurrentMaxValue2,
                    minValue = 1000,
                    maxValue = 2000,
                    compareValue = sprayerCurrentMinValue2,
                    compareRule = COMPARE_RULE_SMALL,
                    step = 10f
                ) {
                    sprayerCurrentMaxValue2 = it.toInt()
                    DroneModel.activeDrone?.sendParameter(
                        VKAg.APTYPE_PUMP2_VALUE_MAX, sprayerCurrentMaxValue2.toFloat()
                    )
                }
            }
            if (DroneModel.isV9) {
                item {
                    val title =
                        stringResource(id = R.string.work_machine_param_centrifugal_nozzle_min)
                    SliderCounterRow(
                        title = title,
                        defaultValue = centrifugalNozzleCurrentMinValue,
                        minValue = 1000,
                        maxValue = 2000,
                        compareValue = centrifugalNozzleCurrentMaxValue,
                        compareRule = COMPARE_RULE_LARGE,
                        step = 10f
                    ) {
                        centrifugalNozzleCurrentMinValue = it.toInt()
                        DroneModel.activeDrone?.sendParameter(
                            VKAg.APTYPE_CENT_MIN, centrifugalNozzleCurrentMinValue.toFloat()
                        )
                    }
                }
                item {
                    val title =
                        stringResource(id = R.string.work_machine_param_centrifugal_nozzle_max)
                    SliderCounterRow(
                        title = title,
                        defaultValue = centrifugalNozzleCurrentMaxValue,
                        minValue = 1000,
                        maxValue = 2000,
                        compareValue = centrifugalNozzleCurrentMinValue,
                        compareRule = COMPARE_RULE_SMALL,
                        step = 10f
                    ) {
                        centrifugalNozzleCurrentMaxValue = it.toInt()
                        DroneModel.activeDrone?.sendParameter(
                            VKAg.APTYPE_CENT_MAX, centrifugalNozzleCurrentMaxValue.toFloat()
                        )
                    }
                }
            }
        }

    }
}

/**
 * 播撒机B
 */
@Composable
fun SeederBContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var currentMinValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_THD_MIN)?.toInt() ?: 800)
    }
    var currentMaxValue1 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_THD_MAX)?.toInt() ?: 800)
    }
    var currentMinValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_SPD_MIN)?.toInt() ?: 800)
    }
    var currentMaxValue2 by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_SEED_SPD_MAX)?.toInt() ?: 800)
    }
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        item {
            val title = stringResource(id = R.string.work_machine_param_seeder_b_planter_valve_min)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue1,
                minValue = 800,
                maxValue = 2200,
                compareValue = currentMaxValue1,
                compareRule = COMPARE_RULE_LARGE,
                step = 50f
            ) {
                currentMinValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_SEED_THD_MIN, currentMinValue1.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_seeder_b_planter_valve_max)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue1,
                minValue = 800,
                maxValue = 2200,
                compareValue = currentMinValue1,
                compareRule = COMPARE_RULE_SMALL,
                step = 50f
            ) {
                currentMaxValue1 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_SEED_THD_MAX, currentMaxValue1.toFloat()
                )
            }
        }
        item {
            val title =
                stringResource(id = R.string.work_machine_param_seeder_b_planter_turntable_slow)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue2,
                minValue = 800,
                maxValue = 2200,
                compareValue = currentMaxValue2,
                compareRule = COMPARE_RULE_LARGE,
                step = 50f
            ) {
                currentMinValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_SEED_SPD_MIN, currentMinValue2.toFloat()
                )
            }
        }
        item {
            val title =
                stringResource(id = R.string.work_machine_param_seeder_b_planter_turntable_fast)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue2,
                minValue = 800,
                maxValue = 2200,
                compareValue = currentMinValue2,
                compareRule = COMPARE_RULE_SMALL,
                step = 50f
            ) {
                currentMaxValue2 = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_SEED_SPD_MAX, currentMaxValue2.toFloat()
                )
            }
        }
    }
}

/**
 * 清洗机
 */
@Composable
fun WashingContent(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var currentMinValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MIN)?.toInt() ?: 1000)
    }
    var currentMaxValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_PUMP_VALUE_MAX)?.toInt() ?: 1000)
    }
    var centrifugalNozzleCurrentMinValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MIN)?.toInt() ?: 1000)
    }
    var centrifugalNozzleCurrentMaxValue by remember {
        mutableIntStateOf(aptypeData?.getValue(VKAg.APTYPE_CENT_MAX)?.toInt() ?: 1000)
    }
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(PARAM_COLUMN_SPACE_BY)
    ) {
        item {
            val title = stringResource(id = R.string.work_machine_param_default_water_pump_min)
            SliderCounterRow(
                title = title,
                defaultValue = currentMinValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMaxValue,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                currentMinValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MIN, currentMinValue.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_default_water_pump_max)
            SliderCounterRow(
                title = title,
                defaultValue = currentMaxValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = currentMinValue,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                currentMaxValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_PUMP_VALUE_MAX, currentMaxValue.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_min)
            SliderCounterRow(
                title = title,
                defaultValue = centrifugalNozzleCurrentMinValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = centrifugalNozzleCurrentMaxValue,
                compareRule = COMPARE_RULE_LARGE,
                step = 10f
            ) {
                centrifugalNozzleCurrentMinValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_CENT_MIN, centrifugalNozzleCurrentMinValue.toFloat()
                )
            }
        }
        item {
            val title = stringResource(id = R.string.work_machine_param_centrifugal_nozzle_max)
            SliderCounterRow(
                title = title,
                defaultValue = centrifugalNozzleCurrentMaxValue,
                minValue = 1000,
                maxValue = 2000,
                compareValue = centrifugalNozzleCurrentMinValue,
                compareRule = COMPARE_RULE_SMALL,
                step = 10f
            ) {
                centrifugalNozzleCurrentMaxValue = it.toInt()
                DroneModel.activeDrone?.sendParameter(
                    VKAg.APTYPE_CENT_MAX, centrifugalNozzleCurrentMaxValue.toFloat()
                )
            }
        }
    }
}

/**
 * 滑块行
 */
@Composable
private fun SliderCounterRow(
    title: String,
    defaultValue: Int = 0,
    minValue: Int = 0,
    maxValue: Int = 100,
    step: Float = 50f,
    compareValue: Int,
    compareRule: String,
    onConfirm: (Float) -> Unit,
) {
    val context = LocalContext.current
    val defaultOldValue = if (defaultValue < minValue) {
        minValue.toFloat()
    } else if (defaultValue > maxValue) {
        maxValue.toFloat()
    } else {
        defaultValue.toFloat()
    }
    var currentValue by remember { mutableStateOf(defaultOldValue) }
    var finishValue by remember { mutableStateOf(defaultOldValue) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.padding(start = 10.dp, end = 50.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                AutoScrollingText(
                    text = currentValue.toString(0),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        SliderCounter(
            step = step,
            number = currentValue,
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            modifier = Modifier.height(30.dp),
            onValueChange = {
                currentValue = it
            },
            onValueChangeFinished = {
                val v = it.roundToInt()
                //当前值 > 比较值
                if (COMPARE_RULE_LARGE == compareRule) {
                    if (it > compareValue) {
                        (context as MapVideoActivity).let { activity ->
                            activity.showDialog {
                                SliderDataErrorPopup(onConfirm = {
                                    currentValue = defaultOldValue
                                    finishValue = defaultOldValue
                                    activity.hideDialog()
                                }, onDismiss = {
                                    currentValue = defaultOldValue
                                    finishValue = defaultOldValue
                                    activity.hideDialog()
                                })
                            }
                        }
                    } else {
                        if (it != finishValue) {
                            (context as MapVideoActivity).let { activity ->
                                activity.showDialog {
                                    SliderDataSetPopup(
                                        title = title,
                                        value = v,
                                        onConfirm = {
                                            currentValue = v.toFloat()
                                            finishValue = v.toFloat()
                                            onConfirm(finishValue)
                                            activity.hideDialog()
                                        },
                                        onDismiss = {
                                            currentValue = defaultOldValue
                                            finishValue = defaultOldValue
                                            activity.hideDialog()
                                        })
                                }
                            }
                        }
                    }
                }
                //当前值 < 比较值
                else {
                    if (it < compareValue) {
                        (context as MapVideoActivity).let { activity ->
                            activity.showDialog {
                                SliderDataErrorPopup(onConfirm = {
                                    currentValue = defaultOldValue
                                    finishValue = defaultOldValue
                                    activity.hideDialog()
                                }, onDismiss = {
                                    currentValue = defaultOldValue
                                    finishValue = defaultOldValue
                                    activity.hideDialog()
                                })
                            }
                        }
                    } else {
                        if (it != finishValue) {
                            (context as MapVideoActivity).let { activity ->
                                activity.showDialog {
                                    SliderDataSetPopup(
                                        title = title,
                                        value = v,
                                        onConfirm = {
                                            currentValue = v.toFloat()
                                            finishValue = v.toFloat()
                                            onConfirm(finishValue)
                                            activity.hideDialog()
                                        },
                                        onDismiss = {
                                            currentValue = defaultOldValue
                                            finishValue = defaultOldValue
                                            activity.hideDialog()
                                        })
                                }
                            }
                        }
                    }
                }
            })
    }
}

/**
 * 滑块数据错误弹窗
 * 处理最大值 < 最小值的情况
 */
@Composable
fun SliderDataErrorPopup(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ScreenPopup(width = 300.dp, content = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.work_machine_param_value_error_tip),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }, showCancel = false, onDismiss = {
        onDismiss()
    }, onConfirm = {
        onConfirm()
    })
}

/**
 * 滑块数据设置弹窗
 */
@Composable
fun SliderDataSetPopup(
    title: String, value: Int, onDismiss: () -> Unit, onConfirm: () -> Unit,
) {
    ScreenPopup(width = 300.dp, content = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp), contentAlignment = Alignment.Center
        ) {
            val tip = stringResource(id = R.string.work_machine_param_set_value_tip)
            Text(
                text = String.format(tip, title, value), style = MaterialTheme.typography.bodyMedium
            )
        }
    }, onDismiss = { onDismiss() }, onConfirm = {
        onConfirm()
    })
}
