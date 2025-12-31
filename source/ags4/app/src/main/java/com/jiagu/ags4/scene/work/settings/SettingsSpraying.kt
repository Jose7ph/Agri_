package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.scene.device.CentrifugalNozzleCard
import com.jiagu.ags4.scene.device.ParameterDataCard
import com.jiagu.ags4.scene.device.WaterLinePumpCard
import com.jiagu.ags4.scene.device.WaterPumpCard
import com.jiagu.ags4.scene.factory.sendParameter
import com.jiagu.ags4.scene.work.MapVideoModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.button.GroupButton
import com.jiagu.tools.ext.UnitHelper

/**
 * 喷洒设置
 */
@Composable
fun SprayingSettings(modifier: Modifier = Modifier, vm: MapVideoModel) {
    val deviceFlowData by DroneModel.deviceFlowData.observeAsState()
    val imuData by DroneModel.imuData.observeAsState()
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val devicePumpData by DroneModel.devicePumpData.observeAsState()
    val deviceLinePumpData by DroneModel.deviceLinePumpData.observeAsState()
    val deviceCentrifugalData by DroneModel.deviceCentrifugalData.observeAsState()
    val deviceSeedData by DroneModel.deviceSeedData.observeAsState()
    val aptypeData by DroneModel.aptypeData.observeAsState()

    //喷洒数据
    var totalFlowRateValue by remember {
        mutableStateOf("----")
    }
    var sprayedAmountValue by remember {
        mutableStateOf("----")
    }
    var medicineBoxLoadCapacityValue by remember {
        mutableStateOf("----")
    }
    var levelGaugeStatusValue by remember {
        mutableStateOf("----")
    }
    deviceFlowData?.let {
        totalFlowRateValue =
            UnitHelper.transCapacity(it.speed_flow1 / 1000f + it.speed_flow2 / 1000f)
    }
    imuData?.let {
        sprayedAmountValue = UnitHelper.transCapacity(it.YiYongYaoLiang)
        levelGaugeStatusValue = it.waterLevel.toString(1)
    }
    deviceWeightData?.let {
        medicineBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
    }

    //播撒数据
    var materialBoxLoadCapacityValue by remember {
        mutableStateOf("----")
    }
    var valveOpeningValue by remember {
        mutableStateOf("----")
    }
    var swingingSpeedValue by remember {
        mutableStateOf("----")
    }
    deviceWeightData?.let {
        materialBoxLoadCapacityValue = UnitHelper.transWeight(it.remain_weight)
    }
    deviceSeedData?.let {
        valveOpeningValue = it.valve.toString()
        swingingSpeedValue = it.speed.toString()
    }
    /**
     * 0：双喷头 1：4喷头全开 2：4喷头前后
     */
    //喷头模式 0：双喷头 非0：4喷头
    var seedMode by remember {
        mutableIntStateOf(-1)
    }
    //四喷头开关 2 4喷头前后 1：4喷头全开
    var fourSeedMode by remember {
        mutableIntStateOf(-1)
    }
    aptypeData?.let {
        when (it.getIntValue(VKAg.APTYPE_SPRAY_NOZZLE_MODE)) {
            0 -> {
                seedMode = 0
            }

            1 -> {
                seedMode = 1
                fourSeedMode = 1
            }

            2 -> {
                seedMode = 1
                fourSeedMode = 2
            }
        }
    }

    /**
     * 0-7bit，喷洒
     * bit0:流量计
     * bit1:重量（APP先不显示）
     * bit2：液位计
     * 8-15bit，播撒
     * bit8:断料记
     * bit9:重量
     */
    val drugType = aptypeData?.getIntValue(VKAg.APTYPE_DRUG_TYPE) ?: 0
    //喷洒断药类型
    var sprayDrug by remember {
        mutableIntStateOf(drugType and 0xFF)
    }
    //播撒断药类型
    var seedDrug by remember {
        mutableIntStateOf((drugType shr 8) and 0xFF)
    }
    Column(
        modifier = modifier
    ) {
        when (DroneModel.currentWorkType.second) {
            VKAg.LOAD_TYPE_SEED -> vm.sprayingType = VKAg.LOAD_TYPE_SEED
            VKAg.LOAD_TYPE_SPRAY -> vm.sprayingType = VKAg.LOAD_TYPE_SPRAY
            else -> {
                SprayModeSwitchButton(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(settingsGlobalButtonHeight), vm = vm
                )
            }
        }
        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(settingsGlobalColumnSpacer)
        ) {
            //喷洒
            if (vm.sprayingType == VKAg.LOAD_TYPE_SPRAY) {
                //喷洒系统
                item {
                    SwitchButtonRow(
                        title = R.string.spray_system,
                        defaultChecked = (aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_SEED_SWITCH)
                            ?: 0) == 1,
                        explain = stringResource(id = R.string.spray_system_explain),
                        explainColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        DroneModel.activeDrone?.sendIndexedParameter(
                            VKAg.APTYPE_SPRAY_SEED_SWITCH, if (it) 1 else 0
                        )
                    }
                }
                //喷头模式
                item {
                    val names = stringArrayResource(id = R.array.seed_mode).toList()
                    val values = listOf(0, 1)
                    GroupButtonRow(
                        title = R.string.seed_mode,
                        defaultNumber = seedMode,
                        names = names,
                        values = values,
                    ) {
                        DroneModel.activeDrone?.sendIndexedParameter(
                            VKAg.APTYPE_SPRAY_NOZZLE_MODE, it
                        )
                    }
                }
                //四喷头开关 (喷头模式=四喷头显示 双喷头隐藏)
                if (seedMode > 0) {
                    item {
                        val names = stringArrayResource(id = R.array.seed_mode_four_switch).toList()
                        val values = listOf(2, 1)
                        GroupButtonRow(
                            title = R.string.seed_mode_four_switch,
                            defaultNumber = fourSeedMode,
                            names = names,
                            values = values
                        ) {
                            DroneModel.activeDrone?.sendIndexedParameter(
                                VKAg.APTYPE_SPRAY_NOZZLE_MODE, it
                            )
                        }
                    }
                }
                //实时数据
                item {
                    val listCardWidth = 250.dp
                    FrameColumn {
                        Box(
                            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            SettingsGlobalRowText(
                                text = stringResource(id = R.string.spray_system_real_data),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ParameterDataCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(
                                    R.string.total_flow_rate,
                                    UnitHelper.capacityUnit()
                                ),
                                content = totalFlowRateValue,
                                style = MaterialTheme.typography.labelMedium,
                                textColor = MaterialTheme.colorScheme.onPrimary
                            )
                            ParameterDataCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(
                                    R.string.sprayed_amount,
                                    UnitHelper.capacityUnit()
                                ),
                                content = sprayedAmountValue,
                                style = MaterialTheme.typography.labelMedium,
                                textColor = MaterialTheme.colorScheme.onPrimary
                            )
                            ParameterDataCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(
                                    R.string.medicine_box_load_capacity,
                                    UnitHelper.weightUnit()
                                ),
                                content = medicineBoxLoadCapacityValue,
                                style = MaterialTheme.typography.labelMedium,
                                textColor = MaterialTheme.colorScheme.onPrimary
                            )
                            ParameterDataCard(
                                modifier = Modifier.weight(1f),
                                title = stringResource(id = R.string.level_gauge_status),
                                content = levelGaugeStatusValue,
                                style = MaterialTheme.typography.labelMedium,
                                textColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp)
                                .padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (deviceLinePumpData == null) {
                                    WaterPumpCard(
                                        modifier = Modifier,
                                        devicePumpData = devicePumpData,
                                        deviceFlowData = deviceFlowData,
                                        style = MaterialTheme.typography.labelMedium,
                                        textColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                deviceLinePumpData?.let {
                                    WaterLinePumpCard(
                                        modifier = Modifier,
                                        devicePumpData = deviceLinePumpData,
                                        deviceFlowData = deviceFlowData,
                                        style = MaterialTheme.typography.labelMedium,
                                        textColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            CentrifugalNozzleCard(
                                modifier = Modifier.weight(1f),
                                deviceCentrifugalData = deviceCentrifugalData,
                                style = MaterialTheme.typography.labelMedium,
                                textColor = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                //断药类型
                item {
                    val names = listOf(
                        stringResource(id = R.string.spray_stop_drug_flowmeter),
                        stringResource(id = R.string.spray_stop_drug_weighing_module),
                        stringResource(id = R.string.spray_stop_drug_liquid_level_gauge)
                    )
                    val values = listOf(1, 2, 4)
                    GroupButtonsRow(
                        title = R.string.spray_stop_drug,
                        defaultNumber = sprayDrug,
                        names = names,
                        values = values
                    ) {
                        sprayDrug = it
                        DroneModel.activeDrone?.sendIndexedParameter(
                            VKAg.APTYPE_DRUG_TYPE,
                            (sprayDrug and 0xFF) or (seedDrug shl 8 and 0xFF00)
                        )
                    }
                }
            }
            //播撒
            else {
                //播撒系统
                item {
                    SwitchButtonRow(
                        title = R.string.seed_system,
                        defaultChecked = (aptypeData?.getIntValue(VKAg.APTYPE_SPRAY_SEED_SWITCH)
                            ?: 0) == 1,
                        explain = stringResource(id = R.string.seed_system_explain),
                        explainColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        DroneModel.activeDrone?.sendIndexedParameter(
                            VKAg.APTYPE_SPRAY_SEED_SWITCH, if (it) 1 else 0
                        )
                    }
                }
                //实时数据
                item {
                    val cardHeight = 60.dp
                    Box(modifier = Modifier.padding(horizontal = settingsGlobalPaddingHorizontal)) {
                        FrameColumn {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SettingsGlobalRowText(
                                    text = stringResource(id = R.string.seed_system_real_data),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(settingsGlobalColumnSpacer))
                            Row(
                                modifier = Modifier
                                    .height(cardHeight)
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp)
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ParameterDataCard(
                                    modifier = Modifier.weight(1f),
                                    title = stringResource(
                                        R.string.material_box_load_capacity,
                                        UnitHelper.weightUnit()
                                    ),
                                    content = materialBoxLoadCapacityValue,
                                    style = MaterialTheme.typography.labelMedium,
                                    textColor = MaterialTheme.colorScheme.onPrimary
                                )
                                ParameterDataCard(
                                    modifier = Modifier.weight(1f),
                                    title = stringResource(id = R.string.valve_opening),
                                    content = valveOpeningValue,
                                    style = MaterialTheme.typography.labelMedium,
                                    textColor = MaterialTheme.colorScheme.onPrimary
                                )
                                ParameterDataCard(
                                    modifier = Modifier.weight(1f),
                                    title = stringResource(id = R.string.swinging_speed),
                                    content = swingingSpeedValue,
                                    style = MaterialTheme.typography.labelMedium,
                                    textColor = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                }
                //断药类型
                item {
                    val names = listOf(
                        stringResource(id = R.string.seed_stop_drug_broken_material_radar),
                        stringResource(id = R.string.seed_stop_drug_weighing_module)
                    )
                    val values = listOf(1, 2)
                    GroupButtonsRow(
                        title = R.string.seed_stop_drug,
                        defaultNumber = seedDrug,
                        names = names,
                        values = values
                    ) {
                        seedDrug = it
                        DroneModel.activeDrone?.sendIndexedParameter(
                            VKAg.APTYPE_DRUG_TYPE,
                            (sprayDrug and 0xFF) or (seedDrug shl 8 and 0xFF00)
                        )
                    }
                }
            }

            //断药检测延迟(s)
            item {
                CounterRow(
                    titleString = stringResource(R.string.bump_detection_drug_delay),
                    counterType = COUNTER_TYPE_FLOAT,
                    floatMin = 0.1f,
                    floatMax = 1.0f,
                    floatStep = 0.1f,
                    floatDefaultNumber = aptypeData?.getValue(VKAg.APTYPE_DRUG_DELAY) ?: 0.5f,
                ) {
                    sendParameter(VKAg.APTYPE_DRUG_DELAY, it)
                }
            }
        }
    }
}

/**
 * 喷洒模式切换
 */
@Composable
fun SprayModeSwitchButton(
    modifier: Modifier = Modifier, vm: MapVideoModel
) {
    var number by remember {
        mutableIntStateOf(1)
    }
    val names = listOf(
        stringResource(id = R.string.spray), stringResource(id = R.string.seed)
    )
    val values = listOf(1, 2)
    GroupButton(
        modifier = modifier,
        items = names,
        indexes = values,
        number = number,
    ) { idx, _ ->
        when (idx) {
            1 -> {
                vm.sprayingType = VKAg.LOAD_TYPE_SPRAY
            }

            2 -> {
                vm.sprayingType = VKAg.LOAD_TYPE_SEED
            }
        }
        number = idx
    }
}
