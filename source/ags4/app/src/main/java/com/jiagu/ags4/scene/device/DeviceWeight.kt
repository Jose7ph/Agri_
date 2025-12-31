package com.jiagu.ags4.scene.device

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_WEIGHT
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_WEIGHT_K
import com.jiagu.ags4.scene.device.SprayerCalibrationTask.Companion.ID_ZERO
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.Validator
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.counter.SliderTitleCounter
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.textfield.NormalTextField
import com.jiagu.tools.ext.UnitHelper


@Composable
fun DeviceWeight() {
    val navController = LocalNavController.current
    val progressModel = LocalProgressModel.current
    val context = LocalContext.current
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val act = context as DeviceManagementActivity
    var k1Show by remember {
        mutableStateOf(true)
    }
    var k2Show by remember {
        mutableStateOf(true)
    }
    var k3Show by remember {
        mutableStateOf(true)
    }
    var k4Show by remember {
        mutableStateOf(true)
    }
    var workModeShow by remember {
        mutableStateOf(true)
    }
    var weightRateShow by remember {
        mutableStateOf(true)
    }
    var maxDrugShow by remember {
        mutableStateOf(true)
    }
    var kValueCalibrationButtonShow by remember {
        mutableStateOf(true)
    }
    val emptyString = EMPTY_TEXT
    if (!BuildConfig.DEBUG) {
        when (deviceWeightData?.manufacture_id) {
            VKAgCmd.DEVINFO_SEED_QIFEI.toInt() -> {//只有启飞有工作模式：喷洒 播撒，其他家都没有  4个传感器

            }

            VKAgCmd.DEVINFO_SEED_VK.toInt(), VKAgCmd.DEVINFO_SEED_TYI.toInt() -> {//没有工作模式   4个传感器
                maxDrugShow = false
                workModeShow = false
            }

            VKAgCmd.DEVINFO_SEED_EFT.toInt() -> {//没有工作模式   3个传感器
                maxDrugShow = false
                workModeShow = false
                k4Show = false
            }

            else -> {//其他家都不显示
                maxDrugShow = false
                workModeShow = false
                k1Show = false
                k2Show = false
                k3Show = false
                k4Show = false
                weightRateShow = false
                kValueCalibrationButtonShow = false
            }
        }
    }
    val progress by progressModel.progress.observeAsState()

    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = progress as ProgressModel.ProgressMessage
            context.showDialog {
                PromptPopup(
                    content = message.text,
                    showConfirm = false,
                    onConfirm = {},
                    onDismiss = {
                        progressModel.next(0)
                        context.hideDialog()
                    })
            }
        }

        is ProgressModel.ProgressNotice -> {
            val notice = progress as ProgressModel.ProgressNotice
            context.showDialog {
                PromptPopup(
                    content = notice.title,
                    onConfirm = { progressModel.next(1) },
                    onDismiss = {
                        progressModel.next(0)
                        context.hideDialog()
                    })
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = progress as ProgressModel.ProgressResult
            val processed = (context as DeviceManagementActivity).taskComplete?.invoke(
                result.success,
                result.msg
            ) ?: false
            if (!processed && result.msg != null) {
                if (result.msg!!.contains("apk")) context.installApk(
                    result.msg!!
                )
                else context.toast(result.msg!!)
            }
            context.hideDialog()
            progressModel.done()
        }
    }
    MainContent(
        title = stringResource(id = R.string.device_management_weight),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val cardModifier = Modifier.weight(1f)
            val cardSpacerWidth = 10.dp
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacerWidth)
                    ) {
                        ParameterDataCard(
                            title = stringResource(id = R.string.device_weight_manufactor),
                            modifier = cardModifier,
                            content = deviceWeightData?.manufacture_name ?: emptyString
                        )
                        ParameterDataCard(
                            title = stringResource(
                                R.string.device_weight_weight, UnitHelper.weightUnit()
                            ),
                            modifier = cardModifier,
                            content = if (deviceWeightData?.remain_weight != null) UnitHelper.transWeight(
                                deviceWeightData?.remain_weight ?: 0f
                            )
                            else emptyString
                        )
                        if (weightRateShow) {
                            ParameterDataCard(
                                title = stringResource(
                                    R.string.device_weight_weight_rate, UnitHelper.weightUnit()
                                ),
                                modifier = cardModifier,
                                content = if (deviceWeightData?.ratio_weight != null) UnitHelper.transWeight(
                                    deviceWeightData?.ratio_weight ?: 0f
                                ) else emptyString
                            )
                        }
                        if (workModeShow) {
                            val content =
                                if (isSeedWorkType()) stringResource(id = R.string.device_weight_work_mode_seed) else stringResource(
                                    id = R.string.device_weight_work_mode_spray
                                )
                            ParameterDataCard(
                                title = stringResource(id = R.string.device_weight_work_mode),
                                modifier = cardModifier,
                                content = content
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacerWidth)
                    ) {
                        ParameterDataCard(
                            title = "${stringResource(id = R.string.radar_weight)}: 1",
                            modifier = cardModifier,
                            content = if (deviceWeightData?.weight1 != null) UnitHelper.transWeight(
                                deviceWeightData?.weight1 ?: 0f
                            )
                            else emptyString
                        )
                        ParameterDataCard(
                            title = "${stringResource(id = R.string.radar_weight)}: 2",
                            modifier = cardModifier,
                            content = if (deviceWeightData?.weight2 != null) UnitHelper.transWeight(
                                deviceWeightData?.weight2 ?: 0f
                            )
                            else emptyString
                        )
                        ParameterDataCard(
                            title = "${stringResource(id = R.string.radar_weight)}: 3",
                            modifier = cardModifier,
                            content = if (deviceWeightData?.weight3 != null) UnitHelper.transWeight(
                                deviceWeightData?.weight3 ?: 0f
                            )
                            else emptyString
                        )
                        ParameterDataCard(
                            title = "${stringResource(id = R.string.radar_weight)}: 4",
                            modifier = cardModifier,
                            content = if (deviceWeightData?.weight4 != null) UnitHelper.transWeight(
                                deviceWeightData?.weight4 ?: 0f
                            )
                            else emptyString
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacerWidth)
                    ) {
                        if (k1Show) {
                            ParameterDataCard(
                                title = stringResource(id = R.string.device_weight_k1),
                                modifier = cardModifier,
                                content = (deviceWeightData?.sensor_k1 ?: emptyString).toString()
                            )
                        }
                        if (k2Show) {
                            ParameterDataCard(
                                title = stringResource(id = R.string.device_weight_k2),
                                modifier = cardModifier,
                                content = (deviceWeightData?.sensor_k2 ?: emptyString).toString()
                            )
                        }
                        if (k3Show) {
                            ParameterDataCard(
                                title = stringResource(id = R.string.device_weight_k3),
                                modifier = cardModifier,
                                content = (deviceWeightData?.sensor_k3 ?: emptyString).toString()
                            )
                        }
                        if (k4Show) {
                            ParameterDataCard(
                                title = stringResource(id = R.string.device_weight_k4),
                                modifier = cardModifier,
                                content = (deviceWeightData?.sensor_k4 ?: emptyString).toString()
                            )
                        }
                    }

                }
                //最大起飞
                item {
                    val flightWeight =
                        DroneModel.aptypeData.value?.getValue(VKAg.APTYPE_MAX_WEIGHT)?.div(10f)
                            ?: 10f
                    SliderTitleCounter(
                        title = stringResource(
                            R.string.device_weight_max_flight_weight, UnitHelper.weightUnit()
                        ),
                        number = flightWeight,
                        fraction = 1,
                        min = 10f,
                        max = 90f,
                        step = 0.1f,
                        converter = if (AppConfig(context).weightUnit == 0) null else UnitHelper.getWeightConverter()
                    ) {
                        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_MAX_WEIGHT, it * 10)
                    }
                }
                //最小断药
                item {
                    val stopDrugWeight =
                        DroneModel.aptypeData.value?.getIntValue(VKAg.APTYPE_EFT_MIN_WEIGHT)
                            ?.div(10f) ?: 0f
                    SliderTitleCounter(
                        title = stringResource(
                            R.string.device_weight_min_stop_drug_weight, UnitHelper.weightUnit()
                        ),
                        number = stopDrugWeight,
                        fraction = 1,
                        min = 0f,
                        max = 5f,
                        step = 0.1f,
                        converter = if (AppConfig(context).weightUnit == 0) null else UnitHelper.getWeightConverter()
                    ) {
                        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_EFT_MIN_WEIGHT, it * 10)
                    }
                }
                //最大下药率
                item {
                    if (maxDrugShow) {
                        val maxDrugRate = deviceWeightData?.max_drug?.toFloat() ?: 0f
                        SliderTitleCounter(
                            title = stringResource(R.string.device_weight_max_drug_rate, UnitHelper.weightUnit()),
                            number = maxDrugRate,
                            fraction = 0,
                            min = 0f,
                            max = 200f,
                            step = 1f,
                            converter = if (AppConfig(context).weightUnit == 0) null else UnitHelper.getWeightConverter()
                        ) {
                            DroneModel.activeDrone?.calibSeeder(26, it.toInt())
                        }
                    }
                }
            }
            VerticalDivider(
                thickness = 1.dp, color = Color.Gray
            )
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                DeviceDetailsCommonButton(
                    text = stringResource(id = R.string.local_upgrade),
                ) {
                    act.upgrade(FirmwareTypeEnum.WEIGHT, false, "", "", "")
                }
                Spacer(modifier = Modifier.height(10.dp))
                DeviceDetailsCommonButton(
                    text = stringResource(id = R.string.peeling_calibration),
                ) {
                    context.showDialog {
                        PeelingCalibrationPopup(
                            progressModel = progressModel,
                            onDismiss = { context.hideDialog() })
                    }
                }
                DeviceDetailsCommonButton(
                    text = stringResource(id = R.string.weight_calibration),
                ) {
                    context.showDialog {
                        WeightCalibrationPopup(
                            progressModel = progressModel,
                            onDismiss = { context.hideDialog() })
                    }
                }
                if (kValueCalibrationButtonShow) {
                    DeviceDetailsCommonButton(
                        text = stringResource(id = R.string.k_value_calibration),
                    ) {
                        context.showDialog {
                            KValueCalibrationPopup(
                                progressModel = progressModel,
                                onDismiss = { context.hideDialog() })
                        }
                    }
                }
            }
        }
    }
}

/**
 * 去皮校准
 */
@Composable
fun PeelingCalibrationPopup(progressModel: ProgressModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val peelingCalibrationTip = stringResource(id = R.string.peeling_calibration_tip)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)

    var step by remember { mutableIntStateOf(1) }

    val calibrationTip = when (step) {
        1 -> peelingCalibrationTip
        2 -> inCalibrationTip
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }

    val confirmText = when (step) {
        1 -> R.string.start_calibration
        3 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                4
            } else {
                3
            }
        }
    }

    ScreenPopup(
        width = 360.dp, confirmText = confirmText, content = {
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .width(330.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.peeling_calibration),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 26.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = if (step == 1) TextAlign.Start else TextAlign.Center,
                            color = if (step == 3) Color.Red else Color.Black
                        )
                    }
                }
            }
        }, onDismiss = {
            Log.d("zhy", "peeling calibration cancel")
            step = 1
            progressModel.done()
            onDismiss()
        }, onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    context.startProgress(
                        task = SprayerCalibrationTask(type = ID_ZERO),
                    )
                }

                //step = 2校准中 无操作
                3 -> {
                    step = 1
                }

                4 -> {
                    step = 1
                    progressModel.done()
                    onDismiss()
                }
            }
        }, showConfirm = when (step) {
            2 -> false
            else -> true
        }, showCancel = when (step) {
            2 -> false
            else -> true
        }
    )
}

/**
 * 重量校准
 */
@Composable
fun WeightCalibrationPopup(
    progressModel: ProgressModel, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    val weightCalibrationTip = stringResource(id = R.string.weight_calibration_tip)
    val inCalibrationTip = stringResource(id = R.string.in_calibration_tip)
    val calibrationSuccessTip = stringResource(id = R.string.calibration_finish_tip)
    val calibrationFailTip = stringResource(id = R.string.calibration_fail_tip)

    var text by remember {
        mutableStateOf(deviceWeightData?.remain_weight?.toString() ?: "")
    }
    val inputValid = Validator.checkNumber(text) || text.isBlank()

    var step by remember {
        mutableIntStateOf(1)
    }
    val calibrationTip = when (step) {
        1 -> weightCalibrationTip
        2 -> inCalibrationTip
        3 -> calibrationFailTip
        4 -> calibrationSuccessTip
        else -> ""
    }

    val confirmText = when (step) {
        1 -> R.string.start_calibration
        3 -> R.string.retry
        else -> R.string.confirm
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            step = if (result.success) {
                4
            } else {
                3
            }
        }
    }

    ScreenPopup(
        width = 360.dp, confirmText = confirmText, content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.weight_calibration),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                if (step == 1) {
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        AutoScrollingText(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    NormalTextField(
                        onValueChange = {
                            text = it
                        },
                        text = text,
                        modifier = Modifier
                            .width(200.dp)
                            .height(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = MaterialTheme.shapes.small
                            ),
                        borderColor = if (inputValid) Color.LightGray else MaterialTheme.colorScheme.error,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = if (inputValid) Color.Black else MaterialTheme.colorScheme.error
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = calibrationTip,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = if (step == 3) Color.Red else Color.Black
                        )
                    }
                }
            }
        }, onDismiss = {
            step = 1
            progressModel.done()
            onDismiss()
        }, onConfirm = {
            when (step) {
                1 -> {
                    step = 2
                    context.startProgress(
                        task = SprayerCalibrationTask(
                            type = ID_WEIGHT, params = intArrayOf(text.toFloat().toInt())
                        ),
                    )
                }

                3 -> {
                    step = 1
                    progressModel.done()
                }

                4 -> {
                    step = 1
                    progressModel.done()
                    onDismiss()
                }
            }
        }, showConfirm = when (step) {
            2 -> false
            else -> true
        }, showCancel = when (step) {
            2 -> false
            else -> true
        }, confirmEnable = inputValid && text.isNotEmpty()
    )
}

/**
 * K值校准
 */
@Composable
fun KValueCalibrationPopup(
    progressModel: ProgressModel, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val kValueCalibrationTip = stringResource(id = R.string.k_value_calibration_tip)

    val deviceWeightData by DroneModel.deviceWeightData.observeAsState()
    var kValueSize = 4
    if (deviceWeightData?.manufacture_id == VKAgCmd.DEVINFO_SEED_EFT.toInt()) {
        kValueSize = 3
    }
    var text1 by remember {
        mutableStateOf(deviceWeightData?.sensor_k1?.toString() ?: "")
    }
    var text2 by remember {
        mutableStateOf(deviceWeightData?.sensor_k2?.toString() ?: "")
    }
    var text3 by remember {
        mutableStateOf(deviceWeightData?.sensor_k3?.toString() ?: "")
    }
    var text4 by remember {
        mutableStateOf(deviceWeightData?.sensor_k4?.toString() ?: "")
    }
    val inputValid1 = Validator.checkNumerical(text1) || text1.isBlank()
    val inputValid2 = Validator.checkNumerical(text2) || text2.isBlank()
    val inputValid3 = Validator.checkNumerical(text3) || text3.isBlank()
    val inputValid4 = Validator.checkNumerical(text4) || text4.isBlank()

    val defaultButtonText = stringResource(id = R.string.confirm)
    val inCalibration = stringResource(id = R.string.in_calibration)
    val calibrationSuccess = stringResource(id = R.string.calibration_success)
    val calibrationFail = stringResource(id = R.string.calibration_fail)
    var buttonText1 by remember {
        mutableStateOf(defaultButtonText)
    }
    var buttonText2 by remember {
        mutableStateOf(defaultButtonText)
    }
    var buttonText3 by remember {
        mutableStateOf(defaultButtonText)
    }
    var buttonText4 by remember {
        mutableStateOf(defaultButtonText)
    }

    var text1CalibrationResult by remember {
        mutableStateOf(true)
    }
    var text2CalibrationResult by remember {
        mutableStateOf(true)
    }
    var text3CalibrationResult by remember {
        mutableStateOf(true)
    }
    var text4CalibrationResult by remember {
        mutableStateOf(true)
    }

    val progress by progressModel.progress.observeAsState()
    when (progress) {
        is ProgressModel.ProgressResult -> {
            val result = (progress as ProgressModel.ProgressResult)
            progressModel.done()
            if (result.success) {
                when (result.msg) {
                    "1" -> {
                        buttonText1 = calibrationSuccess
                        text1CalibrationResult = true
                    }

                    "2" -> {
                        buttonText2 = calibrationSuccess
                        text2CalibrationResult = true
                    }

                    "3" -> {
                        buttonText3 = calibrationSuccess
                        text3CalibrationResult = true
                    }

                    "4" -> {
                        buttonText4 = calibrationSuccess
                        text4CalibrationResult = true
                    }
                }
            } else {
                when (result.msg) {
                    "1" -> {
                        buttonText1 = calibrationFail
                        text1CalibrationResult = false
                    }

                    "2" -> {
                        buttonText2 = calibrationFail
                        text2CalibrationResult = false
                    }

                    "3" -> {
                        buttonText3 = calibrationFail
                        text3CalibrationResult = false
                    }

                    "4" -> {
                        buttonText4 = calibrationFail
                        text4CalibrationResult = false
                    }
                }
            }
        }
    }

    ScreenPopup(width = 360.dp, content = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.k_value_calibration),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            progressModel.done()
                            onDismiss()
                        }) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable {
                                onDismiss()
                            }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    AutoScrollingText(
                        text = kValueCalibrationTip,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (i in 1..kValueSize) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = i.toString(), style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            NormalTextField(
                                onValueChange = {
                                    when (i) {
                                        1 -> text1 = it
                                        2 -> text2 = it
                                        3 -> text3 = it
                                        4 -> text4 = it
                                    }
                                },
                                text = when (i) {
                                    1 -> text1
                                    2 -> text2
                                    3 -> text3
                                    4 -> text4
                                    else -> ""
                                },
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(30.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        shape = MaterialTheme.shapes.small
                                    ),

                                borderColor = when (i) {
                                    1 -> if (inputValid1) Color.LightGray else MaterialTheme.colorScheme.error
                                    2 -> if (inputValid2) Color.LightGray else MaterialTheme.colorScheme.error
                                    3 -> if (inputValid3) Color.LightGray else MaterialTheme.colorScheme.error
                                    4 -> if (inputValid4) Color.LightGray else MaterialTheme.colorScheme.error
                                    else -> Color.LightGray
                                },
                                hint = stringResource(id = R.string.k_value_calibration_hint),
                                hintTextStyle = MaterialTheme.typography.labelMedium,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    when (i) {
                                        1 -> if (inputValid1) Color.Black else MaterialTheme.colorScheme.error
                                        2 -> if (inputValid2) Color.Black else MaterialTheme.colorScheme.error
                                        3 -> if (inputValid3) Color.Black else MaterialTheme.colorScheme.error
                                        4 -> if (inputValid4) Color.Black else MaterialTheme.colorScheme.error
                                        else -> Color.Black
                                    }
                                ),
                                enabled = when (i) {
                                    1 -> {
                                        buttonText1 == defaultButtonText
                                    }

                                    2 -> {
                                        buttonText2 == defaultButtonText
                                    }

                                    3 -> {
                                        buttonText3 == defaultButtonText
                                    }

                                    4 -> {
                                        buttonText4 == defaultButtonText
                                    }

                                    else -> false
                                }

                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                enabled = when (i) {
                                    1 -> inputValid1 && text1.isNotEmpty() && buttonText1 == defaultButtonText
                                    2 -> inputValid2 && text2.isNotEmpty() && buttonText2 == defaultButtonText
                                    3 -> inputValid3 && text3.isNotEmpty() && buttonText3 == defaultButtonText
                                    4 -> inputValid4 && text4.isNotEmpty() && buttonText4 == defaultButtonText
                                    else -> false
                                },
                                onClick = {
                                    when (i) {
                                        1 -> {
                                            buttonText1 = inCalibration
                                            context.startProgress(
                                                task = SprayerCalibrationTask(
                                                    type = ID_WEIGHT_K,
                                                    params = intArrayOf(text1.toInt()),
                                                    kValueNum = 1
                                                ),
                                            )
                                        }

                                        2 -> {
                                            buttonText2 = inCalibration
                                            context.startProgress(
                                                task = SprayerCalibrationTask(
                                                    type = ID_WEIGHT_K,
                                                    params = intArrayOf(text2.toInt()),
                                                    kValueNum = 2
                                                ),
                                            )
                                        }

                                        3 -> {
                                            buttonText3 = inCalibration
                                            context.startProgress(
                                                task = SprayerCalibrationTask(
                                                    type = ID_WEIGHT_K,
                                                    params = intArrayOf(text3.toInt()),
                                                    kValueNum = 3
                                                ),
                                            )
                                        }

                                        4 -> {
                                            buttonText4 = inCalibration
                                            context.startProgress(
                                                task = SprayerCalibrationTask(
                                                    type = ID_WEIGHT_K,
                                                    params = intArrayOf(text4.toInt()),
                                                    kValueNum = 4
                                                ),
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(30.dp),
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (i) {
                                        1 -> if (text1CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                        2 -> if (text2CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                        3 -> if (text3CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                        4 -> if (text4CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                        else -> MaterialTheme.colorScheme.primary
                                    }, disabledContainerColor = when (i) {
                                        1 -> {
                                            if (buttonText1 != inCalibration && buttonText1 != defaultButtonText) {
                                                if (text1CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                            } else {
                                                Color.Unspecified
                                            }
                                        }

                                        2 -> {
                                            if (buttonText2 != inCalibration && buttonText2 != defaultButtonText) {
                                                if (text2CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                            } else {
                                                Color.Unspecified
                                            }
                                        }

                                        3 -> {
                                            if (buttonText3 != inCalibration && buttonText3 != defaultButtonText) {
                                                if (text3CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                            } else {
                                                Color.Unspecified
                                            }
                                        }

                                        4 -> {
                                            if (buttonText4 != inCalibration && buttonText4 != defaultButtonText) {
                                                if (text4CalibrationResult) MaterialTheme.colorScheme.primary else Color.Red
                                            } else {
                                                Color.Unspecified
                                            }
                                        }

                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                val buttonText = when (i) {
                                    1 -> buttonText1
                                    2 -> buttonText2
                                    3 -> buttonText3
                                    4 -> buttonText4
                                    else -> ""
                                }
                                AutoScrollingText(
                                    text = buttonText,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }, showConfirm = false, showCancel = false, onDismiss = {})
}