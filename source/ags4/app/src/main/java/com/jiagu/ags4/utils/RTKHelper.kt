package com.jiagu.ags4.utils

import android.content.Context
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.RtcmInfo

//Created by gengmeng on 6/22/24
object RTKHelper {
    fun formatInfo(context: Context, info: RtcmInfo?): Pair<Boolean, String> {//接口是否有错误 错误信息
        return when {
            info == null -> true to context.getString(com.jiagu.v9sdk.R.string.disconnected)
            info.error != null -> {
                if (info.error == "404") false to context.getString(R.string.rtcm_account_not_auth)
                else true to (info.error ?: context.getString(com.jiagu.v9sdk.R.string.qx_err_else))
            }
            //RTCM数据正常 返回过期时间
            info.status == RtcmInfo.CODE_RTK_DATA_NORMAL -> false to context.getString(com.jiagu.v9sdk.R.string.connected)

            info.status == RtcmInfo.CODE_RTK_DATA_ABNORMAL -> false to context.getString(R.string.rtcm_data_error)//RTCM数据异常
            info.status == RtcmInfo.CODE_MANUAL_CLOSE_RTK -> false to context.getString(com.jiagu.v9sdk.R.string.disconnected)//主动关闭
            info.status == -101 -> false to context.getString(com.jiagu.v9sdk.R.string.certification_fail)
            info.status == -8 -> false to ""
            else -> false to context.getString(com.jiagu.v9sdk.R.string.qx_err_else)
        }
    }
}