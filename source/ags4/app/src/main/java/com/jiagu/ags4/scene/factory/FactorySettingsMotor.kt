package com.jiagu.ags4.scene.factory

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.motor.Motor
import com.jiagu.jgcompose.motor.MotorInfo
import com.jiagu.jgcompose.popup.PromptPopup

/**
 * 电机设置
 */
@Composable
fun FactorySettingsMotor() {
    val context = LocalContext.current
    val motorData by DroneModel.motorData.observeAsState()
    val pwmData by DroneModel.pwmData.observeAsState()
    val motorInfoList =
        buildMotorData(context = context, motorData = motorData?.motors!!, pwmData)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(motorInfoList.size) {
            val motorInfo = motorInfoList[it]
            Motor(
                modifier = Modifier.fillMaxWidth(),
                motorInfo = motorInfo,
                onNameClick = {
                    context.showDialog {
                        PromptPopup(content = stringResource(
                            id = R.string.motor_setting_tip,
                            motorInfo.number + 1
                        ), onConfirm = {
                            DroneModel.activeDrone?.setMotorNumber(motorInfo.number)
                            context.hideDialog()
                        }, onDismiss = {
                            context.hideDialog()
                        })
                    }
                },
                onCheckClick = {
                    context.showDialog {
                        PromptPopup(
                            content = stringResource(
                                id = R.string.motor_check_tip,
                                motorInfo.number + 1
                            ),
                            onConfirm = {
                                DroneModel.activeDrone?.calibMotor(motorInfo.number)
                                context.hideDialog()
                            }, onDismiss = {
                                context.hideDialog()
                            })
                    }
                }
            )
        }
    }
}

/**
 * 构建动力数据
 */
fun buildMotorData(
    context: Context, motorData: Array<VKAg.MotorData>, pwmData: VKAg.PWMData?
): List<MotorInfo> {
    val motorInfoList = mutableListOf<MotorInfo>()
    for ((i, m) in motorData.withIndex()) {
        var percent = m.percent
        if (percent == 0.toShort()) {
            pwmData?.let {
                percent = (it.ElectricOutput[i].toInt() - 100).toShort()
            }
        }
        val warn = stateConvertString(context, m.state).joinToString(" ")
        val throttleSignal = m.state.shr(15)
        val throttle = when (throttleSignal) {
            0 -> context.getString(R.string.motor_warn_throttle_signal_source_can)
            1 -> context.getString(R.string.motor_warn_throttle_signal_source_pwm)
            else -> "${throttleSignal}"
        }
        val motor = MotorInfo(
            numberString = "M${m.number}",
            state = String.format("%04X", m.state),
            throttleSource = throttle,
            speed = m.speed.toString(),
            current = m.current.toString(2),
            voltage = m.voltage.toString(2),
            temperature = m.temperature.toString(),
            percent = percent,
            duration = m.duration.toString(),
            stateString = warn,
            number = i,
            enabled = m.voltage.toInt() != 0
        )
        motorInfoList.add(motor)
    }
    return motorInfoList.toList()
}

private fun stateConvertString(context: Context, state: Int): List<String> {
    val warn = mutableListOf<String>()
    if ((state and 1) == 1) warn.add(context.getString(R.string.motor_warn_over_voltage))
    if ((state.shr(1) and 1) == 1) warn.add(context.getString(R.string.motor_warn_under_voltage))
    if ((state.shr(2) and 1) == 1) warn.add(context.getString(R.string.motor_warn_over_current))
    if ((state.shr(4) and 1) == 1) warn.add(context.getString(R.string.motor_warn_throttle_lost))
    if ((state.shr(5) and 1) == 1) warn.add(context.getString(R.string.motor_warn_throttle_not_zero))
    if ((state.shr(6) and 1) == 1) warn.add(context.getString(R.string.motor_warn_mos_over_temperature))
    if ((state.shr(7) and 1) == 1) warn.add(context.getString(R.string.motor_warn_capacitance_out_temperature))
    if ((state.shr(8) and 1) == 1) warn.add(context.getString(R.string.motor_warn_stuck))
    if ((state.shr(9) and 1) == 1) warn.add(context.getString(R.string.motor_warn_mos_open_circuit))
    if ((state.shr(10) and 1) == 1) warn.add(context.getString(R.string.motor_warn_mos_short_circuit))
    if ((state.shr(11) and 1) == 1) warn.add(context.getString(R.string.motor_warn_disconnected))
    if ((state.shr(12) and 1) == 1) warn.add(context.getString(R.string.motor_warn_working_abnormal))
    if ((state.shr(13) and 1) == 1) warn.add(context.getString(R.string.motor_warn_connect_abnormal))
    return warn
}
