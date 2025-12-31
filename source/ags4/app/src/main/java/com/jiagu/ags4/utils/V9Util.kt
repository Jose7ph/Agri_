package com.jiagu.ags4.utils

object V9Util {
    fun canUpgrade(droneCode: String?): Boolean {
        if (droneCode == null) return false
        return !(droneCode.startsWith("9") && (droneCode[3] == '1' || droneCode[3] == '2'))
    }

    // 40xx的是新GPS需要升级V3版本的，强制推送。
    // 其他20xx，1xxx的是V2版本的不提示升级，其他获取不了版本的不提示升级，
    // 客户如果要升级支持离线手动选择固件
    fun canGnssUpgrade(sw: Int): Boolean {
//        if (sw == null) {
//            return false
//        }
//        if (sw == 2080) {
//            return true
//        }
        return isGnssV3(sw) && sw < 4059
    }

    fun isV9P(droneCode: String?): Boolean {
        if (droneCode == null) {
            return false
        }
        return droneCode.startsWith("9") && (droneCode[3] == '3' || droneCode[3] == '4')
    }

    fun isGnssV3(sw: Int): Boolean {

        return sw in 4000..4999
    }
    fun isGnssV2(sw: Int): Boolean {
        return sw in 1000..2999
    }
}