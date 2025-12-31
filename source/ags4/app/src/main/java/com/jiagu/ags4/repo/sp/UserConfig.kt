package com.jiagu.ags4.repo.sp

import android.content.Context
import android.text.TextUtils
import com.google.gson.Gson
import com.jiagu.ags4.bean.UserInfo

class UserConfig(context: Context) {
    private val pref =
        context.applicationContext.getSharedPreferences("user_conf", Context.MODE_PRIVATE)

    var account: String
        get() = pref.getString("account", "")!!
        set(value) = pref.edit().putString("account", value).apply()

    var group: Long
        get() = pref.getLong("group", 0)
        set(value) = pref.edit().putLong("group", value).apply()

    var user: UserInfo?
        get() {
            val str = pref.getString("user", "")!!
            if (TextUtils.isEmpty(str)) return null
            return try {
                Gson().fromJson(str, UserInfo::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        set(value) {
            if (value == null) {
                pref.edit().remove("user").apply()
            } else {
                try {
                    val str = Gson().toJson(value)
                    pref.edit().putString("user", str).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    //身份认证列表 key:userId, value:认证结果
    private var identityList: HashMap<String, Boolean>
        get() {
            val identityList = pref.getString("identityList", "")!!
            return if (identityList.isBlank()) HashMap()
            else Gson().fromJson<HashMap<String, Boolean>>(identityList, HashMap::class.java)
        }
        set(value) {
            pref.edit().putString("identityList", Gson().toJson(value)).apply()
        }

    fun setIdentity(userId: String) {
        val list = identityList
        list[userId] = true
        identityList = list
    }

    fun getIdentity(userId: String) = identityList[userId] ?: false
}