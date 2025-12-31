package com.jiagu.ags4.scene.work

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jiagu.ags4.MapBaseActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Sortie
import com.jiagu.ags4.repo.Repo
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.repo.sp.DeviceConfig
import com.jiagu.ags4.scene.work.settings.SettingsView
import com.jiagu.ags4.scene.work.settings.WorkMachinaSwitch
import com.jiagu.ags4.ui.components.BatteryStatusDetails
import com.jiagu.ags4.ui.components.CleanFlightInformationPopup
import com.jiagu.ags4.ui.components.DetailType
import com.jiagu.ags4.ui.components.FlightInformation
import com.jiagu.ags4.ui.components.FlightInformationPopup
import com.jiagu.ags4.ui.components.FreeAirRouteFlightInformationPopup
import com.jiagu.ags4.ui.components.GPSStatusDetails
import com.jiagu.ags4.ui.components.PeerS1Details
import com.jiagu.ags4.ui.components.ProgressPopup
import com.jiagu.ags4.ui.components.RTKStatusDetails
import com.jiagu.ags4.ui.components.StatusBar
import com.jiagu.ags4.ui.theme.ComposeTheme
import com.jiagu.ags4.ui.theme.DarkAlpha
import com.jiagu.ags4.ui.theme.LightDarkAlpha
import com.jiagu.ags4.ui.theme.MAP_VIDEO_HEIGHT
import com.jiagu.ags4.ui.theme.MAP_VIDEO_WIDTH
import com.jiagu.ags4.ui.theme.RADAR_BOX_HEIGHT
import com.jiagu.ags4.ui.theme.RADAR_BOX_LEFT_PADDING
import com.jiagu.ags4.ui.theme.WhiteAlpha
import com.jiagu.ags4.utils.AptypeUtil
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.utils.LocalProgressModel
import com.jiagu.ags4.utils.RTKHelper
import com.jiagu.ags4.utils.getViewModel
import com.jiagu.ags4.utils.isSeedWorkType
import com.jiagu.ags4.utils.parsePercent
import com.jiagu.ags4.utils.popToRoot
import com.jiagu.ags4.utils.taskComplete
import com.jiagu.ags4.vm.ABCleanModel
import com.jiagu.ags4.vm.ABModel
import com.jiagu.ags4.vm.AreaCleanModel
import com.jiagu.ags4.vm.BlockDivisionModel
import com.jiagu.ags4.vm.BlockEditModel
import com.jiagu.ags4.vm.BlockModel
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.DroneModel.deviceSeedData
import com.jiagu.ags4.vm.DroneModel.deviceWeightData
import com.jiagu.ags4.vm.DroneModel.sortieAreaData
import com.jiagu.ags4.vm.EnhancedManualModel
import com.jiagu.ags4.vm.FreeAirRouteEditModel
import com.jiagu.ags4.vm.FreeAirRouteModel
import com.jiagu.ags4.vm.FreeAirRouteParamModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.ags4.vm.LocatorModel
import com.jiagu.ags4.vm.RtcmModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.ags4.vm.TrackModel
import com.jiagu.api.ext.dp2px
import com.jiagu.api.ext.formatSecond
import com.jiagu.api.ext.toString
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.vkprotocol.NewWarnTool
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.device.vkprotocol.VKAgCmd
import com.jiagu.device.vkprotocol.VKAgTool
import com.jiagu.device.widget.VideoFrame
import com.jiagu.jgcompose.button.SwitchButton
import com.jiagu.jgcompose.dialog.DialogViewModel
import com.jiagu.jgcompose.ext.disableAutoFocus
import com.jiagu.jgcompose.ext.hideDialog
import com.jiagu.jgcompose.ext.showDialog
import com.jiagu.jgcompose.popup.PromptPopup
import com.jiagu.jgcompose.radar.RadarInstrument
import com.jiagu.jgcompose.radar.RadarTextData
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.tools.ext.UnitHelper
import com.jiagu.tools.map.IMapCanvas
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class MapVideoActivity : MapBaseActivity(true), View.OnClickListener {
    private lateinit var navController: NavHostController
    val mapVideoModel: MapVideoModel by viewModels()
    val trackModel: TrackModel by viewModels()
    private val dialogVM: DialogViewModel by viewModels()
    private val progressModel: ProgressModel by viewModels()
    private lateinit var config: Config
    private lateinit var deviceConfig: DeviceConfig

    companion object {
        const val STATE_NO_VIDEO = 0
        const val STATE_MAP = 1
        const val STATE_VIDEO = 2
        const val STATE_VIDEO2 = 3
    }

    private var videoState by mutableIntStateOf(STATE_MAP)
    private fun switchMode(mode: Int) {
        if (mode == videoState) return
        videoState = mode
        val w = dp2px(MAP_VIDEO_WIDTH)
        val h = dp2px(MAP_VIDEO_HEIGHT)
        val bigLayout = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        )//全屏
        val smallLayout = RelativeLayout.LayoutParams(w, h)
        smallLayout.addRule(RelativeLayout.ALIGN_PARENT_START)
        smallLayout.bottomMargin = dp2px(2f)
        smallLayout.leftMargin = dp2px(2f)
        smallLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        val smallLayout2 = RelativeLayout.LayoutParams(w, h)
        smallLayout2.addRule(RelativeLayout.ALIGN_PARENT_START)
        smallLayout2.bottomMargin = dp2px(92f)
        smallLayout2.leftMargin = dp2px(2f)
        smallLayout2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

        when (videoState) {
            STATE_NO_VIDEO -> {
                mapbox.enableInterceptTouchEvent(false)
                mapbox.layoutParams = bigLayout
                video.visibility = View.GONE
                video2.visibility = View.GONE
            }

            STATE_MAP -> {//地图全屏
                mapbox.enableInterceptTouchEvent(false)
                mapbox.layoutParams = bigLayout
                video.layoutParams = smallLayout
                video.visibility = View.VISIBLE
                video.parent.bringChildToFront(video)
                if (deviceConfig.gimbalCount == 2) {
                    video2.layoutParams = smallLayout2
                    video2.visibility = View.VISIBLE
                    video2.parent.bringChildToFront(video2)
                }

            }

            STATE_VIDEO -> {//视频全屏
                mapbox.enableInterceptTouchEvent(true)
                mapbox.layoutParams = smallLayout
                video.layoutParams = bigLayout
                video.visibility = View.VISIBLE
                video.parent.bringChildToFront(mapbox)
                if (deviceConfig.gimbalCount == 2) {
                    video2.layoutParams = smallLayout2
                    video2.visibility = View.VISIBLE
                    video2.parent.bringChildToFront(video2)
                }

            }

            STATE_VIDEO2 -> {//视频全屏
                mapbox.enableInterceptTouchEvent(true)
                mapbox.layoutParams = smallLayout
                video2.layoutParams = bigLayout
                video2.visibility = View.VISIBLE
                video2.parent.bringChildToFront(mapbox)
                video.layoutParams = smallLayout2
                video.visibility = View.VISIBLE
                video.parent.bringChildToFront(video)

            }
        }
    }

    private val video by lazy { findViewById<VideoFrame>(R.id.video) }
    private val video2 by lazy { findViewById<VideoFrame>(R.id.video2) }
    private var firstReceiveDronePosition = false
    private var dronePosition: GeoHelper.LatLng? = null
    private var phonePosition: GeoHelper.LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = Config(this)
        deviceConfig = DeviceConfig(this)
        video.setOnClickListener(this)
        mapbox.setOnClickListener(this)

        switchMode(STATE_MAP)
        findViewById<ComposeView>(R.id.fragment).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val workPage by workPage.observeAsState()
                val act = this@MapVideoActivity
                navController = rememberNavController()
                // 添加监听
                LaunchedEffect(Unit) {
                    if (savedInstanceState == null) {
                        navController.addOnDestinationChangedListener { _, _, _ ->
                            // 创建新的回调并保存引用
                            object : OnBackPressedCallback(true) {
                                override fun handleOnBackPressed() {
                                    routeSkip()
                                }
                            }.also { callback ->
                                onBackPressedDispatcher.addCallback(act, callback)
                            }
                        }
                    }
                }
                //不显示雷达的页面
                val notShowRadarParamPage = listOf(
                    WorkPageEnum.WORK_BLOCK_PARAM.url,
                    WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
                    WorkPageEnum.WORK_AREA_CLEAN_PARAM.url,
                    WorkPageEnum.WORK_BLOCK_DIVISION.url,
                    WorkPageEnum.WORK_LOCATOR.url,
                )
                //有地图打点的编辑页面也不显示雷达
                val editPage = listOf(
                    WorkPageEnum.WORK_BLOCK_EDIT.url, WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url
                )
                //true 说明不显示雷达
                val notShowEditPageRadar =
                    editPage.contains(workPage) && mapVideoModel.locationType != "drone"


                CompositionLocalProvider(
                    LocalNavController provides navController,
                    LocalMapVideoModel provides mapVideoModel,
                    LocalProgressModel provides progressModel
                ) {
                    ComposeTheme {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .disableAutoFocus()
                        ) {
                            StatusBar(onClickBack = {
                                routeSkip()
                            }, onClickSetting = {
                                mapVideoModel.showSetting = !mapVideoModel.showSetting
                            })
                            if (mapVideoModel.engineType == VKAg.TYPE_ENGINE) {
                                EnergyControl()
                            }
                            //不显示雷达
                            if (!notShowRadarParamPage.contains(workPage) && !notShowEditPageRadar) {
                                RadarPanel()
                            }
                            WorkPage()
                            WarningsPage()
                            //显示工具栏 或者 没有点击statusbar的详情 才显示工具栏
                            if (mapVideoModel.showMapTools && mapVideoModel.showDetailsType == VKAg.INFO_IMU) {
                                MapTools()
                            }
                            when (mapVideoModel.showDetailsType) {
                                VKAg.INFO_BATTERY -> BatteryStatusDetails()
                                DetailType.GNSS.i -> GPSStatusDetails()
                                DetailType.RTK.i -> RTKStatusDetails()
                                DetailType.PEER_S1.i -> PeerS1Details()
                                else -> {}
                            }
                            if (mapVideoModel.showSetting) {
                                SettingsView()
                            }
                            val dialogState by dialogVM.dialogState.collectAsState()
                            if (dialogState.isVisible) {
                                dialogState.content?.let { it() }
                            }
                        }
                    }
                }
            }
        }
        addObserver()
        EventBus.getDefault().register(this)
    }

    private fun routeSkip() {
        when (workPage.value) {
            //一级菜单统一操作为finish
            WorkPageEnum.WORK_AB.url,
            WorkPageEnum.WORK_BLOCK.url,
            WorkPageEnum.WORK_AREA_CLEAN.url,
            WorkPageEnum.WORK_AB_CLEAN.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE.url,
            WorkPageEnum.WORK_MANUAL.url,
            WorkPageEnum.WORK_ENHANCED_MANUAL.url,
                -> {
                finish()
            }

            WorkPageEnum.WORK_AB_START.url,
            WorkPageEnum.WORK_AB_CLEAN_START.url,
            WorkPageEnum.WORK_LOCATOR.url,
                -> {
                flyBackPopup() //返回上一页
            }

            WorkPageEnum.WORK_AREA_CLEAN_START.url,
            WorkPageEnum.WORK_BLOCK_START.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_START.url,
                -> {
                flyBackPopup(true)//返回到列表页
            }

            WorkPageEnum.WORK_AREA_CLEAN_EDIT.url,
            WorkPageEnum.WORK_BLOCK_DIVISION.url,
            WorkPageEnum.WORK_BLOCK_EDIT.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url,
                -> {
                editBackPopup()
            }

            WorkPageEnum.WORK_BLOCK_PARAM.url,
            WorkPageEnum.WORK_AREA_CLEAN_PARAM.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
                -> {
                navController.popBackStack()
            }

            else -> {
                val backResult = navController.popBackStack()
                if (!backResult) finish()
            }
        }
    }

    private fun editBackPopup() {
        showDialog {
            PromptPopup(
                title = stringResource(id = R.string.reback),
                content = stringResource(id = R.string.edit_not_upload),
                onConfirm = {
                    val backResult = navController.popBackStack()
                    if (!backResult) finish()
                    hideDialog()
                },
                onDismiss = {
                    hideDialog()
                })
        }
    }

    /**
     * Fly back popup
     *
     * @param toStart false 返回上一个路由 true 返回首页
     */
    private fun flyBackPopup(toStart: Boolean = false) {
        if (mapVideoModel.airFlag != VKAg.AIR_FLAG_ON_GROUND) {
            showDialog {
                OnFlyBackPopup(onConfirm = {
                    if (!toStart) {
                        val backResult = navController.popBackStack()
                        if (!backResult) finish()
                    } else {
                        navController.popBackStack(mapVideoModel.workModeEnum.url, false)
                    }
                    hideDialog()
                }, onDismiss = { hideDialog() })
            }
        } else {
            if (!toStart) {
                val backResult = navController.popBackStack()
                if (!backResult) finish()
            } else {
                navController.popBackStack(mapVideoModel.workModeEnum.url, false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (deviceConfig.gimbalCount == 2) {
            video2.visibility = View.VISIBLE
            video2.setOnClickListener(this)
            video.setLabel("video1")
            video2.setLabel("video2")
        } else {
            video2.visibility = View.GONE
        }
    }

    @Composable
    fun formatHeight(height: Float, alt: Float): String {
        return "${
            stringResource(
                R.string.distance3,
                UnitHelper.convertLength(height).toString(1)
            )
        }/${stringResource(R.string.distance3, UnitHelper.convertLength(alt).toString(0))}"
    }

    @Composable
    fun RadarPanel() {
        val context = LocalContext.current
        val radarGraph by DroneModel.radarGraphData.observeAsState()
        val workPage by workPage.observeAsState()
        val imuData by DroneModel.imuData.observeAsState()
        val batteryData by DroneModel.batteryData.observeAsState()
        val canGalvInfo by DroneModel.canGALVInfo.observeAsState()
        val aptypeData by DroneModel.aptypeData.observeAsState()
        val workMachinaType = aptypeData?.getIntValue(VKAg.APTYPE_DRONE_TYPE)
        //雷达点击
        var clickRadar by remember {
            mutableStateOf(false)
        }
        var level = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        radarGraph?.let {
            level = it.distances
        }
        var isSmartBattery = false
        imuData?.let {
            if (imuData?.energyType == VKAg.TYPE_SMART_BATTERY) {
                isSmartBattery = true
            }
        }

        //清洗雷达
        val cleanRadarData = @Composable {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //速度
                RadarTextData(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.radar_speed),
                    text = stringResource(
                        id = R.string.distance3,
                        UnitHelper.convertLength(imuData?.hvel ?: 0.0f).toString(1)
                    ),
                    unit = "${UnitHelper.lengthUnit()}/s"
                )
                //高度
                RadarTextData(
                    modifier = Modifier.weight(1f),
                    title = stringResource(id = R.string.radar_height) + "/" + stringResource(id = R.string.altitude),
                    text = formatHeight(
                        DroneModel.imuData.value?.height ?: 0f,
                        DroneModel.imuData.value?.alt ?: 0f
                    ),
                    unit = UnitHelper.lengthUnit()
                )
            }
        }
        val commonRadarData = @Composable {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(R.string.radar_speed),
                        text = stringResource(
                            R.string.distance3,
                            UnitHelper.convertLength(imuData?.hvel ?: 0f).toString(1)
                        ) + "/${String.format(Locale.US, "%.1f", imuData?.targetMu ?: 0.0)}",
                        unit = UnitHelper.speedUnit()
                    )
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(R.string.radar_area),
                        text = UnitHelper.transAreaMu(sortieAreaData.value ?: 0f, 1),
                        unit = UnitHelper.areaUnit(context)
                    )
                }
                Column(
                    modifier = Modifier.weight(1.5f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(id = R.string.radar_height) + "/" + stringResource(id = R.string.altitude),
                        text = formatHeight(
                            DroneModel.imuData.value?.height ?: 0f,
                            DroneModel.imuData.value?.alt ?: 0f
                        ),
                        unit = UnitHelper.lengthUnit()
                    )
                    val titleInt =
                        if (isSeedWorkType()) R.string.radar_valve_opening else R.string.radar_parameter_drug
                    val unit =
                        if (isSeedWorkType()) UnitHelper.weightUnit() else UnitHelper.capacityUnit()
                    val capacity = UnitHelper.transCapacity(imuData?.YiYongYaoLiang ?: 0f)
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(id = titleInt),
                        text = if (isSeedWorkType()) deviceSeedData.value?.valve?.toString()
                            ?: stringResource(R.string.na)
                        else stringResource(R.string.distance3, capacity.ifBlank { "0.0" }),
                        unit = unit
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(R.string.radar_weight),
                        text = stringResource(
                            R.string.distance3,
                            UnitHelper.convertWeight(deviceWeightData.value?.remain_weight ?: 0f)
                                .toString(1)
                        ),
                        unit = UnitHelper.weightUnit()
                    )
                    val titleInt =
                        if (isSeedWorkType()) R.string.radar_swinging_speed else R.string.radar_parameter_flow
                    val unit = if (isSeedWorkType()) "rpm" else "${UnitHelper.capacityUnit()}/min"
                    val capacity = UnitHelper.transCapacity(imuData?.flowRate ?: 0f)
                    RadarTextData(
                        modifier = Modifier,
                        title = stringResource(id = titleInt),
                        text = if (isSeedWorkType()) deviceSeedData.value?.speed?.toString()
                            ?: stringResource(R.string.na)
                        else stringResource(R.string.distance3, capacity.ifBlank { "0.0" }),
                        unit = unit
                    )
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            if (clickRadar) {
                RadarControlSwitchRow(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = RADAR_BOX_HEIGHT + 4.dp, start = RADAR_BOX_LEFT_PADDING)
                        .width(260.dp) //需要跟RadarData组件右侧radar宽度一致
                        .height(30.dp)
                        .background(color = DarkAlpha)
                )
            }
            RadarInstrument(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = RADAR_BOX_LEFT_PADDING, bottom = 2.dp)
                    .width(260.dp)
                    .height(RADAR_BOX_HEIGHT),
                backgroundColor = if (videoState == STATE_VIDEO) LightDarkAlpha else DarkAlpha,
                radarClickEnabled = true,
                onRadarClick = {
                    clickRadar = !clickRadar
                },
                sectorDistances = level,
                content = {
                    when (workPage) {
                        WorkPageEnum.WORK_AB_CLEAN.url,
                        WorkPageEnum.WORK_AB_CLEAN_START.url,
                        WorkPageEnum.WORK_AREA_CLEAN.url,
                        WorkPageEnum.WORK_AREA_CLEAN_EDIT.url,
                        WorkPageEnum.WORK_AREA_CLEAN_START.url,
                            -> { //清洗雷达
                            cleanRadarData()
                        }

                        WorkPageEnum.WORK_AB.url, WorkPageEnum.WORK_MANUAL.url -> {
                            if (workMachinaType == VKAgCmd.DRONE_TYPE_WASHING.toInt()) { //如果当前作业机是清洗机则AB点和手动作业显示清洗雷达
                                cleanRadarData()
                            } else {
                                commonRadarData()
                            }
                        }

                        else -> { //通用雷达
                            commonRadarData()
                        }
                    }
                })
        }
    }

    @Composable
    fun RadarControlSwitchRow(modifier: Modifier = Modifier) {
        val aptypeData by DroneModel.aptypeData.observeAsState()
        val s = aptypeData?.getIntValue(VKAg.APTYPE_SWITCHER)
        var gRadarOpen = false
        var fRadarOpen = false
        var hRadarOpen = false
        if (s != null) {
            gRadarOpen = s and 0x1 == 1//仿地开关
            fRadarOpen = (s and 0x2) != 0//前避障开关
            hRadarOpen = (s and 0x4) != 0//手动控制高度
        }

        //水平布局
        Box(
            modifier = modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.obstacle_avoidance_radar) + ":",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SwitchButton(width = 40.dp, height = 20.dp, defaultChecked = fRadarOpen) {
                        fRadarOpen = it
                        val s1 = if (gRadarOpen) 1 else 0//仿地开关
                        val s2 = if (fRadarOpen) 1 else 0//雷达开关
                        val s3 = if (hRadarOpen) 1 else 0//高精度监测
                        val v = s1 or (s2 shl 1) or (s3 shl 2)
                        DroneModel.activeDrone?.sendIndexedParameter(VKAg.APTYPE_SWITCHER, v)
                    }
                }
                VerticalDivider(
                    thickness = 1.dp, modifier = Modifier.fillMaxHeight(), color = WhiteAlpha
                )
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        AutoScrollingText(
                            text = stringResource(id = R.string.ground_simulation_radar) + ":",
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SwitchButton(width = 40.dp, height = 20.dp, defaultChecked = gRadarOpen) {
                        gRadarOpen = it
                        val s1 = if (gRadarOpen) 1 else 0//仿地开关
                        val s2 = if (fRadarOpen) 1 else 0//雷达开关
                        val s3 = if (hRadarOpen) 1 else 0//高精度监测
                        val v = s1 or (s2 shl 1) or (s3 shl 2)
                        DroneModel.activeDrone?.sendIndexedParameter(VKAg.APTYPE_SWITCHER, v)
                    }
                }

            }
        }
    }

    override fun setContentView() = setContentView(R.layout.activity_map_video)

    @Subscribe
    fun onWorkMachinaSwitch(workMachinaSwitch: WorkMachinaSwitch) {
        val droneType = workMachinaSwitch.workMachinaType
        val currentMenu = mapVideoModel.workModeEnum
        //处理当前页面不存在于对应作业机的情况
        //处理当前作业页面不在当前作业机中页面跳转 手动作业
        val checkMenuIdExists = { workMachinaMenuEnum: WorkMachinaMenuEnum ->
            if (!workMachinaMenuEnum.menuEnums.contains(currentMenu)) {
                navController.popToRoot()
                mapVideoModel.workModeEnum = WorkModeEnum.MANUAL
                mapVideoModel.hideInfoPanel()
            }
        }

        //获取当前作业机对应的页面enum
        val workMachinaMenuEnum = getWorkMenuByDroneType(droneType)
        checkMenuIdExists(workMachinaMenuEnum)
    }

    @Subscribe
    fun onSortie(s: Sortie) {
        val flightInformation = FlightInformation(
            currentWorkDuration = formatSecond(s.flightime),
            currentWorkArea = UnitHelper.transArea(s.area),
            currentDosage = if (isSeedWorkType()) UnitHelper.transWeight(s.drug) else UnitHelper.transCapacity(
                s.drug
            ),
            completedWorkedArea = UnitHelper.transArea(s.workArea.toFloat()),
            completedDosage = if (isSeedWorkType()) UnitHelper.transWeight(s.workDrug.toFloat()) else UnitHelper.transCapacity(
                s.workDrug.toFloat()
            ),
            needWorkArea = UnitHelper.transArea((s.unWorkArea).toFloat()),
            needDosage = (s.naviArea * (s.sprayPerMu / 1000f) - s.workDrug).toString(1),
            landArea = UnitHelper.transArea(s.blockArea.toFloat()),
            airRouteArea = UnitHelper.transArea(s.naviArea.toFloat()),
        )
        when (mapVideoModel.workModeEnum) {
            //todo
            //吊运作业信息
            //清洗模式
            WorkModeEnum.AREA_CLEAN, WorkModeEnum.AB_CLEAN, WorkModeEnum.CLEAN_HORIZONTAL_AB, WorkModeEnum.AB -> {
                showDialog {
                    CleanFlightInformationPopup(flightInformation) {
                        hideDialog()
                    }
                }
            }

            WorkModeEnum.FREE_AIR_ROUTE, WorkModeEnum.TREE_AIR_ROUTE -> {
                showDialog {
                    FreeAirRouteFlightInformationPopup(flightInformation) {
                        hideDialog()
                    }
                }
            }

            else -> {
                showDialog {
                    FlightInformationPopup(flightInformation) {
                        hideDialog()
                    }
                    //TODO 降落后，航线重排；只有航线作业的时候才重排，AB和手动作业降落的时候不用重排
                    //仅编辑参数和开始作业能获取到routeModel，所以仅当前页面处于这两个页面的时候才能进行航线重排
                    //这两个页面所用的routeModel路由都是WorkPageEnum.WORK_BLOCK_PARAM.url
                    val routeModelPage =
                        listOf(
                            WorkPageEnum.WORK_BLOCK_PARAM.url,
                            WorkPageEnum.WORK_BLOCK_START.url
                        )
                    if (routeModelPage.contains(workPage.value)) {
                        val routeModel = navController.getViewModel(
                            WorkPageEnum.WORK_BLOCK_PARAM.url, RouteModel::class.java
                        )
                        val blockParamModel = navController.getViewModel(
                            WorkPageEnum.WORK_BLOCK_PARAM.url, BlockParamModel::class.java
                        )
                        if (routeModel.target != -1 && routeModel.bk != null && config.smartPlan) {
                            DroneModel.imuData.value?.let {
                                routeModel.home = GeoHelper.LatLng(it.lat, it.lng)
                            }
                            mapVideoModel.needUploadNavi = true//落地之后 航线重排 需要重新上传航线
                            routeModel.findNaviLine(
                                routeModel.target,
                                routeModel.bk,
                                blockParamModel.toRouteParameter()
                            )
                            //将断点去掉，否则起飞后，还会往断点飞
                            DroneModel.breakPoint.postValue(null)
                            DroneModel.bk = null
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.video -> switchMode(STATE_VIDEO)
            R.id.mapbox -> switchMode(STATE_MAP)
            R.id.video2 -> switchMode(STATE_VIDEO2)
        }
    }

    override fun onResume() {
        super.onResume()
        canvas.onResume()
        Repo.updateBlockState = false
    }

    override fun onPause() {
        super.onPause()
        canvas.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        canvas.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        canvas.onSaveInstanceState(outState)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let { baseContext ->
            val overrideConfiguration = Configuration(baseContext.resources.configuration)
            overrideConfiguration.fontScale = 1.0f // 保持字体不缩放
            val context = baseContext.createConfigurationContext(overrideConfiguration)
            super.attachBaseContext(context)
        } ?: super.attachBaseContext(newBase)
    }

    @Composable
    fun EnergyControl() {
        val engineData by DroneModel.engineData.observeAsState()
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 120.dp, start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (engineData?.unlock_status?.toInt()) {
                    0 -> {
                        RightButtonCommon(text = stringResource(id = R.string.engine_start)) {
                            showDialog {
                                PromptPopup(
                                    content = stringResource(id = R.string.engine_start_confirm),
                                    onConfirm = {
                                        hideDialog()
                                    },
                                    onDismiss = {
                                        hideDialog()
                                    })
                            }
                        }
                    }

                    1 -> {
                        RightButtonCommon(text = stringResource(id = R.string.engine_stop)) {
                            showDialog {
                                PromptPopup(
                                    content = stringResource(id = R.string.engine_stop_confirm),
                                    onConfirm = {
                                        hideDialog()
                                    },
                                    onDismiss = {
                                        hideDialog()
                                    })
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun WarningsPage() {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(vertical = 50.dp, horizontal = 40.dp)
                    .fillMaxWidth(0.3f)
                    .align(Alignment.TopStart), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mapVideoModel.warnings) { warn ->

                    val color = when (warn.warnType) {
                        NewWarnTool.WARN_TYPE_ERROR -> MaterialTheme.colorScheme.error
                        NewWarnTool.WARN_TYPE_WARN -> MaterialTheme.colorScheme.tertiary
                        else -> androidx.compose.ui.graphics.Color.White
                    }
                    val contentColor = when (warn.warnType) {
                        NewWarnTool.WARN_TYPE_ERROR -> MaterialTheme.colorScheme.onError
                        NewWarnTool.WARN_TYPE_WARN -> MaterialTheme.colorScheme.onTertiary
                        else -> androidx.compose.ui.graphics.Color.Black
                    }
                    Surface(
                        modifier = Modifier,
                        color = color,
                        shape = MaterialTheme.shapes.small,
                        contentColor = contentColor
                    ) {
                        Text(
                            text = warn.warnString,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun MapTools() {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 140.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    color = androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = {
                        mapVideoModel.mapFollowMode = if (mapVideoModel.mapFollowMode == 1) 0 else 1
                        if (phonePosition != null) {
                            canvas.moveMap(phonePosition!!.latitude, phonePosition!!.longitude, 18f)
                        }
                    }) {
                    Image(
                        painter = painterResource(id = R.drawable.default_location_remote),
                        contentDescription = "remote",
                        contentScale = ContentScale.FillHeight,
                        colorFilter = ColorFilter.tint(if (mapVideoModel.mapFollowMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary)
                    )
                }
                Surface(
                    modifier = Modifier.size(30.dp),
                    color = androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = {
                        mapVideoModel.mapFollowMode = if (mapVideoModel.mapFollowMode == 2) 0 else 2
                        dronePosition?.let { canvas.moveMap(it.latitude, it.longitude, 18f) }
                    }) {
                    Image(
                        painter = painterResource(id = R.drawable.default_location_drone),
                        contentDescription = "remote",
                        contentScale = ContentScale.FillHeight,
                        colorFilter = ColorFilter.tint(if (mapVideoModel.mapFollowMode == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary)
                    )
                }
                Surface(
                    modifier = Modifier.size(30.dp),
                    color = androidx.compose.ui.graphics.Color.Unspecified,
                    onClick = { clearWorkLine() }) {
                    Image(
                        painter = painterResource(id = R.drawable.line_clear),
                        contentDescription = "remote",
                        contentScale = ContentScale.FillHeight
                    )
                }
            }
        }
    }

    @Composable
    fun WorkPage() {
        NavHost(
            navController = navController,
            startDestination = WorkPageEnum.WORK_MANUAL.url,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                fadeIn(tween(0))
            },
            exitTransition = {
                fadeOut(tween(0))
            },
            popExitTransition = {
                fadeOut(tween(0))
            },
            popEnterTransition = {
                fadeIn(tween(0))
            }) {
            composable(WorkPageEnum.WORK_MANUAL.url) {
                WorkManual(WorkPageEnum.WORK_MANUAL.url)
            }
            composable(WorkPageEnum.WORK_AB.url) {
                WorkAB(WorkPageEnum.WORK_AB.url)
            }
            composable(WorkPageEnum.WORK_AB_START.url) {
                WorkABStart(WorkPageEnum.WORK_AB.url)
            }
            composable(WorkPageEnum.WORK_AB_CLEAN.url) {
                WorkABClean(WorkPageEnum.WORK_AB_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_AB_CLEAN_START.url) {
                WorkABCleanStart(WorkPageEnum.WORK_AB_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_AREA_CLEAN.url) {
                WorkAreaClean(WorkPageEnum.WORK_AREA_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_AREA_CLEAN_EDIT.url) {
                WorkAreaCleanEdit(WorkPageEnum.WORK_AREA_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_AREA_CLEAN_PARAM.url) {
                WorkAreaCleanParam(WorkPageEnum.WORK_AREA_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_AREA_CLEAN_START.url) {
                WorkAreaCleanStart(WorkPageEnum.WORK_AREA_CLEAN.url)
            }
            composable(WorkPageEnum.WORK_BLOCK.url) {
                WorkBlock(WorkPageEnum.WORK_BLOCK.url)
            }
            composable(WorkPageEnum.WORK_BLOCK_EDIT.url) {
                WorkBlockEdit(
                    blockRoute = WorkPageEnum.WORK_BLOCK.url,
                    blockEditRoute = WorkPageEnum.WORK_BLOCK_EDIT.url
                )
            }
            composable(WorkPageEnum.WORK_BLOCK_DIVISION.url) {
                WorkBlockDivision(
                    blockRoute = WorkPageEnum.WORK_BLOCK.url,
                    blockDivisionRoute = WorkPageEnum.WORK_BLOCK_DIVISION.url
                )
            }
            composable(WorkPageEnum.WORK_BLOCK_PARAM.url) {
                WorkBlockParam(
                    blockRoute = WorkPageEnum.WORK_BLOCK.url,
                    blockParamRoute = WorkPageEnum.WORK_BLOCK_PARAM.url
                )
            }
            composable(WorkPageEnum.WORK_BLOCK_START.url) {
                WorkBlockStart(
                    blockRoute = WorkPageEnum.WORK_BLOCK.url,
                    blockParamRoute = WorkPageEnum.WORK_BLOCK_PARAM.url,
                )
            }

            composable(WorkPageEnum.WORK_FREE_AIR_ROUTE.url) {
                WorkFreeAirRoute(WorkPageEnum.WORK_FREE_AIR_ROUTE.url)
            }
            composable(WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url) {
                WorkFreeAirRouteEdit(
                    freeAirRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE.url,
                    freeAirEditRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url
                )
            }
            composable(WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url) {
                WorkFreeAirRouteParam(
                    freeAirRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE.url,
                    freeAirParamRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url
                )
            }
            composable(WorkPageEnum.WORK_FREE_AIR_ROUTE_START.url) {
                WorkFreeAirRouteStart(
                    freeAirRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE.url,
                    freeAirParamRoute = WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
                )
            }
            composable(WorkPageEnum.WORK_ENHANCED_MANUAL.url) {
                WorkEnhancedManual(WorkPageEnum.WORK_ENHANCED_MANUAL.url)
            }
            composable(WorkPageEnum.WORK_LOCATOR.url) {
                WorkLocator(WorkPageEnum.WORK_LOCATOR.url)
            }
        }
        // 路由由destination中的route属性确定
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val route = navBackStackEntry?.destination?.route ?: return
        when (route) {
            WorkPageEnum.WORK_AB.url,
            WorkPageEnum.WORK_AB_START.url,
            WorkPageEnum.WORK_MANUAL.url,
            WorkPageEnum.WORK_AB_CLEAN.url,
            WorkPageEnum.WORK_AB_CLEAN_START.url,
            WorkPageEnum.WORK_AREA_CLEAN.url,
            WorkPageEnum.WORK_AREA_CLEAN_EDIT.url,
            WorkPageEnum.WORK_AREA_CLEAN_START.url,
            WorkPageEnum.WORK_BLOCK.url,
            WorkPageEnum.WORK_BLOCK_START.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_START.url,
            WorkPageEnum.WORK_ENHANCED_MANUAL.url,
                -> {
                mapState.postValue(STATE_MAP)
            }

            WorkPageEnum.WORK_BLOCK_EDIT.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url,
                -> {
                //仅飞机打点需要显示视频
                if (mapVideoModel.locationType == "drone") {
                    mapState.postValue(STATE_MAP)
                } else {
                    mapState.postValue(STATE_NO_VIDEO)
                }
            }

            WorkPageEnum.WORK_BLOCK_DIVISION.url,
            WorkPageEnum.WORK_BLOCK_PARAM.url,
            WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
            WorkPageEnum.WORK_AREA_CLEAN_PARAM.url,
            WorkPageEnum.WORK_LOCATOR.url,
                -> {
                mapState.postValue(STATE_NO_VIDEO)
            }
        }
        if (workPage.value != route) {
            workPage.postValue(route)
        }
    }

    private var clickListener: IMapCanvas.MapClickListener? = null
    private var mapChangeListener: IMapCanvas.MapChangeListener? = null
    private var markListener: IMapCanvas.MapMarkerSelectListener? = null
    private var markDragListener: IMapCanvas.MarkerDragListener? = null
    private fun clearMapListener() {
        clickListener?.let { canvas.removeClickListener(it) }
        markListener?.let { canvas.removeMarkClickListener(it) }
        markDragListener?.let { canvas.removeMarkerDragListener(it) }
        mapChangeListener?.let { canvas.removeChangeListener(it) }
    }

    fun addMapClickListener(l: IMapCanvas.MapClickListener) {
        clickListener = l
        canvas.addClickListener(l)
    }

    fun addMapChangeListener(l: IMapCanvas.MapChangeListener) {
        mapChangeListener = l;
        canvas.addChangeListener(l)
    }

    fun addMarkClickListener(l: IMapCanvas.MapMarkerSelectListener) {
        markListener = l
        canvas.addMarkClickListener(l)
    }

    fun addMarkDragListener(l: IMapCanvas.MarkerDragListener) {
        markDragListener = l
        canvas.addMarkerDragListener(l)
    }

    private val mapState = MutableLiveData(STATE_MAP)
    private val workPage = MutableLiveData(WorkPageEnum.WORK_MANUAL.url)

    private var dialogState = 0 // 0 - none, 1 - progress, 2 - notice
    private val progressState = mutableStateOf("")
    private fun makeProgressPopup(
        showCancel: Boolean = false, onDismiss: () -> Unit,
    ): @Composable () -> Unit {
        return {
            val progress = parsePercent(progressState.value)
            ProgressPopup(
                text = progressState.value,
                popupWidth = 320.dp,
                progress = progress,
                showCancel = showCancel,
                onDismiss = { onDismiss() })
        }
    }

    private fun addObserver() {
        var showCancel = false
        progressModel.progress.observe(this) {
            when (it) {
                is ProgressModel.ProgressMessage -> {
                    val showCancelNew = it.cancellable
                    if (dialogState != 1 && showCancel != showCancelNew) {
                        dialogState = 1
                        showDialog(makeProgressPopup(showCancel = showCancelNew, onDismiss = {
                            progressModel.next(-1)
                            hideDialog()
                        }))
                        showCancel = showCancelNew
                    }
                    progressState.value = it.text
                }

                is ProgressModel.ProgressNotice -> {
                    if (dialogState != 2) {
                        dialogState = 2
                    }
                }

                is ProgressModel.ProgressResult -> {
                    showCancel = false
                    dialogState = 0
                    progressModel.done()
                    val processed = taskComplete?.invoke(it.success, it.msg) == true
                    if (!processed && it.msg != null) {
                        toast(it.msg!!)
                    }
                }
            }
        }
        mapState.observe(this) {
            switchMode(it)
        }
        workPage.observe(this) {
            stopDataListener()
            canvas.clear()
            droneCanvas.clearText()
            clearMapListener()
            cancelAllJob()
            DroneModel.activeDrone?.getParameters()
//            getNoFlyZone() todo
            //WorkModeEnum是1级菜单url，1级菜单才允许直接切换作业模式
            if (WorkModeEnum.getAllModelUrl().contains(it)) {
                mapVideoModel.workModeEnabled = true
            } else {
                mapVideoModel.workModeEnabled = false
            }
            //增强手动作业自动打开增强模式
            if (it == WorkModeEnum.ENHANCED_MANUAL.url) {
                droneCanvas.setOpenEnhanced(true)
            } else {
                droneCanvas.setOpenEnhanced(false)
            }
            when (it) {
                WorkPageEnum.WORK_MANUAL.url -> {}

                WorkPageEnum.WORK_AB.url,
                WorkPageEnum.WORK_AB_START.url,
                    -> { //垂直清洗与AB点共用一套
                    val abModel = navController.getViewModel(
                        WorkPageEnum.WORK_AB.url, ABModel::class.java
                    )
                    collectAB(abModel)
                }

                WorkPageEnum.WORK_AB_CLEAN.url, WorkPageEnum.WORK_AB_CLEAN_START.url -> {
                    val abCleanModel = navController.getViewModel(
                        WorkPageEnum.WORK_AB_CLEAN.url, ABCleanModel::class.java
                    )
                    collectABClean(abCleanModel)
                }

                WorkPageEnum.WORK_AREA_CLEAN.url,
                    -> {
                    val areaCleanModel = navController.getViewModel(
                        WorkPageEnum.WORK_AREA_CLEAN.url, AreaCleanModel::class.java
                    )
                    collectAreaClean(areaCleanModel)
                }

                WorkPageEnum.WORK_AREA_CLEAN_PARAM.url,
                WorkPageEnum.WORK_AREA_CLEAN_START.url,
                    -> {
                    val areaCleanModel = navController.getViewModel(
                        WorkPageEnum.WORK_AREA_CLEAN.url, AreaCleanModel::class.java
                    )
                    collectAreaCleanParam(areaCleanModel)
                }

                WorkPageEnum.WORK_AREA_CLEAN_EDIT.url -> {
                    val areaCleanModel = navController.getViewModel(
                        WorkPageEnum.WORK_AREA_CLEAN.url, AreaCleanModel::class.java
                    )
                    collectAreaCleanEdit(areaCleanModel)
                }

                WorkPageEnum.WORK_BLOCK.url,
                    -> {
                    val blockModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK.url, BlockModel::class.java
                    )
                    collectBlock(blockModel)
                }

                WorkPageEnum.WORK_BLOCK_EDIT.url -> {
                    val blockEditModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_EDIT.url, BlockEditModel::class.java
                    )
                    val locationModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_EDIT.url, LocationModel::class.java
                    )
                    collectBlockEdit(blockEditModel, locationModel)
                }

                WorkPageEnum.WORK_BLOCK_DIVISION.url -> {
                    val blockDivisionModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_DIVISION.url, BlockDivisionModel::class.java
                    )
                    val locationModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_DIVISION.url, LocationModel::class.java
                    )
                    collectBlockDivision(blockDivisionModel, locationModel)
                }

                WorkPageEnum.WORK_BLOCK_PARAM.url,
                    -> {
                    val blockParamModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, BlockParamModel::class.java
                    )
                    val routeModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, RouteModel::class.java
                    )
                    val locationModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, LocationModel::class.java
                    )
                    val taskModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, TaskModel::class.java
                    )
                    val outPathRouteModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, OutPathRouteModel::class.java
                    )
                    collectBlockParam(
                        blockParamModel, locationModel, routeModel, taskModel, outPathRouteModel
                    )
                }

                WorkPageEnum.WORK_BLOCK_START.url -> { //vm跟param使用同一个
                    val blockParamModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, BlockParamModel::class.java
                    )
                    val routeModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, RouteModel::class.java
                    )
                    val taskModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, TaskModel::class.java
                    )
                    val outPathRouteModel = navController.getViewModel(
                        WorkPageEnum.WORK_BLOCK_PARAM.url, OutPathRouteModel::class.java
                    )
                    collectBlockStart(blockParamModel, routeModel, taskModel, outPathRouteModel)
                }

                WorkPageEnum.WORK_FREE_AIR_ROUTE.url -> {
                    val freeAirRouteModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE.url, FreeAirRouteModel::class.java
                    )
                    collectFreeAirRoute(freeAirRouteModel)
                }

                WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url -> {
                    val freeAirRouteEditModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url, FreeAirRouteEditModel::class.java
                    )
                    val locationModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_EDIT.url, LocationModel::class.java
                    )
                    collectFreeAirRouteEdit(freeAirRouteEditModel, locationModel)

                }

                WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url -> {
                    val freeAirRouteParamModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
                        FreeAirRouteParamModel::class.java
                    )
                    val taskModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url, TaskModel::class.java
                    )
                    collectFreeAirRouteParam(freeAirRouteParamModel, taskModel)
                }

                WorkPageEnum.WORK_FREE_AIR_ROUTE_START.url -> {//vm跟param使用同一个
                    val freeAirRouteParamModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url,
                        FreeAirRouteParamModel::class.java
                    )
                    val taskModel = navController.getViewModel(
                        WorkPageEnum.WORK_FREE_AIR_ROUTE_PARAM.url, TaskModel::class.java
                    )
                    collectFreeAirRouteStart(freeAirRouteParamModel, taskModel)
                }

                WorkPageEnum.WORK_ENHANCED_MANUAL.url -> {
                    val enhanceMode = navController.getViewModel(
                        WorkPageEnum.WORK_ENHANCED_MANUAL.url,
                        EnhancedManualModel::class.java
                    )
                    collectEnhancedManual(enhanceMode)
                }

                WorkPageEnum.WORK_LOCATOR.url -> {
                    val locationModel = navController.getViewModel(
                        WorkPageEnum.WORK_LOCATOR.url, LocationModel::class.java
                    )
                    val locatorModel = navController.getViewModel(
                        WorkPageEnum.WORK_LOCATOR.url,
                        LocatorModel::class.java
                    )
                    collectLocator(locatorModel = locatorModel, locationModel = locationModel)
                }
            }
        }
        DroneModel.imuData.observe(this) {
            mapVideoModel.checkAirFlag(it)
            val iRoll = it.roll.roundToInt()
            video.setRoll(iRoll)
            video2.setRoll(iRoll)
            if (ControllerFactory.deviceModel == "H12" || ControllerFactory.deviceModel == "PHONE") {
                checkFlightDir(it)
            }
            mapVideoModel.engineType = it.energyType
            if (it.GPSStatus > 1) {
                val droneLocation = GeoHelper.LatLng(it.lat, it.lng)
                cacTargetPoint(droneLocation, it.yaw)
                if (it.airFlag == VKAg.AIR_FLAG_ON_AIR) { // drone follow
                    moveMapWithDrone(it.lat, it.lng)
                    trackModel.addPoint(it.lat, it.lng, it.pump == 1.toByte())
                } else {
                    followTime = 0
                }
                droneCanvas.setDronePosition(GeoHelper.LatLng(it.lat, it.lng))
                if (!firstReceiveDronePosition) {
                    firstReceiveDronePosition = true
                    dronePosition = droneLocation
                    canvas.moveMap(it.lat, it.lng, 15f)
                }
                droneCanvas.setProperties(it.yaw, it.GPSStatus)
                if (VKAgTool.isGoHomeMode(it.flyMode.toInt())) {
                    droneCanvas.setDashLine(DroneModel.homeData.value)
                } else {
                    droneCanvas.setDashLine()
                }
            } else {
                droneCanvas.clear()
            }
        }
        canvas.phoneLocation.observe(this) {
            if (phonePosition == null) {
                phonePosition = it
            }
            mapVideoModel.phonePosition = GeoHelper.LatLngAlt(
                it.latitude, it.longitude, 0.0
            )
        }
        DroneModel.homeData.observe(this) {
            canvas.drawLetterMarker("home", it.latitude, it.longitude, "H", Color.rgb(0, 0x60, 0))
        }
        DroneModel.aptypeData.observe(this) {
            AptypeUtil.setAPTypeData(it)
        }
        DroneModel.newWarnListData.observe(this) { updateWarn(it) }
        collectTrack()

        RtcmModel.rtcmInfo.observe(this) {
            mapVideoModel.rtkInfo = RTKHelper.formatInfo(this, it).second
        }
        DroneModel.warnData.value = null
        DroneModel.warnData.observe(this) {
            it?.let {
                when (it.warn_type.toInt()) {
//                    1 -> showWarn(getString(R.string.warn_not_locate))
                    2 -> forceUnlock(getString(R.string.warn_gps_locate), it.warn_type.toInt())
                    3 -> {
                        if (it.warn_content.toInt() == 0) {
                            forceUnlock(getString(R.string.warn_rtk_not_lock), it.warn_type.toInt())
                        } else {
                            // TODO: 国际化
                            showDialog {
                                PromptPopup(
                                    content = "RTK未固定解，不允许解锁",
                                    showCancel = false,
                                    showConfirm = true,
                                    onDismiss = {
                                        hideDialog()
                                    },
                                    onConfirm = {
                                        hideDialog()
                                    }
                                )
                            }
                        }

                    }

                    4 -> forceUnlock(getString(R.string.warn_dual_not_lock), it.warn_type.toInt())
//                    5 -> showWarn(getString(R.string.warn_rtk_lost))
                }
            }
        }
    }

    private var revCount = 0
    private var currentDir = 0 // forward
    private fun checkFlightDir(imu: VKAg.IMUData) {
        if (videoState == STATE_NO_VIDEO) return
//        if (!autoSwitch || imu.hvel < 1f) {
//            revCount = 0
//            return
//        }
        val dir = calcDir(imu)
        if (dir != currentDir) {
            revCount++
            if (revCount > 3) {
                currentDir = dir
                revCount = 0
            }
        } else {
            revCount = 0
        }
        video.specialAction(if (currentDir == 0) "fpv" else "camera2")
    }

    private fun calcDir(imu: VKAg.IMUData): Int {
        val d1 = Math.toRadians(90 - imu.yaw.toDouble())
        val v1 = Point2D(cos(d1), sin(d1))
        val v2 = Point2D(imu.xvel.toDouble(), imu.yvel.toDouble())
        return if (v1.dotProduct(v2) >= 0) 0 else 1
    }

    private fun forceUnlock(text: String, type: Int) {
        showDialog {
            PromptPopup(content = text, onConfirm = {
                DroneModel.activeDrone?.forceUnlock(type)
                hideDialog()
            }, onDismiss = {
                hideDialog()
            })
        }
    }

    private var followTime = 0L
    private fun MapVideoActivity.moveMapWithDrone(lat: Double, lng: Double) {
        if (mapVideoModel.mapFollowMode == 2) {
            val t = System.currentTimeMillis()
            if (t - followTime > 3000) {
                followTime = t
                canvas.moveMap(lat, lng)
            }
        }
    }

    val converter = GeoHelper.GeoCoordConverter()
    private fun cacTargetPoint(pt: GeoHelper.LatLng, yaw: Float) {
        val list = mutableListOf<GeoHelper.LatLng>()
        val point = converter.convertLatLng(pt.latitude, pt.longitude)
        val rad = Math.toRadians(90.0 - yaw)
        val sina = sin(rad)
        val cosa = cos(rad)
        val x = cosa * 1000 + point.x
        val y = sina * 1000 + point.y
        val p = converter.convertPoint(x, y)
        list.add(pt)
        list.add(p)
        canvas.drawLine("extension_line", list, Color.YELLOW)
    }

    private fun updateWarn(warns: List<NewWarnTool.WarnStringData>) {
        val sortedList = warns.sortedBy { it.warnString }
        mapVideoModel.warnings.clear()
        mapVideoModel.warnings.addAll(sortedList)
    }
}

//地图打点
class MapChangedListener(val canvas: IMapCanvas, val locationModel: LocationModel) :
    IMapCanvas.MapChangeListener {
    override fun onCameraChange(bearing: Float) {
        locationModel.mapCenter.postValue(canvas.centerPoint)
    }
}
