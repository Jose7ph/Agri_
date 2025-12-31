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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jiagu.ags4.vm.AreaCleanModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.device.model.UploadNaviData
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkAreaCleanParam(route: String) {
    val navController = LocalNavController.current
    val mapVideoModel = LocalMapVideoModel.current
    val context = LocalContext.current
    val areaCleanModel = remember { navController.getViewModel(route, AreaCleanModel::class.java) }
    val workPoints2D by areaCleanModel.workPoints2D.collectAsState()
    val blockPoints2D by areaCleanModel.blockPoints2D.collectAsState()
    val config = Config(context)

    DisposableEffect(Unit) {
        //初始化速度参数赋值
        areaCleanModel.speed = config.cleanSpeed
        areaCleanModel.generateParam2DData()
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
                    DrawerContentAreaClean(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(LEFT_DRAWER_MAX_WIDTH)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        areaCleanModel = areaCleanModel,
                        flyingEdit = areaCleanModel.breakPoint == null
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
                breakPoint = areaCleanModel.breakPoint2D,
            )
        }
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                Spacer(modifier = Modifier.weight(1f))
                //开始作业
                RightButtonCommon(
                    text = stringResource(id = R.string.start)
                ) {
                    //上传航线
                    val task = areaCleanModel.uploadNavi()
                    task?.let {
                        context.startProgress(task) { success, msg ->
                            if (success) {
                                VoiceMessage.emit(context.getString(com.jiagu.v9sdk.R.string.voice_route_uploaded))
                                context.showDialog {
                                    CleanFlyConfirmPopup(
                                        width = areaCleanModel.width,
                                        repeatCount = areaCleanModel.repeatCount,
                                        speed = areaCleanModel.speed,
                                        confirm = {
                                            areaCleanModel.makePlan {//保存or更新规划
                                                areaCleanModel.blockWorking()//并将地块标记为进行中地块
                                                updateAreaCleanPlan(areaCleanModel)
                                            }
                                            //删除本地已存储的原来地块的断点
                                            areaCleanModel.deleteLocalBreakpoint()
                                            DroneModel.activeDrone?.takeOff2Clean()
                                            navController.navigate(WorkPageEnum.WORK_AREA_CLEAN_START.url)
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
 * @param areaCleanModel
 * @param flyingEdit true 编辑参数页面  false 起飞页面
 */
@Composable
fun DrawerContentAreaClean(
    modifier: Modifier = Modifier,
    areaCleanModel: AreaCleanModel,
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
                defaultNumber = areaCleanModel.width
            ) {
                areaCleanModel.width = it
                areaCleanModel.verticalAB()
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
                defaultNumber = areaCleanModel.repeatCount.toFloat()
            ) {
                areaCleanModel.repeatCount = it.toInt()
                areaCleanModel.verticalAB()
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
                defaultNumber = areaCleanModel.speed
            ) {
                areaCleanModel.speed = it
                config.cleanSpeed = it
                if (!flyingEdit) {
                    DroneModel.activeDrone?.setNaviProperties(
                        UploadNaviData(
                            0f,
                            it,
                            areaCleanModel.width,
                            AptypeUtil.getSprayMu(),
                            AptypeUtil.getPumpAndValve(), 0, 2
                        )
                    )
                }
            }
        }
    }
}

fun updateAreaCleanPlan(areaCleanModel: AreaCleanModel) {
    areaCleanModel.getBlockPlan { bp ->
        bp?.let {
            areaCleanModel.updateBlockPlan(it)//更新列表页的block的plan
            areaCleanModel.updateBlocksList(listOf(it))
//        areaCleanModel.refresh(areaCleanModel.selectedLocalBlockId)//更新后数据后，刷新整个列表
        }
    }
}