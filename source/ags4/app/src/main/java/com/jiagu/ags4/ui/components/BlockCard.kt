package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun BlockCard(
    modifier: Modifier,
    block: BlockPlan,
    blockName: String,
    isSelect: Boolean = false,
    showDelete: Boolean = true,
    showDivide: Boolean = true,
    showRename: Boolean = true,
    showEdit: Boolean = false,
    editState: Boolean = false,
    showArea: Boolean = true,
    hasExtData: Boolean = false,
    onBlockDelete: () -> Unit = {},
    onBlockDivide: () -> Unit = {},
    onBlockRename: () -> Unit = {},
    onBlockEdit: () -> Unit = {},
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .background(color = Color.White, shape = MaterialTheme.shapes.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = block.createTime.millisToDate("yyyy/MM/dd HH:mm"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (block.working) {
                StatusTag(
                    type = TagType.ERROR,
                    title = stringResource(id = R.string.block_filter_working),
                )
            } else if (block.finish) {
                StatusTag(
                    type = TagType.SECONDARY,
                    title = stringResource(id = R.string.block_filter_done),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AutoScrollingText(
                    text = blockName,
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            if (showArea) {
                Box(modifier = Modifier) {
                    AutoScrollingText(
                        text = UnitHelper.transAreaWithUnit(context, block.area),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Hide popup",
                modifier = Modifier.size(16.dp)
            )
            Box(
                modifier = Modifier.weight(1f)
            ) {
                AutoScrollingText(
                    modifier = Modifier.fillMaxWidth(),
                    text = block.regionName ?: "NA",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Start
                )
            }
            if (hasExtData) {
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center) {
                    Text(
                        text = "V",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center)
                }
            }
        }
        if (isSelect) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    ),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //重命名
                if (showRename) {
                    Button(
                        onClick = onBlockRename,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.rename),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                    VerticalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                //编辑
                if (showEdit) {
                    Button(
                        onClick = onBlockEdit,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        AutoScrollingText(
                            text = if (!editState) stringResource(id = R.string.edit) else stringResource(
                                id = R.string.cancel_edit
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                    VerticalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                //分割地块
                if (showDivide) {
                    Button(
                        onClick = onBlockDivide,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.block_slice),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                    VerticalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                //删除
                if (showDelete) {
                    Button(
                        onClick = onBlockDelete,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.delete),
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BlockCardPreview() {
    ComposeTheme {
        BlockCard(
            modifier = Modifier
                .width(200.dp)
                .height(120.dp),
            block = BlockPlan(
                blockId = 125364L,
                blockType = 0,
                boundary = arrayListOf(),
                blockName = "医疗废物暂存点xxxxxx",
                additional = null,
                area = 123.3f,
                createTime = 1628768419000,
                calibPoints = DoubleArray(0),
                planId = 1,
                workId = 1,
                workPercent = 1
            ),
            blockName = "",
        )
    }
}