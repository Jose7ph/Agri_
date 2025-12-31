package com.jiagu.ags4.scene.work

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.jiagu.ags4.R
import com.jiagu.ags4.utils.optimizeRoute
import com.jiagu.ags4.vm.BlockParamModel
import com.jiagu.ags4.vm.DroneModel
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.tools.v9sdk.OutPathRouteModel
import com.jiagu.tools.v9sdk.RouteModel
import com.jiagu.ags4.vm.TaskModel
import com.jiagu.api.ext.toast
import com.jiagu.api.helper.GeoHelper
import com.jiagu.device.model.RoutePoint
import com.jiagu.tools.map.IMapCanvas

private class BlockParamMapClickListener(
    val routeModel: RouteModel,
    val blockParamModel: BlockParamModel,
    val canvas: IMapCanvas,
) : IMapCanvas.MapClickListener {
    override fun onClick(pt: GeoHelper.LatLng) {
        //移动航线、删除航线不触发任何地图、航点点击事件
        if (blockParamModel.showMoveRoutePanel || blockParamModel.showDeletePointPanel) return
        //必须是编辑参数 && 不是删除航点panel出现的时候(防止删除航点误触) 修改起始点
        if (blockParamModel.curPlanType == RouteModel.PLAN_NEW && !blockParamModel.showDeletePointPanel) {
            routeModel.sortRoute(pt,blockParamModel.toRouteParameter())
        }
        blockParamModel.clearMarker()
        //当删除航点panel显示时，点击地图，不隐藏高亮点
        if (!blockParamModel.showDeletePointPanel) {
            unhighlightMarker(canvas)
        }
    }
}

class RouteMarkClickListener(
    val canvas: IMapCanvas, val blockParamModel: BlockParamModel, val mapVideoModel: MapVideoModel,
) : IMapCanvas.MapMarkerSelectListener {
    override fun onMarkerSelect(marker: String) {
        //移动航线时不触发任何地图、航点点击事件
        if (blockParamModel.showMoveRoutePanel) return
        var markerIndex: Int
        if (marker.contains("aux")) { //中转点点击事件
            unhighlightAuxMarker(canvas)
            val markerName = marker.replace("aux", "")
            markerIndex = canvas.indexOfNumberMarker(markerName)
            if (blockParamModel.selectedMarkerIndex == markerIndex) {
                return
            }
            blockParamModel.markerSelect(markerIndex)
            highlightAuxMarker(
                canvas,
                listOf(marker)
            )
        }
        else {//删除航线点击事件
            if (blockParamModel.planType == RouteModel.PLAN_EDGE) return
            val idx = canvas.indexOfNumberMarker(marker)
            blockParamModel.markerDeletePointSelect(idx)

        }
    }
}

fun MapVideoActivity.collectBlockParam(
    blockParamModel: BlockParamModel,
    locationModel: LocationModel,
    routeModel: RouteModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
) {
    showMarker = true
    addMapChangeListener(MapChangedListener(canvas, locationModel))
    addMapClickListener(BlockParamMapClickListener(routeModel, blockParamModel, canvas))
    addMarkClickListener(RouteMarkClickListener(canvas, blockParamModel, mapVideoModel))
    //中转点
    collectFlow(blockParamModel.auxPointsFlow) {
        findStartAuxPath(routeModel, blockParamModel, taskModel, outPathRouteModel) {}
    }
    collectFlow(blockParamModel.selectedBPFlow) {
        if (it.isNotEmpty()) {
            val block = it[0]
            canvas.drawBlock(
                "block", block.boundary[0], true
            )
            drawBarriers(canvas, block.barriers)
            droneCanvas.drawDistance(block.boundary[0], true)
        }
    }
    //已作业轨迹
    drawWorkLine()
    collectFlow(routeModel.baseEdge) {
        it?.let {
            canvas.drawPolyline("edge", it, "#FFFF8D1A".toColorInt(), 10f)
        }
    }
    collectFlow(routeModel.route) { track ->
        when {
            track == null || track.isEmpty() -> clearTrack()
            track.size > 800 -> toast(R.string.plan_toomany_wp)
            else -> {
                showTrack(track, blockParamModel.planType) { allMarkerMap ->
                    blockParamModel.curMarkerCount =
                        allMarkerMap.filter { it.key.startsWith("route_") }.size
                    blockParamModel.curAllMarkers = allMarkerMap
                }
                findStartAuxPath(routeModel, blockParamModel, taskModel, outPathRouteModel) {}
            }
        }
    }
    //断点
    collectFlow(taskModel.breakWpFlow) {
        if (it == null) {
            DroneModel.bk = null
            removeBreakPoint()
        } else {
            DroneModel.bk = it//架次中的断点需要的
            drawBreakpoint(it.lat, it.lng)

            routeModel.bk = GeoHelper.LatLngAlt(it.lat, it.lng, it.alt.toDouble())
            routeModel.target = it.index.toInt() + 1
        }
    }
    //选择的高亮中转点
    collectFlow(blockParamModel.selectedMarkerIndexFlow) {
        unhighlightAuxMarker(canvas)
        if (it >= 0) {
            highlightAuxMarker(canvas, listOf("aux${canvas.nameOfNumberMarker(it)}"))
        }
    }
    //选择的高亮删除航点
    collectFlow(blockParamModel.selectedDeletePointIndexesFlow) {
        unhighlightAuxMarker(canvas)
        removeDeletePointLine()
        if (it.isNotEmpty()) {
            val markerNames = mutableListOf<String>()
            it.forEach { idx ->
                markerNames.add(canvas.nameOfNumberMarker(idx))
            }
            highlightAuxMarker(canvas, markerNames.toList())

            val startKey = "route_${it.first()}"
            val endKey = "route_${it.last()}"
            val lines = blockParamModel.curAllMarkers.entries
                .dropWhile { marker -> marker.key != startKey }// 丢弃直到找到startKey
                .takeWhile { marker -> marker.key != endKey } // 取到 endKey 之前（不包括 endKey）
                .map { marker -> marker.value }            // 提取 value
                .plus(blockParamModel.curAllMarkers[endKey])         // 手动加上 endKey 的 value
                .filterNotNull()             // 确保 endKey 存在（避免 null）
            drawDeletePointLine(lines)
        }
    }
}

var showMarker = false
private var trackNum = 0
private var connNum = 0
fun MapVideoActivity.clearTrack() {
    for (i in 0 until trackNum) {
        canvas.remove("navi$i")
    }
    trackNum = 0
    for (i in 0 until connNum) {
        canvas.remove("conn$i")
    }
}

fun MapVideoActivity.showTrack(
    ps: List<RoutePoint>,
    planType: Int,
    complete: (LinkedHashMap<String, GeoHelper.LatLng>) -> Unit,
) {
    var pts = mutableListOf<RoutePoint>()
    pts.addAll(ps)
    if (planType == RouteModel.PLAN_EDGE || planType == RouteModel.PLAN_BLOCK_EDGE) {
        pts = optimizeRoute(pts).toMutableList()
    }
    var lineNum = 0
    var tn = 0
    var even = true
    val t = mutableListOf<GeoHelper.LatLng>()
    val tt = mutableListOf<MutableList<RoutePoint>>()
    var prePump = pts[0].pump
    for (s in pts) {
        lineNum += 1
        t.add(s)
        if (prePump && !s.pump) {
            tt.add(mutableListOf(s))
        } else if (!prePump) {
            if (tt.isNotEmpty()) tt.last().add(s)
        }
        if (s.goHome) {
            val color = if (even) Color.RED else Color.BLUE
            even = !even
            canvas.drawPolyline("navi$tn", t, color, 2f)
            tn++
            t.clear()
        }
        prePump = s.pump
    }
    if (t.isNotEmpty()) {
        val color = if (even) Color.RED else Color.BLUE
        canvas.drawPolyline("navi$tn", t, color, 2f)
        tn++
    }
    for (i in 0 until connNum) {
        canvas.remove("conn$i")
    }
    connNum = tt.size
    for ((i, ppts) in tt.withIndex()) {
        canvas.drawLine("conn$i", ppts, Color.WHITE, IMapCanvas.Z_HL_LINE, 2f)
    }
    for (i in tn until trackNum) {
        canvas.remove("navi$i")
    }
    trackNum = tn
    if (showMarker) {
        showMarker(ps, planType, complete)
        removeSE()
    } else {
        canvas.drawLetterMarker(
            "start",
            pts[0].latitude,
            pts[0].longitude,
            "S",
            Color.DKGRAY and 0x7FFFFFFF
        )
        canvas.drawLetterMarker(
            "end",
            pts.last().latitude,
            pts.last().longitude,
            "E",
            Color.DKGRAY and 0x7FFFFFFF
        )
    }
}

private fun MapVideoActivity.showMarker(
    pts: List<RoutePoint>,
    planType: Int,
    complete: (LinkedHashMap<String, GeoHelper.LatLng>) -> Unit,
) {
    val markerMap = linkedMapOf<String, GeoHelper.LatLng>()
    val markers = mutableListOf<GeoHelper.LatLng>()
    if (planType == RouteModel.PLAN_EDGE) {
        var toggle = false
        for ((i, pt) in pts.withIndex()) {
            if (pt.index > 0) toggle = !toggle
            if (toggle) {
                val p2 = pts[i + 1]
                markers.add(
                    GeoHelper.LatLng(
                        (pt.latitude + p2.latitude) / 2,
                        (pt.longitude + p2.longitude) / 2
                    )
                )
            }
        }
    } else {
        var index = 0
        for (i in pts.indices) {
            val pt = pts[i]
            if (pt.index > 0) {
                markers.add(pt)
                markerMap["route_${index}"] = pt
                index++
            } else {
                markerMap["other_${i}"] = pt
            }
        }
    }
    canvas.drawNumberMarker(markers)
    complete(markerMap)
}

fun MapVideoActivity.findStartAuxPath(
    routeModel: RouteModel,
    blockParamModel: BlockParamModel,
    taskModel: TaskModel,
    outPathRouteModel: OutPathRouteModel,
    complete: () -> Unit,
) {
    if (routeModel.route.value == null) {
        complete()
        return
    }
//    outPathRouteModel.barrierSafeDist = routeModel.barrierSafeDist
//    outPathRouteModel.planType = routeModel.planType
    outPathRouteModel.home = routeModel.home
    blockParamModel.calcAuxPoints.clear()
    outPathRouteModel.findStartPathByBarrier(
        if (DroneModel.imuData.value == null) null else GeoHelper.LatLng(
            DroneModel.imuData.value!!.lat,
            DroneModel.imuData.value!!.lng
        ),
        blockParamModel.auxPoints,
        blockParamModel.obstacles,
        blockParamModel.block,
        if (DroneModel.breakPoint.value != null) GeoHelper.LatLng(
            DroneModel.breakPoint.value!!.lat,
            DroneModel.breakPoint.value!!.lng
        )
        else if (routeModel.route.value != null) GeoHelper.LatLng(
            routeModel.route.value!![0].latitude,
            routeModel.route.value!![0].longitude
        )
        else null,
        barrierSafeDist = blockParamModel.barrierSafeDist
    ) {
        if (it == null) {
            blockParamModel.calcAuxPoints.addAll(listOf())
            blockParamModel.addAuxWarn()
        } else if (it.isEmpty()) {
            blockParamModel.calcAuxPoints.addAll(listOf())
        } else {
            blockParamModel.calcAuxPoints.addAll(it)
        }
        drawStartCalcAuxPoints(
            routeModel,
            blockParamModel,
            taskModel
        )
        complete()
    }
}

private fun MapVideoActivity.drawStartCalcAuxPoints(
    routeModel: RouteModel,
    blockParamModel: BlockParamModel,
    taskModel: TaskModel,
) {
    //marker点不需要画飞机位置和断点位置
    drawAuxPoint(blockParamModel.auxPoints)
    if (blockParamModel.calcAuxPoints.isEmpty()) {//不需要绕障，不需要画线
        if (blockParamModel.auxPoints.isNotEmpty()) {
            drawAuxLine(blockParamModel.auxPoints)
        } else {
            removeAuxLine()
        }
        return
    }
    //线需要连接飞机位置和断点位置
    val mutableList = blockParamModel.calcAuxPoints.toMutableList()
    if (DroneModel.imuData.value != null) {
        mutableList.add(
            0,
            GeoHelper.LatLng(DroneModel.imuData.value!!.lat, DroneModel.imuData.value!!.lng)
        )
    }
    if (taskModel.breakWpFlow.value != null) {
        mutableList.add(
            GeoHelper.LatLng(
                taskModel.breakWpFlow.value!!.lat,
                taskModel.breakWpFlow.value!!.lng
            )
        )
    } else if (routeModel.route.value != null) {
        mutableList.add(routeModel.route.value!![0])
    }
    drawAuxLine(mutableList)
}