package com.jiagu.ags4.scene.work.settings

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.Constants
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.scene.work.MapVideoModel
import com.jiagu.ags4.ui.theme.buttonGroup
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.RtcmModel
import com.jiagu.api.ext.toastLong
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.chart.BarData
import com.jiagu.jgcompose.chart.GridBarChart
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * RTK设置
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RTKSettings(modifier: Modifier = Modifier, vm: MapVideoModel) {
    val context = LocalContext.current
    val config = Config(context)
    val ntrip = config.ntrip.split("/")
    val valid = ntrip.size > 1
    vm.ntripAccount = (if (valid) ntrip[0] else "")
    vm.ntripPass = (if (valid) ntrip[1] else "")
    vm.ntripMountPoint = (if (valid) ntrip[2] else "")
    vm.ntripPort = (if (valid) ntrip[3] else "")
    vm.ntripHost = (if (valid) ntrip[4] else "")

    vm.rtkSource = config.rtkType
    val rtcmInfo = RtcmModel.rtcmInfo.observeAsState()
    val rtcmData = RtcmModel.rtcmData.observeAsState()
    val rtcmCount = RtcmModel.rtcmCount.observeAsState()
    val rtcmMessage = RtcmModel.rtcmMessage.observeAsState()
    val rtcmDataTotalSize = RtcmModel.rtcmDataTotalSize.observeAsState()
    val rtcmConnectStartTime = RtcmModel.rtcmConnectStartTime.observeAsState(0L)
    var showAnalysis by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RtkModeSwitchButton(defaultNumber = config.rtkType, vm = vm){
            showAnalysis = false
        }
        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .background(Color.Black), // 设置竖线的颜色
        )
        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (vm.rtkSource == Constants.RTK_TYPE_D_RTK) {
                item {
                    SwitchButtonRow(
                        title = R.string.network_rtk_status,
                        defaultChecked = if (rtcmInfo.value?.source == RtcmModel.TYPE_USB && rtcmInfo.value?.status != null && rtcmInfo.value?.status!! > -1) true else false,
                        explain = "",
                        explainColor = MaterialTheme.colorScheme.onPrimary,
                        showLight = if (rtcmInfo.value?.source == RtcmModel.TYPE_USB && rtcmInfo.value?.status != null && rtcmInfo.value?.status!! > -1) true else false,
                        lightCurrentInt = rtcmData.value ?: 0,
                        refreshKey = rtcmCount.value ?: 0
                    ) { checked ->
                        if (checked) DroneModel.openUsbStation()
                        else RtcmModel.closeRtcmProvider()
                    }
                }
                item {
                    SwitchButtonRow(
                        title = R.string.auto_connect,
                        defaultChecked = config.autoUsbRTKConnect,
                    ) { checked ->
                        config.autoUsbRTKConnect = checked
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 5.dp)
                    ) {
                        RtkRowText(
                            text = stringResource(id = R.string.network_rtk_status),
                            explain = vm.rtkInfo
                        )
                        val customNetworkRtkStatusBoxHeight = 180.dp
                        val customNetworkRtkStatusRowHeightRate = 6
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(customNetworkRtkStatusBoxHeight)
                                .padding(top = 5.dp)
                                .border(
                                    width = 1.dp, // 边框宽度
                                    color = MaterialTheme.colorScheme.onPrimary, // 边框颜色
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Row {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .padding(vertical = 5.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextFieldRow(
                                        title = R.string.ntrip_host,
                                        style = MaterialTheme.typography.labelLarge,
                                        defaultValue = vm.ntripHost,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(customNetworkRtkStatusBoxHeight / customNetworkRtkStatusRowHeightRate)
                                    ) {
                                        vm.ntripHost = it
                                    }
                                    TextFieldRow(
                                        title = R.string.port,
                                        style = MaterialTheme.typography.labelLarge,
                                        defaultValue = vm.ntripPort,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(customNetworkRtkStatusBoxHeight / customNetworkRtkStatusRowHeightRate)
                                    ) {
                                        vm.ntripPort = it
                                    }
                                    TextFieldRow(
                                        title = R.string.account,
                                        style = MaterialTheme.typography.labelLarge,
                                        defaultValue = vm.ntripAccount,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(customNetworkRtkStatusBoxHeight / customNetworkRtkStatusRowHeightRate)
                                    ) {
                                        vm.ntripAccount = it
                                    }
                                    TextFieldRow(
                                        title = R.string.password,
                                        style = MaterialTheme.typography.labelLarge,
                                        defaultValue = vm.ntripPass,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(customNetworkRtkStatusBoxHeight / customNetworkRtkStatusRowHeightRate)
                                    ) {
                                        vm.ntripPass = it
                                    }
                                    TextFieldRow(
                                        title = R.string.mount_point,
                                        style = MaterialTheme.typography.labelLarge,
                                        defaultValue = vm.ntripMountPoint,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(customNetworkRtkStatusBoxHeight / customNetworkRtkStatusRowHeightRate)
                                    ) {
                                        vm.ntripMountPoint = it
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(0.2f)
                                ) {
                                    TextButton(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(customNetworkRtkStatusBoxHeight / 2)
                                        .clip(RoundedCornerShape(topEnd = 8.dp))
                                        .background(color = MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = {
                                            try {
                                                connectNtrip(config, vm)
                                            } catch (e: Exception) {
                                                Log.e("lee", "connectNtrip: $e")
                                                context.toastLong(
                                                    "Ntrip: ${
                                                        context.getString(
                                                            R.string.err_network
                                                        )
                                                    }"
                                                )
                                            }
                                        }) {
                                        AutoScrollingText(
                                            text = stringResource(id = R.string.confirm),
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    TextButton(modifier = Modifier
                                        .fillMaxWidth()
                                        .height(customNetworkRtkStatusBoxHeight / 2)
                                        .clip(RoundedCornerShape(bottomEnd = 8.dp))
                                        .background(color = buttonGroup),
                                        contentPadding = PaddingValues(0.dp),
                                        onClick = {
                                            RtcmModel.closeRtcmProvider()
                                        }) {
                                        AutoScrollingText(
                                            text = stringResource(id = R.string.cancel),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                SwitchButtonRow(
                    title = R.string.show_analysis,
                    defaultChecked = showAnalysis,
                    explain = rtcmDataTotalSize.value.toString() + " bytes" + " | " + (System.currentTimeMillis() - rtcmConnectStartTime.value) / 1000 + "s" +
                            " | " + (rtcmDataTotalSize.value ?: 0) / ((System.currentTimeMillis() - rtcmConnectStartTime.value) / 1000).coerceAtLeast(1) + " bytes/s",
                    explainColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    showAnalysis = it
                }
            }
            if (showAnalysis) {
                val types = mutableListOf<String>()
                for (message in rtcmMessage.value ?: emptyList()) {
                    types.add(message.messageTypeStr)
                }
                item {
                    Text(
                        text = types.joinToString(", "),
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp).fillMaxWidth())
                }
                for (message in rtcmMessage.value ?: emptyList()) {
//                    if (message.msmData.signals.all { it.cnr == null || it.cnr == 0.0 }) continue
                    item {
//                        val barData = message.msmData.signals.filter { it.cnr != null && it.cnr!! > 0.0 }.map {
                        val barData = message.msmData.signals.map {
                            BarData(
                                name = it.signalId.toString(),
                                value = if (it.cnr == null) 1f else it.cnr!!.toFloat(),
                                color = when (message.messageType) {
                                    in 1081..1087 -> Color.Blue//GLONASS
                                    in 1071..1077 -> Color.Green//GPS
                                    in 1121..1127 -> Color.Red//BeiDou
                                    in 1091..1097 -> Color(0xFF800080)//Galileo
                                    in 1101..1107 -> Color.Yellow//SBAS
                                    in 1111..1117 -> Color.Magenta//QZSS
                                    else -> Color.White
                                }
                            )
                        }
                        GridBarChart(data = barData.sortedBy { s -> s.value }.reversed(),
                            modifier = Modifier
                                .fillMaxWidth(),
                            barWidth = 16.dp,
                            chartHeight = 30.dp,
                            textColor = Color.White,
                            showName = false,
                            columns = 20
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun RtkModeSwitchButton(
    modifier: Modifier = Modifier, defaultNumber: Int = 0, vm: MapVideoModel,
    onClick: () -> Unit = {}
) {
    var number by remember { mutableStateOf(defaultNumber) }
    val context = LocalContext.current
    val config = Config(context)
    Row(
        modifier = modifier
            .height(settingsGlobalRowHeight)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.width(settingsGlobalTitleWidth)) {
            SettingsGlobalRowText(text = stringResource(id = R.string.rtk_signal_source))
        }

        val names = stringArrayResource(id = R.array.rtk_signal_source).toList()
        val values = mutableListOf<Int>()
        values.add(Constants.RTK_TYPE_NTRIP)
        values.add(Constants.RTK_TYPE_D_RTK)
        GroupButton(
            modifier = modifier
                .weight(1f)
                .height(settingsGlobalButtonHeight),
            items = names,
            indexes = values,
            number = number,
        ) { idx, _ ->
            onClick()
            number = idx
            vm.rtkSource = number
            config.rtkType = number
        }
    }
}

private fun connectNtrip(config: Config, vm: MapVideoModel) {
    RtcmModel.rtcmDataTotalSize.postValue(0)
    RtcmModel.rtcmConnectStartTime.postValue(System.currentTimeMillis())
    val account = vm.ntripAccount
    val pass = vm.ntripPass
    val mountpoint = vm.ntripMountPoint
    val post = vm.ntripPort
    val host = vm.ntripHost
    if (account == "" || pass == "" || mountpoint == "" || post == "" || host == "") {
        return
    }
    val list = listOf(account, pass, mountpoint, post, host)
    Log.v("lee", "connectNtrip: $list")
    config.ntrip = list.joinToString("/")
    DroneModel.openNtrip(list)
}
