package com.jiagu.ags4.scene.factory

import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BaseComponentActivity
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.net.model.AppUIConfig
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.utils.LocalBtDeviceModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.ext.toast
import com.jiagu.api.ext.toastLong
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.device.controller.Controller.Companion.CONNECTED
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.VKAuthTool
import com.jiagu.jgcompose.button.TopBarBottom
import com.jiagu.jgcompose.container.MainContent
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.popup.WarningPopup
import com.jiagu.tools.http.FileDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.exitProcess
import android.graphics.Color as AndroidColor


class FactoryActivity : BaseComponentActivity() {
    private val factoryModel: FactoryModel by viewModels()
    private val btDeviceModel: BtDeviceModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DroneModel.readControllerParam()
        DroneModel.activeDrone?.getChannelMapping()
        DroneModel.activeDrone?.getParameters()
        DroneModel.activeDrone?.getPidParameters()
        addObserver()
    }

    private fun downloadFiles(it: AppUIConfig) {
        val downloader = FileDownloader()
        val dir = File(
            Environment.getExternalStorageDirectory(), "ags4Config"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val theme = File(dir, "theme.json")
        if (theme.exists()) {
            theme.delete()
        }
        Gson().toJson(it).let { json ->
            theme.writeText(json)
        }
        val splash = File(dir, "splash.png")
        val bg = File(dir, "bg.png")
        val splashVideo = File(dir, "splash.mp4")
        if (it.launchPageUrl != null) {
            if (splashVideo.exists()) {
                splashVideo.delete()
            }
            if (splash.exists()) {
                splash.delete()
            }
            downloader.downloadWithoutLength(
                it.launchPageUrl, if (it.launchPageUrl.endsWith("mp4")) splashVideo else splash
            ) {}
        }
        if (it.homePageUrl != null) {
            if (bg.exists()) {
                bg.delete()
            }
            downloader.downloadWithoutLength(it.homePageUrl, bg) {}
        }
    }

    @Composable
    override fun Content() = FactoryModeScreen()

    @Composable
    fun FactoryModeScreen() {
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalFactoryModel provides factoryModel,
            LocalBtDeviceModel provides btDeviceModel
        ) {
            val title = when (factoryModel.selectedMenuId) {
                FactoryTypeEnum.FACTORY_TYPE_MODEL -> R.string.factory_settings_model
                FactoryTypeEnum.FACTORY_TYPE_INSTALL -> R.string.factory_settings_install
                FactoryTypeEnum.FACTORY_TYPE_PARAMETER -> R.string.factory_settings_parameter
                FactoryTypeEnum.FACTORY_TYPE_RC -> R.string.factory_settings_rc
                FactoryTypeEnum.FACTORY_TYPE_MOTOR -> R.string.factory_settings_electrical_machinery
            }
            MainContent(title = stringResource(id = title), barAction = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    //界面初始化
                    TopBarBottom(
                        stringResource(id = R.string.ui_init), MaterialTheme.colorScheme.primary
                    ) {
                        context.showDialog {
                            PromptPopup(
                                content = stringResource(id = R.string.ui_init),
                                onConfirm = {
                                    AgsUser.userInfo?.accountId?.let {
                                        lifecycleScope.launch {
                                            AgsNet.getAppUIConfig(it).networkFlow {
                                                toast(it)
                                            }.collectLatest {
                                                if (it.theme != null) {
                                                    AppConfig(context).primaryColor =
                                                        Color(AndroidColor.parseColor(it.theme))
                                                }

                                                withContext(Dispatchers.Main) {
                                                    toastLong(R.string.success)
                                                }
                                                withContext(Dispatchers.IO) {
                                                    downloadFiles(it)
                                                }
                                                (context as FactoryActivity).finishAffinity()
                                                exitProcess(0)
                                            }
                                        }
                                    }
                                    context.hideDialog()
                                },
                                onDismiss = {
                                    context.hideDialog()
                                })
                        }
                    }
                    //恢复出厂
                    val scope = rememberCoroutineScope()
                    TopBarBottom(
                        stringResource(id = R.string.restore_factory_settings),
                        MaterialTheme.colorScheme.error
                    ) {
                        context.showDialog {
                            WarningPopup(
                                content = stringResource(id = R.string.restore_factory_settings_tip),
                                onConfirm = {
                                    scope.launch {
                                        for (i in 0..6) {
                                            DroneModel.activeDrone?.factoryReset()
                                            delay(300)
                                        }
                                        context.hideDialog()
                                    }
                                },
                                onDismiss = {
                                    context.hideDialog()
                                })
                        }
                    }
                }
            }, breakAction = {
                finish()
            }) {
                FactorySettings()
            }
        }
    }

    private fun addObserver() {
        var getControlMapping = true
        DroneModel.rcafData.observe(this) {
            getControlMapping = false
        }
        DroneModel.controllerConnectionState.observe(this) {
            if (ControllerFactory.deviceModel == "PHONE" && it == CONNECTED) {
                lifecycleScope.launch {
                    while (getControlMapping) {
                        DroneModel.activeDrone?.getChannelMapping()
                        delay(1000)
                    }
                }
            }
        }
    }
}

fun sendIndexedParameter(pid: Int, value: Int) {
    DroneModel.activeDrone?.sendIndexedParameter(pid, value)
}

fun sendPidParameter(pid: Int, value: Int) {
    DroneModel.activeDrone?.sendPidParameter(pid, value)
}

fun sendParameter(pid: Int, value: Float) {
    DroneModel.activeDrone?.sendParameter(pid, value)
}

