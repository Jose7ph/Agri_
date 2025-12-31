package com.jiagu.ags4.scene.work

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import com.jiagu.ags4.vm.ABModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.WarningPopup

@Composable
fun WorkABStart(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val abModel = remember { navController.getViewModel(route, ABModel::class.java) }
    val calcBreaks by abModel.calcBreaks.collectAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val flyMode by DroneModel.imuData
        .map { it?.flyMode }
        .distinctUntilChanged()
        .observeAsState()
    LaunchedEffect(flyMode) {
        if (abModel.showBreak && VKAgTool.isABMode(flyMode?.toInt() ?: 0)) {
            activity.removeCalcBreaksByIndex(abModel.selectBreakIndex)
        }
    }
    LaunchedEffect(imuData) {
        imuData?.let {
            abModel.processImuData(it)
        }
    }
    LaunchedEffect(mapVideoModel.airFlag) {
        if (mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_GROUND) {//落地之后更新当前地块 清除断点12
            abModel.clearBreakIndexAndList()
        }
    }
    //获取断点
    val abplData by DroneModel.abplData.observeAsState()
    LaunchedEffect(abplData) {
        abplData?.let {
            abModel.setABPLData(it)
        }
    }

    //AB点数据
    val abData by DroneModel.abData.observeAsState()
    LaunchedEffect(abData) { //当收到ABdata时，说明到了一个目标点，根据当前选择的断点选择需要删除的其他断点
        abData?.let {
            abModel.collectABData(it, flyMode) {
                activity.removeCalcBreaksByIndex(abModel.selectBreakIndex)
            }
        }
    }

    DisposableEffect(Unit) {
        //start页面默认隐藏参数抽屉
        mapVideoModel.showParam = false
        onDispose {
            mapVideoModel.showParam = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RightBox(modifier = Modifier.align(Alignment.TopEnd), buttons = {
            //继续/暂停
            RightButtonCommon(
                text = stringResource(id = if (VKAgTool.isABMode((flyMode ?: 0).toInt())) R.string.pause else R.string.fly_continue),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR,
            ) {
                DroneModel.activeDrone?.apply {
                    if (VKAgTool.isABMode((flyMode ?: 0).toInt())) {
                        hover()
                    } else {
                        //隐藏返回点
                        abModel.clearABBreak()
                        //选择断点
                        activity.removeCalcBreaksByIndex(abModel.selectBreakIndex)
                        if (abModel.selectBreakIndex > -1 && calcBreaks.isNotEmpty()) {
                            val curBK = calcBreaks[abModel.selectBreakIndex]
                            DroneModel.activeDrone?.sendABInfo(curBK)
                            abModel.curCalcBK = curBK
                        }
                        DroneModel.activeDrone?.takeOff2Ab()
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
                    WarningPopup(
                        title = stringResource(R.string.fly_confirm),
                        onConfirm = {
                            DroneModel.activeDrone?.takeOff2Ab()
                            context.hideDialog()
                        },
                        onDismiss = {
                            context.hideDialog()
                        })
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            //急停仅 REMOTE_ID模式才显示 or Debug
            EmergencyStopButton()
        })
        //作业参数
        Drawer(
            modifier = Modifier.padding(top = STATUS_BAR_HEIGHT),
            color = BlackAlpha,
            context = {
                DrawerContentAB(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LEFT_DRAWER_MAX_WIDTH)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    abModel = abModel,
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
        if (abModel.showBreak) {
            ABRightBreakPoint(
                modifier = Modifier
                    .padding(end = 120.dp)
                    .align(Alignment.CenterEnd),
                defaultNumber = abModel.selectBreakIndex,
                breakpointSize = calcBreaks.size,
            ) {
                abModel.selectBreakIndex = it
            }
        }
    }
}
