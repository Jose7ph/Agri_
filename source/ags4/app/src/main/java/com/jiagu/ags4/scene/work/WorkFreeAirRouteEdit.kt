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
import com.jiagu.ags4.vm.FreeAirRouteEditModel
import com.jiagu.ags4.vm.FreeAirRouteModel
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
fun WorkFreeAirRouteEdit(freeAirRoute: String, freeAirEditRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val config = Config(context)
    val freeAirRoute =
        remember { navController.getViewModel(freeAirRoute, FreeAirRouteModel::class.java) }
    val locationModel =
        remember { navController.getViewModel(freeAirEditRoute, LocationModel::class.java) }
    val freeAirRouteEditModel =
        remember { navController.getViewModel(freeAirEditRoute, FreeAirRouteEditModel::class.java) }
    val btDeviceModel =
        remember { navController.getViewModel(freeAirEditRoute, BtDeviceModel::class.java) }

    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val locConnect by locationModel.locConnect.observeAsState()
    val location by locationModel.location.collectAsState()

    val dotTypes = LocationTypeEnum.entries.map {
        stringResource(it.typeName)
    }
    LaunchedEffect(locConnect) {
        if (locConnect == true) {
            //停止继续检索蓝牙
            btDeviceModel.stopScan()
            //隐藏蓝牙列表
            freeAirRouteEditModel.showBluetoothList = false
            btDeviceModel.list.value = null
            btDeviceModel.searching.value = null
            config.locator = ""
        }
    }
    DisposableEffect(Unit) {
        //如果刚进入页面，则弹出蓝牙列表(不管是否已连接都显示)
        if (mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            freeAirRouteEditModel.showBluetoothList = true
            btDeviceModel.startScan(activity)
            activity.startLocatorJob(locationModel)
        }
        locationModel.setup()
        //selectedBP 不为null说明是编辑
        freeAirRoute.selectedBP?.let {
            freeAirRouteEditModel.initEditBlockPlan(it)

        }
        onDispose { }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //打点器信息
        if (mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            LocatorInfoBox(
                modifier = Modifier
                    .padding(start = 4.dp, top = STATUS_BAR_HEIGHT + 4.dp)
                    .align(Alignment.TopStart), location = location
            )
        }
        //中间置顶信息
        TopCenterMessage(
            modifier = Modifier
                .padding(top = STATUS_BAR_HEIGHT + 4.dp)
                .align(alignment = Alignment.TopCenter),
            freeAirRouteEditModel = freeAirRouteEditModel
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
                    if (freeAirRouteEditModel.points.isNotEmpty()) {
                        context.showDialog {
                            PromptPopup(
                                content = stringResource(R.string.kml_import_overwrite_tip),
                                onConfirm = {
                                    loadKmlFile(
                                        freeAirRouteEditModel = freeAirRouteEditModel,
                                        activity = activity,
                                        context = context,
                                    )
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                })
                        }
                    } else {
                        loadKmlFile(
                            freeAirRouteEditModel = freeAirRouteEditModel,
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
                        ListSelectionPopup(
                            defaultIndexes = listOf(
                                LocationTypeEnum.getIndexByType(
                                    mapVideoModel.locationType
                                )
                            ), list = dotTypes, item = {
                                AutoScrollingText(text = it, modifier = Modifier.fillMaxWidth())
                            }, onConfirm = { indexes, _ ->
                                val index = indexes[0]
                                val type = LocationTypeEnum.getTypeByIndex(index)
                                mapVideoModel.changeLocationType(type)
                                locationModel.setup()
                                //不是打点器则结束打点器job
                                if (type != LocationTypeEnum.LOCATOR.type) {
                                    activity.stopLocatorJob()
                                } else {//是打点器则启动打点器job 并且显示打点器列表(不管是否已连接打点器)
                                    freeAirRouteEditModel.showBluetoothList = true
                                    btDeviceModel.startScan(activity)
                                    activity.startLocatorJob(locationModel)
                                }
                                context.hideDialog()

                            }, onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
                //打点
                RightButtonCommon(
                    text = stringResource(R.string.measure_dot),
                ) {
                    locationModel.location.value?.let {
                        freeAirRouteEditModel.addTrack(GeoHelper.LatLngAlt(it.lat, it.lng, it.alt))
                    }
                }
                val list = listOf(
                    stringResource(id = R.string.delete_last_point),
                    stringResource(id = R.string.delete_all_point)
                )
                RightButtonCommon(
                    enabled = freeAirRouteEditModel.points.isNotEmpty(),
                    text = stringResource(id = R.string.clear)
                ) {
                    context.showDialog {
                        ListSelectionPopup(
                            title = stringResource(id = R.string.delete_point),
                            defaultIndexes = listOf(0),
                            list = list,
                            item = {
                                AutoScrollingText(text = it, modifier = Modifier.fillMaxWidth())
                            },
                            onConfirm = { indexes, _ ->
                                val selected = indexes[0]
                                when (selected) {
                                    0 -> {
                                        //删除最后一个点
                                        freeAirRouteEditModel.clearTrack()
                                    }

                                    1 -> {
                                        //删除全部点
                                        freeAirRouteEditModel.clearTrack(true)
                                    }
                                }
                                context.hideDialog()

                            },
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                //保存
                RightButtonCommon(
                    text = stringResource(R.string.save),
                    enabled = freeAirRouteEditModel.points.size >= 2,
                ) {
                    if (freeAirRouteEditModel.editBlock == null) { //edit = null 是新增
                        context.showDialog {
                            InputPopup(
                                title = stringResource(id = R.string.save),
                                defaultText = System.currentTimeMillis()
                                    .millisToDate("yyyyMMdd_HHmmss"),
                                onConfirm = { name ->
                                    freeAirRouteEditModel.saveBlock(name) { ids ->
                                        context.toast(context.getString(R.string.save_success))
                                        if (ids.isNotEmpty()) {
                                            freeAirRoute.refresh(ids[0])
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
                        freeAirRouteEditModel.saveBlock(freeAirRouteEditModel.editBlock!!.blockName) { ids ->
                            context.toast(context.getString(R.string.save_success))
                            if (ids.isNotEmpty()) {
                                freeAirRoute.refresh(ids[0])
                            }
                            context.hideDialog()
                            navController.popBackStack()
                        }
                    }
                }
            })
        if (freeAirRouteEditModel.showBluetoothList && mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
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
        if (freeAirRouteEditModel.showMarkerPanel) {
            MarkerMovePanel(
                modifier = Modifier.padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp),
                moveDist = freeAirRouteEditModel.moveDist,
                title = stringResource(R.string.measure_move_boundary_title),
                showRadius = false,
                showMarkerChange = freeAirRouteEditModel.points.size > 1,
                markerIndex = freeAirRouteEditModel.selectedMarkerIndex + 1,//显示从1开始
                onTopClick = {
                    freeAirRouteEditModel.moveNorth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onBottomClick = {
                    freeAirRouteEditModel.moveSouth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onLeftClick = {
                    freeAirRouteEditModel.moveWest(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onRightClick = {
                    freeAirRouteEditModel.moveEast(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onTopLeftClick = {
                    freeAirRouteEditModel.moveWestNorth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onTopRightClick = {
                    freeAirRouteEditModel.moveEastNorth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onBottomLeftClick = {
                    freeAirRouteEditModel.moveWestSouth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onBottomRightClick = {
                    freeAirRouteEditModel.moveEastSouth(activity.canvas.angle)?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onPreviousPoint = {
                    freeAirRouteEditModel.changeMarkerPoint()
                },
                onNextPoint = {
                    freeAirRouteEditModel.changeMarkerPoint(true)
                },
                onClickReset = {
                    freeAirRouteEditModel.resetMarkerDist()?.let {
                        freeAirRouteEditModel.updatePoint(it)
                    }
                },
                onClickDelete = {
                    freeAirRouteEditModel.removePointByIndex()
                },
            )
        }
    }
}

@Composable
private fun TopCenterMessage(
    modifier: Modifier = Modifier,
    freeAirRouteEditModel: FreeAirRouteEditModel,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        //距离
        Box(
            modifier = Modifier
                .background(
                    color = BlackAlpha, shape = MaterialTheme.shapes.extraSmall
                )
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = "${UnitHelper.transLength(freeAirRouteEditModel.length)} ${
                    UnitHelper.lengthUnit()
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

private fun loadKmlFile(
    activity: MapVideoActivity,
    freeAirRouteEditModel: FreeAirRouteEditModel,
    context: Context,
) {
    activity.openFileManager {
        //获取当前作业机对应的菜单
        freeAirRouteEditModel.loadKml(it) { success, blockType ->
            //加载kml成功
            if (success) {
                if (blockType == Block.TYPE_TRACK) {
                    //先清空当前全部点数据
                    freeAirRouteEditModel.clearTrack(true)
                    //再加载kml数据
                    freeAirRouteEditModel.importKmlTrack()
                } else {//kml类型不对则清空当前kml数据
                    context.toast(context.getString(R.string.kml_import_type_error))
                    freeAirRouteEditModel.clearKml()
                }
            } else {
                context.toast(context.getString(R.string.fail))
            }
        }
    }
}