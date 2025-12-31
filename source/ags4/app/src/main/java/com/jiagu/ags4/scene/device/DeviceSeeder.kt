package com.jiagu.ags4.scene.device

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.scene.work.ParameterDrawerCounterRow
import com.jiagu.ags4.ui.components.ProgressBar
import com.jiagu.ags4.ui.theme.DarkAlpha
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.getPrimaryButtonColors
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.seedTitle
import com.jiagu.ags4.utils.sprayTitle
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.MaterialRadarCalibTask
import com.jiagu.ags4.vm.task.SeederMaterialCalibrationTask
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.button.RadioButton
import com.jiagu.jgcompose.chart.LineChart
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.progress.Progress
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper

@Composable
fun DeviceSeeder() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val deviceSeederModel = LocalDeviceSeederModel.current
    val progressModel = LocalProgressModel.current

    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val deviceSeedData by DroneModel.deviceSeedData.observeAsState()

    var materialBoxLoadCapacityValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    var valveOpeningValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    var swingingSpeedValue by remember {
        mutableStateOf(EMPTY_TEXT)
    }
    deviceWeightData?.let {
        materialBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
    }
    deviceSeedData?.let {
        valveOpeningValue = it.valve.toString()
        swingingSpeedValue = it.speed.toString()
    }

    var showSeederMaterialList by remember {
        mutableStateOf(false)
    }
    val deviceMaterialSensorData by DroneModel.deviceMaterialSensorData.observeAsState()

    val userData by deviceSeederModel.userData.observeAsState()

    val aptypeData by DroneModel.aptypeData.observeAsState()

    LaunchedEffect(userData) {
        userData?.let {
            deviceSeederModel.processUserData(it)
        }
    }

    DisposableEffect(Unit) {
        deviceSeederModel.startDataMonitor()
        onDispose {
            deviceSeederModel.stopDataMonitor()
        }
    }

    MainContent(
        title = stringResource(id = R.string.device_management_seeder),
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
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val cardModifier = Modifier.weight(1f)
                    ParameterDataCard(
                        modifier = cardModifier, title = stringResource(
                            R.string.material_box_load_capacity, UnitHelper.weightUnit()
                        ), content = materialBoxLoadCapacityValue
                    )
                    ParameterDataCard(
                        modifier = cardModifier,
                        title = stringResource(id = R.string.valve_opening),
                        content = valveOpeningValue
                    )
                    ParameterDataCard(
                        modifier = cardModifier,
                        title = stringResource(id = R.string.swinging_speed),
                        content = swingingSpeedValue
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.weight(1f)) {
                                //亩用量
                                ParameterDrawerCounterRow(
                                    textColor = Color.Black,
                                    title = if (isSeedWorkType()) context.seedTitle() else context.sprayTitle(),
                                    min = 200f,
                                    max = 40000f,
                                    step = 100f,
                                    fraction = 1,
                                    converter = if (isSeedWorkType()) UnitHelper.getSeedConverter() else UnitHelper.getSprayConverter(),
                                    defaultNumber = (aptypeData?.getValue(VKAg.APTYPE_MUYONGLIANG)
                                        ?: 1f) * 1000
                                ) {
                                    AptypeUtil.setSprayMu(it)
                                }
                            }
                            Row(modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)) {
                                //作业行距
                                ParameterDrawerCounterRow(
                                    textColor = Color.Black,
                                    title = stringResource(
                                        R.string.job_line_spacing,
                                        UnitHelper.lengthUnit()
                                    ),
                                    min = 0.3f,
                                    max = 30f,
                                    step = 0.5f,
                                    fraction = 1,
                                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                                    forceStep = true,
                                    defaultNumber = aptypeData?.getValue(VKAg.APTYPE_AB_WIDTH) ?: 4f
                                ) {
                                    AptypeUtil.setABWidth(it)
                                }
                            }
                            Row(modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)) {
                                //飞行速度(m/s)
                                ParameterDrawerCounterRow(
                                    textColor = Color.Black,
                                    title = stringResource(
                                        R.string.flight_speed,
                                        UnitHelper.lengthUnit()
                                    ),
                                    min = 0.3f,
                                    max = 13.8f,
                                    step = 0.5f,
                                    fraction = 1,
                                    converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                                    forceStep = true,
                                    defaultNumber = aptypeData?.getValue(VKAg.APTYPE_AB_MAX_SPEED)
                                        ?: 5f
                                ) {
                                    AptypeUtil.setABSpeed(it)
                                }
                            }
                        }

                    }
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(200.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = stringResource(id = R.string.current_seeder_material),
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (deviceSeederModel.currentSeederMaterial != null) {
                                val seederMaterial = deviceSeederModel.currentSeederMaterial!!
                                SeederMaterialCard(
                                    modifier = Modifier.fillMaxSize(),
                                    chartItem = seederMaterial,
                                    index = deviceSeederModel.curReCalibPos,
                                    isCheck = true,
                                    showCheckIcon = false,
                                    onClick = {},
                                    context = context,
                                    deviceSeederModel = deviceSeederModel,
                                )
                            }
                        }
                    }
                }
            }
            VerticalDivider(
                thickness = 1.dp, modifier = Modifier, color = Color.Gray
            )
            Column(
                modifier = Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceDetailsCommonButton(
                    text = stringResource(id = R.string.add_calibration),
                ) {
                    context.showDialog {
                        AddCalibrationPopup(
                            deviceSeederModel = deviceSeederModel,
                            context = context,
                            progressModel = progressModel,
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
                DeviceDetailsCommonButton(
                    text = stringResource(id = R.string.seeder_material_management),
                ) {
                    showSeederMaterialList = true
                }
                if (deviceMaterialSensorData?.manufacture_id == 31) {
                    DeviceDetailsCommonButton(
                        text = stringResource(id = R.string.seed_material_radar_calib),
                    ) {
                        progressModel.done()
                        context.showDialog {
                            MaterialRadarCalibPopup(
                                progressModel = progressModel, onDismiss = {
                                    context.hideDialog()
                                })
                        }
                    }
                }

            }
        }
    }
    if (showSeederMaterialList) {
        Box(
            modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd
        ) {
            SeederMaterialList(
                onClose = {
                    showSeederMaterialList = false
                })
        }
    }
}

/**
 * 播撒物料列表
 */
@Composable
fun SeederMaterialList(
    onClose: () -> Unit,
) {
    val deviceSeederModel = LocalDeviceSeederModel.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    //用于处理删除后列表数据定位，删除后默认定位到删除元素-1的位置
    LaunchedEffect(deviceSeederModel.currentIndex) {
        if (!deviceSeederModel.initScrollToItemFlag) {
            if (deviceSeederModel.currentIndex != -1) {
                listState.animateScrollToItem(deviceSeederModel.currentIndex)
            } else {
                listState.animateScrollToItem(0)
            }
        }
    }
    //初始化列表时定位已选择的物料位置
    LaunchedEffect(deviceSeederModel.preCheckedPosition) {
        if (deviceSeederModel.initScrollToItemFlag) {
            deviceSeederModel.initScrollToItemFlag = false
            if (deviceSeederModel.preCheckedPosition != -1) {
                listState.animateScrollToItem(deviceSeederModel.preCheckedPosition)
            }
        }
    }
    Row(
        modifier = Modifier
            .padding(top = SIMPLE_BAR_HEIGHT)
            .fillMaxHeight()
            .width(260.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .background(
                    color = DarkAlpha,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                )
                .width(25.dp)
                .height(40.dp)
                .clickable {
                    onClose()
                },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.align(Alignment.Center),
                tint = Color.White
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = DarkAlpha,
                    shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = listState,
            ) {
                items(deviceSeederModel.chartDatas.size) {
                    SeederMaterialCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        chartItem = deviceSeederModel.chartDatas[it],
                        index = it,
                        isCheck = deviceSeederModel.preCheckedPosition == it,
                        onClick = {
                            deviceSeederModel.preCheckedPosition = it
                            deviceSeederModel.currentSeederMaterial =
                                deviceSeederModel.chartDatas[it]
                            deviceSeederModel.setUserData(deviceSeederModel.chartDatas[it].chartData)
                        },
                        context = context,
                        deviceSeederModel = deviceSeederModel,
                    )
                }
            }
        }
    }
}


/**
 * 播撒物料card
 */
@Composable
fun SeederMaterialCard(
    modifier: Modifier = Modifier,
    chartItem: ChartItem,
    index: Int,
    isCheck: Boolean,
    context: Context,
    deviceSeederModel: DeviceSeederModel,
    showCheckIcon: Boolean = true,
    onClick: () -> Unit,
) {
    var chartDatas: Pair<MutableList<Pair<Float, Float>>, MutableList<Pair<Float, Float>>>? = null
    if (chartItem.chartData.data != null) {
        chartDatas = deviceSeederModel.handleChart(chartItem.chartData.data!!)
    }
    if (isCheck) {
        deviceSeederModel.currentSeederMaterial = chartItem
    }

    val operateButton =
        @Composable { mod: Modifier, name: String, enabled: Boolean, click: () -> Unit ->
            Button(
                modifier = mod,
                enabled = enabled,
                onClick = click,
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(0),
                colors = getPrimaryButtonColors()
            ) {
                AutoScrollingText(
                    text = name,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small
            )
            .border(width = 1.dp, color = Color.LightGray, shape = MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .clickable {
                onClick()
            },
    ) {
        //已选择按钮
        Box(modifier = Modifier.height(30.dp), contentAlignment = Alignment.Center) {
            if (showCheckIcon) {
                RadioButton(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterStart),
                    isSelected = isCheck,
                    size = 15.dp
                ) {
                    onClick()
                }
            }
            //物料名称
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = chartItem.chartData.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        //image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), contentAlignment = Alignment.Center
        ) {
            if (chartDatas != null) {
                LineChart(
                    modifier = Modifier.fillMaxSize(),
                    data = chartDatas.first, onTapX = {},
                    xMin = 1000f,
                    xUnit = "pwm",
                    yUnit = "g/s",
                    currentData = getCurrentChartData(chartItem.chartData.data)
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.no_data),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        //操作按钮
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                )
        ) {
            //重命名
            operateButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(), stringResource(id = R.string.rename), true
            ) {
                deviceSeederModel.curReCalibPos = index
                if (deviceSeederModel.onClickCheck(context, chartItem)) {
                    context.showDialog {
                        RenameSeederMaterialPopup(
                            deviceSeederModel = deviceSeederModel,
                            chartItem = chartItem,
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
            }
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = Color.White
            )
            //删除
            operateButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(), stringResource(id = R.string.delete), true
            ) {
                deviceSeederModel.curReCalibPos = index
                if (deviceSeederModel.onClickCheck(context, chartItem)) {
                    context.showDialog {
                        DeleteSeederMaterialPopup(
                            deviceSeederModel = deviceSeederModel,
                            chartItem = chartItem,
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
            }
            VerticalDivider(
                modifier = Modifier.fillMaxHeight(), thickness = 1.dp, color = Color.White
            )
            //查看曲线
            operateButton(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                stringResource(id = R.string.view_curves),
                chartDatas != null
            ) {
                if (chartDatas != null) {
                    context.showDialog {
                        ScreenPopup(width = 480.dp, content = {
                            LineChart(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .width(480.dp)
                                    .height(280.dp), data = chartDatas.first,
                                xMin = 1000f,
                                xUnit = "pwm",
                                yUnit = "g/s",
                                currentData = getCurrentChartData(chartItem.chartData.data),
                                onTapX = {}
                            )
                        }, showCancel = false, showConfirm = false, onDismiss = {
                            context.hideDialog()
                        })
                    }
                }
            }
        }
    }
}

/**
 * 新增校准
 */
@Composable
fun AddCalibrationPopup(
    deviceSeederModel: DeviceSeederModel,
    context: Context,
    progressModel: ProgressModel,
    onDismiss: () -> Unit,
) {
    val addCalibrationTip = stringResource(id = R.string.add_seeder_material_calibration_tip)
    val seederMaterialCalibrationTip = stringResource(id = R.string.seeder_material_calibration_tip)
    val inCalibration = stringResource(id = R.string.in_calibration)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)

    var name by remember {
        mutableStateOf("")
    }

    var step by remember {
        mutableStateOf(1)
    }

    var progressValue by remember {
        mutableStateOf(0f)
    }
    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
            Log.d(
                "zhy", "ProgressMessage: text = ${msg.text},cancellable = ${msg.cancellable} "
            )
            //step = 3 获取任务进度值
            if (step == 3) {
                Log.d("zhy", "progressValue: ${progressValue}")
                progressValue = msg.text.toFloat() / 100
            }
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
            Log.d(
                "zhy", "ProgressNotice: title = ${notice.title},content = ${notice.content} "
            )
            //校准成功/失败返回结果
            if (step == 3) {
                Log.d("zhy", "更新step到 ${notice.title} ")
                step = notice.title.toInt()
                progressModel.next(1)
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            Log.d("zhy", "ProgressResult: success = ${result.success},msg = ${result.msg} ")
            progressModel.done()
            if (step == 5) {
                //保存成功刷新列表 失败则不操作
                if (result.success && deviceSeederModel.currentIndex == -1) {
                    Log.d("zhy", "save data success：refreshList ")
                    deviceSeederModel.currentIndex = 0
                    context.toast(context.getString(R.string.save_success))
                } else {
                    context.toast(context.getString(R.string.save_fail))
                }
            }
        }
    }

    val calibrationTip = when (step) {
        1 -> addCalibrationTip
        2 -> seederMaterialCalibrationTip
        3 -> inCalibration
        4 -> calibrationFailTip
        5 -> calibrationSuccessTip
        else -> ""
    }

    val confirmText = when (step) {
        2 -> R.string.start_calibration
        4 -> R.string.retry
        else -> R.string.confirm
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
                        .height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.add_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        textAlign = TextAlign.Center
                    )
                    when (step) {
                        1 -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = calibrationTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            NormalTextField(
                                onValueChange = {
                                    name = it
                                },
                                text = name,
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(30.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        shape = MaterialTheme.shapes.small
                                    ),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                borderColor = Color.LightGray
                            )
                        }

                        2 -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = calibrationTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        3 -> {
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
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                ProgressBar(
                                    progress = progressValue,
                                    progressHeight = 10.dp,
                                    progressWidth = 200.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = calibrationTip,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = if (step == 4) Color.Red else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        },
        onDismiss = {
            //判断任务 3-校准中 5-校准成功(不保存数据) 任务出于执行中 4-校准失败 任务结束
            when (step) {
                4, 5 -> { //校准成功/失败 取消直接关闭弹窗
                    progressModel.done()
                    step = 1
                    onDismiss()
                }

                3 -> {//校准中
                }

                else -> {
                    step = 1
                    onDismiss()
                }
            }
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                }
                //准备開始校准->校准中
                2 -> {
                    step = 3
                    context.startProgress(
                        task = SeederMaterialCalibrationTask(
                            name = name, seederModel = deviceSeederModel
                        ),
                    )
                }
                //step 3 无确认按钮
                //step 4 失败 -> 重试
                4 -> {
                    step = 1
                }
                //step 5  5-校准成功(保存数据)
                5 -> {
                    onDismiss()
                    step = 1
                }
            }
        },
        confirmEnable = if (step == 1) {
            name.isNotBlank()
        } else {
            true
        },
        showConfirm = step != 3,
        showCancel = step != 3,
    )
}

/**
 * 删除播撒物料
 */
@Composable
fun DeleteSeederMaterialPopup(
    chartItem: ChartItem, deviceSeederModel: DeviceSeederModel, onDismiss: () -> Unit,
) {
    ScreenPopup(
        width = 360.dp,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(330.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.delete_seeder_material),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        onDismiss = onDismiss,
        onConfirm = {
            deviceSeederModel.deleteDroneParam(chartItem.localId) {
                if (it) {
                    onDismiss()
                }
            }
        },
    )
}

/**
 * 重命名播撒物料
 */
@Composable
fun RenameSeederMaterialPopup(
    deviceSeederModel: DeviceSeederModel, chartItem: ChartItem, onDismiss: () -> Unit,
) {
    var text by remember {
        mutableStateOf(chartItem.chartData.name)
    }
    ScreenPopup(
        width = 360.dp, content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(330.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.rename_seeder_material),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        NormalTextField(
                            onValueChange = {
                                text = it
                            },
                            text = text,
                            modifier = Modifier
                                .width(150.dp)
                                .height(30.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                ),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            borderColor = Color.LightGray
                        )
                    }
                }
            }
        }, onDismiss = onDismiss, onConfirm = {
            deviceSeederModel.renameSeederMaterial(rename = text, chartItem = chartItem) {
                onDismiss()
            }
        }, confirmEnable = text.isNotBlank()
    )
}

@Composable
private fun MaterialRadarCalibPopup(progressModel: ProgressModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }
    val progress by progressModel.progress.observeAsState()

    var progressValue by remember {
        mutableFloatStateOf(0f)
    }
    //用于控制校准中关闭弹窗的情况，如果没收到ProgressResult直接关闭弹窗会导致下次打开弹窗时收到上一次progress的消息
    var closeDialogState by remember {
        mutableStateOf(false)
    }

//    var dist by remember { mutableStateOf("") }
//    var db by remember { mutableStateOf("") }
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val msg = (progress as ProgressModel.ProgressMessage)
            Log.d(
                "zhy", "ProgressMessage: text = ${msg.text},cancellable = ${msg.cancellable} "
            )
            progressValue = msg.text.toFloat() / 100
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            Log.d("zhy", "ProgressResult: success = ${result.success},msg = ${result.msg} ")
            if (closeDialogState) {
                onDismiss()
            } else {
                if (result.success) {
                    step = 3
//                    result.msg?.let {
//                        val list = it.split(",")
//                        dist = list[0]
//                        db = list[1]
//                    }

                } else {
                    step = 4
                }
            }
            progressModel.done()
        }
    }

    ScreenPopup(
        content = {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AutoScrollingText(
                        text = stringResource(R.string.seed_material_radar_calib),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        1 -> {
                            Text(
                                text = stringResource(R.string.seed_calib_material_desc),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp
                                )
                            )
                        }

                        2 -> {
                            Text(
                                text = stringResource(R.string.in_calibration),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Progress(progress = progressValue, throughTime = 1000)
                        }
                        //成功
                        3 -> {
                            Text(
                                text = stringResource(R.string.calibration_success),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Green,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = stringResource(R.string.seed_calib_material_dist_title) + ":",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    modifier = Modifier.weight(1f),
//                                    textAlign = TextAlign.Center
//                                )
//                                Text(
//                                    text = dist,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    modifier = Modifier.weight(1f),
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = stringResource(R.string.seed_calib_material_db_title) + ":",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    modifier = Modifier.weight(1f),
//                                    textAlign = TextAlign.Center
//                                )
//                                Text(
//                                    text = db,
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    modifier = Modifier.weight(1f),
//                                    textAlign = TextAlign.Center
//                                )
//                            }
                        }
                        //失败
                        4 -> {
                            Text(
                                text = stringResource(R.string.calibration_fail),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red
                            )
                        }
                    }

                }
            }
        },
        onDismiss = {
            progressModel.done()
            if (step == 2) {
                progressModel.next(-1)
                closeDialogState = true
            } else {
                onDismiss()
            }
        },
        onConfirm = {
            progressModel.done()
            when (step) {
                1 -> {
                    context.startProgress(task = MaterialRadarCalibTask())
                    step = 2
                }

                3 -> {
                    onDismiss()
                }

                4 -> {
                    progressValue = 0f
                }

            }
        },
        showConfirm = step != 2,
        showCancel = step != 3,
        confirmText = if (step == 4) {
            R.string.retry
        } else {
            R.string.confirm
        }
    )

}

/**
 * 重新校准
 * 校准逻辑与新增校准(AddCalibrationPopup)一致，但是没有step = 1阶段，默认从step =2 开始执行
 * 增加启动task时 新增isEdit = true用于判断是更新方法
 * @param name 当前物料名称
 */
//@Composable
//fun RecalibrationPopup(
//    name: String,
//    activity: DeviceManagementActivity,
//    deviceSeederModel: DeviceSeederModel,
//    context: Context,
//    progressModel: ProgressModel
//) {
//    val seederMaterialCalibrationTip = stringResource(id = R.string.seeder_material_calibration_tip)
//    val inCalibration = stringResource(id = R.string.in_calibration)
//    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)
//    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)
//
//
//    var step by remember {
//        mutableStateOf(2)
//    }
//
//    var progressValue by remember {
//        mutableStateOf(0f)
//    }
//
//    val calibrationTip = when (step) {
//        1 -> ""
//        2 -> seederMaterialCalibrationTip
//        3 -> inCalibration
//        4 -> calibrationFailTip
//        5 -> calibrationSuccessTip
//        else -> ""
//    }
//
//    val confirmText = when (step) {
//        2 -> R.string.start_calibration
//        4 -> R.string.retry
//        else -> R.string.confirm
//    }
//
//    val progress by progressModel.progress.observeAsState()
//    when (progress) {
//        is ProgressModel.ProgressMessage -> {
//            val msg = (progress as ProgressModel.ProgressMessage)
//            Log.d(
//                "zhy", "ProgressMessage: text = ${msg.text},cancellable = ${msg.cancellable} "
//            )
//            //step = 3 获取任务进度值
//            if (step == 3) {
//                Log.d("zhy", "progressValue: ${progressValue}")
//                progressValue = msg.text.toFloat() / 100
//            }
//        }
//
//        is ProgressModel.ProgressNotice -> {
//            val notice = progress as ProgressModel.ProgressNotice
//            Log.d(
//                "zhy", "ProgressNotice: title = ${notice.title},content = ${notice.content} "
//            )
//            //校准成功/失败返回结果
//            if (step == 3) {
//                Log.d("zhy", "更新step到 ${notice.title} ")
//                step = notice.title.toInt()
//                progressModel.next(1)
//            }
//        }
//
//        is ProgressModel.ProgressResult -> {
//            val result = (progress as ProgressModel.ProgressResult)
//            Log.d("zhy", "ProgressResult: success = ${result.success},msg = ${result.msg} ")
//            progressModel.done()
//            if (step == 5) {
//                //校准结束等待结束
//                if (result.success && deviceSeederModel.currentIndex == -1) {
//                    deviceSeederModel.currentIndex = 0
//                }
//            }
//        }
//    }
//
//    ScreenPopup(
//        width = 360.dp, confirmText = confirmText,
//        content = {
//            Box(
//                modifier = Modifier
//                    .padding(10.dp)
//                    .width(330.dp),
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(100.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(
//                        text = stringResource(id = R.string.recalibration),
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(5.dp),
//                        textAlign = TextAlign.Center
//                    )
//                    when (step) {
//                        1 -> {
//                            //重新校准不需要输入名称
//                        }
//
//                        2 -> {
//                            Box(
//                                modifier = Modifier.fillMaxSize(),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                AutoScrollingText(
//                                    text = calibrationTip,
//                                    style = MaterialTheme.typography.bodySmall,
//                                    modifier = Modifier.fillMaxWidth(),
//                                )
//                            }
//                        }
//
//                        3 -> {
//                            Column(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .fillMaxHeight()
//                                    .padding(horizontal = 60.dp),
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.Center
//                            ) {
//                                Box(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = calibrationTip,
//                                        style = MaterialTheme.typography.bodySmall,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//                                ProgressBar(
//                                    progress = progressValue,
//                                    progressHeight = 10.dp,
//                                    progressWidth = 200.dp
//                                )
//                            }
//                        }
//
//                        else -> {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .fillMaxHeight(),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = calibrationTip,
//                                    style = MaterialTheme.typography.bodySmall,
//                                    modifier = Modifier.fillMaxWidth(),
//                                    textAlign = TextAlign.Center,
//                                    color = if (step == 4) Color.Red else Color.Black
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        },
//        onDismiss = {
//            //判断任务 3-校准中 5-校准成功(不保存数据) 任务出于执行中 4-校准失败 任务结束
//            when (step) {
//                4, 5 -> {
//                    progressModel.done()
//                    step = 2
//                }
//
//                else -> {
//                    step = 2
//                }
//            }
//            activity.hideDialog()
//        },
//        onConfirm = {
//            Log.d("zhy", "current step = ${step}")
//            when (step) {
//                1 -> {
//
//                }
//                //准备開始校准->校准中
//                2 -> {
//                    step = 3
//                    Log.d("zhy", "current step update = 3")
//                    activity.startProgressTask(
//                        task = SeederMaterialCalibrationTask(
//                            name = name, seederModel = deviceSeederModel, isEdit = true
//                        ),
//                        popup = {},
//                    )
//                }
//                //step 3 无确认按钮
//                //step 4 失败 -> 重试
//                4 -> {
//                    step = 2
//                }
//                //step 5  5-校准成功(保存数据)
//                5 -> {
//                    activity.hideDialog()
//                    step = 2
//                }
//            }
//        },
//        showConfirm = when (step) {
//            3 -> false
//            else -> true
//        },
//        showCancel = step != 3,
//    )
//}

private fun getCurrentChartData(data: IProtocol.UserData?): Pair<Float, Float>? {
    var curPt: Pair<Float, Float>? = null
    if (data?.data == null) return null
    val r = MemoryHelper.MemoryReader(data.data, 0, data.data.size)
    val pts = mutableListOf<Point2D>()
    for (i in 0..5) {
        val v = r.readLEShort()
        if (i < 3) {
            pts.add(Point2D(v.toDouble(), 0.0))
        } else {
            pts[i - 3].y = v.toDouble()
        }
    }
    pts.add(0, Point2D(1000.00, 0.00))
    var indexFlow = -1
    DroneModel.aptypeData.value?.let {
        val mu = it.getValue(VKAg.APTYPE_MUYONGLIANG) * 1000
        val speed = it.getValue(VKAg.APTYPE_AB_MAX_SPEED)
        val width = it.getValue(VKAg.APTYPE_AB_WIDTH)
        val flow = mu / 667.0000 * speed * width
        for ((i, pt) in pts.withIndex()) {
            if (pt.y > flow) {
                indexFlow = i
                break
            }
        }
        if (indexFlow > pts.size - 1) indexFlow = pts.size - 1
        if (indexFlow < 0) indexFlow = 0
        if (indexFlow >= 1) {
            val x2 = pts[indexFlow].x
            val y2 = pts[indexFlow].y
            val x1 = pts[indexFlow - 1].x
            val y1 = pts[indexFlow - 1].y
            val y = flow
            val x = (((x1 - x2) * (y - y1)) / (y1 - y2)) + x1
            curPt = x.toFloat() to flow.toFloat()
        } else {
            curPt = if (pts.last().y < flow) {
                pts.last().x.toFloat() to flow.toFloat()
            } else {
                pts.first().x.toFloat() to flow.toFloat()
            }
        }
    }
    return curPt
}