package com.jiagu.ags4.scene.work.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.scene.work.MapVideoModel
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.startProgress
import com.jiagu.ags4.voice.VoiceService
import com.jiagu.api.ext.toast
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.ScreenPopup
import com.jiagu.tools.vm.VoiceTask
import kotlinx.coroutines.launch

/**
 * 其他设置
 */
@Composable
fun OtherSettings(modifier: Modifier = Modifier, vm: MapVideoModel) {
    val progressModel = LocalProgressModel.current
    val context = LocalContext.current
    val appConfig = AppConfig(context)
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(settingsGlobalColumnSpacer)
    ) {
        //实时参数
        item {
            JumpButtonRow(title = R.string.real_time_parameters, onClick = {
                context.showDialog {
                    RealTimeParam(onClose = {
                        context.hideDialog()
                    })
                }
            })
        }
        //语音提示
        item {
            val names = listOf(
                stringResource(id = R.string.close),
                stringResource(id = R.string.open),
            )
            val values = mutableListOf<Int>()
            for (i in names.indices) {
                values.add(i)
            }
            GroupButtonRow(
                title = R.string.voice_prompts,
                defaultNumber = appConfig.voice,
                names = names,
                values = values
            ) {
                appConfig.voice = it
                if (it == 1) {
                    VoiceService.start(context)
                } else {
                    VoiceService.stop()
                }
            }
        }

        item {
            SingleButtonRow(title = R.string.reset_voice, buttonText = R.string.reset, onClick = {
                context.showDialog {
                    ScreenPopup(
                        content = {
                            Box(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = stringResource(id = R.string.confirm_reset_voice),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }, showCancel = true, showConfirm = true,
                        onConfirm = {
                            coroutineScope.launch {
                                AppConfig(context).voice = 0
                                VoiceTask.resetVoice(context)
                                context.toast(R.string.success)
                                progressModel.clearData().apply {
                                    context.hideDialog()
                                    context.startProgress(VoiceTask())
                                }
                            }
                        },
                        onDismiss = {
                            context.hideDialog()
                        }
                    )
                }
            })
        }
    }
}
