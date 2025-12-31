package com.jiagu.ags4.scene.factory

import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.RtspEnum
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.ui.components.RockerCalibrationBox
import com.jiagu.ags4.utils.LocalBtDeviceModel
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.RCCalibrationTask
import com.jiagu.api.ext.toast
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.controller.Controller.Companion.CONNECTED
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.bluetooth.BluetoothList
import com.jiagu.jgcompose.channel.ChannelInfo
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.remotecontrol.RemoteControl
import com.jiagu.jgcompose.remotecontrol.RemoteControlChannel
import com.jiagu.jgcompose.rtsp.RtspConfig


/**
 * 遥控设置
 */
@Composable
fun FactorySettingsRc() {
    val context = LocalContext.current
    val progressModel = LocalProgressModel.current
    val act = LocalActivity.current as FactoryActivity
    val btDeviceModel = LocalBtDeviceModel.current
    val controllerConnectionState by DroneModel.controllerConnectionState.observeAsState()
    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val controllerKeyMapping by DroneModel.controllerKeyMapping.observeAsState()
    val controllerType by DroneModel.controllerType.observeAsState()
    val pwmData by DroneModel.pwmData.observeAsState()
    val rcFunc by DroneModel.rcafData.observeAsState()

    var rcType by remember { mutableIntStateOf(-1) }
    val rowIds = remember { mutableStateListOf<Int>() }

    LaunchedEffect(controllerKeyMapping) {
        rowIds.clear()
        if (controllerKeyMapping != null) {
            repeat(controllerKeyMapping!!.size) { index ->
                rowIds.add(index + 4)
            }
        }
    }
    controllerType?.let {
        rcType = DroneModel.rcModeIndex(it)
    }
    val deviceConfig = DeviceConfig(context)
    val channelInfos = getChannelInfos(
        rowIds = rowIds,
        controllerKeyMapping = controllerKeyMapping,
        context = context,
        rcFunc = rcFunc,
        pwmData = pwmData
    )
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (ControllerFactory.deviceModel == "PHONE" && controllerConnectionState != CONNECTED) {
            item {
                BluetoothList(
                    modifier = Modifier.height(240.dp),
                    buttonName = stringResource(id = R.string.search) + stringResource(id = R.string.device_management_rc),
                    bluetoothList = bluetoothList,
                    searching = searching ?: false,
                    onItemClick = { address ->
                        if (btDeviceModel.connectingDevice != address) {
                            btDeviceModel.stopScan()
                            DroneModel.connectDevice(
                                act.application, address
                            )
                            btDeviceModel.connectingDevice = address
                        }
                    },
                    onSearchClick = {
                        btDeviceModel.startScan(act)
                    })
            }
        } else {
            item {
                RemoteControl(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    rockerType = rcType,
                    rockerSize = 120.dp,
                    rockerValues = getRockerData(
                        controllerType = controllerType, pwmData = pwmData
                    ),
                    onRockerMode = { type ->
                        DroneModel.changeOperType(type)
                    },
                    onCalibration = {
                        context.showDialog {
                            RemoteControlCalibration(
                                progressModel = progressModel,
                                controllerType = controllerType,
                                startTask = {
                                    act.startProgress(RCCalibrationTask(controllerType!!))
                                },
                                onClose = {
                                    context.hideDialog()
                                }
                            )
                        }
                    }
                )
            }
            //通道
            item {
                RemoteControlChannel(
                    modifier = Modifier.fillMaxWidth(), channelInfos = channelInfos
                )
            }
        }
        //rtsp
        item {
            val channels = mutableListOf<String>()
            channels.add("N/A")
            controllerKeyMapping?.let {
                it.forEach { channel ->
                    if (channel != null && channel.isNotEmpty()) {
                        channels.add(channel.substring(1))
                    }

                }
            }
            val rtspInfoList = RtspEnum.toRtspInfo()
            if (ControllerFactory.deviceModel.contains("EAV")) {
                rtspInfoList.map { it.type = 0 }
            }
            RtspConfig(
                modifier = Modifier.fillMaxWidth(),
                rtspInfoList = rtspInfoList,
                channels = channels,
                upDownChannel = deviceConfig.gimbalControlUpDown,
                leftRightChannel = deviceConfig.gimbalControlLeftRight,
                key = deviceConfig.rtspType,
                customUrl = deviceConfig.customUrl,
                toast = {
                    context.toast(it)
                },
                onRowClick = { key, url ->
                    deviceConfig.rtspType = key
                    deviceConfig.rtspurl = url
                    ControllerFactory.rtspUrl = url
                },
                onCancel = {},
                onConfirm = { upDown, leftRight ->
                    deviceConfig.gimbalControlUpDown = upDown
                    deviceConfig.gimbalControlLeftRight = leftRight
                    DroneModel.readControllerParam()
                })
        }
    }
}

/**
 * 获取通道数据
 */
fun getChannelInfos(
    rowIds: List<Int>,
    controllerKeyMapping: Array<String>?,
    context: Context,
    rcFunc: VKAg.RCAFData?,
    pwmData: VKAg.PWMData?,
): List<ChannelInfo> {
    var channelNames = context.resources.getStringArray(com.jiagu.v9sdk.R.array.channel_name)
    //处理喷洒/播撒情况
    when (DroneModel.currentWorkType.second) {
        VKAg.LOAD_TYPE_SEED -> {
            channelNames = VKAgTool.getChannel(
                context = context, channelNames = channelNames, isSeeder = true
            )
        }

        VKAg.LOAD_TYPE_SPRAY -> {
            channelNames = VKAgTool.getChannel(
                context = context, channelNames = channelNames, isSeeder = false
            )
        }
    }
    //添加初始N/A值
    val mappingNames = channelNames.toMutableList()
    mappingNames.add(0, "N/A")

    val channelDesc =
        context.resources.getStringArray(com.jiagu.v9sdk.R.array.channel_desc).toMutableList()
    //添加N/A对应的command
    channelDesc.add(0, "||")
    val channelDescList = mutableListOf<List<String>>()
    channelDesc.forEach {
        channelDescList.add(it.split("|"))
    }

    val needIndexes =
        intArrayOf(0, 6, 7, 8, 9, 19, 21, 22, 23, 24, 26, 31, 32, 33, 36, 38, 39, 40, 41, 42)
    val filterMappingNames = mappingNames.filterIndexed { index, _ ->
        index in needIndexes && index < mappingNames.size
    }
    val times = if (rowIds.size > 12) 12 else rowIds.size
    val channelInfos = mutableListOf<ChannelInfo>()
    val mappingTips = mutableListOf<String>().apply {
        repeat(needIndexes.size) { add("") }
    }
    val boundTip = "(${context.getString(R.string.already_bound)})"
    repeat(times) {
        val rowId = rowIds[it]
        var function = 0
        var isLocked = false
        var isReverse = false
        rcFunc?.let {
            function = it.functions?.get(rowId)?.toInt() ?: 0
            isLocked = (it.mapping[rowId].toInt().and(2)) == 2
            isReverse = (it.mapping[rowId].toInt().and(1)) == 1
        }
        val pwm = pwmData?.ControllerInput?.get(rowId)?.minus(100) ?: 0
        val mappingName = mappingNames[function]
        val commandTitles =
            if (function < channelDescList.size) channelDescList[function] else listOf()
        val keysName =
            if (controllerKeyMapping != null && rowId - 4 in controllerKeyMapping.indices) {
                val key = controllerKeyMapping[rowId - 4]
                if (key.length > 1) {
                    key.removeRange(0, 1)
                } else {
                    ""
                }
            } else {
                ""
            }
        val mappingIndex = filterMappingNames.indexOf(mappingName)
        if (mappingIndex > 0 && mappingIndex < mappingTips.size) {
            mappingTips[mappingIndex] = boundTip
        }
        val channelInfo = ChannelInfo(
            keysName = keysName,
            commandProgress = pwm / 100f,
            commandNum = 3,
            commandTitles = commandTitles,
            mappingIndex = mappingIndex,
            mappingName = mappingName,
            mappingNames = filterMappingNames,
            isMappingEdit = !isLocked,
            showDirection = !isLocked,
            isReverse = isReverse,
            onConfirm = { idx, reverse ->
                function = needIndexes[idx]
                isReverse = reverse
                DroneModel.activeDrone?.setChannelMapping(rowId, function, reverse)
            }
        )
        channelInfos.add(channelInfo)
    }
    channelInfos.map { it.mappingTips = mappingTips }
    return channelInfos.toList()
}

/**
 * 遥控器校准
 */
@Composable
fun RemoteControlCalibration(
    progressModel: ProgressModel,
    controllerType: String?,
    startTask: () -> Unit,
    onClose: () -> Unit,
) {
    val tip1 = stringResource(id = R.string.remote_control_calibration_before_tip)
    val tip2 = stringResource(id = R.string.remote_control_calibration_after_tip)
    val tip3 = stringResource(id = R.string.remote_control_calibration_finish_tip)
    var step by remember {
        mutableIntStateOf(1)
    }
    var tipContent by remember {
        mutableStateOf(tip1)
    }
    tipContent = when (step) {
        1 -> tip1
        2 -> tip2
        3 -> tip3
        else -> ""
    }

    val confirmText = when (step) {
        1 -> R.string.start_calibration
        else -> R.string.confirm
    }
    val cancelText = when (step) {
        2 -> R.string.in_calibration_click_cancel
        else -> R.string.cancel
    }
    var rockerData = arrayOf<Float>()
    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = (progress as ProgressModel.ProgressMessage)
            if (step in 2..3) {
                rockerData = Gson().fromJson(message.text, Array<Float>::class.java)
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            //不存在校准失败的情况 校准没成功之前一直处于校准状态 除非手动取消校准
            //校准成功
            progressModel.done()
            if (result.success) {
                step = 3
            }
            //取消校准
            else {
                onClose()
            }
        }
    }

    ScreenPopup(
        width = 450.dp, confirmText = confirmText, cancelText = cancelText, content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(425.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.rocker_calibration),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    when (step) {
                        1 -> {
                            Text(
                                text = tip1,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val width = 100.dp
                                val height = 100.dp
                                //圆心半径
                                val centerCircleRadius = 15f
                                //坐标粗细
                                val lineThickness = 5.dp
                                for (i in 1..2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        RockerCalibrationBox(
                                            width = width,
                                            height = height,
                                            centerCircleRadius = centerCircleRadius,
                                            lineThickness = lineThickness,
                                            modifier = Modifier
                                        )
                                    }
                                }
                            }
                        }

                        2, 3 -> {
                            val inCaliTip = when (step) {
                                2 -> stringResource(id = R.string.remote_control_calibration_after_tip)
                                3 -> stringResource(id = R.string.remote_control_calibration_finish_tip)
                                else -> ""
                            }
                            Text(
                                text = inCaliTip,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                //坐标轴长度 建议x和y一致
                                val width = 100.dp
                                val height = 100.dp
                                //中心圆心半径
                                val centerCircleRadius = 15f
                                //坐标粗细
                                val lineThickness = 5.dp
//                            val rockerData = getRockerData()
                                // 左摇杆 x = rockerData[1] , y = rockerData[0]
                                // 右摇杆 x = rockerData[3] , y = rockerData[2]
                                var leftRockerTopRate = 0f
                                var leftRockerRightRate = 0f
                                var leftRockerBottomRate = 0f
                                var leftRockerLeftRate = 0f

                                var rightRockerTopRate = 0f
                                var rightRockerRightRate = 0f
                                var rightRockerBottomRate = 0f
                                var rightRockerLeftRate = 0f
                                // 上:-1 ~ 0   下 :1 ~ 0  左 :1 ~ 0  右 :1 ~ 0
                                if (rockerData.size == 4) {
                                    //左摇杆 x轴 数据
                                    val leftX = rockerData[1]
                                    if (leftX > 0) {
                                        leftRockerRightRate = leftX
                                    } else if (leftX < 0) {
                                        leftRockerLeftRate = -leftX
                                    }
                                    //左摇杆 y轴 数据
                                    val leftY = rockerData[0]
                                    if (leftY > 0) {
                                        leftRockerBottomRate = leftY
                                    } else if (leftY < 0) {
                                        leftRockerTopRate = -leftY
                                    }
                                    //右摇杆 x轴 数据
                                    val rightX = rockerData[3]
                                    if (rightX > 0) {
                                        rightRockerRightRate = rightX
                                    } else if (rightX < 0) {
                                        rightRockerLeftRate = -rightX
                                    }
                                    //右摇杆 y轴 数据
                                    val rightY = rockerData[2]
                                    if (rightY > 0) {
                                        rightRockerBottomRate = rightY
                                    } else if (rightY < 0) {
                                        rightRockerTopRate = -rightY
                                    }
                                }
                                //左摇杆
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RockerCalibrationBox(
                                        width = width,
                                        height = height,
                                        centerCircleRadius = centerCircleRadius,
                                        lineThickness = lineThickness,
                                        topLineFillRate = leftRockerTopRate,
                                        rightLineFillRate = leftRockerRightRate,
                                        bottomLineFillRate = leftRockerBottomRate,
                                        leftLineFillRate = leftRockerLeftRate,
                                        modifier = Modifier
                                    )
                                }
                                //右摇杆
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RockerCalibrationBox(
                                        width = width,
                                        height = height,
                                        centerCircleRadius = centerCircleRadius,
                                        lineThickness = lineThickness,
                                        topLineFillRate = rightRockerTopRate,
                                        rightLineFillRate = rightRockerRightRate,
                                        bottomLineFillRate = rightRockerBottomRate,
                                        leftLineFillRate = rightRockerLeftRate,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }

                        4 -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.calibration_success),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                }
            }
        }, onDismiss = {
            if (step != 1) {
                progressModel.next(-1)
            }
            if (step == 1) {
                onClose()
            }
            step = 1
        }, onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    if (controllerType != null) {
                        startTask()
                    }
                }

                3 -> {
                    step = 4
                    progressModel.done()
                }

                4 -> {
                    progressModel.done()
                    onClose()
                }
            }
        }, showConfirm = step != 2, showCancel = step != 3 && step != 4
    )
}


fun regularValue(v: Short): Float {
    return (v - 150) / 50f
}

fun rockerDataProcess(ch: FloatArray, rcType: String): FloatArray {
    val indies = when (rcType) {
        "jp" -> intArrayOf(3, 0, 2, 1)
        "us" -> intArrayOf(3, 2, 0, 1)
        else -> intArrayOf(1, 0, 2, 3)
    }
    val xy = floatArrayOf(0f, 0f, 0f, 0f)
    for (i in 0 until 4) {
        val c = ch[i] // 100
        val idx = indies[i] //3 0 2 1
        xy[idx] = c // 100
    }
    //上:-1 ~ 0   下 :1 ~ 0  左 :1 ~ 0  右 :1 ~ 0
//    Log.d("zhy", "updateStick: left-x = ${xy[1]},left-y = ${xy[0]}")
//    Log.d("zhy", "updateStick: right-x = ${xy[3]},right-y = ${xy[2]}")
    return xy
}

fun getRockerData(controllerType: String?, pwmData: VKAg.PWMData?): FloatArray? {
    return pwmData?.let {
        val ch = floatArrayOf(0f, 0f, 0f, 0f)
        for (i in 0 until 4) {
            ch[i] = regularValue(it.ControllerInput[i])
        }
        // up/down have to reverse
        ch[2] = -ch[2]
        //处理摇杆数据
        return rockerDataProcess(ch, controllerType ?: "")
    }
}