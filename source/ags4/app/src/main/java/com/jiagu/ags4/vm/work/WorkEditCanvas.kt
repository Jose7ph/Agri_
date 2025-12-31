package com.jiagu.ags4.vm.work

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapRing
import kotlinx.coroutines.flow.MutableStateFlow


interface IWorkEditCanvas {
    val points: SnapshotStateList<GeoHelper.LatLngAlt>
    val pointsFlow: MutableStateFlow<MapRing>
    fun addPoint(pt: GeoHelper.LatLngAlt)
    fun addPoints(pts: List<GeoHelper.LatLngAlt>)
    fun clearAllPoint()
    fun clearLastPoint()
    fun removePoint(index: Int)
    fun pushCanvasData(points: List<GeoHelper.LatLngAlt>)
}

class WorkEditCanvasImpl : IWorkEditCanvas {
    override val points = mutableStateListOf<GeoHelper.LatLngAlt>()
    override val pointsFlow = MutableStateFlow<MapRing>(emptyList())

    override fun addPoint(pt: GeoHelper.LatLngAlt) {
        points.add(pt)
        pushCanvasData(points)
    }

    override fun addPoints(pts: List<GeoHelper.LatLngAlt>) {
        points.addAll(pts)
        pushCanvasData(points)
    }

    override fun clearAllPoint() {
        points.clear()
        pushCanvasData(emptyList())
    }

    override fun clearLastPoint() {
        if (points.isEmpty()) return
        points.removeAt(points.size - 1)
        pushCanvasData(points)
    }

    override fun removePoint(index: Int) {
        if (index < 0 || index >= points.size) return
        points.removeAt(index)
        pushCanvasData(points)
    }

    override fun pushCanvasData(points: List<GeoHelper.LatLngAlt>) {
        pointsFlow.value = points.toList()
    }
}