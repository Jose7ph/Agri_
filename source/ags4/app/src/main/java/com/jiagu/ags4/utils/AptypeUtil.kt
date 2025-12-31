package com.jiagu.ags4.utils

import android.app.Application
import android.util.Log
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.tools.utils.NumberUtil
import java.math.RoundingMode


//Created by gengmeng on 2/10/23.

object AptypeUtil {

    private var droneType = -1
    private var abWidth: Float = 4f
    private var abSpeed: Float = 4f
    private var maxSpeed: Float = 10f
    private var pumpMode: Int = VKAg.BUMP_MODE_NONE
    private var pumpAndValve: Float = 50f
    private var seedSpeedAndCen: Float = 50f
    private var sprayMu: Float = 1000f
    private lateinit var app: Application
    private lateinit var config: Config
    //请求ab data数据
    private var needABData = false
    fun init(application: Application) {
        app = application
        config = Config(app)
    }

    fun setAPTypeData(data: VKAg.APTYPEData) {
        droneType = data.getIntValue(VKAg.APTYPE_DRONE_TYPE)
        pumpAndValve = data.getValue(VKAg.APTYPE_PUMP_FIXED_VALUE)//水泵值/阀门大小
        seedSpeedAndCen = data.getValue(VKAg.APTYPE_CENTRIFUGAL_SIZE)//离心喷头大小/轮盘转速
        sprayMu = data.getValue(VKAg.APTYPE_MUYONGLIANG) * 1000
        pumpMode = data.getIntValue(VKAg.APTYPE_PUMP_MODE)
        abSpeed = data.getValue(VKAg.APTYPE_AB_MAX_SPEED)
        maxSpeed = data.getValue(VKAg.APTYPE_MAX_HSPEED)
        Log.v("shero", "水泵/阀门大小:$pumpAndValve 离心喷头大小/轮盘转速:$seedSpeedAndCen " +
                "水泵模式(1-% 2-L/mu):$pumpMode " +
                "AB宽度:$abWidth AB速度:$abSpeed 最大速度:$maxSpeed 亩用量:$sprayMu")

        //AB点模式中，当width 发生变化，根据状态判断是否需要请求ab data数据，主要用于在空中修改拢距
        val curABWidth = data.getValue(VKAg.APTYPE_AB_WIDTH)
        if(abWidth != curABWidth && needABData){
            needABData= false
            DroneModel.activeDrone?.getABData()
        }
        abWidth = curABWidth
    }

    fun getPumpMode(): Int {
        return pumpMode
    }

    fun setPumpMode(mode: Int) {
        DroneModel.activeDrone?.setPumpMode(mode)
    }

    fun getABSpeed(): Float {
        return abSpeed
    }

    fun setABWidth(value: Float) {
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_AB_WIDTH, value)
        needABData = true
    }

    fun setABSpeed(value: Float) {
        var speed = NumberUtil.round(value, 0, RoundingMode.UP).toFloat()
        if (speed > 13.8f) speed = 13.8f
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_AB_MAX_SPEED, value)
        if (maxSpeed < speed) setMaxSpeed(speed)
    }

    fun setMaxSpeed(value: Float) {
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_MAX_HSPEED, value)
    }

    fun setSprayMu(v: Float) {//34
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_MUYONGLIANG, v / 1000f)
    }

    fun getSprayMu(): Float {
        return sprayMu
    }

    fun getPumpAndValve(): Float {
        return pumpAndValve
    }

    fun getCenAndSeedSpeed(): Float {
        return seedSpeedAndCen
    }

    fun getDroneType(): Int {
        return droneType
    }

    fun setPumpAndValve(v: Float) {//28
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_PUMP_FIXED_VALUE, v / 100f)
    }

    fun setCenAndSeedSpeed(v: Float) {//43
        DroneModel.activeDrone?.sendParameter(VKAg.APTYPE_CENTRIFUGAL_SIZE, v / 100f)
    }
}