package com.jiagu.ags4.scene.device


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.checkGNSS
import com.jiagu.ags4.utils.filterDeviceByTypes
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import androidx.compose.material3.Button
import androidx.compose.material3.Text


@Composable
fun DeviceGNSS() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val progressModel = LocalProgressModel.current
    val deviceList by DroneModel.deviceList.observeAsState()
    val progress by progressModel.progress.observeAsState()
    val gnssList =
        filterDeviceByTypes(idListData = deviceList, filterNum = listOf(VKAgCmd.DEVINFO_GNSS))



    when (progress) {
        is ProgressModel.ProgressMessage -> {
            val message = progress as ProgressModel.ProgressMessage
            context.showDialog {
                PromptPopup(content = message.text,
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
                PromptPopup(content = notice.title,
                    onConfirm = { progressModel.next(1) },
                    onDismiss = {
                        progressModel.next(0)
                        context.hideDialog()
                    })
            }
        }

        is ProgressModel.ProgressResult -> {
            val result = progress as ProgressModel.ProgressResult
            val processed = (context as DeviceManagementActivity).taskComplete?.invoke(result.success, result.msg) ?: false
            if (!processed && result.msg != null) {
                context.toast(result.msg!!)
            }
            context.hideDialog()
            progressModel.done()
        }
    }
    MainContent(title = stringResource(id = R.string.device_management_gnss),
        barAction = {},
        breakAction = {
            navController.popBackStack()
        }) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (gnssList.containsKey(VKAgCmd.DEVINFO_GNSS)) {
                val gnss = gnssList[VKAgCmd.DEVINFO_GNSS]
                var gnssA: VKAg.IDListData? = null
                var gnssB: VKAg.IDListData? = null
                gnss?.let {
                    gnssA = it[0]
                    if (it.size > 1) {
                        gnssB = it[1]
                    }
                }
                //GNSS-A
                gnssA?.let {
                    CardFrame(modifier = Modifier.weight(1f),
                        title = "GNSS-A",
                        sn = it.hwId,
                        version = it.swId,
                        firmwareType = FirmwareTypeEnum.GPS1,
                        upgrade = true,
                        content = {
                            CardUpgradeTextRow(
                                title = stringResource(id = R.string.device_details_serial_number),
                                text = gnssA!!.hwId,
                            )
                            CardUpgradeTextRow(
                                title = stringResource(id = R.string.device_details_version),
                                text = gnssA!!.swId,
                                upgrade = checkGNSS(it.swId)
                            )
                        })
                }
                //GNSS-B
                if (gnssB != null) {
                    gnssB?.let {
                        CardFrame(modifier = Modifier.weight(1f),
                            title = "GNSS-B",
                            sn = it.hwId,
                            version = it.swId,
                            firmwareType = FirmwareTypeEnum.GPS2,
                            upgrade = true,
                            content = {
                                CardUpgradeTextRow(
                                    title = stringResource(id = R.string.device_details_serial_number),
                                    text = it.hwId,
                                )
                                CardUpgradeTextRow(
                                    title = stringResource(id = R.string.device_details_version),
                                    text = it.swId,
                                    upgrade = checkGNSS(it.swId)
                                )
                            })
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

            }else {
                CardFrame(modifier = Modifier.weight(1f),
                    title = "GNSS-A",
                    firmwareType = FirmwareTypeEnum.GPS1,
                    upgrade = true,
                    content = {
                        CardUpgradeTextRow(
                            title = stringResource(id = R.string.device_details_serial_number),
                            text = "",
                        )
                        CardUpgradeTextRow(
                            title = stringResource(id = R.string.device_details_version),
                            text = "",
                        )
                    })
            }
        }
    }
}
