package com.jiagu.ags4.scene.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.jgcompose.container.MainContent

@Composable
fun DeviceRTK() {
    val navController = LocalNavController.current
    val rtkData by DroneModel.deviceRTKData.observeAsState()
    MainContent(title = stringResource(id = R.string.device_rtk), barAction = {
    }, breakAction = {
        navController.popBackStack()
    }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val cardModifier = Modifier
                .weight(1f)
            //定位
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //设备状态
                ParameterDataCard(
                    title = stringResource(id = R.string.device_status),
                    modifier = cardModifier,
                    content = when (rtkData?.status?.toInt()) {
                        1 -> stringResource(R.string.normal)
                        2 -> stringResource(R.string.disconnected)
                        else -> EMPTY_TEXT
                    }
                )
                //定位类型
                ParameterDataCard(
                    title = stringResource(id = R.string.position_type),
                    modifier = cardModifier,
                    content = when (rtkData?.location_type?.toInt()) {
                        1 -> stringResource(R.string.loc_info_type_1)
                        2 -> stringResource(R.string.loc_info_type_5)
                        3 -> stringResource(R.string.loc_info_type_4)
                        else -> EMPTY_TEXT
                    }
                )
                //水平定位精度
                ParameterDataCard(
                    title = stringResource(id = R.string.horizontal_accuracy),
                    modifier = cardModifier,
                    content = rtkData?.horizontal_accuracy?.toString(2) ?: EMPTY_TEXT
                )
                //垂直定位精度
                ParameterDataCard(
                    title = stringResource(id = R.string.vertical_accuracy),
                    modifier = cardModifier,
                    content = rtkData?.vertical_accuracy?.toString(2) ?: EMPTY_TEXT
                )
                //定位数据源
                ParameterDataCard(
                    title = stringResource(id = R.string.position_data_source),
                    modifier = cardModifier,
                    content = when (rtkData?.location_source?.toInt()) {
                        0 -> stringResource(id = R.string.not_using_this_data)
                        1 -> stringResource(id = R.string.using_this_data)
                        else -> EMPTY_TEXT
                    }
                )
            }
            //other
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //ANT1星数
                ParameterDataCard(
                    title = "ANT1" + stringResource(R.string.star_count),
                    modifier = cardModifier,
                    content = rtkData?.satellite_ant1?.toString() ?: EMPTY_TEXT
                )
                //ANT2星数
                ParameterDataCard(
                    title = "ANT2" + stringResource(R.string.star_count),
                    modifier = cardModifier,
                    content = rtkData?.satellite_ant2?.toString() ?: EMPTY_TEXT
                )
                //经度
                ParameterDataCard(
                    title = stringResource(id = R.string.longitude),
                    modifier = cardModifier,
                    content = rtkData?.longitude?.toString(2) ?: EMPTY_TEXT
                )
                //纬度
                ParameterDataCard(
                    title = stringResource(id = R.string.latitude),
                    modifier = cardModifier,
                    content = rtkData?.latitude?.toString(2) ?: EMPTY_TEXT
                )
                //高度
                ParameterDataCard(
                    title = stringResource(id = R.string.radar_height),
                    modifier = cardModifier,
                    content = rtkData?.height?.toString(2) ?: EMPTY_TEXT
                )
            }
            //测向
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //测向类型
                ParameterDataCard(
                    title = stringResource(id = R.string.direction_find_type),
                    modifier = cardModifier,
                    content = when (rtkData?.direction_type?.toInt()) {
                        0 -> stringResource(R.string.no_direction_finding)
                        1 -> stringResource(R.string.poor_direction_finding)
                        2 -> stringResource(R.string.good_direction_finding)
                        else -> EMPTY_TEXT
                    }
                )
                //测向精度
                ParameterDataCard(
                    title = stringResource(id = R.string.direction_find_accuracy),
                    modifier = cardModifier,
                    content = rtkData?.direction_accuracy?.toString(1) ?: EMPTY_TEXT
                )
                //干扰值
                ParameterDataCard(
                    title = stringResource(id = R.string.interference_value),
                    modifier = cardModifier,
                    content = rtkData?.interference_value?.toString() ?: EMPTY_TEXT
                )
                //定向数据源
                ParameterDataCard(
                    title = stringResource(id = R.string.direction_data_source),
                    modifier = cardModifier,
                    content = when (rtkData?.direction_source?.toInt()) {
                        0 -> stringResource(id = R.string.not_using_this_data)
                        1 -> stringResource(id = R.string.using_this_data)
                        else -> EMPTY_TEXT
                    }
                )
            }
        }
    }
}