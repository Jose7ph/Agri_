package com.jiagu.ags4.repo.sp

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.MeasureBlock
import com.jiagu.ags4.bean.RtcmInfo
import com.jiagu.ags4.bean.WorkBlockInfo

class Config(context: Context) {

    private val pref = context.getSharedPreferences("configAGS4", Context.MODE_PRIVATE)

    var block: MeasureBlock?
        get() {
            val text = pref.getString("block", "")!!
            return if (text.isBlank()) null
            else MeasureBlock.fromString(text)
        }
        set(value) {
            val text = value?.stringify() ?: ""
            pref.edit().putString("block", text).apply()
        }

    var ntripAccount: RtcmInfo?
        //重联时用的
        get() {
            val qxInfo = pref.getString("ntripAccount", "")!!
            return if (qxInfo == "") null else Gson().fromJson(qxInfo, RtcmInfo::class.java)
        }
        set(value) {
            pref.edit().putString("ntripAccount", if (value == null) "" else Gson().toJson(value))
                .apply()
        }

    var ntripLastConnectTime: Long
        //记录最后一次连接ntrip的时间
        get() = pref.getLong("ntripLastConnectTime", 0L)
        set(value) = pref.edit().putLong("ntripLastConnectTime", value).apply()

    var ntrip: String
        //ntrip信息：帐号 密码
        get() = pref.getString("ntrip", "")!!
        set(value) = pref.edit().putString("ntrip", value).apply()

    var rtkBase: String
        get() = pref.getString("rtkBase", "")!!
        set(value) = pref.edit().putString("rtkBase", value).apply()

    var locationType: String
        get() = pref.getString("locationType", "map")!!
        set(value) = pref.edit().putString("locationType", value).apply()

    var workHeight: Float
        get() = pref.getFloat("workHeight", 2.0f)
        set(value) = pref.edit().putFloat("workHeight", value).apply()

    var workSpeed: Float
        get() = pref.getFloat("workSpeed", 5.0f)
        set(value) = pref.edit().putFloat("workSpeed", value).apply()

    var cleanSpeed: Float
        get() = pref.getFloat("cleanSpeed", 5.0f)
        set(value) = pref.edit().putFloat("cleanSpeed", value).apply()

    var workRidge: Float
        get() = pref.getFloat("workRidge", 4.0f)
        set(value) = pref.edit().putFloat("workRidge", value).apply()

    var safetyDistance: Float
        get() = pref.getFloat("safetyDistance", 5.0f)
        set(value) = pref.edit().putFloat("safetyDistance", value).apply()

    var safetyDistanceBarrier: Float
        get() = pref.getFloat("safetyDistanceBarrier", 3.0f)
        set(value) = pref.edit().putFloat("safetyDistanceBarrier", value).apply()

    var poleRadius: Float
        get() = pref.getFloat("poleRadius", 5f)
        set(value) = pref.edit().putFloat("poleRadius", value).apply()

    var phoneAccuracy: Int
        get() = pref.getInt("phoneAccuracy", 20)
        set(value) = pref.edit().putInt("phoneAccuracy", value).apply()

    var locator: String
        get() = pref.getString("locator", "")!!
        set(value) = pref.edit().putString("locator", value).apply()

//    var smartPlanQty: Float
//        get() = pref.getFloat("smartPlanQty", 0.0f)
//        set(value) = pref.edit().putFloat("smartPlanQty", value).apply()

    var smartPlanDist: Int
        get() = pref.getInt("smartPlanDist", 0)
        set(value) = pref.edit().putInt("smartPlanDist", value).apply()

    var smartPlan: Boolean
        get() = pref.getBoolean("smartPlan", false)
        set(value) = pref.edit().putBoolean("smartPlan", value).apply()

    var rtkType: Int
        get() = pref.getInt("rtkType", 0)
        set(value) = pref.edit().putInt("rtkType", value).apply()

    var qxSwitch: Int
        get() = pref.getInt("qxSwitch", Constants.SIM_CLOSE)
        set(value) = pref.edit().putInt("qxSwitch", value).apply()

    var workBlockInfo: WorkBlockInfo?
        get() {
            val text = pref.getString("workBlockInfo", "")!!
            return if (text.isBlank()) null
            else Gson().fromJson(text, WorkBlockInfo::class.java)
        }
        set(value) {
            val text = if (value == null) "" else Gson().toJson(value)
            pref.edit().putString("workBlockInfo", text).apply()
        }

    var fenceRadius: Long
        get() = pref.getLong("fenceRadius", 50)
        set(value) = pref.edit().putLong("fenceRadius", value).apply()

    var autoCheckUpgrade: Boolean
        get() = pref.getBoolean("autoCheckUpgrade", true)
        set(value) = pref.edit() { putBoolean("autoCheckUpgrade", value) }
    //自动连接usb rtk
    var autoUsbRTKConnect: Boolean
        get() = pref.getBoolean("autoUsbRTKConnect", false)
        set(value) = pref.edit() { putBoolean("autoUsbRTKConnect", value) }
}