package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.work.LocalMapVideoModel
import com.jiagu.ags4.scene.work.MapVideoModel
import com.jiagu.ags4.scene.work.MapVideoModel.SettingType
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 全局按钮颜色
 */
@Composable
fun buttonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun generateIcon(buttonId: SettingType): Painter {
    val vm = LocalMapVideoModel.current
    // 使用 when 表达式来返回与给定设置类型 ID 对应的图标资源
    return when (buttonId) {
        SettingType.SETTINGS_TYPE_FLYING -> painterResource(id = R.drawable.default_flight_control)
        SettingType.SETTINGS_TYPE_SPRAYING -> {
            if (vm.sprayingType == VKAg.LOAD_TYPE_SEED) {
                painterResource(id = R.drawable.default_device_seeder)
            } else {
                painterResource(id = R.drawable.default_device_sprayer)
            }
        }

        SettingType.SETTINGS_TYPE_RADAR -> painterResource(id = R.drawable.default_radar)
        SettingType.SETTINGS_TYPE_RTK -> painterResource(id = R.drawable.default_rtk)
        SettingType.SETTINGS_TYPE_BATTERY -> painterResource(id = R.drawable.default_battery_settings)
        SettingType.SETTINGS_TYPE_WORK_MACHINA -> painterResource(id = R.drawable.default_factory_mode)
        SettingType.SETTINGS_TYPE_OTHER -> painterResource(id = R.drawable.default_other_settings)
    }
}

@Composable
fun getContentDescription(buttonId: SettingType): String {
    val vm = LocalMapVideoModel.current
    // 使用 when 表达式来返回与给定设置类型 ID 对应的描述资源 ID
    return stringResource(
        when (buttonId) {
            SettingType.SETTINGS_TYPE_FLYING -> R.string.settings_type_flying
            SettingType.SETTINGS_TYPE_SPRAYING -> {
                if (vm.sprayingType == VKAg.LOAD_TYPE_SEED) {
                    R.string.settings_type_seed
                } else {
                    R.string.settings_type_spraying
                }
            }

            SettingType.SETTINGS_TYPE_RADAR -> R.string.settings_type_radar
            SettingType.SETTINGS_TYPE_RTK -> R.string.settings_type_rtk_station
            SettingType.SETTINGS_TYPE_BATTERY -> R.string.settings_type_battery
            SettingType.SETTINGS_TYPE_WORK_MACHINA -> R.string.settings_type_work_machina
            SettingType.SETTINGS_TYPE_OTHER -> R.string.settings_type_other
        }
    )
}


/**
 * 设置图标集合
 */
@Composable
fun SettingsIconList() {
    val mapVideoModel = LocalMapVideoModel.current

    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .fillMaxHeight()
            .background(
                color = Color.Black, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            ), verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        enumValues<SettingType>().forEach { buttonId ->
            val iconColor =
                if (mapVideoModel.selectedSettingButtonId == buttonId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
            Box(
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .width(40.dp)
                    .height(40.dp)
                    .clickable {
                        mapVideoModel.selectedSettingButtonId = buttonId
                    }, contentAlignment = Alignment.Center
            ) {
                val icon = generateIcon(buttonId)
                val contentDescription = getContentDescription(buttonId)
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 设置标题
 */
@Composable
fun SettingsTitle(
    modifier: Modifier = Modifier, settingsTypeName: String, mapVideoModel: MapVideoModel
) {
    //按钮颜色
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimary
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            AutoScrollingText(
                text = settingsTypeName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        mapVideoModel.showSetting = false
                    },
                    colors = buttonColors,
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsView() {
    val vm = LocalMapVideoModel.current
    //打开菜单
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        // 占据左半边的空间
        Spacer(modifier = Modifier
            .weight(0.5f)
            .fillMaxHeight()
            .clickable(enabled = false) {})
        // 在右半边放置Box
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxSize() // 使Box填充可用空间
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                SettingsIconList()
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.onPrimary
                )
                Column(modifier = Modifier.background(color = Color.Black)) {
                    // 第一行Row，它将被固定在顶部
                    SettingsTitle(
                        settingsTypeName = getContentDescription(buttonId = vm.selectedSettingButtonId),
                        modifier = Modifier.height(40.dp),
                        mapVideoModel = vm
                    )
                    // 剩余内容可滚动的Column
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SettingsContentRoute(
                            type = vm.selectedSettingButtonId,
                            modifier = Modifier.fillMaxWidth(),
                            vm = vm
                        )
                    }
                }
            }
        }
    }
}


/**
 * 设置内容
 */
@Composable
fun SettingsContentRoute(
    type: SettingType, modifier: Modifier = Modifier, vm: MapVideoModel
) {
    val bottomPadding = 10.dp
    DroneModel.activeDrone?.getParameters()
    when (type) {
        SettingType.SETTINGS_TYPE_FLYING -> FlyingSettings(modifier = modifier.padding(bottom = bottomPadding))
        SettingType.SETTINGS_TYPE_SPRAYING -> SprayingSettings(
            modifier = modifier.padding(bottom = bottomPadding), vm = vm
        )

        SettingType.SETTINGS_TYPE_RADAR -> RadarSettings(modifier = modifier.padding(bottom = bottomPadding))
        SettingType.SETTINGS_TYPE_RTK -> RTKSettings(
            modifier = modifier.padding(bottom = bottomPadding), vm = vm
        )

        SettingType.SETTINGS_TYPE_BATTERY -> BatterySettings(modifier = modifier.padding(bottom = bottomPadding))
        SettingType.SETTINGS_TYPE_WORK_MACHINA -> WorkMachinaSettings(
            modifier = modifier.padding(bottom = bottomPadding)
        )

        SettingType.SETTINGS_TYPE_OTHER -> OtherSettings(
            modifier = modifier.padding(bottom = bottomPadding), vm = vm
        )
    }
}
