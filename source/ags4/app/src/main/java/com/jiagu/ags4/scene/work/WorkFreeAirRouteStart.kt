package com.jiagu.ags4.scene.work

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
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
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup


@Composable
fun WorkFreeAirRouteStart(freeAirRoute: String, freeAirParamRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val config = Config(context)
    val freeAirRouteModel =
        remember { navController.getViewModel(freeAirRoute, FreeAirRouteModel::class.java) }
    val freeAirRouteParamModel = remember {
        navController.getViewModel(freeAirParamRoute, FreeAirRouteParamModel::class.java)
    }
    val taskModel = remember {
        navController.getViewModel(freeAirParamRoute, TaskModel::class.java)
    }
    val calcBreaks by taskModel.calcBreaks.collectAsState()
    val flyMode by DroneModel.imuData.map { it?.flyMode }.distinctUntilChanged().observeAsState()
    //断点
    val breakPoint by DroneModel.breakPoint.observeAsState()
    LaunchedEffect(breakPoint) {
        taskModel.setBreakPoint(breakPoint)
        taskModel.clearBreaks()
        taskModel.clearCalcBK()
    }
    val imuData by DroneModel.imuData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            freeAirRouteModel.processImuData(it)
            taskModel.checkNaviDone(it, freeAirRouteParamModel.selectedLocalBlockId!!) {
                updateFreeAirRoutePlan(
                    freeAirRouteModel, freeAirRouteParamModel
                )
                //清理断点
                freeAirRouteModel.deleteLocalBreakpoint()
            }
        }
    }

    LaunchedEffect(mapVideoModel.airFlag) {
        if (mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_GROUND) {//落地之后更新当前地块 清除断点12
            updateFreeAirRoutePlan(
                freeAirRouteModel, freeAirRouteParamModel
            )
            taskModel.clearCalcBK()
            taskModel.clearBreaks()
        }
    }

    DisposableEffect(Unit) {
        val track = freeAirRouteParamModel.workPlan?.track ?: listOf()
        taskModel.setupNavi(//用以判断航线是否完成 和 航线完成时加灰线
            track, VKAg.MISSION_FREE
        )
        DroneModel.workRoutePoint = track.toMutableList()
        DroneModel.planId = freeAirRouteParamModel.workPlan?.planId ?: 0
        DroneModel.localPlanId = freeAirRouteParamModel.workPlan?.localPlanId ?: 0
        mapVideoModel.showParam = false
        onDispose {
            mapVideoModel.showParam = true
            freeAirRouteModel.refresh(localBlockId = freeAirRouteModel.selectedLocalBlockId)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //列表
        Drawer(modifier = Modifier.padding(top = STATUS_BAR_HEIGHT), color = BlackAlpha, context = {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LEFT_DRAWER_MAX_WIDTH)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (freeAirRouteParamModel.selectedMarkerIndex != -1) {
                    DrawerContentSingleAirRouteParam(
                        mapVideoModel = mapVideoModel,
                        freeAirRouteParamModel = freeAirRouteParamModel,
                        flyingEdit = false
                    )
                } else {
                    DrawerContentJobParam(freeAirRouteParamModel = freeAirRouteParamModel)
                }
            }
        }, isShow = mapVideoModel.showParam, onShow = {
            mapVideoModel.showParam = true
            mapVideoModel.hideInfoPanel()
        }, onClose = {
            mapVideoModel.showParam = false
        })
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //继续/暂停
                RightButtonCommon(
                    text = stringResource(
                        id = if (VKAgTool.isNavigation(
                                (flyMode ?: 0).toInt()
                            )
                        ) R.string.pause else R.string.fly_continue
                    ),
                    enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR,
                ) {
                    DroneModel.activeDrone?.apply {
                        if (VKAgTool.isNavigation((flyMode ?: 0).toInt())) {
                            hover()
                        } else {
                            taskModel.endCalcBreak() //停止计算断点
                            activity.removeCalcBreaksByIndex(taskModel.selectBreakIndex)
                            if (taskModel.selectBreakIndex > -1 && calcBreaks.isNotEmpty()) {
                                val curBK = calcBreaks[taskModel.selectBreakIndex]
                                DroneModel.activeDrone?.setBreakPoint(curBK)
                                taskModel.curCalcBK = curBK
                            }
                            takeOff2Route()
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
                    DroneModel.activeDrone?.apply {
                        context.showDialog {
                            FreeAirRouteFlyConfirmPopup(
                                mapVideoModel = mapVideoModel,
                                freeAirRouteParamModel = freeAirRouteParamModel,
                                confirm = {//AirRouteFlyConfirmPopup点击confirm时，已经发了起飞去作业的命令
                                    makeFreeAirRoutePlan(freeAirRouteParamModel, config)
                                    //作业页面，更新plan
                                    freeAirRouteParamModel.saveOrUpdatePlan(isUpdate = true) {
                                        updateFreeAirRoutePlan(
                                            freeAirRouteModel, freeAirRouteParamModel
                                        )
                                    }
                                    context.hideDialog()
                                },
                                cancel = {
                                    context.hideDialog()
                                })
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                //急停仅 REMOTE_ID模式才显示 or Debug
                EmergencyStopButton()
            })

        if (taskModel.showBreak) {
            ABRightBreakPoint(
                modifier = Modifier
                    .padding(end = 120.dp)
                    .align(Alignment.CenterEnd),
                defaultNumber = taskModel.selectBreakIndex,
                breakpointSize = calcBreaks.size
            ) {
                taskModel.selectBreakIndex = it
            }
        }
    }
}