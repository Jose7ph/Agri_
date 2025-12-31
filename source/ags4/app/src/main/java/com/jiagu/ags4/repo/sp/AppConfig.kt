package com.jiagu.ags4.repo.sp

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jiagu.ags4.BuildConfig
import com.jiagu.tools.ext.UnitHelper

class AppConfig(context: Context) {
    private val pref = context.applicationContext.getSharedPreferences("app_conf", Context.MODE_PRIVATE)

    var device: String
        get() = pref.getString("device", "")!!
        set(value) = pref.edit().putString("device", value).apply()

    var areaUnit: Int
        get() = pref.getInt("area", UnitHelper.defaultArea())
        set(value) = pref.edit().putInt("area", value).apply()

    var lengthUnit: Int
        get() = pref.getInt("length", UnitHelper.defaultLength())
        set(value) = pref.edit().putInt("length", value).apply()

    var capacityUnit: Int
        get() = pref.getInt("capacity", UnitHelper.defaultCapacity())
        set(value) = pref.edit().putInt("capacity", value).apply()

    var weightUnit: Int
        get() = pref.getInt("weight", UnitHelper.defaultWeight())
        set(value) = pref.edit().putInt("weight", value).apply()

    var voice: Int
        get() = pref.getInt("voice", 1)
        set(value) = pref.edit().putInt("voice", value).apply()

    var mapProvider: String
        get() = pref.getString("map", "mapbox")!!
        set(value) = pref.edit().putString("map", value).apply()

    var mapProviderInt: Int
        get() = mapToInt(mapProvider)
        set(value) = pref.edit().putString("map", intToMap(value)).apply()

    var mapUrl: String
        get() = pref.getString("mapurl", "")!!
        set(value) = pref.edit().putString("mapurl", value).apply()

    var mapLevel: Int
        get() = pref.getInt("maplv", 22)
        set(value) = pref.edit().putInt("maplv", value).apply()

    private fun mapToInt(type: String): Int {
        return when (type) {
            "naver" -> 1
            "google" -> 2
            "custom" -> 3
            else -> 0
        }
    }

    private fun intToMap(type: Int): String {
        return when (type) {
            1 -> "naver"
            2 -> "google"
            3 -> "custom"
            else -> "mapbox"
        }
    }

//    var curMaterialId: Long
//        get() = pref.getLong("curMaterialId", 0)
//        set(value) = pref.edit().putLong("curMaterialId", value).apply()

    var primaryColor: Color
        get() = Color(pref.getInt("primaryColor", BuildConfig.DEFAULT_PRIMARY_COLOR))
        set(value) = pref.edit().putInt("primaryColor", value.toArgb()).apply()
}