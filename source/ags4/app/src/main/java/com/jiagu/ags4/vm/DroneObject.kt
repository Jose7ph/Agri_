package com.jiagu.ags4.vm

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import androidx.core.util.forEach
import androidx.core.util.remove
import androidx.lifecycle.MutableLiveData
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.Battery
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.TrackNode
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.utils.RegionTool
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.logToFile
import com.jiagu.ags4.utils.runTask
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.channel.IChannel
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.SprayVariableData
import com.jiagu.device.model.UploadNaviData
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgProtocol
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.device.vkprotocol.VKDevice
import com.jiagu.v9sdk.R
import org.greenrobot.eventbus.EventBus
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class DroneObject(writer: IChannel.IWriter, val app: Context) : VKDevice(),
    IProtocol.ProtocolListener {

    private var _available = 0

    private class ValidItem(val id: String, val valid: Boolean)

    private val validMap = ConcurrentHashMap<String, ValidItem>()
    val available: Boolean
        get() {
            for ((_, v) in validMap) {
                if (!v.valid) {
                    return false
                }
            }
            return _available > 0
        }

    private fun setServerLock(lock: Int) {
        if (droneCode.isEmpty()) return
        DeviceConfig(app).setDroneLock(droneCode, lock)
    }

    private fun requiredLock(droneId: String): Int {
        val lock = DeviceConfig(app).getDroneLock(droneId)
        LogFileHelper.log("requiredLock $droneId lock:$lock")
        return lock
    }

    private var idCount = 0
    private var lockerCount = -1
    private var connected = false

    private var qxCount = 0
    private var firstReceiveRadioDataTime = 0L
    private var checkVersion = true
    private val sortieList = mutableListOf<VKAg.SortieListData>()

    override fun onProtocolData(data: Any) {
        when (data) {
            is VKAg.IMUData -> {
                processImu(data)
                if (idCount == 0) {
                    protocol.readId()
                    protocol.readNoFlyZone()
                } else if (lockerCount >= 0) {
                    lockerCount++
                    if (lockerCount > 180) {
                        lockDrone()
                    }
                }
                idListCount += 1
                if (idListCount > 20) {
                    idListCount = 0
                    if (devices.scan()) notifyData(devices)
                }
                DroneModel.droneLocation.postValue(data.toRtkLatLng())
                if (data.GPSStatus > 1) {
                    if (qxCount == 0) {
                        val type = when (data.GPSStatus.toInt()) {
                            4, 7 -> 4
                            else -> 1
                        }
                        RtcmModel.sendPosition(data.lat, data.lng, data.alt.toDouble(), type, data.GPSNum.toInt())
                    }
                    qxCount++
                    if (qxCount > 10) qxCount = 0
                }
                if (!isEnteredCLEANMode) {
                    isEnteredCLEANMode = VKAgTool.isCleanMode(data.flyMode.toInt())
                }
                if (droneCode != "" && idListCount > 10 && WhiteList.needCheck(WhiteList.WhiteListType.BATTERY)) {
                    checkSmartBattery()
                }
                imuData.postValue(data)
            }

            is VKAg.HOMEData -> homeData.postValue(data)
            is VKAg.NoFlyData -> noflyData.postValue(data)
            is VKAg.VERData -> {
                droneCode = data.serial
                fcuVersion = data.fwVer

                if (idCount == 0) {
                    runTask {
                        WhiteList.initWhiteList(app)
                    }
                    idCount = 1
                    lockDrone()
                }
                LogFileHelper.log("[FC-$droneCode] $data")
            }

            is VKAg.ABData -> {
                LogFileHelper.log("[FC-$droneCode] $data")
            }

            is VKAg.JiaciData -> {
                if (sortieId != data.jiaciId) {
                    sortieId = data.jiaciId
                    LogFileHelper.log("[FC-$droneCode] sortie: ${data.jiaciId}")
                }
                val allow = DroneModel.allowDrone()
                setLock(!allow)
            }

            is VKAg.ManageInfo -> {
                var regionLock = true
                imuData.value?.let {
                    regionLock = !RegionTool.checkInside(it.lat, it.lng)
                }
                if (regionLock) {
                    protocol.setLock(VKAg.LOCKBIT_REGION)
                }
                protocol.clearLock(VKAg.LOCKBIT_REGION)
                when (requiredLock(droneCode)) {
                    0 -> protocol.clearLock(VKAg.LOCKBIT_REMOTE)
                    1 -> protocol.setLock(VKAg.LOCKBIT_REMOTE)
                    2 -> protocol.setLock(VKAg.LOCKBIT_APP)
                }
            }

            is VKAg.APTYPEData -> {
                sprayWidth = data.getValue(VKAg.APTYPE_AB_WIDTH)
            }

            is VKAg.POSData -> {
                addPosData(data)
                LogFileHelper.log("[FC-$droneCode] $data")
            }

            is VKAg.BatteryGroup -> {
                if (WhiteList.needCheck(WhiteList.WhiteListType.BATTERY)) {
                    LogFileHelper.log("BatteryGroup found battery: ${data.batteries.size}")
                    for ((i, b) in data.batteries.withIndex()) {
                        LogFileHelper.log("found battery: ${b.batId} $i")
                        if (b.batId == "0") {
                            continue
                        }
                        validMap.remove("bat")
                        checkBattery(b.batId, i)
                    }
                }
                saveBattery(data)
            }

            is VKAg.IDListData -> {
                if (devices.update(data)) {
                    notifyData(devices)
                }
                if (data.devType == VKAgCmd.DEVINFO_BATTERY && WhiteList.needCheck(WhiteList.WhiteListType.BATTERY)) {
                    LogFileHelper.log("IDListData found battery: ${data.hwId} ${data.devNum.toInt() - 1}")
                    if (data.hwId != "0") {
                        validMap.remove("bat")
                        checkBattery(data.hwId.takeLast(20), data.devNum.toInt() - 1)
                    }
                }
                protocol.replyIdList(data.devType, data.devNum, true)
            }
        }

        when (data) {
            is VKAg.IMUData -> VoiceMessage.emit(data)
            is VKAg.PWMData -> VoiceMessage.emit(data)

            is VKAg.VERData -> {
                checkVersion = false
                firstReceiveRadioDataTime = 0L
                if (data.fwVer < Constants.MIN_VER) {
                    VoiceMessage.emit(app.getString(R.string.voice_fcu_outdated))
                }
                isV9 = data.isV9
                DroneModel.isV9 = isV9
            }

            is VKAg.POSData -> updatePosData()
            is VKAg.BootloaderStatus -> {
                if (checkVersion) {
                    val currentRadioTime = System.currentTimeMillis()
                    if (firstReceiveRadioDataTime == 0L) {
                        firstReceiveRadioDataTime = currentRadioTime
                    } else if (currentRadioTime - firstReceiveRadioDataTime > 10000) {
                        if (DroneModel.verData.value == null) {
                            _available = 1
                        }
                        checkVersion = false
                    }
                }
            }

            is VKAg.ECUGroup -> {
                if (fcuLock != data.fcuLock) {
                    if (data.fcuLock == 1.toByte()) {
                        VoiceMessage.emit(app.getString(R.string.voice_ecu_ready))
                    }
                    fcuLock = data.fcuLock
                }
            }

            is VKAg.DevInfoData -> {
                devices.updateTime(data.devType, data.devNum)
            }
            is VKAg.NewDevInfoData -> {
                NewWarnTool.updateIsSeederType(isSeeder())
                DroneModel.setWorkType((if (isRecognizedFlowOrSeeder()) DroneModel.FROM_FCU else DroneModel.FROM_USER) to if (isSeeder()) VKAg.LOAD_TYPE_SEED else VKAg.LOAD_TYPE_SPRAY)
                val newDevInfos = ConcurrentHashMap<String, VKAg.DevInfoData>()
                for (d in data.devInfos) {
                    newDevInfos[d.key] = d.value
                }
                val (warns, voices) = NewWarnTool.parseDevWarn(
                    app, newDevInfos, DroneModel.pwmData.value
                )
                val userWarn = NewUserWarnTool.getNewWarnList()
                val total = mutableListOf<NewWarnTool.WarnStringData>()
                total.addAll(warns)
                total.addAll(userWarn)
                curNewWarnData.postValue(total)
                curDevInfoData.postValue(newDevInfos)
                val newWarnString = NewWarnTool.NewWarnStringData()
                newWarnString.voices = voices
                VoiceMessage.emit(newWarnString)
            }
            is VKAg.SortieListData -> {
                Log.v("shero", "receive sortie list data from fcu ${data}")
                LogFileHelper.log("receive sortie list data from fcu ${data}")
                if (data.index > 0 && data.count > 0 && data.index <= data.count) {
                    // 检查是否已存在相同 index 的数据
                    val existingIndex = sortieList.indexOfFirst { it.index == data.index }
                    if (existingIndex == -1) {
                        // 如果不存在相同 index 的数据，则添加新数据
                        sortieList.add(data)
                        sortieList.sortByDescending { sortie -> sortie.sortieId }
                    } else {
                        // 如果存在相同 index 的数据，则更新该数据
                        sortieList[existingIndex] = data
                    }
                }
            }
        }
        if (!connected) {
            DroneModel.droneConnectionState.postValue(true)
            VoiceMessage.emit(app.getString(R.string.voice_fc_connected))
            getParameters()
            getPidParameters()
            getPumpData()
            getBumpParam()
            getSortieList()
            connected = true

            runTask {
                lockDrone()
            }

        }
        notifyData(data)

    }

    fun setDroneLock(lock: Int) {
        protocol.setLock(lock)
    }

    fun clearDroneLock(lock: Int) {
        protocol.clearLock(lock)
    }

    private var idListCount = 20

    class DeviceMap {
        val updateVersion = SparseIntArray()
        val devices = SparseArray<VKAg.IDListData>()
        fun update(data: VKAg.IDListData): Boolean {
            val key = data.devType * 256 + data.devNum
            val ver = updateVersion[key, 0]
            if (ver == 0 || devices[key] != data) {
                devices.put(key, data)
                updateVersion.put(key, ver + 1)
                return true
            }
            return false
        }

        fun getId(type: Short, num: Short): String? {
            val key = type * 256 + num
            val dev = devices[key] ?: return null
            return dev.hwId
        }

        fun updateTime(devType: Short, num: Short) {
            val key = devType * 256 + num
            devices[key]?.recvTime = System.currentTimeMillis()
        }

        fun scan(): Boolean {//设备超过5秒未更新，认为设备已断开
            val current = System.currentTimeMillis()
            val removeIds = mutableListOf<Int>()
            devices.forEach { key, data ->
                if (data.devType != VKAgCmd.DEVINFO_FCU && data.devType != VKAgCmd.DEVINFO_IMU && data.devType != VKAgCmd.DEVINFO_PMU) {
                    if (current - data.recvTime > 5000) {
                        removeIds.add(key)
                    }
                }
            }
            if (removeIds.isEmpty()) return false
            for (i in 0 until removeIds.size) {
                val key = removeIds[i]
                val ver = updateVersion[key, 0]
                devices.remove(key)
                updateVersion.remove(key, ver + 1)
            }
            return true
        }

        fun exportDeviceList(): List<VKAg.IDListData> {
            val out = mutableListOf<VKAg.IDListData>()
            devices.forEach { _, data ->
                out.add(data)
            }
            return out
        }
    }

    private val devices = DeviceMap()
    private var fcuLock: Byte = 0
    private var posIdx = 0
    var posCount = 0
    private val posList = mutableListOf<VKAg.POSData>()
    private var posUpdated = false
    private var isV9 = false
    private fun addPosData(pos: VKAg.POSData) {
        var idx = 0;
        for (p in posList) {
            if (p.posNum == pos.posNum) return
            if (p.posNum > pos.posNum) break
            idx++
        }
        posList.add(idx, pos)
        posUpdated = true
    }

    private fun updatePosData() {
        if (posUpdated) {
            val out = mutableListOf<VKAg.POSData>()
            out.addAll(posList)
//            curPosData.postValue(out)
            posUpdated = false
        }
    }

    fun clearPosData() {
        posIdx = 0
        posCount = 0
        posList.clear()
        posUpdated = true
        updatePosData()
    }

    override fun onRCData(data: Any) {
        if (data is IProtocol.RssiData) {
            notifyData(data)
        }
    }

    override fun onCommandResult(success: Boolean) {
        notifyResult(success)
    }

    override fun onDisconnected() {//每隔5秒会返回一此 handler
        if (connected) {
            VoiceMessage.emit(app.getString(R.string.voice_fc_disconnected))
            devices.devices.clear()
            devices.updateVersion.clear()
            DroneModel.setWorkType(DroneModel.FROM_FCU to VKAg.LOAD_TYPE_NONE)
            DroneModel.newWarnListData.postValue(listOf())
            DroneModel.newDevInfoData.postValue(null)
            DroneModel.droneConnectionState.postValue(false)
            NewWarnTool.clearStringMap(true)
        }
        connected = false
        DroneModel.isCheckAppUpgrade = false
        idCount = 0
        idListCount = 0
        isV9 = false
        _available = 0
        DroneModel.droneType = 0
    }

    private fun processImu(data: VKAg.IMUData) {
        DroneModel.curTotalArea = data.ZuoYeMuShu
        DroneModel.curDrug = data.YiYongYaoLiang
        updateDrone(data)
        saveTrack(data)
    }

    private fun gpsType(status: Short): Int {
        return when (status.toInt()) {
            2, 3, 5, 6 -> 1
            4, 7 -> 4
            else -> 0
        }
    }

    private var stateTime = System.currentTimeMillis()
    private fun updateDrone(data: VKAg.IMUData) {
        if (data.GPSStatus < 2) return

        val current = System.currentTimeMillis()
        if (current - stateTime > 8000) {
            stateTime = current
            val locType = gpsType(data.GPSStatus)
            val state = TrackNode(
                current,
                data.lat,
                data.lng,
                data.alt,
                data.height,
                data.hvel,
                data.vvel,
                data.yaw,
                data.pitch,
                data.roll,
                data.flyTime.toInt(),
                data.ZuoYeMuShu.toFloat(),
                data.flowRate,
                data.YiYongYaoLiang,
                data.GPSNum.toInt(),
                locType,
                data.airFlag == VKAg.AIR_FLAG_ON_AIR,
                data.alertReason.toInt(),
                data.pump.toInt(),
                data.energy,
                DroneModel.canBatteryCurrent
            )
            runTask {
                val current1 = System.currentTimeMillis()
                try {
                    AgsNet.updateDroneState(
                        droneCode, sortieId, data.ZuoYeMuShu.toFloat(), isLive, state
                    )
                    val current2 = System.currentTimeMillis()
                    DroneModel.netConnect = current2 - current1 <= 5000
                    logToFile("updateDroneState ${DroneModel.netConnect}")
                } catch (e: Throwable) {
                    DroneModel.netConnect = false
                    logToFile("updateDroneState error ${DroneModel.netConnect} error:$e")
                }
            }
        }
    }

    private fun getFlyMode(data: VKAg.IMUData): Int {
        return if (VKAgTool.isABMode(data.flyMode.toInt()) || VKAgTool.isNavigation(data.flyMode.toInt())) DroneModel.TYPE_MODE_AU
        else DroneModel.TYPE_MODE_MA
    }

    private var airOrGroundMode = 0
    private val track = mutableListOf<TrackNode>()//飞机起飞后的轨迹数据,上传架次后清空
    private var trackTime = 0L
    private val sortieBattery = mutableListOf<Battery>()//飞机起飞后的电池数据,上传架次后清空
    private var batteryTime = 0L
    private var flyMode = DroneModel.TYPE_MODE_MA

    private fun saveTrack(data: VKAg.IMUData) {
        when (airOrGroundMode) {
            0 -> { // none
                if (data.airFlag == VKAg.AIR_FLAG_ON_AIR) {
                    airOrGroundMode = 1
                    VoiceMessage.emit(app.getString(R.string.voice_unlocked))
                    DroneModel.sortieArea0 = data.ZuoYeMuShu
//                    DroneModel.sortieDrug0 = data.YiYongYaoLiang
                    DroneModel.sortieArea = 0f
                    startTime = System.currentTimeMillis()
                    lat0 = data.lat
                    lng0 = data.lng
                    LogFileHelper.log("[FC-$droneCode] $data")
                }
            }

            1 -> { // take off
                val mode = getFlyMode(data)
                if (mode > flyMode) {
                    flyMode = mode
                }
//                Log.v("shero", "当前飞控面积：${data.ZuoYeMuShu} 飞控药量:${data.YiYongYaoLiang}")
                val tmpArea = data.ZuoYeMuShu - DroneModel.sortieArea0
                var tmpDrug = data.YiYongYaoLiang
                DroneModel.sortieDrug = tmpDrug
                if (tmpDrug < 0) tmpDrug = 0f
                if (tmpArea > DroneModel.sortieArea && tmpArea < 1000) {
                    DroneModel.sortieArea = tmpArea.toFloat()
                    DroneModel.sortieDrug = tmpDrug
                    curSortieAreaData.postValue(tmpArea.toFloat())
                }
                if (data.airFlag == VKAg.AIR_FLAG_ON_GROUND) {
                    airOrGroundMode = 0
                    logToFile(
                        "ground ZuoYeMuShu: ${data.ZuoYeMuShu}, " + "sortieArea0:${DroneModel.sortieArea0} sortieArea:${DroneModel.sortieArea} " + "data.YiYongYaoLiang:${data.YiYongYaoLiang}"
                    )
                    uploadPlanAndTrack(data.YiYongYaoLiang)
                    NewWarnTool.clearStringMap()
                } else {
                    val current = System.currentTimeMillis()
                    if (current > trackTime + 900) {
                        val locType = gpsType(data.GPSStatus)
                        val node = TrackNode(
                            current,
                            data.lat,
                            data.lng,
                            data.alt,
                            data.height,
                            data.hvel,
                            data.vvel,
                            data.yaw,
                            data.pitch,
                            data.roll,
                            data.flyTime.toInt(),
                            DroneModel.sortieArea,
                            data.flowRate,
                            data.YiYongYaoLiang,
                            data.GPSNum.toInt(),
                            locType,
                            data.airFlag == VKAg.AIR_FLAG_ON_AIR,
                            data.alertReason.toInt(),
                            data.pump.toInt() + 1,
                            data.energy,
                            DroneModel.canBatteryCurrent
                        )
                        track.add(node)//起飞后，一秒钟加一次轨迹信息
                        trackTime = current
                    }
                }
                LogFileHelper.log("[FC-$droneCode] $data")
            }
        }
    }

    private fun saveBattery(data: VKAg.BatteryGroup) {
        val time = System.currentTimeMillis()
        //起飞 && 大于一秒
        if (airOrGroundMode == 1 && time > batteryTime + 1000) {
            val batteries = data.batteries
            batteries.forEach {
                val battery = Battery(
                    timestamp = System.currentTimeMillis(),
                    batId = it.batId.ifBlank { "-1" },
                    current = abs(it.current),//输出电流为负 转成正数
                    voltage = it.voltage,
                    temperature = it.temperature,
                    percent = it.percent,
                    cycle = it.cycle,
                    status = (it.status2.toInt() shl 16) or it.status1.toInt()
                )
                sortieBattery.add(battery)
                batteryTime = time
            }
        }
    }

    private var isEnteredCLEANMode = false

    fun containsKeyWithPrefix(prefix: String): Boolean {
        // 遍历 validMap 中的 keys，检查是否有以 prefix 开头的 key
        for (key in validMap.keys) {
            if (key.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

    private fun checkSmartBattery() {
        LogFileHelper.log("checkSmartBattery")
        if (containsKeyWithPrefix("bat")) return
        validMap["bat"] = ValidItem("", false)
        checkLock("bat").let {
            LogFileHelper.log("checkSmartBattery lock $it")
            if (it >= 0) protocol.setLock(VKAg.LOCKBIT_BATTERY)
            else protocol.clearLock(VKAg.LOCKBIT_BATTERY)
        }
    }

    //检查飞控白名单和电池白名单时，没有检查飞控里的锁定状态，检查在白名单里(没有白名单的肯定是true)，直接就给解锁了，导致断网情况下，无法锁定飞机
    private fun checkBattery(batId: String, num: Int) {
        LogFileHelper.log("checkBattery $batId $num")
        val key = "bat$num"
        if (validMap[key]?.id != batId) {
            val valid = WhiteList.checkBattery(batId)
            LogFileHelper.log("checkBattery lock $valid")
            if (valid == -1) {
                protocol.setLock(VKAg.LOCKBIT_BATTERY)
            }
            validMap[key] = ValidItem(batId, valid == 1)
        }
        checkLock("bat").let {
            LogFileHelper.log("checkBattery lock $it")
            if (it >= 0) protocol.setLock(VKAg.LOCKBIT_BATTERY)
            else protocol.clearLock(VKAg.LOCKBIT_BATTERY)
        }
    }

    private fun checkLock(type: String): Int {
        for ((k, v) in validMap) {
            if (!v.valid) {
                if (k.startsWith(type)) return VKAg.LOCKBIT_BATTERY
            }
        }
        return -1
    }

    private fun posSubList(pos: List<VKAg.POSData>, idx: Int): List<VKAg.POSData> {
        val out = mutableListOf<VKAg.POSData>()
        pos.subList(idx, pos.size).forEach { out.add(it) }
        return out
    }

    private fun uploadTrack(sortie: Sortie, complete: (Sortie) -> Unit) {
        runTask {
            if (sim == Constants.SIM_OPEN && !BuildConfig.DEBUG) {
                Repo.uploadSortieSim(sortie, complete)
            } else {
                Repo.uploadSortie(sortie, complete)
            }
        }
    }

    private fun uploadPlanAndTrack(drug: Float) {
        if (track.isEmpty()) return
        val t = mutableListOf<TrackNode>()
        t.addAll(track)
        val endTime = System.currentTimeMillis()
        val duration = (track.last().timestamp - track.first().timestamp).toInt() / 1000
        if (duration < 10) {
            track.clear()
            sortieBattery.clear()
            return
        }

        val sortie = Sortie(
            droneCode,
            sortieId,
            sprayWidth,
            0,
            drug,
            DroneModel.sortieArea,
            t,
            DroneModel.blockId,
            DroneModel.groupId,
            DroneModel.workPercent,
            duration,
            DroneModel.curTotalArea,
            fcuVersion.toString()
        )
        sortie.additional = SortieAdditional(DroneModel.bk)
        sortie.localBlockId = DroneModel.localBlockId
        sortie.localPlanId = DroneModel.localPlanId
        sortie.planId = DroneModel.planId
        sortie.blockId = DroneModel.blockId
        sortie.startTime = startTime
        sortie.endTime = endTime
        sortie.lat0 = lat0
        sortie.lng0 = lng0
        sortie.supplement = sortie.getSupplement(app)
        if (posList.isNotEmpty()) {
            sortie.posData = posSubList(posList, posIdx)
            posCount = posList.size - posIdx
            posIdx = posList.size
        }
        val wt = DroneModel.workRoutePoint
        if (wt.isNotEmpty()) sortie.route = wt

        //因为上传了起始/返航航线，所以imu包里的目标点变了，不是航线的目标点了，而是起始/返航航线的目标点
        val target = (DroneModel.breakPoint.value?.index ?: 1) + 1
        if (target < wt.size) {
            val wr = mutableListOf<GeoHelper.LatLngAlt>()
            for (i in 0 until target - 1) {
                wr.add(GeoHelper.LatLngAlt(wt[i].latitude, wt[i].longitude, wt[i].height.toDouble()))
            }
            val bk = DroneModel.breakPoint.value
            if (bk != null) wr.add(GeoHelper.LatLngAlt(bk.lat, bk.lng, bk.alt.toDouble()))
            if (wr.isNotEmpty()) sortie.workPoints.addAll(wr)
        }

        //航线模式-自动作业 AB模式-AB作业 否则-手动作业
        sortie.workMode = flyMode
        if (sortie.workMode == DroneModel.TYPE_MODE_MA) {
            sortie.localBlockId = 0
            sortie.blockId = 0
            sortie.localPlanId = 0
            sortie.planId = 0
            sortie.additional = null
        }
        //清洗模式-清洗 播撒-播撒 喷洒-喷洒
        sortie.workType = if (isEnteredCLEANMode) DroneModel.TYPE_CLEAN else if (isSeedWorkType()) DroneModel.TYPE_SEED else DroneModel.TYPE_SPRAY //todo 吊运判断
        sortie.battery = sortieBattery.toList()
        flyMode = DroneModel.TYPE_MODE_MA
        isEnteredCLEANMode = false
        Log.d("zhy", "sortie: ${sortie.blockId},${sortie.localBlockId},${sortie.additional}")
        uploadTrack(sortie) {
            EventBus.getDefault().post(it)
        }
        track.clear()
        sortieBattery.clear()
        LogFileHelper.log("droneId:${sortie.id} workArea:${sortie.area} sortieId:${sortie.sortie} userId:${AgsUser.userInfo?.userId}")
    }

    private val protocol = VKAgProtocol(0, Constants.MIN_VER, this, writer)
    var droneCode = ""
    var fcuVersion = 0
    var sortieId = 0
    var isLive = false
    private var startTime = 0L
    private var lat0: Double = 0.0
    private var lng0: Double = 0.0
    private var sprayWidth = 0f
    override fun getUpgradable(component: Int) = createUpgradable(component)

    @Suppress("UNCHECKED_CAST")
    override fun startTimeConsumingTask(task: String, vararg params: Any) {
        when (task) {
            "secure_upload" -> protocol.uploadFirmwareSecure(params[0] as Int, params[1] as InputStream)
            "upload_firmware" -> protocol.uploadFirmware(params[0] as Int, params[1] as InputStream)
            "read_log" -> {
                if (isV9) protocol.readLogV9(params[0] as Int)
                else protocol.readLog(params[0] as Boolean)
            }

            "read_log_v9" -> protocol.readLogV9(params[0] as Int)
            "upload_route" -> protocol.uploadNavi3Points(
                params[0] as List<RoutePoint>, params[1] as Float
            )

            "upload_route_s" -> protocol.uploadStartWaypoints(
                params[0] as List<RoutePoint>, params[1] as Float, params[2] as Float
            )

            "upload_route_e" -> protocol.uploadEndWaypoints(
                params[0] as List<RoutePoint>, params[1] as Float, params[2] as Float
            )

            "seed_flow_calib" -> protocol.calibSeederFlow(0)
            "upload_spray" -> protocol.uploadSprayVariable(params[0] as SprayVariableData)
        }
    }

    override fun stopTimeConsumingTask(task: String) {
        when (task) {
            "upload_firmware" -> protocol.stopUploadFirmware()
            "read_log" -> protocol.stopReadLog()
            "upload_route" -> protocol.stopUploadNaviPoints()
            "upload_spray" -> protocol.stopUploadSpray()
        }
    }

    val imuData = MutableLiveData<VKAg.IMUData>()
    val homeData = MutableLiveData<VKAg.HOMEData>()
    val noflyData = MutableLiveData<VKAg.NoFlyData>()

    lateinit var curSortieAreaData: MutableLiveData<Float>

    lateinit var curNewWarnData: MutableLiveData<List<NewWarnTool.WarnStringData>>
    lateinit var curDevInfoData: MutableLiveData<ConcurrentHashMap<String, VKAg.DevInfoData>>

    fun feedData(data: ByteArray) {
        protocol.onData(data)
    }

    private var reportMode = VKAg.INFO_IMU
    fun changeReport(type: Int) {
        reportMode = type
        protocol.changeReport(type)
    }

    private fun lockDrone() {
        lockerCount = 0
        runTask {
            try {
                val detail = AgsNet.getLock(droneCode)
                val lock = when (val lock = detail.staticInfo.droneIsLock) {
                    0, 1 -> lock
                    else -> 2
                }
                setServerLock(lock)
                LogFileHelper.log("get drone lock status: ${detail.staticInfo.droneIsLock} lock:$lock")
                Log.d("yuhang", "lock: $droneCode, $lock")
                DroneModel.detail.postValue(detail)
            } catch (e: Throwable) {
                Log.d("yuhang", "cannot check lock")
            }
            protocol.getManageInfo()
        }
    }

    fun getVersionInfo() {
        protocol.readId()
    }

    fun sendParameter(idx: Int, value: Float) {
        if (idx == VKAg.APTYPE_AB_WIDTH) {
            sprayWidth = value
        }
        protocol.setParameter(idx, value)
    }

    fun sendIndexedParameter(index: Int, value: Int) {
        protocol.setIndexedParameter(index, value)
    }

    fun getParameters() {
        protocol.getParameters()
    }

    fun getPidParameters() {
        protocol.getPidParameters()
    }

    fun sendPidParameter(index: Int, value: Int) {
        protocol.setPidParameter(index, value)
    }

    fun takeOff2Ab() {
        protocol.takeoffGotoWork(VKAgCmd.CTL_TAKEOFF_WORK_AB.toInt())
        LogFileHelper.log("takeOff(起飞)AB")
    }

    fun takeOff2Clean() {
        protocol.takeoffGotoWork(VKAgCmd.CTL_TAKEOFF_WORK_CLEAN.toInt())
        LogFileHelper.log("takeOff(起飞)CLEAN")
    }

    fun takeOff2Route() {
        protocol.takeoffGotoWork(VKAgCmd.CTL_TAKEOFF_WORK_AU.toInt())
        LogFileHelper.log("takeOff(起飞)ROUTE")
    }

    fun forceUnlock(type: Int) {
        protocol.forceUnlock(type)
        LogFileHelper.log("forceUnlock($type)强制解锁")
    }

    fun land() {
        protocol.land()
        LogFileHelper.log("land(降落)")
    }

    fun hover() {
        protocol.hover()
        LogFileHelper.log("hover(悬停)")
    }

    fun setNaviProperties(param: UploadNaviData) {
        protocol.setNaviProperties(param)
        LogFileHelper.log("setNaviProperties(设置参数)[$param]")
    }

    fun goHome() {
        protocol.goHome()
        LogFileHelper.log("goHome(返航)")
    }
    fun emergencyStop() {
        protocol.emergencyStop()
    }

    fun calibMagnet() {
        protocol.calibMagnet()
    }

    fun calibHorz() {
        protocol.calibHorz()
    }

    fun factoryReset() {
        protocol.factoryReset()
    }

    fun calibController() {
        protocol.calibController()
    }

    fun stopCalibController(status: Int) {//0-完成 1-取消
        protocol.stopCalibController(status)
    }

    fun calibMotor(index: Int) {
        protocol.calibMotor(index)
    }

    fun setMotorNumber(number: Int) {
        protocol.setMotorNumber(number)
    }

    fun calibVolt(volt: Float) {
        protocol.calibVoltage(volt)
    }

    fun setPumpMode(mode: Int) {
        protocol.setPumpMode(mode)
    }

    fun startCalibEFTFlow() {
        protocol.startCalibEFT()
    }

    fun endCalibEFTFlow() {
        protocol.endCalibEFT()
    }

    fun calibEFTFlow(flow: Int, flow2: Int = 0) {
        protocol.calibFlowChart(flow, flow2)
    }

    fun clearBgFlow() {
        protocol.clearBgFlow()
    }

    fun calibYeLunFlowmeter(qty: Float) {
        protocol.calibYeLunFlowmeter(qty)
    }

    fun stopYeLunCalibFlowmeter(qty: Float) {
        protocol.stopYeLunCalibFlowmeter(qty)
    }

    fun calibLinePump(start: Int, pumpIndex: Int) {
        protocol.calibLinePump(start, pumpIndex)
    }

    fun getLinePumpData(pumpIndex: Int) {
        protocol.requestLinePumpData(pumpIndex)
    }

    fun configLinePump() {
        protocol.configLinePump()
    }

    fun calibBumpChart(status: Int) {//0开始 1-取消 0xff-恢复出场设置
        protocol.calibBumpChart(status)
    }

    fun calibMaterialRadar(cmd: Int) {
        protocol.calibMaterialRadar(cmd)
    }

    fun calibSeeder(cmd: Byte, param: Int = 0) {
        protocol.calibSeeder(cmd, param)
    }

    fun calibSeederFlow(status: Int) {
        protocol.calibSeederFlow(status)
    }

    fun setWeightK(cmd: Byte, param: Int = 0) {
        protocol.setEFTWeightK(cmd, param)
    }

    fun setBumpParam(qty: Float, param: Float) {
        protocol.setBumpParam(qty, param)
    }

    fun getBumpParam() {
        protocol.getBumpParam()
    }

    fun getABData(){
        protocol.requestABData()
    }

    private fun setLock(v: Boolean) {
        protocol.localLock(v)
    }

    var sim: Int
        get() = protocol.localSim
        set(v) = protocol.localSim(v)

    fun startSimulator(lat: Double, lng: Double) {
        protocol.startSimulator(lat, lng)
    }

    fun clearAB() {
        protocol.clear("ab")
    }

    fun getUserData(type: Int) {
        protocol.requestUserPackage(type)
    }

    fun setUserData(type: Int, data: ByteArray) {
        protocol.setUserData(type, data)
    }

    fun getABInfo() {
        protocol.requireABInfo()
    }

    fun sendABInfo(abpl: VKAg.ABPLData) {
        Log.d("zhy", "sendABInfo: ${abpl}")
        protocol.uploadABInfo(abpl)
    }

    fun getPumpData() {
        protocol.getPumpData()
    }

    fun getChannelMapping() {
        protocol.getChannelMapping()
    }

    fun setChannelMapping(mapping: ByteArray, functions: ByteArray) {
        protocol.setChannelMapping(mapping, functions)
    }

    fun setChannelMapping(idx: Int, function: Int, revert: Boolean): Boolean {
        return protocol.setChannelMapping(idx, function, revert)
    }

    fun getLogList() {
        protocol.getLogList()
    }

    fun getSortieList() {
        DroneModel.sortieListData.postValue(null)
        sortieList.clear()
        protocol.getSortieList()
    }

    fun setRemoteId(type: Int, id: String) {
        protocol.setRemoteId(type, id)
    }

    fun hasBP(): Boolean {
        val bk = DroneModel.breakPoint.value
        Log.v("shero", "hasBp breakWp:${bk}")
        return (bk != null) && (bk.lat != 0.0) && (bk.lng != 0.0)
    }

    fun setBP() {
        val bk = DroneModel.breakPoint.value
        if ((bk != null) && (bk.lat != 0.0) && (bk.lng != 0.0)) {
            setBreakPoint(bk)
        }
    }

    fun setBreakPoint(bp: VKAg.BreakPoint) {
        protocol.setBreakPoint(bp)
    }

    fun setAPoint() {
        imuData.value?.let {
            if (it.GPSStatus > 1) {
                protocol.setABPoint(VKAgCmd.AB_A_POS, it.lat, it.lng)
            }
        }
    }

    fun setBPoint() {
        imuData.value?.let {
            if (it.GPSStatus > 1) {
                protocol.setABPoint(VKAgCmd.AB_B_POS, it.lat, it.lng)
            }
        }
    }

    fun setAAngle() {
        imuData.value?.let {
            protocol.setABAngle(VKAgCmd.AB_A_ANG, it.yaw)
        }
    }

    fun setBAngle() {
        imuData.value?.let {
            protocol.setABAngle(VKAgCmd.AB_B_ANG, it.yaw)
        }
    }

    fun setABDirection(dir: Int, angleA: Float, angleB: Float) {
        protocol.setABDirection(dir, angleA, angleB)
    }

    fun getBreakPoint() {
        protocol.getBreakPoint()
    }

    fun setMasterGPS(value: Int) {
        protocol.setMasterGPS(value)
    }

    fun responseToReturnNavi(allow: Boolean) {
        protocol.responseToReturnNavi(allow)
    }

    fun responseToStartNavi(allow: Boolean) {
        protocol.responseToStartNavi(allow)
    }

    // only for MK15
    fun sendRtcm(rtcm: ByteArray) {
        protocol.sendRtcm(rtcm, rtcm.size)
    }

    fun isSeeder(): Boolean {
        return protocol.isSeeder
    }

    fun isRecognizedFlowOrSeeder(): Boolean {
        return protocol.isRecognizedFlowOrSeeder
    }

    fun setLedSwitch(type: Int, open: Boolean) {
        protocol.lenSwitch(type, open)
    }

    fun setManualEnhancement(value: Int) {
        LogFileHelper.log("set manual enhancement: $value")
        protocol.setManualEnhancement(value)
    }

    fun setHome(lat: Double, lng: Double, alt: Double) {
        protocol.setHome(lat, lng, alt)
    }
}
