package com.jiagu.ags4.scene.factory

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.V9Util
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.counter.FloatChangeAskCounter
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ImagePromptPopup
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 高级设置-安装设置
 */
@Composable
fun FactorySettingsInstall() {
    val aptypeData by DroneModel.aptypeData.observeAsState(initial = null)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //分隔符
        item {
            RowSeparationBox(
                modifier = Modifier, title = stringResource(id = R.string.fc_install_row_title)
            )
        }
        //飞控安装
        item {
            FlightControlInstallation(
                modifier = Modifier, aptypeData = aptypeData
            )
        }
        //分隔符
        item {
            RowSeparationBox(
                modifier = Modifier, title = stringResource(id = R.string.gnss_install_row_title)
            )
        }
        //GNSS安装
        item {
            GNSSInstallation(
                modifier = Modifier, aptypeData = aptypeData
            )
        }
        //分隔符
        item {
            RowSeparationBox(
                modifier = Modifier, title = stringResource(id = R.string.rtk_install_row_title)
            )
        }
        //RTK安装
        item {
            RTKInstallation(
                modifier = Modifier, aptypeData = aptypeData
            )
        }
        //分隔符
        item {
            RowSeparationBox(
                modifier = Modifier, title = stringResource(id = R.string.radar_install_row_title)
            )
        }
        //雷达安装
        item {
            RadarInstallation(
                modifier = Modifier, aptypeData = aptypeData
            )
        }
        //分隔符
        item {
            RowSeparationBox(
                modifier = Modifier, title = stringResource(id = R.string.motor_type)
            )
        }
        item {
            MotorType(aptypeData = aptypeData)
        }
        item {
            Spacer(modifier = Modifier.height(20.dp)) // 添加底部间距
        }


    }
}

/**
 * 飞控安装
 * 安装方向 1.前 2.右 3.后 4.左 7.上
 * X偏差 -125 ~ 125
 * Y偏差 -125 ~ 125
 */
@Composable
private fun FlightControlInstallation(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    val context = LocalContext.current
    val numberX = aptypeData?.getValue(63) ?: 0f
    val numberY = aptypeData?.getValue(64) ?: 0f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //安装方向
            val isV9p = V9Util.isV9P(DroneModel.verData.value?.serial)
            val allNames =
                stringArrayResource(id = R.array.flight_control_installation_method).toList()
            val allValues = listOf(1, 2, 3, 4, 5, 9)
            val names: List<String>
            val values: List<Int>
            if (isV9p) {
                names = allNames.subList(4, allNames.size)
                values = allValues.subList(4, allValues.size)
            } else {
                names = allNames.subList(0, 4)
                values = allValues.subList(0, 4)
            }
            val value = aptypeData?.getIntValue(VKAg.APTYPE_SETUP_DIR) ?: 0
            GroupAskButtonRow(
                title = stringResource(id = R.string.fc_install_direction),
                items = names,
                indexes = values,
                number = value,
                askPopup = { idx, _, complete ->
                    PromptPopup(content = buildAnnotatedString {
                        append(stringResource(id = R.string.fc_install_direction_confirm_tip))
                        withStyle(
                            style = SpanStyle(color = Color.Red, fontWeight = FontWeight.W800)
                        ) {
                            append(names[values.indexOf(idx)])
                        }
                    }, onConfirm = {
                        sendParameter(VKAg.APTYPE_SETUP_DIR, idx.toFloat())
                        complete(true)
                    }, onDismiss = {
                        complete(false)
                    })
                }
            )
            //偏差设置
            TitleRowText(title = stringResource(id = R.string.fc_install_deviation_setting))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.description_imu),
                    contentDescription = "fc_deviation_img",
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.5f)
                        .clickable {
                            context.showDialog {
                                ImagePromptPopup(image = R.drawable.description_imu,
                                    imageSize = 280.dp,
                                    showCancel = false,
                                    showConfirm = false,
                                    onConfirm = {},
                                    onDismiss = { context.hideDialog() })
                            }
                        })
                //偏差值
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    //偏差X
                    FactoryXYCounterRow(
                        title = R.string.x_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberX,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(63, it.toInt()) },
                        onDismiss = {}
                    )
                    //偏差Y
                    FactoryXYCounterRow(
                        title = R.string.y_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberY,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(64, it.toInt()) },
                        onDismiss = {}
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.weight(0.5f))
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = {
                                    context.showDialog {
                                        PromptPopup(content = stringResource(id = R.string.horizontal_calibration_tip),
                                            onConfirm = {
                                                DroneModel.activeDrone?.calibHorz()
                                                context.hideDialog()
                                            },
                                            onDismiss = { context.hideDialog() })
                                    }

                                },
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(120.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                AutoScrollingText(
                                    text = stringResource(id = R.string.horizontal_standard),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                        }
                    }
                }

            }
        }
        VerticalDivider(
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 10.dp),
            color = Color.Gray
        )
        //提示文本
        LazyColumn(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                TipText(text = buildAnnotatedString {
                    append(stringResource(id = R.string.fc_install_suggest1_1))
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append(" " + stringResource(id = R.string.fc_install_suggest1_2) + " ")
                    }
                    append(stringResource(id = R.string.fc_install_suggest1_3))
                    append("\n\n")
                    append(stringResource(id = R.string.fc_install_suggest2))
                    append("\n\n")
                    append(stringResource(id = R.string.fc_install_suggest3))
                })
            }
        }
    }
}

/**
 * GNSS安装
 * X偏差 -125 ~ 125
 * Y偏差 -125 ~ 125
 */
@Composable
private fun GNSSInstallation(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    val context = LocalContext.current
    val numberX = aptypeData?.getValue(59) ?: 0f
    val numberY = aptypeData?.getValue(60) ?: 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //GNSS配置
            TitleRowText(title = stringResource(id = R.string.gnss_install_setting))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val gpsType = stringArrayResource(id = R.array.gps_a_or_b).toList()
                for (i in gpsType.indices) {
                    Button(
                        onClick = {
                            context.showDialog {
                                val gnssTip =
                                    if (i == 0) R.string.gnss_install_setting_gnss_a else R.string.gnss_install_setting_gnss_b
                                PromptPopup(content = stringResource(id = gnssTip), onConfirm = {
                                    DroneModel.activeDrone?.setMasterGPS(i + 1)
                                    context.hideDialog()
                                }, onDismiss = {
                                    context.hideDialog()
                                })
                            }
                        },
                        modifier = Modifier
                            .width(120.dp)
                            .height(30.dp),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        AutoScrollingText(
                            text = gpsType[i],
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    if (i < gpsType.size - 1) {
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                }
            }
            //偏差设置
            TitleRowText(title = stringResource(id = R.string.fc_install_deviation_setting))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = R.drawable.description_gps),
                    contentDescription = "fc_deviation_img",
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .clickable {
                            context.showDialog {
                                ImagePromptPopup(image = R.drawable.description_gps,
                                    imageSize = 280.dp,
                                    showCancel = false,
                                    showConfirm = false,
                                    onConfirm = {},
                                    onDismiss = { context.hideDialog() })
                            }
                        })
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    //偏差X
                    FactoryXYCounterRow(
                        title = R.string.x_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberX,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(59, it.toInt()) },
                        onDismiss = {}
                    )
                    //偏差Y
                    FactoryXYCounterRow(
                        title = R.string.y_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberY,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(60, it.toInt()) },
                        onDismiss = {}
                    )
                }
            }
        }
        VerticalDivider(
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 10.dp),
            color = Color.Gray
        )
        //提示文本
        LazyColumn(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                TipText(text = buildAnnotatedString {
                    append(stringResource(id = R.string.gnss_install_suggest1))
                    append("\n\n")
                    append(stringResource(id = R.string.gnss_install_suggest2))
                })
            }
        }
    }
}

/**
 * RTK安装
 * 安装方向 0-前后 1-左右 2-左下 3-右下
 * X偏差 -125 ~ 125
 * Y偏差 -125 ~ 125
 */
@Composable
private fun RTKInstallation(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    val context = LocalContext.current
    val numberX = aptypeData?.getValue(VKAg.APTYPE_RTK_X) ?: 0f
    val numberY = aptypeData?.getValue(VKAg.APTYPE_RTK_Y) ?: 0f
    val rtkValue = aptypeData?.getIntValue(VKAg.APTYPE_RTK_POS) ?: -1
    val images = listOf(
        R.drawable.install_before,
        R.drawable.install_left,
        R.drawable.install_backward,
        R.drawable.install_forward
    )
    var image by remember {
        mutableIntStateOf(if (rtkValue == -1) R.drawable.description_rtk else images[rtkValue])
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(
                10.dp
            )
        ) {
            //安装方向
            val names = stringArrayResource(id = R.array.rtk_installation_method).toList()
            val values = mutableListOf<Int>()
            for (i in names.indices) {
                values.add(i)
            }
            GroupAskButtonRow(
                title = stringResource(id = R.string.fc_install_direction),
                items = names,
                indexes = values,
                number = rtkValue,
                askPopup = { idx, _, complete ->
                    ImagePromptPopup(image = images[idx],
                        imageSize = 180.dp,
                        content = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append(names[idx])
                            }
                            append("\n")
                            withStyle(SpanStyle(color = Color.Red)) {
                                append(stringResource(id = R.string.click_rtk_direction_tip))
                            }
                        },
                        onConfirm = {
                            DroneModel.activeDrone?.let {
                                sendParameter(
                                    VKAg.APTYPE_RTK_POS, idx.toFloat()
                                )
                                image = images[idx]
                            }
                            complete(true)
                        },
                        onDismiss = {
                            complete(false)
                        })
                }
            )
            //偏差设置
            TitleRowText(title = stringResource(id = R.string.fc_install_deviation_setting))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = image),
                    contentDescription = "fc_deviation_img",
                    modifier = Modifier
                        .size(140.dp)
                        .fillMaxHeight()
                        .clickable {
                            context.showDialog {
                                ImagePromptPopup(image = image,
                                    imageSize = 280.dp,
                                    showCancel = false,
                                    showConfirm = false,
                                    onConfirm = {},
                                    onDismiss = { context.hideDialog() })
                            }
                        })
                Column(
                    modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround
                ) {
                    //偏差X
                    FactoryXYCounterRow(
                        title = R.string.x_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberX,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(VKAg.APTYPE_RTK_X, it.toInt()) },
                        onDismiss = {}
                    )
                    //偏差Y
                    FactoryXYCounterRow(
                        title = R.string.y_deviation,
                        max = 125f,
                        min = -125f,
                        step = 1f,
                        number = numberY,
                        fraction = 0,
                        onConfirm = { sendIndexedParameter(VKAg.APTYPE_RTK_Y, it.toInt()) },
                        onDismiss = {}
                    )
                }
            }
        }
        VerticalDivider(
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 10.dp),
            color = Color.Gray
        )
        //提示文本
        LazyColumn(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                TipText(text = buildAnnotatedString {
                    append(stringResource(id = R.string.rtk_install_suggest1))
                    append("\n\n")
                    append(stringResource(id = R.string.rtk_install_suggest2))
                })
            }
        }
    }
}

/**
 * 雷达安装
 * 前避障雷达灵敏度 1 ~ 100
 * 后避障雷达灵敏度 1 ~ 100
 * 仿地雷达灵敏度 1 ~ 100
 */
@Composable
private fun RadarInstallation(modifier: Modifier = Modifier, aptypeData: VKAg.APTYPEData?) {
    var fRadar = aptypeData?.getValue(57) ?: 0f
    var rRadar = aptypeData?.getValue(58) ?: 0f
    var terrain = aptypeData?.getValue(73) ?: 0f
    val counterWidth = 200.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            FactoryXYCounterRow(
                title = R.string.before_obstacle_avoidance_radar_sensitivity,
                max = 100f,
                min = 1f,
                step = 1f,
                number = fRadar,
                fraction = 0,
                onConfirm = {
                    fRadar = it
                    sendIndexedParameter(57, it.toInt())
                },
                onDismiss = {}
            )
            FactoryXYCounterRow(
                title = R.string.after_obstacle_avoidance_radar_sensitivity,
                max = 100f,
                min = 1f,
                step = 1f,
                number = rRadar,
                fraction = 0,
                onConfirm = {
                    rRadar = it
                    sendIndexedParameter(58, it.toInt())
                },
                onDismiss = {}
            )
            FactoryXYCounterRow(
                title = R.string.ground_defense_radar_sensitivity,
                max = 100f,
                min = 1f,
                step = 1f,
                number = terrain,
                fraction = 0,
                onConfirm = {
                    terrain = it
                    sendIndexedParameter(73, it.toInt())
                },
                onDismiss = {}
            )
        }
        VerticalDivider(
            thickness = 1.dp, modifier = Modifier.fillMaxHeight(), color = Color.Gray
        )
        //提示区域
        LazyColumn(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                TipText(text = buildAnnotatedString {
                    append(stringResource(id = R.string.radar_install_suggest1))
                    append("\n\n")
                    append(stringResource(id = R.string.radar_install_suggest2))
                })
            }
        }
    }
}

/**
 * 电调类型
 */
@Composable
private fun MotorType(aptypeData: VKAg.APTYPEData?) {
    val names = arrayOf("HOBBYWING","SINEMOTION","T-MOTOR").toList()
    val values = listOf(0, 1, 2)
    val value = aptypeData?.getIntValue(VKAg.APTYPE_MOTOR_TYPE) ?: 0
    GroupButtonRow(
        title = stringResource(R.string.motor_type),
        titleTip = "",
        items = names, indexes = values, number = value
    ) {
        sendParameter(VKAg.APTYPE_MOTOR_TYPE, it.toFloat())
    }
}