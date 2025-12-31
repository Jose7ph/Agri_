package com.jiagu.ags4.scene.mine

import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.api.ext.millisToDateTime
import com.jiagu.api.ext.toString
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.picker.DateRangePicker
import com.jiagu.jgcompose.picker.ListPicker
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.utils.DateUtils
import com.jiagu.tools.ext.UnitHelper
import java.util.Date


@Composable
fun TeamGroupWorkReport(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val teamManagementModel = LocalTeamManagementModel.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MineActivity
    MainContent(title = stringResource(R.string.team_info_work_list), breakAction = {
        if (!navController.popBackStack()) finish()
    }) {
        Row(modifier = Modifier.fillMaxSize()) {
            //统计
            ShadowFrame(modifier = Modifier.weight(0.18f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    //作业面积
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        TeamDataColumn(
                            title = stringResource(
                                R.string.flight_information_work_area, UnitHelper.areaUnit(context)
                            ), text = UnitHelper.transAreaMu(
                                teamManagementModel.teamWorkStatic?.sprayRange ?: 0f, 1
                            )
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )
                    //飞行次数
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        TeamDataColumn(
                            title = stringResource(R.string.mine_flight_frequency),
                            text = (teamManagementModel.teamWorkStatic?.sortieCount
                                ?: 0).toString()
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )
                    //作业时长
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        TeamDataColumn(
                            title = stringResource(R.string.mine_work_duration) + "(${
                                stringResource(
                                    id = R.string.hour
                                )
                            })",
                            text = (teamManagementModel.teamWorkStatic?.flightTime
                                ?: 0f).toString(1)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            ) {
                //时间筛选
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    //时间选择
                    ShadowFrame {
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(shape = MaterialTheme.shapes.extraSmall)
                                .noEffectClickable {
                                    context.showDialog {
                                        DateRangePicker(defaultStartDate = DateUtils.getDateString(
                                            teamManagementModel.workReportStartTime
                                        ),
                                            defaultEndDate = DateUtils.getDateString(
                                                teamManagementModel.workReportEndTime
                                            ),
                                            onConfirm = { _, _, st, _, et ->
                                                teamManagementModel.workReportStartTime =
                                                    DateUtils.stringToDateTime(st) ?: Date()
                                                teamManagementModel.workReportEndTime =
                                                    DateUtils.stringToDateTime(et) ?: Date()
                                                teamManagementModel.loadTeamWorkReport()
                                                context.hideDialog()
                                            },
                                            onCancel = {
                                                context.hideDialog()
                                            })
                                    }
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(60.dp)
                                    .background(color = MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = stringResource(R.string.time), color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = DateUtils.getDateString(teamManagementModel.workReportStartTime) + " ~ " + DateUtils.getDateString(
                                        teamManagementModel.workReportEndTime
                                    ), style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "show time range"
                            )
                        }
                    }
                    //人员选择
                    ShadowFrame {
                        val selectedIndexes = remember { mutableStateListOf<Int>() }
                        Row(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(shape = MaterialTheme.shapes.extraSmall)
                                .noEffectClickable {
                                    context.showDialog {
                                        ListPicker(
                                            rowItemNum = 5,
                                            selectedIndexes = selectedIndexes,
                                            items = teamManagementModel.teamUserList.map { it.username },
                                            isSingleSelect = false,
                                            onConfirm = { indexes ->
                                                teamManagementModel.selectedMembers.clear()
                                                selectedIndexes.clear()
                                                if (indexes.isNotEmpty()) {
                                                    selectedIndexes.addAll(indexes)
                                                }
                                                val selectedMembers =
                                                    teamManagementModel.teamUserList
                                                        .filterIndexed { index, _ ->
                                                            indexes.contains(index)
                                                        }
                                                teamManagementModel.selectedMembers.addAll(
                                                    selectedMembers
                                                )
                                                teamManagementModel.loadTeamWorkReport()
                                                context.hideDialog()
                                            },
                                            onCancel = {
                                                selectedIndexes.clear()
                                                teamManagementModel.selectedMembers.clear()
                                                teamManagementModel.loadTeamWorkReport()
                                                context.hideDialog()
                                            }
                                        )
                                    }
                                }, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(60.dp)
                                    .background(color = MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = stringResource(R.string.team_info_member),
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = if (teamManagementModel.selectedMembers.isEmpty()) stringResource(
                                        R.string.block_filter_all
                                    ) else teamManagementModel.selectedMembers.joinToString(",") { it.username },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "show member"
                            )
                        }
                    }
                }
                //列表
                LazyGridPaging(modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    columns = GridCells.Fixed(2),
                    items = teamManagementModel.teamWorkReportList,
                    item = {report ->
                        ShadowFrame {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .noEffectClickable {
                                        activity.toDetail(
                                            sortieId = report.sortieId,
                                            droneId = report.droneId,
                                            startTime = report.createTime
                                        )
                                    },
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        //时间
                                        Box(modifier = Modifier.weight(1f)) {
                                            AutoScrollingText(
                                                text = report.createTime.millisToDateTime(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.DarkGray
                                            )
                                        }
                                        //手动/自动
                                        Box(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .background(
                                                    color = if (report.isAuto) MaterialTheme.colorScheme.primary else Color.Red,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                )
                                        ) {
                                            AutoScrollingText(
                                                text = if (report.isAuto) stringResource(
                                                    R.string.auto
                                                ) else stringResource(R.string.manual),
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        //地区
                                        Box(modifier = Modifier.weight(1f)) {
                                            AutoScrollingText(
                                                text = report.regionName,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                        //面积
                                        Box(
                                            modifier = Modifier
                                        ) {
                                            AutoScrollingText(
                                                text = UnitHelper.transAreaMu(
                                                    report.sprayRange,
                                                    1
                                                ) + UnitHelper.areaUnit(context),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                }
                                //icon
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "show sortie",
                                    modifier = Modifier.width(20.dp)
                                )
                            }
                        }
                    },
                    onRefresh = {
                        teamManagementModel.loadTeamWorkReport()
                    })

            }
        }

    }
}