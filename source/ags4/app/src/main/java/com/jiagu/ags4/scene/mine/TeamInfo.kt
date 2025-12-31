package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.jiagu.api.ext.toast
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun TeamInfo(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = stringResource(id = R.string.team_info_title), breakAction = {
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
                        Text(
                            text = teamManagementModel.groupInfo?.groupName ?: "",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 30.sp, lineHeight = 34.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                        if (teamManagementModel.curLeaderId == AgsUser.userInfo?.userId) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "edit team name",
                                modifier = Modifier
                                    .size(24.dp)
                                    .noEffectClickable {
                                        context.showDialog {
                                            InputPopup(
                                                title = stringResource(R.string.update_team_name),
                                                hint = stringResource(R.string.team_name_hint),
                                                defaultText = teamManagementModel.groupInfo?.groupName
                                                    ?: "",
                                                textAlign = TextAlign.Start,
                                                isLengthLimit = true,
                                                maxInputLength = 10,
                                                onDismiss = {
                                                    context.hideDialog()
                                                },
                                                onConfirm = { newName ->
                                                    teamManagementModel.updateTeamName(
                                                        newName
                                                    ) { success ->
                                                        if (!success) {
                                                            context.toast(context.getString(R.string.err_server))
                                                        }
                                                    }
                                                    context.hideDialog()
                                                },
                                                confirmText = R.string.save
                                            )
                                        }
                                    },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                ShadowFrame(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        //成员
                        TeamDataRow(
                            title = stringResource(R.string.team_info_member),
                            text = (teamManagementModel.groupInfo?.userCount ?: 0).toString()
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )
                        //任务
                        TeamDataRow(
                            title = stringResource(R.string.team_info_task),
                            text = (teamManagementModel.groupInfo?.taskCount ?: 0).toString()
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        )
                        //作业面积
                        TeamDataRow(
                            title = stringResource(
                                R.string.flight_information_work_area, UnitHelper.areaUnit(context)
                            ), text = UnitHelper.transAreaMu(
                                teamManagementModel.groupInfo?.sprayArea ?: 0f, 1
                            )
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
                        //成员
                        TeamTitleMenuRow(title = stringResource(R.string.team_info_member),
                            content = { //头像
                                if (teamManagementModel.teamUserList.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.fillMaxHeight(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val loadHeadMaxCount = 3
                                        var currentLoadCount = 0
                                        for (user in teamManagementModel.teamUserList) {
                                            if (currentLoadCount >= loadHeadMaxCount) {
                                                Image(
                                                    modifier = Modifier
                                                        .clip(shape = CircleShape)
                                                        .size(30.dp),
                                                    painter = painterResource(id = R.drawable.default_more),
                                                    contentDescription = "default head",
                                                )
                                                break
                                            }
                                            currentLoadCount++
                                            if (!user.userHeadUrl.isNullOrEmpty()) {
                                                AsyncImage(
                                                    modifier = Modifier.size(30.dp).clip(CircleShape),
                                                    model = user.userHeadUrl,
                                                    contentDescription = "background",
                                                    contentScale = ContentScale.FillBounds
                                                )
                                            } else {
                                                Image(
                                                    modifier = Modifier
                                                        .clip(
                                                            shape = CircleShape
                                                        )
                                                        .size(30.dp),
                                                    painter = painterResource(id = R.drawable.default_no_avatar),
                                                    contentDescription = "default head"
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            onClick = {
                                teamManagementModel.refreshTeamMember()
                                navController.navigate("team_member_list")
                            }
                        )
                        //团队作业报表
                        if (AgsUser.userInfo?.userId == teamManagementModel.curLeaderId || teamManagementModel.groupInfo?.hasAuth == true) {
                            TeamTitleMenuRow(title = stringResource(R.string.team_info_work_list),
                                onClick = {
                                    teamManagementModel.loadTeamWorkReport()
                                    navController.navigate("team_group_work_report")
                                })
                        }
                        //个人作业报表
                        if (teamManagementModel.groupInfo?.inGroup == true) {
                            TeamTitleMenuRow(title = stringResource(R.string.team_info_member_work_list),
                                onClick = {
                                    AgsUser.userInfo?.let { user ->
                                        teamManagementModel.loadPersonWorkReport(
                                            userId = user.userId
                                        )
                                        navController.navigate("team_person_work_report")
                                    }
                                }
                            )
                        }
                    }
                }
                //button 解散/退出
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    teamManagementModel.groupInfo?.let {
                        if (it.hasAuth) {
                            Button(modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    context.showDialog {
                                        PromptPopup(content = stringResource(R.string.team_info_disband_content),
                                            onConfirm = {
                                                teamManagementModel.deleteTeam { success ->
                                                    if (success) {
                                                        navController.popBackStack()
                                                    } else {
                                                        context.toast(context.getString(R.string.err_server))
                                                    }
                                                }
                                                context.hideDialog()
                                            },
                                            onDismiss = {
                                                context.hideDialog()
                                            })
                                    }
                                }) {
                                AutoScrollingText(
                                    text = stringResource(R.string.team_info_disband),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        if (it.inGroup && AgsUser.userInfo?.userId != it.leaderUserId) {
                            Button(modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small,
                                onClick = {
                                    context.showDialog {
                                        PromptPopup(content = stringResource(R.string.team_info_leave_content),
                                            onConfirm = {
                                                teamManagementModel.exitTeam { success ->
                                                    if (success) {
                                                        navController.popBackStack()
                                                    } else {
                                                        context.toast(context.getString(R.string.err_server))
                                                    }
                                                }
                                                context.hideDialog()
                                            },
                                            onDismiss = {
                                                context.hideDialog()
                                            })
                                    }
                                }) {
                                AutoScrollingText(
                                    text = stringResource(R.string.exit_the_team),
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