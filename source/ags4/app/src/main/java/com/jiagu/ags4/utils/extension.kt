package com.jiagu.ags4.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.jiagu.ags4.bean.Battery
import com.jiagu.ags4.bean.PlanParamInfo
import com.jiagu.ags4.bean.SortieAdditional
import com.jiagu.ags4.bean.SortieRoute
import com.jiagu.ags4.bean.TrackNode
import com.jiagu.ags4.bean.WorkRoute
import com.jiagu.ags4.repo.net.model.SortieRouteInfo
import com.jiagu.api.ext.setParams
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.model.MapBlock3D
import com.jiagu.api.model.MapRing3D
import com.jiagu.api.viewmodel.BtDeviceModel
import com.jiagu.api.viewmodel.ProgressModel
import com.jiagu.api.viewmodel.ProgressTask
import com.jiagu.device.model.RoutePoint
import com.jiagu.device.vkprotocol.VKAg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

val LocalProgressModel = compositionLocalOf<ProgressModel> { error("No ProgressModel provided") }

val LocalBtDeviceModel = compositionLocalOf<BtDeviceModel> {
    error("No BtDeviceModel provided")
}

fun <T : Activity> Activity.startActivity(cls: Class<T>, vararg params: Pair<String, Any>) {
    val intent = Intent(this, cls)
    intent.setParams(params)
    startActivity(intent)
}

fun ViewModel.exeIoTask(action: suspend () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
        action()
    }
}

fun ViewModel.exeComplexTask(action: suspend () -> Unit) {
    viewModelScope.launch(Dispatchers.Default) {
        action()
    }
}

fun ViewModel.exeTask(action: suspend () -> Unit) {
    viewModelScope.launch {
        action()
    }
}

/**
 * start task
 *
 */
var taskComplete: ((Boolean, String?) -> Boolean)? = null
fun Context.startProgress(task: ProgressTask, block: ((Boolean, String?) -> Boolean)? = null) {
    val activity = when (this) {
        is AppCompatActivity -> this
        is ComponentActivity -> this
        else -> return
    }
    val viewModel = ViewModelProvider(activity)[ProgressModel::class.java]
    taskComplete = block
    viewModel.start(task)
}

fun GeoHelper.LatLngAlt.format(): String {
    return String.format(Locale.US, "%f %f %f", longitude, latitude, altitude)
}

fun formatMapRing3D(b: MapRing3D): String {
    return b.joinToString(",") { it.format() }
}

fun formatMapBlock3D(b: MapBlock3D): String {
    return b.joinToString("|") { formatMapRing3D(it) }
}

fun parseLatLngAlt(s: String): GeoHelper.LatLngAlt {
    val ss = s.split(" ")
    return GeoHelper.LatLngAlt(ss[1].toDouble(), ss[0].toDouble(), ss[2].toDouble())
}

fun parseMapRing3D(s: String): MapRing3D {
    val ss = s.split(",")
    val out = ArrayList<GeoHelper.LatLngAlt>(ss.size)
    for (t in ss) {
        out.add(parseLatLngAlt(t))
    }
    return out
}

fun parseMapBlock3D(s: String): MapBlock3D {
    val ss = s.split("|")
    val out = ArrayList<MapRing3D>(ss.size)
    for (t in ss) {
        out.add(parseMapRing3D(t))
    }
    return out
}

fun formatRoutePoint(r: List<RoutePoint>?): String {
    if (r == null) return ""
    return r.joinToString("|") { it.toString() }
}

fun parseRoutePoint(s: String): List<RoutePoint> {
    val ss = if (s.contains("{")) Gson().fromJson(s, SortieRouteInfo::class.java).route ?: listOf()
    else s.split("|")
    val out = mutableListOf<RoutePoint>()
    for (t in ss) {
        out.add(RoutePoint.fromString(t))
    }
    return out
}

fun routePointToListString(data: List<RoutePoint>): List<String> {
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out
}

fun formatPlanParamInfo(r: PlanParamInfo): String {
    return r.toString()
}

fun parsePlanParamInfo(s: String): PlanParamInfo {
    return PlanParamInfo.fromString(s)
}

fun trackNodeToArray(data: List<TrackNode>?): String? {
    if (data == null) return null
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out.joinToString("|")
}

fun trackNodeToList(data: List<TrackNode>): List<String> {
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out
}

fun parseTrackNode(s: String?): List<TrackNode> {
    if (s.isNullOrBlank()) return listOf()
    val ss = s.split("|")
    val out = mutableListOf<TrackNode>()
    for (t in ss) {
        out.add(TrackNode.fromString(t))
    }
    return out
}

fun batteryToArray(data: List<Battery>?): String? {
    if (data == null) return null
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out.joinToString("|")
}

fun batteryToList(data: List<Battery>): List<String> {
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out
}

fun parseBattery(s: String?): List<Battery> {
    if (s.isNullOrBlank()) return listOf()
    val ss = s.split("|")
    val out = mutableListOf<Battery>()
    for (t in ss) {
        out.add(Battery.fromString(t))
    }
    return out
}

fun posToString(data: List<VKAg.POSData>?): String? {
    if (data == null) return null
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out.joinToString("|")
}

fun posToListString(data: List<VKAg.POSData>): List<String> {
    val out = mutableListOf<String>()
    for (s in data) {
        val rp = s.toString()
        out.add(rp)
    }
    return out

}

fun parsePosData(s: String?): List<VKAg.POSData> {
    if (s == null) return listOf()
    val ss = s.split("|")
    val out = mutableListOf<VKAg.POSData>()
    for (t in ss) {
        out.add(VKAg.POSData.fromString(t))
    }
    return out
}

fun sortieAdditionalToString(data: SortieAdditional?): String? {
    return SortieAdditional.additionalToString(data)
}

fun parseSortieAdditional(s: String?): SortieAdditional? {
    return if (s == null) null else SortieAdditional.fromString(s)
}

fun parseSortieRoute(s: String): SortieRoute? {
    return if (s.isBlank()) null else Gson().fromJson(s, SortieRoute::class.java)
}

fun sortieRouteToString(data: SortieRoute?): String {
    return if (data == null) "" else Gson().toJson(data)
}

fun workRouteToString(data: WorkRoute?): String? {
    return data?.toString()
}

fun stringToWorkRoute(s: String?): WorkRoute? {
    return if (s == null) null else WorkRoute.fromString(s)
}

fun sortieRouteInfoToSortieRoute(data: SortieRouteInfo?): SortieRoute {
    return if (data?.route == null) SortieRoute(listOf())
    else {
        val out = mutableListOf<RoutePoint>()
        for (d in data.route) {
            out.add(RoutePoint.fromString(d))
        }
        SortieRoute(out)
    }
}

fun optimizeRoute(pts: List<RoutePoint>): List<RoutePoint> {
    val out = mutableListOf<RoutePoint>()
    var idx = 0
    while (idx < pts.size) {
        val p = if (idx == pts.size - 1) pts[idx]
        else {
            val p1 = pts[idx]
            val p2 = pts[idx + 1]
            if (abs(p1.latitude - p2.latitude) > 1E-6 || abs(p1.longitude - p2.longitude) > 1E-6) p1
            else {
                idx++
                p2
            }
        }
        idx++
        out.add(p)
    }
    return out
}