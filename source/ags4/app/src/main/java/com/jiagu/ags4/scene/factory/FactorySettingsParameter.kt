package com.jiagu.ags4.scene.factory

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.ParamTool
import com.jiagu.ags4.utils.ProtectionType
import com.jiagu.ags4.utils.parseProtectionMode
import com.jiagu.ags4.utils.setProtectionMode
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.ags4.utils.startProgress
import com.jiagu.jgcompose.popup.InputPopup
import com.jiagu.jgcompose.popup.ListSelectionPopup
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.theme.LocalExtendedColors

/**
 * 参数设置
 */
@Composable
fun FactorySettingsParameter() {
    val factoryModel = LocalFactoryModel.current
    val progressModel = LocalProgressModel.current
    val context = LocalContext.current
    val aptypeData by DroneModel.aptypeData.observeAsState()
    val pidData by DroneModel.pidData.observeAsState()
    val progress by progressModel.progress.observeAsState()
    //参数导入进度条
    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = progress as ProgressModel.ProgressMessage
            context.toast(message.text)
        }

        is ProgressModel.ProgressResult -> {
            val result = progress as ProgressModel.ProgressResult
            progressModel.done()
            DroneModel.activeDrone?.getPidParameters()
            factoryModel.uploadAndDownloadEnabled = true
            context.toast(result.msg ?: "")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        //滤波带宽设置
        item {
            FilterBandwidthSetting(
                aptypeData = aptypeData
            )
        }
        //电机解锁阈值
        item {
            MotorUnlockingThreshold(
                aptypeData = aptypeData
            )
        }
        //remote id 开关
        item {
            RemoteIdSwitch(
                aptypeData = aptypeData
            )
        }
        //分隔符
        item {
            RowSeparationBox(
                title = stringResource(id = R.string.factory_settings_sensitivity)
            )
        }
        //感度设置
        item {
            SensitivitySettingContent(
                pidData = pidData
            )
        }
        //刹车系数
        item {
            AptypeSettingContent(
                aptypeData = aptypeData
            )
        }

        // RTK保护机制
        item {
            RtkProtectionMechanism(
                aptypeData = aptypeData
            )
        }
        //一键保存/导入/定位增强
        item {
            OperationButtons(
                aptypeData = aptypeData, factoryModel = factoryModel
            )
        }
    }
}

/**
 * 滤波带宽设置
 * 2-弱 1-中 0-强 6-很强 5-超强
 */
@Composable
private fun FilterBandwidthSetting(aptypeData: VKAg.APTYPEData?) {
    val number = aptypeData?.getIntValue(VKAg.APTYPE_NOISE_SUPPRESSION) ?: 0
    val names = listOf("1", "2", "3", "4", "5")
    val values = listOf(2, 1, 0, 6, 5)
    GroupButtonRow(
        title = stringResource(id = R.string.parameter_setting_filter_bandwidth_setting),
        titleTip = stringResource(id = R.string.parameter_setting_filter_bandwidth_setting_tip),
        items = names, indexes = values, number = number
    ) {
        sendParameter(VKAg.APTYPE_NOISE_SUPPRESSION, it.toFloat())
    }
}

/**
 * 电机解锁阈值
 * 1100--低速，1150--慢速，1200--中速，1250-快速，1300--高速
 */
@Composable
private fun MotorUnlockingThreshold(aptypeData: VKAg.APTYPEData?) {
    val number = aptypeData?.getIntValue(VKAg.APTYPE_IDLE_SPEED) ?: 0
    val names = stringArrayResource(id = R.array.motor_idle).toList()
    val values = listOf(1100, 1150, 1200, 1250, 1300)
    GroupButtonRow(
        title = stringResource(id = R.string.parameter_setting_motor_unlocking_threshold),
        titleTip = stringResource(id = R.string.parameter_setting_motor_unlocking_threshold_tip),
        items = names, indexes = values, number = number
    ) {
        sendParameter(VKAg.APTYPE_IDLE_SPEED, it.toFloat())
    }
}

@Composable
private fun RemoteIdSwitch(aptypeData: VKAg.APTYPEData?) {
    val number = aptypeData?.getIntValue(VKAg.APTYPE_REMOTE_ID) ?: 0
    val names = listOf(
        stringResource(R.string.close), stringResource(R.string.open)
    )
    val values = listOf(0, 1)
    GroupButtonRow(
        title = stringResource(id = R.string.remote_id_switch),
        items = names, indexes = values, number = number
    ) {
        sendParameter(VKAg.APTYPE_REMOTE_ID, it.toFloat())
    }
}

/**
 * 感度设置
 * 姿态自稳 R基础感度 50 ~ 300
 * 姿态感度 R姿态感度 200 ~ 700
 * 姿态阻尼 R阻尼 0 ~ 25
 * 航向自稳 Y基础感度 120 ~ 600
 * 航向感度 Y姿态感 200 ~ 700
 * 航向阻尼 Y阻尼 0 ~ 25
 * 垂直感度 V高度加速度P 200 ~ 400
 * 垂直阻尼 V高度加速度I 5 ~ 70
 * 水平感度 水平速度系数 50 ~ 200
 * 水平阻尼 水平速度阻尼 0 ~ 100
 */
@Composable
private fun SensitivitySettingContent(pidData: VKAg.PIDData?) {
    //姿态自稳
    var rBase = pidData?.getValue(VKAg.PID_R_BASE) ?: -1f
    //姿态感度
    var rZitai = pidData?.getValue(VKAg.PID_R_ZITAI) ?: -1f
    //姿态阻尼
    var rStable = pidData?.getValue(VKAg.PID_R_STABLE) ?: -1f
    //航向自稳
    var yBase = pidData?.getValue(VKAg.PID_Y_BASE) ?: -1f
    //航向感度
    var yZitai = pidData?.getValue(VKAg.PID_Y_ZITAI) ?: -1f
    //航向阻尼
    var yStable = pidData?.getValue(VKAg.PID_Y_STABLE) ?: -1f
    //垂直感度
    var vSpeed = pidData?.getValue(VKAg.PID_V_SPEED) ?: -1f
    //垂直阻尼
    var vAccel = pidData?.getValue(VKAg.PID_V_ACCEL_P) ?: -1f
    //水平感度
    var hSpeed = pidData?.getValue(VKAg.PID_H_SPEED) ?: -1f
    //水平阻尼
    var hAccel = pidData?.getValue(VKAg.PID_H_ACCEL_P) ?: -1f

    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        frameColumn {
            //姿态自稳
            SensitivitySetting(
                text = stringResource(id = R.string.posture_self_stabilization),
                value = rBase,
                min = 50f,
                max = 300f,
                tipString = stringResource(id = R.string.posture_self_stabilization_tip)
            ) {
                rBase = it
                sendPidParameter(31, it.toInt())
            }
            //姿态感度
            SensitivitySetting(
                text = stringResource(id = R.string.posture_sensitivity),
                value = rZitai,
                min = 200f,
                max = 700f,
                tipString = stringResource(id = R.string.posture_sensitivity_tip)
            ) {
                rZitai = it
                sendPidParameter(34, it.toInt())
            }
            //姿态阻尼
            SensitivitySetting(
                text = stringResource(id = R.string.posture_damping),
                value = rStable,
                min = 0f,
                max = 25f,
                tipString = stringResource(id = R.string.posture_damping_tip)
            ) {
                rStable = it
                sendPidParameter(32, it.toInt())
            }
        }
        frameColumn {
            //航向自稳
            SensitivitySetting(
                text = stringResource(id = R.string.course_self_stabilization),
                value = yBase,
                min = 120f,
                max = 600f,
                tipString = stringResource(id = R.string.course_self_stabilization_tip)
            ) {
                yBase = it
                sendPidParameter(VKAg.PID_Y_BASE, it.toInt())
            }
            //航向感度
            SensitivitySetting(
                text = stringResource(id = R.string.course_sensitivity),
                value = yZitai,
                min = 200f,
                max = 700f,
                tipString = stringResource(id = R.string.course_sensitivity_tip)
            ) {
                yZitai = it
                sendPidParameter(VKAg.PID_Y_ZITAI, it.toInt())
            }
            //航向阻尼
            SensitivitySetting(
                text = stringResource(id = R.string.course_damping),
                value = yStable,
                min = 0f,
                max = 25f,
                tipString = stringResource(id = R.string.course_damping_tip)
            ) {
                yStable = it
                sendPidParameter(VKAg.PID_Y_STABLE, it.toInt())
            }
        }
        frameColumn {
            //垂直感度
            SensitivitySetting(
                text = stringResource(id = R.string.vertical_sensitivity),
                value = vSpeed,
                min = 200f,
                max = 400f,
                tipString = stringResource(id = R.string.vertical_sensitivity_tip)
            ) {
                vSpeed = it
                sendPidParameter(VKAg.PID_V_SPEED, it.toInt())
            }
            //垂直阻尼
            SensitivitySetting(
                text = stringResource(id = R.string.vertical_damping),
                value = vAccel,
                min = 5f,
                max = 70f,
                tipString = stringResource(id = R.string.vertical_damping_tip)
            ) {
                vAccel = it
                sendPidParameter(33, it.toInt())
            }
        }
        frameColumn {
            //水平感度
            SensitivitySetting(
                text = stringResource(id = R.string.horizontal_sensitivity),
                value = hSpeed,
                min = 50f,
                max = 200f,
                tipString = stringResource(id = R.string.horizontal_sensitivity_tip)
            ) {
                hSpeed = it
                sendPidParameter(VKAg.PID_H_SPEED, it.toInt())
            }
            //水平阻尼
            SensitivitySetting(
                text = stringResource(id = R.string.horizontal_damping),
                value = hAccel,
                min = 0f,
                max = 100f,
                tipString = stringResource(id = R.string.horizontal_damping_tip)
            ) {
                hAccel = it
                //todo 后续改35
                sendPidParameter(VKAg.PID_H_ACCEL_P, it.toInt())
            }
        }
    }
}

@Composable
private fun AptypeSettingContent(aptypeData: VKAg.APTYPEData?) {
    //刹车系数
    var brake = aptypeData?.getValue(VKAg.APTYPE_BRAKE) ?: -1f
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        frameColumn {
            //刹车系数
            ParamSetting(
                text = stringResource(id = R.string.param_pid_brake),
                value = brake,
                min = 10f,
                max = 30f,
            ) {
                brake = it
                sendIndexedParameter(VKAg.APTYPE_BRAKE, it.toInt())
            }
        }
    }
}

/**
 * RTK保护机制
 */
@Composable
private fun RtkProtectionMechanism(aptypeData: VKAg.APTYPEData?) {
    // TODO: 国际化
    val number = aptypeData?.getIntValue(VKAg.APTYPE_RTK_PROTECTION_MECHANISM) ?: 0
    val names1 = listOf("允许解锁", "提示", "禁止解锁")
    val names2 = listOf("关闭", "警告", "悬停", "返航", "降落")
    val names3 = listOf("关闭", "警告", "悬停", "返航", "降落", "迫降")
    val values1 = listOf(0, 1, 2)
    val values2 = listOf(0, 1, 2, 3, 4)
    val values3 = listOf(0, 1, 2, 3, 4, 5)
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        frameColumn {
            GroupButtonRow(
                title = "固定解-允许解锁",
                items = names1, indexes = values1, number = parseProtectionMode(number, ProtectionType.FIXED_UNLOCK)
            ) {
                sendParameter(VKAg.APTYPE_RTK_PROTECTION_MECHANISM, setProtectionMode(number, ProtectionType.FIXED_UNLOCK, it).toFloat())
            }
            GroupButtonRow(
                title = "固定解-丢失保护",
                items = names3, indexes = values3, number = parseProtectionMode(number, ProtectionType.FIXED_LOST)
            ) {
                sendParameter(VKAg.APTYPE_RTK_PROTECTION_MECHANISM, setProtectionMode(number, ProtectionType.FIXED_LOST, it).toFloat())
            }
            GroupButtonRow(
                title = "测向-允许解锁",
                items = names1, indexes = values1, number = parseProtectionMode(number, ProtectionType.DIRECTION_UNLOCK)
            ) {
                sendParameter(VKAg.APTYPE_RTK_PROTECTION_MECHANISM, setProtectionMode(number, ProtectionType.DIRECTION_UNLOCK, it).toFloat())
            }
            GroupButtonRow(
                title = "测向-丢失保护",
                items = names2, indexes = values2, number = parseProtectionMode(number, ProtectionType.DIRECTION_LOST)
            ) {
                sendParameter(VKAg.APTYPE_RTK_PROTECTION_MECHANISM, setProtectionMode(number, ProtectionType.DIRECTION_LOST, it).toFloat())

            }
        }
    }
}
/**
 * 一键保存/导入/定位增强
 */
@Composable
private fun OperationButtons(
    aptypeData: VKAg.APTYPEData?, factoryModel: FactoryModel,
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val buttonWidth = 100.dp
    var positionSwitch = aptypeData?.getIntValue(53) ?: -1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        //定位增强
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.positioning_enhancement),
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            SwitchButton(
                defaultChecked = positionSwitch > 0,
                width = 70.dp,
                height = 30.dp,
                onCheckedChange = {
                    positionSwitch = if (it) 1 else 0
                    sendIndexedParameter(53, positionSwitch)
                },
                backgroundColors = listOf(
                    extendedColors.buttonDisabled, MaterialTheme.colorScheme.primary
                ),
                switchColors = listOf(
                    MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        //一键保存
        Button(
            onClick = {
                context.showDialog {
                    InputPopup(
                        title = stringResource(id = R.string.ver_param_file_name),
                        hint = stringResource(id = R.string.ver_param_fn_example),
                        onConfirm = { text ->
                            DroneModel.activeDrone?.let { drone ->
                                factoryModel.uploadAndDownloadEnabled = false
                                context.startProgress(
                                    ParamTool.DownloadParamTask(
                                        drone, factoryModel.getParamFile(text), text
                                    )
                                )
                            }
                            context.hideDialog()
                        },
                        onDismiss = {
                            context.hideDialog()
                        })
                }
            },
            modifier = Modifier
                .width(buttonWidth)
                .height(30.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = factoryModel.uploadAndDownloadEnabled
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.one_click_save),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall
            )
        }
        //一键导入
        Button(
            onClick = {
                //获取下载的参数列表
                factoryModel.uploadParam { success ->
                    //成功获取数据
                    if (success) {
                        (context as FactoryActivity).apply {
                            showDialog {
                                ListSelectionPopup(list = factoryModel.paramList, item = { item ->
                                    //参数名
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AutoScrollingText(
                                            text = item.paramName,
                                            style = MaterialTheme.typography.titleSmall,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    //删除图标
                                    Box(
                                        modifier = Modifier
                                            .weight(0.2f)
                                            .fillMaxHeight()
                                            .clickable {
                                                factoryModel.deleteTemplate(item.paramId)
                                            }, contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "delete param"
                                        )
                                    }
                                }, onConfirm = { _, selectedItems ->
                                    val item = selectedItems[0]
                                    factoryModel.uploadAndDownloadEnabled = false
                                    factoryModel.uploadParamToFCU(
                                        paramId = item.paramId
                                    ) { droneParam ->
                                        DroneModel.activeDrone?.let {
                                            context.startProgress(
                                                ParamTool.UploadParamTask(
                                                    it,
                                                    factoryModel.getParamFile(item.paramName),
                                                    droneParam.param!!
                                                )
                                            )
                                        }
                                    }
                                    hideDialog()
                                }, onDismiss = {
                                    hideDialog()
                                })
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .width(buttonWidth)
                .height(
                    30.dp
                ),
            contentPadding = PaddingValues(0.dp),
            enabled = factoryModel.uploadAndDownloadEnabled
        ) {
            AutoScrollingText(
                text = stringResource(id = R.string.one_click_import),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/**
 * 感度设置
 *
 * @param text 标题文本
 * @param value 当前值
 * @param min 最小值
 * @param max 最大值
 * @param tipString 提示文本
 * @param onChange 变更回调
 */
@Composable
private fun SensitivitySetting(
    text: String,
    value: Float,
    min: Float,
    max: Float,
    tipString: String,
    onChange: (Float) -> Unit,
) {
    val context = LocalContext.current
    val act = context as FactoryActivity
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            FactorySliderAskCounter(
                title = text,
                number = value,
                min = min,
                max = max,
                onConfirm = {
                    onChange(it)
                    act.hideDialog()
                },
                onDismiss = {
                    act.hideDialog()
                })
        }
        VerticalDivider(
            thickness = 1.dp, color = Color.Gray, modifier = Modifier.height(50.dp)
        )
        Box(
            modifier = Modifier
                .weight(0.4f)
                .height(40.dp)
        ) {
            Text(
                text = tipString,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }
}

/**
 * APTYPE
 *
 * @param text 标题文本
 * @param value 当前值
 * @param min 最小值
 * @param max 最大值
 * @param onChange 变更回调
 */
@Composable
private fun ParamSetting(
    text: String,
    value: Float,
    min: Float,
    max: Float,
    onChange: (Float) -> Unit,
) {
    val context = LocalContext.current
    val act = context as FactoryActivity
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FactorySliderAskCounter(
            title = text,
            number = value,
            min = min,
            max = max,
            onConfirm = {
                onChange(it)
                act.hideDialog()
            },
            onDismiss = {
                act.hideDialog()
            })
    }
}

val frameColumn = @Composable { content: @Composable () -> Unit ->
    Row(
        modifier = Modifier
            .border(
                width = 1.dp, color = Color.Gray, shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}