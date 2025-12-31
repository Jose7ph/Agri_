package com.jiagu.ags4.scene.factory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.jgcompose.text.AutoScrollingText

/**
 * 工厂模式-设置
 */
@Composable
fun FactorySettings() {
    val factoryModel = LocalFactoryModel.current
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        //菜单对应内容
        Box(modifier = Modifier.weight(1f)) {
            DroneModel.activeDrone?.getPidParameters()
            when (factoryModel.selectedMenuId) {
                FactoryTypeEnum.FACTORY_TYPE_MODEL -> FactorySettingsModel()
                FactoryTypeEnum.FACTORY_TYPE_INSTALL -> FactorySettingsInstall()
                FactoryTypeEnum.FACTORY_TYPE_PARAMETER -> FactorySettingsParameter()
                FactoryTypeEnum.FACTORY_TYPE_RC -> FactorySettingsRc()
                FactoryTypeEnum.FACTORY_TYPE_MOTOR -> FactorySettingsMotor()
            }
        }
        //菜单
        Box(modifier = Modifier.weight(0.15f)) {
            FactorySettingsMenu(factoryModel = factoryModel)
        }
    }
}

fun getFactoryMenus(): Array<FactoryTypeEnum> {
    val enumValues = enumValues<FactoryTypeEnum>()
    return enumValues
}

/**
 * 高级设置-菜单
 */
@Composable
fun FactorySettingsMenu(factoryModel: FactoryModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.surfaceDim),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        getFactoryMenus().forEach { buttonId ->
            item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // 取消点击效果
                        ) {
                            factoryModel.selectedMenuId = buttonId
                            //遥控器设置 则获取通道映射
                            if (buttonId == FactoryTypeEnum.FACTORY_TYPE_RC) {
                                DroneModel.activeDrone?.getChannelMapping()
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = when (buttonId) {
                                FactoryTypeEnum.FACTORY_TYPE_MODEL -> R.drawable.default_flight_control
                                FactoryTypeEnum.FACTORY_TYPE_INSTALL -> R.drawable.default_install_settings
                                FactoryTypeEnum.FACTORY_TYPE_PARAMETER -> R.drawable.default_parameter_settings
                                FactoryTypeEnum.FACTORY_TYPE_RC -> R.drawable.default_remote_control
                                FactoryTypeEnum.FACTORY_TYPE_MOTOR -> R.drawable.default_motor
                            }
                        ),
                        contentDescription = buttonId.name,
                        colorFilter = ColorFilter.tint(if (factoryModel.selectedMenuId == buttonId) MaterialTheme.colorScheme.primary else Color.Black),
                        modifier = Modifier
                            .size(30.dp)
                            .padding(4.dp)
                    )
                    AutoScrollingText(
                        text = when (buttonId) {
                            FactoryTypeEnum.FACTORY_TYPE_MODEL -> stringResource(id = R.string.factory_settings_model)
                            FactoryTypeEnum.FACTORY_TYPE_INSTALL -> stringResource(id = R.string.factory_settings_install)
                            FactoryTypeEnum.FACTORY_TYPE_PARAMETER -> stringResource(id = R.string.factory_settings_parameter)
                            FactoryTypeEnum.FACTORY_TYPE_RC -> stringResource(id = R.string.factory_settings_rc)
                            FactoryTypeEnum.FACTORY_TYPE_MOTOR -> stringResource(id = R.string.factory_settings_electrical_machinery)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (factoryModel.selectedMenuId == buttonId) MaterialTheme.colorScheme.primary else Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}