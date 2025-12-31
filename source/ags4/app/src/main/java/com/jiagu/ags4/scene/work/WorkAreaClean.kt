package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.ui.components.BlockCard
import com.jiagu.ags4.ui.components.SortButton
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.VeryDarkAlpha
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.AreaCleanModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.work.SyncStatusEnum
import com.jiagu.jgcompose.drawer.Drawer
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.icon.BackToTopIcon
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import kotlinx.coroutines.launch

@Composable
fun WorkAreaClean(route: String) {
    val navController = LocalNavController.current
    val mapVideoModel = LocalMapVideoModel.current
    val context = LocalContext.current
    val areaCleanModel = remember { navController.getViewModel(route, AreaCleanModel::class.java) }
    val imuData by DroneModel.imuData.observeAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val curIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(imuData) {
        imuData?.let {
            areaCleanModel.processImuData(it)
        }
    }
    DisposableEffect(Unit) {
        if (imuData == null) {
            //初始化数据默认给手机位置，如果后续imu有数据则会覆盖
            areaCleanModel.dronePosition = mapVideoModel.phonePosition
        } else {
            areaCleanModel.processImuData(imuData!!)
        }
        //查询当前地块断点
        areaCleanModel.getBreakpoint()
        onDispose {

        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        //列表
        Drawer(
            modifier = Modifier.padding(top = STATUS_BAR_HEIGHT),
            color = BlackAlpha,
            context = {
                DrawerContentBlockCardList(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(LEFT_DRAWER_MAX_WIDTH)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    areaCleanModel = areaCleanModel,
                    listState = listState
                )
                if (curIndex != 0) {
                    BackToTopIcon(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp)
                            .size(30.dp)
                            .background(color = VeryDarkAlpha, shape = CircleShape)
                            .noEffectClickable {
                                scope.launch {
                                    listState.scrollToItem(0)
                                }
                            },
                        iconColor = Color.White
                    )
                }
            },
            isShow = mapVideoModel.showParam,
            onShow = {
                mapVideoModel.showParam = true
                mapVideoModel.hideInfoPanel()
            },
            onClose = {
                mapVideoModel.showParam = false
            })
        RightBox(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End,
            buttons = {
                //新增
                RightButtonCommon(
                    text = stringResource(id = R.string.add_block),
                ) {
                    areaCleanModel.routeYaw = null
                    areaCleanModel.clearBP()
                    navController.navigate(WorkPageEnum.WORK_AREA_CLEAN_EDIT.url)
                }
                //参数
                RightButtonCommon(
                    text = stringResource(id = R.string.work_parameter),
                    enabled = areaCleanModel.selectedBP != null
                ) {
                    //有断点 && 作业中状态
                    if (areaCleanModel.breakPoint != null && areaCleanModel.selectedBP?.working == true) {
                        context.showDialog {
                            PromptPopup(
                                content = stringResource(R.string.breakpoint_detected_continue),
                                onConfirm = {
                                    //vm有断点有限用vm的，vm没有用规划的
                                    areaCleanModel.loadSelectBpParam()
                                    navController.navigate(WorkPageEnum.WORK_AREA_CLEAN_PARAM.url)
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    areaCleanModel.clearBreakpoint()
                                    areaCleanModel.loadSelectBpParam()
                                    navController.navigate(WorkPageEnum.WORK_AREA_CLEAN_PARAM.url)
                                    context.hideDialog()
                                })
                        }
                    } else {
                        areaCleanModel.clearBreakpoint()
                        areaCleanModel.loadSelectBpParam()
                        navController.navigate(WorkPageEnum.WORK_AREA_CLEAN_PARAM.url)
                    }
                    //处理2d图
                    areaCleanModel.verticalAB()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContentBlockCardList(
    modifier: Modifier = Modifier,
    areaCleanModel: AreaCleanModel,
    listState: LazyListState,
) {
    val context = LocalContext.current
    val state = rememberPullToRefreshState()
    val localBlocksList by areaCleanModel.localBlocksListFlow.collectAsState()
    DisposableEffect(Unit) {
        if (areaCleanModel.pageDataInit) {
            areaCleanModel.refresh()
            areaCleanModel.pageDataInit = false
        }
        onDispose {

        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            //排序
            SortButton(
                title = stringResource(id = R.string.time),
                currentType = areaCleanModel.timeSort,
                textColor = MaterialTheme.colorScheme.onPrimary,
                imageSize = 16.dp,
                modifier = Modifier.weight(1f)
            ) {
                areaCleanModel.timeSort = it
                areaCleanModel.refresh()
            }
            if (areaCleanModel.syncStatus != SyncStatusEnum.NONE) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (areaCleanModel.syncStatus == SyncStatusEnum.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(12.dp),
                            color = Color.White,
                            strokeWidth = 1.dp
                        )
                    }
                    AutoScrollingText(
                        text = when (areaCleanModel.syncStatus) {
                            SyncStatusEnum.SYNCING -> stringResource(R.string.syncing)
                            SyncStatusEnum.SUCCESS -> stringResource(R.string.sync_success)
                            SyncStatusEnum.FAILED -> stringResource(R.string.sync_fail)
                            else -> ""
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

        }
        //检索条件
        BlockCardListCondition(
            text = areaCleanModel.search,
            type = areaCleanModel.blockState,
            onTextChanged = {
                areaCleanModel.search = it
            },
            onTypeChanged = {
                areaCleanModel.blockState = it
            },
            onSearch = {
                areaCleanModel.refresh()
            }
        )
        PullToRefreshBox(
            modifier = Modifier,
            state = state,
            isRefreshing = areaCleanModel.isLoading,
            onRefresh = { areaCleanModel.refresh() },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                state = listState
            ) {
                items(localBlocksList.size) {
                    val block = localBlocksList[it]
                    var isSelected = areaCleanModel.selectedLocalBlockId == block.localBlockId
                    var blockName by remember {
                        mutableStateOf(block.blockName)
                    }
                    BlockCard(
                        modifier = Modifier
                            .clickable {
                                areaCleanModel.clearBP()
                                if (!isSelected) {
                                    areaCleanModel.setBP(block)
                                }
                            },
                        block = block,
                        blockName = blockName,
                        isSelect = isSelected,
                        showArea = false,
                        showDivide = false,
                        onBlockDelete = {
                            context.showDialog {
                                PromptPopup(
                                    title = stringResource(id = R.string.text_dialog_delete_block),
                                    content = stringResource(id = R.string.text_dialog_delete_block_text),
                                    onConfirm = {
                                        areaCleanModel.clearBP()
                                        areaCleanModel.deleteLocalBlock(
                                            block.localBlockId
                                        ) {
                                            areaCleanModel.refresh()
                                        }
                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    })
                            }
                        },
                        onBlockRename = {
                            context.showDialog {
                                InputPopup(
                                    title = stringResource(id = R.string.rename),
                                    hint = blockName,
                                    defaultText = blockName,
                                    onConfirm = {
                                        areaCleanModel.renameLocalBlock(
                                            localBlockId = block.localBlockId,
                                            name = it
                                        ) {
                                            blockName = it
                                            context.hideDialog()
                                        }
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        })
                }
            }
        }
    }
}