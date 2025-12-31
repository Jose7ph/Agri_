package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.ABCleanModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup

@Composable
fun WorkABCleanStart(route: String) {
    val navController = LocalNavController.current
    val mapVideoModel = LocalMapVideoModel.current
    val context = LocalContext.current
    val abCleanModel = remember { navController.getViewModel(route, ABCleanModel::class.java) }
    val blockPoints2D by abCleanModel.blockPoints2D.collectAsState()
    val workPoints2D by abCleanModel.workPoints2D.collectAsState()
    //实时飞机信息
    val imuData by DroneModel.imuData.observeAsState()
    val flyMode by DroneModel.imuData
        .map { it?.flyMode }
        .distinctUntilChanged()
        .observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            abCleanModel.processImuData(it)
        }
    }
    //用于起飞后页面返回显示断点用
    val breakPoint by DroneModel.breakPoint.observeAsState()
    LaunchedEffect(breakPoint) {
        abCleanModel.setBK(breakPoint)
    }
    DisposableEffect(Unit) {
        //start页面默认隐藏参数抽屉
        mapVideoModel.showParam = false
        onDispose {
            mapVideoModel.showParam = true
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(top = STATUS_BAR_HEIGHT)) {
            //参数
            Drawer(
                modifier = Modifier,
                color = BlackAlpha,
                context = {
                    DrawerContentABClean(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(LEFT_DRAWER_MAX_WIDTH)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        abCleanModel = abCleanModel,
                        flyingEdit = false
                    )
                },
                isShow = mapVideoModel.showParam,
                onShow = {
                    mapVideoModel.showParam = true
                    mapVideoModel.hideInfoPanel()
                },
                onClose = {
                    mapVideoModel.showParam = false
                })
            //2d航线
            VerticalTrackBox(
                modifier = Modifier
                    .padding(top = 2.dp, start = 2.dp)
                    .width(160.dp)
                    .height(160.dp)
                    .background(Color.Black),
                boundary = blockPoints2D,
                points = workPoints2D,
                isClosed = false,
                dronePosition = abCleanModel.dronePoint2D,
                breakPoint = abCleanModel.breakPoint2D
            )
        }
        RightBox(modifier = Modifier.align(Alignment.TopEnd), buttons = {
            //继续/暂停
            RightButtonCommon(
                text = stringResource(
                    id = if (VKAgTool.isCleanMode((flyMode ?: 0).toInt())) R
                        .string.pause else R.string.fly_continue
                ),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR,
            ) {
                DroneModel.activeDrone?.apply {
                    if (VKAgTool.isCleanMode((flyMode ?: 0).toInt())) {
                        hover()
                    } else {
                        DroneModel.activeDrone?.takeOff2Clean()
                    }
                }
            }
            //返航
            RightButtonCommon(
                text = stringResource(id = R.string.protect_returning),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR
            ) {
                DroneModel.activeDrone?.apply {
                    context.showDialog {
                        PromptPopup(
                            title = stringResource(id = R.string.protect_returning),
                            content = stringResource(id = R.string.work_ask_gohome),
                            onDismiss = {
                                context.hideDialog()
                            },
                            onConfirm = {
                                goHome()
                                context.hideDialog()
                            })
                    }
                }
            }
            //降落
            RightButtonCommon(
                text = stringResource(id = R.string.touch_down),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR
            ) {
                DroneModel.activeDrone?.apply {
                    context.showDialog {
                        PromptPopup(
                            title = stringResource(id = R.string.touch_down),
                            content = stringResource(id = R.string.work_ask_land),
                            onDismiss = {
                                context.hideDialog()
                            },
                            onConfirm = {
                                land()
                                context.hideDialog()
                            })
                    }
                }
            }
            //起飞
            RightButtonCommon(
                text = stringResource(id = R.string.take_off),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_GROUND
            ) {
                context.showDialog {
                    CleanFlyConfirmPopup(
                        width = abCleanModel.width,
                        repeatCount = abCleanModel.repeatCount,
                        speed = abCleanModel.speed,
                        confirm = {
                            DroneModel.activeDrone?.takeOff2Clean()
                            context.hideDialog()
                        },
                        cancel = {
                            context.hideDialog()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            //急停仅 REMOTE_ID模式才显示 or Debug
            EmergencyStopButton()
        })
    }
}