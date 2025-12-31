package com.jiagu.ags4.scene.work

import android.content.Context
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.WorkBlockInfo
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.components.BreakpointBox
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.DarkAlpha
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.getDefaultButtonColors
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.ags4.vm.task.NaviTask
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.RtkLatLng
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.button.ComboRollButton
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.button.SliderSwitchButton
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.rocker.DiagonalDirectionRocker
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.LeftIconTextField
import com.jiagu.tools.ext.ConverterPair
import com.jiagu.tools.ext.UnitHelper

@Composable
fun RightButtonCommon(
    text: String,
    enabled: Boolean = true,
    buttonWidth: Dp = 100.dp,
    buttonHeight: Dp = 36.dp,
    leftImage: @Composable () -> Unit = {},
    borderColors: ButtonColors = getDefaultButtonColors(),
    onClick: () -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = {
            onClick()
        },
        modifier = Modifier
            .width(buttonWidth)
            .height(buttonHeight),
        colors = borderColors,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leftImage()
            AutoScrollingText(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier,
                color = LocalContentColor.current
            )
        }
    }
}

/**
 * 起飞确认 (滑块+按钮)
 */
@Composable
fun FlightStartButtonRow(onSuccess: () -> Unit) {
    SliderSwitchButton(
        sliderTitle = stringResource(id = R.string.slider_tip),
        buttonName = stringResource(id = R.string.execute_work),
        sliderWidth = 220.dp,
        buttonWidth = 100.dp,
        height = 40.dp,
        onSuccess = { success ->
            if (success) onSuccess()
        })
}

/**
 * 打点器信息
 */
@Composable
fun LocatorInfoBox(modifier: Modifier = Modifier, location: RtkLatLng?) {
    Box(
        modifier = modifier
            .widthIn(max = 340.dp)
            .height(30.dp)
            .background(color = BlackAlpha, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp), contentAlignment = Alignment.Center
    ) {
        AutoScrollingText(
            text = location?.info?.format(LocalContext.current, " | ") ?: "N/A",
            color = Color.White,
            textAlign = TextAlign.Start,
        )
    }
}

/**
 * 返航确认弹窗
 */
@Composable
fun OnFlyBackPopup(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PromptPopup(
        content = stringResource(id = R.string.on_air_back_tip),
        onDismiss = { onDismiss() },
        onConfirm = {
            DroneModel.activeDrone?.apply {
                hover()
                onConfirm()
            }
        })
}

/**
 * 急停按钮
 */
@Composable
fun EmergencyStopButton() {
    val deviceList by DroneModel.deviceList.observeAsState()
    val deviceGroup = filterDeviceByTypes(
        idListData = deviceList, filterNum = listOf(VKAgCmd.DEVINFO_REMOTE_ID)
    )
    val remoteId = deviceGroup[VKAgCmd.DEVINFO_REMOTE_ID]
    val content = LocalContext.current
    if (!remoteId.isNullOrEmpty() || BuildConfig.DEBUG) {
        RightButtonCommon(
            text = "RID", borderColors = ButtonDefaults.buttonColors().copy(
                containerColor = Color.Red, contentColor = Color.White
            )
        ) {
            DroneModel.activeDrone?.apply {
                content.showDialog {
                    EmergencyStopPopup {
                        content.hideDialog()
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
fun EmergencyStopPopupPreview() {
    EmergencyStopPopup({})
}

@Composable
fun EmergencyStopPopup(onDismiss: () -> Unit) {
    val remoteIdData by DroneModel.remoteIdData.observeAsState()
    val red = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
    val green =
        ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.White)
    ScreenPopup(content = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier
                        .size(170.dp, 40.dp)
                        .noEffectClickable { },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (remoteIdData?.armStatus == 1) green else red,
                    onClick = { },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.device_arm_status), color = Color.White
                    )
                }
                Button(
                    modifier = Modifier
                        .size(170.dp, 40.dp)
                        .noEffectClickable { },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (remoteIdData?.ridCommsStatus == 1) green else red,
                    onClick = { },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.device_rid_comms), color = Color.White
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier
                        .size(170.dp, 40.dp)
                        .noEffectClickable { },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (remoteIdData?.gcsGpsStatus == 1) green else red,
                    onClick = { },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.device_gcp_gps), color = Color.White
                    )
                }
                Button(
                    modifier = Modifier
                        .size(170.dp, 40.dp)
                        .noEffectClickable { },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (remoteIdData?.basicIdStatus == 1) green else red,
                    onClick = { },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.device_basic_id), color = Color.White
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    modifier = Modifier
                        .size(170.dp, 40.dp)
                        .noEffectClickable { },
                    shape = MaterialTheme.shapes.medium,
                    colors = if (remoteIdData?.operatorIdStatus == 1) green else red,
                    onClick = { },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.device_operator_id), color = Color.White
                    )
                }
            }

            AutoScrollingText(
                text = remoteIdData?.errorInfo ?: "", color = Color.Red
            )

            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    modifier = Modifier.size(330.dp, 60.dp),
                    colors = red,
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        DroneModel.activeDrone?.apply {
                            emergencyStop()
                        }
                    }) {
                    Text(
                        text = stringResource(id = R.string.device_emergency),
                        color = Color.White,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                }
            }


        }
    }, width = 380.dp, showConfirm = false, onDismiss = { onDismiss() })
}

@Composable
fun RightBox(
    modifier: Modifier = Modifier,
    buttons: @Composable ColumnScope.() -> Unit = {},
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(20.dp),
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(top = STATUS_BAR_HEIGHT + 10.dp, end = 10.dp, bottom = 20.dp),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement
    ) {
        buttons()
    }
}

/**
 * 垂直轨迹
 *
 * @param points 航点数组
 * @param isClosed 是否闭合 true 最后一个点会跟第一个点连起来
 */
@Composable
fun VerticalTrackBox(
    modifier: Modifier = Modifier,
    boundary: List<Point2D>,
    points: List<Point2D> = listOf(),
    isClosed: Boolean = false,
    dronePosition: Point2D? = null,
    breakPoint: Point2D? = null,
) {
    Box(
        modifier = modifier
    ) {
        if (boundary.size > 1) {
            val textMeasurer = rememberTextMeasurer()
            val textStyle = TextStyle(fontSize = 8.sp, color = Color.White)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            ) {
                val maxWidth = size.width
                val maxHeight = size.height
                // 根据地块边界 计算数据的最小值和最大值
                var xMin = boundary.minOf { it.x }
                var xMax = boundary.maxOf { it.x }
                var yMin = boundary.minOf { it.y }
                var yMax = boundary.maxOf { it.y }
                dronePosition?.let {
                    xMin = minOf(xMin, it.x)
                    xMax = maxOf(xMax, it.x)
                    yMin = minOf(yMin, it.y)
                    yMax = maxOf(yMax, it.y)
                }

                // 计算缩放比例
                val scaleX = maxWidth / (xMax - xMin)
                val scaleY = if (yMax - yMin == 0.0) {
                    1.0 // 若为 0，设置默认缩放比例为 1
                } else {
                    maxHeight / (yMax - yMin)
                }
                //取x、y最小比例
                // 将数据点转换为 Canvas 坐标系
                val transformedBoundary = boundary.map {
                    Offset(
                        x = ((it.x - xMin) * scaleX).toFloat(),
                        y = (maxHeight - (it.y - yMin) * scaleY).toFloat(),//反转y轴
                    )
                }
                //地块
                if (transformedBoundary.isNotEmpty()) {
                    // 绘制多边形边界
                    val path = Path().apply {
                        transformedBoundary.forEachIndexed { index, offset ->
                            if (index == 0) moveTo(offset.x, offset.y)
                            else lineTo(offset.x, offset.y)
                        }
                        close() // 闭合路径
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 2f)
                    )
                }
                val transformedDrone = dronePosition?.let {
                    Offset(
                        x = ((it.x - xMin) * scaleX).toFloat(),
                        y = (maxHeight - (it.y - yMin) * scaleY).toFloat(),//反转y轴
                    )
                }
                val transformedBreakPoint = breakPoint?.let {
                    Offset(
                        x = ((it.x - xMin) * scaleX).toFloat(),
                        y = (maxHeight - (it.y - yMin) * scaleY).toFloat(),//反转y轴
                    )
                }
                val transformedPoints = points.map {
                    Offset(
                        x = ((it.x - xMin) * scaleX).toFloat(),
                        y = (maxHeight - (it.y - yMin) * scaleY).toFloat(),//反转y轴
                    )
                }
                //航线
                if (transformedPoints.size > 1) {
                    var startX = transformedPoints[0].x
                    var startY = transformedPoints[0].y
                    for (i in 1..<transformedPoints.size) {
                        val point = transformedPoints[i]
                        val nextX = point.x
                        val nextY = point.y
//                        Log.d("shero", "nextX: $nextX nextY: $nextY")
                        drawLine(
                            color = Color.Red,
                            start = Offset(startX, startY),
                            end = Offset(nextX, nextY),
                            strokeWidth = 3f
                        )
                        if (i < transformedPoints.size - 1) {
                            startX = nextX
                            startY = nextY
                        }
                    }
                    if (isClosed) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(transformedPoints.last().x, transformedPoints.last().y),
                            end = Offset(
                                transformedPoints[0].x, transformedPoints[0].y
                            ),
                            strokeWidth = 3f
                        )
                    }
                    val resultMap = transformedPoints.distinct().associateWith { -1 }.toMutableMap()
                    var index = 0
                    //航点
                    repeat(transformedPoints.size) {
                        val point = transformedPoints[it]
                        if (resultMap.containsKey(point) && resultMap[point] == -1) {
                            index++
                            resultMap[point] = index
                            val text = "$index"
                            val textLayoutResult = textMeasurer.measure(
                                text = text,
                                style = textStyle,
                                constraints = Constraints(
                                    minWidth = 0,
                                    maxWidth = (size.width - (point.x - 10)).toInt()
                                        .coerceAtLeast(0), // 添加宽度约束
                                    minHeight = 0,
                                    maxHeight = size.height.toInt()
                                )
                            )
                            // 使用textLayoutResult来获取文本的宽度和高度
                            val textWidth = textLayoutResult.size.width
                            val textHeight = textLayoutResult.size.height
                            drawText(
                                textMeasurer = textMeasurer,
                                text = text,
                                style = textStyle,
                                topLeft = Offset(
                                    (point.x - 10 - textWidth).coerceIn(
                                        0f,
                                        size.width - textWidth
                                    ), // 限制在画布范围内
                                    (point.y - 12 - textHeight / 2).coerceIn(
                                        0f,
                                        size.height - textHeight
                                    )
                                ),
                            )
                        }
                    }
                }
                dronePosition?.let {
                    drawCircle(
                        color = Color.Green, //航点颜色
                        center = Offset(transformedDrone!!.x, transformedDrone.y), //航点位置
                        radius = 12f //航点大小
                    )
                }
                //断点
                breakPoint?.let {
                    drawCircle(
                        color = Color.Red, //航点颜色
                        center = Offset(transformedBreakPoint!!.x, transformedBreakPoint.y), //航点位置
                        radius = 14f //航点大小
                    )
                    val textLayoutResult = textMeasurer.measure(
                        text = "B",
                        style = textStyle,
                        constraints = Constraints(
                            minWidth = 0,
                            maxWidth = (size.width - (transformedBreakPoint.x - 10)).toInt()
                                .coerceAtLeast(0), // 添加宽度约束
                            minHeight = 0,
                            maxHeight = size.height.toInt()
                        )
                    )
                    //断点文本
                    // 使用textLayoutResult来获取文本的宽度和高度
                    val textWidth = textLayoutResult.size.width
                    val textHeight = textLayoutResult.size.height
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "B",
                        style = textStyle,
                        topLeft = Offset(
                            (transformedBreakPoint.x - 8).coerceIn(
                                0f,
                                size.width - textWidth
                            ),
                            (transformedBreakPoint.y - 16).coerceIn(
                                0f,
                                size.height - textHeight
                            )
                        ),
                    )
                }
            }
        }
    }
}

/**
 * 清洗飞行确认
 */
@Composable
fun CleanFlyConfirmPopup(
    width: Float,
    repeatCount: Int,
    speed: Float,
    confirm: () -> Unit = {}, cancel: () -> Unit = {},
) {
    ScreenPopup(
        width = 360.dp,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.fly_confirm),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = { cancel() },
                            modifier = Modifier
                                .size(30.dp)
                                .align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "close",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val titleWidth = 230.dp
                        val rowHeight = 30.dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.job_line_spacing, UnitHelper.lengthUnit()
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            AutoScrollingText(
                                text = UnitHelper.convertLength(width).toString(1),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(R.string.job_line_repeat_count),
                                style = MaterialTheme.typography.bodyLarge,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            AutoScrollingText(
                                text = repeatCount.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.flight_speed, UnitHelper.lengthUnit()
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            AutoScrollingText(
                                text = speed.toString(1),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                            )

                        }
                        FlightStartButtonRow() {
                            confirm()
                        }
                    }
                }
            }
        },
        showConfirm = false,
        showCancel = false,
    )
}

@Composable
fun BlockCardListCondition(
    text: String,
    type: Int,
    showStatusFilter: Boolean = true,
    onTextChanged: (String) -> Unit,
    onTypeChanged: (Int) -> Unit = {},
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LeftIconTextField(
            text = text,
            onValueChange = onTextChanged,
            leftIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 2.dp)
                        .clickable { onSearch() },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .background(color = Color.White, shape = MaterialTheme.shapes.small),
            showClearIcon = false,
            borderColor = Color.Transparent,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
            })
        )
        if (showStatusFilter) {
            val filterTypeNames = stringArrayResource(id = R.array.filter_type_name).toList()
            ComboRollButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                index = type,
                items = filterTypeNames,
                value = filterTypeNames[type],
                onConfirm = { idx, _ ->
                    onTypeChanged(idx)
                    onSearch()
                })
        }
    }
}

@Composable
fun SimpleBlockCard(blockPlan: BlockPlan, onTap: () -> Unit, onDoubleTap: () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.small)
            .background(
                color = MaterialTheme.colorScheme.onPrimary, shape = MaterialTheme.shapes.small
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    onTap()
                }, onDoubleTap = {
                    onDoubleTap()
                })
            }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AutoScrollingText(
                    text = blockPlan.blockName,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                AutoScrollingText(
                    text = UnitHelper.transAreaWithUnit(
                        context, blockPlan.area
                    ), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun MarkerMovePanel(
    modifier: Modifier = Modifier,
    title: String,
    moveDist: Double,
    showRadius: Boolean = false,
    showMarkerChange: Boolean = false,
    markerIndex: Int,
    radius: Float = 0f,
    onTopClick: () -> Unit,
    onBottomClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onTopLeftClick: () -> Unit,
    onTopRightClick: () -> Unit,
    onBottomLeftClick: () -> Unit,
    onBottomRightClick: () -> Unit,
    onPreviousPoint: () -> Unit = {},
    onNextPoint: () -> Unit = {},
    onRadiusChange: (Float) -> Unit = {},
    onClickReset: () -> Unit,
    onClickDelete: () -> Unit,
    converter: ConverterPair? = null,
) {
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val config = Config(context)
    Column(
        modifier = modifier
            .width(200.dp)
            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
            .clip(shape = MaterialTheme.shapes.medium)
            .padding(top = 6.dp)
            .clickable(false) { },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //title
        Box {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
        }
        //rocker
        DiagonalDirectionRocker(
            modifier = Modifier.size(120.dp),
            centerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = markerIndex.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            onTopClick = onTopClick,
            onBottomClick = onBottomClick,
            onLeftClick = onLeftClick,
            onRightClick = onRightClick,
            onTopLeftClick = onTopLeftClick,
            onTopRightClick = onTopRightClick,
            onBottomLeftClick = onBottomLeftClick,
            onBottomRightClick = onBottomRightClick,
        )
        //move distance
        val convertDist = converter?.first?.invoke(moveDist.toFloat())
            ?: moveDist.toString(1)
        AutoScrollingText(
            text = stringResource(
                R.string.moving_distance, UnitHelper.lengthUnit()
            ) + ":${convertDist}"
        )
        // if 圆 半径
        if (showRadius) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.radius_meter, UnitHelper.lengthUnit()) + ":",
                    style = MaterialTheme.typography.bodyMedium
                )
                FloatCounter(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.extraSmall
                        ),
                    number = radius,
                    max = 50.0f,
                    min = 1.0f,
                    step = 0.5f,
                    fraction = 1,
                    converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                ) {
                    config.poleRadius = it
                    onRadiusChange(it)
                }
            }
        }
        //marker change
        if (showMarkerChange) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .width(80.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    onClick = onPreviousPoint
                ) {
                    Text(
                        text = stringResource(id = R.string.previous),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
                Button(
                    modifier = Modifier
                        .width(80.dp),
                    shape = MaterialTheme.shapes.small,
                    contentPadding = PaddingValues(0.dp),
                    onClick = onNextPoint
                ) {
                    Text(
                        text = stringResource(id = R.string.next),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                    )
                }
            }
        }
        //rest
        //delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .noEffectClickable { onClickReset() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.reset),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            VerticalDivider(
                thickness = 1.dp, color = Color.White, modifier = Modifier.fillMaxHeight()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .noEffectClickable { onClickDelete() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.delete), color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun RouteMovePanel(
    modifier: Modifier = Modifier,
    title: String,
    onTopClick: () -> Unit,
    onBottomClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onTopLeftClick: () -> Unit,
    onTopRightClick: () -> Unit,
    onBottomLeftClick: () -> Unit,
    onBottomRightClick: () -> Unit,
    onClickReset: () -> Unit,
    onClickConfirm: () -> Unit
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
            .clip(shape = MaterialTheme.shapes.medium)
            .padding(top = 6.dp)
            .clickable(false) { },
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //title
        Box {
            AutoScrollingText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
        }
        //rocker
        DiagonalDirectionRocker(
            modifier = Modifier.size(120.dp),
            onTopClick = onTopClick,
            onBottomClick = onBottomClick,
            onLeftClick = onLeftClick,
            onRightClick = onRightClick,
            onTopLeftClick = onTopLeftClick,
            onTopRightClick = onTopRightClick,
            onBottomLeftClick = onBottomLeftClick,
            onBottomRightClick = onBottomRightClick,
        )
        //rest
        //delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .noEffectClickable { onClickReset() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.reset),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            VerticalDivider(
                thickness = 1.dp, color = Color.White, modifier = Modifier.fillMaxHeight()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .noEffectClickable { onClickConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.confirm), color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/**
 * 飞行确认
 */
@Composable
fun BlockFlyConfirmPopup(
    mapVideoModel: MapVideoModel,
    blockParamModel: BlockParamModel,
    routeModel: RouteModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
    confirm: () -> Unit = {},
    cancel: () -> Unit = {},
) {
    val activity = LocalActivity.current as MapVideoActivity
    val engineData by DroneModel.engineData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val config = Config(context)

    ScreenPopup(
        width = 380.dp,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(350.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.fly_confirm),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = { cancel() },
                            modifier = Modifier
                                .width(40.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "close",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val titleWidth = 180.dp
                        val rowHeight = 30.dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .clickable(false) {},
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.flying_hight,
                                    UnitHelper.lengthUnit()
                                ) + ":",
                                style = MaterialTheme.typography.bodyMedium,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            FloatCounter(
                                modifier = Modifier,
                                number = aptypeData?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT)
                                    ?: 3f,
                                min = 3f,
                                max = 30f,
                                step = 1f,
                                fraction = 1,
                                converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
                            ) {
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_TAKEOFF_HEIGHT,
                                    it
                                )
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_GOHOME_HEIGHT,
                                    it
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .clickable(false) {},
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.flying_speed,
                                    UnitHelper.lengthUnit()
                                ) + ":",
                                style = MaterialTheme.typography.bodyMedium,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            FloatCounter(
                                modifier = Modifier,
                                number = aptypeData?.getValue(VKAg.APTYPE_START_END_SPEED)
                                    ?: 1f,
                                min = 1f,
                                max = 10f,
                                step = 1f,
                                fraction = 1,
                                converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
                            ) {
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_START_END_SPEED,
                                    it
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .clickable(false) {},
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.stop_drug_protect),
                                style = MaterialTheme.typography.bodyMedium,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            val names = listOf(
                                stringResource(id = R.string.stop_drug_protect_close),
                                stringResource(id = R.string.stop_drug_protect_hover),
                                stringResource(id = R.string.stop_drug_protect_returning)
                            )

                            val stopDrugProtectValue =
                                (aptypeData?.getValue(VKAg.APTYPE_DRUG_ACT) ?: 1).toInt()
                            GroupButton(
                                modifier = Modifier,
                                items = names,
                                indexes = listOf(1, 2, 3, 4),
                                number = stopDrugProtectValue,
                                textStyle = MaterialTheme.typography.titleSmall
                            ) { idx, _ ->
                                DroneModel.activeDrone?.sendIndexedParameter(
                                    VKAg.APTYPE_DRUG_ACT,
                                    idx
                                )
                            }
                        }
                        //油混发动机 && 发动机未启动
                        if (mapVideoModel.engineType == VKAg.TYPE_ENGINE) {
                            var flightStartEnabled by remember {
                                mutableStateOf(engineData?.unlock_status?.toInt() == 1)
                            }
                            if (!flightStartEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AutoScrollingText(
                                            text = stringResource(id = R.string.engine_flight_start_tip),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    RightButtonCommon(text = stringResource(id = R.string.engine_start)) {
                                        flightStartEnabled = true
                                    }
                                }
                            } else {
                                FlightStartButtonRow {
                                    activity.checkStartAUX2(
                                        routeModel = routeModel,
                                        blockParamModel = blockParamModel,
                                        taskModel = taskModel,
                                        outPathRouteModel = outPathRouteModel
                                    ) {
                                        // TODO set work block
                                        DroneModel.activeDrone?.takeOff2Route()
                                        context.hideDialog()
                                    }
                                    val localBlockId = blockParamModel.selectedBP?.localBlockId ?: 0
                                    val blockId = blockParamModel.selectedBP?.blockId ?: 0
                                    config.workBlockInfo = WorkBlockInfo(
                                        blockId,
                                        localBlockId
                                    )
                                    confirm()
                                }
                            }
                        } else {
                            FlightStartButtonRow() {
                                activity.checkStartAUX2(
                                    routeModel = routeModel,
                                    blockParamModel = blockParamModel,
                                    taskModel = taskModel,
                                    outPathRouteModel = outPathRouteModel
                                ) {
                                    // TODO set work block
                                    DroneModel.activeDrone?.takeOff2Route()
                                    context.hideDialog()
                                }
                                val localBlockId = blockParamModel.selectedBP?.localBlockId ?: 0
                                val blockId = blockParamModel.selectedBP?.blockId ?: 0
                                config.workBlockInfo = WorkBlockInfo(
                                    blockId,
                                    localBlockId
                                )
                                confirm()
                            }
                        }
                    }
                }
            }
        },
        showConfirm = false,
        showCancel = false,
    )
}

/**
 * 飞行确认
 */
@Composable
fun FreeAirRouteFlyConfirmPopup(
    mapVideoModel: MapVideoModel,
    freeAirRouteParamModel: FreeAirRouteParamModel,
    confirm: () -> Unit = {},
    cancel: () -> Unit = {},
) {
    val context = LocalContext.current
    val aptypeData by DroneModel.aptypeData.observeAsState(initial = null)
    val engineData by DroneModel.engineData.observeAsState()
    val config = Config(context)
    val appConfig = AppConfig(context)

    ScreenPopup(
        width = 380.dp,
        content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(350.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.fly_confirm),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(
                            onClick = { cancel() },
                            modifier = Modifier
                                .width(40.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "close",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val titleWidth = 180.dp
                        val rowHeight = 30.dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .clickable(false) {},
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.flying_hight,
                                    UnitHelper.lengthUnit()
                                ) + ":",
                                style = MaterialTheme.typography.bodyMedium,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            FloatCounter(
                                modifier = Modifier,
                                number = aptypeData?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT)
                                    ?: 3f,
                                min = 3f,
                                max = 30f,
                                step = 1f,
                                fraction = 0,
                                converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
                            ) {
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_TAKEOFF_HEIGHT,
                                    it.toFloat()
                                )
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_GOHOME_HEIGHT,
                                    it.toFloat()
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(rowHeight)
                                .clickable(false) {},
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            AutoScrollingText(
                                text = stringResource(
                                    R.string.flying_speed,
                                    UnitHelper.lengthUnit()
                                ) + ":",
                                style = MaterialTheme.typography.bodyMedium,
                                width = titleWidth,
                                modifier = Modifier
                            )
                            FloatCounter(
                                modifier = Modifier,
                                number = aptypeData?.getValue(VKAg.APTYPE_START_END_SPEED) ?: 1f,
                                min = 1f,
                                max = 10f,
                                step = 1f,
                                fraction = 1,
                                converterPair = if (appConfig.lengthUnit == 0) null else UnitHelper.getLengthConverter()
                            ) {
                                DroneModel.activeDrone?.sendParameter(
                                    VKAg.APTYPE_START_END_SPEED,
                                    it
                                )
                            }
                        }
                        //油混发动机 && 发动机未启动
                        if (mapVideoModel.engineType == VKAg.TYPE_ENGINE) {
                            var flightStartEnabled by remember {
                                mutableStateOf(engineData?.unlock_status?.toInt() == 1)
                            }
                            if (!flightStartEnabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        AutoScrollingText(
                                            text = stringResource(id = R.string.engine_flight_start_tip),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    RightButtonCommon(text = stringResource(id = R.string.engine_start)) {
                                        flightStartEnabled = true
                                    }
                                }
                            }
                            if (flightStartEnabled) {
                                FlightStartButtonRow {
                                    DroneModel.activeDrone?.takeOff2Route()
                                    val localBlockId =
                                        freeAirRouteParamModel.selectedBP?.localBlockId ?: 0
                                    val blockId = freeAirRouteParamModel.selectedBP?.blockId ?: 0
                                    config.workBlockInfo = WorkBlockInfo(
                                        blockId,
                                        localBlockId
                                    )
                                    confirm()
                                }
                            }
                        } else {
                            FlightStartButtonRow {
                                DroneModel.activeDrone?.takeOff2Route()
                                val localBlockId =
                                    freeAirRouteParamModel.selectedBP?.localBlockId ?: 0
                                val blockId = freeAirRouteParamModel.selectedBP?.blockId ?: 0
                                config.workBlockInfo = WorkBlockInfo(
                                    blockId,
                                    localBlockId
                                )
                                confirm()
                            }
                        }
                    }
                }
            }
        },
        showConfirm = false,
        showCancel = false,
    )
}

private fun MapVideoActivity.checkStartAUX2(
    routeModel: RouteModel,
    blockParamModel: BlockParamModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
    complete: (Boolean) -> Unit,
) {
    findStartAuxPath(
        routeModel = routeModel,
        blockParamModel = blockParamModel,
        taskModel = taskModel,
        outPathRouteModel = outPathRouteModel
    ) {
        if (blockParamModel.calcAuxPoints.isNotEmpty()) {
            val path = mutableListOf<RoutePoint>()
            for (a in blockParamModel.calcAuxPoints) {
                path.add(RoutePoint(a.latitude, a.longitude, false, 0))
            }
            uploadAux(
                context = this,
                type = UploadNaviData.TYPE_AUX_S,
                route = path,
                blockParamModel = blockParamModel
            ) { success, _ ->
                complete(success)
                false
            }
        } else {
            if (blockParamModel.auxPoints.isEmpty()) {
                complete(true)
                return@findStartAuxPath
            }
            val path = mutableListOf<RoutePoint>()
            for (s in blockParamModel.auxPoints) {
                path.add(RoutePoint(s.latitude, s.longitude, false, 0))
            }
            uploadAux(
                context = this,
                type = UploadNaviData.TYPE_AUX_S,
                route = path,
                blockParamModel = blockParamModel
            ) { success, _ ->
                complete(success)
                false
            }
        }
    }
}

fun uploadAux(
    context: Context,
    type: Int,
    route: List<RoutePoint>,
    blockParamModel: BlockParamModel,
    complete: ((Boolean, String?) -> Boolean)? = null,
) {
    DroneModel.activeDrone?.let {
        blockParamModel.workPlan?.let { plan ->
            val speed =
                DroneModel.aptypeData.value?.getValue(VKAg.APTYPE_START_END_SPEED) ?: plan.speed
            val height = DroneModel.aptypeData.value?.getValue(VKAg.APTYPE_TAKEOFF_HEIGHT)
                ?: (if (plan.height == 0f) 3f else plan.height)
            val task = NaviTask(it)
            var home: GeoHelper.LatLngAlt? = null
            it.homeData.value?.apply { home = GeoHelper.LatLngAlt(lat, lng, alt) }
            val d = UploadNaviData(
                height,
                speed,
                plan.width,
                plan.drugQuantity,
                plan.drugFix.toFloat(),
                0,
                plan.routeMode
            )
            d.route = route
            d.type = type
            task.setParam(home, d)
            context.startProgress(task) { success, msg ->
                complete?.invoke(success, msg)
                false
            }
        }
    }
}


@Composable
fun ABRightBreakPoint(
    modifier: Modifier,
    defaultNumber: Int,
    breakpointSize: Int,
    onSelect: (Int) -> Unit = {},
) {
    val breakpointList = when (breakpointSize) {
        1 -> listOf(stringResource(R.string.break_break))
        2 -> listOf(
            stringResource(R.string.break_break),
            stringResource(R.string.break_return1)
        )

        3 -> listOf(
            stringResource(R.string.break_break),
            stringResource(R.string.break_return1),
            stringResource(R.string.break_return2)
        )

        else -> emptyList()
    }
    if (breakpointList.isNotEmpty()) {
        BreakpointBox(
            modifier = modifier
                .width(120.dp)
                .background(color = DarkAlpha, shape = MaterialTheme.shapes.extraSmall),
            title = stringResource(id = R.string.break_box_title),
            breakpointList = breakpointList,
            defaultClickNumber = defaultNumber
        ) { onSelect(it) }
    }
}