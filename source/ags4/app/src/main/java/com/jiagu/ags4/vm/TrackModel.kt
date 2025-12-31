package com.jiagu.ags4.vm

import androidx.lifecycle.ViewModel
import com.jiagu.ags4.utils.exeComplexTask
import com.jiagu.ags4.utils.exeTask
import com.jiagu.api.helper.GeoHelper
import com.jiagu.tools.math.GeometryHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs

class TrackModel: ViewModel() {

    companion object {
        const val OPTIM_SIZE = 100
        const val OPTIM_THRES = 0.3
    }

    val track = MutableStateFlow<List<TrackTotal>>(listOf())
    class TrackTotal(val pump: Boolean, val track: MutableList<GeoHelper.LatLng>, var index: Int = 0)

    private var activated: TrackTotal? = null
    private val optimized = mutableListOf<TrackTotal>()
    private var completed: TrackTotal? = null
    private var optimizing = false
    private var index = 1

    fun addPoint(lat: Double, lng: Double, pump: Boolean) {
        val s = activated
        if (s == null) {
            val t = mutableListOf(GeoHelper.LatLng(lat, lng))
            activated = TrackTotal(pump, t)
            exeTask { complete() }
            return
        }

        val t = s.track.last()
        val latDif = abs(lat - t.latitude)
        val lngDif = abs(lng - t.longitude)
        if (latDif + lngDif < 2E-6) {
            return
        }

        val lt = GeoHelper.LatLng(lat, lng)
        if (s.pump != pump) {//喷->不喷/不喷->喷
            if (!optimizing) {
                optimizing = true
                index += 1
                val ls = s.track.last()
                activated = TrackTotal(pump, mutableListOf(ls, lt), index = index)//目标点改变/换行时，添加上一条线的最后一个点，由于相近的点会直接跳过，会导致漏掉几个点，而不连续
                optimizeTrack(s)
            }
        } else {//不变
            s.track.add(lt)
            exeTask { complete() }
        }
    }

    private suspend fun complete() {
        if (optimizing) return
        val out = mutableListOf<TrackTotal>()
        if (completed != null) out.add(completed!!)
        out.addAll(optimized)
        activated?.let {
            val s = mutableListOf<GeoHelper.LatLng>()
            s.addAll(it.track)
            out.add(TrackTotal(it.pump, s, it.index))
        }
        track.emit(out)
    }

    fun clear() {
        exeTask {
            activated = null
            completed = null
            optimized.clear()
            track.emit(listOf())
        }
    }

    private fun optimizeTrack(t: TrackTotal) {
        exeComplexTask {
            val list = t.track
            val firstPoint = list[0]
            list.removeAt(0)
            if (list.isNotEmpty()) {
                val converter = GeoHelper.GeoCoordConverter()
                val out = GeometryHelper.simplifyMapRing(list, converter, OPTIM_THRES)
                val outM = out.toMutableList()
                outM.add(0, firstPoint)
                optimized.add(TrackTotal(t.pump, outM, t.index))
            }
            if (optimized.size >= OPTIM_SIZE) {
                optimized.removeAt(0)
            }
            optimizing = false
            complete()
        }
    }
}
