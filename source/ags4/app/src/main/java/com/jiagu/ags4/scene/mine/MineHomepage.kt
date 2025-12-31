package com.jiagu.ags4.scene.mine

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.LocalSortieManagementVM
import com.jiagu.api.ext.toString
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

@Composable
fun MineHomepage(finish: () -> Unit = {}) {
    val navController = LocalNavController.current
    val mineModel = LocalMineModel.current
    val noFlyZoneModel = LocalNoFlyZoneModel.current
    val sortieManagementVM = LocalSortieManagementVM.current
    val context = LocalContext.current
    val teamManagementModel = LocalTeamManagementModel.current
    var showSyn by remember {
        mutableIntStateOf(0)
    }
    var showAccountChange by rememberSaveable {
        mutableStateOf(false)
    }
    MainContent(title = stringResource(id = R.string.accounts), barAction = {
        if (showSyn >= 10) {
            TopBarBottom(text = stringResource(id = R.string.sync_data)) {
                mineModel.sync()
            }
        }
    }, breakAction = {
        if (!navController.popBackStack()) finish()
    }) {
        //content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ShadowFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                ) {
                    MineInformation(modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp)
                        .clickable(enabled = showSyn < 10,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            showSyn++
                        })
                }
                ShadowFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    WorkData(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 30.dp)
                            .padding(vertical = 6.dp), mineModel = mineModel
                    )
                }
                Logout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                )
            }
            ShadowFrame(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MineMenu(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                    onMenuClick = { menuType ->
                        when (menuType) {
                            MineMenuType.SORTIE_MANAGEMENT -> {
                                sortieManagementVM.refresh()
                                navController.navigate(
                                    "mine_sortie_management"
                                )
                            }

                            MineMenuType.LAND_MANAGEMENT -> {
                                mineModel.refresh()
                                navController.navigate(
                                    "mine_land_block_management"
                                )
                            }

                            MineMenuType.MINE_DEVICE -> {
                                navController.navigate(
                                    "mine_device"
                                )
                            }

                            MineMenuType.TEAM_MANAGEMENT -> {
                                teamManagementModel.refreshTeam()
                                navController.navigate(
                                    "mine_team_management"
                                )
                            }

                            MineMenuType.LOG_MANAGEMENT -> (context as MineActivity).startActivity(
                                LogManagementActivity::class.java
                            )

                            MineMenuType.NO_FLY_ZONE -> {
                                noFlyZoneModel.refresh()
                                navController.navigate(
                                    "no_fly_zone"
                                )
                            }

                            MineMenuType.GENERAL_SETTINGS -> navController.navigate(
                                "mine_general_settings"
                            )

                            MineMenuType.ACCOUNT_SECURITY -> showAccountChange = true

                        }
                    })
            }
        }
    }
    if (showAccountChange) {
        AccountChangeMenu(navController = navController, onClose = { showAccountChange = false })
    }
}

/**
 * 我的信息
 */
@Composable
fun MineInformation(modifier: Modifier = Modifier) {
    val userName = AgsUser.userInfo?.userName ?: ""
    val phoneNumber = AgsUser.userInfo?.userPhone ?: ""
    val userHeadUrl = AgsUser.userInfo?.userHeadUrl ?: ""
    val email = AgsUser.userInfo?.email ?: ""
    val team = AgsUser.workGroup?.groupName ?: ""
    val headImageSize = 36.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (userHeadUrl.isNotBlank()) {
            AsyncImage(
                model = userHeadUrl,
                contentDescription = "avatar",
                modifier = Modifier
                    .size(headImageSize)
                    .clip(CircleShape)

            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_mine_avatar),
                contentDescription = "avatar",
                modifier = Modifier
                    .size(headImageSize)
                    .clip(CircleShape)
                    .border(
                        color = MaterialTheme.colorScheme.primary, width = 1.dp, shape = CircleShape
                    )
                    .padding(2.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            AutoScrollingText(
                text = userName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            AutoScrollingText(
                text = phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            if (email.isNotEmpty()) {
                AutoScrollingText(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            if (team.isNotEmpty()) {
                AutoScrollingText(
                    text = team,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

/**
 * 工作数据
 */
@Composable
private fun WorkData(modifier: Modifier = Modifier, mineModel: MineModel) {
    val imageSize = 36.dp
    val rowDataSpace = 10.dp
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowDataSpace)
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_work_area),
                contentDescription = "mine work area",
                modifier = Modifier.size(imageSize),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Column(
                modifier = Modifier
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.mine_work_area),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                AutoScrollingText(
                    text = UnitHelper.transAreaWithUnit(
                        context, mineModel.userStatistic?.allSprayMu ?: 0F
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowDataSpace)
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_work_time),
                contentDescription = "mine work duration",
                modifier = Modifier.size(imageSize),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Column(
                modifier = Modifier
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.mine_work_duration),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                AutoScrollingText(
                    text = "${mineModel.userStatistic?.allFlyTime?.toString(1)}${stringResource(id = R.string.hour)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(rowDataSpace)
        ) {
            Image(
                painter = painterResource(id = R.drawable.default_flight_count),
                contentDescription = "mine flight frequency",
                modifier = Modifier.size(imageSize),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Column(
                modifier = Modifier
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.mine_flight_frequency),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                AutoScrollingText(
                    text = mineModel.userStatistic?.allFlyNum.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

/**
 * 菜单
 */
@Composable
private fun MineMenu(
    modifier: Modifier = Modifier, onMenuClick: (MineMenuType) -> Unit
) {
    val mineMenuTypes = MineMenuType.entries.toTypedArray()
    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.SpaceEvenly
    ) {
        for ((i, menuType) in mineMenuTypes.withIndex()) {
            if ((menuType == MineMenuType.NO_FLY_ZONE) && !BuildConfig.DEBUG) continue
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable {
                            onMenuClick(menuType)
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = menuType.image),
                        contentDescription = "image $i",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                    AutoScrollingText(
                        text = stringResource(id = menuType.title),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Logout(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val act = LocalActivity.current as MineActivity
    Button(
        onClick = {
            context.showDialog {
                PromptPopup(content = stringResource(id = R.string.logout_confirm), onConfirm = {
                    act.cleanUserInfo()
                    context.hideDialog()
                }, onDismiss = {
                    context.hideDialog()
                })
            }
        },
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small
    ) {
        AutoScrollingText(
            text = stringResource(id = R.string.logout),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun AccountChangeMenu(navController: NavController, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = DrawerDefaults.scrimColor)
            .clickable(enabled = false) { }, contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = Color.White, shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .clickable(false) { },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                AutoScrollingText(
                    text = stringResource(id = R.string.account) + stringResource(
                        id = R.string.change
                    ), style = MaterialTheme.typography.titleSmall
                )
                Icon(imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            onClose()
                        })

            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AccountChangeRow(title = stringResource(id = R.string.phone_number)) {
                    navController.navigate("mine_account_bind_phone_number")
                }
                AccountChangeRow(title = stringResource(id = R.string.email)) {
                    navController.navigate("mine_account_bind_email")
                }
                AccountChangeRow(title = stringResource(id = R.string.change_password)) {
                    navController.navigate("mine_change_password")
                }
            }
        }
    }
}

@Composable
private fun AccountChangeRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "KeyboardArrowRight",
            modifier = Modifier.fillMaxHeight()
        )
    }
}
