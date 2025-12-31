package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText

@Composable
fun TeamMemberList(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = stringResource(id = R.string.team_member_list_title), breakAction = {
        if (!navController.popBackStack()) finish()
    }, barAction = {
        if (AgsUser.userInfo?.userId == teamManagementModel.curLeaderId || teamManagementModel.curHasAuth) {
            TopBarBottom(text = stringResource(R.string.add_member), onClick = {
                navController.navigate("team_add_member_menu")
            })
        }
    }) {
        LazyGridPaging(modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            items = teamManagementModel.teamMemberList,
            item = {member ->
                ShadowFrame {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .padding(start = 10.dp)
                            .noEffectClickable {
                                teamManagementModel.curStaff = member
                                teamManagementModel.getStaffInfo {
                                    navController.navigate("team_member_info")
                                }
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!member.userHeadUrl.isNullOrEmpty()) {
                            AsyncImage(
                                modifier = Modifier.size(30.dp).clip(CircleShape),
                                model = member.userHeadUrl,
                                contentDescription = "background",
                                contentScale = ContentScale.FillBounds
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .clip(shape = CircleShape)
                                    .size(30.dp),
                                painter = painterResource(id = R.drawable.default_no_avatar),
                                contentDescription = "default head",
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = member.username,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        if (member.userId == teamManagementModel.curLeaderId) {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(26.dp)
                                    .background(
                                        color = Color.Red,
                                        shape = MaterialTheme.shapes.small
                                    ), contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = stringResource(id = R.string.team_identify_leader),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            onRefresh = {
                teamManagementModel.refreshTeamMember()
            })
    }
}