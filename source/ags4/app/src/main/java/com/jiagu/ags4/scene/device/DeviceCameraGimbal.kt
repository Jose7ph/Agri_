package com.jiagu.ags4.scene.device

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.RtspEnum
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.rtsp.RtspConfig
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField

@Composable
fun DeviceCameraGimbal() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val controllerKeyMapping by DroneModel.controllerKeyMapping.observeAsState()
    val deviceConfig = DeviceConfig(context)
    val rtspInfoList = RtspEnum.toRtspInfo()
    if (ControllerFactory.deviceModel.contains("EAV")) {
        rtspInfoList.map { it.type = 0 }
    }
    var gimbalCount by remember {
        mutableIntStateOf(deviceConfig.gimbalCount)
    }
    var rtspUrl2 by remember {
        mutableStateOf(deviceConfig.rtspurl2)
    }
    MainContent(title = stringResource(id = R.string.device_camera_gimbal), barAction = {
    }, breakAction = {
        navController.popBackStack()
    }) {
        Row(modifier = Modifier) {
            LazyColumn(
                modifier = Modifier,
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 推流地址
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
                    ShadowFrame(shape = MaterialTheme.shapes.medium) {
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
                                if (key == "custom") deviceConfig.customUrl = url
                            },
                            onCancel = {},
                            onConfirm = { upDown, leftRight ->
                                deviceConfig.gimbalControlUpDown = upDown
                                deviceConfig.gimbalControlLeftRight = leftRight
                                DroneModel.readControllerParam()
                            })

                    }
                }
                item {
                    ShadowFrame(
                        modifier = Modifier.padding(bottom = 20.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color.Gray,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .background(Color.White, MaterialTheme.shapes.medium)
                                .padding(vertical = 10.dp, horizontal = 30.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(id = com.jiagu.jgcompose.R.string.set_rtsp),
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                modifier = Modifier.height(40.dp),
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SwitchButton(
                                        width = 50.dp,
                                        height = 25.dp,
                                        backgroundColors = listOf(
                                            Color.LightGray,
                                            MaterialTheme.colorScheme.primary
                                        ),
                                        defaultChecked = gimbalCount == 2
                                    ) {
                                        gimbalCount = if (it) 2 else 1
                                        deviceConfig.gimbalCount = gimbalCount
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    NormalTextField(
                                        text = rtspUrl2,
                                        onValueChange = { value ->
                                            rtspUrl2 = value
                                        },
                                        hintPosition = TextAlign.Center,
                                        modifier = Modifier.height(30.dp),
                                        borderColor = Color.LightGray
                                    )
                                }
                                Button(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(40.dp),
                                    onClick = {
                                        deviceConfig.rtspurl2 = rtspUrl2
                                    },
                                    enabled = true,
                                    contentPadding = PaddingValues(0.dp),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    AutoScrollingText(
                                        text = stringResource(id = com.jiagu.jgcompose.R.string.setting),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }

                    }
                }


            }
        }
    }
}