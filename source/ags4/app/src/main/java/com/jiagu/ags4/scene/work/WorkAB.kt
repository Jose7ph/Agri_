package com.jiagu.ags4.scene.work

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.seedTitle
import com.jiagu.ags4.utils.sprayTitle
import com.jiagu.ags4.vm.ABModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.task.CommandsTask
import com.jiagu.api.ext.toast
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.WarningPopup
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkAB(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = context as MapVideoActivity
    val abModel = remember { navController.getViewModel(route, ABModel::class.java) }
    val imuData by DroneModel.imuData.observeAsState()
    val abStatus by DroneModel.imuData.map { it?.ABStatus }.distinctUntilChanged().observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            abModel.processImuData(it)
            if(abModel.calcBreaks.value.isNotEmpty()){ //AB打点页面不显示的计算的断点1、2
                abModel.clearABBreak()
                abModel.clearBreakIndexAndList()
            }
        }
    }
    LaunchedEffect(abStatus) {
        //ab点状态为无AB时，清除model数据 ，相当于点了清除AB点按钮
        if (abStatus == VKAgCmd.AB_STA_NO.toShort()) {
            abModel.clearModel()
        }
    }
    //获取断点 当通过返回按钮触发悬停时显示断点用
    val abplData by DroneModel.abplData.observeAsState()
    LaunchedEffect(abplData) {
        abplData?.let {
            abModel.setABPLData(it)
        }
    }
    //AB点数据
    val abData by DroneModel.abData.observeAsState()
    LaunchedEffect(abData) {
        abData?.let {
            abModel.collectABData(it, imuData?.flyMode) {
                activity.removeCalcBreaksByIndex(abModel.selectBreakIndex)
            }
        }
    }
    DisposableEffect(Unit) {
        aptypeData?.let {
            abModel.setAptypeData(it)
        }
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val abButtonWidth = 80.dp
        val abButtonHeight = 33.dp
        val buttonImageSize = 20.dp
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //A点
                RightButtonCommon(
                    buttonWidth = abButtonWidth,
                    buttonHeight = abButtonHeight,
                    text = "A",
                    enabled = abStatus == VKAgCmd.AB_STA_NO.toShort() || abStatus == VKAgCmd.AB_STA_A.toShort() || abStatus == VKAgCmd.AB_STA_AL.toShort(),
                    leftImage = {
                        Image(
                            painter = painterResource(id = R.drawable.default_work_ab_locate),
                            contentDescription = "position A",
                            modifier = Modifier.size(buttonImageSize),
                            colorFilter = ColorFilter.tint(color = Color.Black)
                        )
                    }) {
                    DroneModel.activeDrone?.setAPoint()
                }
                //A角度
                RightButtonCommon(
                    buttonWidth = abButtonWidth,
                    buttonHeight = abButtonHeight,
                    text = "A°",
                    enabled = abStatus == VKAgCmd.AB_STA_A.toShort() || abStatus == VKAgCmd.AB_STA_AL.toShort(),
                    leftImage = {
                        Image(
                            painter = painterResource(id = R.drawable.default_work_ab_angle),
                            contentDescription = "angle A",
                            modifier = Modifier.size(buttonImageSize),
                            colorFilter = ColorFilter.tint(color = Color.Black)
                        )
                    }) {
                    DroneModel.activeDrone?.setAAngle()
                }
                //B点
                RightButtonCommon(
                    buttonWidth = abButtonWidth,
                    buttonHeight = abButtonHeight,
                    text = "B",
                    enabled = abStatus == VKAgCmd.AB_STA_A.toShort() || abStatus == VKAgCmd.AB_STA_AL.toShort() || abStatus == VKAgCmd.AB_STA_AB.toShort() || abStatus == VKAgCmd.AB_STA_LOCK.toShort(),
                    leftImage = {
                        Image(
                            painter = painterResource(id = R.drawable.default_work_ab_locate),
                            contentDescription = "position B",
                            modifier = Modifier.size(buttonImageSize),
                            colorFilter = ColorFilter.tint(color = Color.Black)
                        )
                    }) {
                    abModel.setPointBFlag = true
                    DroneModel.activeDrone?.setBPoint()
                }
                //B角度
                RightButtonCommon(
                    buttonWidth = abButtonWidth,
                    buttonHeight = abButtonHeight,
                    text = "B°",
                    enabled = abStatus == VKAgCmd.AB_STA_AB.toShort() || abStatus == VKAgCmd.AB_STA_LOCK.toShort(),
                    leftImage = {
                        Image(
                            painter = painterResource(id = R.drawable.default_work_ab_angle),
                            contentDescription = "angle B",
                            modifier = Modifier.size(buttonImageSize),
                            colorFilter = ColorFilter.tint(color = Color.Black)
                        )
                    }) {
                    //由于打B点和打B点角度，ABStatus状态值一样，所以打B点角度时，把当前值修改掉，
                    // 这样打完角度后会根据两个角度计算一个最后方向，并画AB线，并设置AB方向
                    DroneModel.activeDrone?.setBAngle()
                }
                //左右箭头
                RightButtonCommon(
                    buttonWidth = abButtonWidth,
                    buttonHeight = abButtonHeight,
                    text = "",
                    enabled = abStatus == VKAgCmd.AB_STA_AB.toShort() || abStatus == VKAgCmd.AB_STA_LOCK.toShort(),
                    leftImage = {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.default_work_ab_left2),
                                contentDescription = "ab left arrow",
                                modifier = Modifier.size(buttonImageSize),
                                colorFilter = ColorFilter.tint(color = if (abModel.toLeft == true) MaterialTheme.colorScheme.primary else Color.Black)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.default_work_ab_right2),
                                contentDescription = "ab right arrow",
                                modifier = Modifier.size(buttonImageSize),
                                colorFilter = ColorFilter.tint(color = if (abModel.toLeft == false) MaterialTheme.colorScheme.primary else Color.Black)
                            )
                        }
                    }) {
                    abModel.toLeft = !abModel.toLeft
                    abModel.changeABDir()
                }
                //清除
                RightButtonCommon(
                    text = stringResource(
                        id = R.string.clear
                    )
                ) {
                    DroneModel.activeDrone?.clearAB()
                }
                //执行
                RightButtonCommon(
                    text = stringResource(id = R.string.execute),
                    enabled = abStatus == VKAgCmd.AB_STA_AB.toShort() || abStatus == VKAgCmd.AB_STA_LOCK.toShort()
                ) {
                    context.showDialog {
                        WarningPopup(title = stringResource(R.string.fly_confirm), onConfirm = {
                            DroneModel.activeDrone?.takeOff2Ab()
                            navController.navigate(WorkPageEnum.WORK_AB_START.url)
                            context.hideDialog()
                        }, onDismiss = {
                            context.hideDialog()
                        })
                    }
                }
            })
        //作业参数
        Drawer(modifier = Modifier.padding(top = STATUS_BAR_HEIGHT), color = BlackAlpha, context = {
            DrawerContentAB(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(LEFT_DRAWER_MAX_WIDTH)
                    .padding(horizontal = 10.dp, vertical = 6.dp), abModel = abModel
            )
        }, isShow = mapVideoModel.showParam, onShow = {
            mapVideoModel.showParam = true
            mapVideoModel.hideInfoPanel()
        }, onClose = {
            mapVideoModel.showParam = false
        })
    }
}

@Composable
fun DrawerContentAB(
    modifier: Modifier = Modifier,
    abModel: ABModel,
    flyingEdit: Boolean = true,
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        if (abModel.isJobLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                //模板选择
                item {
                    TemplateSelect(
                        templateName = abModel.templateName.ifEmpty { context.getString(R.string.unused_template) },
                        enabled = flyingEdit,
                        onDeleteTemplate = { id, complete ->
                            abModel.removeTemplate(id) { success ->
                                if (success) {
                                    complete()
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onSave = { name ->
                            abModel.saveTemplateData(name = name) { success ->
                                if (success) {
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onClick = { complete ->
                            abModel.getTemplateParamList { success, data ->
                                if (!success) {
                                    context.toast(context.getString(R.string.fail))
                                    return@getTemplateParamList
                                }

                                data?.let { complete(it) } ?: return@getTemplateParamList
                            }
                        },
                        onConfirm = { templateParam ->
                            abModel.setTemplate(templateParam) { success, commands ->
                                if (success) {
                                    if (!commands.isNullOrEmpty()) {
                                        DroneModel.activeDrone?.let { activeDrone ->
                                            abModel.isJobLoading = true
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
                                                abModel.isJobLoading = false
                                                abModel.templateName = templateParam.name

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
                        defaultNumber = abModel.mode,
                        names = names,
                        values = values,
                    ) {
                        AptypeUtil.setPumpMode(it)
                        abModel.mode = it
                    }
                }
                //喷洒模式 -固定
                if (abModel.mode == VKAg.BUMP_MODE_FIXED) {
                    //流量大小
                    item {
                        SliderTitleCounter(
                            number = abModel.pumpOrValveSize.toFloat(),
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
                            abModel.pumpOrValveSize = it.toInt()
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
                            defaultNumber = abModel.sprayOrSeedMu
                        ) {
                            AptypeUtil.setSprayMu(it)
                            abModel.sprayOrSeedMu = it
                        }

                    }
                }
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
                        defaultNumber = abModel.width
                    ) {
                        AptypeUtil.setABWidth(it)
                        abModel.width = it
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
                        defaultNumber = abModel.speed
                    ) {
                        AptypeUtil.setABSpeed(it)
                        abModel.speed = it
                    }
                }
                //喷头转速
                item {
                    SliderTitleCounter(
                        number = abModel.rotationalSpeed.toFloat(),
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
                        abModel.rotationalSpeed = it.toInt()
                    }
                }
            }
        }
    }
}