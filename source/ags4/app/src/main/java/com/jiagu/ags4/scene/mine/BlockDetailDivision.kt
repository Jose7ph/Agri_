package com.jiagu.ags4.scene.mine

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.work.LocationTypeEnum
import com.jiagu.ags4.scene.work.MarkerMovePanel
import com.jiagu.ags4.scene.work.RightBox
import com.jiagu.ags4.scene.work.RightButtonCommon
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.api.helper.GeoHelper
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup

@Composable
fun BlockDetailDivision() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as BlockDetailActivity
    val blockDivisionModel = activity.blockDivisionModel
    val locationModel = activity.locationModel

    DisposableEffect(Unit) {
        activity.changeLocationType(LocationTypeEnum.MAP.type)
        locationModel.setup()
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp)
                .size(36.dp)
                .clip(shape = CircleShape)
                .align(Alignment.TopStart)
                .clickable {
                    navController.popBackStack()
                }, color = Color.White
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                modifier = Modifier
                    .size(36.dp)
                    .padding(vertical = 6.dp),
                contentDescription = null,
                tint = Color.Black
            )
        }
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
                    text = if(blockDivisionModel.divisionPoints.size > 1) stringResource(R.string.slice_next_edge) else stringResource(R.string.slice_dot),
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
