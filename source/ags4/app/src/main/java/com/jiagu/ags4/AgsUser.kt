package com.jiagu.ags4

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jiagu.ags4.bean.AllFirm
import com.jiagu.ags4.bean.UserInfo
import com.jiagu.ags4.repo.net.model.Group

object AgsUser {
    var userInfo: UserInfo? by mutableStateOf(null)

    var appVersion = 0
    var appChangeLog = ""
    var allFirm: AllFirm? = null
    var firmPrefix = "ags4-vk"

    // v9
    var pmuVersion = 0
    var fmuVersion = 0
    var radarVersionV9 = 0
    var fRadarVersionV9 = 0
    var bRadarVersionV9 = 0

    var showFirmUpdate = false

    var openDroneType = true

    var netIsConnect = false

    var flavor = ""
    //当前团队 仅展示用
    var workGroup: Group? = null

    fun clearUser() {
        userInfo = null
        workGroup = null
    }

    fun setUser(newUser: UserInfo) {
        userInfo = newUser
    }
}
