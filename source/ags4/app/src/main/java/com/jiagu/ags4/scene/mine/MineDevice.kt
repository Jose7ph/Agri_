package com.jiagu.ags4.scene.mine

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.components.LazyPageVerticalGrid
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper

@Composable
fun MineDevice(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val mineDeviceModel = LocalMineDeviceModel.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val refreshFlag = remember {
        mutableStateOf(false)
    }
    var activeStatus by remember { mutableIntStateOf(DroneModel.detail.value?.staticInfo?.activeStatus ?: 0) }
    val isService = AgsUser.userInfo?.isService() == true

    MainContent(
        title = stringResource(id = R.string.mine_device),
        breakAction = { if (!navController.popBackStack()) finish() },
        barAction = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeStatus == 1 && isService) {
                    TopBarBottom(
                        text = stringResource(id = R.string.activate_device),
                        buttonColor = Color.White,
                        textColor = Color.Black
                    ) {
                        mineDeviceModel.droneId = DroneModel.activeDrone?.droneCode ?: ""
                        context.showDialog {
                            PromptPopup(
                                onDismiss = { context.hideDialog() },
                                onConfirm = {
                                    mineDeviceModel.activateDevice(fail = {
                                        context.toast(R.string.fail)
                                        context.hideDialog()
                                    }, success = {
                                        context.toast(R.string.success)
                                        context.hideDialog()
                                        DroneModel.detail.value.let { detail ->
                                            detail?.staticInfo?.activeStatus = 2
                                        }
                                        activeStatus = 2
                                        mineDeviceModel.droneListRefresh { }
                                    })
                                },
                                content = stringResource(
                                    R.string.activate_device_confirm,
                                    mineDeviceModel.droneId ?: ""
                                ),
                            )
                        }
                    }


                }
                DroneModel.activeDrone?.let {
                    TopBarBottom(
                        text = stringResource(id = R.string.current_device),
                        buttonColor = Color.White,
                        textColor = Color.Black
                    ) {
                        mineDeviceModel.droneId = it.droneCode
                        //获取详情
                        mineDeviceModel.initDroneDetail()
                        navController.navigate("mine_device_detail")
                    }
                }
            }


        }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            DroneData(
                workArea = UnitHelper.transAreaWithUnit(
                    context, mineDeviceModel.droneWorkArea
                ),
                workTime = "${mineDeviceModel.droneWorkTime}${stringResource(id = R.string.hour)}",
                flyCount = mineDeviceModel.droneFlyCount.toString()
            )
            ShadowFrame(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NormalTextField(
                        text = mineDeviceModel.droneSearch,
                        onValueChange = {
                            mineDeviceModel.droneSearch = it
                        },
                        showClearIcon = false,
                        modifier = Modifier.weight(1f),
                        hint = stringResource(id = R.string.please_enter_drone_id),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        hintTextStyle = MaterialTheme.typography.bodySmall,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            mineDeviceModel.droneId = mineDeviceModel.droneSearch
                            refreshFlag.value = true
                            keyboardController?.hide()
                        }),
                        borderColor = Color.Transparent
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                mineDeviceModel.droneId = mineDeviceModel.droneSearch
                                refreshFlag.value = true
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

            }
            LazyPageVerticalGrid(
                modifier = Modifier,
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                columns = GridCells.Fixed(2),
                listDataTotal = mineDeviceModel.droneDeviceTotal,
                listData = mineDeviceModel.droneDeviceList,
                refreshFlag = refreshFlag,
                refreshData = { complete ->
                    mineDeviceModel.droneListRefresh {
                        complete(it)
                    }
                },
                loadMore = { pageIndex, complete ->
                    mineDeviceModel.droneListLoadMore(pageIndex) {
                        complete(it)
                    }
                },
                //这里不清除数据 为了方便跳转detail后返回依然保留当前位置和数据
                disposableFlag = false,
                clearData = {
                    mineDeviceModel.droneListClear()
                },
                item = { drone ->
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(8.dp))
                            .padding(start = 10.dp)
                            .clickable {
                                mineDeviceModel.droneId = drone.droneId
                                //获取详情
                                mineDeviceModel.initDroneDetail()
                                navController.navigate("mine_device_detail")
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center
                        ) {
                            if (drone.pictureUrl != "" && drone.pictureUrl != null) {
                                AsyncImage(
                                    model = drone.pictureUrl,
                                    contentDescription = "drone image",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clickable {
                                            context.showDialog {
                                                ClickImagePopup(
                                                    url = drone.pictureUrl, onDismiss = {
                                                        context.hideDialog()
                                                    })
                                            }
                                        })
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_flight_control),
                                    contentDescription = "drone",
                                    modifier = Modifier.size(30.dp),
                                    colorFilter = ColorFilter.tint(Color.Black)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.width(136.dp),
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            AutoScrollingText(
                                text = "${drone.droneId} ${drone.modelName ?: ""}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            AutoScrollingText(
                                text = "${drone.droneName ?: "--"} / ${drone.zzDroneNum ?: "--"}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        var backgroundColor = Color.Transparent
                        var status = ""
                        when (drone.status) {
                            FlyStatus.ONLINE.status -> {
                                status = stringResource(id = R.string.dev_online)
                                backgroundColor = Color.Red
                            }

                            FlyStatus.OFFLINE.status -> {
                                status = stringResource(id = R.string.dev_offline)
                                backgroundColor = MaterialTheme.colorScheme.tertiary
                            }

                            FlyStatus.LOCK.status -> {
                                status = stringResource(id = R.string.dev_lock)
                                backgroundColor = MaterialTheme.colorScheme.surfaceDim
                            }

                            FlyStatus.WORK.status -> {
                                status = stringResource(id = R.string.dev_work)
                                backgroundColor = MaterialTheme.colorScheme.primary
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(30.dp)
                                .background(
                                    color = backgroundColor, shape = MaterialTheme.shapes.small
                                ), contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = status,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(30.dp)
                        )
                    }
                }
            )
        }
    }
}

/**
 * 飞机数据
 */
@Composable
private fun DroneData(workArea: String, flyCount: String, workTime: String) {
    ShadowFrame(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            DataRow(
                image = painterResource(id = R.drawable.default_work_area),
                title = stringResource(id = R.string.mine_work_area),
                data = workArea,
                modifier = Modifier.weight(1f)
            )
            DataRow(
                image = painterResource(id = R.drawable.default_work_time),
                title = stringResource(id = R.string.mine_work_duration),
                data = workTime,
                modifier = Modifier.weight(1f)
            )
            DataRow(
                image = painterResource(id = R.drawable.default_flight_count),
                title = stringResource(id = R.string.mine_flight_frequency),
                data = flyCount,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DataRow(modifier: Modifier = Modifier, image: Painter, title: String, data: String) {
    val imageSize = 36.dp
    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = image,
            contentDescription = "mine flight frequency",
            modifier = Modifier.size(imageSize),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Column(
            modifier = Modifier,
        ) {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            AutoScrollingText(
                text = data,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }
    }
}

/**
 * 点击图片弹窗
 */
@Composable
fun ClickImagePopup(url: String, onDismiss: () -> Unit) {
    ScreenPopup(width = 280.dp, content = {
        Box(
            modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = url, contentDescription = "drone image", modifier = Modifier.fillMaxSize()
            )
        }
    }, showCancel = false, showConfirm = false, onDismiss = {
        onDismiss()
    })
}