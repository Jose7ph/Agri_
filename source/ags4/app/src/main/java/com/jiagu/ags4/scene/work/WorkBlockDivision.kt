package com.jiagu.ags4.scene.work

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.BlockDivisionModel
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup

@Composable
fun WorkBlockDivision(blockRoute: String, blockDivisionRoute: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val blockModel = remember { navController.getViewModel(blockRoute, BlockModel::class.java) }
    val blockDivisionModel = remember {
        navController.getViewModel(blockDivisionRoute, BlockDivisionModel::class.java)
    }
    val locationModel =
        remember { navController.getViewModel(blockDivisionRoute, LocationModel::class.java) }

    DisposableEffect(Unit) {
        mapVideoModel.changeLocationType("map")
        locationModel.setup()
        //初始化block plan
        blockModel.selectedBP?.let {
            blockDivisionModel.initEditBlockPlan(it)
        }
        onDispose { }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.map_locator),
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .align(Alignment.Center),
            contentDescription = null
        )
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //确定(切割地块)
                RightButtonCommon(
                    text = if (blockDivisionModel.divisionPoints.size > 1) stringResource(R.string.slice_next_edge) else stringResource(
                        R.string.slice_dot
                    ),
                ) {
                    locationModel.location.value?.let {
                        blockDivisionModel.addDivisionPoint(
                            GeoHelper.LatLngAlt(
                                it.lat,
                                it.lng,
                                0.0
                            )
                        )
                    }
                }
                //清除
                RightButtonCommon(
                    text = stringResource(R.string.clear)
                ) {
                    blockDivisionModel.clearDivisionBlocks()
                }
                Spacer(modifier = Modifier.weight(1f))
                RightButtonCommon(
                    text = stringResource(R.string.save_as),
                    enabled = blockDivisionModel.divisionBlocks.size > 1
                ) {
                    context.showDialog {
                        PromptPopup(
                            title = stringResource(R.string.block_save_title),
                            content = stringResource(R.string.save_confirm),
                            onConfirm = {
                                blockDivisionModel.saveAs {
                                    blockModel.refresh()
                                    navController.popBackStack()
                                }
                                context.hideDialog()
                            },
                            onDismiss = {
                                context.hideDialog()
                            }
                        )
                    }
                }
            })
        //marker point panel
        if (blockDivisionModel.showMarkerPanel) {
            MarkerMovePanel(
                modifier = Modifier.padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp),
                moveDist = blockDivisionModel.moveDist,
                title = stringResource(R.string.slice_edit_point),
                markerIndex = blockDivisionModel.selectedMarkerIndex + 1,//显示从1开始
                onTopClick = {
                    blockDivisionModel.moveNorth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onBottomClick = {
                    blockDivisionModel.moveSouth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onLeftClick = {
                    blockDivisionModel.moveWest(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onRightClick = {
                    blockDivisionModel.moveEast(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onTopLeftClick = {
                    blockDivisionModel.moveWestNorth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onTopRightClick = {
                    blockDivisionModel.moveEastNorth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onBottomLeftClick = {
                    blockDivisionModel.moveWestSouth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onBottomRightClick = {
                    blockDivisionModel.moveEastSouth(activity.canvas.angle)?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onClickReset = {
                    blockDivisionModel.resetMarkerDist()?.let {
                        blockDivisionModel.updatePoint(it)
                    }
                },
                onClickDelete = {
                    blockDivisionModel.removePointByIndex()
                },
            )
        }
    }
}


