package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.ABCleanModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.model.UploadNaviData
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkABClean(route: String) {
    val navController = LocalNavController.current
    val mapVideoModel = LocalMapVideoModel.current
    val context = LocalContext.current
    val abCleanModel = remember { navController.getViewModel(route, ABCleanModel::class.java) }
    val locationModel = remember { navController.getViewModel(route, LocationModel::class.java) }
    val config = Config(context)
    val blockPoints2D by abCleanModel.blockPoints2D.collectAsState()
    val workPoints2D by abCleanModel.workPoints2D.collectAsState()
    //实时飞机信息
    val imuData by DroneModel.imuData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            abCleanModel.processImuData(it)
        }
    }
    //用于起飞后页面返回显示断点用、
    val breakPoint by DroneModel.breakPoint.observeAsState()
    LaunchedEffect(breakPoint) {
        abCleanModel.setBK(breakPoint)
    }
    DisposableEffect(Unit) {
        //设置打点方式为飞机打点
        mapVideoModel.changeLocationType("drone")
        locationModel.setup()
        //初始化速度参数赋值
        abCleanModel.speed = config.cleanSpeed
        onDispose {
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //作业参数
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
                dronePosition = abCleanModel.dronePoint2D,
                isClosed = false,
                breakPoint = abCleanModel.breakPoint2D
            )
        }
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //打点
                RightButtonCommon(
                    text = stringResource(id = R.string.measure_dot),
                    enabled = abCleanModel.points.size < 2
                ) {
                    if (abCleanModel.routeYaw == null || abCleanModel.points.isEmpty()
                    ) {
                        abCleanModel.routeYaw = DroneModel.imuData.value?.yaw
                    }
                    locationModel.location.value?.let {
                        LogFileHelper.log(
                            "locationVM: ${it.lat} ${it.lng} " +
                                    "imu: ${DroneModel.imuData.value?.lat} ${DroneModel.imuData.value?.lng}"
                        )
                        abCleanModel.addPoint(
                            GeoHelper.LatLngAlt(
                                it.lat,
                                it.lng,
                                DroneModel.imuData.value?.height?.toDouble() ?: 0.0
                            )
                        )
                        if (abCleanModel.points.size == 2) {
                            abCleanModel.genPointAndCalc()
                        }
                    }
                }
                RightButtonCommon(
                    enabled = abCleanModel.points.isNotEmpty(),
                    text = stringResource(id = R.string.clear)
                ) {
                    abCleanModel.removeLastPoint()
                }
                Spacer(modifier = Modifier.weight(1f))
                //执行
                RightButtonCommon(
                    text = stringResource(id = R.string.start),
                    enabled = abCleanModel.points.size == 2
                ) {
                    //清除断点，AB清洗每次起飞都是新的航线
                    abCleanModel.clearBreakpoint()
                    //上传航线
                    val task = abCleanModel.uploadNavi()
                    task?.let {
                        context.startProgress(task) { success, msg ->
                            if (success) {
                                VoiceMessage.emit(context.getString(com.jiagu.v9sdk.R.string.voice_route_uploaded))
                                context.showDialog {
                                    CleanFlyConfirmPopup(
                                        width = abCleanModel.width,
                                        repeatCount = abCleanModel.repeatCount,
                                        speed = abCleanModel.speed,
                                        confirm = {
                                            DroneModel.activeDrone?.takeOff2Clean()
                                            navController.navigate(WorkPageEnum.WORK_AB_CLEAN_START.url)
                                            context.hideDialog()
                                        },
                                        cancel = {
                                            context.hideDialog()
                                        }
                                    )
                                }
                                true
                            }
                            false
                        }
                    }
                }
            }
        )
    }
}

/**
 * Drawer content a b clean
 *
 * @param modifier
 * @param abCleanModel
 * @param flyingEdit true 编辑参数页面  false 起飞页面
 */
@Composable
fun DrawerContentABClean(
    modifier: Modifier = Modifier,
    abCleanModel: ABCleanModel,
    flyingEdit: Boolean = true,
) {
    val context = LocalContext.current
    val config = Config(context)
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        //作业行距
        item {
            ParameterDrawerCounterRow(
                title = stringResource(R.string.job_line_spacing, UnitHelper.lengthUnit()),
                min = 0.3f,
                max = 30f,
                step = 0.5f,
                fraction = 1,
                converter = if (AppConfig(context).lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                forceStep = true,
                editEnabled = flyingEdit,
                defaultNumber = abCleanModel.width
            ) {
                abCleanModel.width = it
                if (abCleanModel.points.size == 2) {
                    abCleanModel.genPointAndCalc()
                }
            }
        }
        //单条航线重复次数
        item {
            ParameterDrawerCounterRow(
                title = stringResource(R.string.job_line_repeat_count),
                min = 0f,
                max = 10f,
                step = 1f,
                fraction = 0,
                editEnabled = flyingEdit,
                defaultNumber = abCleanModel.repeatCount.toFloat()
            ) {
                abCleanModel.repeatCount = it.toInt()
                if (abCleanModel.points.size == 2) {
                    abCleanModel.genPointAndCalc()
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
                defaultNumber = abCleanModel.speed
            ) {
                abCleanModel.speed = it
                config.cleanSpeed = it
                if (!flyingEdit) {
                    DroneModel.activeDrone?.setNaviProperties(
                        UploadNaviData(
                            0f,
                            it,
                            abCleanModel.width,
                            0f,
                            AptypeUtil.getPumpAndValve(), 0, 2
                        )
                    )
                }
            }
        }
    }
}