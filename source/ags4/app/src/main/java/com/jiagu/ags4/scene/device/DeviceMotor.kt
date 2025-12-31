package com.jiagu.ags4.scene.device

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
import com.jiagu.ags4.scene.factory.buildMotorData
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.motor.Motor
import com.jiagu.jgcompose.popup.PromptPopup


@Composable
fun DeviceMotor() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val motorData by DroneModel.motorData.observeAsState()
    val pwmData by DroneModel.pwmData.observeAsState()
    val motorInfoList =
        buildMotorData(context = context, motorData = motorData?.motors!!, pwmData = pwmData)

    MainContent(
        title = stringResource(id = R.string.device_management_dynamic_system),
        barAction = {
        },
        breakAction = {
            navController.popBackStack()
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
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
}
