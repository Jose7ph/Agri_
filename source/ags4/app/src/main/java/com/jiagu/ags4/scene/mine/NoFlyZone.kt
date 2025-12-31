package com.jiagu.ags4.scene.mine

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.startActivity
import com.jiagu.api.ext.millisToDate
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.label.ComboRollButtonLabel
import com.jiagu.jgcompose.label.InputSearchLabel
import com.jiagu.jgcompose.paging.LazyGridPaging
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 禁飞区
 *
 */
@Composable
fun NoFlyZone() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val noFlyZoneModel = LocalNoFlyZoneModel.current

    MainContent(title = stringResource(id = R.string.no_fly_zone), breakAction = {
        navController.popBackStack()
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //检索条件
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)

            ) {
                //名称
                ShadowFrame(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    InputSearchLabel(modifier = Modifier.fillMaxSize(),
                        labelName = stringResource(id = R.string.dev_detail_base_name),
                        labelWidth = 80.dp,
                        text = noFlyZoneModel.detailAddress,
                        hint = stringResource(id = R.string.please_enter),
                        onInputChange = {
                            noFlyZoneModel.detailAddress = it
                        },
                        onSearch = {
                            noFlyZoneModel.refresh()
                        })
                }
                //类型
                ShadowFrame(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                ) {
                    ComboRollButtonLabel(modifier = Modifier.fillMaxSize(),
                        labelName = stringResource(id = R.string.device_engine_type),
                        labelWidth = 80.dp,
                        comboIndex = noFlyZoneModel.noflyType.ordinal,
                        comboValue = stringResource(id = noFlyZoneModel.noflyType.value),
                        comboItems = NoFlyTypeEnum.entries.map { stringResource(id = it.value) },
                        onConfirm = { index, _ ->
                            noFlyZoneModel.noflyType = NoFlyTypeEnum.entries[index]
                            noFlyZoneModel.refresh()
                        },
                        onCancel = {})
                }
            }

            //列表
            LazyGridPaging(modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                items = noFlyZoneModel.noFlyZonePageList,
                item = {noFlyZone ->
                    ShadowFrame(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 10.dp)
                                .clickable {
                                    (context as Activity).startActivity(
                                        NoFlyZoneActivity::class.java,
                                        "noFlyBlocks" to noFlyZone.orbit
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AutoScrollingText(
                                    text = noFlyZone.detailAddress,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                AutoScrollingText(
                                    text = "${noFlyZone.effectStartTime.millisToDate("yyyy/MM/dd")} ~ ${
                                        noFlyZone.effectEndTime.millisToDate("yyyy/MM/dd")
                                    }",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(0.3f)
                                    .height(30.dp)
                                    .background(
                                        color = if (noFlyZone.isEnable == 1) MaterialTheme.colorScheme.primary else Color.Red,
                                        shape = MaterialTheme.shapes.small
                                    ), contentAlignment = Alignment.Center
                            ) {
                                AutoScrollingText(
                                    text = when (noFlyZone.isEnable) {
                                        1 -> stringResource(id = R.string.effective)
                                        else -> stringResource(id = R.string.invalid)
                                    }, color = Color.White

                                )
                            }
                        }
                    }
                },
                onRefresh = {
                    noFlyZoneModel.refresh()
                })
        }
    }
}

@Composable
fun NoFlyBlocks(finish: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .padding(top = 10.dp, start = 10.dp)
                .size(36.dp)
                .clip(shape = CircleShape)
                .align(Alignment.TopStart)
                .clickable { finish() },
            color = Color.White
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                modifier = Modifier
                    .size(36.dp)
                    .padding(vertical = 6.dp),
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}

