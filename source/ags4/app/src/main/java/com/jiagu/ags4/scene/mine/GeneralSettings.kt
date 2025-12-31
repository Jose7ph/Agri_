package com.jiagu.ags4.scene.mine

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.Constants
import com.jiagu.ags4.Constants.SIM_OPEN
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.scene.work.settings.LocationActivity
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.startActivity
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toastLong
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.counter.FloatCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.shadow.ShadowFrame
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper

private val titleRowWidth = 140.dp

@Composable
fun GeneralSettings() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val config = AppConfig(context)
    //模拟器模式
    var simulatorModeSwitch by remember {
        mutableIntStateOf(DroneModel.activeDrone?.sim ?: Constants.SIM_CLOSE)
    }
    MainContent(title = stringResource(id = R.string.mine_general_settings), breakAction = {
        navController.popBackStack()
    }) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            //地图类型
            item {
                MapTypeRow(config = config, context = context)
            }

            //面积单位
            item {
                AreaUnitRow(config = config, context = context)
            }
            //容量单位
            item {
                CapacityUnitRow(config = config, context = context)
            }
            //重量单位
            item {
                WeightUnitRow(config = config, context = context)
            }
            //长度单位
            item {
                LengthUnitRow(config = config, context = context)
            }
            //模拟器模式
            item {
                SimulatorMode(simulatorModeSwitch = simulatorModeSwitch, onClick = {
                    simulatorModeSwitch = it
                }) {
                    (context as Activity).startActivity(LocationActivity::class.java)
                }
            }
            if (simulatorModeSwitch == SIM_OPEN) {
                item {
                    AircraftPosition()
                }
            }
        }
    }
}

@Composable
fun MapTypeRow(config: AppConfig, context: Context) {
    var value by remember { mutableIntStateOf(config.mapProviderInt) }
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.weight(0.4f)) {
                AutoScrollingText(
                    text = stringResource(id = R.string.general_settings_map_type),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            }
            val names = stringArrayResource(id = R.array.general_settings_map_type).toList()
            val values = mutableListOf<Int>()
            for (i in names.indices) {
                values.add(i)
            }
            ShadowFrame(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                GroupButton(
                    items = names,
                    indexes = values,
                    number = value,
                    modifier = Modifier.fillMaxSize(),
                ) { i, _ ->
                    value = i
                    config.mapProviderInt = i
                }
            }
        }
        //自定义显示url row 和map level row
        if (value == 3) {
            var url by remember {
                mutableStateOf(config.mapUrl)
            }
            ShadowFrame(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.height(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            AutoScrollingText(
                                text = "URL",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Black,
                                textAlign = TextAlign.Start
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AutoScrollingText(
                                text = url,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.End
                            )
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(80.dp),
                            onClick = {
                                context.showDialog {
                                    var enterValue by remember {
                                        mutableStateOf("")
                                    }
                                    ScreenPopup(content = {
                                        Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(30.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "URL",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                NormalTextField(
                                                    text = enterValue,
                                                    onValueChange = { value ->
                                                        enterValue = value
                                                    },
                                                    hint = stringResource(id = R.string.please_enter) + " URL",
                                                    hintPosition = TextAlign.Center,
                                                    modifier = Modifier
                                                        .width(240.dp)
                                                        .height(30.dp),
                                                    borderColor = Color.LightGray
                                                )
                                            }
                                        }
                                    }, onDismiss = {
                                        context.hideDialog()
                                    }, onConfirm = {
                                        url = enterValue
                                        config.mapUrl = enterValue
                                        context.hideDialog()
                                    })
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            AutoScrollingText(
                                text = stringResource(id = R.string.edit),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.height(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var number by remember { mutableIntStateOf(config.mapLevel) }
                        Box(modifier = Modifier.weight(0.4f)) {
                            AutoScrollingText(
                                text = "MAP Level",
                                modifier = Modifier.width(titleRowWidth),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Black,
                                textAlign = TextAlign.Start
                            )
                        }
                        FloatCounter(
                            modifier = Modifier.width(180.dp),
                            number = number.toFloat(),
                            max = 22f,
                            min = 17f,
                            step = 1f,
                            fraction = 0
                        ) {
                            number = it.toInt()
                            config.mapLevel = number
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun AreaUnitRow(config: AppConfig, context: Context) {
    var value by remember { mutableIntStateOf(config.areaUnit) }
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.general_settings_area_unit),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        val names = UnitHelper.areaUnitList(context).toList()
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            GroupButton(
                items = names,
                indexes = values,
                number = value,
                modifier = Modifier.fillMaxSize(),
            ) { i, _ ->
                value = i
                config.areaUnit = i
                UnitHelper.setAreaUnit(i)
            }
        }
    }
}

@Composable
fun CapacityUnitRow(config: AppConfig, context: Context) {
    var value by remember { mutableIntStateOf(config.capacityUnit) }
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.general_settings_capacity_unit),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        val names = UnitHelper.capacityUnitList(context).toList()
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            GroupButton(
                items = names,
                indexes = values,
                number = value,
                modifier = Modifier.fillMaxSize(),
            ) { i, _ ->
                value = i
                config.capacityUnit = i
                UnitHelper.setCapacityUnit(i)
            }
        }
    }
}

@Composable
fun WeightUnitRow(config: AppConfig, context: Context) {
    var value by remember { mutableIntStateOf(config.weightUnit) }
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.general_settings_weight_unit),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        val names = UnitHelper.weightUnitList(context).toList()
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            GroupButton(
                items = names,
                indexes = values,
                number = value,
                modifier = Modifier.fillMaxSize(),
            ) { i, _ ->
                value = i
                config.weightUnit = i
                UnitHelper.setWeightUnit(i)
            }
        }
    }
}

@Composable
fun LengthUnitRow(config: AppConfig, context: Context) {
    var value by remember { mutableIntStateOf(config.lengthUnit) }
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.general_settings_length_unit),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        val names = UnitHelper.lengthUnitList(context).toList()
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            GroupButton(
                items = names,
                indexes = values,
                number = value,
                modifier = Modifier.fillMaxSize(),
            ) { i, _ ->
                value = i
                config.lengthUnit = i
                UnitHelper.setLengthUnit(i)
            }
        }
    }
}

/**
 * 模拟器模式
 */
@Composable
fun SimulatorMode(
    simulatorModeSwitch: Int,
    onClick: (Int) -> Unit,
    toPosition: () -> Unit = {},
) {
    val context = LocalContext.current
    val message = stringResource(id = R.string.drone_disconnected)
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.simulator_mode),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        val names = listOf(
            stringResource(id = R.string.close),
            stringResource(id = R.string.open),
        )
        val values = mutableListOf<Int>()
        for (i in names.indices) {
            values.add(i)
        }
        ShadowFrame(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            GroupButton(
                items = names,
                indexes = values,
                number = simulatorModeSwitch,
                modifier = Modifier.fillMaxSize(),
            ) { i, _ ->
                if (DroneModel.droneConnectionState.value == false) context.toastLong(message)
                else {
                    DroneModel.activeDrone?.apply {
                        onClick(i)
                        sim = i
                        if (i == SIM_OPEN) toPosition()
                    }
                }
            }
        }
    }
}

@Composable
fun AircraftPosition() {
    val context = LocalContext.current
    Row(
        modifier = Modifier.height(30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(modifier = Modifier.weight(0.4f)) {
            AutoScrollingText(
                text = stringResource(id = R.string.aircraft_position),
                modifier = Modifier.width(titleRowWidth),
                style = MaterialTheme.typography.titleSmall,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
        }
        Button(
            onClick = {
                (context as Activity).startActivity(LocationActivity::class.java)
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = MaterialTheme.colorScheme.onPrimary,
                containerColor = MaterialTheme.colorScheme.primary
            ),
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.setting),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}