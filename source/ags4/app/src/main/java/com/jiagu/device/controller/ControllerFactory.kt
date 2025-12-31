package com.jiagu.device.controller

import android.content.Context
import android.os.Build
import android.util.Log
import com.jiagu.api.helper.LogFileHelper

object ControllerFactory {

    private var controllerModel = ""

    fun dumpModel() {
        Log.d("yuhang", "MANUFACTURER: ${Build.MANUFACTURER}")
        Log.d("yuhang", "PRODUCT: ${Build.PRODUCT}")
        Log.d("yuhang", "MODEL: ${Build.MODEL}")
        Log.d("yuhang", "VERSION: ${Build.VERSION.SDK_INT}")
        Log.d("yuhang", "ABI: ${Build.SUPPORTED_ABIS.joinToString(",")}")
        Log.d("yuhang", "CONTROLLER: ${deviceModel}")
        LogFileHelper.log("MANUFACTURER: ${Build.MANUFACTURER}")
        LogFileHelper.log("PRODUCT: ${Build.PRODUCT}")
        LogFileHelper.log("MODEL: ${Build.MODEL}")
        LogFileHelper.log("VERSION: ${Build.VERSION.SDK_INT}")
        LogFileHelper.log("ABI: ${Build.SUPPORTED_ABIS.joinToString(",")}")
        LogFileHelper.log("CONTROLLER: ${deviceModel}")
    }

    private val controllerCreators = mutableMapOf<String, (IController.Listener, String) -> Controller>()
    fun registerController(model: String, creator: (IController.Listener, String) -> Controller) {
        controllerCreators[model] = creator
    }

    fun registerController(model: Array<String>, creator: (IController.Listener, String) -> Controller) {
        model.forEach { controllerCreators[it] = creator }
    }

    private val videoCreators = mutableMapOf<String, (Context, String) -> IVideo>()
    fun registerVideo(model: String, creator: (Context, String) -> IVideo) {
        videoCreators[model] = creator
    }

    fun registerVideo(model: Array<String>, creator: (Context, String) -> IVideo) {
        model.forEach { videoCreators[it] = creator }
    }

    fun registerBarePhone() {
        registerController("PHONE") { l, _ -> PhoneController(l) }
        registerVideo("PHONE") { c, _ -> PhoneVideo(c) }
    }

    val deviceModel: String
        get() {
            if (controllerModel.isNotBlank()) return controllerModel
            controllerModel = when (Build.PRODUCT) {
                "msm8953_64" -> when (Build.MODEL) {
                    "MK15", "MK32" -> "MK15"
                    "S1" -> "S1"
                    "S2" -> "S2"
                    else -> "H12"
                }
                "bengal_515", "G20" -> "G20"
                "H30", "ec10" -> "H30"
                "G12" -> "G12"
                "trinket" -> when (Build.MODEL) {
                    "Standard_94" -> "UNIRC7"
                    else -> "UNIRC7"
                }
                "H12" -> "H12"
                "H12Pro" -> "H12Pro"
                "H20" -> "H20"
                "arowana-rc" -> "H16"
                "EAV-RC50" -> "EAV-RC50"
                else -> when (Build.MODEL) {
                    "EAV-RC50" -> "EAV-RC50"
                    else -> "PHONE"
                }
            }
            return controllerModel
        }
    var rtspUrl = ""
    var rtspUrl2 = ""

    fun createController(l: IController.Listener): Controller {
        val creator = controllerCreators[deviceModel] ?: controllerCreators["PHONE"] ?: throw Throwable("NO CONTROLLER registered ($deviceModel)")
        return creator(l, deviceModel)
    }

    fun createVideo(ctx: Context): IVideo {
        val creator = videoCreators[deviceModel] ?: videoCreators["PHONE"] ?: throw Throwable("NO CONTROLLER registered ($deviceModel)")
        return creator(ctx, deviceModel)
    }
    fun createVideo2(ctx: Context): IVideo {
        val creator = videoCreators["$deviceModel-2"] ?: videoCreators["PHONE"] ?: throw Throwable("NO CONTROLLER registered ($deviceModel)")
        return creator(ctx, "$deviceModel-2")
    }
}
