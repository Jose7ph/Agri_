package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.R
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.bean.MeasurePole
import com.jiagu.ags4.repo.sp.Config
import com.jiagu.ags4.scene.work.PointTypeEnum
import com.jiagu.ags4.utils.arrayToCalib
import com.jiagu.ags4.utils.calibToArray
import com.jiagu.ags4.utils.exeComplexTask
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.utils.mergeAlt
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.IWorkMapMarker
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.WorkMapMarkerImpl
import com.jiagu.ags4.vm.work.IWorkEdit
import com.jiagu.ags4.vm.work.IWorkKml
import com.jiagu.ags4.vm.work.WorkEditImpl
import com.jiagu.ags4.vm.work.WorkKmlImpl
import com.jiagu.ags4.voice.VoiceMessage
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.helper.MathHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MapRing3D
import com.jiagu.api.model.MathRing
import com.jiagu.api.viewmodel.getString
import com.jiagu.tools.math.GeometryHelper
import kotlinx.coroutines.flow.MutableStateFlow
import org.locationtech.jts.geom.GeometryFactory
import kotlin.math.cos
import kotlin.math.sin

class BlockEditModel(app: Application) : AndroidViewModel(app),
    IWorkEdit by WorkEditImpl(),
    IWorkMapMarker by WorkMapMarkerImpl(),
    IWorkKml by WorkKmlImpl() {
    val context = getApplication<Application>()
    val config = Config(app)
    private val converter = GeoHelper.GeoCoordConverter()
    private val factory = GeometryFactory()
    var pointType by mutableStateOf(PointTypeEnum.EDGE)
    var errMessage by mutableStateOf<ErrorMessageEnum?>(ErrorMessageEnum.NONE)
    var area by mutableFloatStateOf(0f)
    var editBlock: BlockPlan? = null

    val edgePoints = mutableStateListOf<GeoHelper.LatLngAlt>() //边界点
    val edgePointsFlow = MutableStateFlow<MapRing>(emptyList()) //边界点canvas
    val obstaclePoints = mutableStateListOf(mutableListOf<GeoHelper.LatLngAlt>()) //障碍点
    val obstaclePointsFlow = MutableStateFlow<List<MapRing>>(emptyList()) //障碍点canvas
    val circleObstaclePoints = mutableStateListOf<MeasurePole>() //圆形障碍点
    val circleObstaclePointsFlow = MutableStateFlow<List<MapRing3D>>(emptyList()) //圆形障碍点canvas
    var calibPt: GeoHelper.LatLngAlt? = null //校准点
    val lineFlow = MutableStateFlow<MapRing>(listOf())

    //dialog control
    var showObstacleErrorDialog by mutableStateOf(false) //障碍点校验错误弹窗控制 点击地图触发用
    var showBluetoothList by mutableStateOf(false) //蓝牙列表弹窗

    var canvasFit = false

    //编辑状态初始化当前blockplan
    fun initEditBlockPlan(blockPlan: BlockPlan) {
        editBlock = blockPlan
        val boundary = blockPlan.boundary
        if (boundary.isNotEmpty()) {
            edgePoints.addAll(boundary[0])
            obstaclePoints.clear()
            for (i in 1 until boundary.size) {
                val h = mutableListOf<GeoHelper.LatLngAlt>()
                h.addAll(boundary[i])
                obstaclePoints.add(h)
            }
            obstaclePoints.add(mutableListOf())
        }
        val pts = arrayToCalib(blockPlan.calibPoints)
        calibPt = if (pts.isEmpty()) null else pts[0]
        curObstacle = obstaclePoints.lastIndex
        exeTask {
            pushEditBlockPlan()
        }
        calcArea()
    }

    //导入kml地块
    fun importKmlBlock() {
        kmlBlock?.let {
            val block = it.first
            val blockAlt = it.second
            if (block.isNotEmpty()) {
                edgePoints.addAll(mergeAlt(block[0], blockAlt[0]))
                for (i in 1 until block.size) {
                    val h = mutableListOf<GeoHelper.LatLngAlt>()
                    h.addAll(mergeAlt(block[i], blockAlt[i]))
                    obstaclePoints.add(h)
                }
            }
            if (obstaclePoints.isNotEmpty()) {
                curObstacle = obstaclePoints.lastIndex
            }
            exeTask {
                edgePointsFlow.emit(edgePoints.toList())
                pushLine(edgePoints.toList())
                val mappedObstacles = obstaclePoints.map { it.toList() } // 使用更简洁的写法
                obstaclePointsFlow.emit(mappedObstacles)
                canvasFit = true
            }
            calcArea()
        }
    }

    //清除全部点
    fun clearAllPoints() {
        clearAllEdgePoint()
        clearAllObstaclePoint()
        clearAllCircleObstaclePoint()
    }

    private suspend fun pushEditBlockPlan() {
        edgePointsFlow.emit(edgePoints.toList())
        lineFlow.emit(edgePoints.toList())

        val obstacles = mutableListOf<MapRing>()
        for (obstacle in obstaclePoints) {
            obstacles.add(obstacle.toList())
        }
        obstaclePointsFlow.emit(obstacles)

        val circleObstacles = mutableListOf<MapRing3D>()
        for (circleObstacle in circleObstaclePoints) {
            circleObstacles.add(
                makePoleArea(
                    circleObstacle.center,
                    circleObstacle.radius.toDouble()
                )
            )
        }
        circleObstaclePointsFlow.emit(circleObstacles.toList())
    }

    fun addPoint(point: GeoHelper.LatLngAlt) {
        when (pointType) {
            PointTypeEnum.EDGE -> {
                addEdgePoint(point)
            }

            PointTypeEnum.OBSTACLE -> {
                addObstaclePoint(point)
            }

            PointTypeEnum.CIRCLE_OBSTACLE -> {
                val radius = config.poleRadius
                addCircleObstaclePoint(point, if (radius < 1) 1.0 else radius.toDouble())
            }
        }
    }

    //点击marker drag 和 移动点时调用
    fun updatePoint(pt: GeoHelper.LatLngAlt) {
        when (pointType) {
            PointTypeEnum.EDGE -> updateEdgePoint(selectedMarkerIndex, pt)
            PointTypeEnum.OBSTACLE -> updateObstaclePoint(selectedMarkerIndex, pt)
            PointTypeEnum.CIRCLE_OBSTACLE -> updateCircleObstaclePoint(pt)
        }
    }

    fun changePointType(type: PointTypeEnum) {
        pointType = type
        exeTask {
            lineFlow.emit(emptyList())
        }
    }

    fun saveBlock(name: String, complete: (List<Long>) -> Unit) {
        val calibPoints = if (calibPt == null) doubleArrayOf() else calibToArray(listOf(calibPt!!))
        val block = Block(
            Block.TYPE_BLOCK, name, toMapBlock(), calibPoints, area, DroneModel.groupId
        )
        exeTask {
            if (editBlock != null) {
                block.localBlockId = editBlock!!.localBlockId
                block.blockId = editBlock!!.blockId
                block.region = editBlock!!.region ?: -1
                block.regionName = editBlock!!.regionName ?: ""
                updateBlock(block = block, complete = complete)
            } else {
                buildAndSaveBlock(name = name, buildBlock = {
                    block.createTime = System.currentTimeMillis()
                    block
                }, complete = complete)
            }
        }
    }

    fun getErrMessage(): String? {
        return when (errMessage) {
            ErrorMessageEnum.ERROR_CODE_CROSS -> context.getString(R.string.measure_error_cross)
            ErrorMessageEnum.ERROR_CODE_CROSS_OBSTACLE -> context.getString(R.string.measure_error_cross_obstacle)
            ErrorMessageEnum.ERROR_CODE_BARRIER -> context.getString(R.string.measure_error_barrier)
            ErrorMessageEnum.ERROR_CODE_DISTANCE -> context.getString(R.string.measure_error_distance_save)
            ErrorMessageEnum.ERROR_CODE_DISTANCE_OBSTACLE -> context.getString(R.string.measure_error_distance_obstacle_save)
            ErrorMessageEnum.ERROR_CODE_DISTANCE_BLOCK -> context.getString(R.string.measure_error_distance_block_save)
            ErrorMessageEnum.ERROR_CODE_POINT_COUNT -> context.getString(R.string.measure_error_max_point_count)
            ErrorMessageEnum.ERROR_CODE_MAX_AREA -> context.getString(R.string.measure_error_max_area)
            else -> null
        }
    }

    //检查障碍点数量是否满足要求
    fun checkObstaclePoints(): Boolean {
        return obstaclePoints[curObstacle].size >= 3 || obstaclePoints[curObstacle].isEmpty()
    }

    private fun toMapBlock(): MapBlock3D {
        val out = mutableListOf(copyRing(edgePoints))
        obstaclePoints.forEach { if (it.isNotEmpty()) out.add(copyRing(it)) }
        circleObstaclePoints.forEach { out.add(makePoleArea(it.center, it.radius.toDouble())) }
        return out
    }

    private fun copyRing(ring: MapRing3D): MapRing3D {
        val out = mutableListOf<GeoHelper.LatLngAlt>()
        out.addAll(ring)
        return out
    }

    private fun makePoleArea(pt: GeoHelper.LatLngAlt, radius: Double): MapRing3D {
        val converter = GeoHelper.GeoCoordConverter(pt.latitude, pt.longitude)
        var rad = 0.0
        val out = mutableListOf<GeoHelper.LatLngAlt>()
        repeat(6) {
            val x = cos(rad) * radius
            val y = sin(rad) * radius
            val s = converter.convertPoint(x, y)
            out.add(GeoHelper.LatLngAlt(s.latitude, s.longitude, pt.altitude))
            rad += Math.PI / 3
        }
        return out
    }

    private fun addEdgePoint(pt: GeoHelper.LatLngAlt) {
        if (edgePoints.isNotEmpty()) {
            val p1 = converter.convertLatLng(pt)
            val p0 = converter.convertLatLng(edgePoints.last())
            if (p1.distance(p0) < 3) {
                errMessage = ErrorMessageEnum.ERROR_CODE_DISTANCE
            }
        }
        if (edgePoints.size >= 400) {
            errMessage = ErrorMessageEnum.ERROR_CODE_POINT_COUNT
        }
        edgePoints.add(pt)
        VoiceMessage.emit(getString(R.string.voice_edge_pt))
        VoiceMessage.emit(edgePoints.size.toString())
        exeTask {
            pushEdgePoint()
        }
    }

    private fun updateEdgePoint(idx: Int, pt: GeoHelper.LatLngAlt) {
        if (idx < 0 || idx >= edgePoints.size) return
        edgePoints[idx] = pt
        exeTask {
            pushEdgePoint()
        }
    }

    //清除全部边界点
    fun clearAllEdgePoint() {
        clearMarker()
        edgePoints.clear()
        exeTask {
            pushEdgePoint()
        }
    }

    private suspend fun pushEdgePoint() {
        edgePointsFlow.emit(edgePoints.toList())
        pushLine(edgePoints.toList())
        calcArea()
    }

    private var curObstacle = 0
    fun addObstaclePoint(pt: GeoHelper.LatLngAlt) {
        val obstacles = obstaclePoints[curObstacle]
        if (obstacles.isNotEmpty()) {
            val p1 = converter.convertLatLng(pt)
            val p0 = converter.convertLatLng(obstacles.last())
            if (p1.distance(p0) < 3) {
                errMessage = ErrorMessageEnum.ERROR_CODE_DISTANCE
            }
        }
        obstacles.add(pt)
        exeTask {
            pushObstaclePoint()
        }
    }

    private fun updateObstaclePoint(idx: Int, pt: GeoHelper.LatLngAlt) {
        if (curObstacle >= obstaclePoints.size || curObstacle < 0) return
        val obstacles = obstaclePoints[curObstacle]
        if (idx >= obstacles.size) return
        obstacles[idx] = pt
        exeTask {
            pushObstaclePoint()
        }
    }

    //清除当前障碍点
    fun clearObstaclePoint() {
        showObstacleErrorDialog = false
        obstaclePoints[curObstacle].clear()
        exeTask {
            pushObstaclePoint()
        }
    }

    //清除全部障碍点
    fun clearAllObstaclePoint() {
        clearMarker()
        curObstacle = 0
        obstaclePoints.clear()
        obstaclePoints.add(mutableListOf())
        exeTask {
            pushObstaclePoint()
        }
    }

    private suspend fun pushObstaclePoint() {
        if (obstaclePoints.isNotEmpty()) {
            val mappedObstacles = obstaclePoints.map { it.toList() } // 使用更简洁的写法
            obstaclePointsFlow.emit(mappedObstacles)
            pushLine(mappedObstacles[curObstacle])
        } else {
            obstaclePointsFlow.emit(emptyList())
            pushLine(emptyList())
        }
        calcArea()
    }

    fun addCircleObstaclePoint(pt: GeoHelper.LatLngAlt, radius: Double) {
        circleObstaclePoints.add(MeasurePole(pt, radius.toFloat()))
        exeTask {
            pushCircleObstaclePoint()
        }
    }

    private fun updateCircleObstaclePoint(pt: GeoHelper.LatLngAlt) {
        if (selectedMarkerIndex >= circleObstaclePoints.size) return
        circleObstaclePoints[selectedMarkerIndex].center = pt
        exeTask {
            pushCircleObstaclePoint()
        }
    }

    fun clearAllCircleObstaclePoint() {
        clearMarker()
        circleObstaclePoints.clear()
        exeTask {
            pushCircleObstaclePoint()
        }
    }

    private suspend fun pushCircleObstaclePoint() {
        if (circleObstaclePoints.isEmpty()) {
            circleObstaclePointsFlow.emit(emptyList())
        } else {
            val circleObstacles = mutableListOf<MapRing3D>()
            for (circleObstacle in circleObstaclePoints) {
                val pole = makePoleArea(
                    circleObstacle.center,
                    circleObstacle.radius.toDouble()
                )
                circleObstacles.add(pole)
            }
            circleObstaclePointsFlow.emit(circleObstacles.toList())
        }
        calcArea()
    }

    private suspend fun pushLine(mapRing: MapRing, closed: Boolean = false) {
        val out = mutableListOf<GeoHelper.LatLng>()
        out.addAll(mapRing)
        if (closed) out.add(mapRing[0])
        lineFlow.emit(out)
    }

    private fun calcArea(complete: () -> Unit = {}) {
        exeComplexTask {
            var errMsg: ErrorMessageEnum? = null
            try {
                val poly = mutableListOf<Point2D>()
                val hpoly = mutableListOf<Point2D>()
                converter.convertLatLng(edgePoints as Collection<GeoHelper.LatLng>?, poly)
                converter.convertLatLng(
                    obstaclePoints[curObstacle] as Collection<GeoHelper.LatLng>?,
                    hpoly
                )
                errMsg = checkDistanceErr()

                var bcross = false
                var a = 0.0
                val hcross = if (hpoly.size > 2) MathHelper.isPolygonCrossed(hpoly, true) else false
                if (poly.size > 2) {
                    bcross = MathHelper.isPolygonCrossed(poly, true)
                    if (!bcross && !hcross) {
                        val polygon = mutableListOf<MathRing>(poly)
                        for (h in obstaclePoints) {
                            if (h.size < 3) continue
                            val polyHole = mutableListOf<Point2D>()
                            converter.convertLatLng(h as Collection<GeoHelper.LatLng>?, polyHole)
                            polygon.add(polyHole)
                        }
                        if (hpoly.size > 2) {
                            polygon.add(hpoly)
                        }
                        for (p in circleObstaclePoints) {
                            val po = mutableListOf<Point2D>()
                            converter.convertLatLng(makePoleArea(p.center, p.radius.toDouble()), po)
                            polygon.add(po)
                        }
                        a = GeometryHelper.regularPolygonArea(factory, polygon) / 666.6667
                    }
                    if (a > 5000) errMsg = ErrorMessageEnum.ERROR_CODE_MAX_AREA
                    if (bcross) errMsg = ErrorMessageEnum.ERROR_CODE_CROSS
                    if (hcross) errMsg = ErrorMessageEnum.ERROR_CODE_CROSS_OBSTACLE
                }
                area = a.toFloat()
                errMessage = errMsg
                complete()
            } catch (e: Throwable) {
                errMessage = errMsg
                Log.e("zhy", "calcArea error:${e.message}")
            }
        }
    }

    private fun checkDistanceErr(): ErrorMessageEnum? {
        var err: ErrorMessageEnum? = null
        for (i in 0 until edgePoints.size) {
            if (edgePoints.size < 2) continue
            val index_1 = i
            val index_2 = (i + 1) % edgePoints.size
            val p1 = edgePoints[index_1]
            val p2 = edgePoints[index_2]
            val pt1 = converter.convertLatLng(p1)
            val pt2 = converter.convertLatLng(p2)
            if (pt1.distance(pt2) < 3) {
                err = ErrorMessageEnum.ERROR_CODE_DISTANCE_BLOCK
                break
            }
        }
        for (obstacle in obstaclePoints) {
            if (obstacle.size < 2) continue
            for (i in 0 until obstacle.size) {
                val index_1 = i
                val index_2 = (i + 1) % obstacle.size
                val p1 = obstacle[index_1]
                val p2 = obstacle[index_2]
                val pt1 = converter.convertLatLng(p1)
                val pt2 = converter.convertLatLng(p2)
                if (pt1.distance(pt2) < 3) {
                    err = ErrorMessageEnum.ERROR_CODE_DISTANCE_OBSTACLE
                    break
                }
            }
        }
        return err
    }

    fun clickMapBlock(blocks: List<String>) {
        if (pointType == PointTypeEnum.OBSTACLE) {
            //障碍点校验不通过，弹窗提示，不进行后续操作,校验通过执行后续操作
            showObstacleErrorDialog = !checkObstaclePoints()
            if (showObstacleErrorDialog) {
                return
            }
        }
        if (obstaclePoints[curObstacle].isNotEmpty()) {
            obstaclePoints.add(mutableListOf())
            curObstacle = obstaclePoints.lastIndex
        }
        clearMarker()
        lineFlow.value = emptyList()

        blocks.firstOrNull { it.startsWith("post") }?.let { n ->
            changePointType(PointTypeEnum.CIRCLE_OBSTACLE)
            if (circleObstaclePoints.isNotEmpty()) {
                exeTask {
                    val centerList = circleObstaclePoints.map { it.center }
                    pushLine(centerList)
                }
            }
            return
        }
        //障碍物
        blocks.firstOrNull { it.startsWith("barrier") }?.let { n ->
            val idx = n.removePrefix("barrier").toInt()
            changePointType(PointTypeEnum.OBSTACLE)
            curObstacle = if (idx < 0) obstaclePoints.lastIndex else idx
            val obstaclePoint = obstaclePoints[curObstacle]
            exeTask {
                pushLine(obstaclePoint)
            }
            return
        }
        //边界点
        if ("boundary" in blocks) {
            changePointType(PointTypeEnum.EDGE)
            exeTask { pushLine(edgePoints.toList()) }
        }
    }

    fun markerSelect(mIdx: Int) {
        if (mIdx < 0) {
            return
        }
        var markerPosition: GeoHelper.LatLngAlt? = null
        var radius = config.poleRadius.toDouble()
        when (pointType) {
            PointTypeEnum.EDGE -> {
                if (edgePoints.isNotEmpty()) {
                    markerPosition = edgePoints[mIdx]
                }
            }

            PointTypeEnum.OBSTACLE -> {
                if (obstaclePoints.isNotEmpty() && obstaclePoints.size > curObstacle && obstaclePoints[curObstacle].size > mIdx) {
                    markerPosition = obstaclePoints[curObstacle][mIdx]
                }
            }

            PointTypeEnum.CIRCLE_OBSTACLE -> {
                selectedMarkerIndexFlow.value =
                    -1 //设置为-1，因为圆形障碍点每个marker 的index都是0，flow在相同值时不会触发，所以先设置成-1
                //如果当前圆形障碍物修改过radius则使用当前值
                if (circleObstaclePoints.isNotEmpty() && circleObstaclePoints.size > mIdx) {
                    val circleObstacle = circleObstaclePoints[mIdx]
                    radius = circleObstacle.radius.toDouble()
                    markerPosition = circleObstacle.center
                }
            }
        }
        markerPosition?.let {
            changeSelectedMarkerIndex(mIdx)
            showMarkerPanel = true
            initMarker(it, radius)
        }
    }

    //点击marker,删除点
    fun removePointByIndex() {
        when (pointType) {
            PointTypeEnum.EDGE -> {
                edgePoints.removeAt(selectedMarkerIndex)
                exeTask {
                    pushEdgePoint()
                }
            }

            PointTypeEnum.OBSTACLE -> {
                val obstaclePoint = obstaclePoints[curObstacle]
                if (obstaclePoint.size > selectedMarkerIndex) {
                    obstaclePoint.removeAt(selectedMarkerIndex)
                    if (obstaclePoints[curObstacle].isEmpty()) {
                        if (obstaclePoints.size > 1) {
                            obstaclePoints.removeAt(curObstacle)
                            curObstacle = obstaclePoints.lastIndex
                        }
                    }
                    exeTask {
                        pushObstaclePoint()
                    }
                }
            }

            PointTypeEnum.CIRCLE_OBSTACLE -> {
                if (circleObstaclePoints.size > selectedMarkerIndex) {
                    circleObstaclePoints.removeAt(selectedMarkerIndex)
                    exeTask {
                        pushCircleObstaclePoint()
                    }
                }
            }
        }
        clearMarker()
    }

    fun changeMarkerPoint(isNext: Boolean = false) {
        var markerPosition: GeoHelper.LatLngAlt? = null
        var radius = config.poleRadius.toDouble()
        var curIdx = selectedMarkerIndex

        when (pointType) {
            PointTypeEnum.EDGE -> handleEdgePoints(isNext, curIdx).also { (newIdx, pos) ->
                curIdx = newIdx
                markerPosition = pos
            }

            PointTypeEnum.OBSTACLE -> handleObstaclePoints(isNext, curIdx).also { (newIdx, pos) ->
                curIdx = newIdx
                markerPosition = pos
            }

            PointTypeEnum.CIRCLE_OBSTACLE -> handleCircleObstacles(
                isNext,
                curIdx
            ).also { (newIdx, pos, rad) ->
                curIdx = newIdx
                markerPosition = pos
                radius = rad
            }
        }

        markerPosition?.let {
            changeSelectedMarkerIndex(curIdx)
            initMarker(it, radius)
        }
    }

    private fun handleEdgePoints(
        isNext: Boolean,
        currentIdx: Int,
    ): Pair<Int, GeoHelper.LatLngAlt?> {
        if (edgePoints.size <= 1) return currentIdx to null
        val newIdx = calculateNewIndex(currentIdx, edgePoints.lastIndex, isNext)
        return newIdx to edgePoints[newIdx]
    }

    private fun handleObstaclePoints(
        isNext: Boolean,
        currentIdx: Int,
    ): Pair<Int, GeoHelper.LatLngAlt?> {
        val obstacleGroup = obstaclePoints.getOrNull(curObstacle)
        if (obstacleGroup == null || obstacleGroup.size <= 1) return currentIdx to null
        val newIdx = calculateNewIndex(currentIdx, obstacleGroup.lastIndex, isNext)
        return newIdx to obstacleGroup[newIdx]
    }

    private fun handleCircleObstacles(
        isNext: Boolean,
        currentIdx: Int,
    ): Triple<Int, GeoHelper.LatLngAlt?, Double> {
        val circles = circleObstaclePoints
        var newIdx = currentIdx
        var radius = config.poleRadius.toDouble()

        if (circles.size > 1) {
            newIdx = calculateNewIndex(currentIdx, circles.lastIndex, isNext)
        }

        return Triple(
            newIdx,
            circles.getOrNull(newIdx)?.center,
            circles.getOrNull(newIdx)?.radius?.toDouble() ?: radius
        )
    }

    private fun calculateNewIndex(current: Int, maxIndex: Int, isNext: Boolean): Int {
        return when {
            current == 0 -> if (isNext) 1 else maxIndex
            current == maxIndex -> if (isNext) 0 else maxIndex - 1
            else -> current + if (isNext) 1 else -1
        }
    }

    fun updateCircleObstaclePointRadius(radius: Float) {
        circleObstaclePoints[selectedMarkerIndex].radius = radius.toFloat()
        markerRadius = radius.toDouble()
        exeTask {
            pushCircleObstaclePoint()
        }
    }
}

enum class ErrorMessageEnum() {
    NONE, ERROR_CODE_CROSS, ERROR_CODE_CROSS_OBSTACLE, ERROR_CODE_BARRIER, ERROR_CODE_DISTANCE, ERROR_CODE_DISTANCE_OBSTACLE, ERROR_CODE_DISTANCE_BLOCK, ERROR_CODE_POINT_COUNT, ERROR_CODE_MAX_AREA
}