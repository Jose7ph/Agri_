package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.AreaCleanModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.ext.millisToDate
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.text.AutoScrollingText

@Composable
fun WorkAreaCleanEdit(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val areaCleanModel = remember { navController.getViewModel(route, AreaCleanModel::class.java) }
    val locationModel = remember { navController.getViewModel(route, LocationModel::class.java) }
    val blockPoints2D by areaCleanModel.blockPoints2D.collectAsState()
    val imuData by DroneModel.imuData.observeAsState()
    LaunchedEffect(imuData) {
        imuData?.let {
            areaCleanModel.processImuData(imuData = it, isEdit = true)
        }
    }

    DisposableEffect(Unit) {
        //设置打点方式为飞机打点
        mapVideoModel.changeLocationType("drone")
        locationModel.setup()
        onDispose {
            //清理打点数据
            areaCleanModel.removeAllPoint()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalTrackBox(
            modifier = Modifier
                .padding(top = STATUS_BAR_HEIGHT + 2.dp, start = 2.dp)
                .width(160.dp)
                .height(160.dp)
                .background(Color.Black),
            boundary = blockPoints2D,
            isClosed = true,
            dronePosition = areaCleanModel.dronePoint2D
        )
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //打点
                RightButtonCommon(
                    text = stringResource(id = R.string.measure_dot),
                    enabled = areaCleanModel.points.size < 4
                ) {
                    if (areaCleanModel.routeYaw == null || areaCleanModel.points.isEmpty()) {
                        areaCleanModel.routeYaw = DroneModel.imuData.value?.yaw
                    }
                    locationModel.location.value?.let {
                        areaCleanModel.addPoint(
                            GeoHelper.LatLngAlt(
                                it.lat,
                                it.lng,
                                DroneModel.imuData.value?.height?.toDouble() ?: 0.0
                            )
                        )
                        areaCleanModel.calcBlockPoint()
                    }
                }
                val list = listOf(
                    stringResource(id = R.string.delete_last_point),
                    stringResource(id = R.string.delete_all_point)
                )
                RightButtonCommon(
                    enabled = areaCleanModel.points.isNotEmpty(),
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
                                        areaCleanModel.removeLastPoint()
                                    }

                                    1 -> {
                                        //删除全部点
                                        areaCleanModel.removeAllPoint()
                                    }
                                }
                                context.hideDialog()

                            },
                            onDismiss = {
                                context.hideDialog()
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                RightButtonCommon(
                    enabled = areaCleanModel.points.size == 4,
                    text = stringResource(id = R.string.save)
                ) {
                    context.showDialog {
                        InputPopup(
                            title = stringResource(id = R.string.save),
                            defaultText = System.currentTimeMillis()
                                .millisToDate("yyyyMMdd_HHmmss"),
                            onConfirm = { name ->
                                areaCleanModel.saveBlock(name) { ids ->
                                    if (ids.isNotEmpty()) {
                                        areaCleanModel.refresh(ids[0])
                                    }
                                    context.hideDialog()
                                    navController.popBackStack()
                                }
                            },
                            onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
            }
        )
    }
}