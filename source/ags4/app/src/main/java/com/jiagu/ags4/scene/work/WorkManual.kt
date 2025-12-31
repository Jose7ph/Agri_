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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.seedTitle
import com.jiagu.ags4.utils.sprayTitle
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.ManualModel
import com.jiagu.ags4.vm.task.CommandsTask
import com.jiagu.api.ext.toast
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkManual(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val isCleanType = VKAgCmd.DRONE_TYPE_WASHING.toInt() == AptypeUtil.getDroneType()
    val manualModel = remember { navController.getViewModel(route, ManualModel::class.java) }
    val aptypeData by DroneModel.aptypeData.observeAsState()
    DisposableEffect(Unit) {
        aptypeData?.let {
            manualModel.setAptypeData(it)
        }
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RightBox(modifier = Modifier.align(Alignment.TopEnd), buttons = {
            Spacer(modifier = Modifier.weight(1f))
            //返航
            RightButtonCommon(
                text = stringResource(id = R.string.protect_returning),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR,
            ) {
                DroneModel.activeDrone?.apply {
                    context.showDialog {
                        ScreenPopup(content = {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(id = R.string.work_ask_gohome),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }, onDismiss = { context.hideDialog() }, onConfirm = {
                            goHome()
                            context.hideDialog()
                        })
                    }
                }
            }
            //降落
            RightButtonCommon(
                text = stringResource(id = R.string.touch_down),
                enabled = mapVideoModel.airFlag == VKAg.AIR_FLAG_ON_AIR,
            ) {
                DroneModel.activeDrone?.apply {
                    context.showDialog {
                        ScreenPopup(content = {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(id = R.string.work_ask_land),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }, onDismiss = { context.hideDialog() }, onConfirm = {
                            land()
                            context.hideDialog()
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            RightButtonCommon(text = stringResource(R.string.location_detection)) {
                navController.navigate(WorkPageEnum.WORK_LOCATOR.url)
            }
            //急停仅 REMOTE_ID模式才显示 or Debug
            EmergencyStopButton()
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
                        manualModel = manualModel
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
private fun DrawerContent(modifier: Modifier = Modifier, manualModel: ManualModel) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        if (manualModel.isJobLoading) {
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
                        templateName = manualModel.templateName.ifEmpty { context.getString(R.string.unused_template) },
                        onDeleteTemplate = { id, complete ->
                            manualModel.removeTemplate(id) { success ->
                                if (success) {
                                    complete()
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onSave = { name ->
                            manualModel.saveTemplateData(name = name) { success ->
                                if (success) {
                                    context.toast(context.getString(R.string.success))
                                } else {
                                    context.toast(context.getString(R.string.err_server))
                                }
                            }
                        },
                        onClick = { complete ->
                            manualModel.getTemplateParamList { success, data ->
                                if (!success) {
                                    context.toast(context.getString(R.string.fail))
                                    return@getTemplateParamList
                                }

                                data?.let { complete(it) } ?: return@getTemplateParamList
                            }
                        },
                        onConfirm = { templateParam ->
                            manualModel.setTemplate(templateParam) { success, commands ->
                                if (success) {
                                    if (!commands.isNullOrEmpty()) {
                                        DroneModel.activeDrone?.let { activeDrone ->
                                            manualModel.isJobLoading = true
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
                                                manualModel.isJobLoading = false
                                                manualModel.templateName = templateParam.name
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
                        defaultNumber = manualModel.mode,
                        names = names,
                        values = values,
                    ) {
                        AptypeUtil.setPumpMode(it)
                        manualModel.mode = it
                    }
                }
                //喷洒模式 -固定
                if (manualModel.mode == VKAg.BUMP_MODE_FIXED) {
                    //流量大小
                    item {
                        SliderTitleCounter(
                            number = manualModel.pumpOrValveSize.toFloat(),
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
                            manualModel.pumpOrValveSize = it.toInt()
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
                            defaultNumber = manualModel.sprayOrSeedMu
                        ) {
                            AptypeUtil.setSprayMu(it)
                            manualModel.sprayOrSeedMu = it
                        }

                    }
                }
                //喷头转速
                item {
                    SliderTitleCounter(
                        number = manualModel.rotationalSpeed.toFloat(),
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
                        manualModel.rotationalSpeed = it.toInt()
                    }
                }
            }
        }
    }
}