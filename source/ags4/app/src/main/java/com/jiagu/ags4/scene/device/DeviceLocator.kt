package com.jiagu.ags4.scene.device

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.scene.work.LocationTypeEnum
import com.jiagu.ags4.utils.LocalBtDeviceModel
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.LocalLocationModel
import com.jiagu.api.ext.toast
import com.jiagu.device.model.RtkLatLng
import com.jiagu.jgcompose.bluetooth.BluetoothList
import com.jiagu.jgcompose.card.ParameterCard
import com.jiagu.jgcompose.container.MainContent

@Composable
fun DeviceLocator() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as DeviceManagementActivity
    val btDeviceModel = LocalBtDeviceModel.current
    val locationModel = LocalLocationModel.current
    val config = Config(context)
    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val location by locationModel.location.collectAsState()
    val locConnect by locationModel.locConnect.observeAsState()

    LaunchedEffect(locConnect) {
        if (locConnect == true) {
            //停止继续检索蓝牙
            btDeviceModel.stopScan()
            btDeviceModel.list.value = null
            btDeviceModel.searching.value = null
            config.locator = ""
            context.toast(context.getString(R.string.success))
        }
    }

    DisposableEffect(Unit) {
        config.locationType = LocationTypeEnum.LOCATOR.type
        locationModel.setup()
        onDispose { }
    }

    MainContent(
        title = stringResource(id = R.string.locate_type_locator),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 6.dp)
                    .fillMaxHeight()
                    .weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //连接状态
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.connection_status) + ":",
                        modifier = Modifier
                            .weight(0.7f)
                    )
                    Text(
                        text = if (locConnect == true) stringResource(id = R.string.main_device_connected) else stringResource(
                            id = R.string.main_device_disconnected
                        ),
                        color = if (locConnect == true) MaterialTheme.colorScheme.primary else Color.Red,
                        modifier = Modifier
                            .weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                //蓝牙
                BluetoothList(
                    modifier = Modifier
                        .background(color = Color.White, shape = MaterialTheme.shapes.medium),
                    buttonName = stringResource(id = R.string.search_locator),
                    bluetoothList = bluetoothList,
                    searching = searching == true,
                    onItemClick = { address ->
                        btDeviceModel.stopScan()
                        config.locator = "skydroid*$address"
                        locationModel.setup()
                    },
                    onSearchClick = {
                        btDeviceModel.startScan(activity)
                    })
            }
            VerticalDivider(modifier = Modifier.fillMaxHeight())
            //打点器详情
            LocatorInfoBox(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                location = location
            )
        }

    }
}

/**
 * 打点器信息
 */
@Composable
private fun LocatorInfoBox(modifier: Modifier = Modifier, location: RtkLatLng?) {
    val locationInfo = location?.info
    val cardHeight = 80.dp
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //经度
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.longitude),
                text = (location?.lng ?: "----").toString()
            )
            //纬度
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.latitude),
                text = (location?.lat ?: "----").toString()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //海拔
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.altitude),
                text = (location?.alt ?: "----").toString()
            )
            //定位类型
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.position_type),
                text = when (locationInfo?.locType) {
                    1, 2 -> stringResource(com.jiagu.v9sdk.R.string.loc_info_type_1)
                    4 -> stringResource(com.jiagu.v9sdk.R.string.loc_info_type_4)
                    5 -> stringResource(com.jiagu.v9sdk.R.string.loc_info_type_5)
                    else -> "N/A"
                }
            )

        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            //卫星数
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.star_count),
                text = (locationInfo?.svNum ?: "----").toString()
            )
            //定位精度
            ParameterCard(
                modifier = Modifier
                    .weight(1f)
                    .height(cardHeight),
                title = stringResource(id = R.string.position_accuracy),
                text = (locationInfo?.hdop ?: "----").toString()
            )
        }
    }
}