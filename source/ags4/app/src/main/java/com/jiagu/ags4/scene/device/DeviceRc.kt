package com.jiagu.ags4.scene.device

import android.util.Log
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.factory.RemoteControlCalibration
import com.jiagu.ags4.scene.factory.getChannelInfos
import com.jiagu.ags4.scene.factory.getRockerData
import com.jiagu.ags4.utils.LocalBtDeviceModel
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.startProgress
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.EavRcConfigTask
import com.jiagu.ags4.vm.task.RCCalibrationTask
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.controller.Controller.Companion.CONNECTED
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.bluetooth.BluetoothList
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.remotecontrol.RemoteControl
import com.jiagu.jgcompose.remotecontrol.RemoteControlChannel
import com.jiagu.jgcompose.text.AutoScrollingText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeviceRc() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val act = LocalActivity.current as DeviceManagementActivity
    val progressModel = LocalProgressModel.current
    val controllerConnectionState by DroneModel.controllerConnectionState.observeAsState()
    val controllerKeyMapping by DroneModel.controllerKeyMapping.observeAsState()
    val btDeviceModel = LocalBtDeviceModel.current
    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val controllerType by DroneModel.controllerType.observeAsState()
    val rcFunc by DroneModel.rcafData.observeAsState()
    val pwmData by DroneModel.pwmData.observeAsState()
    val rowIds = remember { mutableStateListOf<Int>() }

    LaunchedEffect(controllerKeyMapping) {
        rowIds.clear()
        if (controllerKeyMapping != null) {
            for (index in 0 until controllerKeyMapping!!.size) {
                rowIds.add(index + 4)
                // 添加提前结束条件，例如当 index >= 5 时退出
                if (index >= 15) {  // 这里只是示例条件，请根据实际需求修改
                    break
                }
            }
        }
    }
    var rcType by remember { mutableIntStateOf(-1) }
    controllerType?.let {
        rcType = DroneModel.rcModeIndex(it)
    }

    val channelInfos = getChannelInfos(
        rowIds = rowIds,
        controllerKeyMapping = controllerKeyMapping,
        context = context,
        rcFunc = rcFunc,
        pwmData = pwmData
    )
    MainContent(
        title = stringResource(id = R.string.device_management_rc),
        barAction = {
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TopBarBottom(
                    text = stringResource(id = R.string.channel_initialization),
                    buttonColor = Color.White,
                    textColor = Color.Black
                ) {
                    context.showDialog {
                        PromptPopup(
                            title = stringResource(id = R.string.channel_initialization),
                            content = stringResource(id = R.string.channel_initialize_tip),
                            onConfirm = {
                                channelInitialization(rowIds = rowIds, rcFunc = rcFunc)
                                context.hideDialog()
                            },
                            onDismiss = {
                                context.hideDialog()
                            }
                        )
                    }
                }
                //对频 s1 s2 手机不显示
                when (ControllerFactory.deviceModel) {
                    "EAV-RC50" -> {
                        TopBarBottom(
                            text = stringResource(id = R.string.init_config),
                            buttonColor = Color.White,
                            textColor = Color.Black
                        ) {
                            context.showDialog {
                                InitConfigPopup(
                                    progressModel = progressModel,
                                    startTask = {
                                        context.startProgress(EavRcConfigTask(), null)
                                    },
                                    onClose = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        }
                    }

                    else -> {
                    }
                }
            }
        },
        breakAction = {
            navController.popBackStack()
        }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (ControllerFactory.deviceModel == "PHONE" && controllerConnectionState != CONNECTED) {
                item {
                    BluetoothList(
                        modifier = Modifier
                            .height(240.dp),
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
                        }
                    )
                }
            } else {
                //摇杆模式
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
                            //todo
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
                        })
                }
                //通道
                item {
                    RemoteControlChannel(
                        modifier = Modifier.fillMaxWidth(), channelInfos = channelInfos
                    )
                }
            }
        }
    }
}

/**
 * 对频弹窗
 */
@Composable
fun DevicePairPopup(progressModel: ProgressModel, startPairing: () -> Unit, onClose: () -> Unit) {

    //0 - 准备对频 1 - 对频中 2 - 对频成功 3 - 对频失败
    var pairStatus by remember {
        mutableIntStateOf(0)
    }

    //准备对频dialog内容
    val startPairContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.device_pair),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.device_pair_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    //正在对频dialog内容
    val pairingContent = @Composable {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.device_pairing),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    //对频成功dialog内容
    val pairSuccessContent = @Composable {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp), contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.device_pair_success))
        }
    }
    //对频失败dialog内容
    val pairErrorContent = @Composable {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp), contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.device_pair_error))
        }
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            Log.d(
                "zhy",
                "ProgressResult: success=${(progress as ProgressModel.ProgressResult).success}," + "msg=${(progress as ProgressModel.ProgressResult).msg}"
            )
            progressModel.done()
            val result = (progress as ProgressModel.ProgressResult)
            //对频成功
            pairStatus = if (result.success) {
                2
            }
            //对频失败
            else {
                3
            }

        }
    }
    ScreenPopup(
        width = 310.dp,
        showConfirm = pairStatus != 1,
        showCancel = pairStatus != 1,
        confirmText = when (pairStatus) {
            3 -> R.string.retry
            else -> R.string.confirm
        },
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(280.dp),
            ) {
                when (pairStatus) {
                    0 -> startPairContent()
                    1 -> pairingContent()
                    2 -> pairSuccessContent()
                    3 -> pairErrorContent()
                    else -> {}
                }
            }
        },
        onDismiss = {
            //正在对频不允许关闭
            if (pairStatus != 1) {
                onClose()
            }
        },
        onConfirm = {
            when (pairStatus) {
                //准备对频 -> 正在对频
                0 -> {
                    pairStatus = 1
                    startPairing()
                }
                //对频成功
                2 -> {
                    onClose()
                }
                //对频失败 ->重试
                3 -> {
                    pairStatus = 0
                }

            }
        },
        confirmEnable = pairStatus != 1, //正在对频不许确认和取消
        cancelEnable = pairStatus != 1 //正在对频不许确认和取消
    )
}

/**
 * 初始化配置
 */
@Composable
private fun InitConfigPopup(
    progressModel: ProgressModel,
    startTask: () -> Unit,
    onClose: () -> Unit,
) {
    var step by remember {
        mutableIntStateOf(1)
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            Log.d(
                "zhy",
                "ProgressResult: success=${(progress as ProgressModel.ProgressResult).success}," + "msg=${(progress as ProgressModel.ProgressResult).msg}"
            )
            progressModel.done()
            val result = (progress as ProgressModel.ProgressResult)
            step = if (result.success) {
                3
            } else {
                4
            }
        }
    }

    ScreenPopup(
        width = 300.dp,
        content = {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .heightIn(max = 200.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //title
                AutoScrollingText(
                    text = stringResource(id = R.string.init_config),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    //content
                    when (step) {
                        1 -> {
                            Text(
                                text = stringResource(id = R.string.init_config_warning_tip),
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.in_configuration_tip),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }

                        3 -> {
                            Text(
                                text = stringResource(id = R.string.success),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        4 -> {
                            Text(
                                text = stringResource(id = R.string.fail),
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        onDismiss = {
            if (step != 2) {
                onClose()
            }
        },
        onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    startTask()
                }

                3 -> {
                    onClose()
                }

                4 -> {//失败重试
                    step = 1
                }
            }
        },
        showConfirm = step !in 2..3,
        showCancel = step != 2,
        confirmText = when (step) {
            4 -> R.string.retry
            else -> R.string.confirm
        }
    )
}

private fun channelInitialization(rowIds: List<Int>, rcFunc: VKAg.RCAFData?) {
    if (rowIds.isEmpty() || rcFunc == null) return
    CoroutineScope(Dispatchers.Default).launch {
        for (rowId in rowIds.toList()) {
            when (rowId) {
                4 -> { // 模式
                    DroneModel.activeDrone?.setChannelMapping(rowId, 5, false)
                }

                5 -> { //AB点
                    DroneModel.activeDrone?.setChannelMapping(rowId, 6, false)
                }

                6 -> { //返航
                    DroneModel.activeDrone?.setChannelMapping(rowId, 7, false)
                }

                else -> { //其他通道设置成NA
                    DroneModel.activeDrone?.setChannelMapping(rowId, 0, false)
                }
            }
            //延迟100ms
            delay(100)
        }
    }
}