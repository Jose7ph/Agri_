package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.SortieItem
import com.jiagu.ags4.ui.components.ComboBox
import com.jiagu.ags4.ui.theme.DarkAlpha
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.buttonDisabled
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.CacheModel
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.picker.DateRangePicker
import com.jiagu.jgcompose.picker.RegionPicker
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper

@Composable
fun MineDeviceDetail(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val mineDeviceModel = LocalMineDeviceModel.current
    val context = LocalContext.current
    var positionValue by remember {
        mutableStateOf("")
    }
    var longClickState by remember {
        mutableStateOf(false)
    }

    var showDetails by remember {
        mutableStateOf(false)
    }

    MainContent(title = stringResource(id = R.string.flight_records), barAction = {
        Button(
            modifier = Modifier
                .width(60.dp)
                .height(26.dp), onClick = {
                showDetails = !showDetails
            }, contentPadding = PaddingValues(0.dp), shape = MaterialTheme.shapes.small
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.details),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }, breakAction = { if (!navController.popBackStack()) finish() }) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            FlightData(
                workArea = UnitHelper.transAreaWithUnit(
                    context, mineDeviceModel.droneDetailWorkArea
                ),
                workTime = "${mineDeviceModel.droneDetailWorkTime}${stringResource(id = R.string.hour)}",
                flyCount = mineDeviceModel.droneDetailFlyCount.toString(),
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //时间
                ShadowFrame(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight()
                                .fillMaxHeight()
                                .background(
                                    color = Color.White, shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    (context as MineActivity).let {
                                        it.showDialog {
                                            DateRangePicker(
                                                defaultStartDate = mineDeviceModel.droneDetailStartDate,
                                                defaultEndDate = mineDeviceModel.droneDetailEndDate,
                                                onConfirm = { range, _, st, _, et ->
                                                    mineDeviceModel.droneDetailStartDate = st
                                                    mineDeviceModel.droneDetailEndDate = et
                                                    mineDeviceModel.droneDetailDateRange = range
                                                    //更新数据
                                                    mineDeviceModel.droneDetailRefresh()
                                                    //关闭窗口
                                                    it.hideDialog()
                                                },
                                                onCancel = {
                                                    //检索开始时间清空
                                                    mineDeviceModel.droneDetailStartDate = ""
                                                    //检索结束时间清空
                                                    mineDeviceModel.droneDetailEndDate = ""
                                                    //时间文本框清空
                                                    mineDeviceModel.droneDetailDateRange = ""
                                                    //更新数据
                                                    mineDeviceModel.droneDetailRefresh()
                                                    //关闭窗口
                                                    it.hideDialog()
                                                },
                                            )
                                        }
                                    }
                                }, contentAlignment = Alignment.CenterStart
                        ) {
                            AutoScrollingText(
                                text = mineDeviceModel.droneDetailDateRange.ifEmpty {
                                    stringResource(
                                        id = R.string.time
                                    )
                                },
                                color = if (mineDeviceModel.droneDetailDateRange.isNotEmpty()) Color.Black else Color.Gray,
                                textAlign = TextAlign.Start,
                                style = if (mineDeviceModel.droneDetailDateRange.isNotEmpty()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp),
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }

                }
                //位置
                ShadowFrame(modifier = Modifier.weight(0.6f)) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .wrapContentHeight()
                                .fillMaxHeight()
                                .background(
                                    color = Color.White, shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    context.showDialog {
                                        RegionPicker(
                                            regions = CacheModel.convertAddressList(),
                                            onConfirm = {
                                                positionValue = it.name
                                                mineDeviceModel.droneDetailRegion = it
                                                mineDeviceModel.droneDetailRefresh()
                                                context.hideDialog()
                                            },
                                            onDismiss = {
                                                positionValue = ""
                                                mineDeviceModel.droneDetailRegion = null
                                                mineDeviceModel.droneDetailRefresh()
                                                context.hideDialog()
                                            },
                                        )
                                    }
                                }, contentAlignment = Alignment.CenterStart

                        ) {
                            AutoScrollingText(
                                text = positionValue.ifEmpty { stringResource(id = R.string.position) },
                                color = if (positionValue.isNotEmpty()) Color.Black else Color.Gray,
                                textAlign = TextAlign.Start,
                                style = if (positionValue.isNotEmpty()) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 10.dp)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
                //团队
                ShadowFrame(modifier = Modifier.weight(0.4f)) {
                    ComboBox(
                        items = mineDeviceModel.teamList.map { it.groupName },
                        selectedValue = mineDeviceModel.selGroupName.ifEmpty { stringResource(id = R.string.all_team) },
                        onIndexChange = {
                            //判断值修改则查询
                            if (mineDeviceModel.selGroupId == mineDeviceModel.teamList[it].groupId) {
                                return@ComboBox
                            }
                            //如果是it = 0说明选择全部 则id = null
                            if (it == 0) {
                                mineDeviceModel.selGroupId = null
                            } else {
                                mineDeviceModel.selGroupId = mineDeviceModel.teamList[it].groupId
                            }
                            mineDeviceModel.selGroupName = mineDeviceModel.teamList[it].groupName
                            mineDeviceModel.droneDetailRefresh()
                        },
                        width = 100.dp,
                        fontStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.small)
                            .background(color = MaterialTheme.colorScheme.onPrimary),
                    )
                }
                //个人
                ShadowFrame(modifier = Modifier.weight(0.4f)) {
                    ComboBox(
                        items = mineDeviceModel.personList.map { it.username },
                        selectedValue = mineDeviceModel.selUserName.ifEmpty { stringResource(id = R.string.all_person) },
                        onIndexChange = {
                            //判断值修改则查询
                            if (mineDeviceModel.selUserId == mineDeviceModel.personList[it].userId) {
                                return@ComboBox
                            }
                            //如果是it = 0说明选择全部 则id = null
                            if (it == 0) {
                                mineDeviceModel.selUserId = null
                            } else {
                                mineDeviceModel.selUserId = mineDeviceModel.personList[it].userId
                            }
                            mineDeviceModel.selUserName = mineDeviceModel.personList[it].username
                            mineDeviceModel.droneDetailRefresh()
                        },
                        width = 100.dp,
                        fontStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.small)
                            .background(color = MaterialTheme.colorScheme.onPrimary),
                    )
                }
            }
            LazyGridPaging(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                items = mineDeviceModel.droneDetailPageList,
                item = {droneDetail ->
                    Row(
                        modifier = Modifier
                            .height(60.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(8.dp)),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(onPress = {
                                        //点击按下
//                                            Log.d("zhy", "onPress..... ")
                                    }, onTap = {
                                        //长按出发后不可以点击跳转页面
//                                            Log.d("zhy", "longClickState:${longClickState} ")
                                        if (!longClickState) {
                                            //单点跳转详情页面
                                            (context as MineActivity).toDetail(
                                                droneId = droneDetail.droneId,
                                                sortieId = droneDetail.sortieId,
                                                startTime = droneDetail.createTime,
                                            )
                                        } else {
                                            checkSelectedProcess(
                                                mineDeviceModel, droneDetail, true
                                            )
                                        }
                                    }, onLongPress = {
                                        longClickState = true
                                    }, onDoubleTap = {})
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (longClickState) {
                                Checkbox(
                                    checked = mineDeviceModel.droneDetailSelectedList.any { selected -> selected == droneDetail },
                                    onCheckedChange = {
                                        checkSelectedProcess(
                                            mineDeviceModel, droneDetail, false
                                        )
                                    },
                                    modifier = Modifier.fillMaxHeight()
                                )

                            }
                            Column(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(vertical = 6.dp),
                                verticalArrangement = Arrangement.SpaceAround
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    //日期
                                    Box(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AutoScrollingText(
                                            text = droneDetail.createTime.millisToDate(
                                                "yyyy/MM/dd HH:mm"
                                            ),
                                            style = MaterialTheme.typography.labelLarge,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                    //名称
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AutoScrollingText(
                                            text = droneDetail.operUserName,
                                            style = MaterialTheme.typography.labelLarge,
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                    //自动/手动
                                    val backgroundColor: Color
                                    val status: String
                                    if (droneDetail.isAuto) {
                                        status = stringResource(id = R.string.auto)
                                        backgroundColor = MaterialTheme.colorScheme.primary
                                    } else {
                                        status = stringResource(id = R.string.manual)
                                        backgroundColor = Color.Red
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(20.dp)
                                            .background(
                                                color = backgroundColor,
                                                shape = MaterialTheme.shapes.small,
                                            ), contentAlignment = Alignment.Center
                                    ) {
                                        AutoScrollingText(
                                            text = status,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AutoScrollingText(
                                            text = droneDetail.regionName,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.width(60.dp)
                                    ) {
                                        AutoScrollingText(
                                            text = UnitHelper.transAreaWithUnit(
                                                context, droneDetail.sprayRange
                                            ),
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
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
                },
                onRefresh = {
                    mineDeviceModel.droneDetailRefresh()
                }
            )
        }
    }
    //多选
    if (longClickState) {
        val bottomWidth = 80.dp
        val bottomHeight = 30.dp
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.White)
                    .padding(horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .align(Alignment.Top),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //全选
                    Box(
                        modifier = Modifier
                            .height(bottomHeight)
                            .width(bottomWidth)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable() {
                                //都没有选择 则直接全选
                                if (mineDeviceModel.droneDetailSelectedList.size == 0) {
                                    mineDeviceModel.droneDetailSelectedList.addAll(mineDeviceModel.droneDetailList)
                                } else {
                                    //部分选择 则判断重复
                                    for ((_, detail) in mineDeviceModel.droneDetailList.withIndex()) {
                                        if (!mineDeviceModel.droneDetailSelectedList.contains(detail)) {
                                            mineDeviceModel.droneDetailSelectedList.add(detail)
                                        }
                                    }
                                }
                            }, contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.select_all),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    //取消全选
                    Box(
                        modifier = Modifier
                            .height(bottomHeight)
                            .width(bottomWidth)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable() {
                                mineDeviceModel.droneDetailSelectedList.clear()
                            },

                        contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.unselect_all),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                //已选
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .align(Alignment.CenterVertically), contentAlignment = Alignment.Center
                ) {
                    AutoScrollingText(text = stringResource(id = R.string.selected_data) + mineDeviceModel.droneDetailSelectedList.size)
                }
                //在地图上查看
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(bottomHeight)
                            .background(
                                color = if (mineDeviceModel.droneDetailSelectedList.size > 0) MaterialTheme.colorScheme.primary else buttonDisabled,
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable(mineDeviceModel.droneDetailSelectedList.size > 0) {
                                (context as MineActivity).toDetailList(
                                    sortieIds = mineDeviceModel.droneDetailSelectedList.map { it.id },
                                )
                            }, contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.view_on_the_map),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                //关闭
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.Bottom)
                        .clickable() {
                            longClickState = false
                        }, contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                }
            }

        }
    }

    //详情
    if (showDetails) {
        DroneDetailsInfo(onHide = { showDetails = false })
    }
}

/**
 * 飞行数据
 */
@Composable
private fun FlightData(
    modifier: Modifier = Modifier, workArea: String, flyCount: String, workTime: String,
) {
    ShadowFrame(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .height(60.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small
                )
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

/**
 * 数据行
 */
@Composable
private fun DataRow(modifier: Modifier = Modifier, image: Painter, title: String, data: String) {
    val imageSize = 36.dp
    val rowDataSpace = 16.dp
    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(rowDataSpace)
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
 * 详情
 */
@Composable
private fun DroneDetailsInfo(onHide: () -> Unit) {
    val mineDeviceModel = LocalMineDeviceModel.current
    val context = LocalContext.current
    val droneDetail = mineDeviceModel.droneDetail?.staticInfo
    val auth = 1 == droneDetail?.auth
    val isMaker = AgsUser.userInfo?.isMaker() ?: false
    val rowHeight = 20.dp
    val titleWidth = 100.dp
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = SIMPLE_BAR_HEIGHT)
            .clickable(enabled = false) {}) {
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable() {
                    onHide()
                })
        Box(
            modifier = Modifier
                .weight(0.8f)
                .fillMaxHeight()
                .background(DarkAlpha)
        ) {
            LazyColumn(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        //图片
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            if (droneDetail?.pictureUrl != "" && droneDetail?.pictureUrl != null) {
                                AsyncImage(
                                    model = droneDetail.pictureUrl,
                                    contentDescription = "drone image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            (context as MineActivity).let {
                                                it.showDialog {
                                                    ClickImagePopup(
                                                        url = droneDetail.pictureUrl,
                                                        onDismiss = {
                                                            it.hideDialog()
                                                        })
                                                }
                                            }
                                        })
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.default_flight_control),
                                    contentDescription = "drone",
                                    modifier = Modifier.fillMaxSize(),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }
                    }
                }
                //名称
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_name),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = droneDetail?.droneName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        if (auth && !isMaker) {
//                        if (true) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .clickable() {
                                        (context as MineActivity).let {
                                            it.showDialog {
                                                DroneDetailsNamePopup(
                                                    isMark = false,
                                                    oldName = droneDetail?.droneName ?: "",
                                                    onCancel = {
                                                        it.hideDialog()
                                                    },
                                                    onConfirm = { newName ->
                                                        mineDeviceModel.updateDroneDetailsName(
                                                            isMaker = false,
                                                            droneId = droneDetail!!.droneId,
                                                            name = newName
                                                        )
                                                        it.hideDialog()
                                                    }

                                                )
                                            }
                                        }
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "name edit",
                                    modifier = Modifier.fillMaxSize(0.7f),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                //机型
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_type),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = droneDetail?.modelName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                //飞控ID
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_plan_id),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = droneDetail?.droneId ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                //飞机状态
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_status),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        val droneStatus = when (droneDetail?.status) {
                            FlyStatus.ONLINE.status -> context.getString(R.string.dev_online)
                            FlyStatus.OFFLINE.status -> context.getString(R.string.dev_offline)
                            FlyStatus.WORK.status -> context.getString(R.string.dev_work)
                            FlyStatus.LOCK.status -> context.getString(R.string.dev_lock)
                            else -> ""
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(rowHeight)
                        ) {
                            AutoScrollingText(
                                text = droneStatus,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                //锁定状态
//                if (true) {
                if (auth) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(titleWidth)) {
                                AutoScrollingText(
                                    text = stringResource(id = R.string.dev_detail_base_lock_status),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            val ownerLock =
                                (if (isMaker) droneDetail?.zzIsLock else droneDetail?.yyIsLock) ?: 0
                            val lockStatus = when (ownerLock) {
                                1 -> context.getString(R.string.dev_action_unlock)
                                2 -> context.getString(R.string.dev_action_lock)
                                else -> context.getString(R.string.dev_action_applock)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AutoScrollingText(
                                    text = lockStatus,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .clickable() {
                                        (context as MineActivity).let {
                                            it.showDialog {
                                                var selected by remember {
                                                    mutableStateOf(ownerLock)
                                                }
                                                ScreenPopup(width = 200.dp, content = {
                                                    val locks =
                                                        stringArrayResource(id = R.array.lock_name)
                                                    Column(
                                                        modifier = Modifier.padding(
                                                            vertical = 10.dp, horizontal = 20.dp
                                                        ),
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            8.dp
                                                        )
                                                    ) {
                                                        for ((i, lockName) in locks.withIndex()) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(
                                                                    30.dp
                                                                ),
                                                                modifier = Modifier.clickable() {
                                                                    selected = i + 1
                                                                }) {
                                                                RadioButton(
                                                                    selected = selected == i + 1,
                                                                    onClick = {
                                                                        selected = i + 1
                                                                    },
                                                                    modifier = Modifier.size(30.dp)
                                                                )
                                                                AutoScrollingText(
                                                                    text = lockName,
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    textAlign = TextAlign.Start
                                                                )
                                                            }
                                                        }
                                                    }
                                                }, onDismiss = {
                                                    it.hideDialog()
                                                }, onConfirm = {
                                                    mineDeviceModel.lockDrone(
                                                        droneId = droneDetail!!.droneId,
                                                        opr = selected
                                                    )
                                                    it.hideDialog()
                                                })
                                            }
                                        }
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "lock edit",
                                    modifier = Modifier.fillMaxSize(0.7f),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                //制造企业
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_company),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(rowHeight)
                        ) {
                            AutoScrollingText(
                                text = droneDetail?.zzAccountName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                //制造商标识
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_zzname),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = droneDetail?.zzDroneNum ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        if (auth && isMaker) {
//                        if (true) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .clickable() {
                                        (context as MineActivity).let {
                                            it.showDialog {
                                                DroneDetailsNamePopup(
                                                    isMark = true,
                                                    oldName = droneDetail?.zzDroneNum ?: "",
                                                    onCancel = {
                                                        it.hideDialog()
                                                    },
                                                    onConfirm = { newName ->
                                                        mineDeviceModel.updateDroneDetailsName(
                                                            isMaker = true,
                                                            droneId = droneDetail!!.droneId,
                                                            name = newName
                                                        )
                                                        it.hideDialog()
                                                    }

                                                )
                                            }
                                        }
                                    }, contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "zzname edit",
                                    modifier = Modifier.fillMaxSize(0.7f),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                //激活时间
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(titleWidth)) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.dev_detail_base_active_date),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = droneDetail?.activeTime ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                //转让 TODO
//                if (auth && !isMaker) {
//                if (true) {
//                    item {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Button(
//                                onClick = {
//                                    (context as MineActivity).let {
//                                        it.showDialog {
//                                        }
//                                    }
//
//                                },
//                                shape = MaterialTheme.shapes.small,
//                                contentPadding = PaddingValues(0.dp),
//                                modifier = Modifier
//                                    .width(120.dp)
//                                    .height(40.dp)
//                            ) {
//                                Text(
//                                    text = stringResource(id = R.string.transfer_device),
//                                    color = Color.White,
//                                    style = MaterialTheme.typography.titleSmall
//                                )
//                            }
//                        }
//                    }
//                }
            }
        }
    }
}

/**
 * 设备名称/制造商标识更新弹窗
 */
@Composable
fun DroneDetailsNamePopup(
    isMark: Boolean, oldName: String, onConfirm: (String) -> Unit, onCancel: () -> Unit,
) {
    val deviceNameMaxLength = 10
    var name by remember {
        mutableStateOf(oldName)
    }
    ScreenPopup(width = 300.dp, content = {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val title =
                if (isMark) stringResource(id = R.string.dev_detail_base_zzname) else stringResource(
                    id = R.string.dev_detail_base_name
                )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = title + stringResource(id = R.string.update))
            }

            NormalTextField(
                text = name, modifier = Modifier
                    .width(180.dp)
                    .height(30.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.small
                    ), onValueChange = {
                    //输入长度超过10截取前10位
                    name = if (!isMark && it.length > deviceNameMaxLength) {
                        it.substring(0..deviceNameMaxLength)
                    } else {
                        it
                    }
                }, borderColor = Color.LightGray
            )

        }
    }, onConfirm = {
        onConfirm(name)
    }, onDismiss = {
        onCancel()
    })
}

/**
 * 校验多选
 */
fun checkSelectedProcess(
    mineDeviceModel: MineDeviceModel, droneDetail: SortieItem, batchFlag: Boolean,
) {
    //判断重复
    val isRepeat = mineDeviceModel.droneDetailSelectedList.any { selected ->
        selected == droneDetail
    }
    //批量则只添加没有的
    if (!batchFlag) {
        if (!isRepeat) {
            mineDeviceModel.droneDetailSelectedList.add(droneDetail)
        } else {
            mineDeviceModel.droneDetailSelectedList.remove(droneDetail)
        }
    }
    //不是批量，删除重复的
    else {
        if (!isRepeat) {
            mineDeviceModel.droneDetailSelectedList.add(droneDetail)
        }
    }
}