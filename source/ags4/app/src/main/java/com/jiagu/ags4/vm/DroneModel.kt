package com.jiagu.ags4.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jiagu.ags4.AgsUser
import com.jiagu.ags4.BuildConfig
import com.jiagu.ags4.Constants
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.RtcmInfo
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.net.model.DroneDetail
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.LogFileHelper
import com.jiagu.device.channel.IChannel
import com.jiagu.device.controller.Controller
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.controller.IController
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.model.RtkLatLng
import com.jiagu.device.rtcm.IRtcmProvider
import com.jiagu.device.vkprotocol.IProtocol
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.v9sdk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.math.sqrt

object DroneModel : IController.Listener {
    // data of current drone
    val detail = MutableLiveData<DroneDetail?>()
    val homeData = MutableLiveData<GeoHelper.LatLng>()
    val imuData = MutableLiveData<VKAg.IMUData>()
    val pwmData = MutableLiveData<VKAg.PWMData>()
    val motorData = MutableLiveData<VKAg.MotorGroup>(VKAg.MotorGroup(0, 0, Array(8) { index -> VKAg.MotorData((index + 1).toByte()) }))
    val rcafData = MutableLiveData<VKAg.RCAFData>()
    val aptypeData = MutableLiveData<VKAg.APTYPEData>()
    val pidData = MutableLiveData<VKAg.PIDData>()
    val flowData = MutableLiveData<VKAg.FlowData>()
    val engineData = MutableLiveData<VKAg.EngineData>()
    val batteryData = MutableLiveData<VKAg.BatteryGroup>()
    val hydrogenBatteryData1 = MutableLiveData<VKAg.HydrogenBatteryData>()
    val hydrogenBatteryData2 = MutableLiveData<VKAg.HydrogenBatteryData>()
    val abData = MutableLiveData<VKAg.ABData>()
    val abplData = MutableLiveData<VKAg.ABPLData>()
    val rssiData = MutableLiveData<Int>()
    val verData = MutableLiveData<VKAg.VERData>()
    val pumpData = MutableLiveData<VKAg.PumpData>()
    val deviceData = MutableLiveData<VKAg.DeviceData>()
    val sortieAreaData = MutableLiveData<Float>()//当前作业面积
    val workAreaData = MutableLiveData<Double>()//当前作业面积
    val posData = MutableLiveData<List<VKAg.POSData>>()
    val warnData = MutableLiveData<VKAg.WARNData>()
    val breakPoint = MutableLiveData<VKAg.BreakPoint?>()
    val seedData = MutableLiveData<VKAg.SeedData>()
    val ecuData = MutableLiveData<VKAg.ECUGroup>()
    val wlData = MutableLiveData<VKAg.WLData>()
    val auxData = MutableLiveData<VKAg.AUXData>()
    val deviceList = MutableLiveData<List<VKAg.IDListData>>(listOf())
    val deviceSeedData = MutableLiveData<VKAg.EFTSeedData>()
    val deviceWeightData = MutableLiveData<VKAg.EFTWeightData>()
    val devicePumpData = MutableLiveData<VKAg.EFTPumpData>()
    val deviceCentrifugalData = MutableLiveData<VKAg.EFTCentrifugalData>()
    val deviceBoomSensorData = MutableLiveData<VKAg.EFTBoomSensorData>()
    val deviceMaterialSensorData = MutableLiveData<VKAg.EFTMaterialSensorData>()
    val deviceFlowData = MutableLiveData<VKAg.EFTFlowData>()
    val deviceRTKData = MutableLiveData<VKAg.RTKInfo>()
    val deviceGPSData = MutableLiveData<VKAg.GNSSInfo>()
    val deviceTerrainData = MutableLiveData<VKAg.TerrainInfo>()
    val deviceRadarData = MutableLiveData<VKAg.RadarObstacleInfo>()
    val deviceLinePumpData = MutableLiveData<VKAg.LinePumpGroup>()
    val newWarnListData = MutableLiveData<List<NewWarnTool.WarnStringData>>()
    val newDevInfoData = MutableLiveData<ConcurrentHashMap<String, VKAg.DevInfoData>>()
    val sortieListData = MutableLiveData<List<VKAg.SortieListData>>()
    val idList = MutableLiveData<DroneObject.DeviceMap>()
    val taskComplete = MutableLiveData<Boolean?>()
    val blockPlan = MutableStateFlow<BlockPlan?>(null)

    val radarGraphData = MutableLiveData<VKAg.Radar360Info>()
    val obstacleInfo = floatArrayOf(0f, 0f, 0f, 0f)
    val canGALVInfo = MutableLiveData<VKAg.CANGALVInfo>()
    val remoteIdData = MutableLiveData<VKAg.RemoteIdInfo>()

    var canBatteryCurrent = 0f
    val localS1 = MutableLiveData<String>()
    val peerS1 = MutableLiveData<String>()

    private val droneListener = { data: Any ->
        when (data) {
            is IProtocol.RssiData -> rssiData.postValue(data.rssi)
            is VKAg.HOMEData -> homeData.postValue(GeoHelper.LatLng(data.lat, data.lng))
            is VKAg.IMUData -> {
                VoiceMessage.emit(data)
                imuData.postValue(data)
                //自动连接usb rtk
                autoRTKConnect()
            }

            is VKAg.PWMData -> {
                VoiceMessage.emit(data)
                pwmData.postValue(data)
            }

//            is VKAg.MotorGroup -> motorData.postValue(data)
            is VKAg.RCAFData -> rcafData.postValue(data)
            is VKAg.PIDData -> pidData.postValue(data)
            is VKAg.APTYPEData -> {
                droneType = data.getIntValue(VKAg.APTYPE_DRONE_TYPE)
                aptypeData.postValue(data)
            }

            is VKAg.FlowData -> flowData.postValue(data)
//            is VKAg.BatteryGroup -> batteryData.postValue(data)
            is VKAg.EngineData -> engineData.postValue(data)
            is VKAg.ABData -> abData.postValue(data)
            is VKAg.ABPLData -> {
                if (data.break_lat == 0.0 || data.break_lng == 0.0
                    || data.lat1 == 0.0 || data.lng1 == 0.0
                    || data.lat2 == 0.0 || data.lng2 == 0.0
                ) {
                } else {
                    abplData.postValue(data)
                }
            }

            is VKAg.BreakPoint -> {
                if (data.lat == 0.0 || data.lng == 0.0) {
                } else {
                    breakPoint.postValue(data)
                    saveLocalBK(data)
                }
            }

            is VKAg.VERData -> {
//                if (data.fwVer < Constants.MIN_VER) {
//                    VoiceMessage.emit(app.getString(R.string.voice_fcu_outdated))
//                }
                if (!isCheckAppUpgrade) {
                    isCheckAppUpgrade = true
                    checkAppUpgrade.postValue(true)
                }
                verData.postValue(data)
            }

            is VKAg.PumpData -> pumpData.postValue(data)
            is VKAg.BootloaderStatus -> {
                if (data.status != 0) {
                    val sid = when (data.component) {
                        VKAgCmd.COMPONENT_MAIN_CTRL -> R.string.voice_fcu_bootloader_fail
                        VKAgCmd.COMPONENT_IMU -> R.string.voice_imu_bootloader_fail
                        VKAgCmd.COMPONENT_PMU -> R.string.voice_pmu_bootloader_fail
                        else -> 0
                    }
                    if (sid != 0) VoiceMessage.emit(app.getString(sid))
                }
            }

            is VKAg.WARNData -> {
                VoiceMessage.emit(data)
                warnData.postValue(data)
            }

            is VKAg.WLData -> wlData.postValue(data)
            is VKAg.ECUGroup -> ecuData.postValue(data)
            is VKAg.AUXData -> auxData.postValue(data)
            is DroneObject.DeviceMap -> {
                val devices = data.exportDeviceList()
                deviceList.postValue(devices)
            }

            is VKAg.DevInfoData -> {
                when (val info = data.info) {
                    is VKAg.EFTFlowData -> {
                        info.manufacture_name = VKAgTool.getFlowName(app, info.manufacture_id)
                        deviceFlowData.postValue(info)
                    }

                    is VKAg.EFTSeedData -> {
                        info.manufacture_name = VKAgTool.getSeedName(app, info.manufacture_id)
                        deviceSeedData.postValue(info)
                    }

                    is VKAg.EFTWeightData -> {
                        info.manufacture_name = VKAgTool.getSeedName(app, info.manufacture_id)
                        deviceWeightData.postValue(info)
                    }

                    is VKAg.EFTPumpData -> devicePumpData.postValue(info)
                    is VKAg.LinePump -> {
                        val datas =
                            deviceLinePumpData.value?.data?.toMutableList() ?: mutableListOf()
                        updateLinePumpDataList(datas, info, data.devNum)
                        deviceLinePumpData.postValue(VKAg.LinePumpGroup(datas))
                    }

                    is VKAg.EFTCentrifugalData -> deviceCentrifugalData.postValue(info)
                    is VKAg.EFTBoomSensorData -> deviceBoomSensorData.postValue(info)
                    is VKAg.EFTMaterialSensorData -> deviceMaterialSensorData.postValue(info)
                    is VKAg.RTKInfo -> deviceRTKData.postValue(info)
                    is VKAg.GNSSInfo -> deviceGPSData.postValue(info)
                    is VKAg.TerrainInfo -> deviceTerrainData.postValue(info)
                    is VKAg.RadarObstacleInfo -> {
                        deviceRadarData.postValue(info)
                        val dist = sqrt(info.dist_x * info.dist_x + info.dist_y * info.dist_y)
                        if (data.devNum == 1.toShort()) obstacleInfo[0] = dist
                        else obstacleInfo[2] = dist
                        val graph = VKAg.Radar360Info().apply { distances = obstacleInfo }
                        radarGraphData.postValue(graph)
                    }

                    is VKAg.Radar360Info -> radarGraphData.postValue(info)
                    is VKAg.HydrogenBatteryData -> if (info.index == 1) hydrogenBatteryData1.postValue(info) else hydrogenBatteryData2.postValue(info)

                    is VKAg.MotorInfo -> {
                        val motors = motorData.value!!.motors
                        updateMotorDataList(motors, info, data.devNum.toByte())
                        motorData.postValue(VKAg.MotorGroup(0, 0, motors))
                    }

                    is VKAg.BatteryInfo -> {
                        val batteries = batteryData.value?.batteries?.toMutableList() ?: mutableListOf()
                        updateBatteryDataList(batteries, info, data.devNum)
                        batteryData.postValue(VKAg.BatteryGroup(batteries))
                    }

                    is VKAg.RemoteIdInfo -> {
                        remoteIdData.postValue(info)
                    }

                    is VKAg.CANGALVInfo -> {
                        canBatteryCurrent = info.current
                        canGALVInfo.postValue(info)
                    }
                }
            }

            is VKAg.DeviceData -> deviceData.postValue(data)
        }
    }

    fun updateMotorDataList(
        motorDataList: Array<VKAg.MotorData>,
        motorInfo: VKAg.MotorInfo,
        devNum: Byte,
    ) {

        // 创建新的 MotorData
        val newMotorData = VKAg.MotorData().apply {
            number = devNum
            state = motorInfo.status
            speed = motorInfo.speed
            percent = motorInfo.percent.toShort()
            duration = motorInfo.duration
            voltage = motorInfo.voltage
            current = motorInfo.current
            temperature = motorInfo.temperature.toInt()
        }

        // 添加并排序
        motorDataList[devNum - 1] = newMotorData
    }

    fun updateBatteryDataList(
        batteryDataList: MutableList<VKAg.BatteryData>,
        batteryInfo: VKAg.BatteryInfo,
        devNum: Short,
    ) {
        // 查找现有数据
        val existingBattery = batteryDataList.firstOrNull { it.devNum == devNum }

        if (existingBattery != null) {
            // 更新现有数据
            with(existingBattery) {
                cycle = batteryInfo.cycle
                voltage = batteryInfo.voltage
                current = batteryInfo.current
                temperature = batteryInfo.temperature
                percent = batteryInfo.percent
                status1 = batteryInfo.status.toShort()
                cellVolt = batteryInfo.cells.clone()
                factoryId = batteryInfo.manufacture_id.toInt()
            }
        } else {
            // 创建新的 BatteryData
            val newBatteryData = VKAg.BatteryData().apply {
                this.devNum = devNum
                this.cycle = batteryInfo.cycle
                this.voltage = batteryInfo.voltage
                this.current = batteryInfo.current
                this.temperature = batteryInfo.temperature
                this.percent = batteryInfo.percent
                this.status1 = batteryInfo.status.toShort()
                this.cellVolt = batteryInfo.cells.clone()
                this.factoryId = batteryInfo.manufacture_id.toInt()
                // batId, status2 和 out_* 字段保持默认值
            }

            // 添加到列表并按 devNum 排序
            batteryDataList.add(newBatteryData)
            batteryDataList.sortBy { it.devNum }
        }
    }

    private fun updateLinePumpDataList(
        dataList: MutableList<VKAg.LinePump>,
        info: VKAg.LinePump,
        devNum: Short,
    ) {
        // 查找现有数据
        val existingBattery = dataList.firstOrNull { it.devNum == devNum }

        if (existingBattery != null) {
            // 更新现有数据
            with(existingBattery) {
                manufacture_id = info.manufacture_id
                warn = info.warn
                rotate_speed = info.rotate_speed
                k = info.k
            }
        } else {
            // 创建新的Data
            val newData = VKAg.LinePump().apply {
                this.devNum = devNum
                this.manufacture_id = info.manufacture_id
                this.rotate_speed = info.rotate_speed
                this.k = info.k
            }

            // 添加到列表并按 devNum 排序
            dataList.add(newData)
            dataList.sortBy { it.devNum }
        }
    }

    /*
     * Remote Controller related
     */
    private var t0 = System.currentTimeMillis()
    override fun onRadioData(index: Int, data: ByteArray) {
        if (drone != null) {
            drone?.feedData(data)
            if (rcId == "") {
                val t = System.currentTimeMillis()
                if (t - t0 > 2000) {
                    readControllerId()
                    t0 = t
                }
            }
        } else {
            drone = DroneObject(CtrlWriter(index), app).apply {
                this.curSortieAreaData = sortieAreaData
//                this.curWorkAreaData = workAreaData
                this.curNewWarnData = newWarnListData
                this.curDevInfoData = newDevInfoData
                // automatically open simulator for debug
                this.sim = if (BuildConfig.DEBUG) 1 else 0
                startDataMonitor(droneListener)
                feedData(data)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onControllerState(name: String, value: Any) {
        when (name) {
            "type" -> {
                controllerType.postValue(value as String)
                readControllerId()
            }

            "location" -> {
                val pos = value as RtkLatLng
                if (pos.type > 0) {
                    RtcmModel.sendPosition(
                        pos.lat,
                        pos.lng,
                        pos.alt,
                        pos.info.locType ?: 1,
                        pos.info.svNum ?: 10
                    )
                }
            }

            "pdt", "id" -> {
                rcId = value as String
                controllerPn.postValue(rcId)
                LogFileHelper.log("pdtinfo: $value")
            }

            "connection" -> {
                val status = value as Int
                controllerConnectionState.postValue(status)
                if (status == Controller.DISCONNECTED) {
                    unsubscribeRtcmProvider()
                    rcId = ""
                }
            }

            "rssi" -> rssiData.postValue(value as Int)
            "key" -> {
                val v = value as Array<String>
                controllerKeyMapping.postValue(v)
            }

            "s1_local" -> {
                localS1.postValue(value as String)
                LogFileHelper.log(value.replace("\n", " "))
            }

            "s1_peer" -> {
                peerS1.postValue(value as String)
                LogFileHelper.log(value.replace("\n", " "))
            }
        }
        rcListeners.forEach {
            it(name, value)
        }
    }

    private class CtrlWriter(val index: Int) : IChannel.IWriter {
        override fun write(data: ByteArray) {
            controller.sendRadio(index, data)
        }
    }

    /*
     * RTCM related
     */

    fun openUsbStation() {
        RtcmModel.rtcmDataTotalSize.postValue(0)
        RtcmModel.rtcmConnectStartTime.postValue(System.currentTimeMillis())
        val rtcmInfo = RtcmInfo(
            4, 0, "", "", "", "", "", "",
            "", "", "", "", 0, true, 0, ""
        )
        rtcmInfo.source = RtcmModel.TYPE_USB
        RtcmModel.setRtcmModel(
            rtcmInfo
        )
        RtcmModel.closeClient()
        if (ControllerFactory.deviceModel == "MK15") {
            RtcmModel.subscribeRtcm(app, 60000, 4292, controllerReceiver)
        } else if (ControllerFactory.deviceModel == "UNIRC7") {
            RtcmModel.subscribeRtcm(app, "/dev/ttyHS1", 230400, controllerReceiver)
        } else {
            RtcmModel.subscribeRtcm(app, 0, 0, controllerReceiver)
        }
    }

    fun openNtrip(server: String, port: Int, mount: String, user: String, pass: String) {
        RtcmModel.subscribeRtcm(server, port, mount, user, pass, controllerReceiver)
    }

    fun openNtrip(list: List<String>) {
        if (list.size < 4 || list[0].isBlank() || list[1].isBlank() || list[2].isBlank() || list[3].isBlank() || list[4].isBlank()) return
        val info = RtcmInfo(
            0, 0L, "", "", "", "", "", "",
            "", "", "", "", 0, true, 0
        )
        info.source = RtcmModel.TYPE_NTRIP
        RtcmModel.closeClient()
        RtcmModel.setRtcmModel(info)//open ntrip
        RtcmModel.subscribeRtcm(
            list[4],
            list[3].toInt(),
            list[2],
            list[0],
            list[1],
            controllerReceiver
        )
    }

    fun unsubscribeRtcmProvider() {
        RtcmModel.unsubscribeRtcm(controllerReceiver)
    }

    fun bindController() {
        controller = ControllerFactory.createController(this)
    }

    private lateinit var controller: Controller
    private var drone: DroneObject? = null

    fun postWorkType() {
        workTypeLiveData.postValue(currentWorkType)
    }

    fun setWorkType(type: Pair<Int, Int>) {
        currentWorkType = type
        postWorkType()
    }

    const val FROM_FCU = 1
    const val FROM_USER = 2
    var droneType = -1
    var currentWorkType = FROM_USER to VKAg.LOAD_TYPE_NONE
    val workTypeLiveData = MutableLiveData<Pair<Int, Int>>()
    val activeDrone: DroneObject?
        get() {
            val d = drone ?: return null
//            return if (d.available) drone else null
            return d
        }

    val controllerConnectionState = MutableLiveData<Int>()
    val controllerType = MutableLiveData<String>()
    val controllerKeyMapping = MutableLiveData(arrayOf(" 5", " 6", " 7", " 8", " 9", "10"))
    val droneLocation = MutableLiveData<RtkLatLng>()
    val controllerPn = MutableLiveData<String>()

    val droneConnectionState = MutableLiveData<Boolean>()
    val checkAppUpgrade = MutableLiveData<Boolean>()
    var isCheckAppUpgrade = false
    var netConnect = true

    fun hasGPS(): Boolean {
        val ids = idList.value ?: return false
        ids.getId(VKAgCmd.DEVINFO_GNSS, 1) ?: return false
        val gpsInfo = deviceGPSData.value ?: return false
        return gpsInfo.status.toInt() == 1
    }

    fun hasRadar(): Boolean {
        val ids = idList.value ?: return false
        ids.getId(VKAgCmd.DEVINFO_OBSTACLE, 1) ?: return false
        val gpsInfo = deviceRadarData.value ?: return false
        if (gpsInfo.status.toInt() == 1) return true
        return false
    }

    fun hasTerrain(): Boolean {
        val ids = idList.value ?: return false
        ids.getId(VKAgCmd.DEVINFO_TERRAIN, 1) ?: return false
        val gpsInfo = deviceTerrainData.value ?: return false
        if (gpsInfo.status.toInt() == 1) return true
        return false
    }

    var lastHeartTime = 0L
    private var config: Config? = null
    private var deviceConfig: DeviceConfig? = null
    private val controllerReceiver = object : IRtcmProvider.RtcmSubscriber {
        override fun receiveRtcmData(rtcm: ByteArray) {
            sendRtcm(rtcm)
            val current = System.currentTimeMillis()
            if (current - lastHeartTime > 300000) {//5分钟
                lastHeartTime = current
                config?.ntripLastConnectTime = System.currentTimeMillis()
            }
        }
    }

    lateinit var app: Application
    fun connectDevice(app: Application, device: String = "") {
        this.app = app
        controller.connect(app, device)
        config = Config(app)
        deviceConfig = DeviceConfig(app)
    }

    fun connectLastDevice(app: Application) {
        this.app = app
        controller.connect(app, "")
        config = Config(app)
        deviceConfig = DeviceConfig(app)
        postWorkType()
    }

    fun close() {
        controller.destroy()
    }

    fun readControllerId() {
        controller.readId()
    }

    fun readControllerParam() {
        controller.readParameters()
    }

    fun setControllerParam(type: String, param: String) {
        controller.setParameters(type, param)
        when (type) {
            "type" -> controllerType.postValue(param)
        }
    }

    fun pushButtonHandler(handler: IController.ButtonHandler) {
        controller.pushButtonHandler(handler)
    }

    fun popButtonHandler() {
        controller.popButtonHandler()
    }

    fun sendRtcm(rtcm: ByteArray) {
        activeDrone?.sendRtcm(rtcm)
    }

    fun allowDrone(): Boolean {
        return AgsUser.userInfo != null
    }

    const val TYPE_SPRAY = 1//喷洒作业
    const val TYPE_SEED = 2//播撒作业
    const val TYPE_LIFTING = 3//吊运作业
    const val TYPE_CLEAN = 4//清洗作业

    const val TYPE_MODE_MA = 1//手动作业
    const val TYPE_MODE_AB = 2//AB作业
    const val TYPE_MODE_AU = 3//自动作业
    //大田作业/自由航线 进入过航线模式-自动 没有则手动 type=播撒/喷洒/清洗(作业机类型)

    const val OPER_JAPAN = 0
    const val OPER_US = 1
    const val OPER_CN = 2

    var groupId: Long? = null
    var workMode = TYPE_MODE_MA
    var curTotalArea = 0.0
    var curDrug = 0f
    var isV9 = false

    //自动作业的参数
    var blockId: Long = 0
    var localBlockId: Long = 0
    var planId: Long = 0
    var localPlanId: Long = 0
    var workPercent: Int = 0
    var workDrug: Float = 0f
    var sortieArea0 = 0.0
    var sortieDrug0 = 0f
    var sortieArea = 0f//本架次作业面积
    var sortieDrug = 0f//本架次作业药量
    var workRoutePoint = mutableListOf<RoutePoint>()
    var bk: VKAg.BreakPoint? = null//架次需要的

    //最后一次航线home点位置
    var lastHomePos: GeoHelper.LatLngAlt? = null

    fun changeOperType(value: Int) {
//        handType = value
        when (value) {
            OPER_JAPAN -> setControllerParam("type", "jp")
            OPER_US -> setControllerParam("type", "us")
            OPER_CN -> setControllerParam("type", "cn")
        }
//        updateRcMode(value)
    }

    fun rcModeIndex(mode: String): Int {
        return when (mode) {
            "jp" -> OPER_JAPAN
            "us" -> OPER_US
            "cn" -> OPER_CN
            else -> -1
        }
    }

    private val rcListeners = ConcurrentLinkedDeque<(String, Any) -> Unit>()
    fun subscribeRcState(listener: (String, Any) -> Unit) {
        rcListeners.add(listener)
    }

    fun unsubscribeRcState(listener: (String, Any) -> Unit) {
        rcListeners.remove(listener)
    }

    var rcId = ""

    fun setEavRcDefaultParam() {
        controller.setParameters("config", "")
    }

    //请求断点的localBlockId，只有在请求VKag.breakpoint断点的时候才会修改值,断点处理完后置为null
    var reqBKLocalBlockId: Long? = null
    fun getBreakPoint(localBlockId: Long) {
        activeDrone?.let {
            reqBKLocalBlockId = localBlockId
            it.getBreakPoint()
        }
    }

    private var saveLocalBKJob: Job? = null
    private val ioScope = CoroutineScope(Dispatchers.IO)
    fun saveLocalBK(breakPoint: VKAg.BreakPoint) {
        saveLocalBKJob?.cancel()
        saveLocalBKJob = ioScope.launch {
            reqBKLocalBlockId?.let {
                try {
                    Repo.saveBreakpoint(it, breakPoint)
                }finally {
                    reqBKLocalBlockId = null
                    saveLocalBKJob = null // 确保清理引用
                }
            }
        }
    }

    //自动连接USB RTK
    fun autoRTKConnect() {
        val rtcmInfo = RtcmModel.rtcmInfo.value
        config?.let {
            if (it.autoUsbRTKConnect && (rtcmInfo == null || (rtcmInfo.source == RtcmModel.TYPE_USB && rtcmInfo.status < -1))) {
                openUsbStation()
                if (it.rtkType != Constants.RTK_TYPE_D_RTK) it.rtkType =
                    Constants.RTK_TYPE_D_RTK
            }
        }
    }
}
