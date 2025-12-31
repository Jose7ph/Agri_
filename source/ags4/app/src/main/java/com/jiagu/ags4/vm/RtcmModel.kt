package com.jiagu.ags4.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.jiagu.ags4.bean.RtcmInfo
import com.jiagu.ags4.utils.logToFile
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.api.helper.MemoryHelper
import com.jiagu.device.rtcm.IRtcmProvider
import com.jiagu.device.rtcm.NtripClient
import com.jiagu.device.rtcm.RTCMParser
import com.jiagu.device.rtcm.SerialPortRtcmProvider
import com.jiagu.device.rtcm.UsbRtcmProvider

object RtcmModel {

    const val TYPE_NTRIP = "ntrip"
    const val TYPE_USB = "usb"

    val rtcmData = MutableLiveData<Int>()
//    val stationInfo = MutableLiveData<StationInfo>()
    val ntripInfo = MutableLiveData<String>()
    val rtcmInfo = MutableLiveData<RtcmInfo?>()
    val rtcmMessage = MutableLiveData<List<RTCMParser.RTCMMessage>>()
    val rtcmDataTotalSize = MutableLiveData(0)
    val rtcmConnectStartTime = MutableLiveData<Long>(0)

    private var curRtcmInfo: RtcmInfo? = null
    private var client: IRtcmProvider? = null
    var rtcmCount = MutableLiveData<Int>(0)
    val rtcmParser = RTCMParser()
    private var preTime = 0L
    private val rtcmListener = object : IRtcmProvider.RtcmListener {
        override fun onRtcm(data: ByteArray) {
            logToFile("rtcm: ${MemoryHelper.dumpData(data)}")
            rtcmCount.postValue(rtcmCount.value?.plus(1))
            rtcmData.postValue(data.size)
            rtcmDataTotalSize.postValue((rtcmDataTotalSize.value ?: 0) + data.size)
            rtcmParser.parseRTCMData(data)
            val current = System.currentTimeMillis()
            if (current - preTime > 1000) {
                preTime = current
                rtcmMessage.postValue(rtcmParser.getData())
            }

            if (curRtcmInfo?.status != RtcmInfo.CODE_RTK_DATA_NORMAL && data.size > 0) {//2千寻数据正常  1千寻数据异常
                curRtcmInfo?.status = RtcmInfo.CODE_RTK_DATA_NORMAL
                postRtcmInfo()
            }
            if (curRtcmInfo?.status != RtcmInfo.CODE_RTK_DATA_ABNORMAL && data.size <= 0) {//1千寻数据异常
                curRtcmInfo?.status = RtcmInfo.CODE_RTK_DATA_ABNORMAL
                postRtcmInfo()
            }
        }
        override fun onStationPosition(bid: String, lat: Double, lng: Double) {
//            stationInfo.postValue(StationInfo(bid, lat, lng))
        }

        override fun onStatus(status: Int) {
            val success = status >= 0 //千寻0代表千寻账号开启成功
            ntripInfo.postValue(if (success) TYPE_NTRIP else null)
            curRtcmInfo?.let {
                if (status == 0) it.status = 0
                else if (status < 0 && status < it.status) {
                    it.status = status
                    postRtcmInfo()
                }//数值越小，优先级越高
            }
            if (!success) {
                Log.d("yuhang", "rtcm connect error: $status")
                rtcmData.postValue(0)
            }
        }
    }

    fun subscribeRtcm(server: String, port: Int, mount: String, user: String, pass: String, s: IRtcmProvider.RtcmSubscriber) {
        if (client == null) {
            client = NtripClient(server, port, mount, user, pass, rtcmListener)
            curRtcmInfo?.source = TYPE_NTRIP
        }
        client?.subscribeRtcm(s)
    }

    fun subscribeRtcm(context: Context, pid: Int, sid: Int, s: IRtcmProvider.RtcmSubscriber) {
        if (client == null) {
            client = UsbRtcmProvider(context, pid, sid, rtcmListener)
            curRtcmInfo?.source = TYPE_USB
        }
        client?.subscribeRtcm(s)
    }

    fun subscribeRtcm(context: Context, path: String, baudRate: Int,s: IRtcmProvider.RtcmSubscriber) {
        if (client == null) {
            LogFileHelper.log("subscribeRtcm: UNIRC7")
            client = SerialPortRtcmProvider(context, path, baudRate, rtcmListener)
            curRtcmInfo?.source = TYPE_USB
        }
        client?.subscribeRtcm(s)
    }

    fun unsubscribeRtcm(s: IRtcmProvider.RtcmSubscriber) {
        client?.unsubsribeRtcm(s)
        curRtcmInfo?.status = RtcmInfo.CODE_MANUAL_CLOSE_RTK
        curRtcmInfo?.error = null
        postRtcmInfo()
    }

    fun closeRtcmProvider() {
        try {
            client?.close()
            client = null
            curRtcmInfo?.status = RtcmInfo.CODE_MANUAL_CLOSE_RTK
            curRtcmInfo?.error = null
            postRtcmInfo()
        } catch (e: Exception) {
            LogFileHelper.log("closeRtcmProvider: $e")
        }

    }

    private fun postRtcmInfo() {
        rtcmInfo.postValue(curRtcmInfo)
    }

    fun closeClient() {
        client?.close()
        client = null
    }

    fun sendPosition(lat: Double, lng: Double, alt: Double, type: Int, satellite: Int) {
        client?.sendPosition(lat, lng, alt, type, satellite)
    }

    fun isRtcmProviderAvailable(): Boolean {
        return client != null
    }

    fun addProviderListener(l: IRtcmProvider) {
        client = l
    }

    fun setRtcmModel(d: RtcmInfo?) {
        curRtcmInfo = d
        postRtcmInfo()
    }
}