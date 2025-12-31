package com.jiagu.ags4.scene.work

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.ui.components.BlockCard
import com.jiagu.ags4.ui.components.SortButton
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.LEFT_DRAWER_MAX_WIDTH
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.VeryDarkAlpha
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteModel
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
fun WorkFreeAirRoute(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val freeAirRouteModel =
        remember { navController.getViewModel(route, FreeAirRouteModel::class.java) }
    val imuData by DroneModel.imuData.observeAsState()
    val highlightBlocks by freeAirRouteModel.highlightBlocks.collectAsState()
    val localBlocksCanvas by freeAirRouteModel.localBlocksCanvasFlow.collectAsState(emptyList())
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val curIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(imuData) {
        imuData?.let {
            freeAirRouteModel.processImuData(it)
        }
    }
    DisposableEffect(Unit) {
        if (imuData == null) {
            //初始化数据默认给手机位置，如果后续imu有数据则会覆盖
            freeAirRouteModel.dronePosition = mapVideoModel.phonePosition
        } else {
            freeAirRouteModel.processImuData(imuData!!)
        }
        if (freeAirRouteModel.pageDataInit) {
            freeAirRouteModel.refresh()
            freeAirRouteModel.pageDataInit = false
        } else {
            //如果列表有数据，但是canvas没有数据，则只触发canvas绘制地块,主要用于切换页面后返回，不重新refresh列表数据的情况
            if (freeAirRouteModel.localBlocksListFlow.value.isNotEmpty() && localBlocksCanvas.isEmpty()) {
                freeAirRouteModel.pushCanvasFlow()
            }
        }
        onDispose {
            freeAirRouteModel.stopCanvasFlow() //页面切换后组件销毁，同时停止正在执行的列表刷新和正在绘制的canvas
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
                    freeAirRouteModel = freeAirRouteModel,
                    highlightBlocks = highlightBlocks,
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
                RightButtonCommon(text = stringResource(id = R.string.add_block)) {
                    //清除选中的数据
                    freeAirRouteModel.clearBP()
                    navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url)
                }
                //编辑
                RightButtonCommon(
                    text = stringResource(id = R.string.edit_block),
                    enabled = freeAirRouteModel.selectedBP != null,
                ) {
                    navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url)
                }
                //参数
                RightButtonCommon(
                    text = stringResource(id = R.string.work_parameter),
                    enabled = freeAirRouteModel.selectedBP != null
                ) {
                    freeAirRouteModel.selectedBP?.let { bp ->
                        //判断是新规划还是已有规划 (有断点 && 有plan && 正在工作) 必须都满足
                        freeAirRouteModel.isNewPlan =
                            !(bp.plan != null && bp.working && freeAirRouteModel.breakPoint != null)
                        //已有规划给弹窗提示是否继续
                        if (!freeAirRouteModel.isNewPlan) {
                            context.showDialog {
                                PromptPopup(
                                    content = stringResource(R.string.breakpoint_detected_continue),
                                    onConfirm = {
                                        freeAirRouteModel.isNewPlan = false
                                        navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url)
                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        freeAirRouteModel.isNewPlan = true
                                        navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url)
                                        context.hideDialog()
                                    })
                            }
                        } else {
                            freeAirRouteModel.isNewPlan = true
                            navController.navigate(WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url)
                            context.hideDialog()
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContentBlockCardList(
    modifier: Modifier = Modifier,
    freeAirRouteModel: FreeAirRouteModel,
    listState: LazyListState,
    highlightBlocks: List<BlockPlan>,
) {
    val context = LocalContext.current
    val state = rememberPullToRefreshState()
    val localBlocksList by freeAirRouteModel.localBlocksListFlow.collectAsState()
    LaunchedEffect(highlightBlocks) {
        if (highlightBlocks.size == 1) {
            // 计算目标索引
            val targetIndex = localBlocksList.indexOfFirst {
                it.uniqueId() == highlightBlocks[0].uniqueId()
            }
            // 判断是否可见
            val isItemVisible =
                targetIndex in listState.layoutInfo.visibleItemsInfo.map { it.index }
            if (targetIndex != -1 && !isItemVisible) {
                listState.scrollToItem(targetIndex)
            }
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
                currentType = freeAirRouteModel.timeSort,
                textColor = MaterialTheme.colorScheme.onPrimary,
                imageSize = 16.dp,
                modifier = Modifier.weight(1f)
            ) {
                freeAirRouteModel.timeSort = it
                freeAirRouteModel.refresh()
            }
            if (freeAirRouteModel.syncStatus != SyncStatusEnum.NONE) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (freeAirRouteModel.syncStatus == SyncStatusEnum.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(12.dp),
                            color = Color.White,
                            strokeWidth = 1.dp
                        )
                    }
                    AutoScrollingText(
                        text = when (freeAirRouteModel.syncStatus) {
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
            text = freeAirRouteModel.search,
            type = freeAirRouteModel.blockState,
            onTextChanged = {
                freeAirRouteModel.search = it
            },
            onTypeChanged = {
                freeAirRouteModel.blockState = it
            },
            onSearch = {
                freeAirRouteModel.refresh()
            }
        )
        Row {
            PullToRefreshBox(
                modifier = Modifier,
                state = state,
                isRefreshing = freeAirRouteModel.isLoading,
                onRefresh = { freeAirRouteModel.refresh() },
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    state = listState
                ) {
                    items(localBlocksList.size) {
                        val block = localBlocksList[it]
                        var blockName by remember {
                            mutableStateOf(block.blockName)
                        }
                        val isSelect = freeAirRouteModel.selectedLocalBlockId == block.localBlockId
                        BlockCard(
                            modifier = Modifier
                                .clickable {
                                    freeAirRouteModel.clearBP()
                                    if (!isSelect) {
                                        freeAirRouteModel.setBP(block)
                                    }
                                },
                            block = block,
                            blockName = blockName,
                            isSelect = isSelect,
                            showDivide = false,
                            showArea = false,
                            onBlockDelete = {
                                context.showDialog {
                                    PromptPopup(
                                        title = stringResource(id = R.string.text_dialog_delete_block),
                                        content = stringResource(id = R.string.text_dialog_delete_block_text),
                                        onConfirm = {
                                            freeAirRouteModel.clearBP()
                                            freeAirRouteModel.deleteLocalBlock(
                                                block.localBlockId
                                            ) {
                                                //如果右侧高亮地块列表有这个数据切这个数据删除了，则将数据从列表中删除
                                                if (freeAirRouteModel.highlightedBlocks.size > 1) {
                                                    freeAirRouteModel.highlightedBlocks.removeIf { it.localBlockId == block.localBlockId }
                                                }
                                                freeAirRouteModel.refresh()
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
                                        defaultText = blockName,
                                        hint = blockName,
                                        onConfirm = {
                                            freeAirRouteModel.renameLocalBlock(
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
}