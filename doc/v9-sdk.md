# V9 SDK

## Interface Description

### IController

A generic interface for remote controllers: all remote controllers implement this interface, and it's used in the DroneModel to handle the remote controllers' logic.

```Kotlin
interface IController {

    class RcVersion(val rc: String, val receiver: String, val gi: String, val si: String)

    interface Listener {
        // "connection" -> CONNECTED / CONNECTING / DISCONNECTED
        // "type" -> "jp" / "us" / "cn" / "unknown"
        // "id" -> ID
        // "state" -> "ok" / "error"
        // "rssi" -> rssi
        // "key" -> Array<String>
        // "version" -> RcVersion
        fun onControllerState(name: String, value: Any)
        fun onRadioData(index: Int, data: ByteArray)
    }

    fun sendRadio(index: Int, data: ByteArray)

    fun readId()
    fun readParameters()
    fun setParameters(cmd: String, value: String)
    fun sendRadioRtcm(rtcm: ByteArray)
}
```

#### Method Description

| Method         | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| sendRadio      | Send data to the flight controller, the index is always 0.   |
| readId         | Retrieve the remote controller ID.                           |
| readParameters | Retrieve basic information of the remote control, such as hand configuration settings, channel settings, etc. |
| setParameters  | Send remote control commands, such as reading channel values, setting hand gestures, etc. |

The above methods are all asynchronous methods, and the data is returned through ```IController.Listener```.

#### IController.Listener

| Method            | Description                                                  |
| ----------------- | ------------------------------------------------------------ |
| onControllerState | Return remote control information; see the table below for details. |
| onRadioData       | Return flight control data                                   |

####  Remote Control Information

| Name       | **Description**   | value         |
| ---------- | ----------------- | ------------- |
| connection | Connection Status |               |
| type       | Hand Type         | Mode 1 Mode 2 |
| id         | ID                |               |
| rssi       | Signal            |               |
| key        | Channel Mapping   |               |



### IMapCanvas

Map generic interface. All maps implement this interface, and it is used in MapBaseActivity, MapVideoBaseActivity, and their derived classes.

```Kotlin
abstract class IMapCanvas(val context: Context) {

    interface MapReadyListener {
        fun onMapReady(canvas: IMapCanvas)
    }
    interface MapChangeListener {
        fun onCameraChange(bearing: Float)
    }
    interface MapClickListener {
        fun onClick(pt: GeoHelper.LatLng)
    }
    interface MapLongClickListener {
        fun onLongClick(pt: GeoHelper.LatLng)
    }
    interface MapMarkerSelectListener {
        fun onMarkerSelect(marker: String)
    }

    interface MarkerDragListener {
        fun onDrag(name: String, pt: GeoHelper.LatLng)
        fun onDragStart(name: String, pt: GeoHelper.LatLng)
        fun onDragFinish(name: String, pt: GeoHelper.LatLng)
    }

    abstract class MarkerOption(
        val anchor: Int,
        val textColor: Int,
        val textSize: Float,
    ) {
        abstract fun draw(title: String, color: Int): Bitmap
    }
    fun applyMarkerOption(option: MarkerOption)

    class LatLngBound(val north: Double, val east: Double, val south: Double, val west: Double)

    val phoneLocation = MutableLiveData<GeoHelper.LatLng>()
 
    fun addMarkerDragListener(l: MarkerDragListener)
    fun removeMarkerDragListener(l: MarkerDragListener)
    fun addClickListener(l: MapClickListener)
    fun removeClickListener(l: MapClickListener)
    fun addLongClickListener(l: MapLongClickListener)
    fun removeLongClickListener(l: MapLongClickListener)
    fun addMarkClickListener(l: MapMarkerSelectListener)
    fun removeMarkClickListener(l: MapMarkerSelectListener)
    fun addChangeListener(l: MapChangeListener)
    fun removeChangeListener(l: MapChangeListener)

    abstract fun moveMap(lat: Double, lng: Double, zoom: Float)
    abstract fun moveMap(lat: Double, lng: Double)
    abstract fun fixMap()

    abstract fun clear()
    abstract fun remove(name: String)
    abstract fun findBlock(pt: GeoHelper.LatLng): List<String>

    abstract fun drawMarker(name: String, lat: Double, lng: Double, resourceId: Int, z: Int, anchor: Int = Params.ANCHOR_CENTER)
    abstract fun drawLetterMarker(name: String, lat: Double, lng: Double, letter: String, color: Int)

    abstract fun highlightLetterMarker(name: String, letter: String, color: Int, highlight: Boolean)
    abstract fun highlightBlock(name: String, highlight: Boolean)

    abstract fun fit()
    abstract fun fit(names: List<String>)
    abstract fun toLatLng(x: Int, y: Int): GeoHelper.LatLng
    abstract fun toPoint(lat: Double, lng: Double): Point2D

    abstract val angle: Float
    abstract val centerPoint: GeoHelper.LatLng?
    abstract val boundingBox: LatLngBound

    protected abstract fun drawPolygon()
    protected abstract fun drawPolygonWithHoles()
    protected abstract fun drawLines()

    fun drawNumberMarker(pts: List<GeoHelper.LatLng>)
    fun clearNumberMarker()
    fun indexOfNumberMarker(name: String)
    fun nameOfNumberMarker(index: Int)

    fun drawBlock(
        name: String, block: MapRing, hasEdge: Boolean,
        color: Int = Params.BLOCK_FILL_COLOR,
        strokeColor: Int = Params.BLOCK_STROKE_COLOR,
        strokeWidth: Float = Params.BLOCK_WIDTH
    )

    fun drawForbiddenZone(name: String, block: MapRing)
    fun drawBlockWithHoles(name: String, block: MapBlock)
    fun drawBarrier(name: String, barrier: MapRing, hasEdge: Boolean)
    fun drawEdge(name: String, pts: MapTrack)
    fun drawTrack(name: String, pts: MapTrack)
    fun drawRing(name: String, pts: MapRing, color: Int)
    fun drawPolyline(name: String, pts: MapTrack, color: Int, width: Float)
    fun drawCompletion(name: String, pts: MapTrack)
    fun drawLine(name: String, pts: MapTrack, color: Int, z: Int, width: Float)
}
```

#### Method Description

Map features (polygons, lines, markers, etc.) are identified by name, and the name must be unique.

| Method                | Description                                                  |
| --------------------- | ------------------------------------------------------------ |
| addXXXListener        | Add listener                                                 |
| removeXXXListener     | Remove listener                                              |
| moveMap               | Move Map                                                     |
| fixMap                | Used to disable map rotation (in cases of special requirements) |
| clear                 | Delete all map markers                                       |
| remove                | Delete specified map markers                                 |
| findBlock             | Find the polygon containing the specified coordinates        |
| drawMarker            | Draw Marker with icon                                        |
| drawLetterMarker      | Draw Marker with Text Marker                                 |
| highlightLetterMarker | Highlight Marker with Text Tag                               |
| highlightBlock        | Highlight Polygon                                            |
| fit                   | Resize map to include all (or specified) markers             |
| toLatLng              | Screen coordinates converted to latitude and longitude       |
| toPoint               | Convert latitude and longitude to screen coordinates (note that conversion is not successful under all conditions) |
| angle                 | Current rotation angle of the map                            |
| centerPoin            | Map current center coordinates                               |
| boundingBox           | The range of the map currently displayed (note that this is not correct under all conditions) |
| drawPolygon           | Draw Polygons                                                |
| drawPolygonWithHoles  | Draw polygons with holes                                     |
| drawLines             | Draw Lines                                                   |
| drawNumberMarker      | Draw a bunch of dots with serial numbers                     |
| clearNumberMarker     | Delete point with serial number                              |
| indexOfNumberMarker   | Find the serial number of this Marker according to the name  |
| nameOfNumberMarker    | Find the name according to the serial number                 |

#### Listener

```MapReadyListener``` is used to notify that the map has been initialized. When the map is not properly initialized, operations on the map may cause exceptions.

```MapChangeListener``` is used to notify when the map changes (move, zoom, rotate, etc.).

```MapClickListener``` is used to notify when the map is clicked.

```MapLongClickListener``` is used to notify when the map is long pressed.

```MapMarkerSelectListener``` is used to notify when a Marker is clicked.

```MarkerDragListener``` is used to notify when a Marker is dragged.

### VKAgProtocol

This class is used for communication with the flight controller.

#### Method Description

| Method                                                       | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| lenSwitch(int type, boolean open)                            | Spotlight switch. The default value of type is 3, which means turning on/off both front and rear spotlights at the same time |
| startCan(boolean start)                                      | Start CAN debugging                                          |
| openCan(boolean open)                                        | Open CAN debugging                                           |
| takeOff()                                                    | The drone only performs the takeoff action and hovers after takeoff |
| setRadarF()                                                  | Set forward obstacle avoidance                               |
| setRadarB()                                                  | Set rear obstacle avoidance                                  |
| setRadarReset(int content)                                   | Radar factory reset. Content represents resetting the front/rear/ground radar to factory settings |
| forceUnlock(int type)                                        | Force unlock when drone positioning is poor                  |
| startWaypoint()                                              | Drone starts operation                                       |
| resumeWaypoint()                                             | Drone continues operation                                    |
| startFromBreakpoint()                                        | Drone starts breakpoint operation                            |
| hover()                                                      | Drone hovers                                                 |
| finishWork(int type)                                         | Let the drone complete the current operation. Type: AB operation completed/Autonomous route completed |
| takeoffGotoWork(int type)                                    | Drone takes off and starts route operation. Type: Start AB operation/Start autonomous route |
| readId()                                                     | Get drone version information and MSG_IDLIST device serial number list |
| replyIdList(short devType, short devNum, boolean ok)         | Reply to the flight controller whether MSG_IDLIST device information is received. The flight controller will continue to send the next device information only after receiving successful feedback from the app |
| readNoFlyZone()                                              | Get no-fly zone information                                  |
| setBreakPoint(VKAg.BreakPoint bp)                            | Set flight breakpoint information                            |
| setABPoint(byte cmdId, double lat, double lng)               | Set AB point position information. cmdId: A point position/B point position |
| setABAngle(byte cmdId, float angle)                          | Set AB point angle. cmdId: A point angle/B point angle       |
| setABDirection(int dir, float angleA, float angleB)          | Set AB direction. dir: Left/Right                            |
| getBreakPoint()                                              | Get breakpoint information                                   |
| getWLParam()                                                 | Get autonomous route parameters (altitude, speed)            |
| goHome()                                                     | Drone returns home                                           |
| land()                                                       | Drone lands in place                                         |
| openPump(boolean open)                                       | Turn water pump on/off                                       |
| changeReport(int type)                                       | Request data distribution. Type: MSG_PWM/MSG_OKCELL/MSG_DEVICE |
| remoteLock(int lock)                                         | Drone unlock/lock                                            |
| clearNoFlyZone()                                             | Clear no-fly zone                                            |
| localSim(int sim)                                            | Set simulator on/off                                         |
| getLocalSim()                                                | Get simulator on/off status                                  |
| getManageInfo()                                              | Request MSG_MAN management message                           |
| calibMagnet()                                                | Magnetometer calibration                                     |
| calibHorz()                                                  | Level calibration                                            |
| factoryReset()                                               | Drone parameter factory reset                                |
| calibController()                                            | Calibrate remote controller                                  |
| stopCalibController(int status)                              | Cancel/Complete remote controller calibration. Status: Cancel/Complete |
| calibMotor(int index)                                        | Check drone motor. Index is the motor serial number          |
| setMotorNumber(int number)                                   | Set motor number                                             |
| calibVoltage(float volt)                                     | Non-smart battery voltage calibration                        |
| setPumpMode(int mode)                                        | Set spraying mode. Mode: Fixed/Speed-based                   |
| startCalibEFT()                                              | Start flowmeter calibration                                  |
| endCalibEFT()                                                | Cancel flowmeter calibration                                 |
| calibFlowChart(int flow, int flow2)                          | CAN flowmeter calibration. Flow1: flow in cup 1 of flowmeter 1 in ml, Flow2: flow in cup 2 of flowmeter 2 in ml |
| resetFlow()                                                  | Flowmeter factory reset                                      |
| clearBgFlow()                                                | Flowmeter background zeroing                                 |
| calibBumpChart(int status)                                   | Water pump calibration curve. Cmd: Start/Cancel              |
| calibMaterialRadar(int cmd)                                  | Material radar calibration. Cmd: Start/Cancel               |
| calibLinePump(int cmd, int content)                          | Calibrate linear water pump. Cmd: Start/Cancel. Content: Water pump 1/Water pump 2 |
| setLinePump(int cmd, float content)                          | Set linear water pump value. Cmd: Water pump 1/Water pump 2. Content: Water pump value |
| setBumpParam(float qty, float param)                         | Set MSG_FLOW flow tank information                           |
| getBumpParam()                                               | Get MSG_FLOW flowmeter information                           |
| getParameters()                                              | Get MSG_APTYPE parameter list                                |
| getPumpData()                                                | Get MSG_PUMP water pump information                          |
| setIndexedParameter(int index, int value)                    | Set MSG_APTYPE parameter. Used when unit conversion is not required |
| setParameter(int index, float value)                         | Set MSG_APTYPE parameter. Used when unit conversion is required |
| getPidParameters()                                           | Get PID parameters                                           |
| setPidParameter(int idx, int value)                          | Set PID parameters                                           |
| getChannelMapping()                                          | Get channel mapping                                          |
| setChannelMapping(@NonNull byte[] mapping, @NonNull byte[] functions) | Set channel mapping                                          |
| calibSeeder(byte cmd, int param)                             | Calibration function (weight calibration of weighing sensor/weight zeroing of weighing sensor/flowmeter factory reset all use this function) |
| setEFTWeightK(byte cmd, int param)                           | Set weighing sensor K value                                  |
| calibSeederFlow(int status)                                  | Seeder flow calibration. Status: Start/Cancel calibration   |
| responseToStartNavi(boolean allow)                           | Whether to allow direct route start. Allow: true to start route directly, false to wait for route start (used when avoiding obstacles) |
| responseToReturnNavi(boolean allow)                          | Whether to allow direct return to home. Allow: true to return directly, false to wait for route completion |
| startSimulator(double lat, double lng)                       | When the simulator function is turned on, set the drone position |
| requestUserPackage(int type)                                 | Request MSG_CAL_SEED seeder calibration chart information (request after seeder flow calibration, save parameters after receiving this information) |
| requestLinePumpData()                                        | Request linear water pump parameters                         |
| setUserData(int type, byte[] data)                           | Used when setting seeder material template                   |
| setLinePumpData(byte[] data)                                 | Send MSG_CAL_L_PUMP linear water pump calibration chart data |
| isRecognizedFlowOrSeeder()                                   | Whether the drone is currently in spraying mode or seeder mode |
| readLogV9(int index)                                         | Read flight controller log. Index is the log number          |
| stopReadLog()                                                | Cancel reading flight controller log                         |
| uploadStartWaypoints(@NotNull List<RoutePoint> route, float height, float speed) | Upload start route (when there are transfer points or obstacle avoidance is needed, the obstacle avoidance route needs to be uploaded, and the drone will fly according to the obstacle-avoided path) |
| uploadEndWaypoints(@NotNull List<RoutePoint> route, float height, float speed) | Upload end route (same as start route, used when there are transfer points or obstacle avoidance is needed during return to home) |
| uploadNavi3Points(@NotNull List<RoutePoint> route, float speed) | Upload autonomous route                                      |
| setNaviProperties(UploadNaviData param)                      | Set route parameters                                         |
| stopUploadNaviPoints()                                       | Cancel route upload                                          |
| uploadFirmware(int component, @NotNull InputStream input)    | Upgrade firmware. Component: firmware type, FMU/PMU; input: file stream |
| stopUploadFirmware()                                         | Cancel firmware upgrade                                      |
| clear(@NonNull String prop)                                  | Clear AB points ("ab")                                       |
| requireABInfo()                                              | Request AB point information                                 |
| uploadABInfo(@NonNull VKAg.ABPLData abpl)                    | Upload AB point information                                  |
| sendRtcm(@NonNull byte[] data, int len)                      | Send RTCM data                                               |
| getLogList()                                                 | Get V9 flight controller log list                            |
| sendLogListReq()                                             | Request V9 flight controller log list                        |
| sendRawPackage(byte cmdId, byte[] data)                      | Send raw data (used when not going through the library and splicing flight controller data by yourself) |

### RouteModel

This class is used for route planning.

#### Method Description

| Method                                                       | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| findNaviLine(target: Int, bk: GeoHelper.LatLng?)             | Used to calculate the optimal route. Pass in the route target point and breakpoint, remove the route planned before the breakpoint, and use the remaining route to find the nearest point to the drone for optimal route calculation |
| planType2VK(type: Int): Int                                  | Convert planning type to VK protocol type. A set of types is used in the project, and planning needs to be converted to VK protocol when saving and uploading |
| vk2PlanType(vk: Int): Int                                    | Convert VK protocol type to planning type                    |
| initParam()                                                  | Initialize planning parameters                               |
| addObstacles(list: List<List<GeoHelper.LatLngAlt>>)          | Add obstacles (there may be multiple obstacles)              |
| setRCurEdge(e: Int)                                          | Set the reference edge during planning (which edge the planned route is parallel to) |
| getRCurEdge(): Int                                           | Get the current edge. Need to record the reference edge of this planning when saving |
| setup(block: MapRing3D, bars: MapBlock3D)                    | Initialize field boundary and obstacles. Need to initialize before planning routes |
| calcRoute(byAngle: Boolean = false)                          | Start route planning. byAngle: true to plan by angle, false to plan by reference edge |
| sortRoute(pt: GeoHelper.LatLng)                              | Re-plan the route after modifying the starting point         |
| sortNaviByBreak()                                            | If there is no breakpoint when calculating the optimal route, there is no need to delete the route before the breakpoint, and the optimal route can be calculated directly |
| makeNaviPts(list: List<RoutePoint>, complete: () -> Unit?)   | When selecting a field to continue operation, restore the data needed for planning through the last planned route, for later modification of planning and calculation of optimal route |

### OutPathRouteModel

This class is used for route obstacle avoidance at the start.

#### Method Description

| Method                                                       | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| findStartPathByBarrier(     dronePosition: GeoHelper.LatLng?,     auxPoints: List<GeoHelper.LatLng>,     allBarriers: List<List<GeoHelper.LatLngAlt>>,     block: List<GeoHelper.LatLngAlt>,     firstPosition: GeoHelper.LatLng?,     complete: (List<GeoHelper.LatLng>?) -> Unit, ) | Used for takeoff obstacle avoidance. dronePosition: Current drone position; auxPoints: Transfer point array; allBarriers: List of all obstacles in this field; block: Field boundary point list; firstPosition: First point of route; complete: Obstacle-avoided waypoint list = Current drone position + Obstacle-avoided point of transfer point 1 + Obstacle-avoided point of transfer point 2 + ... + Obstacle-avoided point of transfer point N + First waypoint. If obstacle avoidance fails, return null. When obstacle avoidance fails, prompt the user to delete/modify the transfer point position for re-obstacle avoidance |
| findEndPathByBarrier(     dronePosition: GeoHelper.LatLng?,     auxPoints: List<GeoHelper.LatLng>,     allBarriers: List<List<GeoHelper.LatLngAlt>>,     block: List<GeoHelper.LatLngAlt>,     homePosition: GeoHelper.LatLng?,     complete: (List<GeoHelper.LatLng>?) -> Unit, ) | Used for return obstacle avoidance. dronePosition: Current drone position; auxPoints: Transfer point array; allBarriers: List of all obstacles in this field; block: Field boundary point list; homePosition: Return point position; complete: Obstacle-avoided waypoint list = Current drone position + Obstacle-avoided point of transfer point N + Obstacle-avoided point of transfer point N-1 + ... + Obstacle-avoided point of transfer point 1 + Return point. If obstacle avoidance fails, return null. When obstacle avoidance fails, prompt the user to delete/modify the transfer point position for re-obstacle avoidance |



### Data Structures

Some data structures used in the library.

#### Data Structure Description

| Data Type                                                | Description              |
| ------------------------------------------------------- | ------------------------- |
| GeoHelper.LatLng(double lat, double lng)                | Latitude, Longitude       |
| GeoHelper.LatLngAlt(double lat, double lng, double alt) | Latitude, Longitude, Altitude |
| MapTrack | List of latitude and longitude |
| MapTrack3D                                              | List of latitude, longitude and altitude |