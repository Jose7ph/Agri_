package com.jiagu.ags4.scene.work

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.ui.components.TranslationIcon
import com.jiagu.ags4.ui.components.TurnAroundIcon
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.seedTitle
import com.jiagu.ags4.utils.sprayTitle
import com.jiagu.ags4.utils.startProgress
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.EnhancedManualModel
import com.jiagu.ags4.vm.task.CommandsTask
import com.jiagu.api.ext.toast
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkEnhancedManual(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val isCleanType = VKAgCmd.DRONE_TYPE_WASHING.toInt() == AptypeUtil.getDroneType()
    val enhancedManualModel =
        remember { navController.getViewModel(route, EnhancedManualModel::class.java) }
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            enhancedManualModel.checkFlyModel(it)
        }
    }
    DisposableEffect(Unit) {
        aptypeData?.let {
            enhancedManualModel.setAptypeData(it)
        }
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RightBox(modifier = Modifier.align(Alignment.TopEnd), buttons = {
            Spacer(modifier = Modifier.weight(1f))
            // 开始M+ GPS模式下
            RightButtonCommon(
                text = "开始M+",
                enabled = VKAgTool.isGpsMode(enhancedManualModel.flyMode),
                leftImage = {
//                    TranslationIcon(modifier = Modifier)
                }, onClick = {
                    DroneModel.activeDrone?.setManualEnhancement(4)
                    imuData?.let { enhancedManualModel.setAPoint(it) }
                })
            //向左横移
            RightButtonCommon(
                text = "",
                enabled = VKAgTool.isEnhanceMode(enhancedManualModel.flyMode) && enhancedManualModel.routeChangeFlag,
                leftImage = {
                    TranslationIcon(modifier = Modifier)
                }, onClick = {
                    DroneModel.activeDrone?.setManualEnhancement(2)
                    imuData?.let { enhancedManualModel.setBPoint(it) }
                })
            //向右横移
            RightButtonCommon(
                text = "",
                enabled = VKAgTool.isEnhanceMode(enhancedManualModel.flyMode) && enhancedManualModel.routeChangeFlag,
                leftImage = {
                    TranslationIcon(modifier = Modifier, isLeft = false)
                }, onClick = {
                    DroneModel.activeDrone?.setManualEnhancement(1)
                    imuData?.let { enhancedManualModel.setBPoint(it) }
                })
            //掉头
            RightButtonCommon(
                text = "",
                enabled = VKAgTool.isEnhanceMode(enhancedManualModel.flyMode) && enhancedManualModel.routeChangeFlag,
                leftImage = {
                    TurnAroundIcon()
                }, onClick = {
                    DroneModel.activeDrone?.setManualEnhancement(3)
                })
            Spacer(modifier = Modifier.weight(1f))
        })
        //作业参数 && 当前机型清洗机 不显示
        if (!isCleanType) {
            Drawer(
                modifier = Modifier.padding(top = STATUS_BAR_HEIGHT),
                color = BlackAlpha,
                context = {
                    DrawerContent(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(LEFT_DRAWER_MAX_WIDTH)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        enhancedManualModel = enhancedManualModel
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
        }
    }
}

@Composable
private fun DrawerContent(modifier: Modifier = Modifier, enhancedManualModel: EnhancedManualModel) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        if (enhancedManualModel.isJobLoading) {
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
                        templateName = enhancedManualModel.templateName.ifEmpty {
                            context.getString(
                                R.string.unused_template
                            )
                        },
                        onDeleteTemplate = { id, complete ->
                            enhancedManualModel.removeTemplate(id) { success ->
                                if (success) {
                                    complete()
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onSave = { name ->
                            enhancedManualModel.saveTemplateData(name = name) { success ->
                                if (success) {
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onClick = { complete ->
                            enhancedManualModel.getTemplateParamList { success, data ->
                                if (!success) {
                                    context.toast(context.getString(R.string.fail))
                                    return@getTemplateParamList
                                }

                                data?.let { complete(it) } ?: return@getTemplateParamList
                            }
                        },
                        onConfirm = { templateParam ->
                            enhancedManualModel.setTemplate(templateParam) { success, commands ->
                                if (success) {
                                    if (!commands.isNullOrEmpty()) {
                                        DroneModel.activeDrone?.let { activeDrone ->
                                            enhancedManualModel.isJobLoading = true
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
                                                enhancedManualModel.isJobLoading = false
                                                enhancedManualModel.templateName =
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
                //喷洒模式
                item {
                    val names = if (isSeedWorkType()) listOf(
                        stringResource(R.string.manual),
                        stringResource(R.string.linkage)
                    ) else stringArrayResource(id = R.array.job_parameter_spray_mode).toList()
                    val values = mutableListOf<Int>()
                    values.add(VKAg.BUMP_MODE_FIXED)
                    values.add(VKAg.BUMP_MODE_SPEED)
                    ParameterDrawerGroupButtonRow(
                        title = if (isSeedWorkType()) stringResource(R.string.job_seed_mode) else stringResource(
                            R.string.job_spray_mode
                        ),
                        defaultNumber = enhancedManualModel.mode,
                        names = names,
                        values = values,
                    ) {
                        AptypeUtil.setPumpMode(it)
                        enhancedManualModel.mode = it
                    }
                }
                //喷洒模式 -固定
                if (enhancedManualModel.mode == VKAg.BUMP_MODE_FIXED) {
                    //流量大小
                    item {
                        SliderTitleCounter(
                            number = enhancedManualModel.pumpOrValveSize.toFloat(),
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
                            enhancedManualModel.pumpOrValveSize = it.toInt()
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
                            defaultNumber = enhancedManualModel.sprayOrSeedMu
                        ) {
                            AptypeUtil.setSprayMu(it)
                            enhancedManualModel.sprayOrSeedMu = it
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
                        defaultNumber = enhancedManualModel.width
                    ) {
                        AptypeUtil.setABWidth(it)
                        enhancedManualModel.width = it
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
                        defaultNumber = enhancedManualModel.speed
                    ) {
                        AptypeUtil.setABSpeed(it)
                        enhancedManualModel.speed = it
                    }
                }
                //喷头转速
                item {
                    SliderTitleCounter(
                        number = enhancedManualModel.rotationalSpeed.toFloat(),
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
                        enhancedManualModel.rotationalSpeed = it.toInt()
                    }
                }
            }
        }
    }
}