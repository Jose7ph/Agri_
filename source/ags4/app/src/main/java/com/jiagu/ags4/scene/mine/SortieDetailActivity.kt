package com.jiagu.ags4.scene.mine

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.jiagu.ags4.MapActivity
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.DroneInfo
import com.jiagu.ags4.bean.Locus
import com.jiagu.ags4.bean.SprayLocus
import com.jiagu.ags4.ui.theme.BlackAlpha
import com.jiagu.ags4.ui.theme.SIMPLE_BAR_HEIGHT
import com.jiagu.ags4.utils.formatSecond
import com.jiagu.api.ext.millisToDateTime
import com.jiagu.api.ext.toString
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.jgcompose.ext.noEffectClickable
import com.jiagu.jgcompose.text.AutoScrollingText
import com.jiagu.jgcompose.video.VideoPanel
import com.jiagu.tools.ext.UnitHelper
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.LinkedList

class SortieDetailActivity : MapActivity() {
    companion object {
        const val EXTRA_DRONE_ID = "extra_drone_id"
        const val EXTRA_SORTIE_ID = "extra_sortie_id"
        const val EXTRA_SORTIE_TIME = "extra_sortie_start"
    }

    var sortieId: Long = 0L
    var droneId: String = ""
    var startTime: Long = 0L
    private val mineModel: MineModel by viewModels()
    private var trackInfo: List<DroneInfo>? = null
    private var originalLocus = mutableListOf<Locus>()
    private var originalSprayLocus = mutableListOf<SprayLocus>()
    private val playLocus = LinkedList<Locus>()
    private val playedLocus = LinkedList<Locus>()
    var playProgress by mutableFloatStateOf(0f)
    var playSpeed by mutableIntStateOf(1)
    var playState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sortieId = intent.getLongExtra(EXTRA_SORTIE_ID, 0L)
        droneId = intent.getStringExtra(EXTRA_DRONE_ID) ?: ""
        startTime = intent.getLongExtra(EXTRA_SORTIE_TIME, 0L)

        collectSortieDetail()

        mineModel.block.observe(this) {
            Log.d("lee", "block: $it")
            if (mineModel.blockType == Block.TYPE_BLOCK) {
                canvas.drawBlockWithHoles("block", it)
                droneCanvas.drawDistance(it[0], true)
            } else {
                canvas.drawTrack("block", it[0])
                droneCanvas.drawDistance(it[0], false)
            }
            canvas.fit()
        }
        mineModel.track.observe(this) {
            val info = it?.droneInfos

            var flowSpeedValue = 0.0f
            var heightValue = 0.0f
            var speedValue = 0.0f
            if (info != null && info.isNotEmpty()) {
                trackInfo = info.sortedBy { it.recordTime }
                trackInfo!!.forEach {
                    val locus = Locus(it.lat, it.lng, it.mha, it.recordTime)
                    originalLocus.add(locus)
                    val pump = it.flowSpeed > 0
                    if (originalSprayLocus.isEmpty()) {
                        originalSprayLocus.add(SprayLocus(pump, mutableListOf(locus)))
                    } else {
                        val o0 = originalSprayLocus.last()
                        if (o0.pump == pump) {
                            originalSprayLocus.last().locus.add(locus)
                        } else {
                            originalSprayLocus.last().locus.add(locus)
                            originalSprayLocus.add(SprayLocus(pump, mutableListOf(locus)))
                        }
                    }
                    flowSpeedValue += it.flowSpeed
                    speedValue += it.xspeed
                    heightValue += it.height
                }
                flowSpeedValue /= trackInfo!!.size
                heightValue /= trackInfo!!.size
                speedValue /= trackInfo!!.size
            }
            mineModel.flowSpeedValue.value = flowSpeedValue
            mineModel.heightValue.value = heightValue
            mineModel.speedValue.value = speedValue

            resetLocus()
//            if (it.posData != null && it.posData.isNotEmpty()) {
//                binding.flight.totlalPesticideTitle.text = getString(R.string.mine_work_throwing)
//                binding.flight.totlalPesticide.text = it.posData.size.toString()
//                drawPos(it.posData)
//            }
            if (it != null) {
                if (it.posData != null && it.posData.isNotEmpty()) {
                    drawPos(it.posData)
                }
            }
            canvas.fit()
        }
    }

    private fun drawPos(list: List<String>) {
        val pos = mutableListOf<VKAg.POSData>()
        for (l in list) {
            val ll = l.split(":")
            if (ll.size < 2) return
            pos.add(VKAg.POSData.fromString(ll[1]))
        }
        if (pos.isNotEmpty()) drawPosData(pos)
    }

    private var centerNum = 0
    private fun drawPosData(centers: List<VKAg.POSData>) {
        for ((i, c) in centers.withIndex()) {
            val (icon, z) = if (c.rtk == 0xF) R.drawable.locater_g to IMapCanvas.Z_MARKER - 1
            else R.drawable.locater_r to IMapCanvas.Z_MARKER - 2
            canvas.drawMarker("_center2_$i", c.lat, c.lng, icon, z)
        }
        for (i in centers.size until centerNum) {
            canvas.remove("_center2_$i")
        }
        centerNum = centers.size
    }

    private fun drawDrone(lat: Double, lng: Double, angle: Float, gps: Short) {
        droneCanvas.setDronePosition(GeoHelper.LatLng(lat, lng))
        droneCanvas.setProperties(angle, gps)
    }

    // 轨迹白色背景
    private fun drawFlyLocusBackLine() {
        if (originalSprayLocus.isEmpty()) return
        val sprayColor = Color.WHITE
        val sprayColor2 = Color.YELLOW
        for ((i, o) in originalSprayLocus.withIndex()) {
            canvas.drawLine("fly${i}", o.locus, if (o.pump) sprayColor2 else sprayColor, width = 8f)
        }
    }

    private fun resetLocus() {
        playLocus.clear()
        originalLocus.apply {
            if (size > 0) {
                drawDrone(this[0].latitude, this[0].longitude, this[0].angle, 3)
                drawFlyLocusBackLine()
                playLocus.addAll(this)
                playedLocus.clear()
            }
        }
    }

    //处理播放进度的情况
    private fun changeLocus(rate: Float) {
        //清除已播放 和 未播放
        playedLocus.clear()
        playLocus.clear()
        //重置进度条到当前位置
        playProgress = rate
        //计算当前进度条位置对应的索引大概位置
        val index = (originalLocus.size.toFloat() * (1 - rate)).toInt()
        //计算已播放的数量
        val playedCount = originalLocus.size - index
        //添加已播放
        playedLocus.addAll(originalLocus.take(playedCount))
        //添加未播放
        playLocus.addAll(originalLocus.subList(playedCount, originalLocus.size))
        //绘制飞机当前位置
        if(playLocus.size>0){
            drawDrone(
                playLocus[0].latitude,
                playLocus[0].longitude,
                playLocus[0].angle,
                3
            )
        }
    }

    private fun playFlyLocus() {
        lifecycleScope.launch {
            while (playState) {
                if (playLocus.size > 0) {
                    drawDrone(
                        playLocus[0].latitude,
                        playLocus[0].longitude,
                        playLocus[0].angle,
                        3
                    )
                    playedLocus.add(playLocus.removeFirst())
                    playProgress = 1 - (playLocus.size.toFloat() / originalLocus.size.toFloat())
                    if (playLocus.isNotEmpty()) {
                        Log.d(
                            "zhy",
                            "delay: ${(playLocus[0].time - playedLocus[playedLocus.size - 1].time) / playSpeed}"
                        )
                        delay((playLocus[0].time - playedLocus[playedLocus.size - 1].time) / playSpeed)
                    } else {
                        resetLocus()
                        break
                    }
                } else {
                    playState = true
                    resetLocus()
                }
            }
            playState = false
        }
    }

    private fun collectSortieDetail() {
        mineModel.getSortieDetail(sortieId = sortieId, droneId = droneId, startTime = startTime)
    }

    @Composable
    override fun ContentPage() {
        SortieDetailPage()
    }

    @Composable
    fun SortieDetailPage() {
        val info = mineModel.info.observeAsState()
        val track = mineModel.track.observeAsState()
        val context = LocalContext.current
        var flow = 0f
        if (track.value?.sprayCapacity != null && track.value?.timeLength != null) {
            flow = (track.value!!.sprayCapacity / (track.value!!.timeLength / 60.0)).toFloat()
        }
        var showDetails by remember { mutableStateOf(true) }
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
                    .size(36.dp)
                    .clip(shape = CircleShape)
                    .align(Alignment.TopStart)
                    .clickable { finish() },
                color = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(vertical = 6.dp),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Black
                )
            }

            if (showDetails) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = SIMPLE_BAR_HEIGHT + 10.dp, bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(300.dp)
                            .background(BlackAlpha, shape = MaterialTheme.shapes.medium),
                        verticalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        // ID
                        SortieDetailRow(
                            title = stringResource(id = R.string.dev_detail_base_plan_id),
                            content = droneId
                        )
                        // 开始时间
                        SortieDetailRow(
                            title = stringResource(id = R.string.dev_detail_start_time),
                            content = "${info.value?.startTime?.millisToDateTime()}"
                        )
                        // 结束时间
                        SortieDetailRow(
                            title = stringResource(id = R.string.dev_detail_end_time),
                            content = "${info.value?.endTime?.millisToDateTime()} "
                        )
                        // 飞行时长
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_duration),
                            content = formatSecond(track.value?.timeLength ?: 0L, true)
                        )
                        // 飞行速度
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_speed),
                            content = mineModel.speedValue.value.toString(1)
                        )
                        // 相对高度
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_high),
                            content = mineModel.heightValue.value.toString(1)
                        )
                        // 喷洒量(升)
                        SortieDetailRow(
                            title = stringResource(
                                R.string.sortie_detail_drug,
                                UnitHelper.capacityUnit()
                            ),
                            content = UnitHelper.transCapacity(info.value?.sprayCapacity ?: 0f)
                        )
                        // 流量(升/分钟)
                        SortieDetailRow(
                            title = stringResource(
                                R.string.sortie_detail_flow,
                                UnitHelper.capacityUnit()
                            ),
                            content = UnitHelper.transCapacity(flow)
                        )
                        // 喷幅(米)
                        SortieDetailRow(
                            title = stringResource(
                                R.string.sortie_detail_line_spacing,
                                UnitHelper.lengthUnit()
                            ),
                            content = UnitHelper.transLength(info.value?.sprayWidth ?: 0f)
                        )
                        // 架次编号
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_sortie_id),
                            content = sortieId.toString()
                        )
                        // 作业面积
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_area),
                            content = UnitHelper.transAreaWithUnit(
                                context,
                                info.value?.sprayRange ?: 0f
                            )
                        )
                        // 地理位置
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_region),
                            content = info.value?.region?.detailName ?: ""
                        )
                        // 作业飞手
                        SortieDetailRow(
                            title = stringResource(id = R.string.sortie_detail_flighter),
                            content = info.value?.operUserName ?: ""
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .width(40.dp)
                            .height(80.dp)
                            .background(
                                color = BlackAlpha,
                                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .noEffectClickable {
                                    showDetails = false
                                }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(top = SIMPLE_BAR_HEIGHT + 30.dp)
                        .width(40.dp)
                        .height(80.dp)
                        .background(
                            color = BlackAlpha,
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .fillMaxSize()
                            .noEffectClickable {
                                showDetails = true
                            }
                    )
                }
            }
            if (trackInfo != null && trackInfo!!.isNotEmpty()) {
                VideoPanel(
                    modifier = Modifier
                        .padding(end = 10.dp, bottom = 20.dp)
                        .width(300.dp)
                        .height(90.dp)
                        .align(Alignment.BottomEnd)
                        .background(color = BlackAlpha, shape = MaterialTheme.shapes.medium)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clickable(enabled = false) { },
                    buttonColor = androidx.compose.ui.graphics.Color.White,
                    textColor = androidx.compose.ui.graphics.Color.Black,
                    playState = playState,
                    playSpeed = playSpeed,
                    progress = playProgress,
                    onClickPlay = { state ->
                        playState = state
                        playFlyLocus()
                    },
                    onClickStop = {
                        playState = false
                        playProgress = 0f
                        resetLocus()
                    },
                    onClickSpeed = { speed ->
                        playSpeed = speed
                    },
                    onProgressChange = { pos ->
                        changeLocus(pos)
                    }
                )
            }

        }
    }
}

@Composable
fun SortieDetailRow(title: String, content: String) {
    val textSpaced = 20.dp
    val labelWidth = 140.dp
    val autoScrollingWidth = 140.dp
    val rowPadding = 12.dp
    Row(
        modifier = Modifier.padding(horizontal = rowPadding),
        horizontalArrangement = Arrangement.spacedBy(textSpaced),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AutoScrollingText(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            width = labelWidth,
            color = MaterialTheme.colorScheme.onPrimary
        )
        AutoScrollingText(
            text = content,
            width = autoScrollingWidth,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}