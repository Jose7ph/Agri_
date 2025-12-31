package com.jiagu.ags4.scene.work

import android.content.Context
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.components.EditNaviLinePanel
import com.jiagu.ags4.ui.components.RockerBox
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.WorkUtils
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.optimizeRoute
import com.jiagu.ags4.utils.seedTitle
import com.jiagu.ags4.utils.sprayTitle
import com.jiagu.ags4.utils.startProgress
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.ags4.vm.task.CommandsTask
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper
import kotlin.math.roundToInt


@Composable
fun WorkBlockParam(blockRoute: String, blockParamRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val blockModel = remember { navController.getViewModel(blockRoute, BlockModel::class.java) }
    val blockParamModel =
        remember { navController.getViewModel(blockParamRoute, BlockParamModel::class.java) }
    val routeModel =
        remember { navController.getViewModel(blockParamRoute, RouteModel::class.java) }
    val outPathRouteModel =
        remember { navController.getViewModel(blockParamRoute, OutPathRouteModel::class.java) }
    val taskModel = remember { navController.getViewModel(blockParamRoute, TaskModel::class.java) }
    val locationModel =
        remember { navController.getViewModel(blockParamRoute, LocationModel::class.java) }
    val config = Config(context)

    val userData by blockParamModel.userData.observeAsState()
    LaunchedEffect(userData) {
        userData?.let {
            blockParamModel.processUserData(it)
        }
    }

    DisposableEffect(Unit) {
        mapVideoModel.changeLocationType("map")
        locationModel.setup()
        blockParamModel.startDataMonitor()
        //初始化block plan
        blockModel.selectedBP?.let {
            if (!blockParamModel.isInit) {
                blockParamModel.isInit = true
                initRouteByConfig(
                    blockPlan = it,
                    blockParamModel = blockParamModel,
                    routeModel = routeModel,
                    config = config
                )
                blockParamModel.initBlockPlanAndParam(it, blockModel.isNewPlan)
                if (!blockModel.isNewPlan) {
                    taskModel.setBreakPoint(blockModel.breakPoint)
                } else {
                    taskModel.clearBK()
                }
            }
        }
        onDispose {
            blockParamModel.stopDataMonitor()
        }
    }
    LaunchedEffect(blockParamModel.showDeletePointPanel, blockParamModel.showMoveRoutePanel) {
        //删除航点panel出现则隐藏地图工具
        mapVideoModel.showMapTools =
            !(blockParamModel.showDeletePointPanel || blockParamModel.showMoveRoutePanel || blockParamModel.showMarkerPanel)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            blockParamModel.showDeletePointPanel -> { //删除航线
                val start = blockParamModel.selectedDeletePointIndexes.first + 1
                val end = blockParamModel.selectedDeletePointIndexes.second + 1
                EditNaviLinePanel(
                    modifier = Modifier
                        .padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp)
                        .width(260.dp),
                    start = start,
                    end = end,
                    max = blockParamModel.curMarkerCount,
                    onConfirm = {
                        context.showDialog {
                            PromptPopup(
                                content = stringResource(
                                    R.string.delete_waypoints_tip,
                                    start.toString(),
                                    end.toString()
                                ),
                                onConfirm = {
                                    routeModel.route.value?.let { route ->
                                        routeModel.planRemoveNaviLine(
                                            route,
                                            start - 1,
                                            end - 1,
                                            planType = blockParamModel.planType,
                                            poleRadius = blockParamModel.poleRadius,
                                            smartDist = blockParamModel.smartDist
                                        )
                                    }
                                    blockParamModel.clearDeletePointMarker()
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        }
                    },
                    onCancel = {
                        blockParamModel.clearDeletePointMarker()
                    },
                    onChange = { start, end ->
                        blockParamModel.markerDeletePointSelect(
                            start - 1,
                            end - 1
                        )
                    }
                )
            }

            blockParamModel.showMoveRoutePanel -> { //移动航线
                RouteMovePanel(
                    modifier = Modifier.padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp),
                    title = stringResource(R.string.move_route),
                    onTopClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = 0.0,
                            distY = blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onBottomClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = 0.0,
                            distY = -blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onLeftClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = -blockParamModel.defaultDistStep,
                            distY = 0.0,
                            angle = activity.canvas.angle,
                        )
                    },
                    onRightClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = blockParamModel.defaultDistStep,
                            distY = 0.0,
                            angle = activity.canvas.angle,
                        )
                    },
                    onTopLeftClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = -blockParamModel.defaultDistStep,
                            distY = blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onTopRightClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = blockParamModel.defaultDistStep,
                            distY = blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onBottomLeftClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = -blockParamModel.defaultDistStep,
                            distY = -blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onBottomRightClick = {
                        offsetRoute(
                            blockParamModel = blockParamModel,
                            routeModel = routeModel,
                            distX = blockParamModel.defaultDistStep,
                            distY = -blockParamModel.defaultDistStep,
                            angle = activity.canvas.angle,
                        )
                    },
                    onClickReset = {
                        blockParamModel.selectedBP?.let {
                            blockParamModel.initBlockAndObstacles(it) {
                                routeModel.setup(
                                    blockParamModel.block,
                                    blockParamModel.obstacles,
                                    blockParamModel.barrierSafeDist
                                )
                                routeModel.calcRoute(true, blockParamModel.toRouteParameter())
                            }
                        }
                    },
                    onClickConfirm = {
                        blockParamModel.showMoveRoutePanel = false
                    },
                )
            }

            blockParamModel.showMarkerPanel -> { //移动中转点
                MarkerMovePanel(
                    modifier = Modifier.padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp),
                    moveDist = blockParamModel.moveDist,
                    title = stringResource(R.string.plan_edit_aux_point),
                    markerIndex = blockParamModel.selectedMarkerIndex + 1,//显示从1开始
                    showMarkerChange = blockParamModel.auxPoints.size > 1,
                    onTopClick = {
                        blockParamModel.moveNorth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onBottomClick = {
                        blockParamModel.moveSouth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onLeftClick = {
                        blockParamModel.moveWest(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onRightClick = {
                        blockParamModel.moveEast(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onTopLeftClick = {
                        blockParamModel.moveWestNorth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onTopRightClick = {
                        blockParamModel.moveEastNorth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onBottomLeftClick = {
                        blockParamModel.moveWestSouth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onBottomRightClick = {
                        blockParamModel.moveEastSouth(activity.canvas.angle)?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onClickReset = {
                        blockParamModel.resetMarkerDist()?.let {
                            blockParamModel.updatePoint(it)
                        }
                    },
                    onClickDelete = {
                        blockParamModel.removePointByIndex()
                    },
                    onPreviousPoint = {
                        blockParamModel.changeMarkerPoint()
                    },
                    onNextPoint = {
                        blockParamModel.changeMarkerPoint(true)
                    }
                )
            }

            else -> { //默认情况
                Image(
                    painter = painterResource(id = R.drawable.map_locator),
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .align(Alignment.Center),
                    contentDescription = null
                )
                //参数
                Drawer(
                    modifier = Modifier.padding(top = STATUS_BAR_HEIGHT),
                    color = BlackAlpha,
                    context = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(LEFT_DRAWER_MAX_WIDTH)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (blockModel.isNewPlan) {
                                DrawerContentWorkParamAll(
                                    blockParamModel = blockParamModel,
                                    locationModel = locationModel,
                                    routeModel = routeModel
                                )
                            } else {
                                DrawerContentJobParam(
                                    blockParamModel = blockParamModel,
                                    routeModel = routeModel
                                )
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
                        //移动航线
                        RightButtonCommon(text = stringResource(id = R.string.move_route)) {
                            blockParamModel.showMoveRoutePanel = !blockParamModel.showMoveRoutePanel
                        }
                        //新增中转点
                        RightButtonCommon(text = stringResource(id = R.string.plan_add_aux)) {
                            locationModel.location.value?.let {
                                blockParamModel.addAuxPoint(
                                    GeoHelper.LatLng(
                                        it.lat,
                                        it.lng,
                                    )
                                )
                            }
                        }
                        //上传航线
                        RightButtonCommon(text = stringResource(id = R.string.upload_route)) {
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
                                            confirm = {//FlyConfirmPopup点击confirm时，已经发了起飞去作业的命令
                                                savePlan(blockParamModel) {//保存规划
                                                    blockParamModel.blockWorking()//并将地块标记为进行中地块
                                                    updateBlockPlan(blockModel, blockParamModel)
                                                    blockModel.deleteLocalBreakpoint()//删除原地块断点
                                                }
                                                navController.navigate(WorkPageEnum.WORK_BLOCK_START.url)
                                                context.hideDialog()
                                            },
                                            cancel = {
                                                context.hideDialog()
                                            })
                                    }
                                }
                            }
                        }
                    })
                if (blockModel.isNewPlan) {
                    //摇杆
                    RockerBox(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp),
                        centerCircleRadius = 40f,
                        canvasSize = 120.dp,
                        centerCircleColor = MaterialTheme.colorScheme.primary,
                        lineColor = MaterialTheme.colorScheme.onPrimary
                    ) { offset, angle ->
                        blockParamModel.curAngle = angle.roundToInt()
                        routeModel.calcRoute(true, blockParamModel.toRouteParameter())
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContentWorkParamAll(
    blockParamModel: BlockParamModel,
    locationModel: LocationModel,
    routeModel: RouteModel,
) {
    var paramType by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        //航线参数/作业参数
        val names = listOf(
            stringResource(id = R.string.air_route_parameter),
            stringResource(id = R.string.job_parameter)
        )
        val indexes = listOf(0, 1)
        GroupButton(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(),
            number = paramType,
            items = names,
            indexes = indexes
        ) { idx, _ ->
            paramType = idx
        }
        if (paramType == 0) {//航线参数
            DrawerContentAirRouteParam(
                locationModel = locationModel,
                routeModel = routeModel,
                blockParamModel = blockParamModel
            )
        } else {//作业参数
            DrawerContentJobParam(blockParamModel = blockParamModel, routeModel = routeModel)
        }
    }
}

/**
 * 航线参数
 * 统一使用routeModel
 *
 * @param locationModel
 * @param routeModel
 */
@Composable
fun DrawerContentAirRouteParam(
    locationModel: LocationModel,
    routeModel: RouteModel,
    blockParamModel: BlockParamModel,
) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val config = Config(context)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //作业行距
        item {
            ParameterDrawerCounterRow(
                title = stringResource(R.string.job_line_spacing, UnitHelper.lengthUnit()),
                min = 1f,
                max = 30f,
                step = 0.5f,
                fraction = 1,
                defaultNumber = blockParamModel.curRidge,
                converter = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
            ) {
                blockParamModel.curRidge = it
                config.workRidge = it
                routeModel.calcRoute(false, blockParamModel.toRouteParameter())
            }
        }
        //障碍物安全距离
        item {
            ParameterDrawerCounterRow(
                title = stringResource(
                    R.string.obstacle_safety_distance,
                    UnitHelper.lengthUnit()
                ),
                min = 0f,
                max = 10f,
                step = 0.5f,
                fraction = 1,
                defaultNumber = blockParamModel.barrierSafeDist,
                converter = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
            ) {
                blockParamModel.barrierSafeDist = it
                config.safetyDistanceBarrier = it
                routeModel.setup(
                    blockParamModel.block,
                    blockParamModel.obstacles,
                    blockParamModel.barrierSafeDist
                )
                routeModel.calcRoute(false, blockParamModel.toRouteParameter())
            }
        }

        //上一条 / 下一条 按钮
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ParameterDrawerTextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.previous_edge)
                ) {
                    val maxIndex = blockParamModel.distances.size
                    blockParamModel.curEdge--
                    if (blockParamModel.curEdge < 0) blockParamModel.curEdge = maxIndex - 1
                    if (blockParamModel.curEdge > maxIndex) blockParamModel.curEdge = 0
                    routeModel.calcRoute(routeParameter = blockParamModel.toRouteParameter())
                }
                ParameterDrawerTextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.next_edge)
                ) {
                    val maxIndex = blockParamModel.distances.size
                    blockParamModel.curEdge++
                    if (blockParamModel.curEdge < 0) blockParamModel.curEdge = maxIndex
                    if (blockParamModel.curEdge > maxIndex) blockParamModel.curEdge = 0
                    routeModel.calcRoute(routeParameter = blockParamModel.toRouteParameter())
                }
            }
        }
        //地块内缩
        item {
            LandConsolidationRow(routeModel = routeModel, blockParamModel = blockParamModel)
        }
        //起始点设置
        item {
            StartPointSettingRow(
                onClick = {
                    val pt = locationModel.location.value?.let {
                        GeoHelper.LatLng(it.lat, it.lng)
                    }
                    if (pt != null)
                        routeModel.sortRoute(
                            pt = pt,
                            routeParameter = blockParamModel.toRouteParameter()
                        )
                }
            )
        }
        //航线类型
        item {
            val imageButtonNames =
                stringArrayResource(id = R.array.air_route_parameter_type).toList()
            val indexes =
                listOf(RouteModel.PLAN_BLOCK, RouteModel.PLAN_BLOCK_EDGE, RouteModel.PLAN_EDGE)
            ParameterDrawerGroupImageButtonRow(
                title = R.string.air_route_type,
                defaultNumber = blockParamModel.planType,
                names = imageButtonNames,
                indexes = indexes,
                images = listOf(
                    R.drawable.default_plan_type_block,
                    R.drawable.default_plan_type_block_edge,
                    R.drawable.default_plan_type_edge
                )
            ) {
                blockParamModel.planType = it
                routeModel.calcRoute(routeParameter = blockParamModel.toRouteParameter())
            }
        }
    }
}

/**
 * 作业参数
 * 统一使用blockParamModel
 *
 * @param modifier
 * @param blockParamModel
 * @param flyingEdit
 */
@Composable
fun DrawerContentJobParam(
    modifier: Modifier = Modifier,
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    flyingEdit: Boolean = true,
) {
    val context = LocalContext.current
    val config = Config(context)
    Box(modifier = modifier) {
        if (blockParamModel.isJobLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                //模板选择
                if (flyingEdit) {
                    item {
                        TemplateSelect(
                            templateName = blockParamModel.templateName.ifEmpty {
                                context.getString(
                                    R.string.unused_template
                                )
                            },
                            enabled = flyingEdit,
                            onDeleteTemplate = { id, complete ->
                                blockParamModel.removeTemplate(id) { success ->
                                    if (success) {
                                        complete()
                                        context.toast(context.getString(R.string.success))
                                    } else {
                                        context.toast(context.getString(R.string.err_server))
                                    }
                                }
                            },
                            onSave = { name ->
                                blockParamModel.saveTemplateData(name = name) { success ->
                                    if (success) {
                                        context.toast(context.getString(R.string.success))
                                    } else {
                                        context.toast(context.getString(R.string.err_server))
                                    }
                                }
                            },
                            onClick = { complete ->
                                blockParamModel.getTemplateParamList { success, data ->
                                    if (!success) {
                                        context.toast(context.getString(R.string.fail))
                                        return@getTemplateParamList
                                    }

                                    data?.let { complete(it) } ?: return@getTemplateParamList
                                }
                            },
                            onConfirm = { templateParam ->
                                blockParamModel.setTemplate(templateParam) { success, commands ->
                                    if (success) {
                                        if (!commands.isNullOrEmpty()) {
                                            DroneModel.activeDrone?.let { activeDrone ->
                                                blockParamModel.isJobLoading = true
                                                context.startProgress(
                                                    CommandsTask(
                                                        activeDrone, commands
                                                    )
                                                ) { success, msg ->
                                                    if (success) {
                                                        context.toast(context.getString(R.string.success))
                                                    } else {
                                                        context.toast(
                                                            msg ?: context.getString(R.string.fail)
                                                        )
                                                    }
                                                    blockParamModel.isJobLoading = false
                                                    blockParamModel.templateName =
                                                        templateParam.name

                                                    true
                                                }
                                            }
                                        }
                                    } else {
                                        context.toast(context.getString(R.string.err_server))
                                    }
                                }
                            })
                    }
                }
                //作业行距(仅查看 行距在航线参数已经设置,使用routeModel中的curRidge)
                item {
                    ParameterDrawerCounterRow(
                        title = stringResource(R.string.job_line_spacing, UnitHelper.lengthUnit()),
                        min = 0.3f,
                        max = 30f,
                        step = 0.5f,
                        fraction = 1,
                        converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                        forceStep = true,
                        editEnabled = false,
                        defaultNumber = blockParamModel.curRidge
                    ) {}
                }
                //喷洒模式
                item {
                    val names = if (isSeedWorkType()) listOf(
                        stringResource(R.string.manual),
                        stringResource(R.string.linkage)
                    )
                    else stringArrayResource(id = R.array.job_parameter_spray_mode).toList()
                    val values = mutableListOf<Int>()
                    values.add(VKAg.BUMP_MODE_FIXED)
                    values.add(VKAg.BUMP_MODE_SPEED)
                    ParameterDrawerGroupButtonRow(
                        title = if (isSeedWorkType()) stringResource(R.string.job_seed_mode) else stringResource(
                            R.string.job_spray_mode
                        ),
                        defaultNumber = blockParamModel.mode,
                        names = names,
                        values = values,
                    ) {
                        AptypeUtil.setPumpMode(it)
                        blockParamModel.mode = it
                    }
                }
                //播撒物料
                if (DroneModel.currentWorkType.second == VKAg.LOAD_TYPE_SEED) {
                    item {
                        SeedMaterialRow(blockParamModel = blockParamModel, flyingEdit = flyingEdit)
                    }
                }
                //喷洒模式 -固定
                if (blockParamModel.mode == VKAg.BUMP_MODE_FIXED) {
                    //流量大小
                    item {
                        SliderTitleCounter(
                            number = blockParamModel.pumpOrValveSize.toFloat(),
                            step = 1f,
                            min = 3f,
                            max = 100f,
                            title = stringResource(if (isSeedWorkType()) R.string.spray_valve_size else R.string.spray_pump_size),
                            fraction = 0,
                            titleColor = Color.White,
                            valueColor = Color.White,
                            titleSpace = 0.dp
                        ) {
                            AptypeUtil.setPumpAndValve(it)
                            blockParamModel.pumpOrValveSize = it.toInt()
                        }
                    }
                } else { //喷洒模式 - 随速
                    //亩用量
                    item {
                        ParameterDrawerCounterRow(
                            title = if (isSeedWorkType()) context.seedTitle() else context.sprayTitle(),
                            min = 200f,
                            max = 40000f,
                            step = 100f,
                            fraction = 1,
                            converter = if (isSeedWorkType()) UnitHelper.getSeedConverter() else UnitHelper.getSprayConverter(),
                            defaultNumber = blockParamModel.sprayOrSeedMu
                        ) {
                            AptypeUtil.setSprayMu(it)
                            blockParamModel.sprayOrSeedMu = it
                        }

                    }
                }
                //飞行速度(m/s)
                item {
                    ParameterDrawerCounterRow(
                        title = stringResource(R.string.flight_speed, UnitHelper.lengthUnit()),
                        min = 0.3f,
                        max = 13.8f,
                        step = 0.5f,
                        fraction = 1,
                        converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                        forceStep = true,
                        defaultNumber = blockParamModel.speed
                    ) {
                        AptypeUtil.setABSpeed(it)
                        blockParamModel.speed = it
                        config.workSpeed = it

                        DroneModel.activeDrone?.setNaviProperties(
                            toUploadData(
                                blockParamModel,
                                routeModel
                            )
                        )
                    }
                }
                //喷头转速
                item {
                    SliderTitleCounter(
                        number = blockParamModel.rotationalSpeed.toFloat(),
                        step = 1f,
                        min = 3f,
                        max = 100f,
                        title = if (isSeedWorkType()) stringResource(R.string.radar_swinging_speed) + "(%):"
                        else stringResource(R.string.spray_nozzle_speed),
                        fraction = 0,
                        titleColor = Color.White,
                        valueColor = Color.White,
                        titleSpace = 0.dp
                    ) {
                        AptypeUtil.setCenAndSeedSpeed(it)
                        blockParamModel.rotationalSpeed = it.toInt()
                    }
                }
                //相对作物高度(m)
                item {
                    ParameterDrawerCounterRow(
                        title = stringResource(
                            R.string.relative_crop_height,
                            UnitHelper.lengthUnit()
                        ),
                        min = 1f,
                        max = 30f,
                        step = 0.5f,
                        fraction = 1,
                        defaultNumber = blockParamModel.relativeHeight,
                        converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter()
                    ) {
                        blockParamModel.relativeHeight = it
                        DroneModel.activeDrone?.setNaviProperties(
                            toUploadData(
                                blockParamModel,
                                routeModel
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SeedMaterialRow(blockParamModel: BlockParamModel, flyingEdit: Boolean = true) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            ParameterDrawerGlobalRowText(
                text = stringResource(id = R.string.seeder_material),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        val items = blockParamModel.materialList.map {
            it.chartData.name
        }.toList()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(color = Color.White, shape = MaterialTheme.shapes.extraSmall)
                .noEffectClickable(enabled = flyingEdit) {
                    context.showDialog {
                        ListSelectionPopup(
                            defaultIndexes = if (blockParamModel.materialIndex >= 0) listOf(
                                blockParamModel.materialIndex
                            ) else listOf(),
                            list = items,
                            item = {
                                AutoScrollingText(text = it, modifier = Modifier.fillMaxWidth())
                            },
                            onConfirm = { indexes, names ->
                                val idx = indexes[0]
                                blockParamModel.setMaterialInfoByIndex(idx)
                                context.hideDialog()
                            },
                            onDismiss = {
                                context.hideDialog()
                            }
                        )
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    AutoScrollingText(text = blockParamModel.materialName, color = Color.Black)
                }
                if (flyingEdit) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .size(20.dp)
                    )
                }
            }
        }
    }
}

private fun toUploadData(blockParamModel: BlockParamModel, routeModel: RouteModel): UploadNaviData {
    val blockType = blockParamModel.selectedBP!!.blockType
    val sprayF = blockParamModel.pumpOrValveSize
    val sprayMu = blockParamModel.sprayOrSeedMu
    val pumpMode = blockParamModel.mode
    val seedSpeed = blockParamModel.rotationalSpeed
    val height = blockParamModel.relativeHeight
    val speed = blockParamModel.speed
    val width = blockParamModel.curRidge
    val d = UploadNaviData(
        if (height == 0f) 3f else height,
        speed,
        width,
        sprayMu,
        sprayF.toFloat(),
        routeModel.naviId,
        blockType
    )//保存规划的时候 只拿航点的经纬度 计算naviId，用处：当飞控里的航线和缓存时不一样时，需要上传新航线
    val track = optimizeRoute(routeModel.route.value ?: emptyList())
    for (p in track) {
        p.height = height
    }
    val cmds = mutableListOf<() -> Unit>()
    cmds.add({ DroneModel.activeDrone?.setNaviProperties(d) })
    cmds.add({ AptypeUtil.setPumpAndValve(sprayF.toFloat()) })
    cmds.add({ AptypeUtil.setSprayMu(sprayMu) })
    cmds.add({ AptypeUtil.setPumpMode(pumpMode) })
    cmds.add({ AptypeUtil.setCenAndSeedSpeed(seedSpeed.toFloat()) })
    d.commands = cmds
    d.route = track
    return d
}

/**
 * 地块内缩
 */
@Composable
fun LandConsolidationRow(blockParamModel: BlockParamModel, routeModel: RouteModel) {
    val context = LocalContext.current
    val config = Config(context)
    val appConfig = AppConfig(context)
    var edgeNumber by remember { mutableIntStateOf(0) }
    var distance by remember {
        mutableFloatStateOf(
            if (blockParamModel.distances.isNotEmpty() && blockParamModel.curEdge < blockParamModel.distances.size) {
                blockParamModel.distances[blockParamModel.curEdge]
            } else config.safetyDistance
        )
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        val names =
            stringArrayResource(id = R.array.air_route_parameter_land_consolidation).toList()
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }

        ParameterDrawerGroupButtonRow(
            title = stringResource(R.string.land_consolidation, UnitHelper.lengthUnit()),
            defaultNumber = edgeNumber,
            names = names,
            values = values
        ) {
            edgeNumber = it
            when (edgeNumber) {
                0 -> {
                    blockParamModel.distances = MutableList(blockParamModel.block.size) { distance }
                    routeModel.calcRoute(routeParameter = blockParamModel.toRouteParameter())
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        ) {
            FloatCounter(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.extraSmall
                ),
                number = distance,
                min = 0f,
                max = 10f,
                step = 0.5f,
                fraction = 1,
                converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
            ) { dist ->
                distance = dist
                when (edgeNumber) {
                    0 -> {
                        blockParamModel.distances =
                            MutableList(blockParamModel.block.size) { distance }
                    }

                    1 -> {
                        blockParamModel.distances[blockParamModel.curEdge] = distance
                    }
                }
                config.safetyDistance = distance
                routeModel.calcRoute(routeParameter = blockParamModel.toRouteParameter())
            }
        }
    }
}

/**
 * 起始点设置
 */
@Composable
fun StartPointSettingRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParameterDrawerGlobalRowText(
            text = stringResource(id = R.string.start_point_setting) + ":",
            modifier = Modifier.weight(1f),
            width = 120.dp,
            color = MaterialTheme.colorScheme.onPrimary
        )
        ParameterDrawerTextButton(
            text = stringResource(R.string.setting),
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}

fun makePlan(
    routeModel: RouteModel, blockParamModel: BlockParamModel, config: Config,
) {
    val track = optimizeRoute(routeModel.route.value ?: emptyList())
    val vkType = WorkUtils.planType2VK2(blockParamModel.planType)//VKType
    val naviArea = routeModel.naviArea.value ?: 0.0
    val workSpeed = blockParamModel.speed
    //1-手动(阀门大小) 2-联动(亩用量)
    val sprayPerMu = blockParamModel.sprayOrSeedMu
    val spray = blockParamModel.pumpOrValveSize.toFloat()
    val cen = blockParamModel.rotationalSpeed.toFloat()
    val relativeHeight = blockParamModel.relativeHeight
    val mode = blockParamModel.mode
    routeModel.naviId = WorkUtils.getNaviId(track)
    val planParam = PlanParamInfo().apply {
        curEdge = blockParamModel.curEdge
        curRidge = blockParamModel.curRidge
        curAngle = blockParamModel.curAngle
//        curRadius=?
        planType = vkType
        barrierSafeDist = blockParamModel.barrierSafeDist
        edgeSafeDist = blockParamModel.distances.toTypedArray()
        seedMu = sprayPerMu
        sprayMu = sprayPerMu
        seedRotateSpeed = cen.roundToInt()
        centrifugalSize = cen.roundToInt()
        pumpSize = spray
        valveSize = spray.roundToInt()
        speed = workSpeed
        seedMode = mode
        pumpMode = mode
        height = relativeHeight
//        maxSpeed = ?
        materialId = blockParamModel.materialId
        materialName = blockParamModel.materialName
    }
    //修改规划
    if (blockParamModel.curPlanType == RouteModel.PLAN_NEW) {
        val height = config.workHeight
        for (rp in track) {
            rp.routeType = vkType
            rp.height = height
        }
        val plan = Plan(
            vkType,
            track,
            blockParamModel.curRidge,
            height,
            workSpeed,
            sprayPerMu,
            spray.roundToInt(),
            blockParamModel.selectedBP?.blockId ?: 0,
            planParam
        )
        plan.localBlockId = blockParamModel.selectedBP?.localBlockId ?: 0
        plan.localPlanId = blockParamModel.selectedBP?.localPlanId ?: 0
        plan.naviArea = naviArea
        blockParamModel.workPlan = plan// 传到后台的数据用PlanType+100
    } else {//修改规划
        for (rp in track) {
            rp.routeType = vkType
            rp.height = blockParamModel.workPlan?.height ?: 2f
        }
        blockParamModel.workPlan?.track = track
        blockParamModel.workPlan?.param = planParam
        blockParamModel.workPlan?.width = blockParamModel.curRidge
        blockParamModel.workPlan?.naviArea = naviArea
        blockParamModel.workPlan?.drugQuantity = sprayPerMu
        blockParamModel.workPlan?.drugFix = spray.roundToInt()
    }
    DroneModel.workRoutePoint = track.toMutableList()
    Log.v(
        "shero",
        "规划参数planParam: ${planParam.toLog()} 规划workPlan：${blockParamModel.workPlan?.param?.toString()}"
    )
}


fun uploadNavi(
    context: Context,
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    complete: (Boolean) -> Unit = {},
) {
    DroneModel.activeDrone?.let {
        it.clearPosData()
        val task = NaviTask(it)
        var home: GeoHelper.LatLngAlt? = null
        it.homeData.value?.apply {
            home = GeoHelper.LatLngAlt(lat, lng, alt)
        }
        task.setParam(home, toUploadData(blockParamModel, routeModel))
        context.startProgress(task) { success, _ ->
            if (success) {
                VoiceMessage.emit(context.getString(com.jiagu.v9sdk.R.string.voice_route_uploaded))
            }
            complete(success)
            false
        }
    }
}

fun savePlan(
    blockParamModel: BlockParamModel,
    complete: () -> Unit = {},
) {//保存规划
    if (blockParamModel.curPlanType == RouteModel.PLAN_NEW //重新规划
        || (blockParamModel.curPlanType == RouteModel.PLAN_LAST_WORK && blockParamModel.selectedBP?.finish == true)
    ) {//已经作业完的地块，继续上次作业，再次上传航线后，认为是重新开始作业
        blockParamModel.saveOrUpdatePlan(isUpdate = false) {
            DroneModel.planId = blockParamModel.workPlan?.planId ?: 0
            DroneModel.localPlanId = blockParamModel.workPlan?.localPlanId ?: 0
            DroneModel.workRoutePoint =
                blockParamModel.workPlan?.track?.toMutableList() ?: mutableListOf()
            complete()
        }
    } else {
        blockParamModel.saveOrUpdatePlan(isUpdate = true) {
            complete()
        }
    }
}

fun updateBlockPlan(blockModel: BlockModel, blockParamModel: BlockParamModel) {
    blockParamModel.getBlockPlan { bp ->
        bp?.let {
            blockParamModel.updateBlockPlan(it)
            blockModel.updateBlockPlan(it)//更新列表页的block的plan
            blockModel.updateBlocksList(listOf(it))
//        blockModel.refresh(blockModel.selectedLocalBlockId)//更新后数据后，刷新整个列表
        }
    }
}

fun offsetRoute(
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    distX: Double,
    distY: Double,
    angle: Float,
) {
    if (blockParamModel.block.isNotEmpty()) {
        val (offsetLat, offsetLng) = blockParamModel.calcDelta(
            distX = distX,
            distY = distY,
            lat = blockParamModel.block[0].latitude,
            lng = blockParamModel.block[0].longitude,
            angle = angle
        )
        blockParamModel.offsetBlockAndObstacles(offsetLat, offsetLng) {
            routeModel.setup(
                blockParamModel.block,
                blockParamModel.obstacles,
                blockParamModel.barrierSafeDist
            )
            routeModel.calcRoute(
                byAngle = routeModel.lastCalcByAngle,
                routeParameter = blockParamModel.toRouteParameter()
            )
        }
    }
}

fun initRouteByConfig(
    blockPlan: BlockPlan,
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    config: Config,
) {
    val blockSize = blockPlan.boundary[0].size
    routeModel.initParam()
    blockParamModel.distances = MutableList(blockSize) { config.safetyDistance }
    blockParamModel.barrierSafeDist = config.safetyDistanceBarrier
    blockParamModel.curRidge = config.workRidge
    blockParamModel.smartDist = config.smartPlanDist
    blockParamModel.planType = RouteModel.PLAN_BLOCK
    blockParamModel.initBlockAndObstacles(blockPlan) {
        routeModel.setup(
            blockParamModel.block,
            blockParamModel.obstacles,
            blockParamModel.barrierSafeDist
        )
    }
    val routeParameter = blockParamModel.toRouteParameter()
    val plan = blockPlan.plan
    if (plan != null) {
        val route = plan.track
        if (route.isNotEmpty()) routeModel.home = route[0]//使用上次航线的第一个点作为home,规划航线第一次进来要保证SE一样
        if (blockPlan.blockType == Block.TYPE_BLOCK) {
            if (blockPlan.sortieRoute?.route.isNullOrEmpty()) {
                if (plan.track.isNotEmpty()) routeModel.makeNaviPts(
                    list = plan.track,
                    routeParameter = routeParameter
                ) {//恢复routemodel规划参数
                    routeModel.calcRoute(byAngle = true, routeParameter = routeParameter)
                }
            } else {
                routeModel.makeNaviPts(
                    list = blockPlan.sortieRoute?.route!!,
                    routeParameter = routeParameter
                ) {}//航线重排的地块，不要重新规划，否则会导致航线不对
            }
        }
        plan.param?.let { param ->
            val paramBarrierSize = param.edgeSafeDist.toMutableList().size
            val t = if (param.planType >= 100) param.planType - 100 else param.planType
            val planType = WorkUtils.vk2PlanType(t)
            blockParamModel.updateRouteByPlanParam(param, planType, blockSize, paramBarrierSize)
        }
    } else {
        routeModel.calcRoute(byAngle = true, routeParameter = routeParameter)
    }
}
