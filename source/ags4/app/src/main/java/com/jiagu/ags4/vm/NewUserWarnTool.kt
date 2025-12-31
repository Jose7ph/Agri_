package com.jiagu.ags4.vm

import com.jiagu.device.vkprotocol.NewWarnTool


//Created by gengmeng on 7/25/24.

object NewUserWarnTool {

    private val newUserWarnList = mutableListOf<NewWarnTool.WarnStringData>()

    fun addWarn(warn: NewWarnTool.WarnStringData) {
        val find = newUserWarnList.find { it.warnString == warn.warnString }
        if (find == null) newUserWarnList.add(warn)
    }

    fun getNewWarnList(): List<NewWarnTool.WarnStringData> {
        val current = System.currentTimeMillis()
        for (w in newUserWarnList.size - 1 downTo 0) {
            if (current - newUserWarnList[w].recvTime > newUserWarnList[w].delayCheckTime) {
                newUserWarnList.removeAt(w)
            }
        }
        return newUserWarnList
    }
}