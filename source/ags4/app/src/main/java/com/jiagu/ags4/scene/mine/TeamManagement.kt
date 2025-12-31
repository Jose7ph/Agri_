package com.jiagu.ags4.scene.mine

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.model.Team
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText

@Composable
fun TeamManagement(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val teamManagementModel = LocalTeamManagementModel.current
    MainContent(title = stringResource(id = R.string.mine_team), breakAction = {
        if (!navController.popBackStack()) finish()
    }, barAction = {
        TopBarBottom(text = stringResource(R.string.add_team), onClick = {
            context.showDialog {
                InputPopup(
                    title = stringResource(R.string.create_team_title),
                    hint = stringResource(R.string.team_name_hint),
                    textAlign = TextAlign.Start,
                    isLengthLimit = true,
                    maxInputLength = 10,
                    onDismiss = {
                        context.hideDialog()
                    },
                    onConfirm = {
                        teamManagementModel.createTeam(it)
                        context.hideDialog()
                    },
                    confirmText = R.string.save
                )
            }
        })
    }) {
        LazyGridPaging(modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            items = teamManagementModel.teamPageList,
            item = {team ->
                val isAdmin = team.hasAuth
                var showIdentify = team.inGroup
                var identityName = ""
                var identityColor = Color.Transparent
                if (team.inGroup) {
                    showIdentify = true
                    when (team.identity) {
                        Team.LEADER -> {
                            identityName = stringResource(id = R.string.team_identify_leader)
                            identityColor = Color.Red
                        }

                        Team.MEMBER -> {
                            identityName = stringResource(id = R.string.team_identify_member)
                            identityColor = MaterialTheme.colorScheme.tertiary
                        }
                    }
                }
                ShadowFrame {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .padding(start = 10.dp)
                            .noEffectClickable {
                                teamManagementModel.loadTeamInfos(team.groupId)
                                navController.navigate("team_info")
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            AutoScrollingText(
                                text = team.groupName,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(26.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    ), contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = stringResource(id = R.string.team_identify_admin),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (showIdentify) {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(26.dp)
                                    .background(
                                        color = identityColor, shape = MaterialTheme.shapes.small
                                    ), contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = identityName,
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
                teamManagementModel.refreshTeam()
            })
    }
}

