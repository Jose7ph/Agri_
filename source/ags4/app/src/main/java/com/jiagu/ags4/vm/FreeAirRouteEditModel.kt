package com.jiagu.ags4.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.jiagu.ags4.bean.Block
import com.jiagu.ags4.bean.BlockPlan
import com.jiagu.ags4.utils.exeTask
import com.jiagu.ags4.utils.mergeAlt
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.IWorkMapMarker
import com.jiagu.ags4.vm.com.jiagu.ags4.vm.work.WorkMapMarkerImpl
import com.jiagu.ags4.vm.work.IWorkEdit
import com.jiagu.ags4.vm.work.IWorkEditCanvas
import com.jiagu.ags4.vm.work.IWorkKml
import com.jiagu.ags4.vm.work.WorkEditCanvasImpl
import com.jiagu.ags4.vm.work.WorkEditImpl
import com.jiagu.ags4.vm.work.WorkKmlImpl
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D

class FreeAirRouteEditModel(app: Application) : AndroidViewModel(app),
    IWorkEditCanvas by WorkEditCanvasImpl(),
    IWorkEdit by WorkEditImpl(),
    IWorkMapMarker by WorkMapMarkerImpl(),
    IWorkKml by WorkKmlImpl() {
    val context = getApplication<Application>()
    var editBlock: BlockPlan? = null
    private val converter = GeoHelper.GeoCoordConverter()

    //总长度
    var length by mutableFloatStateOf(0f)

    //dialog control
    var showBluetoothList by mutableStateOf(false) //蓝牙列表弹窗

    var canvasFit = false

    //编辑状态初始化当前blockplan
    fun initEditBlockPlan(blockPlan: BlockPlan) {
        editBlock = blockPlan
        val boundary = blockPlan.boundary
        if (boundary.isNotEmpty()) {
            addPoints(boundary[0])
        }
    }

    //导入kml地块
    fun importKmlTrack() {
        kmlTrack?.let {
            val track = it.first
            val trackAlt = it.second
            if (track.isNotEmpty()) {
                addPoints(mergeAlt(track, trackAlt))
            }
            calcLength()
            canvasFit = true
        }
    }

    fun addTrack(pt: GeoHelper.LatLngAlt) {
        if (points.isNotEmpty()) {
            val p1 = converter.convertLatLng(pt)
            val p0 = converter.convertLatLng(points.last())
            if (p1.distance(p0) < 3) return
        }
        addPoint(pt)
        calcLength()
    }

    fun clearTrack(isAll: Boolean = false) {
        if (isAll) {
            clearAllPoint()
        } else {
            clearLastPoint()
        }
        calcLength()
    }

    //点击marker drag 和 移动点时调用
    fun updatePoint(pt: GeoHelper.LatLngAlt) {
        if (selectedMarkerIndex >= points.size) return
        points[selectedMarkerIndex] = pt
        pushCanvasData(points)
    }

    fun saveBlock(name: String, complete: (List<Long>) -> Unit) {
        val block = Block(
            Block.TYPE_TRACK,
            name,
            boundary = listOf(points),
            calibPoints = doubleArrayOf(),
            area = 0f,
            groupId = DroneModel.groupId
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

    fun markerSelect(mIdx: Int) {
        if (mIdx < 0) {
            return
        }
        var markerPosition = points[mIdx]
        changeSelectedMarkerIndex(mIdx)
        showMarkerPanel = true
        initMarker(markerPosition)
    }

    //点击marker,删除点
    fun removePointByIndex() {
        removePoint(selectedMarkerIndex)
        clearMarker()
    }

    fun changeMarkerPoint(isNext: Boolean = false) {
        var markerPosition: GeoHelper.LatLngAlt? = null
        var curIdx = selectedMarkerIndex
        if (points.size > 1) {
            curIdx = when (selectedMarkerIndex) {
                points.lastIndex -> { //最后一个索引
                    if (isNext) 0 else curIdx - 1
                }

                0 -> {//第一个索引
                    if (isNext) curIdx + 1 else points.lastIndex
                }

                else -> { //中间
                    if (isNext) curIdx + 1 else curIdx - 1
                }
            }
            markerPosition = points[curIdx]
        }
        markerPosition?.let {
            changeSelectedMarkerIndex(curIdx)
            initMarker(it)
        }
    }

    private fun calcLength() {
        if (points.size < 2) {
            length = 0f
            return
        }
        val mpts = mutableListOf<Point2D>()
        converter.convertLatLng(points as Collection<GeoHelper.LatLng>?, mpts)
        var total = 0.0
        for (i in 0 until points.size - 1) {
            total += mpts[i].distance(mpts[i + 1])
        }
        length = total.toFloat()
        Log.d("zhy", "calc: length=${length}")
    }
}