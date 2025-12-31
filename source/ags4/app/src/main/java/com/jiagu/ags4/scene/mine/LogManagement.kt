package com.jiagu.ags4.scene.mine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.LocalLogModel
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.container.MainContent

@Composable
fun LogManagement(finish: () -> Unit = {}) {
    val logVM = LocalLogModel.current
    var value by remember { mutableStateOf(2) }
    val ctx = LocalContext.current
    val activity = ctx as LogManagementActivity
    MainContent(
        title = stringResource(id = R.string.mine_log_management),
        breakAction = { finish() }) {

        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .fillMaxWidth()
                .height(40.dp),
        ) {
            val names = stringArrayResource(id = R.array.mine_log_management_type).toList()
            val values = mutableListOf<Int>()
            for (i in names.indices) {
                values.add(i)
            }
            GroupButton(
                items = names,
                indexes = values,
                number = value,
                modifier = Modifier.width(320.dp).height(40.dp),
                textStyle = MaterialTheme.typography.titleSmall
            ) {idx,_->
                value = idx
                logVM.curPage = idx
                logVM.getData()
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .fillMaxHeight()
                    .width(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable {
                        activity.clearLogs()
                    }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.clear_all),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),

            ) {
            if (logVM.curPage != 2) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(logVM.appLogs.size) {
                        val log = logVM.appLogs[it]
                        Row(
                            modifier = Modifier
                                .height(50.dp)
                                .shadow(elevation = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = log.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                )
                                Text(
                                    text = log.fileSize,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                )
                            }
                            UploadAndShareBottom(modifier = Modifier.weight(0.4f), onUpload = {
                                activity.uploadFile(log)
                            }, onShare = {
                                activity.shareFile(log)
                            })
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    items(logVM.logList.size) {
                        val log = logVM.logList[it]
                        Row(
                            modifier = Modifier
                                .height(50.dp)
                                .background(
                                    MaterialTheme.colorScheme.onPrimary,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = log.file,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (log.downloaded) {
                                UploadAndShareBottom(modifier = Modifier.weight(0.4f), onUpload = {
                                    activity.uploadFcuFile(log.file)
                                }, onShare = {
                                    activity.shareFcuFile(log.file)
                                })
                            } else {
                                OperateIcon(
                                    iconInt = R.drawable.default_download
                                ) {
                                    activity.downloadFcuLog(log.file, log.fcIdx)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 上传 and 分享
 */
@Composable
private fun UploadAndShareBottom(
    modifier: Modifier = Modifier, onUpload: () -> Unit, onShare: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OperateIcon(iconInt = R.drawable.default_upload) {
            onUpload()
        }
        Spacer(modifier = Modifier.width(10.dp))
        OperateIcon(iconInt = R.drawable.default_share) {
            onShare()
        }
    }
}

@Composable
private fun OperateIcon(
    modifier: Modifier = Modifier, iconInt: Int, onClick: () -> Unit
) {
    Icon(
        painter = painterResource(id = iconInt),
        contentDescription = "$iconInt",
        modifier = modifier
            .size(26.dp)
            .clickable {
                onClick()
            },
        tint = MaterialTheme.colorScheme.primary
    )
}

