package com.jiagu.ags4.ui.components

import SignalIndicator
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.work.LocalMapVideoModel
import com.jiagu.ags4.scene.work.MapVideoModel
import com.jiagu.ags4.scene.work.WorkModeEnum
import com.jiagu.ags4.scene.work.settings.RealTimeParam
import com.jiagu.ags4.ui.shapes.TrapezoidShape
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.ui.theme.VeryDarkAlpha
import com.jiagu.ags4.ui.theme.buttonDisabled
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.utils.goto
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.formatSecond
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.text.AutoScrollingText
import kotlin.math.abs


enum class LoadType {
    None, SPRAY, SEED,
}

enum class DetailType(val i: Int) {
    PEER_S1(101),
    GNSS(102),
    RTK(103),
}

data class StatusInfo(
    var flyMode: String = "-",
    var flyTime: String = "00:00",
    var battery1: String = "0",
    var battery2: String? = null,
    var signal: Int = -1,
    var isGNSS: Boolean = false,
    var gnssAStarCount: String = "N/A",
    var gnssALevel: Int = 0, // 0 1 2 3
    var gnssBStarCount: String? = null,
    var gnssBLevel: Int = 0, // 0 1 2 3
    var isRTK: Boolean = false,
    var rtkRH: String = "",
    var rtkStarCount: String = "N/A",
    var rtkLevel: Int = 0, // 0 1 2 3
    var capacity1: Int = 100,
    var capacity2: Int? = null,
    var energy: Float = 50f,
    var energyType: Int = 0,
    var lijiajuli: Int = 0,
)

private val statusBarIconSize = 16.dp

private fun buildStatusInfo(
    context: Context,
    imuData: VKAg.IMUData?,
    rssiData: Int?,
    newDevInfoData: Map<String, VKAg.DevInfoData>?,
    deviceRTKData: VKAg.RTKInfo?,
    batteryData: VKAg.BatteryGroup?,
    hydrogenBatteryData1: VKAg.HydrogenBatteryData?,
    hydrogenBatteryData2: VKAg.HydrogenBatteryData?,
): StatusInfo {
    val statusInfo = StatusInfo()
    imuData?.let {
        it.flyMode.let { flyMode ->
            statusInfo.flyMode = VKAgTool.modeToString(context, flyMode.toInt())
        }
        it.flyTime.let { flyTime ->
            statusInfo.flyTime = formatSecond(flyTime.toInt())
        }
        statusInfo.energyType = it.energyType
        when (it.energyType) {
            VKAg.TYPE_BATTERY -> {
                statusInfo.energy = it.energy
                statusInfo.battery1 = context.getString(R.string.volt, it.energy.toString(1))
            }

            VKAg.TYPE_SMART_BATTERY -> {
                if (batteryData != null && batteryData.batteries?.size!! > 1) {
                    //电池1
                    statusInfo.capacity1 = batteryData.batteries[0].percent
                    val capText1 = when (batteryData.batteries[0].percent) {
                        100 -> "99"
                        in 0..9 -> "0${batteryData.batteries[0].percent}"
                        else -> "${batteryData.batteries[0].percent}"
                    }
                    statusInfo.battery1 = capText1
                    //电池2
                    statusInfo.capacity2 = batteryData.batteries[1].percent
                    val capText2 = when (batteryData.batteries[1].percent) {
                        100 -> "99"
                        in 0..9 -> "0${batteryData.batteries[1].percent}"
                        else -> "${batteryData.batteries[1].percent}"
                    }
                    statusInfo.battery2 = capText2
                } else {
                    statusInfo.capacity1 = it.capacity
                    val capText = when (it.capacity) {
                        100 -> "99"
                        in 0..9 -> "0${it.capacity}"
                        else -> "${it.capacity}"
                    }
                    statusInfo.battery1 = capText
                }
            }

            VKAg.TYPE_ENGINE -> {
                statusInfo.energy = it.energy
                statusInfo.battery1 = context.getString(R.string.volt, it.energy.toString(1))
            }

            VKAg.TYPE_HYDROGEN_BATTERY -> {
                statusInfo.energy = it.energy
                hydrogenBatteryData1?.let {
                    statusInfo.battery1 = it.batteryVoltage.toString(1)
                }
                hydrogenBatteryData2?.let {
                    statusInfo.battery2 = it.batteryVoltage.toString(1)
                }
            }
        }
        statusInfo.lijiajuli = it.LiJiaJuLi
    }

    rssiData?.let {
        statusInfo.signal = it
    }
    //RTK
    if (deviceRTKData != null) {
        statusInfo.isRTK = true
        statusInfo.rtkRH =
            if (deviceRTKData.location_type.toInt() == 3 && deviceRTKData.direction_type.toInt() == 2) {
                statusInfo.rtkLevel = 3
                "RH"
            } else if (deviceRTKData.location_type.toInt() == 3 && deviceRTKData.direction_type.toInt() != 2) {
                statusInfo.rtkLevel = 2
                "R"
            } else if (deviceRTKData.location_type.toInt() != 3 && deviceRTKData.direction_type.toInt() == 2) {
                statusInfo.rtkLevel = 1
                "H"
            } else {
                statusInfo.rtkLevel = 0
                ""
            }
        statusInfo.rtkStarCount =
            deviceRTKData.satellite_ant1.toString() + "\n" + deviceRTKData.satellite_ant2.toString()
    } else {
        statusInfo.isRTK = false
        statusInfo.rtkStarCount = "N/A"
        statusInfo.rtkLevel = 0
    }
    //GNSS
    newDevInfoData?.let {
        val devInfos = it.values
        var gnssA: VKAg.GNSSInfo? = null
        var gnssB: VKAg.GNSSInfo? = null
        devInfos.forEach { devInfo ->
            if (devInfo.devType == VKAgCmd.DEVINFO_GNSS) {
                when (devInfo.devNum.toInt()) {
                    1 -> gnssA = devInfo.info as VKAg.GNSSInfo
                    2 -> gnssB = devInfo.info as VKAg.GNSSInfo
                }
            }
        }
        if (gnssA != null || gnssB != null) {
            statusInfo.isGNSS = true
            gnssA?.let { ga ->
                statusInfo.gnssAStarCount = ga.satellite.toString()
                statusInfo.gnssALevel = if (ga.status.toInt() == 2)  0 else ga.type.toInt()
            }
            gnssB?.let { gb ->
                statusInfo.gnssBStarCount = gb.satellite.toString()
                statusInfo.gnssBLevel =  if (gb.status.toInt() == 2)  0 else gb.type.toInt()
            }
        } else {
            statusInfo.isGNSS = false
            statusInfo.gnssAStarCount = "N/A"
            statusInfo.gnssBStarCount = "N/A"
            statusInfo.gnssALevel = 0
            statusInfo.gnssBLevel = 0
        }
    }
    return statusInfo
}

@Composable
fun StatusBar(onClickBack: () -> Unit, onClickSetting: () -> Unit) {
    val context = LocalContext.current
    val imuData by DroneModel.imuData.observeAsState()
    val rssiData by DroneModel.rssiData.observeAsState()
    val deviceRTKData by DroneModel.deviceRTKData.observeAsState()
    val batteryData by DroneModel.batteryData.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val newDevInfoData by DroneModel.newDevInfoData.observeAsState()

    val statusInfo = buildStatusInfo(
        context = context,
        imuData = imuData,
        rssiData = rssiData,
        newDevInfoData = newDevInfoData,
        deviceRTKData = deviceRTKData,
        batteryData = batteryData,
        hydrogenBatteryData1 = hydrogenBatteryData1,
        hydrogenBatteryData2 = hydrogenBatteryData2
    )
    Row(
        Modifier
            .fillMaxWidth()
            .height(STATUS_BAR_HEIGHT)
            .background(Color.Black)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton {
            onClickBack()
        }
        FlyModeComponent(
            flyMode = statusInfo.flyMode, modifier = Modifier.weight(0.6f)

        )
        RightComponents(
            statusInfo = statusInfo
        )
        //作业类型 航线/AB/手动/吊运...
        WorkMode(aptypeData = aptypeData)
        //settings
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(20.dp)
                .clickable {
                    onClickSetting()
                },
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .size(24.dp)
            .padding(vertical = 8.dp, horizontal = 2.dp)
            .aspectRatio(1f),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            tint = Color.White
        )

    }
}

@Composable
fun FlyModeComponent(flyMode: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary, BlackAlpha
                    )
                ), shape = TrapezoidShape(16f, 16f)
            )
            .padding(start = 10.dp, end = 2.dp)
            .clickable {
                context.showDialog {
                    RealTimeParam {
                        context.hideDialog()
                    }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AutoScrollingText(
            modifier = Modifier.fillMaxWidth(),
            text = flyMode,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun RightComponents(
    statusInfo: StatusInfo,
) {
    val mapVideoModel = LocalMapVideoModel.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconText(
            text = statusInfo.flyTime, icon = R.drawable.default_time
        )
        IconSignal(signal = statusInfo.signal)
        IconText(
            text = statusInfo.lijiajuli.toString(),
            icon = R.drawable.default_distance,
        )
        var batteryTint = MaterialTheme.colorScheme.primary
        when (statusInfo.energyType) {
            VKAg.TYPE_BATTERY -> batteryTint = getBatteryVoltTextColor(volt = statusInfo.energy)
            VKAg.TYPE_SMART_BATTERY -> {
                var signal = statusInfo.capacity1
                if (statusInfo.capacity2 != null) {
                    signal = (statusInfo.capacity1 + statusInfo.capacity2!!) / 2
                }
                batteryTint = getBatteryColor(signal = signal)
            }

            VKAg.TYPE_ENGINE -> batteryTint = getBatteryVoltTextColor(volt = statusInfo.energy)
        }
        IconText(
            modifier = Modifier.clickable {
                if (mapVideoModel.showDetailsType != VKAg.INFO_BATTERY) {
                    mapVideoModel.showDetailsType = VKAg.INFO_BATTERY
                } else {
                    mapVideoModel.showDetailsType = VKAg.INFO_IMU
                }
                //若左侧有内容显示 则隐藏
                if (mapVideoModel.showParam) {
                    mapVideoModel.showParam = false
                }
            },
            text = statusInfo.battery1 + if (statusInfo.battery2 != null) "|${statusInfo.battery2}" else "",
            tint = batteryTint,
            icon = R.drawable.default_battery_status,
        )

        //GPS
        if (statusInfo.isGNSS) {
            var gpsTint1 = MaterialTheme.colorScheme.primary
            var gpsTint2 = MaterialTheme.colorScheme.primary
            when (statusInfo.gnssALevel) {
                0 -> gpsTint1 = Color.White
                1 -> gpsTint1 = MaterialTheme.colorScheme.error
                2 -> gpsTint1 = MaterialTheme.colorScheme.tertiary
                3 -> gpsTint1 = MaterialTheme.colorScheme.primary
            }
            when (statusInfo.gnssBLevel) {
                0 -> gpsTint2 = Color.White
                1 -> gpsTint2 = MaterialTheme.colorScheme.error
                2 -> gpsTint2 = MaterialTheme.colorScheme.tertiary
                3 -> gpsTint2 = MaterialTheme.colorScheme.primary
            }
            val gnssStarCount =
                if (statusInfo.gnssAStarCount.isNotEmpty() && !statusInfo.gnssBStarCount.isNullOrEmpty()) {
                    "${statusInfo.gnssAStarCount}\n${statusInfo.gnssBStarCount}"
                } else {
                    listOfNotNull(statusInfo.gnssAStarCount, statusInfo.gnssBStarCount)
                        .filterNot { it.isEmpty() }
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("\n")
                        ?: "N/A"
                }
            IconText(
                modifier = Modifier.widthIn(min = 30.dp).clickable {
                    if (mapVideoModel.showDetailsType != DetailType.GNSS.i) {
                        mapVideoModel.showDetailsType = DetailType.GNSS.i
                    } else {
                        mapVideoModel.showDetailsType = VKAg.INFO_IMU
                    }
                    //若左侧有内容显示 则隐藏
                    if (mapVideoModel.showParam) {
                        mapVideoModel.showParam = false
                    }
                },
                text = gnssStarCount,
                icon = R.drawable.default_gps,
                tint = gpsTint1,
                tint2 = gpsTint2
            )
        }
        //RTK
        if (statusInfo.isRTK) {
            var rtkTint = MaterialTheme.colorScheme.primary
            when (statusInfo.rtkLevel) {
                0 -> rtkTint = Color.White
                1 -> rtkTint = MaterialTheme.colorScheme.error
                2 -> rtkTint = MaterialTheme.colorScheme.tertiary
                3 -> rtkTint = MaterialTheme.colorScheme.primary
            }
            IconText(
                modifier = Modifier
                    .widthIn(min = 46.dp)
                    .clickable {
                    if (mapVideoModel.showDetailsType != DetailType.RTK.i) {
                        mapVideoModel.showDetailsType = DetailType.RTK.i
                    } else {
                        mapVideoModel.showDetailsType = VKAg.INFO_IMU
                    }
                    //若左侧有内容显示 则隐藏
                    if (mapVideoModel.showParam) {
                        mapVideoModel.showParam = false
                    }
                },
                text = statusInfo.rtkRH,
                text2 = statusInfo.rtkStarCount,
                icon = R.drawable.default_rtk,
                tint = rtkTint,
                tint2 = rtkTint
            )
        }

        NozzleButton()
    }
}

@Composable
private fun WorkMode(aptypeData: VKAg.APTYPEData?) {
    val context = LocalContext.current
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxHeight()
            .background(
                color = if (mapVideoModel.workModeEnabled) MaterialTheme.colorScheme.onPrimary else buttonDisabled,
                shape = MaterialTheme.shapes.extraSmall
            )
            .clickable(mapVideoModel.workModeEnabled && AgsUser.userInfo != null) {
                //获取作业机用
                DroneModel.activeDrone?.getParameters()
                //清除droneModel中的blockPlan,防止切换模式后还保留上次的blockPlan
                DroneModel.blockPlan.value = null
                context.showDialog {
                    WorkModeChoosePopup(
                        mapVideoModel = mapVideoModel,
                        aptypeData = aptypeData,
                        onClick = { mode ->
                            //作业模式变了切换页面，否则只隐藏模式选择页面
                            if (mode != mapVideoModel.workModeEnum) {
                                navController.goto(
                                    mode.url, mapVideoModel.workModeEnum.url
                                )
                                mapVideoModel.workModeEnum = mode
                                mapVideoModel.hideInfoPanel()
                            }
                            context.hideDialog()
                        },
                        onClose = {
                            context.hideDialog()
                        })
                }

            }, contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            //图片和名称索引从0开始，作业方式返回的数据从1开始
            Image(
                painter = painterResource(id = mapVideoModel.workModeEnum.image),
                contentDescription = "work mode image",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(24.dp)
            )
            AutoScrollingText(
                text = stringResource(id = mapVideoModel.workModeEnum.modeName),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun NozzleButton() {
    val loadType = when (DroneModel.currentWorkType.second) {
        VKAg.LOAD_TYPE_SEED -> LoadType.SEED
        VKAg.LOAD_TYPE_SPRAY -> LoadType.SPRAY
        else -> LoadType.None
    }
    Box(
        modifier = Modifier
            .size(24.dp)
            .padding(1.dp)
            .clip(MaterialTheme.shapes.extraSmall),
        contentAlignment = Alignment.Center
    ) {
        when (loadType) {
            LoadType.None -> Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )

            LoadType.SPRAY -> Icon(
                painter = painterResource(id = R.drawable.default_device_sprayer),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )

            LoadType.SEED -> Icon(
                painter = painterResource(id = R.drawable.default_device_seeder),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun IconText(
    modifier: Modifier = Modifier,
    text: String,
    text2: String? = null,
    icon: Int,
    space: Dp = 2.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    tint2: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(space)
    ) {
        Icon(
            modifier = Modifier
                .size(statusBarIconSize)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color.White
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp
            ),
            color = tint
        )
        if (text2 != null) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = text2,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp
                ),
                color = tint2
            )
        }
    }
}

@Composable
private fun IconSignal(signal: Int) {
    val mapVideoModel = LocalMapVideoModel.current
    val signalColor = when (signal) {
        -1, 0 -> Color.LightGray
        in 1..29 -> MaterialTheme.colorScheme.error
        in 30..59 -> MaterialTheme.colorScheme.tertiary
        in 60..200 -> MaterialTheme.colorScheme.primary
        else -> Color.LightGray
    }
    val signalNum = when (signal) {
        in 1..19 -> 1
        in 20..39 -> 2
        in 40..59 -> 3
        in 60..79 -> 4
        in 80..200 -> 5
        else -> 0
    }
    Row(modifier = Modifier.clickable {
        if (mapVideoModel.showDetailsType != DetailType.PEER_S1.i) {
            mapVideoModel.showDetailsType = DetailType.PEER_S1.i
        } else {
            mapVideoModel.showDetailsType = VKAg.INFO_IMU
        }
        //若左侧有内容显示 则隐藏
        if (mapVideoModel.showParam) {
            mapVideoModel.showParam = false
        }
    }, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier
                .size(statusBarIconSize)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.default_remote_control),
            contentDescription = null,
            tint = Color.White
        )
        SignalIndicator(
            modifier = Modifier
                .height(14.dp)
                .align(Alignment.Bottom)
                .padding(start = 2.dp),
            signal = signalNum,
            barCount = 5,
            activeColor = signalColor
        )

    }
}


@Composable
fun getBatteryColor(signal: Int): Color {
    DroneModel.aptypeData.value?.let {
        val level1 = it.getValue(VKAg.APTYPE_PROTECT_CAP1)
        val level2 = it.getValue(VKAg.APTYPE_PROTECT_CAP2)
        return if (signal == -1) {
            Color.White
        } else if (signal <= level2) {
            MaterialTheme.colorScheme.error
        } else if (signal > level1) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        }
    }
    return Color.White
}

@Composable
fun getBatteryVoltTextColor(volt: Float): Color {
    DroneModel.aptypeData.value?.let {
        val volt1 = it.getValue(VKAg.APTYPE_PROTECT_LV1)
        val volt2 = it.getValue(VKAg.APTYPE_PROTECT_LV2)
        return if (volt <= 0f) {
            Color.White
        } else if (volt <= volt2) {
            MaterialTheme.colorScheme.error
        } else if (volt > volt1) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.tertiary
        }
    }
    return Color.White
}

/**
 * 电池详细数据
 */
@Composable
fun BatteryStatusDetails() {
    val batteryData by DroneModel.batteryData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val engineData by DroneModel.engineData.observeAsState()
    val hydrogenBatteryData1 by DroneModel.hydrogenBatteryData1.observeAsState()
    val hydrogenBatteryData2 by DroneModel.hydrogenBatteryData2.observeAsState()
    val deviceList by DroneModel.deviceList.observeAsState()
    val batteryGroup = filterDeviceByTypes(
        idListData = deviceList, filterNum = listOf(VKAgCmd.DEVINFO_HYDROGEN_BATTERY)
    )
    var hydrogenBatteryId1 = ""
    var hydrogenBatteryId2 = ""
    if (batteryGroup.containsKey(VKAgCmd.DEVINFO_HYDROGEN_BATTERY)) {
        val hydrogenBatterys = batteryGroup[VKAgCmd.DEVINFO_HYDROGEN_BATTERY]
        hydrogenBatterys?.let {
            if (it.isNotEmpty()) {
                hydrogenBatteryId1 = it[0].swId
                if (it.size > 1) {
                    hydrogenBatteryId2 = it[1].swId
                }
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = STATUS_BAR_HEIGHT, end = 200.dp),
        horizontalArrangement = Arrangement.End
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .heightIn(max = 200.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //油电发动机
            if (imuData?.energyType == VKAg.TYPE_ENGINE) {
                item {
                    Column(
                        modifier = Modifier
                            .width(120.dp)
                            .padding(2.dp),
                    ) {
                        //转速
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_speed) + ":",
                            data = engineData?.speed.toString()
                        )
                        //油门
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_throttle) + ":",
                            data = engineData?.throttle.toString()
                        )
                        //气缸1温度
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_cylinder_1_temperature) + ":",
                            data = engineData?.temp1.toString()
                        )
                        //气缸2温度
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_cylinder_2_temperature) + ":",
                            data = engineData?.temp2.toString()
                        )
                        //温度
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_temperature) + ":",
                            data = engineData?.tempPCB.toString()
                        )
                        //油量
                        DetailRowCommon(
                            title = stringResource(id = R.string.fuel_capacity) + ":",
                            data = "${engineData?.fuel ?: 0}%"
                        )
                        //电压
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_voltage) + ":",
                            data = "${engineData?.voltage?.toString(1)}"
                        )
                        //电流
                        DetailRowCommon(
                            title = stringResource(id = R.string.device_engine_current) + ":",
                            data = "${engineData?.currents?.toString(1)}"
                        )
                    }
                }
            }
            //智能电池
            if (imuData?.energyType == VKAg.TYPE_SMART_BATTERY) {
                items(batteryData?.batteries?.size ?: 0) {
                    val battery = batteryData?.batteries?.get(it)
                    Column(
                        modifier = Modifier
                            .width(120.dp)
                            .padding(2.dp),
                    ) {
                        //电池id
                        DetailRowCommon(
                            title = stringResource(id = R.string.battery_id) + ":",
                            data = battery?.batId ?: ""
                        )
                        //电压
                        DetailRowCommon(
                            title = stringResource(id = R.string.battery_voltage) + ":",
                            data = battery?.voltage?.toString(1) ?: ""
                        )
                        //电量
                        DetailRowCommon(
                            title = stringResource(id = R.string.battery_capacity) + "(%):",
                            data = (battery?.percent ?: "").toString()
                        )
                        //电流
                        DetailRowCommon(
                            title = stringResource(id = R.string.battery_current) + ":",
                            data = battery?.current?.toString(1) ?: "",
                        )
                        //温度
                        DetailRowCommon(
                            title = stringResource(id = R.string.battery_temperature) + ":",
                            data = battery?.temperature?.toString(1) ?: "",
                        )
                    }
                }
            }
            //氢电池
            if (imuData?.energyType == VKAg.TYPE_HYDROGEN_BATTERY) {
                item {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hydrogenBatteryData1?.let {
                            Column(
                                modifier = Modifier.width(140.dp),
                            ) {
                                //id
                                DetailRowCommon(
                                    title = "ID:", data = hydrogenBatteryId1
                                )
                                // 电池电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.battery_voltage1) + "(V):",
                                    data = it.batteryVoltage.toString(1)
                                )
                                // 电堆电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.stack_voltage) + "(V):",
                                    data = it.stackVoltage.toString(1)
                                )
                                // 电机电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.motor_voltage) + "(V):",
                                    data = it.motorVoltage.toString()
                                )
                                // 电机电流
                                DetailRowCommon(
                                    title = stringResource(id = R.string.motor_current) + "(A):",
                                    data = abs(it.motorCurrent).toString(1)
                                )
                                //电池补能电流
                                DetailRowCommon(
                                    title = stringResource(id = R.string.battery_recharge_current) + "(A):",
                                    data = it.rechargeCurrent.toString(1)
                                )
                                //气罐压力
                                DetailRowCommon(
                                    title = stringResource(id = R.string.tank_pressure) + "(Mpa):",
                                    data = it.gasCylinderPressure.toString(1)
                                )
                                // 管道压力
                                DetailRowCommon(
                                    title = stringResource(id = R.string.pipeline_pressure) + "(Mpa):",
                                    data = abs(it.pipelinePressure).toString(1)
                                )
                                //主板温度
                                DetailRowCommon(
                                    title = stringResource(id = R.string.mainboard_temperature) + "(℃):",
                                    data = it.mainBoardTemperature.toString(1)
                                )
                                // 电堆温度
                                DetailRowCommon(
                                    title = stringResource(id = R.string.stack_temperature) + "(℃):",
                                    data = it.stackTemperature.toString()
                                )
                            }
                        }
                        hydrogenBatteryData2?.let {
                            VerticalDivider(
                                color = Color.Black,
                                modifier = Modifier.height(144.dp),
                                thickness = 1.dp
                            )
                            Column(
                                modifier = Modifier.width(140.dp),
                            ) {
                                //id
                                DetailRowCommon(
                                    title = "ID:", data = hydrogenBatteryId2
                                )
                                // 电池电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.battery_voltage1) + "(V):",
                                    data = it.batteryVoltage.toString(1)
                                )
                                // 电堆电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.stack_voltage) + "(V):",
                                    data = it.stackVoltage.toString(1)
                                )
                                // 电机电压
                                DetailRowCommon(
                                    title = stringResource(id = R.string.motor_voltage) + "(V):",
                                    data = it.motorVoltage.toString()
                                )
                                // 电机电流
                                DetailRowCommon(
                                    title = stringResource(id = R.string.motor_current) + "(A):",
                                    data = abs(it.motorCurrent).toString(1)
                                )
                                //电池补能电流
                                DetailRowCommon(
                                    title = stringResource(id = R.string.battery_recharge_current) + "(A):",
                                    data = it.rechargeCurrent.toString(1)
                                )
                                //气罐压力
                                DetailRowCommon(
                                    title = stringResource(id = R.string.tank_pressure) + "(Mpa):",
                                    data = it.gasCylinderPressure.toString(1)
                                )
                                // 管道压力
                                DetailRowCommon(
                                    title = stringResource(id = R.string.pipeline_pressure) + "(Mpa):",
                                    data = abs(it.pipelinePressure).toString(1)
                                )
                                //主板温度
                                DetailRowCommon(
                                    title = stringResource(id = R.string.mainboard_temperature) + "(℃):",
                                    data = it.mainBoardTemperature.toString(1)
                                )
                                // 电堆温度
                                DetailRowCommon(
                                    title = stringResource(id = R.string.stack_temperature) + "(℃):",
                                    data = it.stackTemperature.toString()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * GPS 详细数据
 */
@Composable
fun GPSStatusDetails() {
    val newDevInfoData by DroneModel.newDevInfoData.observeAsState()

    var gnssA by remember { mutableStateOf<VKAg.GNSSInfo?>(null) }
    var gnssB by remember { mutableStateOf<VKAg.GNSSInfo?>(null) }
    LaunchedEffect(newDevInfoData) {
        newDevInfoData?.let {
            val devInfos = it.values
            devInfos.forEach { devInfo ->
                if (devInfo.devType == VKAgCmd.DEVINFO_GNSS) {
                    when (devInfo.devNum.toInt()) {
                        1 -> gnssA = devInfo.info as VKAg.GNSSInfo
                        2 -> gnssB = devInfo.info as VKAg.GNSSInfo
                    }
                }
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = STATUS_BAR_HEIGHT, end = 120.dp),
        horizontalArrangement = Arrangement.End
    ) {
        //GNSS-A
        gnssA?.let {
            GNSSDetails(title = "GNSS-A", gnssInfo = it)
        }
        if (gnssA != null && gnssB != null) {
            Spacer(modifier = Modifier.width(4.dp))
        }
        //GNSS-B
        gnssB?.let {
            GNSSDetails(title = "GNSS-B", gnssInfo = it)
        }
    }
}

/**
 * RTK 详细数据
 */
@Composable
fun RTKStatusDetails() {
//    val rtkInfo = VKAg.RTKInfo()
    val rtkInfo by DroneModel.deviceRTKData.observeAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = STATUS_BAR_HEIGHT, end = 120.dp),
        horizontalArrangement = Arrangement.End
    ) {
        //RTK
        rtkInfo?.let {
            RTKDetails(title = "RTK", rtkInfo = it)
        }
    }
}

@Composable
fun PeerS1Details() {
    val peerS1 by DroneModel.peerS1.observeAsState()
    val localS1 by DroneModel.localS1.observeAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = STATUS_BAR_HEIGHT, end = 240.dp),
        horizontalArrangement = Arrangement.End
    ) {
        peerS1?.let {
            Box(
                modifier = Modifier
                    .widthIn(max = 120.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (peerS1 != null && localS1 != null) {
            Spacer(modifier = Modifier.width(4.dp))
        }
        localS1?.let {
            Box(
                modifier = Modifier
                    .widthIn(max = 120.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun DetailRowCommon(
    title: String,
    data: String = "",
    textColor: Color = Color.Black,
    scales: FloatArray = floatArrayOf(1f, 0.4f),
) {
    if (scales.size < 2) return
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
        ) {
            AutoScrollingText(
                text = title,
                color = textColor,
                modifier = Modifier,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Box(
            modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterEnd
        ) {
            AutoScrollingText(
                text = data,
                color = textColor,
                modifier = Modifier,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun GNSSDetails(title: String, gnssInfo: VKAg.GNSSInfo) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
            )
    ) {
        DetailRowCommon(title = "$title:")
        //定位类型1:差, 2:良, 3:优
        DetailRowCommon(
            title = stringResource(id = R.string.position_type) + ":",
            data = when (gnssInfo.type.toInt()) {
                1 -> stringResource(id = R.string.poor)
                2 -> stringResource(id = R.string.good)
                3 -> stringResource(id = R.string.excellent)
                else -> stringResource(id = R.string.na)
            }
        )
        //水平定位精度
        DetailRowCommon(
            title = stringResource(id = R.string.horizontal_accuracy) + ":",
            data = gnssInfo.horizontal_accuracy.toString(2)
        )

        //垂直定位精度
        DetailRowCommon(
            title = stringResource(id = R.string.vertical_accuracy) + ":",
            data = gnssInfo.horizontal_accuracy.toString(2)
        )
        //星数(颗)
        DetailRowCommon(
            title = stringResource(id = R.string.star_count) + ":",
            data = gnssInfo.satellite.toString()
        )
    }
}

@Composable
private fun RTKDetails(title: String, rtkInfo: VKAg.RTKInfo) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
            )
    ) {
        DetailRowCommon(title = "$title:")
        //定位类型
        DetailRowCommon(
            title = stringResource(R.string.position_type),
            data = when (rtkInfo.location_type.toInt()) {
                1 -> stringResource(id = R.string.loc_info_type_1)
                2 -> stringResource(id = R.string.loc_info_type_5)
                3 -> stringResource(id = R.string.loc_info_type_4)
                else -> stringResource(id = R.string.na)
            }
        )
        //水平定位精度
        DetailRowCommon(
            title = stringResource(id = R.string.horizontal_accuracy) + ":",
            data = rtkInfo.horizontal_accuracy.toString(2)
        )

        //垂直定位精度
        DetailRowCommon(
            title = stringResource(id = R.string.vertical_accuracy) + ":",
            data = rtkInfo.vertical_accuracy.toString(2)
        )
        //ANT1 星数
        DetailRowCommon(
            title = "ANT1 " + stringResource(R.string.star_count),
            data = rtkInfo.satellite_ant1.toString()
        )
        //ANT2 星数
        DetailRowCommon(
            title = "ANT2 " + stringResource(R.string.star_count),
            data = rtkInfo.satellite_ant2.toString()
        )
    }
}

@Composable
private fun WorkModeChoosePopup(
    mapVideoModel: MapVideoModel,
    aptypeData: VKAg.APTYPEData?,
    onClick: (WorkModeEnum) -> Unit,
    onClose: () -> Unit,
) {
    val workMachinaType = aptypeData?.getIntValue(VKAg.APTYPE_DRONE_TYPE)
    var debugMode by remember {
        mutableIntStateOf(1)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = VeryDarkAlpha)
            .padding(start = 20.dp, end = 20.dp, bottom = 30.dp)
            .clickable(false) { },
    ) {
        //返回 - 标题
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back work",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable {
                        onClose()
                    },
                tint = Color.White
            )
            Text(
                text = stringResource(id = R.string.work_mode),
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //debug用
                if (BuildConfig.DEBUG) {
                    DebugMenuModeSwitch(debugMode) {
                        debugMode = it
                    }
                }
            }
        }
        //card
        if (BuildConfig.DEBUG) {
            when (debugMode) {
                1 -> WorkModeMenuCommon(mapVideoModel = mapVideoModel, onClick = onClick)
                2 -> WorkModeMenuLifting(mapVideoModel = mapVideoModel, onClick = onClick)
                3 -> WorkModeMenuCleaning(mapVideoModel = mapVideoModel, onClick = onClick)
            }
        } else {
            when (workMachinaType) {
                //清洗机
                VKAgCmd.DRONE_TYPE_WASHING.toInt() -> WorkModeMenuCleaning(
                    mapVideoModel = mapVideoModel, onClick = onClick
                )
                //吊运机 todo
                //其他
                else -> WorkModeMenuCommon(mapVideoModel = mapVideoModel, onClick = onClick)
            }
        }
    }
}

@Composable
private fun WorkModeCardCommon(
    modifier: Modifier = Modifier,
    workModeEnum: WorkModeEnum,
    isCheck: Boolean,
    onClick: () -> Unit,
) {
    val defaultColor = Color.DarkGray
    val checkColor = MaterialTheme.colorScheme.primary
    val cardShape = MaterialTheme.shapes.extraSmall
    val imageSize = 50.dp
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (isCheck) checkColor else defaultColor, shape = cardShape
            )
            .clickable {
                onClick()
            }) {
        Image(
            painter = painterResource(id = workModeEnum.image),
            contentDescription = "${workModeEnum.modeName} image",
            modifier = Modifier
                .padding(top = 10.dp, end = 10.dp)
                .align(Alignment.TopEnd)
                .size(imageSize),
            colorFilter = ColorFilter.tint(if (isCheck) Color.White else Color.Gray)
        )
        Text(
            text = stringResource(id = workModeEnum.modeName),
            modifier = Modifier
                .padding(start = 10.dp, bottom = 10.dp)
                .align(Alignment.BottomStart),
            style = MaterialTheme.typography.headlineLarge,
            color = if (isCheck) Color.White else Color.Gray
        )
    }
}

/**
 * common work mode menu
 */
@Composable
private fun WorkModeMenuCommon(
    mapVideoModel: MapVideoModel, onClick: (WorkModeEnum) -> Unit,
) {
    val spacedWidth = 20.dp
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(spacedWidth)
    ) {
        //left
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacedWidth)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacedWidth)
            ) {
                //大田
                WorkModeCardCommon(
                    modifier = Modifier.weight(1f),
                    workModeEnum = WorkModeEnum.LARGE_FIELD,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.LARGE_FIELD,
                    onClick = {
                        onClick(WorkModeEnum.LARGE_FIELD)
                    })
                //果树航线
                WorkModeCardCommon(
                    modifier = Modifier.weight(0.6f),
                    workModeEnum = WorkModeEnum.TREE_AIR_ROUTE,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.TREE_AIR_ROUTE,
                    onClick = {
                        onClick(WorkModeEnum.TREE_AIR_ROUTE)
                    })
                //自由航线
                WorkModeCardCommon(
                    modifier = Modifier.weight(0.6f),
                    workModeEnum = WorkModeEnum.FREE_AIR_ROUTE,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.FREE_AIR_ROUTE,
                    onClick = {
                        onClick(WorkModeEnum.FREE_AIR_ROUTE)
                    })
            }
            Row(
                modifier = Modifier.weight(0.6f),
                horizontalArrangement = Arrangement.spacedBy(spacedWidth)
            ) {
                //AB
                WorkModeCardCommon(
                    modifier = Modifier.weight(1f),
                    workModeEnum = WorkModeEnum.AB,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.AB,
                    onClick = {
                        onClick(WorkModeEnum.AB)
                    })
                //手动
                WorkModeCardCommon(
                    modifier = Modifier.weight(1f),
                    workModeEnum = WorkModeEnum.MANUAL,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.MANUAL,
                    onClick = {
                        onClick(WorkModeEnum.MANUAL)
                    })
                //增强手动
                WorkModeCardCommon(
                    modifier = Modifier.weight(1f),
                    workModeEnum = WorkModeEnum.ENHANCED_MANUAL,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.ENHANCED_MANUAL,
                    onClick = {
                        onClick(WorkModeEnum.ENHANCED_MANUAL)
                    })
            }

        }
    }
}

/**
 * cleaning work mode menu
 */
@Composable
private fun WorkModeMenuCleaning(
    mapVideoModel: MapVideoModel, onClick: (WorkModeEnum) -> Unit,
) {
    val spacedWidth = 20.dp
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(spacedWidth)
    ) {
        //left
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacedWidth)
        ) {
            //区域清洗
            WorkModeCardCommon(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                workModeEnum = WorkModeEnum.AREA_CLEAN,
                isCheck = mapVideoModel.workModeEnum == WorkModeEnum.AREA_CLEAN,
                onClick = {
                    onClick(WorkModeEnum.AREA_CLEAN)
                })
            //手动
            WorkModeCardCommon(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth(),
                workModeEnum = WorkModeEnum.MANUAL,
                isCheck = mapVideoModel.workModeEnum == WorkModeEnum.MANUAL,
                onClick = {
                    onClick(WorkModeEnum.MANUAL)
                })

        }
        //right
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(spacedWidth)
        ) {
            //垂直AB清洗
            WorkModeCardCommon(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                workModeEnum = WorkModeEnum.AB_CLEAN,
                isCheck = mapVideoModel.workModeEnum == WorkModeEnum.AB_CLEAN,
                onClick = {
                    onClick(WorkModeEnum.AB_CLEAN)
                })
            //水平AB清洗
            WorkModeCardCommon(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                workModeEnum = WorkModeEnum.CLEAN_HORIZONTAL_AB,
                isCheck = mapVideoModel.workModeEnum == WorkModeEnum.CLEAN_HORIZONTAL_AB,
                onClick = {
                    onClick(WorkModeEnum.CLEAN_HORIZONTAL_AB)
                })
        }
    }
}

/**
 * lifting work mode menu
 */
@Composable
private fun WorkModeMenuLifting(
    mapVideoModel: MapVideoModel, onClick: (WorkModeEnum) -> Unit,
) {
    val spacedWidth = 20.dp
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(spacedWidth)
    ) {
        //left
        Column(
            modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(spacedWidth)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacedWidth)
            ) {
                //手动
                WorkModeCardCommon(
                    modifier = Modifier.weight(0.6f),
                    workModeEnum = WorkModeEnum.MANUAL,
                    isCheck = mapVideoModel.workModeEnum == WorkModeEnum.MANUAL,
                    onClick = {
                        onClick(WorkModeEnum.MANUAL)
                    })
            }
        }
    }
}

/**
 * Debug 菜单切换
 */
@Composable
fun DebugMenuModeSwitch(mode: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .background(
                    color = if (mode == 1) MaterialTheme.colorScheme.primary else buttonDisabled,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable() {
                    if (mode != 1) {
                        onChange(1)
                    } else {
                        onChange(0)
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Text("通用", color = Color.White)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .background(
                    color = if (mode == 2) MaterialTheme.colorScheme.primary else buttonDisabled,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable() {
                    if (mode != 2) {
                        onChange(2)
                    } else {
                        onChange(0)
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Text("吊运", color = Color.White)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(50.dp)
                .background(
                    color = if (mode == 3) MaterialTheme.colorScheme.primary else buttonDisabled,
                    shape = MaterialTheme.shapes.extraSmall
                )
                .clickable() {
                    if (mode != 3) {
                        onChange(3)
                    } else {
                        onChange(0)
                    }
                }, contentAlignment = Alignment.Center
        ) {
            Text("清洗", color = Color.White)
        }
    }
}