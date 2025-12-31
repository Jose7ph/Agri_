package com.jiagu.ags4.scene.work

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.ui.theme.STATUS_BAR_HEIGHT
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.ags4.vm.LocatorModel
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.jgcompose.bluetooth.BluetoothList


@Composable
fun WorkLocator(route: String) {
    val mapVideoModel = LocalMapVideoModel.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val activity = LocalActivity.current as MapVideoActivity
    val locatorModel = remember { navController.getViewModel(route, LocatorModel::class.java) }
    val locationModel =
        remember { navController.getViewModel(route, LocationModel::class.java) }
    val btDeviceModel =
        remember { navController.getViewModel(route, BtDeviceModel::class.java) }
    val config = Config(context)

    val bluetoothList by btDeviceModel.list.observeAsState()
    val searching by btDeviceModel.searching.observeAsState()
    val locConnect by locationModel.locConnect.observeAsState()
    val location by locationModel.location.collectAsState()

    LaunchedEffect(locConnect) {
        if (locConnect == true) {
            //停止继续检索蓝牙
            btDeviceModel.stopScan()
            //隐藏蓝牙列表
            locatorModel.showBluetoothList = false
            btDeviceModel.list.value = null
            btDeviceModel.searching.value = null
            config.locator = ""
        }
    }
    LaunchedEffect(location, locatorModel.isStartRecord) {
        if (locatorModel.isStartRecord) {
            location?.let {
                locatorModel.addPoint(it)
            }
        }
    }

    DisposableEffect(Unit) {
        //设置打点方式为定位器打点
        config.locationType = LocationTypeEnum.LOCATOR.type
        mapVideoModel.changeLocationType(LocationTypeEnum.LOCATOR.type)
        locationModel.setup()
        onDispose {

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        //打点器信息
        LocatorInfoBox(
            modifier = Modifier
                .padding(start = 4.dp, top = STATUS_BAR_HEIGHT + 4.dp)
                .align(Alignment.TopStart),
            location = location
        )
        if (locatorModel.showBluetoothList && mapVideoModel.locationType == LocationTypeEnum.LOCATOR.type) {
            BluetoothList(
                modifier = Modifier
                    .padding(top = STATUS_BAR_HEIGHT + 4.dp, start = 4.dp)
                    .height(240.dp)
                    .width(200.dp)
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
        RightBox(modifier = Modifier.align(Alignment.TopEnd), buttons = {
            //连接打点器
            RightButtonCommon(
                text = stringResource(id = R.string.locate_type_locator),
                enabled = !locatorModel.isStartRecord
            ) {
                //已连接了打点器，再次点击可以关闭列表，没有连接打点器不允许关闭列表
                locatorModel.showBluetoothList =
                    !(locConnect == true && locatorModel.showBluetoothList)
            }
            //自动打点
            RightButtonCommon(
                text = stringResource(id = R.string.start),
                enabled = !locatorModel.isStartRecord,
            ) {
                locatorModel.isStartRecord = true
            }
            //暂停
            RightButtonCommon(
                text = stringResource(id = R.string.pause),
                enabled = locatorModel.isStartRecord,
            ) {
                locatorModel.isStartRecord = false
            }
        })
    }
}
