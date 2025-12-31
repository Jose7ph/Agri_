package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.BlockItem
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.CacheModel
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.label.DateRangeLabel
import com.jiagu.jgcompose.label.InputSearchLabel
import com.jiagu.jgcompose.label.RegionSelectionLabel
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun LandBlockManagement(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val mineModel = LocalMineModel.current
    val context = LocalContext.current
    val activity = context as MineActivity
    MainContent(title = stringResource(id = R.string.mine_land_management), breakAction = {
        mineModel.landBlockVmDataClean()
        if (!navController.popBackStack()) finish()
    }) {
        //检索条件
        RetrievalCondition(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .height(30.dp), vm = mineModel
        )
        //地块列表
        LazyGridPaging(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            items = mineModel.landBlockPageList,
            item = {landBlock ->
                ShadowFrame {
                    LandInfo(
                        modifier = Modifier
                            .height(60.dp)
                            .clickable {
                                activity.startActivity(
                                    BlockDetailActivity::class.java, "blockId" to landBlock.blockId
                                )
                            }, landBlock = landBlock
                    )
                }
            },
            onRefresh = {
                mineModel.refresh()
            }
        )
    }
}

@Composable
fun RetrievalCondition(
    modifier: Modifier = Modifier, vm: MineModel,
) {
    val titleWidth = 50.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //关键词
        ShadowFrame(modifier = Modifier.weight(0.3f)) {
            InputSearchLabel(
                modifier = Modifier.fillMaxSize(),
                text = vm.search ?: "",
                hint = stringResource(id = R.string.please_enter),
                labelWidth = titleWidth,
                labelName = stringResource(id = R.string.search),
                onInputChange = {
                    vm.search = it
                },
                onSearch = {
                    vm.refresh()
                })

        }
        //时间
        ShadowFrame(modifier = Modifier.weight(0.5f)) {
            DateRangeLabel(
                modifier = Modifier.fillMaxSize(),
                labelWidth = titleWidth,
                labelName = stringResource(id = R.string.time),
                defaultStartDate = vm.startTime ?: "",
                defaultEndDate = vm.endTime ?: "",
                onConfirm = { _, _, st, _, et ->
                    vm.startTime = st
                    vm.endTime = et
                    //更新数据
                    vm.refresh()
                },
                onCancel = {
                    //检索开始时间清空
                    vm.startTime = null
                    //检索结束时间清空
                    vm.endTime = null
                    //更新数据
                    vm.refresh()
                })
        }
        //位置
        ShadowFrame(modifier = Modifier.weight(0.3f)) {
            RegionSelectionLabel(
                modifier = Modifier.fillMaxSize(),
                labelWidth = titleWidth,
                labelName = stringResource(id = R.string.position),
                defaultText = vm.region?.name ?: "",
                regions = CacheModel.convertAddressList(),
                onConfirm = {
                    vm.region = it
                    vm.refresh()
                },
                onDismiss = {
                    vm.region = null
                    vm.refresh()
                })
        }
    }
}

@Composable
fun LandInfo(
    modifier: Modifier = Modifier, landBlock: BlockItem,
) {
    val context = LocalContext.current
    val width = 150.dp
    Row(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.width(width),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AutoScrollingText(
                    text = landBlock.blockName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                AutoScrollingText(
                    text = landBlock.regionName ?: "NA",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            AutoScrollingText(
                text = UnitHelper.transAreaWithUnit(context, landBlock.area),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            AutoScrollingText(
                text = landBlock.createTime.millisToDate("yyyy-MM-dd"),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}