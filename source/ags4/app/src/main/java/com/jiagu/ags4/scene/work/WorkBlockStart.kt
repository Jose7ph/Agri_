package com.jiagu.ags4.scene.work

import android.content.Context
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
import com.jiagu.ags4.utils.WorkUtils
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup

@Composable
fun WorkBlockStart(blockRoute: String, blockParamRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val config = Config(context)
    val blockModel = remember { navController.getViewModel(blockRoute, BlockModel::class.java) }
    val blockParamModel =
        remember { navController.getViewModel(blockParamRoute, BlockParamModel::class.java) }
    val taskModel = remember { navController.getViewModel(blockParamRoute, TaskModel::class.java) }
    val routeModel =
        remember { navController.getViewModel(blockParamRoute, RouteModel::class.java) }
    val outPathRouteModel =
        remember { navController.getViewModel(blockParamRoute, OutPathRouteModel::class.java) }
    val calcBreaks by taskModel.calcBreaks.collectAsState()
    val flyMode by DroneModel.imuData.map { it?.flyMode }.distinctUntilChanged().observeAsState()
    //断点
    val breakPoint by DroneModel.breakPoint.observeAsState()
    LaunchedEffect(breakPoint) {
        taskModel.setBreakPoint(breakPoint)
        taskModel.clearCalcBK()
//        activity.removeAllCalcBreakpoint()
    }
    val imuData by DroneModel.imuData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            blockModel.processImuData(it)
            taskModel.checkNaviDone(it, blockParamModel.selectedLocalBlockId!!) {
                updateBlockPlan(blockModel, blockParamModel)
                //清除断点
                blockModel.deleteLocalBreakpoint()
            }
        }
    }
    LaunchedEffect(mapVideoModel.airFlag) {
        if (mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_GROUND) {//落地之后更新当前地块 清除断点12
            updateBlockPlan(blockModel, blockParamModel)
            taskModel.clearCalcBK()
            taskModel.clearBreaks()
        }
    }
    DisposableEffect(Unit) {
        val track = blockParamModel.workPlan?.track ?: listOf()
        taskModel.setupNavi(//用以判断航线是否完成 和 航线完成时加灰线
            track, WorkUtils.planType2VK2(blockParamModel.planType)
        )
        DroneModel.workRoutePoint = track.toMutableList()
        DroneModel.planId = blockParamModel.workPlan?.planId ?: 0
        DroneModel.localPlanId = blockParamModel.workPlan?.localPlanId ?: 0
        //start页面默认隐藏参数抽屉
        mapVideoModel.showParam = false
        onDispose {
            mapVideoModel.showParam = true
            blockModel.refresh(localBlockId = blockModel.selectedLocalBlockId)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //列表
        Drawer(modifier = Modifier.padding(top = STATUS_BAR_HEIGHT), color = BlackAlpha, context = {
            DrawerContentJobParam(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LEFT_DRAWER_MAX_WIDTH)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                blockParamModel = blockParamModel,
                routeModel = routeModel,
                flyingEdit = false
            )
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
                            if (mapVideoModel.needUploadNavi) {//继续作业按钮 如果手动起飞 则需要上传航线
                                newSortieUploadNavi(
                                    routeModel = routeModel,
                                    blockParamModel = blockParamModel,
                                    mapVideoModel = mapVideoModel,
                                    taskModel = taskModel,
                                    outPathRouteModel = outPathRouteModel,
                                    blockModel = blockModel,
                                    config = config,
                                    context = context
                                )
                                mapVideoModel.needUploadNavi = false//上传完航线后就不需要再上传了
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
                        activity.let {
                            if (mapVideoModel.needUploadNavi) {//起飞 开了航线智能航线，需要重新上传航线
                                newSortieUploadNavi(
                                    routeModel = routeModel,
                                    blockParamModel = blockParamModel,
                                    mapVideoModel = mapVideoModel,
                                    taskModel = taskModel,
                                    outPathRouteModel = outPathRouteModel,
                                    blockModel = blockModel,
                                    config = config,
                                    context = context
                                )
                                mapVideoModel.needUploadNavi = false//上传完航线后就不需要再上传了
                            } else {//否则，直接起飞
                                it.showDialog {
                                    BlockFlyConfirmPopup(
                                        mapVideoModel = mapVideoModel,
                                        blockParamModel = blockParamModel,
                                        routeModel = routeModel,
                                        taskModel = taskModel,
                                        outPathRouteModel = outPathRouteModel,
                                        confirm = {
                                            makePlan(routeModel, blockParamModel, config)
                                            blockParamModel.saveOrUpdatePlan(isUpdate = true) {
                                                routeModel.resetBK()
                                                updateBlockPlan(blockModel, blockParamModel)
                                            }
                                            context.hideDialog()
                                        },
                                        cancel = {
                                            activity.hideDialog()
                                        })
                                }
                            }
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
                breakpointSize = calcBreaks.size,
            ) {
                taskModel.selectBreakIndex = it
            }
        }
    }
}

//一个架次结束后，重新再次继续作业
private fun newSortieUploadNavi(
    routeModel: RouteModel,
    blockParamModel: BlockParamModel,
    mapVideoModel: MapVideoModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
    blockModel: BlockModel,
    config: Config,
    context: Context,
) {
    taskModel.clearDone()
    makePlan(routeModel, blockParamModel, config)
    uploadNavi(//新增plan
        context, blockParamModel, routeModel
    ) {
        if (it) {
            context.showDialog {
                BlockFlyConfirmPopup(
                    mapVideoModel = mapVideoModel,
                    blockParamModel = blockParamModel,
                    routeModel = routeModel,
                    taskModel = taskModel,
                    outPathRouteModel = outPathRouteModel,
                    confirm = {
                        val track = blockParamModel.workPlan?.track ?: listOf()
                        taskModel.setupNavi(//用以判断航线是否完成 和 航线完成时加灰线  航线重排后，需要重新设置新的航线
                            track, WorkUtils.planType2VK2(blockParamModel.planType)
                        )
                        blockParamModel.saveOrUpdatePlan(isUpdate = true) {//更新规划
                            routeModel.resetBK()
                            updateBlockPlan(blockModel, blockParamModel)
                        }
                        context.hideDialog()
                    },
                    cancel = {
                        context.hideDialog()
                    })
            }
        }
    }
}