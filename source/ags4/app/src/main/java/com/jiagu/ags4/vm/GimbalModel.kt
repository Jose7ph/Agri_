package com.jiagu.ags4.vm

import android.content.Context
import com.jiagu.ags4.bean.RtspEnum
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.device.controller.IController
import com.jiagu.device.gimbal.IGimbal
import com.jiagu.device.gimbal.SiYiGimbal

object GimbalModel {
    private var gimbal: IGimbal? = null
    private var pitchKey: String? = null
    private var yawKey: String? = null
    private var pitchStop = true
    private var yawStop = true
    fun initGimbal(ctx: Context) {
        val config = DeviceConfig(ctx)
        when (config.rtspType) {
            RtspEnum.SIYI_A2_MINI.key -> gimbal = SiYiGimbal()
        }

        pitchKey = config.gimbalControlUpDown
        yawKey = config.gimbalControlLeftRight
        DroneModel.pushButtonHandler(gimbalCtrl)
    }

    private val gimbalCtrl by lazy { object: IController.ButtonHandler {
        override fun onButton(key: String, lastValue: Int, value: Int): Boolean {
            return when (key) {
                pitchKey -> {
                    val speed = (value - 1500) / 25f
                    when {
                        value > 1550 -> {
                            pitchStop = false
                            gimbalMove(0f, speed, 0f)
                        }
                        value < 1450 -> {
                            pitchStop = false
                            gimbalMove(0f, speed, 0f)
                        }
                        else -> if (!pitchStop) {
                            pitchStop = true
                            gimbalCtrl(IGimbal.CTL_STOP)
                        }
                    }
                    true
                }
                yawKey -> {
                    val speed = (value - 1500) / 25f
                    when {
                        value > 1550 -> {
                            yawStop = false
                            gimbalMove(speed, 0f, 0f)
                        }
                        value < 1450 -> {
                            yawStop = false
                            gimbalMove(speed, 0f, 0f)
                        }
                        else -> if (!yawStop) {
                            yawStop = true
                            gimbalCtrl(IGimbal.CTL_STOP)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }}

    fun gimbalMove(yaw: Float, pitch: Float, roll: Float) {
        gimbal?.gimbalMove(yaw, pitch, roll)
    }

    fun gimbalCtrl(cmd: Int) {
        gimbal?.gimbalCtrl(cmd)
    }
}