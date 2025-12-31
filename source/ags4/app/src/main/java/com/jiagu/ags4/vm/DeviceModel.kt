package com.jiagu.ags4.vm

import android.app.Application
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.device.vkprotocol.NewWarnTool

class DeviceCardInfo(
    val deviceCardType: Short,
    val deviceType: Int,
    val deviceImage: Int,
    val target: String,
    var content: @Composable ColumnScope.() -> Unit = {}
)


val LocalDeviceModel = compositionLocalOf<DeviceModel> { error("No DeviceModel provided") }

class DeviceModel(app: Application) : AndroidViewModel(app) {
    //设备类型拓展
    enum class DeviceTypeExpand(val type: Short) {
        DEVICE_CAMERA_GIMBAL(200), //摄像头/云台
        DEVICE_LOCATOR(201) //打点器
    }

    var deviceWarns = mutableStateListOf<NewWarnTool.WarnStringData>()

    val errorCards = mutableStateListOf<NewWarnTool.WarnStringData>()
    val warnCards = mutableStateListOf<NewWarnTool.WarnStringData>()

    var fcuWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var batteryWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var motorWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var radarWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var seedWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var pumpWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
    var rtkWarnType by mutableIntStateOf(NewWarnTool.WARN_TYPE_DISC)
}
