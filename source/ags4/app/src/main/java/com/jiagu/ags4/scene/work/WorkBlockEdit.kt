package com.jiagu.ags4.scene.work

import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.openFileManager
import com.jiagu.ags4.vm.BlockEditModel
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.ext.millisToDate
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.jgcompose.bluetooth.BluetoothList
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun WorkBlockEdit(blockRoute: String, blockEditRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val config = Config(context)
    val blockModel = remember { navController.getViewModel(blockRoute, BlockModel::class.java) }
    val locationModel =
        remember { navController.getViewModel(blockEditRoute, LocationModel::class.java) }
    val blockEditModel =
        remember { navController.getViewModel(blockEditRoute, BlockEditModel::class.java) }
    val btDeviceModel =
        remember { navController.getViewModel(blockEditRoute, BtDeviceModel::class.java) }

    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val locConnect by locationModel.locConnect.observeAsState()
    val location by locationModel.location.collectAsState()

    val dotTypes = LocationTypeEnum.entries.map {
        stringResource(it.typeName)
    }
    val pointTypes = PointTypeEnum.entries.map {
        stringResource(it.typeName)
    }
    LaunchedEffect(locConnect) {
        if (locConnect == true) {
            //停止继续检索蓝牙
            btDeviceModel.stopScan()
            //隐藏蓝牙列表
            blockEditModel.showBluetoothList = false
            btDeviceModel.list.value = null
            btDeviceModel.searching.value = null
            config.locator = ""
        }
    }
    DisposableEffect(Unit) {
        //如果刚进入页面，则弹出蓝牙列表(不管是否已连接都显示)
        if (mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            blockEditModel.showBluetoothList = true
            btDeviceModel.startScan(activity)
            activity.startLocatorJob(locationModel)
        }
        locationModel.setup()
        //selectedBP 不为null说明是编辑
        blockModel.selectedBP?.let {
            blockEditModel.initEditBlockPlan(it)

        }
        onDispose { }
    }
    //点击地图时，判断当前障碍点数量
    LaunchedEffect(blockEditModel.showObstacleErrorDialog) {
        if (blockEditModel.showObstacleErrorDialog) {
            context.showDialog {
                ObstaclePointsErrorPopup(
                    isSave = false,
                    onConfirm = {
                        blockEditModel.clearObstaclePoint()
                        blockEditModel.clearMarker()
                        context.hideDialog()
                    },
                    onDismiss = {
                        //仅改变显示状态，不删除原障碍点
                        blockEditModel.showObstacleErrorDialog = false
                        context.hideDialog()
                    }
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //打点器信息
        if (mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            LocatorInfoBox(
                modifier = Modifier
                    .padding(start = 4.dp, top = STATUS_BAR_HEIGHT + 4.dp)
                    .align(Alignment.TopStart),
                location = location
            )
        }
        //中间置顶信息
        TopCenterMessage(
            modifier = Modifier
                .padding(top = STATUS_BAR_HEIGHT + 4.dp)
                .align(alignment = Alignment.TopCenter),
            blockEditModel = blockEditModel
        )
        //地图打点角标
        if (mapVideoModel.locationType == LocationTypeEnum.MAP.type) {
            Image(
                painter = painterResource(id = R.drawable.map_locator),
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
                    .align(Alignment.Center),
                contentDescription = null
            )
        }
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //KML导出
                RightButtonCommon(
                    text = "KML " + stringResource(R.string.import_kml),
                ) {
                    //判断是否有打点数据
                    if (blockEditModel.edgePoints.isNotEmpty()) {
                        context.showDialog {
                            PromptPopup(
                                content = stringResource(R.string.kml_import_overwrite_tip),
                                onConfirm = {
                                    loadKmlFile(
                                        blockEditModel = blockEditModel,
                                        activity = activity,
                                        context = context,
                                    )
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        }
                    } else {
                        loadKmlFile(
                            blockEditModel = blockEditModel,
                            activity = activity,
                            context = context,
                        )
                    }
                }
                //打点方式 飞机/地图/遥控器/打点器
                RightButtonCommon(
                    text = stringResource(LocationTypeEnum.getTypeNameByType(mapVideoModel.locationType)),
                ) {
                    context.showDialog {
                        //检查障碍物 >= 3
                        if (blockEditModel.checkObstaclePoints()) {
                            ListSelectionPopup(
                                defaultIndexes = listOf(
                                    LocationTypeEnum.getIndexByType(
                                        mapVideoModel.locationType
                                    )
                                ),
                                list = dotTypes,
                                item = {
                                    AutoScrollingText(text = it, modifier = Modifier.fillMaxWidth())
                                },
                                onConfirm = { indexes, _ ->
                                    val index = indexes[0]
                                    val type = LocationTypeEnum.getTypeByIndex(index)
                                    mapVideoModel.changeLocationType(type)
                                    locationModel.setup()
                                    //不是打点器则结束打点器job
                                    if (type != LocationTypeEnum.LOCATOR.type) {
                                        activity.stopLocatorJob()
                                    } else {//是打点器则启动打点器job 并且显示打点器列表(不管是否已连接打点器)
                                        blockEditModel.showBluetoothList = true
                                        btDeviceModel.startScan(activity)
                                        activity.startLocatorJob(locationModel)
                                    }
                                    context.hideDialog()

                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        } else {//障碍物有问题
                            ObstaclePointsErrorPopup(
                                isSave = false,
                                onConfirm = {
                                    blockEditModel.clearObstaclePoint()
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        }
                    }
                }
                //打点类型 边界/障碍/圆形障碍
                RightButtonCommon(
                    text = stringResource(blockEditModel.pointType.typeName),
                ) {
                    context.showDialog {
                        //检查障碍物 >= 3
                        if (blockEditModel.checkObstaclePoints()) {
                            ListSelectionPopup(
                                defaultIndexes = listOf(blockEditModel.pointType.ordinal),
                                list = pointTypes,
                                item = {
                                    AutoScrollingText(text = it, modifier = Modifier.fillMaxWidth())
                                },
                                onConfirm = { indexes, _ ->
                                    val index = indexes[0]
                                    val pointType = PointTypeEnum.getPointTypeByIndex(index)
                                    blockEditModel.changePointType(pointType)
                                    context.hideDialog()

                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        } else {//障碍物有问题
                            ObstaclePointsErrorPopup(
                                isSave = false,
                                onConfirm = {
                                    blockEditModel.clearObstaclePoint()
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                }
                            )
                        }
                    }
                }
                //打点
                RightButtonCommon(
                    text = stringResource(R.string.measure_dot),
                ) {
                    locationModel.location.value?.let {
                        blockEditModel.addPoint(GeoHelper.LatLngAlt(it.lat, it.lng, it.alt))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                //保存
                RightButtonCommon(
                    text = stringResource(R.string.save),
                    enabled = blockEditModel.edgePoints.size >= 3 && blockEditModel.errMessage == null,
                ) {
                    //检查障碍物 >= 3
                    if (blockEditModel.checkObstaclePoints()) {
                        if (blockEditModel.editBlock == null) { //edit = null 是新增
                            context.showDialog {
                                InputPopup(
                                    title = stringResource(id = R.string.save),
                                    defaultText = System.currentTimeMillis()
                                        .millisToDate("yyyyMMdd_HHmmss"),
                                    onConfirm = { name ->
                                        blockEditModel.saveBlock(name) { ids ->
                                            context.toast(context.getString(R.string.save_success))
                                            if (ids.isNotEmpty()) {
                                                blockModel.refresh(ids[0])
                                            }
                                            context.hideDialog()
                                            navController.popBackStack()
                                        }
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    })
                            }
                        } else { //更新地块 不需要输入名称
                            blockEditModel.saveBlock(blockEditModel.editBlock!!.blockName) { ids ->
                                context.toast(context.getString(R.string.save_success))
                                if (ids.isNotEmpty()) {
                                    blockModel.refresh(ids[0])
                                }
                                context.hideDialog()
                                navController.popBackStack()
                            }
                        }
                    } else {//障碍物有问题
                        context.showDialog {
                            ObstaclePointsErrorPopup(
                                isSave = true,
                                onConfirm = {
                                    blockEditModel.clearObstaclePoint()
                                    context.hideDialog()
                                },
                                onDismiss = { context.hideDialog() }
                            )
                        }
                    }

                }
            })
        if (blockEditModel.showBluetoothList && mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            BluetoothList(
                modifier = Modifier
                    .padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp)
                    .height(240.dp)
                    .width(200.dp)
                    .background(color = Color.White, shape = MaterialTheme.shapes.medium),
                buttonName = stringResource(id = R.string.search_locator),
                bluetoothList = bluetoothList,
                searching = searching == true,
                onItemClick = { address ->
                    btDeviceModel.stopScan()
                    config.locator = "skydroid*$address"
                    locationModel.setup()
                },
                onSearchClick = {
                    btDeviceModel.startScan(activity)
                })
        }
        //marker point panel
        if (blockEditModel.showMarkerPanel) {
            MarkerMovePanel(
                modifier = Modifier.padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp),
                moveDist = blockEditModel.moveDist,
                title = when (blockEditModel.pointType) {
                    PointTypeEnum.EDGE -> stringResource(R.string.measure_move_boundary_title)
                    PointTypeEnum.OBSTACLE -> stringResource(R.string.measure_move_obstacle_title)
                    PointTypeEnum.CIRCLE_OBSTACLE -> stringResource(R.string.measure_move_obstacle_circle_title)
                },
                showRadius = blockEditModel.pointType == PointTypeEnum.CIRCLE_OBSTACLE,
                showMarkerChange = true,
                markerIndex = blockEditModel.selectedMarkerIndex + 1,//显示从1开始
                radius = blockEditModel.markerRadius.toFloat(),
                onTopClick = {
                    blockEditModel.moveNorth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onBottomClick = {
                    blockEditModel.moveSouth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onLeftClick = {
                    blockEditModel.moveWest(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onRightClick = {
                    blockEditModel.moveEast(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onTopLeftClick = {
                    blockEditModel.moveWestNorth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onTopRightClick = {
                    blockEditModel.moveEastNorth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onBottomLeftClick = {
                    blockEditModel.moveWestSouth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onBottomRightClick = {
                    blockEditModel.moveEastSouth(activity.canvas.angle)?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onPreviousPoint = {
                    blockEditModel.changeMarkerPoint()
                },
                onNextPoint = {
                    blockEditModel.changeMarkerPoint(true)
                },
                onClickReset = {
                    blockEditModel.resetMarkerDist()?.let {
                        blockEditModel.updatePoint(it)
                    }
                },
                onRadiusChange = {
                    blockEditModel.updateCircleObstaclePointRadius(it)
                },
                onClickDelete = {
                    blockEditModel.removePointByIndex()
                },
            )
        }
    }
}

@Composable
private fun TopCenterMessage(modifier: Modifier = Modifier, blockEditModel: BlockEditModel) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        //面积
        Box(
            modifier = Modifier
                .background(
                    color = BlackAlpha,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = "${UnitHelper.transArea(blockEditModel.area)} ${
                    UnitHelper.areaUnit(
                        context
                    )
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
        //error msg
        blockEditModel.getErrMessage()?.let {
            Box(
                modifier = Modifier
                    .background(Color.Red, shape = MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ObstaclePointsErrorPopup(
    isSave: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PromptPopup(
        content = if (isSave) stringResource(R.string.measure_error_obstacle_count_save) else stringResource(
            R.string.measure_error_obstacle_count
        ),
        onConfirm = {
            onConfirm()
        },
        onDismiss = {
            onDismiss()
        }
    )
}

private fun loadKmlFile(
    activity: MapVideoActivity,
    blockEditModel: BlockEditModel,
    context: Context,
) {
    activity.openFileManager {
        //获取当前作业机对应的菜单
        blockEditModel.loadKml(it) { success, blockType ->
            //加载kml成功
            if (success) {
                if (blockType == Block.TYPE_BLOCK) {
                    //先清空当前全部点数据
                    blockEditModel.clearAllPoints()
                    //再加载kml数据
                    blockEditModel.importKmlBlock()
                } else {//kml类型不对则清空当前kml数据
                    context.toast(context.getString(R.string.kml_import_type_error))
                    blockEditModel.clearKml()
                }
            } else {
                context.toast(context.getString(R.string.fail))
            }
        }
    }
}