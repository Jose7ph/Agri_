package com.jiagu.ags4.repo.sp

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DeviceConfig(context: Context) {
    private val pref = context.applicationContext.getSharedPreferences("device", Context.MODE_PRIVATE)

    var h16uart: Int
        get() = pref.getInt("h16uart", 1)
        set(value) = pref.edit().putInt("h16uart", value).apply()

    var rtspType: String
        get() = pref.getString("rtsptype", "")!!
        set(value) = pref.edit().putString("rtsptype", value).apply()

    var customUrl: String
        get() = pref.getString("customurl", "")!!
        set(value) = pref.edit().putString("customurl", value).apply()

    var gimbalCount: Int
        get() = pref.getInt("gimbalCount", 1)
        set(value) = pref.edit().putInt("gimbalCount", value).apply()

    var rtspurl: String
        get() = pref.getString("rtspurl", "")!!
        set(value) = pref.edit().putString("rtspurl", value).apply()
    var rtspurl2: String
        get() = pref.getString("rtspurl2", "")!!
        set(value) = pref.edit().putString("rtspurl2", value).apply()

    var gimbalControlUpDown: String
        get() = pref.getString("gimbalControlUpDown", "N/A")!!
        set(value) = pref.edit().putString("gimbalControlUpDown", value).apply()

    var gimbalControlLeftRight: String
        get() = pref.getString("gimbalControlLeftRight", "N/A")!!
        set(value) = pref.edit().putString("gimbalControlLeftRight", value).apply()

    fun getDroneLock(droneId: String): Int {
        val key = "lock_$droneId"
        return pref.getInt(key, 1)
    }

    fun setDroneLock(droneId: String, lock: Int) {
        val key = "lock_$droneId"
        pref.edit().putInt(key, lock).apply()
    }
    @Keep
    class StringHashMapTypeToken : TypeToken<HashMap<String, String>>()

    var rackNoMap: HashMap<String, String>?
        get() {
            val text = pref.getString("rackNoMap", "")!!
            return if (text.isBlank()) null
            else Gson().fromJson(text, StringHashMapTypeToken().type)
        }
        set(value) {
            pref.edit().putString("rackNoMap", if (value == null) "" else Gson().toJson(value)).apply()
        }
}