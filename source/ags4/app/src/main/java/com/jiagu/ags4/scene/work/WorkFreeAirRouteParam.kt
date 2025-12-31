package com.jiagu.ags4.scene.work

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.Constants
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.Plan
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.WorkUtils
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.utils.toSingedString
import com.jiagu.tools.ext.UnitHelper
import kotlin.math.roundToInt


@Composable
fun WorkFreeAirRouteParam(freeAirRoute: String, freeAirParamRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val freeAirRouteModel =
        remember { navController.getViewModel(freeAirRoute, FreeAirRouteModel::class.java) }
    val freeAirRouteParamModel = remember {
        navController.getViewModel(freeAirParamRoute, FreeAirRouteParamModel::class.java)
    }
    val taskModel = remember {
        navController.getViewModel(freeAirParamRoute, TaskModel::class.java)
    }
    val config = Config(context)

    DisposableEffect(Unit) {
        //selectedBP 不为null说明是编辑
        freeAirRouteModel.selectedBP?.let {
            if (!freeAirRouteParamModel.isInit) {
                freeAirRouteParamModel.isInit = true
                freeAirRouteParamModel.initBlockPlanAndParam(it, freeAirRouteModel.isNewPlan)
                if (!freeAirRouteModel.isNewPlan) {
                    taskModel.setBreakPoint(freeAirRouteModel.breakPoint)
                } else {
                    taskModel.clearBK()
                }
            }
        }
        onDispose {}
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
                        flyingEdit = freeAirRouteModel.isNewPlan
                    )
                } else {
                    if (freeAirRouteModel.isNewPlan) {
                        DrawerContentFreeAirRouteParamAll(
                            mapVideoModel = mapVideoModel,
                            freeAirRouteParamModel = freeAirRouteParamModel
                        )
                    } else {
                        DrawerContentJobParam(freeAirRouteParamModel = freeAirRouteParamModel)
                    }
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
                Spacer(modifier = Modifier.weight(1f))
                //上传航线
                RightButtonCommon(text = stringResource(id = R.string.upload_route)) {
                    freeAirRouteParamModel.workTrack.forEach { rp ->
                        rp.height += freeAirRouteParamModel.heightDif
                        rp.elevation += freeAirRouteParamModel.heightDif
                        if (rp.height < 2f) rp.height = 2f
                    }
                    makeFreeAirRoutePlan(freeAirRouteParamModel, config)//根据飞控数据更新作业参数
                    uploadAirRouteNavi(
                        freeAirRouteParamModel = freeAirRouteParamModel, context = context
                    ) { success ->
                        if (success) {
                            mapVideoModel.needUploadNavi = false//上传完航线后就不需要再上传了
                            context.showDialog {
                                FreeAirRouteFlyConfirmPopup(
                                    mapVideoModel = mapVideoModel,
                                    freeAirRouteParamModel = freeAirRouteParamModel,
                                    confirm = {////FlyConfirmPopup点击confirm时，已经发了起飞去作业的命令
                                        //编辑参数页面，根据类型判断是保存还是更新plan
                                        saveFreeAirRoutePlan(freeAirRouteParamModel) {//保存规划
                                            freeAirRouteParamModel.blockWorking()//并将地块标记为进行中地块
                                            updateFreeAirRoutePlan(
                                                freeAirRouteModel, freeAirRouteParamModel
                                            )
                                            freeAirRouteModel.deleteLocalBreakpoint()//删除原地块断点
                                            navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_START.url)
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
                Spacer(modifier = Modifier.weight(1f))
            })
    }
}

private fun uploadAirRouteNavi(
    freeAirRouteParamModel: FreeAirRouteParamModel,
    context: Context,
    complete: (Boolean) -> Unit = {},
) {
    DroneModel.activeDrone?.let {
        it.clearPosData()
        val task = NaviTask(it)
        var home: GeoHelper.LatLngAlt? = null
        it.homeData.value?.apply {
            home = GeoHelper.LatLngAlt(lat, lng, alt)
        }
        //目前第一个点航点高度设置无效，会按照航线高度
        val naviData = toFreeAirRouteUploadData(freeAirRouteParamModel)
        task.setParam(home, naviData)

        context.startProgress(task) { success, _ ->
            if (success) {
                VoiceMessage.emit(context.getString(com.jiagu.v9sdk.R.string.voice_route_uploaded))
            }
            complete(success)
            false
        }
    }
}

@Composable
fun DrawerContentFreeAirRouteParamAll(
    mapVideoModel: MapVideoModel,
    freeAirRouteParamModel: FreeAirRouteParamModel,
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
                mapVideoModel = mapVideoModel, freeAirRouteParamModel = freeAirRouteParamModel
            )
        } else {//作业参数
            DrawerContentJobParam(freeAirRouteParamModel = freeAirRouteParamModel)
        }
    }
}

@Composable
private fun DrawerContentAirRouteParam(
    mapVideoModel: MapVideoModel,
    freeAirRouteParamModel: FreeAirRouteParamModel,
) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val airPointHeightNames = listOf(
        stringResource(id = R.string.relative_height), stringResource(id = R.string.altitude)
    )
    var pointHeight by remember { mutableFloatStateOf(0f) }
    var pointHeightType by remember { mutableIntStateOf(freeAirRouteParamModel.pointHeightType) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //自由航线显示航线喷洒，果树航线不显示
        if (mapVideoModel.workModeEnum == WorkModeEnum.FREE_AIR_ROUTE) {
            //航线喷洒
            item {
                val airRouteSprayNames =
                    listOf(stringResource(id = R.string.close), stringResource(id = R.string.open))
                ParameterDrawerGroupButtonRow(
                    title = stringResource(id = R.string.air_route_spray_all),
                    defaultNumber = freeAirRouteParamModel.naviPump,
                    names = airRouteSprayNames,
                    values = listOf(0, 1)
                ) {
                    freeAirRouteParamModel.naviPump = it
                    freeAirRouteParamModel.workTrack.forEach { rp ->
                        rp.wlMission = if (it == 1) {
                            VKAgCmd.WL_MISSION_PUMP.toInt()//航线喷洒-开
                        } else {
                            VKAgCmd.WL_MISSION_NONE.toInt()
                        }
                    }
                }
            }
        }
        //航点高度类型
        item {
            ParameterDrawerGroupButtonRow(
                title = stringResource(id = R.string.air_point_height_type_all),
                defaultNumber = freeAirRouteParamModel.pointHeightType,
                names = airPointHeightNames,
                values = listOf(0, 1)
            ) {
                freeAirRouteParamModel.pointHeightType = it
                var sum = 0f
                freeAirRouteParamModel.workTrack.forEach {
                    sum += it.height
                }
                val avgHeight = (sum / freeAirRouteParamModel.workTrack.size).roundToInt()
                context.showDialog {
                    PromptPopup(
                        content = stringResource(
                        id = R.string.point_height_type_change_confirm,
                        avgHeight,
                        DroneModel.imuData.value?.alt?.toInt() ?: 0,
                        airPointHeightNames[it]
                    ), onConfirm = {
                        freeAirRouteParamModel.workTrack.forEach { rp ->
                            rp.heightType = it
                        }
                        pointHeightType = it
                        context.hideDialog()
                    }, onDismiss = {
                        freeAirRouteParamModel.pointHeightType = pointHeightType
                        context.hideDialog()
                    })
                }
            }
        }
        //高度加减全部(m)
        item {
            SliderTitleCounter(
                title = stringResource(
                    id = R.string.height_add_and_subtract_all, UnitHelper.lengthUnit()
                ),
                numberPrefix2 = "+",
                number = freeAirRouteParamModel.heightDif,
                min = -20f,
                max = 20f,
                step = 1f,
                fraction = 0,
                titleColor = Color.White,
                valueColor = Color.White,
                converter = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
            ) {
                freeAirRouteParamModel.heightDif = it
                context.showDialog {
                    PromptPopup(
                        content = stringResource(
                        id = R.string.modify_all_waypoints_height_confirm,
                        airPointHeightNames[freeAirRouteParamModel.pointHeightType],
                        if (it > 0) "+${it.toString(0)}" else it.toString(0)
                    ), onConfirm = {
                        freeAirRouteParamModel.heightDif = it
                        pointHeight = it
                        context.hideDialog()
                    }, onDismiss = {
                        freeAirRouteParamModel.heightDif = pointHeight
                        context.hideDialog()
                    })
                }
            }
        }
        //转弯方式
        item {
            val turningNames = listOf(
                stringResource(id = R.string.automatic_turning),
                stringResource(id = R.string.hover_turning)
            )
            ParameterDrawerGroupButtonRow(
                title = stringResource(
                    id = R.string.turning_method
                ) + "(" + stringResource(
                    id = R.string.block_filter_all
                ) + ")",
                defaultNumber = freeAirRouteParamModel.pointPump,
                names = turningNames,
                values = listOf(0, 1)
            ) {
                freeAirRouteParamModel.pointPump = it
                freeAirRouteParamModel.workTrack.forEach { rp ->
                    rp.pump = it == 1
                    rp.wpParam = if (rp.pump) freeAirRouteParamModel.pointPumpTime else 0
                }
            }
        }
        //航点喷洒时间
        if (freeAirRouteParamModel.pointPump == 1) {
            item {
                SliderTitleCounter(
                    title = stringResource(id = R.string.air_point_spray_time_all),
                    number = freeAirRouteParamModel.pointPumpTime.toFloat(),
                    min = 0f,
                    max = 60f,
                    step = 1f,
                    fraction = 0,
                    titleColor = Color.White,
                    valueColor = Color.White,
                ) {
                    freeAirRouteParamModel.pointPumpTime = it.toInt()
                    freeAirRouteParamModel.workTrack.forEach { rp ->
                        rp.wpParam = if (rp.pump) it.toInt() else 0
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContentJobParam(
    freeAirRouteParamModel: FreeAirRouteParamModel,
) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val config = Config(context)
    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //喷洒模式 -固定
        //流量大小
        item {
            SliderTitleCounter(
                number = freeAirRouteParamModel.pumpOrValveSize.toFloat(),
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
                freeAirRouteParamModel.pumpOrValveSize = it.toInt()
            }
        }
        //飞行速度(m/s)
        item {
            ParameterDrawerCounterRow(
                title = stringResource(R.string.flight_speed, UnitHelper.lengthUnit()),
                min = 0.3f,
                max = Constants.MAX_SPEED,
                step = 0.5f,
                fraction = 1,
                defaultNumber = freeAirRouteParamModel.speed,
                forceStep = true,
                converter = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
            ) {
                config.workSpeed = it
                freeAirRouteParamModel.speed = it
                DroneModel.activeDrone?.setNaviProperties(
                    toFreeAirRouteUploadData(
                        freeAirRouteParamModel
                    )
                )
            }
        }
    }
}

@Composable
fun DrawerContentSingleAirRouteParam(
    mapVideoModel: MapVideoModel,
    freeAirRouteParamModel: FreeAirRouteParamModel,
    flyingEdit: Boolean = true,
) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    var naviPump by remember { mutableIntStateOf(0) }
    var pointPump by remember { mutableIntStateOf(0) }
    var pointHeight by remember { mutableFloatStateOf(0f) }
    var sprayTime by remember { mutableIntStateOf(5) }

    val airPointHeightNames = listOf(
        stringResource(id = R.string.relative_height), stringResource(id = R.string.altitude)
    )

    LaunchedEffect(key1 = freeAirRouteParamModel.selectedMarkerIndex) {
        if (freeAirRouteParamModel.selectedMarkerIndex != -1) {
            val curRoutePoint =
                freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex]
            val changePoint = WorkUtils.getNaviTree(curRoutePoint)
            naviPump = changePoint.naviPump
            pointPump = changePoint.pointPump
            sprayTime = changePoint.sprayTime
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //当前航点
        item {
            ParameterDrawerTextRow(
                title = stringResource(id = R.string.current_waypoint),
                content = "${freeAirRouteParamModel.selectedMarkerIndex + 1}"
            )
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 2.dp),
                color = Color.White
            )
        }
        //上一个点 / 下一个点
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.width(80.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        freeAirRouteParamModel.changeMarkerPoint()
                    }) {
                    Text(
                        text = stringResource(id = R.string.previous),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
                Button(
                    modifier = Modifier.width(80.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        freeAirRouteParamModel.changeMarkerPoint(true)
                    }) {
                    Text(
                        text = stringResource(id = R.string.next),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
            }
        }
        //自由航线显示航线喷洒，果树航线不显示
        if (mapVideoModel.workModeEnum == WorkModeEnum.FREE_AIR_ROUTE) {
            //航线喷洒
            item {
                val airRouteSprayNames =
                    listOf(stringResource(id = R.string.close), stringResource(id = R.string.open))
                ParameterDrawerGroupButtonRow(
                    title = stringResource(id = R.string.air_route_spray),
                    values = listOf(0, 1),
                    names = airRouteSprayNames,
                    defaultNumber = naviPump,
                    enabled = flyingEdit
                ) {
                    naviPump = it
                    freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].apply {
                        wlMission = if (it == 1) {
                            VKAgCmd.WL_MISSION_PUMP.toInt()//航线喷洒-开
                        } else {
                            VKAgCmd.WL_MISSION_NONE.toInt()
                        }
                    }
                    //检查所有航点轨迹中是否存在至少一个开启喷洒（pump为true）的航点，只要有一个航点开启喷洒，则全局开关设为1（开），否则设为0（关）
                    freeAirRouteParamModel.naviPump = if (freeAirRouteParamModel.workTrack.any {r -> r.wlMission ==  VKAgCmd.WL_MISSION_PUMP.toInt() }) { 1 } else { 0 }
                }
            }

        }
        //航点高度类型 只能通过 航点高度类型(全部) 去修改 单点不能修改
        item {
            ParameterDrawerTextRow(
                title = stringResource(id = R.string.air_point_height_type),
                content = airPointHeightNames[freeAirRouteParamModel.pointHeightType]
            )
        }
        //航点高度(m)
        item {
            SliderTitleCounter(
                title = stringResource(
                    id = R.string.height_add_and_subtract, UnitHelper.lengthUnit()
                ),
                numberPrefix = "(${
                    freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].height.toString(
                        0
                    )
                }${freeAirRouteParamModel.heightDif.toInt().toSingedString()})",
                numberPrefix2 = "+",
                number = pointHeight,
                min = -(freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].height - 2),
                max = 20f,
                step = 1f,
                fraction = 0,
                titleColor = Color.White,
                valueColor = Color.White,
                converter = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                enabled = flyingEdit,
                onConfirm = {
                    pointHeight = it
                    context.showDialog {
                        PromptPopup(
                            content = stringResource(id = R.string.current_waypoint) + airPointHeightNames[freeAirRouteParamModel.pointHeightType] + "(${
                            if (it > 0) "+${
                                it.toString(
                                    0
                                )
                            }" else it.toString(0)
                        })", onConfirm = {
                            freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].apply {
                                elevation += it
                                height += it
                                if (height < 2f) height = 2f
                            }
                            pointHeight = 0f
                            context.hideDialog()
                        }, onDismiss = {
                            pointHeight = 0f
                            context.hideDialog()
                        })
                    }
                })
        }
        //转弯方式
        item {
            val turningNames = listOf(
                stringResource(id = R.string.automatic_turning),
                stringResource(id = R.string.hover_turning)
            )
            ParameterDrawerGroupButtonRow(
                title = stringResource(id = R.string.turning_method),
                values = listOf(0, 1),
                names = turningNames,
                defaultNumber = pointPump,
                enabled = flyingEdit
            ) {
                pointPump = it
                freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].apply {
                    pump = it == 1
                }
                //检查是否存在至少一个开启喷洒（wlMission为1）的航点，只要有一个航点开启航线喷洒，则全局设为1（开），只有当所有航点都关闭喷洒（wlMission=0）时才设为0（关）。
                freeAirRouteParamModel.pointPump = if (freeAirRouteParamModel.workTrack.any {r -> r.pump }) { 1 } else { 0 }
            }
        }
        //航点喷洒时间
        if (pointPump == 1) {
            item {
                SliderTitleCounter(
                    title = stringResource(id = R.string.air_point_spray_time),
                    number = sprayTime.toFloat(),
                    min = 0f,
                    max = 60f,
                    fraction = 0,
                    step = 1f,
                    titleColor = Color.White,
                    valueColor = Color.White,
                    enabled = flyingEdit
                ) {
                    sprayTime = it.toInt()
                    freeAirRouteParamModel.workTrack[freeAirRouteParamModel.selectedMarkerIndex].apply {
                        //自由航点,航线喷，航点不喷 vm.wayLineType == 1
                        wpParam = if (pump) it.toInt() else 0
                    }
                }
            }
        }
    }
}

fun makeFreeAirRoutePlan(
    freeAirRouteParamModel: FreeAirRouteParamModel, config: Config,
) {
    val track = freeAirRouteParamModel.workTrack
    val naviArea = 0.0
    val workSpeed = freeAirRouteParamModel.speed
    //1-手动(阀门大小) 2-联动(亩用量)
    val sprayPerMu = freeAirRouteParamModel.sprayOrSeedMu
    val spray = freeAirRouteParamModel.pumpOrValveSize.toFloat()
    val cen =freeAirRouteParamModel.rotationalSpeed.toFloat()
    val planParam = PlanParamInfo().apply {
        seedRotateSpeed = cen.roundToInt()
        centrifugalSize = cen.roundToInt()
        seedMu = sprayPerMu
        sprayMu = sprayPerMu
        pumpSize = spray
        valveSize = spray.roundToInt()
        speed = workSpeed
    }
    //修改规划
    if (freeAirRouteParamModel.curPlanType == RouteModel.PLAN_NEW) {
        val height = config.workHeight
        val plan = Plan(
            freeAirRouteParamModel.wayLineType,
            track,
            0f,
            height,
            workSpeed,
            sprayPerMu,
            spray.roundToInt(),
            freeAirRouteParamModel.selectedBP?.blockId ?: 0,
            planParam
        )
        plan.localBlockId = freeAirRouteParamModel.selectedBP?.localBlockId ?: 0
        plan.localPlanId = freeAirRouteParamModel.selectedBP?.localPlanId ?: 0
        plan.naviArea = naviArea
        freeAirRouteParamModel.workPlan = plan// 传到后台的数据用PlanType+100
    } else {//修改规划
        freeAirRouteParamModel.workPlan?.track = track
        freeAirRouteParamModel.workPlan?.param = planParam
        freeAirRouteParamModel.workPlan?.width = 0f
        freeAirRouteParamModel.workPlan?.naviArea = naviArea
        freeAirRouteParamModel.workPlan?.drugQuantity = sprayPerMu
        freeAirRouteParamModel.workPlan?.drugFix = spray.roundToInt()
    }
    DroneModel.workRoutePoint = track.toMutableList()

}

private fun toFreeAirRouteUploadData(
    freeAirRouteParamModel: FreeAirRouteParamModel,
): UploadNaviData {
    val height = DroneModel.aptypeData.value?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT) ?: 3f //自动起飞高度
    val sprayF = freeAirRouteParamModel.pumpOrValveSize.toFloat()
    val speed = freeAirRouteParamModel.speed
    val track = freeAirRouteParamModel.workTrack.toList()
    track.forEach { rp ->
        if (!rp.pump) {
            rp.wpParam = 0
        }
    }
    //保存规划的时候 只拿航点的经纬度 计算naviId，用处：当飞控里的航线和缓存时不一样时，需要上传新航线
    val uploadNaviData = UploadNaviData(
        height,
        speed,
        width = 0f,
        qty = 0f,
        fixed = sprayF,
        naviId = WorkUtils.getNaviId(track),
        naviType = Block.TYPE_TRACK
    )
    uploadNaviData.speed = speed
    val cmds = mutableListOf<() -> Unit>()
    cmds.add { DroneModel.activeDrone?.setNaviProperties(uploadNaviData) }
    cmds.add { AptypeUtil.setPumpAndValve(sprayF) }
    cmds.add { AptypeUtil.setPumpMode(VKAg.BUMP_MODE_FIXED) }//默认固定%格式
    uploadNaviData.commands = cmds
    uploadNaviData.route = track
    Log.d("shero", "air upload navi:$uploadNaviData")
    return uploadNaviData
}

private fun saveFreeAirRoutePlan(
    freeAirRouteParamModel: FreeAirRouteParamModel,
    complete: () -> Unit = {},
) {//保存规划
    if (freeAirRouteParamModel.curPlanType == RouteModel.PLAN_NEW //重新规划
        || (freeAirRouteParamModel.curPlanType == RouteModel.PLAN_LAST_WORK && freeAirRouteParamModel.selectedBP?.finish == true)
    ) {//已经作业完的地块，继续上次作业，再次上传航线后，认为是重新开始作业
        freeAirRouteParamModel.saveOrUpdatePlan(isUpdate = false) {
            DroneModel.planId = freeAirRouteParamModel.workPlan?.planId ?: 0
            DroneModel.localPlanId = freeAirRouteParamModel.workPlan?.localPlanId ?: 0
            DroneModel.workRoutePoint =
                freeAirRouteParamModel.workPlan?.track?.toMutableList() ?: mutableListOf()
            complete()
        }
    } else {
        freeAirRouteParamModel.saveOrUpdatePlan(isUpdate = true) {
            complete()
        }
    }
}

fun updateFreeAirRoutePlan(
    freeAirRouteModel: FreeAirRouteModel,
    freeAirRouteParamModel: FreeAirRouteParamModel,
) {
    freeAirRouteParamModel.getBlockPlan { bp ->
        bp?.let {
            freeAirRouteParamModel.updateBlockPlan(it)
            freeAirRouteModel.updateBlockPlan(it)
            freeAirRouteModel.updateBlocksList(listOf(it))
//        freeAirRouteModel.refresh(freeAirRouteModel.selectedLocalBlockId)//更新后数据后，刷新整个列表
        }
    }
}