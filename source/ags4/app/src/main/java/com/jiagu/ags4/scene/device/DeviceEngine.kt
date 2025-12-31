package com.jiagu.ags4.scene.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toString
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup

@Composable
fun DeviceEngine() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val engineData by DroneModel.engineData.observeAsState()
    val engineTypes = stringArrayResource(id = R.array.engine_type)
    val engineRunningStates = stringArrayResource(id = R.array.engine_running_state)
    val engineUnlockStatus = stringArrayResource(id = R.array.device_engine_unlock_status)
    val engineTypeName = when (engineData?.type?.toInt()) {
        1 -> engineTypes[0]
        2 -> engineTypes[1]
        3 -> engineTypes[2]
        4 -> engineTypes[3]
        else -> EMPTY_TEXT
    }
    val engineRunningStateName = when (engineData?.status?.toInt()) {
        0 -> engineRunningStates[0]
        1 -> engineRunningStates[1]
        2 -> engineRunningStates[2]
        3 -> engineRunningStates[3]
        4 -> engineRunningStates[4]
        else -> EMPTY_TEXT
    }
    val engineUnlockStatusName = when (engineData?.unlock_status?.toInt()) {
        0 -> engineUnlockStatus[0]
        1 -> engineUnlockStatus[1]
        else -> EMPTY_TEXT
    }
    MainContent(title = stringResource(id = R.string.device_management_engine), barAction = {
    }, breakAction = {
        navController.popBackStack()
    }) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val cardModifier = Modifier
                .weight(1f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_type),
                        modifier = cardModifier,
                        content = engineTypeName
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_brand),
                        modifier = cardModifier,
                        content = engineData?.brand ?: EMPTY_TEXT
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_serial_number),
                        modifier = cardModifier,
                        content = engineData?.serial ?: EMPTY_TEXT
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_running_state),
                        modifier = cardModifier,
                        content = engineRunningStateName
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_unlock_status),
                        modifier = cardModifier,
                        content = engineUnlockStatusName
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_speed),
                        modifier = cardModifier,
                        content = (engineData?.speed ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_throttle),
                        modifier = cardModifier,
                        content = (engineData?.throttle ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_voltage),
                        modifier = cardModifier,
                        content = (engineData?.voltage?.toString(1) ?: EMPTY_TEXT)
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_current),
                        modifier = cardModifier,
                        content = (engineData?.currents?.toString(1) ?: EMPTY_TEXT)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_innage),
                        modifier = cardModifier,
                        content = (engineData?.fuel ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_cylinder_1_temperature),
                        modifier = cardModifier,
                        content = (engineData?.temp1 ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_cylinder_2_temperature),
                        modifier = cardModifier,
                        content = (engineData?.temp2 ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_temperature),
                        modifier = cardModifier,
                        content = (engineData?.tempPCB ?: EMPTY_TEXT).toString()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_run_time),
                        modifier = cardModifier,
                        content = (engineData?.duration ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_maintenance_time),
                        modifier = cardModifier,
                        content = (engineData?.beforeMaintain ?: EMPTY_TEXT).toString()
                    )
                    ParameterDataCard(
                        title = stringResource(id = R.string.device_engine_lock_time),
                        modifier = cardModifier,
                        content = (engineData?.beforeLock ?: EMPTY_TEXT).toString()
                    )
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
                when (engineData?.unlock_status?.toInt()) {
                    0 -> {
                        DeviceDetailsCommonButton(
                            text = stringResource(id = R.string.engine_start),
                        ) {
                            context.showDialog {
                                ScreenPopup(
                                    width = 300.dp,
                                    content = {
                                        Box(modifier = Modifier.padding(20.dp)) {
                                            Text(
                                                text = stringResource(id = R.string.engine_start_confirm),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }, showCancel = true, showConfirm = true,
                                    onConfirm = {

                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        DeviceDetailsCommonButton(
                            text = stringResource(id = R.string.engine_stop),
                        ) {
                            context.showDialog {
                                ScreenPopup(
                                    width = 300.dp,
                                    content = {
                                        Box(modifier = Modifier.padding(20.dp)) {
                                            Text(
                                                text = stringResource(id = R.string.engine_stop_confirm),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }, showCancel = true, showConfirm = true,
                                    onConfirm = {

                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        }
                    }

                    else -> {
                        DeviceDetailsCommonButton(
                            text = stringResource(id = R.string.engine_start),
                            enable = false
                        ) {
                            context.showDialog {
                                ScreenPopup(
                                    width = 300.dp,
                                    content = {
                                        Box(modifier = Modifier.padding(20.dp)) {
                                            Text(
                                                text = stringResource(id = R.string.engine_start_confirm),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }, showCancel = true, showConfirm = true,
                                    onConfirm = {
                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        }
                        DeviceDetailsCommonButton(
                            text = stringResource(id = R.string.engine_stop),
                            enable = false
                        ) {
                            context.showDialog {
                                ScreenPopup(
                                    width = 300.dp,
                                    content = {
                                        Box(modifier = Modifier.padding(20.dp)) {
                                            Text(
                                                text = stringResource(id = R.string.engine_stop_confirm),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }, showCancel = true, showConfirm = true,
                                    onConfirm = {
                                        context.hideDialog()
                                    },
                                    onDismiss = {
                                        context.hideDialog()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}