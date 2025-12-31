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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.LocalSortieManagementVM
import com.jiagu.ags4.vm.SortieManagementVM
import com.jiagu.ags4.vm.UserSortieCount
import com.jiagu.ags4.vm.UserSortieCountDetail
import com.jiagu.api.ext.millisToDate
import com.jiagu.api.ext.toString
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.label.ComboRollButtonLabel
import com.jiagu.jgcompose.label.DateRangeLabel
import com.jiagu.jgcompose.paging.LazyColumnPaging
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun SortieManagement(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val vm = LocalSortieManagementVM.current
    val activity = LocalContext.current as MineActivity

    MainContent(
        title = stringResource(id = R.string.mine_sortie_management),
        breakAction = { if (!navController.popBackStack()) finish() }) {
        val titleWidth = 60.dp
        val workTypeNames = listOf(
            stringResource(id = R.string.block_filter_all),
            stringResource(id = R.string.spray),
            stringResource(id = R.string.seed),
            stringResource(id = R.string.lifting)
        )
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(30.dp)
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //时间
            ShadowFrame(modifier = Modifier.weight(1f), shape = MaterialTheme.shapes.extraSmall) {
                DateRangeLabel(
                    modifier = Modifier.fillMaxSize(),
                    labelWidth = titleWidth,
                    labelName = stringResource(id = R.string.time),
                    defaultStartDate = vm.startTime,
                    defaultEndDate = vm.endTime,
                    onConfirm = { range, _, st, _, et ->
                        vm.startTime = st
                        vm.endTime = et
                        //更新数据
                        vm.refresh()
                    },
                    onCancel = {
                        //检索开始时间清空
                        vm.startTime = vm.defaultStartTime
                        //检索结束时间清空
                        vm.endTime = ""
                        //更新数据
                        vm.refresh()
                    }
                )
            }
            Spacer(modifier = Modifier.weight(0.2f))
            //类型
            ShadowFrame(modifier = Modifier.weight(0.6f), shape = MaterialTheme.shapes.extraSmall) {
                ComboRollButtonLabel(
                    modifier = Modifier.fillMaxSize(),
                    labelName = stringResource(id = R.string.device_engine_type),
                    labelWidth = titleWidth,
                    comboIndex = if (vm.sortieType.isEmpty()) 0 else vm.sortieType.toInt(),
                    comboItems = workTypeNames,
                    comboValue = workTypeNames[if (vm.sortieType.isEmpty()) 0 else vm.sortieType.toInt()],
                    onConfirm = { idx, _ ->
                        vm.sortieType = when (idx) {
                            0 -> ""
                            else -> idx.toString()
                        }
                        vm.refresh()
                    },
                    onCancel = {}
                )
            }
        }
        SortieDateInfo(vm = vm)
//        FlightManagement(toDetail = {
//            activity.toDetail(it)
//        })
    }
}

@Composable
fun SortieDateInfo(vm: SortieManagementVM) {
    val context = LocalContext.current
    val act = context as MineActivity
    //长按状态判断 true开启多选
    var longClickState by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumnPaging(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (longClickState) {
                        Modifier.height(250.dp)
                    } else {
                        Modifier
                    }
                ),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
            items = vm.sortieInfoPageList, item = {sortieDateInfo ->
                val date = sortieDateInfo.startDate.millisToDate("yyyy-MM-dd")
                SortieDateInfoDetails(
                    vm = vm,
                    sortieDateInfo = sortieDateInfo,
                    longClickState = longClickState,
                    selectedChildes = vm.selectedAllChildes[date] ?: setOf(),
                    onLongClick = {
                        longClickState = true
                    },
                    onSelectedChange = {
                        vm.selectedAllChildes[date] = it
//                        Log.d("zhy", "vm.selectedAllChildes: ${vm.selectedAllChildes.size}")
//                        Log.d(
//                            "zhy",
//                            "vm.selectedAllChildes[date]: ${vm.selectedAllChildes[date]?.size}"
//                        )
//                        Log.d(
//                            "zhy",
//                            "vm.all: ${vm.selectedAllChildes.values.flatten().size}"
//                        )
                    }
                )
            }, onRefresh = {
                vm.refresh()
            })
        //多选
        if (longClickState) {
            val bottomWidth = 80.dp
            val bottomHeight = 30.dp
            val sortieIds = vm.selectedAllChildes.values.flatten()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(color = Color.Gray)
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
                        //取消选择
                        Box(
                            modifier = Modifier
                                .height(bottomHeight)
                                .width(bottomWidth)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable {
                                    vm.selectedAllChildes.clear()
                                },

                            contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.cancel) + stringResource(id = R.string.selected_data),
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
                        AutoScrollingText(text = stringResource(id = R.string.selected_data) + sortieIds.size)
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
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable(sortieIds.isNotEmpty()) {
                                    act.toDetailList(
                                        sortieIds = sortieIds,
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
                            .clickable {
                                longClickState = false
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "close")
                    }
                }

            }
        }
    }
}

@Composable
fun SortieDateInfoDetails(
    vm: SortieManagementVM,
    sortieDateInfo: UserSortieCount,
    selectedChildes: Set<Long>,
    longClickState: Boolean,
    onLongClick: () -> Unit,
    onSelectedChange: (Set<Long>) -> Unit,
) {
    val context = LocalContext.current
    val date = sortieDateInfo.startDate.millisToDate("yyyy-MM-dd")
//    Log.d("zhy", "sortieDateInfo.startDate: ${sortieDateInfo.startDate},date:${date}")
    val details = remember {
        mutableStateListOf<UserSortieCountDetail>().apply {
            addAll(vm.sortieDateInfoDetailsMap[date] ?: listOf())
        }
    }
    var isShowDetails by rememberSaveable {
        mutableStateOf(false)
    }
    var parentState by rememberSaveable {
        mutableStateOf(ToggleableState.Off)
    }
    LaunchedEffect(selectedChildes.size) {
        parentState = when {
            selectedChildes.isEmpty() -> ToggleableState.Off
            selectedChildes.size == details.size -> ToggleableState.On
            else -> ToggleableState.Indeterminate
        }
    }
//    Log.d("zhy", "details: ${details.size}")
//    Log.d("zhy", "isShowDetails: ${isShowDetails}")
//    Log.d("zhy", "parentState: ${parentState}")
//    Log.d("zhy", "selectedChildes: ${selectedChildes.size}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ShadowFrame {
            Row(
                modifier = Modifier
                    .padding(start = 5.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                    .fillMaxWidth()
                    .height(30.dp)
                    .then(
                        if (longClickState) {
                            Modifier.noEffectClickable {
                                if (details.isEmpty()) {
                                    vm.toggleShowDetails(date) {
                                        details.addAll(it)
                                    }
                                }
                                isShowDetails = !isShowDetails
                            }
                        } else {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    if (details.isEmpty()) {
                                        vm.toggleShowDetails(date) {
                                            details.addAll(it)
                                        }
                                    }
                                    isShowDetails = !isShowDetails
                                }, onLongPress = {
                                    onLongClick()
                                })
                            }
                        }
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                if (longClickState) {
                    TriStateCheckbox(
                        state = parentState, onClick = {
                            //判断child数据是否已经查到了 没查到先去查询
                            //childList是空 && 架次数量 > 0，查询数据
                            if (details.isEmpty() && sortieDateInfo.flightCount > 0) {
                                vm.toggleShowDetails(date) { dataList ->
                                    details.addAll(dataList)
                                    //根据当前parent状态调整
                                    when (parentState) {
                                        //未选择、部分选择 -> 全选
                                        ToggleableState.Off, ToggleableState.Indeterminate -> {
                                            val ids = details.map { it.id }
                                            onSelectedChange(ids.toSet())
                                            parentState = ToggleableState.On
                                        }
                                        //全选 -> 未选择
                                        ToggleableState.On -> {
                                            onSelectedChange(setOf())
                                            parentState = ToggleableState.Off
                                        }
                                    }
//                                    Log.d("zhy", "parentState 4: ${parentState}")
                                }
                            } else {
                                //根据当前parent状态调整
                                when (parentState) {
                                    //未选择、部分选择 -> 全选
                                    ToggleableState.Off, ToggleableState.Indeterminate -> {
                                        val ids = details.map { it.id }
                                        onSelectedChange(ids.toSet())
                                        parentState = ToggleableState.On
                                    }
                                    //全选 -> 未选择
                                    ToggleableState.On -> {
                                        onSelectedChange(setOf())
                                        parentState = ToggleableState.Off
                                    }
                                }
//                                Log.d("zhy", "parentState 2: ${parentState}")
                            }

                        }, modifier = Modifier.fillMaxHeight()
                    )
                }
                Image(
                    painter = painterResource(R.drawable.default_position),
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
                VerticalDivider(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .height(24.dp) // 设置竖线的高度
                        .background(Color.Gray), // 设置竖线的颜色
                )
                Text(
                    text = date,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(R.drawable.default_up),
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                        .padding(start = 10.dp),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
                Text(
                    text = sortieDateInfo.flightCount.toString(),
                    color = Color(0xFF808080),
                    modifier = Modifier
                        .width(40.dp)
                        .padding(start = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(R.drawable.default_spray),
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .padding(start = 10.dp)
                )
                Text(
                    text = sortieDateInfo.sprayQuantity.toString(1),
                    color = Color(0xFF808080),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(start = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    painter = painterResource(R.drawable.default_seed),
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .padding(start = 10.dp)
                )
                Text(
                    text = sortieDateInfo.seedingQuantity.toString(1),
                    color = Color(0xFF808080),
                    modifier = Modifier
                        .width(50.dp)
                        .padding(start = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = UnitHelper.transAreaWithUnit(context, sortieDateInfo.sprayArea),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                        .rotate(if (isShowDetails) 90f else 0f)
                )
            }
        }
        if (isShowDetails) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (detail in details) {
                    ShadowFrame {
                        SortieDetailsInfo(
                            sortieDetails = detail,
                            longClickState = longClickState,
                            isSelected = selectedChildes.contains(detail.id),
                            onLongClick = onLongClick,
                            onSelected = {
                                val curSelectedChildes = selectedChildes.toMutableSet()
                                if (it) {
                                    curSelectedChildes.add(detail.id)
                                } else {
                                    curSelectedChildes.remove(detail.id)
                                }
                                onSelectedChange(curSelectedChildes.toSet())
                                parentState = when {
                                    curSelectedChildes.isEmpty() -> ToggleableState.Off
                                    curSelectedChildes.size == details.size -> ToggleableState.On
                                    else -> ToggleableState.Indeterminate
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortieDetailsInfo(
    sortieDetails: UserSortieCountDetail,
    longClickState: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onSelected: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val act = context as MineActivity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .then(
                if (longClickState) {
                    Modifier.noEffectClickable {
                        onSelected(!isSelected)
                    }
                } else {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            act.toDetail(sortieDetails)
                        }, onLongPress = {
                            onLongClick()
                        })
                    }
                }
            ), verticalAlignment = Alignment.CenterVertically
    ) {
        if (longClickState) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = {
                    onSelected(it)
                })
        }
        Text(
            text = sortieDetails.userName,
            modifier = Modifier
                .weight(0.5f)
                .padding(horizontal = 10.dp),
            style = MaterialTheme.typography.bodyMedium
        )

        Column(Modifier.weight(1f)) {
            Text(
                text = sortieDetails.startTime.millisToDate("HH:mm:ss") + " - " + sortieDetails.endTime.millisToDate(
                    "HH:mm:ss"
                ), style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = sortieDetails.regionName ?: stringResource(R.string.na),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Column(Modifier.weight(2f)) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.default_spray),
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
                Text(
                    text = sortieDetails.sprayQuantity.toString(1),
                    color = Color(0xFF808080),
                    modifier = Modifier
                        .width(100.dp)
                        .padding(horizontal = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = UnitHelper.transAreaWithUnit(context, sortieDetails.sprayArea),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Image(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Position",
                    modifier = Modifier
                        .width(20.dp)
                        .height(20.dp)
                )
            }
        }
    }
}