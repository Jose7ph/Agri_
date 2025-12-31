package com.jiagu.ags4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper

/**
 * 大田架次落地弹窗
 *
 * @param flightInformation
 * @param onConfirm
 * @receiver
 */
@Composable
fun FlightInformationPopup(
    flightInformation: FlightInformation,
    onConfirm: () -> Unit = {},
) {
    val context = LocalContext.current
    val cardModifier = Modifier
        .clip(shape = MaterialTheme.shapes.small)
    ScreenPopup(
        onConfirm = onConfirm,
        width = 400.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //弹窗标题
                Box(Modifier.fillMaxWidth()) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.flight_information),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(top = 5.dp, bottom = 5.dp)
                    ) {
                        //作业时长
                        Box(modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 5.dp)) {
                            val workDurationText =
                                stringResource(id = R.string.flight_information_current_work_duration) + " ${flightInformation.currentWorkDuration}"
                            AutoScrollingText(
                                text = workDurationText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        //本次作业面积
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    id = R.string.flight_information_work_area,
                                    UnitHelper.areaUnit(context)
                                ),
                                content = flightInformation.currentWorkArea
                            )
                            //本次用药量
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    R.string.flight_information_current_dosage,
                                    if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                                ),
                                content = flightInformation.currentDosage
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //已作业面积
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                id = R.string.flight_information_worked,
                                UnitHelper.areaUnit(context)
                            ),
                            content = flightInformation.completedWorkedArea
                        )
                        //已用药量
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                R.string.flight_information_completed_dosage,
                                if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                            ),
                            content = flightInformation.completedDosage
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //还需作业面积
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                id = R.string.flight_information_need_work_area,
                                UnitHelper.areaUnit(context)
                            ),
                            content = flightInformation.needWorkArea
                        )
                        //还需用药量（计算不出来暂时无用）
                        Spacer(modifier = Modifier.weight(1f))
//                            FlightInformationCard(
//                                modifier = cardModifier.weight(1f),
//                                title = stringResource(
//                                    id = R.string.flight_information_need_dosage,
//                                    if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
//                                ),
//                                content = flightInformation.needDosage
//                            )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //航线面积
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                id = R.string.flight_information_air_route_area,
                                UnitHelper.areaUnit(context)
                            ),
                            content = flightInformation.airRouteArea
                        )
                        //地块面积
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                id = R.string.flight_information_land_block,
                                UnitHelper.areaUnit(context)
                            ),
                            content = flightInformation.landArea
                        )
                    }
                }
            }
        },
        showCancel = false
    )
}

/**
 * 清洗架次落地弹窗
 *
 * @param flightInformation
 * @param onConfirm
 * @receiver
 */
@Composable
fun CleanFlightInformationPopup(
    flightInformation: FlightInformation,
    onConfirm: () -> Unit = {},
) {
    val context = LocalContext.current
    val cardModifier = Modifier
        .clip(shape = MaterialTheme.shapes.small)
    ScreenPopup(
        onConfirm = onConfirm,
        width = 400.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //弹窗标题
                Box(Modifier.fillMaxWidth()) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.flight_information),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(top = 5.dp, bottom = 5.dp)
                    ) {
                        //作业时长
                        Box(modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 5.dp)) {
                            val workDurationText =
                                stringResource(id = R.string.flight_information_current_work_duration) + " ${flightInformation.currentWorkDuration}"
                            AutoScrollingText(
                                text = workDurationText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        //本次作业面积
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    id = R.string.flight_information_work_area,
                                    UnitHelper.areaUnit(context)
                                ),
                                content = flightInformation.currentWorkArea
                            )
                            //本次用药量
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    R.string.flight_information_current_dosage,
                                    if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                                ),
                                content = flightInformation.currentDosage
                            )
                        }
                    }
                }
            }
        }, showCancel = false
    )
}

/**
 * 自由航线/果树模式落地弹窗
 *
 * @param flightInformation
 * @param onConfirm
 * @receiver
 */
@Composable
fun FreeAirRouteFlightInformationPopup(
    flightInformation: FlightInformation,
    onConfirm: () -> Unit = {},
) {
    val context = LocalContext.current
    val cardModifier = Modifier
        .clip(shape = MaterialTheme.shapes.small)
    ScreenPopup(
        onConfirm = onConfirm,
        width = 400.dp,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //弹窗标题
                Box(Modifier.fillMaxWidth()) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.flight_information),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(top = 5.dp, bottom = 5.dp)
                    ) {
                        //作业时长
                        Box(modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 5.dp)) {
                            val workDurationText =
                                stringResource(id = R.string.flight_information_current_work_duration) + " ${flightInformation.currentWorkDuration}"
                            AutoScrollingText(
                                text = workDurationText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        //本次作业面积
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    id = R.string.flight_information_work_area,
                                    UnitHelper.areaUnit(context)
                                ),
                                content = flightInformation.currentWorkArea
                            )
                            //本次用药量
                            FlightInformationCard(
                                modifier = cardModifier.weight(1f),
                                title = stringResource(
                                    R.string.flight_information_current_dosage,
                                    if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                                ),
                                content = flightInformation.currentDosage
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        //已作业面积
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                id = R.string.flight_information_worked,
                                UnitHelper.areaUnit(context)
                            ),
                            content = flightInformation.completedWorkedArea
                        )
                        //已用药量
                        FlightInformationCard(
                            modifier = cardModifier.weight(1f),
                            title = stringResource(
                                R.string.flight_information_completed_dosage,
                                if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                            ),
                            content = flightInformation.completedDosage
                        )
                    }
                }
            }
        },
        showCancel = false
    )
}

class FlightInformation(
    val currentWorkDuration: String, //本次作业时长
    val currentWorkArea: String,//本次作业面积
    val currentDosage: String,//本次用药量
    val completedWorkedArea: String, //已作业面积
    val completedDosage: String, //已用药量
    val needWorkArea: String,//还需作业面积
    val needDosage: String,//还需用药量
    val landArea: String,//地块面积
    val airRouteArea: String,//航线面积
)

@Composable
fun LiftingInformationPopup(
    liftingInformation: LiftingInformation,
    onConfirm: () -> Unit = {},
) {
    val titleStyle = MaterialTheme.typography.titleMedium
    val cardModifier = Modifier
        .clip(shape = MaterialTheme.shapes.small)
    ScreenPopup(
        onConfirm = onConfirm,
        width = 400.dp, content = {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //弹窗标题
                Box(Modifier.fillMaxWidth()) {
                    AutoScrollingText(
                        text = stringResource(id = R.string.flight_information),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        style = titleStyle
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FlightInformationCard(
                        modifier = cardModifier.weight(1f),
                        title = stringResource(id = R.string.linear_flight) + stringResource(id = R.string.distance) + "(${UnitHelper.lengthUnit()})",
                        content = liftingInformation.distance + "/" + liftingInformation.flyDistance
                    )
                    FlightInformationCard(
                        modifier = cardModifier.weight(1f),
                        title = stringResource(R.string.time) + "(min)",
                        content = liftingInformation.time
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FlightInformationCard(
                        modifier = cardModifier.weight(1f),
                        title = stringResource(id = R.string.radar_height) + "(${UnitHelper.lengthUnit()})",
                        content = liftingInformation.height
                    )
                    FlightInformationCard(
                        modifier = cardModifier.weight(1f),
                        title = stringResource(id = R.string.avg_max_speed) + "(${UnitHelper.lengthUnit()}/s)",
                        content = "${liftingInformation.avgSpeed}/${liftingInformation.maxSpeed}"
                    )
                }
            }
        }, showCancel = false
    )
}

class LiftingInformation(
    val distance: String, //距离
    val flyDistance: String, //飞行距离
    val time: String,//时间
    val height: String,//高度
    val avgSpeed: String,//平均速度
    val maxSpeed: String,//最大速度
)

/**
 * 飞行信息card
 */
@Composable
private fun FlightInformationCard(
    modifier: Modifier = Modifier, title: String, content: String,
) {
    Column(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AutoScrollingText(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            AutoScrollingText(
                text = content,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}