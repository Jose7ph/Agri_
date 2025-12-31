package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun TeamMemberInfo(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = stringResource(id = R.string.member_info_title), breakAction = {
        if (!navController.popBackStack()) finish()
    }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            //left
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //title
                ShadowFrame(modifier = Modifier.weight(0.6f)) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //头像
                        if (!teamManagementModel.curStaff?.userHeadUrl.isNullOrEmpty()) {
                            AsyncImage(
                                modifier = Modifier.size(60.dp).clip(CircleShape),
                                model = teamManagementModel.curStaff?.userHeadUrl,
                                contentDescription = "background",
                                contentScale = ContentScale.FillBounds
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .clip(shape = CircleShape)
                                    .size(60.dp),
                                painter = painterResource(id = R.drawable.default_no_avatar),
                                contentDescription = "default head",
                            )
                        }
                        //名称
                        Text(
                            text = teamManagementModel.curStaff?.username ?: "",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 30.sp, lineHeight = 34.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                ShadowFrame(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //作业面积
                        TeamDataRow(
                            title = stringResource(
                                R.string.flight_information_work_area, UnitHelper.areaUnit(context)
                            ), text = UnitHelper.transAreaMu(
                                teamManagementModel.staffInfo?.allSprayMu ?: 0f, 1
                            )
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )
                        //飞行次数
                        TeamDataRow(
                            title = stringResource(R.string.mine_flight_frequency),
                            text = (teamManagementModel.staffInfo?.allFlyNum ?: 0).toString()
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )
                        //作业时长
                        TeamDataRow(
                            title = stringResource(R.string.mine_work_duration) + "(${
                                stringResource(
                                    id = R.string.hour
                                )
                            })", text = (teamManagementModel.staffInfo?.allFlyTime ?: 0).toString()
                        )
                    }
                }
            }
            //right
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ShadowFrame(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        //手机号
                        TeamTitleRow(
                            title = stringResource(R.string.member_info_phone_num),
                            text = teamManagementModel.curStaff?.userphone ?: ""
                        )
                        //加入时间
                        TeamTitleRow(
                            title = stringResource(R.string.member_info_join_date),
                            text = teamManagementModel.curStaff?.addToGroupTime?.millisToDate("yyyy-MM-dd HH:mm")
                                ?: ""
                        )
                        //角色
                        TeamTitleRow(
                            title = stringResource(R.string.member_info_identify),
                            text = if (teamManagementModel.curStaff?.userId == teamManagementModel.curLeaderId) stringResource(
                                R.string.team_identify_leader
                            )
                            else stringResource(R.string.team_identify_member)
                        )
                    }
                }
                //button 转让/删除
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    teamManagementModel.curStaff?.let {
                        if (it.userId == teamManagementModel.curLeaderId && it.userId == AgsUser.userInfo?.userId) {
                            Button(modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    teamManagementModel.refreshTeamMember()
                                    navController.navigate("team_transfer_leader")
                                }) {
                                AutoScrollingText(
                                    text = stringResource(R.string.member_info_transfer_leader),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        if ((teamManagementModel.curHasAuth ||
                                    (it.userId == teamManagementModel.curLeaderId &&
                                            it.userId == AgsUser.userInfo?.userId))
                            && it.userId != teamManagementModel.curLeaderId
                        ) {
                            Button(modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    context.showDialog {
                                        PromptPopup(content = stringResource(R.string.member_info_del_member),
                                            onConfirm = {
                                                teamManagementModel.deleteTeamMember {
                                                    navController.popBackStack()
                                                }
                                                context.hideDialog()
                                            },
                                            onDismiss = {
                                                context.hideDialog()
                                            })
                                    }
                                }) {
                                AutoScrollingText(
                                    text = stringResource(R.string.member_info_del_member),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}