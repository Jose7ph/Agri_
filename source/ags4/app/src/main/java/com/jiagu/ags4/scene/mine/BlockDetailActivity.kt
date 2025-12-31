package com.jiagu.ags4.scene.mine

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jiagu.ags4.MapActivity
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.repo.net.AgsNet
import com.jiagu.ags4.repo.net.AgsNet.networkFlow
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.scene.work.LocationTypeEnum
import com.jiagu.ags4.utils.LocalNavController
import com.jiagu.ags4.vm.BlockDivisionModel
import com.jiagu.ags4.vm.BlockEditModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.DroneModel.blockId
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockDetailActivity : MapActivity() {
    private lateinit var navController: NavHostController

    val locationModel: LocationModel by viewModels()
    val btDeviceModel: BtDeviceModel by viewModels()
    val blockEditModel: BlockEditModel by viewModels()
    val blockDivisionModel: BlockDivisionModel by viewModels()

    var curBlockPlan = MutableStateFlow<BlockPlan?>(null)

    private val config by lazy { Config(this) }

    var locationType by mutableStateOf(LocationTypeEnum.MAP.type)
    fun changeLocationType(type: String) {
        locationType = type
        config.locationType = type
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockId = intent.getLongExtra("blockId", 0L)
//        locationType = config.locationType
        addObserver()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun initBlockPlan(){
        lifecycleScope.launch {
            AgsNet.getBlockDetail(blockId).networkFlow {
                Log.v("lee", "error: $it")
            }.collectLatest {
                Log.v("lee", "getLandBlockDetail: $it")
                val bp = BlockPlan(
                    blockId = it.blockId,
                    blockType = it.blockType,
                    blockName = it.blockName,
                    boundary = it.boundary,
                    calibPoints = it.calibPoints,
                    area = it.area,
                    createTime = it.createTime,
                    workPercent = null,
                    planId = 0,
                    workId = 0,
                    additional = null,
                ).apply {
                    //localblockid默认0触发直接上传方法
                    region = it.region
                    regionName = it.regionName
                }
                curBlockPlan.emit(bp)
                blockEditModel.initEditBlockPlan(bp)
                blockDivisionModel.initEditBlockPlan(bp)
            }
        }
    }

    private fun addObserver() {
        workState.observe(this) {
            canvas.clear()
            clearMapListener()
            cancelAllJob()
            DroneModel.activeDrone?.getParameters()
            when (it) {
                BlockDetailPageUrlEnum.BLOCK_DETAIL.url -> {
                    initBlockPlan()
                    collectBlockDetail()
                }

                BlockDetailPageUrlEnum.BLOCK_EDIT.url -> {
                    collectBlockEdit()
                }

                BlockDetailPageUrlEnum.BLOCK_DIVISION.url -> {
                    collectBlockDivision()
                }
            }
        }
    }

    private fun collectBlockDetail() {
        collectFlow(curBlockPlan) { cbp ->
            cbp?.let {
                when (it.blockType) {
                    Block.TYPE_BLOCK, Block.TYPE_GROUND_BLOCK -> {
                        canvas.drawBlock(it.blockId.toString(), it.boundary[0], false)
                        canvas.highlightBlock(it.blockId.toString(), true)
                    }

                    Block.TYPE_TRACK, Block.TYPE_GROUND_TRACK, Block.TYPE_LIFTING, Block.TYPE_AREA_CLEAN -> {
                        canvas.drawLine(
                            it.blockId.toString(), it.boundary[0],
                            color = IMapCanvas.Params.BLOCK_FILL_COLOR,
                            z = IMapCanvas.Z_HL_LINE,
                            width = IMapCanvas.Params.COMPLETION_WIDTH
                        )
                    }
                }
                canvas.fit(listOf(it.blockId.toString()))
            }
        }
    }

    private val workState = MutableLiveData("")

    @Composable
    override fun ContentPage() {
        navController = rememberNavController()
        CompositionLocalProvider(
            LocalNavController provides navController,
        ) {
            NavHost(
                navController = navController,
                startDestination = BlockDetailPageUrlEnum.BLOCK_DETAIL.url,
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
                composable(BlockDetailPageUrlEnum.BLOCK_DETAIL.url) { LandBlockDetails { finish() } }
                composable(BlockDetailPageUrlEnum.BLOCK_EDIT.url) {
                    BlockDetailEdit()
                }//编辑地块
                composable(BlockDetailPageUrlEnum.BLOCK_DIVISION.url) {
                    BlockDetailDivision()
                }
            }
        }
        // 路由由destination中的route属性确定
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val route = navBackStackEntry?.destination?.route ?: return
        if (workState.value != route) {
            workState.postValue(route)
        }
    }
}

enum class BlockDetailPageUrlEnum(val url: String) {
    BLOCK_DETAIL("block_detail"),
    BLOCK_EDIT("block_edit"),
    BLOCK_DIVISION("block_division"),
}