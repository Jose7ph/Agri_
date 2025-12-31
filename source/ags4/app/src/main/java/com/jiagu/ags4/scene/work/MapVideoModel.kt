package com.jiagu.ags4.scene.work

import android.app.Application
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.Constants
import com.jiagu.ags4.R
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd

val LocalMapVideoModel = compositionLocalOf<MapVideoModel> {
    error("No LocalMapVideoModel provided")
}

class MapVideoModel(app: Application) : AndroidViewModel(app) {

    enum class SettingType {
        SETTINGS_TYPE_FLYING, //飞行
        SETTINGS_TYPE_SPRAYING, //喷洒
        SETTINGS_TYPE_RADAR, //雷达
        SETTINGS_TYPE_RTK, //RTK
        SETTINGS_TYPE_BATTERY, //电池
        SETTINGS_TYPE_WORK_MACHINA, //作业机
        SETTINGS_TYPE_OTHER, //其他
    }

    val config = Config(app)

    var sprayingType by mutableIntStateOf(1)
    var rtkSource by mutableIntStateOf(Constants.RTK_TYPE_NTRIP)
    var rtkInfo by mutableStateOf("")

    var airFlag by mutableStateOf(VKAg.AIR_FLAG_ON_GROUND)
    var showSetting by mutableStateOf(false)
    var selectedSettingButtonId by mutableStateOf(SettingType.SETTINGS_TYPE_FLYING)
    var mapFollowMode by mutableIntStateOf(0) // 0:OFF, 1:RC, 2:Drone
    var workModeEnum by mutableStateOf(WorkModeEnum.MANUAL)
    var needUploadNavi = false

    //用于控制作业菜单切换弹出控制，在部分作业时不允许弹出菜单
    var workModeEnabled by mutableStateOf(true)

    var phonePosition: GeoHelper.LatLngAlt? = null
    var workLineTail = 0

    var ntripAccount = ""
    var ntripPass = ""
    var ntripMountPoint = ""
    var ntripPort = ""
    var ntripHost = ""

    var warnings = mutableStateListOf<NewWarnTool.WarnStringData>()

    var engineType by mutableIntStateOf(-1) //TYPE_BATTERY / TYPE_SMART_BATTERY / TYPE_ENGINE / TYPE_HYDROGEN_BATTERY

    //航线作业 左侧地块列表显示开关
    var showParam by mutableStateOf(true)

    //点击状态栏显示的数据类型 -1 不显示
    var showDetailsType by mutableIntStateOf(VKAg.INFO_IMU)

    //地图工具显示开关
    var showMapTools by mutableStateOf(true)

    fun showInfo(type: Int) {
        if (type < INFO_HISTORY) {
            DroneModel.activeDrone?.changeReport(type)
        }
    }

    fun hideInfoPanel() {
        if (showDetailsType != VKAg.INFO_IMU) {
            showDetailsType = VKAg.INFO_IMU
            DroneModel.activeDrone?.changeReport(VKAg.INFO_IMU)
        }
    }

    fun checkAirFlag(imuData: VKAg.IMUData?) {
        if (imuData == null) return
        engineType = imuData.energyType
        if (airFlag != imuData.airFlag) {
            airFlag = imuData.airFlag
        }
    }

    var locationType by mutableStateOf(config.locationType)

    fun changeLocationType(type: String) {
        locationType = type
        config.locationType = type
    }

    companion object {
        const val INFO_HISTORY = 1000
    }

}

enum class WorkMachinaMenuEnum(val menuEnums: Array<WorkModeEnum>) {
    COMMON(
        arrayOf(
            WorkModeEnum.LARGE_FIELD,
            WorkModeEnum.AB,
            WorkModeEnum.FREE_AIR_ROUTE,
            WorkModeEnum.TREE_AIR_ROUTE,
            WorkModeEnum.MANUAL,
            WorkModeEnum.ENHANCED_MANUAL,
        )
    ),
    CLEANING(
        arrayOf(
            WorkModeEnum.MANUAL,
            WorkModeEnum.AREA_CLEAN,
            WorkModeEnum.CLEAN_HORIZONTAL_AB,
            WorkModeEnum.AB_CLEAN,
        )
    )
}

/**
 * 根据作业机类型获取菜单
 */
fun getWorkMenuByDroneType(droneType: Int?): WorkMachinaMenuEnum {
    //获取当前作业机对应的页面enum
    return when (droneType) {
        //清洗机
        VKAgCmd.DRONE_TYPE_WASHING.toInt() -> {
            WorkMachinaMenuEnum.CLEANING
        }
        //其他
        else -> {
            WorkMachinaMenuEnum.COMMON
        }
    }
}

//注意:若需要同时出现多个使用相同组件的页面，请创建新的页面url，从navHost中控制该url跳转的组件(如：果树航线/自由航线、AB作业/水平AB清洗)
enum class WorkModeEnum(val modeName: Int, val image: Int, val url: String) {
    LARGE_FIELD(
        R.string.work_type_large_field,
        R.drawable.default_route_mode,
        WorkPageEnum.WORK_BLOCK.url
    ),
    AB(R.string.work_type_ab, R.drawable.default_ab_mode, WorkPageEnum.WORK_AB.url),
    MANUAL(
        R.string.work_type_ma,
        R.drawable.default_manual_mode,
        WorkPageEnum.WORK_MANUAL.url
    ),
    FREE_AIR_ROUTE(
        R.string.work_type_free_air_route,
        R.drawable.default_free_air_mode,
        WorkPageEnum.WORK_FREE_AIR_ROUTE.url
    ),
    TREE_AIR_ROUTE(
        R.string.work_type_tree_air_route,
        R.drawable.default_tree_air_mode,
        WorkPageEnum.WORK_FREE_AIR_ROUTE.url
    ),
    AB_CLEAN(
        R.string.work_type_vertical_ab_clean,
        R.drawable.default_ab_v_mode,
        WorkPageEnum.WORK_AB_CLEAN.url
    ),
    CLEAN_HORIZONTAL_AB(
        R.string.work_type_horizontal_ab_clean,
        R.drawable.default_ab_h_mode,
        WorkPageEnum.WORK_AB.url
    ),
    AREA_CLEAN(
        R.string.work_type_area_clean,
        R.drawable.default_free_air_mode,
        WorkPageEnum.WORK_AREA_CLEAN.url
    ),
    ENHANCED_MANUAL(
        com.jiagu.v9sdk.R.string.work_type_enhanced_manual,
        R.drawable.default_manual_enhanced_mode,
        WorkPageEnum.WORK_ENHANCED_MANUAL.url
    ),
    ;

    companion object {
        fun getAllModelUrl(): List<String> {
            return entries.map { it.url }.toList()
        }
    }
}

enum class WorkPageEnum(val url: String) {
    WORK_MANUAL("work_manual"),
    WORK_AB("work_ab"),
    WORK_AB_START("work_ab_start"),
    WORK_AB_CLEAN("work_ab_clean"),
    WORK_AB_CLEAN_START("work_ab_clean_start"),
    WORK_AREA_CLEAN("work_area_clean"),
    WORK_AREA_CLEAN_EDIT("work_area_clean_edit"),
    WORK_AREA_CLEAN_PARAM("work_area_clean_param"),
    WORK_AREA_CLEAN_START("work_area_clean_start"),
    WORK_BLOCK("work_block"),
    WORK_BLOCK_EDIT("work_block_edit"),
    WORK_BLOCK_DIVISION("work_block_division"),
    WORK_BLOCK_PARAM("work_block_param"),
    WORK_BLOCK_START("work_block_start"),
    WORK_FREE_AIR_ROUTE("work_free_air_route"),
    WORK_FREE_AIR_ROUTE_EDIT("work_free_air_route_edit"),
    WORK_FREE_AIR_ROUTE_PARAM("work_free_air_route_param"),
    WORK_FREE_AIR_ROUTE_START("work_free_air_route_start"),
    WORK_ENHANCED_MANUAL("work_enhanced_manual"),
    WORK_LOCATOR("work_locator"),
}

enum class PointTypeEnum(val typeName: Int) {
    //    CALIB(),//校准点
    EDGE(R.string.voice_edge_pt),
    OBSTACLE(R.string.voice_barrier_pt),
    CIRCLE_OBSTACLE(R.string.voice_barrier_circle_pt);

    companion object {
        fun getPointTypeByIndex(index: Int): PointTypeEnum {
            return PointTypeEnum.entries.getOrNull(index) ?: EDGE
        }
    }
}

enum class LocationTypeEnum(val type: String, val typeName: Int) {
    DRONE("drone", R.string.locate_type_device), MAP(
        "map",
        R.string.locate_type_map
    ),
    PHONE("phone", R.string.locate_type_rc), LOCATOR("locator", R.string.locate_type_locator);

    companion object {
        fun getTypeNameByType(type: String): Int {
            return LocationTypeEnum.entries.find { it.type == type }?.typeName ?: 0
        }

        fun getTypeByIndex(index: Int): String {
            return LocationTypeEnum.entries.getOrNull(index)?.type ?: ""
        }

        fun getIndexByType(type: String): Int {
            return LocationTypeEnum.entries.indexOfFirst { it.type == type }
        }
    }
}