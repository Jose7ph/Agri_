package com.jiagu.ags4.scene.work

import android.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.repo.db.AgsDB
import com.jiagu.ags4.repo.db.LocalNoFlyZone
import com.jiagu.ags4.repo.net.model.NoFlyZoneInfo
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing
import com.jiagu.device.vkprotocol.VKAg
import com.jiagu.tools.map.IMapCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//已作业轨迹
fun MapVideoActivity.drawWorkLine() {
    collectFlow(DroneModel.blockPlan) {
        drawWorkLine(it)
    }
}

//断点12
fun MapVideoActivity.drawCalcBreaks(taskModel: TaskModel) {
    collectFlow(taskModel.calcBreaks) {
        if (it.isNotEmpty()) {
            when (it.size) {
                3 -> {
                    drawABBreak(it[0].lat, it[0].lng)
                    drawABBreak1(it[1].lat, it[1].lng)
                    drawABBreak2(it[2].lat, it[2].lng)
                }

                2 -> {
                    drawABBreak(it[0].lat, it[0].lng)
                    drawABBreak1(it[1].lat, it[1].lng)
                    removeABBreak2()
                }

                1 -> {
                    drawABBreak(it[0].lat, it[0].lng)
                    removeABBreak1()
                    removeABBreak2()
                }
            }
        }else{
            removeAllCalcBreakpoint()
        }
    }
}

//根据imu数据清除断点
fun MapVideoActivity.clearBreaksByIMU(taskModel: TaskModel) {
    collectFlow(DroneModel.imuData.asFlow()) {
        taskModel.setImuData(it)
        if (taskModel.checkStartEndMode(it)) removeAuxLine()

        taskModel.breakWp?.let { bk ->
            if (taskModel.selectBreakIndex != -1) { //选择了计算的断点
                taskModel.curCalcBK?.let { curBK ->
                    if (it.target.toInt() > curBK.index + 1) {
                        removeAllCalcBreakpoint()
                        taskModel.clearCalcBK()
                    }
                }
            } else {
                if (it.target.toInt() > bk.index + 1) {
                    removeBreakPoint()
                }
            }
        }
    }
}

private var highlighted = ""
fun unhighlightMarker(canvas: IMapCanvas) {
    val idx = canvas.indexOfNumberMarker(highlighted)
    if (idx >= 0) {
        canvas.highlightLetterMarker(
            highlighted, "${idx + 1}", IMapCanvas.Params.MARKER_OTHER_COLOR, false
        )
    }
}

fun highlightMarker(canvas: IMapCanvas, idx: Int) {
    if (idx < 0) return
    val name = canvas.nameOfNumberMarker(idx)
    highlighted = name
    canvas.highlightLetterMarker(
        name, "${idx + 1}", IMapCanvas.Params.MARKER_HL_COLOR, true
    )
}

fun MapVideoActivity.removeAuxLine() {
    canvas.remove("aux_point_line")
}

fun MapVideoActivity.drawAuxLine(lats: List<GeoHelper.LatLng>) {
    canvas.drawLine(
        "aux_point_line",
        lats,
        IMapCanvas.Params.MARKER_AUX_COLOR.toInt(),
        IMapCanvas.Z_HL_MARKER,
        8f
    )
}

fun MapVideoActivity.removeDeletePointLine() {
    canvas.remove("delete_point_line")
}

fun MapVideoActivity.drawDeletePointLine(lats: List<GeoHelper.LatLng>) {
    canvas.drawLine(
        "delete_point_line",
        lats,
        IMapCanvas.Params.ZONE_STROKE_COLOR.toInt(),
        IMapCanvas.Z_HL_MARKER,
        5f
    )
}

private var auxNum = 0
fun MapVideoActivity.drawAuxPoint(data: List<GeoHelper.LatLng>) {
    for ((idx, pt) in data.withIndex()) {
        canvas.drawLetterMarker(
            "aux____$idx",
            pt.latitude,
            pt.longitude,
            (idx + 1).toString(),
            IMapCanvas.Params.MARKER_AUX_COLOR.toInt()
        )
    }
    for (idx in data.size until auxNum) {
        canvas.remove("aux____$idx")
    }
    auxNum = data.size
}

private var highlightedAux = listOf<String>()
fun unhighlightAuxMarker(canvas: IMapCanvas) {
    for ((_, name) in highlightedAux.withIndex()) {
        val isAux = name.contains("aux")
        val na = if (isAux) name.replace("aux", "") else name
        val color =
            if (isAux) IMapCanvas.Params.MARKER_AUX_COLOR.toInt() else IMapCanvas.Params.MARKER_OTHER_COLOR
        val idx = canvas.indexOfNumberMarker(na)
        canvas.highlightLetterMarker(name, "${idx + 1}", color, false)
    }
    highlightedAux = listOf()
}

fun highlightAuxMarker(canvas: IMapCanvas, names: List<String>) {
    for (name in names) {
        val idx =
            canvas.indexOfNumberMarker(if (name.contains("aux")) name.replace("aux", "") else name)
        canvas.highlightLetterMarker(name, "${idx + 1}", IMapCanvas.Params.MARKER_HL_COLOR, true)
    }
    val set1 = names.toSet()
    val old = names.filter { it !in set1 }
    for (i in 0 until old.size) {
        val name = old[i]
        val isAux = name.contains("aux")
        val na = if (isAux) name.replace("aux", "") else name
        val color =
            if (isAux) IMapCanvas.Params.MARKER_AUX_COLOR.toInt() else IMapCanvas.Params.MARKER_OTHER_COLOR
        val idx = canvas.indexOfNumberMarker(na)
        canvas.highlightLetterMarker(name, "${idx + 1}", color, false)
    }
    highlightedAux = names
}

private var barrierNum = 0
fun drawBarriers(canvas: IMapCanvas, barriers: List<MapRing>) {
    //如果barriers为空，且barrierNum>0，则清空已绘制的barriers
    if (barriers.isEmpty() && barrierNum > 0) {
        for (i in 0..barrierNum) {
            canvas.remove("barrier$i")
            canvas.remove("edge_b$i")
        }
        return
    }
    var index = 0
    for (barrier in barriers) {
        val name = "barrier$index"
        canvas.drawBarrier(name, barrier, false)
        index++
    }
    for (i in index until barrierNum) {
        canvas.remove("barrier$i")
        canvas.remove("edge_b$i")
    }
    barrierNum = index
}

fun MapVideoActivity.drawBreakpoint(lat: Double, lng: Double) {
    canvas.drawLetterMarker("break_point", lat, lng, "Br", Color.YELLOW and 0x7FFFFFFF)
}

fun MapVideoActivity.removeBreakPoint() = canvas.remove("break_point")

fun MapVideoActivity.drawABBreak(lat: Double, lng: Double) {
    canvas.drawLetterMarker("BK", lat, lng, "Bk", Color.RED)
}

fun MapVideoActivity.drawABBreak1(lat: Double, lng: Double) {
    canvas.drawLetterMarker("BK1", lat, lng, "1", Color.RED)
}

fun MapVideoActivity.drawABBreak2(lat: Double, lng: Double) {
    canvas.drawLetterMarker("BK2", lat, lng, "2", Color.RED)
}

fun MapVideoActivity.removeABBreak() = canvas.remove("BK")
fun MapVideoActivity.removeABBreak1() = canvas.remove("BK1")
fun MapVideoActivity.removeABBreak2() = canvas.remove("BK2")

//绘制地块规划轨迹
private var workIndex = 0
fun MapVideoActivity.drawWorkLine(bp: BlockPlan?) {
    if (bp == null || bp.finish) {
        for (i in 0 until workIndex) {
            canvas.remove("work:$i")
        }
        workIndex = 0
    } else {
        if (bp.workRoute?.workRoute != null) {
            for (i in 0 until workIndex) {
                canvas.remove("work:$i")
            }
            for (i in 0 until (bp.workRoute!!.workRoute.size)) {
                canvas.drawLine(
                    "work:$i",
                    bp.workRoute!!.workRoute[i],
                    Color.GRAY,
                    IMapCanvas.Z_MARKER,
                    IMapCanvas.Params.COMPLETION_WIDTH
                )
            }
            workIndex = bp.workRoute?.workRoute?.size ?: 0
        } else {
            for (i in 0 until workIndex) {
                canvas.remove("work:$i")
            }
            workIndex = 0
        }
    }
}

//禁飞区
val noFlyZones = MutableStateFlow<List<LocalNoFlyZone>>(listOf())
fun MapVideoActivity.getNoFlyZone() {
    lifecycleScope.launch {
        withContext(Dispatchers.IO) {
            val lat = DroneModel.imuData.value?.lat ?: 0.0
            val lng = DroneModel.imuData.value?.lng ?: 0.0
            val off = GeoHelper.boundOffset(GeoHelper.LatLng(lat, lng), 50000.0f)
            val n = lat + off.latitude
            val s = lat - off.latitude
            val e = lng + off.longitude
            val w = lng - off.longitude
            val noFlyZonesTemp = AgsDB.getNoFlyZoneList(w, e, s, n)
            noFlyZones.emit(noFlyZonesTemp)
        }
        collectNoFlyZone()
    }
}

fun MapVideoActivity.collectNoFlyZone() {
    collectFlow(noFlyZones) {
        repeat(it.size) { idx ->
            val noFlyZone = it[idx]
            val boundary = NoFlyZoneInfo.convertBoundary(noFlyZone.orbit)
            canvas.drawBlock(
                noFlyZone.noflyId.toString(), boundary, false, 0x4DE12C2C
            )
        }
    }
}

fun MapVideoActivity.removeSE() {
    canvas.remove("start")
    canvas.remove("end")
}

val auxReq: MutableStateFlow<Int> = MutableStateFlow(0)
val wpListener: (Any) -> Unit = {
    if (it is VKAg.REQData) {
        auxReq.value = it.req.toInt()
    }
}

fun startDataListener() {
    DroneModel.activeDrone?.startDataMonitor(wpListener)
}

fun stopDataListener() {
    DroneModel.activeDrone?.stopDataMonitor(wpListener)
}


fun MapVideoActivity.collectTrack() {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            trackModel.track.collectLatest {
                if (it.isEmpty()) clearWorkLine()
                else {
                    //删除之前的线，只保留比如100条线，index会一直累加 1 100 101 201 300
                    for (i in 1 until it.first().index) {
                        canvas.remove("track_$i")
                    }
                    //每次只画最后一条线
                    canvas.drawLine(
                        "track_${it.last().index}",
                        it.last().track,
                        if (it.last().pump) Color.GREEN else Color.WHITE,
                        IMapCanvas.Z_HL_LINE
                    )
                    mapVideoModel.workLineTail = it.last().index
                }
            }
        }
    }
}

fun MapVideoActivity.clearWorkLine() {
    trackModel.clear()
    for (i in 0 until mapVideoModel.workLineTail + 1) {
        canvas.remove("track_$i")
    }
    mapVideoModel.workLineTail = 0
}

//根据索引删除计算的AB断点和断点1、2
fun MapVideoActivity.removeCalcBreaksByIndex(index: Int) {
    when (index) {
        -1 -> { //未选择断点，则全部删除 保留br
            removeABBreak()
            removeABBreak1()
            removeABBreak2()
        }

        0 -> { //断点BK 保留bk
            removeABBreak1()
            removeABBreak2()
            removeBreakPoint()
        }

        1 -> {//断点1 保留断点1
            removeABBreak()
            removeABBreak2()
            removeBreakPoint()
        }

        2 -> {//断点2 保留断点2
            removeABBreak()
            removeABBreak1()
            removeBreakPoint()
        }
    }
}

fun MapVideoActivity.removeAllCalcBreakpoint() {
    removeABBreak()
    removeABBreak1()
    removeABBreak2()
}